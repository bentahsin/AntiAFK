package com.bentahsin.antiafk.gui;

import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Menu implements InventoryHolder {

    protected Inventory inventory;
    protected final PlayerMenuUtility playerMenuUtility;
    protected Map<Integer, Runnable> actions = new HashMap<>();

    public Menu(PlayerMenuUtility playerMenuUtility) {
        this.playerMenuUtility = playerMenuUtility;
    }

    public abstract String getMenuName();
    public abstract int getSlots();
    public abstract void setMenuItems();

    public final void handleMenu(InventoryClickEvent e) {
        if (actions.containsKey(e.getSlot())) {
            actions.get(e.getSlot()).run();
        }
    }

    public void open() {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
        this.setMenuItems();
        playerMenuUtility.getOwner().openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    protected ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtil.color(name));
            if (lore != null) {
                meta.setLore(Arrays.stream(lore).map(ChatUtil::color).collect(Collectors.toList()));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}