package com.bentahsin.antiafk.turing.captcha;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.api.turing.ICaptcha;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.turing.CaptchaManager;
import com.bentahsin.antiafk.utils.ChatUtil;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ColorPaletteCaptcha implements ICaptcha, Listener {

    private final AntiAFKPlugin plugin;
    private final ConfigManager configManager;
    private final PlayerLanguageManager playerLanguageManager;
    private final Provider<CaptchaManager> captchaManagerProvider;

    private final Random random = new Random();
    private final Map<UUID, ActivePaletteTest> activeTests = new ConcurrentHashMap<>();

    @Inject
    public ColorPaletteCaptcha(
            AntiAFKPlugin plugin,
            ConfigManager configManager,
            PlayerLanguageManager playerLanguageManager,
            Provider<CaptchaManager> captchaManagerProvider
    ) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerLanguageManager = playerLanguageManager;
        this.captchaManagerProvider = captchaManagerProvider;
    }

    @Override
    public String getTypeName() {
        return "COLOR_PALETTE";
    }

    @Override
    public void start(Player player) {
        final int guiRows = configManager.getColorPaletteGuiRows();
        final int timeLimit = configManager.getColorPaletteTimeLimit();
        final int correctCount = configManager.getColorPaletteCorrectCount();
        final int distractorColorCount = configManager.getColorPaletteDistractorColorCount();
        final int distractorItemCount = configManager.getColorPaletteDistractorItemCount();
        final List<String> availableColors = configManager.getColorPaletteAvailableColors();

        if (availableColors.isEmpty()) {
            captchaManagerProvider.get().failChallenge(player, "Renk paleti yapılandırılmamış.");
            return;
        }

        final String correctColorStr = availableColors.get(random.nextInt(availableColors.size()));
        final List<String> distractorColors = new ArrayList<>();
        List<String> tempColorPool = new ArrayList<>(availableColors);
        tempColorPool.remove(correctColorStr);
        Collections.shuffle(tempColorPool);
        for (int i = 0; i < distractorColorCount && i < tempColorPool.size(); i++) {
            distractorColors.add(tempColorPool.get(i));
        }

        final String translatedColor = getTranslatedColorName(correctColorStr);
        final String guiTitle = playerLanguageManager.getMessage("turing_test.captcha_palette.instruction", "%color%", translatedColor)
                .replace(playerLanguageManager.getPrefix(), "");

        final Inventory gui = Bukkit.createInventory(player, guiRows * 9, guiTitle);

        List<ItemStack> itemsToPlace = new ArrayList<>();
        for (int i = 0; i < correctCount; i++) itemsToPlace.add(createWoolItem(correctColorStr));
        for (String color : distractorColors) {
            for (int i = 0; i < distractorItemCount; i++) itemsToPlace.add(createWoolItem(color));
        }

        Collections.shuffle(itemsToPlace);
        for (ItemStack item : itemsToPlace) {
            int randomSlot;
            do {
                randomSlot = random.nextInt(gui.getSize());
            } while (gui.getItem(randomSlot) != null);
            gui.setItem(randomSlot, item);
        }

        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (activeTests.containsKey(player.getUniqueId())) {
                player.closeInventory();
                playerLanguageManager.sendMessage(player, "turing_test.captcha_palette.failure_time_out");
                captchaManagerProvider.get().failChallenge(player, "Renk paleti süresi doldu.");
            }
        }, timeLimit * 20L);

        activeTests.put(player.getUniqueId(), new ActivePaletteTest(correctColorStr, correctCount, timeoutTask, gui));
        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
    }

    @Override
    public void reopen(Player player) {
        ActivePaletteTest test = activeTests.get(player.getUniqueId());
        if (test != null) {
            player.openInventory(test.getGui());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ActivePaletteTest test = activeTests.get(player.getUniqueId());
        if (test == null || !event.getInventory().equals(test.getGui())) return;

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String correctColorDisplayName = getTranslatedColorName(test.getCorrectColor());
        if (clickedItem.hasItemMeta() && Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName().equals(correctColorDisplayName)) {
            event.setCurrentItem(new ItemStack(Material.AIR));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
            test.incrementCorrectClicks();
            if (test.isCompleted()) {
                player.closeInventory();
                captchaManagerProvider.get().passChallenge(player);
            }
        } else {
            player.closeInventory();
            playerLanguageManager.sendMessage(player, "turing_test.captcha_palette.failure_wrong_item");
            captchaManagerProvider.get().failChallenge(player, "Yanlış renge tıkladı.");
        }
    }

    @Override
    public void cleanUp(Player player) {
        ActivePaletteTest test = activeTests.remove(player.getUniqueId());
        if (test != null) {
            test.getTimeoutTask().cancel();
        }
    }


    private ItemStack createWoolItem(String colorName) {
        Material woolMaterial = Material.matchMaterial(colorName.toUpperCase() + "_WOOL");
        if (woolMaterial == null) woolMaterial = Material.WHITE_WOOL;

        ItemStack item = new ItemStack(woolMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(getTranslatedColorName(colorName));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String getTranslatedColorName(String colorKey) {
        String path = "turing_test.captcha_palette_colors." + colorKey.toUpperCase();
        String translatedName = playerLanguageManager.getRawMessage(path);

        if (translatedName == null) {
            plugin.getLogger().warning("messages.yml dosyasında renk çevirisi bulunamadı: " + path + ". Anahtar adı kullanılacak.");
            return colorKey;
        }

        return ChatUtil.color(translatedName);
    }

    private static class ActivePaletteTest {
        private final String correctColor;
        private final int totalCorrectItems;
        private final BukkitTask timeoutTask;
        private final Inventory gui;
        private int correctClicks = 0;

        public ActivePaletteTest(String correctColor, int totalCorrectItems, BukkitTask timeoutTask, Inventory gui) {
            this.correctColor = correctColor;
            this.totalCorrectItems = totalCorrectItems;
            this.timeoutTask = timeoutTask;
            this.gui = gui;
        }

        public String getCorrectColor() { return correctColor; }
        public BukkitTask getTimeoutTask() { return timeoutTask; }
        public Inventory getGui() { return gui; }
        public void incrementCorrectClicks() { this.correctClicks++; }
        public boolean isCompleted() { return this.correctClicks >= this.totalCorrectItems; }
    }
}