package com.bentahsin.antiafk.commands.pattern;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.learning.Pattern;
import com.bentahsin.antiafk.learning.RecordingManager;
import com.bentahsin.antiafk.learning.serialization.ISerializer;
import com.bentahsin.antiafk.learning.serialization.JsonPatternSerializer;
import com.bentahsin.antiafk.learning.serialization.KryoPatternSerializer;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * /antiafk pattern ... komutlarını yöneten sınıf.
 * ACF kullanılarak yeniden yazılmıştır.
 * İçerdiği alt komutlar:
 * - record: Oyuncu hareketlerini kaydeder.
 * - list: Kayıtlı desenleri listeler.
 * - manage: Desenleri siler, taşır veya format dönüştürür.
 */
@Singleton
@CommandAlias("%antiafk_cmd")
@Subcommand("pattern")
@CommandPermission("antiafk.admin.pattern")
@SuppressWarnings("unused")
public class PatternCommand extends BaseCommand {

    private final AntiAFKPlugin plugin;
    private final RecordingManager recordingManager;
    private final PlayerLanguageManager lang;
    private final SystemLanguageManager sysLang;
    private final ConfigManager configManager;

    @Inject
    public PatternCommand(
            AntiAFKPlugin plugin,
            RecordingManager recordingManager,
            PlayerLanguageManager lang,
            SystemLanguageManager sysLang,
            ConfigManager configManager
    ) {
        this.plugin = plugin;
        this.recordingManager = recordingManager;
        this.lang = lang;
        this.sysLang = sysLang;
        this.configManager = configManager;
    }

    /**
     * Varsayılan yardım mesajı.
     */
    @Default
    @HelpCommand
    public void onHelp(CommandSender sender) {
        lang.sendMessage(sender, "command.pattern.help.header");
        lang.sendMessage(sender, "command.pattern.help.entry", "%usage%", "record <player> <start|save|cancel>");
        lang.sendMessage(sender, "command.pattern.help.entry", "%usage%", "list [records|known_routes]");
        lang.sendMessage(sender, "command.pattern.help.entry", "%usage%", "manage <delete|move> <folder> <pattern>");
        lang.sendMessage(sender, "command.pattern.help.entry", "%usage%", "manage transform <json|kryo> [folder]");
        lang.sendMessage(sender, "command.pattern.help.footer");
    }

    /**
     * /antiafk pattern record ...
     * Hareket kaydetme işlemlerini yönetir.
     */
    @Subcommand("record")
    @CommandPermission("antiafk.admin.pattern.record")
    @CommandCompletion("@players start|save|cancel @nothing json|kryo")
    @Syntax("<player> <start|save|cancel> [pattern_name] [format]")
    @Description("Bir oyuncunun hareketlerini desen olarak kaydeder.")
    public void onRecord(CommandSender sender, String targetName, String action, @Optional String patternName, @Default("json") String format) {
        if (!configManager.isLearningModeEnabled()) {
            lang.sendMessage(sender, "command.pattern.learning_mode_disabled");
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            lang.sendMessage(sender, "error.player_not_found");
            return;
        }

        switch (action.toLowerCase()) {
            case "start":
                if (recordingManager.startRecording(target)) {
                    lang.sendMessage(sender, "command.pattern.record.start.success", "%player%", target.getName());
                    lang.sendMessage(target, "command.pattern.record.start.notify_player");
                } else {
                    lang.sendMessage(sender, "command.pattern.record.start.already_recording", "%player%", target.getName());
                }
                break;

            case "save":
                if (patternName == null) {
                    lang.sendMessage(sender, "command.pattern.record.usage_save");
                    return;
                }
                if (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("kryo")) {
                    lang.sendMessage(sender, "command.pattern.record.save.invalid_format");
                    return;
                }

                if (recordingManager.stopRecording(target, patternName, format)) {
                    lang.sendMessage(sender, "command.pattern.record.save.success", "%pattern_name%", patternName);
                    lang.sendMessage(target, "command.pattern.record.save.notify_player");
                } else {
                    lang.sendMessage(sender, "command.pattern.record.save.not_recording_or_no_data", "%player%", target.getName());
                }
                break;

            case "cancel":
                if (recordingManager.cancelRecording(target)) {
                    lang.sendMessage(sender, "command.pattern.record.cancel.success", "%player%", target.getName());
                    lang.sendMessage(target, "command.pattern.record.cancel.notify_player");
                } else {
                    lang.sendMessage(sender, "command.pattern.record.cancel.not_recording", "%player%", target.getName());
                }
                break;

            default:
                lang.sendMessage(sender, "command.pattern.record.invalid_action");
                break;
        }
    }

    /**
     * /antiafk pattern list ...
     * Klasördeki desenleri listeler.
     */
    @Subcommand("list")
    @CommandPermission("antiafk.admin.pattern.list")
    @CommandCompletion("records|known_routes")
    @Syntax("[folder]")
    @Description("Kayıtlı desen dosyalarını listeler.")
    public void onList(CommandSender sender, @Default("records") String folderName) {
        File directory;
        if (folderName.equalsIgnoreCase("records")) {
            directory = new File(plugin.getDataFolder(), "records");
        } else if (folderName.equalsIgnoreCase("known_routes")) {
            directory = new File(plugin.getDataFolder(), "known_routes");
        } else {
            lang.sendMessage(sender, "command.pattern.manage.invalid_folder");
            return;
        }

        CompletableFuture.runAsync(() -> {
            if (!directory.exists() || !directory.isDirectory()) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        lang.sendMessage(sender, "command.pattern.list.folder_empty_or_not_found", "%folder%", folderName));
                return;
            }

            File[] files = directory.listFiles(f -> f.getName().endsWith(".pattern"));
            if (files == null || files.length == 0) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        lang.sendMessage(sender, "command.pattern.list.no_patterns_found", "%folder%", folderName));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                sender.sendMessage(lang.getMessage("command.pattern.list.header", "%folder%", folderName));
                for (File file : files) {
                    String fileName = file.getName().replace(".pattern", "");
                    String format = fileName.endsWith(".json") ? "JSON" : (fileName.endsWith(".kryo") ? "Kryo" : "Bilinmiyor");
                    String patternName = fileName.replace(".json", "").replace(".kryo", "");

                    sender.sendMessage(lang.getMessage("command.pattern.list.entry",
                            "%pattern_name%", patternName,
                            "%format%", format,
                            "%size%", String.valueOf(file.length() / 1024)
                    ));
                }
            });
        });
    }

    /**
     * /antiafk pattern manage <delete|move> ...
     * Dosya silme ve taşıma işlemleri.
     */
    @Subcommand("manage")
    @CommandPermission("antiafk.admin.pattern.manage")
    @CommandCompletion("delete|move records|known_routes @nothing")
    @Syntax("<action> <folder> <pattern_name>")
    @Description("Desen dosyalarını yönetir (silme, taşıma).")
    public void onManage(CommandSender sender, String action, String folderName, String patternName) {
        if (action.equalsIgnoreCase("transform")) {
            return;
        }

        File sourceDir;
        if (folderName.equalsIgnoreCase("records")) {
            sourceDir = new File(plugin.getDataFolder(), "records");
        } else if (folderName.equalsIgnoreCase("known_routes")) {
            sourceDir = new File(plugin.getDataFolder(), "known_routes");
        } else {
            lang.sendMessage(sender, "command.pattern.manage.invalid_folder");
            return;
        }

        CompletableFuture.runAsync(() -> {
            File patternFile = findPatternFile(sourceDir, patternName);

            if (patternFile == null) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        lang.sendMessage(sender, "command.pattern.manage.pattern_not_found", "%pattern%", patternName, "%folder%", folderName));
                return;
            }

            if (action.equalsIgnoreCase("delete")) {
                boolean deleted = patternFile.delete();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (deleted) {
                        lang.sendMessage(sender, "command.pattern.manage.delete.success", "%filename%", patternFile.getName());
                    } else {
                        lang.sendMessage(sender, "command.pattern.manage.delete.error", "%filename%", patternFile.getName());
                    }
                });
            } else if (action.equalsIgnoreCase("move")) {
                File targetDir = folderName.equalsIgnoreCase("records")
                        ? new File(plugin.getDataFolder(), "known_routes")
                        : new File(plugin.getDataFolder(), "records");

                if (!targetDir.exists()) {
                    boolean ignored = targetDir.mkdirs();
                }

                File destFile = new File(targetDir, patternFile.getName());
                try {
                    Files.move(patternFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        lang.sendMessage(sender, "command.pattern.manage.move.success", "%filename%", patternFile.getName(), "%target_folder%", targetDir.getName());
                        lang.sendMessage(sender, "command.pattern.reload_required");
                    });
                } catch (Exception e) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            lang.sendMessage(sender, "command.pattern.manage.move.error", "%error%", e.getMessage()));
                }
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> lang.sendMessage(sender, "command.pattern.manage.invalid_action"));
            }
        });
    }

    /**
     * /antiafk pattern manage transform ...
     * JSON <-> Kryo dönüşümü yapar.
     */
    @Subcommand("manage transform")
    @CommandPermission("antiafk.admin.pattern.manage")
    @CommandCompletion("json|kryo records|known_routes")
    @Syntax("<target_format> [folder]")
    @Description("Desen dosyalarının formatını dönüştürür.")
    public void onTransform(CommandSender sender, String targetFormat, @Default("records") String folderName) {
        if (!targetFormat.equalsIgnoreCase("json") && !targetFormat.equalsIgnoreCase("kryo")) {
            lang.sendMessage(sender, "command.pattern.manage.transform.invalid_format");
            return;
        }

        File directory = new File(plugin.getDataFolder(), folderName.toLowerCase());
        lang.sendMessage(sender, "command.pattern.manage.transform.in_progress", "%folder%", folderName, "%format%", targetFormat);

        CompletableFuture.supplyAsync(() -> {
            ISerializer targetSerializer = targetFormat.equalsIgnoreCase("kryo") ? new KryoPatternSerializer() : new JsonPatternSerializer();
            ISerializer jsonSerializer = new JsonPatternSerializer();
            ISerializer kryoSerializer = new KryoPatternSerializer();

            int convertedCount = 0;
            File[] files = directory.listFiles(f -> f.getName().endsWith(".pattern"));
            if (files == null) return 0;

            for (File file : files) {
                try {
                    String fileName = file.getName();
                    Pattern pattern;

                    if (fileName.endsWith(".json.pattern")) {
                        pattern = jsonSerializer.deserialize(Files.newInputStream(file.toPath()));
                    } else if (fileName.endsWith(".kryo.pattern")) {
                        pattern = kryoSerializer.deserialize(Files.newInputStream(file.toPath()));
                    } else {
                        continue;
                    }

                    String baseName = fileName.replace(".json.pattern", "").replace(".kryo.pattern", "");
                    File newFile = new File(directory, baseName + "." + targetSerializer.getFileExtension() + ".pattern");

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        targetSerializer.serialize(pattern, fos);
                    }

                    if (!file.equals(newFile)) {
                        boolean ignored = file.delete();
                    }
                    convertedCount++;
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, sysLang.getSystemMessage(Lang.PATTERN_TRANSFORM_FILE_ERROR, file.getName()), e);
                }
            }
            return convertedCount;
        }).whenComplete((count, ex) -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (ex != null) {
                    lang.sendMessage(sender, "command.pattern.manage.transform.error", "%error%", ex.getMessage());
                } else {
                    lang.sendMessage(sender, "command.pattern.manage.transform.success", "%count%", String.valueOf(count));
                    lang.sendMessage(sender, "command.pattern.reload_required");
                }
            });
        });
    }

    /**
     * Yardımcı Metot: Verilen klasörde ismi eşleşen (uzantısı ne olursa olsun) dosyayı bulur.
     */
    private File findPatternFile(File directory, String patternName) {
        File jsonFile = new File(directory, patternName + ".json.pattern");
        if (jsonFile.exists()) return jsonFile;

        File kryoFile = new File(directory, patternName + ".kryo.pattern");
        if (kryoFile.exists()) return kryoFile;

        return null;
    }
}