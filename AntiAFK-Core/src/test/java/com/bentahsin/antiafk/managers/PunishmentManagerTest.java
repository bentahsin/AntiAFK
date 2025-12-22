package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.models.PunishmentLevel;
import com.bentahsin.antiafk.storage.DatabaseManager;
import com.bentahsin.antiafk.utils.DiscordWebhookUtil;
import com.github.benmanes.caffeine.cache.Cache;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PunishmentManagerTest {

    @Mock private AntiAFKPlugin plugin;
    @Mock private ConfigManager configManager;
    @Mock private DatabaseManager databaseManager;
    @Mock private PlayerStateManager stateManager;
    @Mock private PlayerLanguageManager plLang;
    @Mock private DebugManager debugManager;
    @Mock private DiscordWebhookUtil discordUtil;
    @Mock private Player player;
    @Mock private PluginManager pluginManager;

    private PunishmentManager punishmentManager;
    private final UUID playerUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(player.getName()).thenReturn("TestPlayer");
        when(configManager.getRejoinCooldownSeconds()).thenReturn(300L);

        punishmentManager = new PunishmentManager(
                plugin, configManager, databaseManager, stateManager, plLang, debugManager, discordUtil
        );
    }

    @Test
    @DisplayName("Kademeli Ceza: Artan Ceza Sayısına Göre İşlem Yapılmalı")
    void testProgressivePunishmentSelection() {
        try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
            bukkitMock.when(Bukkit::getPluginManager).thenReturn(pluginManager);

            when(configManager.isProgressivePunishmentEnabled()).thenReturn(true);
            when(configManager.getHighestPunishmentCount()).thenReturn(5);
            when(databaseManager.getPunishmentCount(playerUUID)).thenReturn(2);

            List<PunishmentLevel> levels = new ArrayList<>();
            levels.add(new PunishmentLevel(5, Collections.emptyList()));
            levels.add(new PunishmentLevel(3, Collections.emptyList()));
            levels.add(new PunishmentLevel(1, Collections.emptyList()));

            when(configManager.getPunishmentLevels()).thenReturn(levels);
            when(configManager.getActions()).thenReturn(Collections.emptyList());

            punishmentManager.applyPunishment(player, null);

            verify(databaseManager, times(1)).getPunishmentCount(playerUUID);
            verify(databaseManager, times(1)).incrementPunishmentCount(playerUUID);
            verify(databaseManager, never()).resetPunishmentCount(playerUUID);
        }
    }

    @Test
    @DisplayName("Kademeli Ceza: Maksimum Seviyeye Ulaşınca Sıfırlanmalı")
    void testProgressivePunishmentReset() {
        try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
            bukkitMock.when(Bukkit::getPluginManager).thenReturn(pluginManager);

            when(configManager.isProgressivePunishmentEnabled()).thenReturn(true);
            when(configManager.getHighestPunishmentCount()).thenReturn(5);
            when(databaseManager.getPunishmentCount(playerUUID)).thenReturn(5);

            List<PunishmentLevel> levels = new ArrayList<>();
            levels.add(new PunishmentLevel(5, Collections.emptyList()));
            when(configManager.getPunishmentLevels()).thenReturn(levels);

            punishmentManager.applyPunishment(player, null);

            verify(databaseManager, times(1)).resetPunishmentCount(playerUUID);
            verify(databaseManager, never()).incrementPunishmentCount(playerUUID);
        }
    }

    @Test
    @DisplayName("Rejoin Koruması: Önbellek (Cache) Çalışıyor mu?")
    void testRejoinCacheInitialization() throws NoSuchFieldException, IllegalAccessException {
        Field cacheField = PunishmentManager.class.getDeclaredField("rejoinProtectedPlayers");
        cacheField.setAccessible(true);
        Cache<UUID, Long> cache = (Cache<UUID, Long>) cacheField.get(punishmentManager);

        assertNotNull(cache, "Cache nesnesi oluşturulmamış (null)!");

        cache.put(playerUUID, System.currentTimeMillis());

        assertNotNull(cache.getIfPresent(playerUUID), "Cache veriyi tutmuyor!");
    }
}