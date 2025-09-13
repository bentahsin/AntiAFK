package com.bentahsin.antiafk.commands.antiafk.pattern;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.learning.Pattern;
import com.bentahsin.antiafk.learning.serialization.ISerializer;
import com.bentahsin.antiafk.learning.serialization.JsonPatternSerializer;
import com.bentahsin.antiafk.learning.serialization.KryoPatternSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FileManagementPatternCommand implements IPatternSubCommand {

    private final AntiAFKPlugin plugin;

    public FileManagementPatternCommand(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "manage";
    }

    @Override
    public String getUsage() {
        return "manage <delete|move|transform> ...";
    }

    @Override
    public String getPermission() {
        return "antiafk.admin.pattern.manage";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cKullanım: /antiafk pattern " + getUsage());
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "transform":
                if (args.length < 2) {
                    sender.sendMessage("§cKullanım: /antiafk pattern manage transform <hedef_format> [klasör]");
                    return;
                }
                handleTransform(sender, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "delete":
            case "move":
                if (args.length < 3) {
                    sender.sendMessage("§cKullanım: /antiafk pattern manage " + action + " <klasör> <desen_adı>");
                    return;
                }

                String folderName = args[1].toLowerCase();
                String patternName = args[2];

                File sourceDir;
                if (folderName.equals("records")) {
                    sourceDir = new File(plugin.getDataFolder(), "records");
                } else if (folderName.equals("known_routes")) {
                    sourceDir = new File(plugin.getDataFolder(), "known_routes");
                } else {
                    sender.sendMessage("§cGeçersiz klasör. Kullanılabilir: records, known_routes");
                    return;
                }

                File patternFile = findPatternFile(sourceDir, patternName);
                if (patternFile == null) {
                    sender.sendMessage("§c'" + patternName + "' adında bir desen '" + folderName + "' klasöründe bulunamadı.");
                    return;
                }

                if (action.equals("delete")) {
                    handleDelete(sender, patternFile);
                } else {
                    handleMove(sender, patternFile, folderName);
                }
                break;

            default:
                sender.sendMessage("§cGeçersiz eylem. Kullanılabilir: delete, move, transform");
                break;
        }
    }

    private void handleTransform(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cKullanım: /antiafk pattern manage transform <hedef_format> [klasör]");
            return;
        }

        String targetFormat = args[0].toLowerCase();
        if (!targetFormat.equals("json") && !targetFormat.equals("kryo")) {
            sender.sendMessage("§cGeçersiz hedef format. Kullanılabilir: json, kryo");
            return;
        }

        String folderName = (args.length > 1) ? args[1].toLowerCase() : "records";
        File directory = new File(plugin.getDataFolder(), folderName);

        sender.sendMessage("§e'" + folderName + "' klasöründeki desenler '" + targetFormat + "' formatına dönüştürülüyor...");

        transformAll(directory, targetFormat).whenComplete((count, ex) -> {
            if (ex != null) {
                sender.sendMessage("§cDönüştürme sırasında bir hata oluştu: " + ex.getMessage());
            } else {
                sender.sendMessage("§aBaşarılı! " + count + " adet desen dosyası dönüştürüldü.");
                sender.sendMessage("§eDeğişikliklerin etkili olması için '/antiafk reload' kullanın.");
            }
        });
    }

    private CompletableFuture<Integer> transformAll(File directory, String targetFormat) {
        return CompletableFuture.supplyAsync(() -> {
            ISerializer targetSerializer = targetFormat.equals("kryo") ? new KryoPatternSerializer() : new JsonPatternSerializer();
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
                        pattern = jsonSerializer.deserialize( Files.newInputStream(file.toPath()));
                    } else if (fileName.endsWith(".kryo.pattern")) {
                        pattern = kryoSerializer.deserialize(Files.newInputStream(file.toPath()));
                    } else {
                        continue;
                    }

                    File newFile = new File(directory, pattern.getName() + "." + targetSerializer.getFileExtension() + ".pattern");
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        targetSerializer.serialize(pattern, fos);
                    }

                    if (!file.equals(newFile)) {
                        file.delete();
                    }
                    convertedCount++;
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Could not transform pattern file: " + file.getName(), e);
                }
            }
            return convertedCount;
        });
    }

    private void handleDelete(CommandSender sender, File file) {
        if (file.delete()) {
            sender.sendMessage("§a'" + file.getName() + "' deseni başarıyla silindi.");
        } else {
            sender.sendMessage("§c'" + file.getName() + "' deseni silinirken bir hata oluştu.");
        }
    }

    private void handleMove(CommandSender sender, File sourceFile, String sourceFolderName) {
        File targetDir;
        if (sourceFolderName.equals("records")) {
            targetDir = new File(plugin.getDataFolder(), "known_routes");
        } else {
            targetDir = new File(plugin.getDataFolder(), "records");
        }
        if (!targetDir.exists()) targetDir.mkdirs();

        File destFile = new File(targetDir, sourceFile.getName());

        try {
            Files.move(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            sender.sendMessage("§a'" + sourceFile.getName() + "' deseni '" + targetDir.getName() + "' klasörüne taşındı.");
            sender.sendMessage("§eDeğişikliklerin etkili olması için '/antiafk reload' kullanın.");
        } catch (Exception e) {
            sender.sendMessage("§cDosya taşınırken bir hata oluştu: " + e.getMessage());
        }
    }

    private File findPatternFile(File directory, String patternName) {
        File jsonFile = new File(directory, patternName + ".json.pattern");
        if (jsonFile.exists()) return jsonFile;

        File kryoFile = new File(directory, patternName + ".kryo.pattern");
        if (kryoFile.exists()) return kryoFile;

        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("delete", "move", "transform"), new ArrayList<>());
        }

        String action = args[0].toLowerCase();

        if (action.equals("transform")) {
            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("json", "kryo"), new ArrayList<>());
            }
            if (args.length == 3) {
                return StringUtil.copyPartialMatches(args[2], Arrays.asList("records", "known_routes"), new ArrayList<>());
            }
        }

        if (action.equals("delete") || action.equals("move")) {
            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("records", "known_routes"), new ArrayList<>());
            }

            if (args.length == 3) {
                String folderName = args[1].toLowerCase();
                File directory;

                if (folderName.equals("records")) {
                    directory = new File(plugin.getDataFolder(), "records");
                } else if (folderName.equals("known_routes")) {
                    directory = new File(plugin.getDataFolder(), "known_routes");
                } else {
                    return Collections.emptyList();
                }

                if (!directory.exists() || !directory.isDirectory()) {
                    return Collections.emptyList();
                }

                File[] files = directory.listFiles(f -> f.getName().endsWith(".pattern"));
                if (files == null) {
                    return Collections.emptyList();
                }

                List<String> patternNames = Arrays.stream(files)
                        .map(file -> file.getName()
                                .replace(".json.pattern", "")
                                .replace(".kryo.pattern", ""))
                        .distinct()
                        .collect(Collectors.toList());

                return StringUtil.copyPartialMatches(args[2], patternNames, new ArrayList<>());
            }
        }

        return Collections.emptyList();
    }
}