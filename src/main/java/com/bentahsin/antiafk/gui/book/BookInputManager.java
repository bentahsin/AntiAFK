package com.bentahsin.antiafk.gui.book;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * ProtocolLib kullanarak oyuncuya sanal bir kitap açtıran yönetici sınıf.
 * Bu yöntem, oyuncu düzenlemeyi bitirene kadar kitabı oyuncunun elinde tutar.
 */
public class BookInputManager {

    private final Map<UUID, BookInputRequest> inputRequests = new HashMap<>();
    private final Plugin plugin;

    public BookInputManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Oyuncuya düzenlenebilir bir kitap arayüzü açar.
     * @param player Oyuncu
     * @param initialText Kitabın başlangıç metni
     * @param onFinish Oyuncu kitabı imzaladığında çalışacak eylem
     */
    public void prompt(Player player, String initialText, Consumer<String> onFinish) {
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta != null) {
            meta.setPages(initialText);
            book.setItemMeta(meta);
        }

        ItemStack originalItem = player.getInventory().getItemInMainHand().clone();
        BookInputRequest request = new BookInputRequest(onFinish, originalItem);
        inputRequests.put(player.getUniqueId(), request);
        player.getInventory().setItemInMainHand(book);

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer openBookPacket = protocolManager.createPacket(PacketType.Play.Server.OPEN_BOOK);
        openBookPacket.getHands().write(0, EnumWrappers.Hand.MAIN_HAND);

        protocolManager.sendServerPacket(player, openBookPacket);
    }

    /**
     * Bir oyuncu kitabı düzenlediğinde çağrılır.
     */
    public void handleBookEdit(Player player, BookMeta newBookMeta) {
        UUID playerUUID = player.getUniqueId();
        final BookInputRequest request = inputRequests.remove(playerUUID);

        if (request == null) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> player.getInventory().setItemInMainHand(request.getOriginalItem()), 1L);

        String newContent = String.join("\n", newBookMeta.getPages()).trim();
        request.getCallback().accept(newContent);
    }

    /**
     * Oyuncu sunucudan ayrılırsa bekleyen isteği temizler ve eşyasını geri verir.
     */
    public void onPlayerQuit(Player player) {
        BookInputRequest request = inputRequests.remove(player.getUniqueId());
        if (request != null) {
            player.getInventory().setItemInMainHand(request.getOriginalItem());
        }
    }
}

class BookInputRequest {
    private final Consumer<String> callback;
    private final ItemStack originalItem;

    public BookInputRequest(Consumer<String> callback, ItemStack originalItem) {
        this.callback = callback;
        this.originalItem = originalItem;
    }

    public Consumer<String> getCallback() {
        return callback;
    }

    public ItemStack getOriginalItem() {
        return originalItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookInputRequest that = (BookInputRequest) o;
        return Objects.equals(callback, that.callback) && Objects.equals(originalItem, that.originalItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callback, originalItem);
    }

    @Override
    public String toString() {
        return "BookInputRequest{" +
                "callback=" + callback +
                ", originalItem=" + originalItem +
                '}';
    }
}