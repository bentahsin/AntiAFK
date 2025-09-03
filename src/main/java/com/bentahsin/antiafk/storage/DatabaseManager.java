package com.bentahsin.antiafk.storage;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.models.PlayerStats;
import com.bentahsin.antiafk.models.TopEntry;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * SQLite veritabanı bağlantısını ve işlemlerini yönetir.
 */
public class DatabaseManager {

    private final AntiAFKPlugin plugin;
    private Connection connection;

    public DatabaseManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Veritabanı bağlantısını kurar ve oyuncu tablosunu oluşturur.
     */
    public void connect() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "playerdata.db");
            if (!dbFile.exists()) {
                dbFile.createNewFile();
            }
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);

            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS player_stats (" +
                        "uuid TEXT PRIMARY KEY NOT NULL," +
                        "username TEXT," +
                        "total_afk_time BIGINT DEFAULT 0 NOT NULL," +
                        "times_punished INTEGER DEFAULT 0 NOT NULL," +
                        "last_punishment_time BIGINT DEFAULT 0 NOT NULL," +
                        "turing_tests_passed INTEGER DEFAULT 0 NOT NULL," +
                        "turing_tests_failed INTEGER DEFAULT 0 NOT NULL," +
                        "most_frequent_reason TEXT" +
                        ");");
            }
            plugin.getLogger().info("Database connection established and table verified.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not connect to the database!", e);
        }
    }

    /**
     * Veritabanı bağlantısını güvenli bir şekilde kapatır.
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not close the database connection!", e);
        }
    }

    /**
     * Bir oyuncunun tüm sabıka verilerini veritabanından alır.
     * Bu metot, veriyi döndürmeden önce ceza sayacının zaman aşımına uğrayıp uğramadığını kontrol eder.
     * @param playerUUID Oyuncunun UUID'si.
     * @param playerName Oyuncunun güncel adı.
     * @return Oyuncunun verilerini içeren bir PlayerStats nesnesi.
     */
    public PlayerStats getPlayerStats(UUID playerUUID, String playerName) {
        String sql = "SELECT * FROM player_stats WHERE uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                long lastPunishmentTime = rs.getLong("last_punishment_time");
                int timesPunished = rs.getInt("times_punished");

                long resetAfterMillis = plugin.getConfigManager().getPunishmentResetMillis();

                if (resetAfterMillis > 0 && timesPunished > 0 &&
                        (System.currentTimeMillis() - lastPunishmentTime) > resetAfterMillis) {

                    plugin.getLogger().info("Resetting punishment count for player " + playerName + " due to inactivity.");
                    resetPunishmentCountToZero(playerUUID);

                    timesPunished = 0;
                    lastPunishmentTime = 0;
                }

                return new PlayerStats(
                        playerUUID,
                        rs.getString("username"),
                        rs.getLong("total_afk_time"),
                        timesPunished,
                        lastPunishmentTime,
                        rs.getInt("turing_tests_passed"),
                        rs.getInt("turing_tests_failed"),
                        rs.getString("most_frequent_reason")
                );
            } else {
                createPlayerEntry(playerUUID, playerName);
                return PlayerStats.createDefault(playerUUID, playerName);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not retrieve stats for " + playerUUID, e);
        }
        return PlayerStats.createDefault(playerUUID, playerName);
    }

    /**
     * Bir oyuncunun ceza sayısını 0'a sıfırlar (ASENKRON).
     * Bu metot, ceza zaman aşımına uğradığında çağrılır.
     * @param uuid Sıfırlanacak oyuncunun UUID'si.
     */
    private void resetPunishmentCountToZero(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            String sql = "UPDATE player_stats SET times_punished = 0 WHERE uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not reset punishment count to zero for " + uuid, e);
            }
        }).thenRun(() -> plugin.getPlayerStatsManager().invalidateCache(uuid));
    }

    /**
     * Veritabanında yeni bir oyuncu için boş bir kayıt oluşturur.
     */
    private void createPlayerEntry(UUID uuid, String username) {
        CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR IGNORE INTO player_stats (uuid, username) VALUES (?, ?);";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, username);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create async entry for " + uuid, e);
            }
        });
    }
    /**
     * Bir oyuncunun ceza sayısını veritabanından alır.
     * @param uuid Oyuncunun UUID'si.
     * @return Ceza sayısı.
     */
    public int getPunishmentCount(UUID uuid) {
        String sql = "SELECT punishment_count FROM player_stats WHERE uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("punishment_count");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not retrieve punishment count for " + uuid, e);
        }
        return 0;
    }

    /**
     * Bir oyuncunun ceza sayısını 1'e sıfırlar ve son ceza zamanını günceller.
     * @param uuid Oyuncunun UUID'si.
     */
    public void resetPunishmentCount(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            String sql = "UPDATE player_stats SET times_punished = 1, last_punishment_time = ? WHERE uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, System.currentTimeMillis());
                pstmt.setString(2, uuid.toString());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not reset async punishment count for " + uuid, e);
            }
        });
    }

    /**
     * Bir oyuncunun ceza sayısını bir artırır ve son ceza zamanını günceller.
     * @param uuid Oyuncunun UUID'si.
     */
    public void incrementPunishmentCount(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            String sql = "UPDATE player_stats SET times_punished = times_punished + 1, last_punishment_time = ? WHERE uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, System.currentTimeMillis());
                pstmt.setString(2, uuid.toString());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not increment async punishment count for " + uuid, e);
            }
        }).thenRun(() -> plugin.getPlayerStatsManager().invalidateCache(uuid));
    }

    /**
     * Bir oyuncunun toplam AFK süresine saniye ekler.
     * @param uuid Oyuncunun UUID'si.
     * @param secondsToAdd Eklenecek süre (saniye cinsinden).
     */
    public void updateTotalAfkTime(UUID uuid, long secondsToAdd) {
        CompletableFuture.runAsync(() -> {
            String sql = "UPDATE player_stats SET total_afk_time = total_afk_time + ? WHERE uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, secondsToAdd);
                pstmt.setString(2, uuid.toString());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not update async total AFK time for " + uuid, e);
            }
        }).thenRun(() -> plugin.getPlayerStatsManager().invalidateCache(uuid));
    }

    /**
     * Bir oyuncunun başarılı Turing Testi sayısını bir artırır.
     * @param uuid Oyuncunun UUID'si.
     */
    public void incrementTestsPassed(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            String sql = "UPDATE player_stats SET turing_tests_passed = turing_tests_passed + 1 WHERE uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not increment async tests passed for " + uuid, e);
            }
        });
    }

    /**
     * Bir oyuncunun başarısız Turing Testi sayısını bir artırır.
     * @param uuid Oyuncunun UUID'si.
     */
    public void incrementTestsFailed(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            String sql = "UPDATE player_stats SET turing_tests_failed = turing_tests_failed + 1 WHERE uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not increment async tests failed for " + uuid, e);
            }
        });
    }

    /**
     * Belirtilen kritere göre en üst sıradaki oyuncuların listesini döndürür.
     * @param orderBy Hangi sütuna göre sıralanacağı ("total_afk_time" veya "times_punished").
     * @param limit Döndürülecek maksimum oyuncu sayısı.
     * @return TopEntry nesnelerinden oluşan bir liste.
     */
    public CompletableFuture<List<TopEntry>> getTopPlayers(String orderBy, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<TopEntry> topPlayers = new ArrayList<>();
            if (!orderBy.equals("total_afk_time") && !orderBy.equals("times_punished")) {
                plugin.getLogger().warning("Invalid column name passed to getTopPlayers: " + orderBy);
                return topPlayers;
            }
            String sql = "SELECT username, " + orderBy + " FROM player_stats ORDER BY " + orderBy + " DESC LIMIT ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    topPlayers.add(new TopEntry(rs.getString("username"), rs.getLong(orderBy)));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not retrieve async top players for " + orderBy, e);
            }
            return topPlayers;
        });
    }
}