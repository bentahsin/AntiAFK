package com.bentahsin.antiafk.commands.antiafk.pattern;

import com.bentahsin.antiafk.AntiAFKPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListPatternCommand implements IPatternSubCommand {

    private final AntiAFKPlugin plugin;

    public ListPatternCommand(AntiAFKPlugin plugin) {
        this.plugin = plugin;
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
            sender.sendMessage("§cGeçersiz klasör. Kullanılabilir: records, known_routes");
            return;
        }

        if (!directory.exists() || !directory.isDirectory()) {
            sender.sendMessage("§e'" + folderName + "' klasörü boş veya bulunamadı.");
            return;
        }

        File[] files = directory.listFiles(f -> f.getName().endsWith(".pattern"));
        if (files == null || files.length == 0) {
            sender.sendMessage("§e'" + folderName + "' klasöründe hiç desen bulunamadı.");
            return;
        }

        sender.sendMessage("§8§m----§r §6Desenler (" + folderName + ") §8§m----");
        for (File file : files) {
            String fileName = file.getName().replace(".pattern", "");
            String format = fileName.endsWith(".json") ? "JSON" : (fileName.endsWith(".kryo") ? "Kryo" : "Bilinmiyor");
            String patternName = fileName.replace(".json", "").replace(".kryo", "");

            sender.sendMessage("§e- " + patternName + " §7(Format: " + format + ", Boyut: " + (file.length() / 1024) + " KB)");
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