package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.LanguageManager;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Tehlikeli işlemler için genel amaçlı bir onay menüsü.
 * Bu menü, bir "onaylama" ve bir "iptal etme" eylemi alır.
 */
public class ConfirmationGUI extends Menu {

    private final String title;
    private final ItemStack confirmationItem;
    private final Consumer<InventoryClickEvent> onConfirm;
    private final Consumer<InventoryClickEvent> onCancel;
    private final LanguageManager lang;

    /**
     * Yeni bir onay menüsü oluşturur.
     *
     * @param playerMenuUtility Oyuncunun menü bilgileri.
     * @param title             Menünün başlığı.
     * @param confirmationItem  Menünün ortasında gösterilecek ve neyin onaylandığını belirten öğe.
     * @param onConfirm         "Onayla" butonuna basıldığında çalışacak eylem.
     * @param onCancel          "İptal Et" butonuna basıldığında çalışacak eylem.
     */
    public ConfirmationGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin, String title, ItemStack confirmationItem, Consumer<InventoryClickEvent> onConfirm, Consumer<InventoryClickEvent> onCancel) {
        super(playerMenuUtility);
        this.title = title;
        this.confirmationItem = confirmationItem;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        this.lang = plugin.getLanguageManager();
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
                lang.getMessage("gui.confirmation.confirm_button.name"),
                lang.getMessageList("gui.confirmation.confirm_button.lore").toArray(new String[0])
        );
        actions.put(11, () -> onConfirm.accept(null));
        inventory.setItem(11, confirmButton);

        inventory.setItem(13, confirmationItem);

        ItemStack cancelButton = createGuiItem(Material.RED_STAINED_GLASS_PANE,
                lang.getMessage("gui.confirmation.cancel_button.name"),
                lang.getMessageList("gui.confirmation.cancel_button.lore").toArray(new String[0])
        );
        actions.put(15, () -> onCancel.accept(null));
        inventory.setItem(15, cancelButton);

        ItemStack filler = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
}