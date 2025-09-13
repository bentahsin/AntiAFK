package com.bentahsin.antiafk.commands.antiafk.pattern;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.learning.Pattern;
import com.bentahsin.antiafk.learning.serialization.ISerializer;
import com.bentahsin.antiafk.learning.serialization.JsonPatternSerializer;
import com.bentahsin.antiafk.learning.serialization.KryoPatternSerializer;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FileManagementPatternCommand implements IPatternSubCommand {

    private final AntiAFKPlugin plugin;
    private final Logger logger;
    private final PlayerLanguageManager plLang;
    private final SystemLanguageManager sysLang;

    public FileManagementPatternCommand(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.plLang = plugin.getPlayerLanguageManager();
        this.sysLang = plugin.getSystemLanguageManager();
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
            plLang.sendMessage(sender, "command.pattern.manage.usage");
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "transform":
                if (args.length < 2) {
                    plLang.sendMessage(sender, "command.pattern.manage.transform.usage");
                    return;
                }
                handleTransform(sender, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "delete":
            case "move":
                if (args.length < 3) {
                    plLang.sendMessage(sender, "command.pattern.manage.action.usage", "%action%", action);
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
                    plLang.sendMessage(sender, "command.pattern.manage.invalid_folder");
                    return;
                }

                File patternFile = findPatternFile(sourceDir, patternName);
                if (patternFile == null) {
                    plLang.sendMessage(sender, "command.pattern.manage.pattern_not_found", "%pattern%", patternName, "%folder%", folderName);
                    return;
                }

                if (action.equals("delete")) {
                    handleDelete(sender, patternFile);
                } else {
                    handleMove(sender, patternFile, folderName);
                }
                break;

            default:
                plLang.sendMessage(sender, "command.pattern.manage.invalid_action");
                break;
        }
    }

    private void handleTransform(CommandSender sender, String[] args) {
        if (args.length < 1) {
            plLang.sendMessage(sender, "command.pattern.manage.transform.usage");
            return;
        }

        String targetFormat = args[0].toLowerCase();
        if (!targetFormat.equals("json") && !targetFormat.equals("kryo")) {
            plLang.sendMessage(sender, "command.pattern.manage.transform.invalid_format");
            return;
        }

        String folderName = (args.length > 1) ? args[1].toLowerCase() : "records";
        File directory = new File(plugin.getDataFolder(), folderName);

        plLang.sendMessage(sender, "command.pattern.manage.transform.in_progress", "%folder%", folderName, "%format%", targetFormat);

        transformAll(directory, targetFormat).whenComplete((count, ex) -> {
            if (ex != null) {
                plLang.sendMessage(sender, "command.pattern.manage.transform.error", "%error%", ex.getMessage());
            } else {
                plLang.sendMessage(sender, "command.pattern.manage.transform.success", "%count%", String.valueOf(count));
                plLang.sendMessage(sender, "command.pattern.reload_required");
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
                        boolean ignored = file.delete();
                    }
                    convertedCount++;
                } catch (Exception e) {
                    logger.log(Level.WARNING, sysLang.getSystemMessage(
                            Lang.PATTERN_TRANSFORM_FILE_ERROR,
                            file.getName()
                    ), e);
                }
            }
            return convertedCount;
        });
    }

    private void handleDelete(CommandSender sender, File file) {
        if (file.delete()) {
            plLang.sendMessage(sender, "command.pattern.manage.delete.success", "%filename%", file.getName());
        } else {
            plLang.sendMessage(sender, "command.pattern.manage.delete.error", "%filename%", file.getName());
        }
    }

    private void handleMove(CommandSender sender, File sourceFile, String sourceFolderName) {
        File targetDir;
        if (sourceFolderName.equals("records")) {
            targetDir = new File(plugin.getDataFolder(), "known_routes");
        } else {
            targetDir = new File(plugin.getDataFolder(), "records");
        }
        if (!targetDir.exists()) { boolean ignored = targetDir.mkdirs(); }

        File destFile = new File(targetDir, sourceFile.getName());

        try {
            Files.move(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plLang.sendMessage(sender, "command.pattern.manage.move.success", "%filename%", sourceFile.getName(), "%target_folder%", targetDir.getName());
            plLang.sendMessage(sender, "command.pattern.reload_required");
        } catch (Exception e) {
            plLang.sendMessage(sender, "command.pattern.manage.move.error", "%error%", e.getMessage());
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