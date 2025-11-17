package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerListGUI extends Menu {

    private final AntiAFKPlugin plugin;
    private final AFKManager afkManager;
    private final PlayerLanguageManager lang;
    private final int page;

    public PlayerListGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin, int page) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.afkManager = plugin.getAfkManager();
        this.lang = plugin.getPlayerLanguageManager();
        this.page = page;
    }

    @Override
    public String getMenuName() {
        return lang.getMessage("gui.menu_titles.player_list", "%page%", String.valueOf(page + 1))
                .replace(lang.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void setMenuItems() {
        addBottomBar();

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        int maxItemsPerPage = 45;

        for (int i = 0; i < maxItemsPerPage; i++) {
            int index = maxItemsPerPage * page + i;
            if (index >= players.size()) break;

            Player targetPlayer = players.get(index);
            ItemStack playerHead = createPlayerHead(targetPlayer);

            actions.put(i, () -> {
                playerMenuUtility.setTargetPlayerUUID(targetPlayer.getUniqueId());
                playerMenuUtility.setLastPlayerListPage(this.page);
                new PlayerActionGUI(playerMenuUtility, plugin).open();
            });

            inventory.setItem(i, playerHead);
        }
    }

    private void addBottomBar() {
        actions.put(49, () -> new AdminPanelGUI(playerMenuUtility, plugin).open());
        inventory.setItem(49, createGuiItem(
                Material.BARRIER,
                lang.getMessage("gui.player_list_menu.back_to_main_menu_button.name"),
                lang.getMessageList("gui.player_list_menu.back_to_main_menu_button.lore").toArray(new String[0])
        ));

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        int maxItemsPerPage = 45;
        int maxPages = (int) Math.ceil((double) players.size() / maxItemsPerPage);

        if (page > 0) {
            actions.put(48, () -> new PlayerListGUI(playerMenuUtility, plugin, page - 1).open());
            inventory.setItem(48, createGuiItem(
                    Material.ARROW,
                    lang.getMessage("gui.player_list_menu.previous_page_button.name"),
                    lang.getMessageList("gui.player_list_menu.previous_page_button.lore").toArray(new String[0])
            ));
        }

        if (page + 1 < maxPages) {
            actions.put(50, () -> new PlayerListGUI(playerMenuUtility, plugin, page + 1).open());
            inventory.setItem(50, createGuiItem(
                    Material.ARROW,
                    lang.getMessage("gui.player_list_menu.next_page_button.name"),
                    lang.getMessageList("gui.player_list_menu.next_page_button.lore").toArray(new String[0])
            ));
        }
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName("§r" + player.getDisplayName());


            String afkTime = TimeUtil.formatTime(afkManager.getStateManager().getAfkTime(player));
            String statusKey = afkManager.getStateManager().isManuallyAFK(player) ? "gui.player_list_menu.status_afk" : "gui.player_list_menu.status_active";
            String status = lang.getMessage(statusKey);
            String world = player.getWorld().getName();

            List<String> loreTemplate = lang.getMessageList("gui.player_list_menu.player_head_lore");

            List<String> finalLore = loreTemplate.stream()
                    .map(line -> line
                            .replace("%afk_time%", afkTime)
                            .replace("%status%", status)
                            .replace("%world%", world)
                    )
                    .collect(Collectors.toList());

            meta.setLore(finalLore);
            head.setItemMeta(meta);
        }
        return head;
    }
}