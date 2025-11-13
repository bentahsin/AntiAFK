package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.geysermc.geyser.api.GeyserApi;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * GeyserMC ile uyumluluğu yönetir. Bedrock oyuncularına özel
 * davranışlar ve alternatif arayüzler sunar.
 */
public class GeyserCompatibilityManager {

    private final AntiAFKPlugin plugin;
    private boolean geyserInstalled = false;
    private GeyserApi geyserApi;

    public GeyserCompatibilityManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
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

    /**
     * Geyser entegrasyonunun aktif olup olmadığını döndürür.
     * @return Geyser yüklü ve API erişilebilir ise true.
     */
    public boolean isGeyserInstalled() {
        return geyserInstalled;
    }

    /**
     * Bir oyuncunun Bedrock istemcisinden bağlanıp bağlanmadığını kontrol eder.
     * @param playerUUID Kontrol edilecek oyuncunun UUID'si.
     * @return Oyuncu Bedrock'tan bağlıysa true.
     */
    public boolean isBedrockPlayer(UUID playerUUID) {
        if (!isGeyserInstalled()) {
            return false;
        }
        return geyserApi.isBedrockPlayer(playerUUID);
    }

    /**
     * Bir oyuncudan metin girdisi ister. Oyuncunun platformuna göre
     * en uygun yöntemi (Sohbet, Form vb.) seçer.
     *
     * @param player Girdi istenecek oyuncu.
     * @param title Oyuncuya gösterilecek başlık veya talimat.
     * @param onConfirm Girdi alındığında çalıştırılacak fonksiyon.
     */
    public void promptForInput(Player player, String title, String initialText, Consumer<String> onConfirm, Runnable onCancel) {
        if (isBedrockPlayer(player.getUniqueId())) {
            promptViaChat(player, title, onConfirm);
        } else {
            promptViaAnvil(player, title, initialText, onConfirm, onCancel);
        }
    }

    public void promptForInput(Player player, String title, Consumer<String> onConfirm, Runnable onCancel) {
        promptForInput(player, title, "", onConfirm, onCancel);
    }

    /**
     * Bedrock oyuncuları için sohbet tabanlı bir girdi isteme yöntemi.
     * Bu, en basit ve en güvenilir alternatiftir.
     */
    private void promptViaChat(Player player, String title, Consumer<String> callback) {
        plugin.getPlayersInChatInput().add(player.getUniqueId());
        plugin.getChatInputCallbacks().put(player.getUniqueId(), callback);

        player.closeInventory();

        plugin.getPlayerLanguageManager().sendMessage(player, "gui.region.input_prompt_geyser_title", "%title%", title);
        plugin.getPlayerLanguageManager().sendMessage(player, "gui.region.input_prompt_geyser_instruction");
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