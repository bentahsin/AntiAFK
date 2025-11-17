package com.bentahsin.antiafk.gui.book;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

public class BookInputListener implements Listener {

    private final BookInputManager bookInputManager;

    public BookInputListener(BookInputManager bookInputManager) {
        this.bookInputManager = bookInputManager;
    }

    @EventHandler
    public void onBookSign(PlayerEditBookEvent event) {
        Player player = event.getPlayer();
        if (!event.isSigning()) {
            return;
        }

        BookMeta newBookMeta = event.getNewBookMeta();
        bookInputManager.handleBookEdit(player, newBookMeta);
    }
}