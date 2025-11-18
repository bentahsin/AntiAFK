package com.bentahsin.antiafk.commands.antiafk.pattern;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Singleton
public class ListPatternCommand implements IPatternSubCommand {

    private final AntiAFKPlugin plugin;
    private final PlayerLanguageManager plLang;

    @Inject
    public ListPatternCommand(AntiAFKPlugin plugin, PlayerLanguageManager plLang) {
        this.plugin = plugin;
        this.plLang = plLang;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getUsage() {
        return "list [records|known_routes]";
    }

    @Override
    public String getPermission() {
        return "antiafk.admin.pattern.list";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String folderName = (args.length > 0) ? args[0].toLowerCase() : "records";
        File directory;

        if (folderName.equals("records")) {
            directory = new File(plugin.getDataFolder(), "records");
        } else if (folderName.equals("known_routes")) {
            directory = new File(plugin.getDataFolder(), "known_routes");
        } else {
            plLang.sendMessage(sender, "command.pattern.manage.invalid_folder");
            return;
        }

        if (!directory.exists() || !directory.isDirectory()) {
            plLang.sendMessage(sender, "command.pattern.list.folder_empty_or_not_found", "%folder%", folderName);
            return;
        }

        File[] files = directory.listFiles(f -> f.getName().endsWith(".pattern"));
        if (files == null || files.length == 0) {
            plLang.sendMessage(sender, "command.pattern.list.no_patterns_found", "%folder%", folderName);
            return;
        }

        sender.sendMessage(plLang.getMessage("command.pattern.list.header", "%folder%", folderName));
        for (File file : files) {
            String fileName = file.getName().replace(".pattern", "");
            String format = fileName.endsWith(".json") ? "JSON" : (fileName.endsWith(".kryo") ? "Kryo" : "Bilinmiyor");
            String patternName = fileName.replace(".json", "").replace(".kryo", "");

            sender.sendMessage(plLang.getMessage("command.pattern.list.entry",
                    "%pattern_name%", patternName,
                    "%format%", format,
                    "%size%", String.valueOf(file.length() / 1024)
            ));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("records", "known_routes"), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}