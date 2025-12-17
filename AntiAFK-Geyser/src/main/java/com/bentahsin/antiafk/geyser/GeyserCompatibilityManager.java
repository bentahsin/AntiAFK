package com.bentahsin.antiafk.geyser;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.platform.IInputCompatibility;
import com.google.inject.Inject;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.geysermc.geyser.api.GeyserApi;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * IInputCompatibility arayüzünün Geyser (Bedrock) oyuncuları için
 * özel mantık içeren implementasyonu. Bu sınıf, Geyser modülü içinde yer alır
 * ve Guice tarafından yönetilir.
 */
public class GeyserCompatibilityManager implements IInputCompatibility {

    private final AntiAFKPlugin plugin;
    private final PlayerLanguageManager playerLanguageManager;
    private boolean geyserInstalled = false;
    private GeyserApi geyserApi;
    private static final int INPUT_TIMEOUT_SECONDS = 45;

    @Inject
    public GeyserCompatibilityManager(AntiAFKPlugin plugin, PlayerLanguageManager playerLanguageManager) {
        this.plugin = plugin;
        this.playerLanguageManager = playerLanguageManager;

        if (plugin.getServer().getPluginManager().getPlugin("Geyser-Spigot") != null) {
            try {
                this.geyserApi = GeyserApi.api();
                this.geyserInstalled = true;
                plugin.getLogger().info("Geyser-Spigot bulundu. Bedrock oyuncuları için uyumluluk katmanı etkinleştirildi.");
            } catch (Exception e) {
                plugin.getLogger().warning("Geyser-Spigot bulundu ancak Geyser API'sine erişilemedi. Uyumluluk katmanı devre dışı.");
                this.geyserInstalled = false;
            }
        }
    }

    public boolean isGeyserInstalled() {
        return geyserInstalled;
    }

    @Override
    public boolean isBedrockPlayer(UUID playerUUID) {
        if (!isGeyserInstalled()) {
            return false;
        }
        return geyserApi.isBedrockPlayer(playerUUID);
    }

    @Override
    public void promptForInput(Player player, String title, String initialText, Consumer<String> onConfirm, Runnable onCancel) {
        if (isBedrockPlayer(player.getUniqueId())) {
            promptViaChat(player, title, onConfirm);
        } else {
            promptViaAnvil(player, title, initialText, onConfirm, onCancel);
        }
    }

    @Override
    public void promptForInput(Player player, String title, Consumer<String> onConfirm, Runnable onCancel) {
        promptForInput(player, title, "", onConfirm, onCancel);
    }

    private void promptViaChat(Player player, String title, Consumer<String> callback) {
        UUID playerUUID = player.getUniqueId();

        if (plugin.getPlayersInChatInput().contains(playerUUID)) {
            plugin.clearPlayerChatInput(playerUUID);
        }

        plugin.getPlayersInChatInput().add(playerUUID);

        final BukkitTask[] timeoutTask = new BukkitTask[1];

        Consumer<String> wrappedCallback = (input) -> {
            if (timeoutTask[0] != null && !timeoutTask[0].isCancelled()) {
                timeoutTask[0].cancel();
            }
            callback.accept(input);
        };

        plugin.getChatInputCallbacks().put(playerUUID, wrappedCallback);

        player.closeInventory();

        playerLanguageManager.sendMessage(player, "gui.region.input_prompt_geyser_title", "%title%", title);
        playerLanguageManager.sendMessage(player, "gui.region.input_prompt_geyser_instruction");

        timeoutTask[0] = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getPlayersInChatInput().contains(playerUUID)) {
                plugin.clearPlayerChatInput(playerUUID);
                playerLanguageManager.sendMessage(player, "gui.region.input_cancelled"); // Veya "Zaman aşımı" mesajı
            }
        }, INPUT_TIMEOUT_SECONDS * 20L);
    }

    private void promptViaAnvil(Player player, String title, String initialText, Consumer<String> onConfirm, Runnable onCancel) {
        final AtomicBoolean confirmed = new AtomicBoolean(false);

        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> {
                    if (!confirmed.get() && onCancel != null) {
                        Bukkit.getScheduler().runTaskLater(plugin, onCancel, 1L);
                    }
                })
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }
                    confirmed.set(true);
                    onConfirm.accept(stateSnapshot.getText());
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .preventClose()
                .text(initialText)
                .itemLeft(new ItemStack(Material.PAPER))
                .title(title)
                .plugin(plugin)
                .open(player);
    }
}