package com.bentahsin.antiafk.platform;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.google.inject.Inject;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * IInputCompatibility arayüzünün standart Java oyuncuları için AnvilGUI
 * kullanarak çalışan varsayılan implementasyonu.
 */
public class JavaInputCompatibility implements IInputCompatibility {

    private final AntiAFKPlugin plugin;

    @Inject
    public JavaInputCompatibility(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void promptForInput(Player player, String title, String initialText, Consumer<String> onConfirm, Runnable onCancel) {
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

    @Override
    public void promptForInput(Player player, String title, Consumer<String> onConfirm, Runnable onCancel) {
        promptForInput(player, title, "", onConfirm, onCancel);
    }

    @Override
    public boolean isBedrockPlayer(UUID playerUUID) {
        return false;
    }
}