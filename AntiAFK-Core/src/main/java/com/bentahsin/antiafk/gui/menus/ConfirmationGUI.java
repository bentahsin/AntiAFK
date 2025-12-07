package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Tehlikeli işlemler için genel amaçlı bir onay menüsü.
 * Bu menü, Guice'ın AssistedInject özelliği kullanılarak oluşturulur.
 */
public class ConfirmationGUI extends Menu {

    private final String title;
    private final ItemStack confirmationItem;
    private final Consumer<InventoryClickEvent> onConfirm;
    private final Runnable onCancel;

    private final PlayerLanguageManager playerLanguageManager;

    @Inject
    public ConfirmationGUI(
            @Assisted PlayerMenuUtility playerMenuUtility,
            @Assisted String title,
            @Assisted ItemStack confirmationItem,
            @Assisted Consumer<InventoryClickEvent> onConfirm,
            @Assisted Runnable onCancel,
            PlayerLanguageManager playerLanguageManager
    ) {
        super(playerMenuUtility);
        this.title = title;
        this.confirmationItem = confirmationItem;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        this.playerLanguageManager = playerLanguageManager;
    }

    @Override
    public String getMenuName() {
        return title;
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        ItemStack confirmButton = createGuiItem(Material.LIME_STAINED_GLASS_PANE,
                playerLanguageManager.getMessage("gui.confirmation.confirm_button.name"),
                playerLanguageManager.getMessageList("gui.confirmation.confirm_button.lore").toArray(new String[0])
        );
        actions.put(11, () -> onConfirm.accept(null));
        inventory.setItem(11, confirmButton);

        inventory.setItem(13, confirmationItem);

        ItemStack cancelButton = createGuiItem(Material.RED_STAINED_GLASS_PANE,
                playerLanguageManager.getMessage("gui.confirmation.cancel_button.name"),
                playerLanguageManager.getMessageList("gui.confirmation.cancel_button.lore").toArray(new String[0])
        );
        actions.put(15, onCancel);
        inventory.setItem(15, cancelButton);

        ItemStack filler = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
}