package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.models.RegionOverride;
import com.bentahsin.antiafk.utils.TimeUtil;
import com.bentahsin.antiafk.data.PointlessActivityData;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AFKManager {

    private final AntiAFKPlugin plugin;
    private final ConfigManager configManager;
    private final LanguageManager lang;

    private final Cache<UUID, Long> lastActivity;
    private final Cache<UUID, Long> lastWarningTime;
    private final Cache<UUID, Long> rejoinProtectedPlayers;
    private final Cache<UUID, PointlessActivityData> botDetectionData;

    private final Set<UUID> manualAFKPlayers;
    private final Set<UUID> autoSetAfkPlayers;
    private final Set<UUID> autonomousPlayers;
    private final Set<UUID> systemPunishedPlayers;
    private final Map<UUID, String> originalDisplayNames;
    private final Map<UUID, String> afkReasons;
    private final Set<UUID> suspiciousPlayers;

    public AFKManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.lang = plugin.getLanguageManager();

        this.lastActivity = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build();

        this.lastWarningTime = Caffeine.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();

        this.rejoinProtectedPlayers = Caffeine.newBuilder()

                .expireAfterWrite(Math.max(10, configManager.getRejoinCooldownSeconds() * 2), TimeUnit.SECONDS)
                .build();

        this.botDetectionData = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build();

        this.manualAFKPlayers = ConcurrentHashMap.newKeySet();
        this.autoSetAfkPlayers = ConcurrentHashMap.newKeySet();
        this.autonomousPlayers = ConcurrentHashMap.newKeySet();
        this.systemPunishedPlayers = ConcurrentHashMap.newKeySet();
        this.suspiciousPlayers = ConcurrentHashMap.newKeySet();
        this.originalDisplayNames = new ConcurrentHashMap<>();
        this.afkReasons = new ConcurrentHashMap<>();
    }

    public void updateActivity(Player player) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
        lastWarningTime.invalidate(player.getUniqueId());
    }

    public void addPlayer(Player player) {
        updateActivity(player);
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        lastActivity.invalidate(uuid);
        lastWarningTime.invalidate(uuid);
        botDetectionData.invalidate(uuid);
        manualAFKPlayers.remove(uuid);
        autoSetAfkPlayers.remove(uuid);
        originalDisplayNames.remove(uuid);
        afkReasons.remove(uuid);
        autonomousPlayers.remove(uuid);
        systemPunishedPlayers.remove(uuid);
        suspiciousPlayers.remove(player.getUniqueId());
    }

    public long getAfkTime(Player player) {
        Long last = lastActivity.getIfPresent(player.getUniqueId());
        if (last == null) {
            updateActivity(player);
            return 0;
        }
        return (System.currentTimeMillis() - last) / 1000;
    }

    /**
     * Oyuncunun manuel veya otomatik olarak AFK durumunda olup olmadığını kontrol eder.
     */
    public boolean isEffectivelyAfk(Player player) {
        UUID uuid = player.getUniqueId();
        return manualAFKPlayers.contains(uuid) || autoSetAfkPlayers.contains(uuid) || systemPunishedPlayers.contains(uuid);
    }

    /**
     * Sadece oyuncunun kendi /afk komutuyla mı AFK olduğunu kontrol eder.
     */
    public boolean isManuallyAFK(Player player) {
        return manualAFKPlayers.contains(player.getUniqueId());
    }

    public void checkPlayer(Player player) {
        if (player.hasPermission(configManager.getPermBypassAll())) return;
        if (player.hasPermission(configManager.getPermBypassClassic())) return;
        if (configManager.getDisabledWorlds().contains(player.getWorld().getName())) return;
        if (configManager.getExemptGameModes().contains(player.getGameMode().name())) return;

        RegionOverride override = configManager.getRegionOverrideForPlayer(player);
        long effectiveMaxAfkTime;
        List<Map<String, String>> effectiveActions;
        if (override != null) {
            if (override.getMaxAfkTime() < 0) return;
            effectiveMaxAfkTime = override.getMaxAfkTime();
            effectiveActions = override.getActions();
        } else {
            effectiveMaxAfkTime = configManager.getMaxAfkTimeSeconds();
            effectiveActions = configManager.getActions();
        }

        long afkSeconds = getAfkTime(player);

        if (afkSeconds >= effectiveMaxAfkTime) {
            if (!isEffectivelyAfk(player)) {
                executeActions(player, effectiveActions);
                systemPunishedPlayers.add(player.getUniqueId());
                if (configManager.isRejoinProtectionEnabled()) {
                    rejoinProtectedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
                }
            }
            return;
        }

        long autoAfkThreshold = configManager.getAutoSetAfkSeconds();
        if (autoAfkThreshold > 0 && afkSeconds >= autoAfkThreshold) {
            if (!isEffectivelyAfk(player)) {
                setAutoAfkStatus(player);
            }
        }

        if (!isEffectivelyAfk(player)) {
            checkAndSendWarning(player, afkSeconds, effectiveMaxAfkTime);
        }
    }

    /**
     * Sunucuya katılan bir oyuncunun yeniden giriş koruması altında olup olmadığını kontrol eder.
     * Eğer koruma altındaysa ve süre dolmamışsa, oyuncuyu tekrar cezalandırır.
     * @param player Sunucuya katılan oyuncu.
     */
    public void checkRejoin(Player player) {
        if (!configManager.isRejoinProtectionEnabled()) return;
        UUID uuid = player.getUniqueId();
        Long punishedTime = rejoinProtectedPlayers.getIfPresent(uuid);

        if (punishedTime != null) {
            long currentTime = System.currentTimeMillis();
            long cooldownMillis = configManager.getRejoinCooldownSeconds() * 1000;

            if ((currentTime - punishedTime) < cooldownMillis) {
                long timeLeftSeconds = (cooldownMillis - (currentTime - punishedTime)) / 1000;
                String message = lang.getMessage("rejoin_kick", "%time_left%", TimeUtil.formatTime(timeLeftSeconds));

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.sendMessage(message);
                    executeActions(player, configManager.getActions());
                }, 5L);

            } else {
                rejoinProtectedPlayers.invalidate(uuid);
            }
        }
    }

    private void checkAndSendWarning(Player player, long afkSeconds, long maxAfkTime) {
        for (Map<String, Object> warning : configManager.getWarnings()) {
            long warningTime = (long) warning.get("time");
            long timeLeft = maxAfkTime - afkSeconds;

            if (timeLeft <= warningTime) {
                Long lastWarned = lastWarningTime.getIfPresent(player.getUniqueId());
                if (lastWarned != null && lastWarned > warningTime) {
                    continue;
                }
                sendWarning(player, warning, timeLeft, maxAfkTime);
                lastWarningTime.put(player.getUniqueId(), warningTime);
                break;
            }
        }
    }

    private void sendWarning(Player player, Map<String, Object> warning, long timeLeft, long maxAfkTime) {
        String type = (String) warning.get("type");
        String message = ChatColor.translateAlternateColorCodes('&', applyPlaceholders(player, (String) warning.get("message"), timeLeft, maxAfkTime));
        String subtitle = ChatColor.translateAlternateColorCodes('&', applyPlaceholders(player, (String) warning.get("subtitle"), timeLeft, maxAfkTime));

        switch (type.toUpperCase()) {
            case "TITLE":
                player.sendTitle(lang.getPrefix() + message, lang.getPrefix() + subtitle, 10, 70, 20);
                break;
            case "ACTION_BAR":
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(lang.getPrefix() + message));
                break;
            case "CHAT":
            default:
                player.sendMessage(lang.getPrefix() + message);
                break;
        }

        String soundName = (String) warning.get("sound");
        if (soundName != null && !soundName.isEmpty()) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound name in config.yml warnings: " + soundName);
            }
        }
    }

    /**
     * Sistemi, oyuncuyu otomatik olarak AFK durumuna geçirir.
     */
    private void setAutoAfkStatus(Player player) {
        if (isEffectivelyAfk(player)) return;
        autoSetAfkPlayers.add(player.getUniqueId());
        lang.sendMessage(player, "command.afk.now_afk");
    }

    /**
     * Bir oyuncunun AFK sebebini döndürür.
     * Eğer oyuncu AFK değilse veya sebep belirtilmemişse boş bir string döndürür.
     * @param player Sebebi öğrenilecek oyuncu.
     * @return Oyuncunun AFK sebebi.
     */
    public String getAfkReason(Player player) {
        if (!isEffectivelyAfk(player)) {
            return "";
        }
        return afkReasons.getOrDefault(player.getUniqueId(), "");
    }

    public void setManualAFK(Player player, String reasonOrKey) {
        if (isEffectivelyAfk(player)) return;

        manualAFKPlayers.add(player.getUniqueId());

        if (reasonOrKey.startsWith("behavior.")) {
            autonomousPlayers.add(player.getUniqueId());
        }

        String finalReason;
        if (reasonOrKey.startsWith("behavior.") || reasonOrKey.startsWith("command.afk")) {
            finalReason = lang.getMessage(reasonOrKey).replace(lang.getPrefix(), "");
        } else {
            finalReason = reasonOrKey;
        }

        afkReasons.put(player.getUniqueId(), finalReason);

        lang.sendMessage(player, "command.afk.now_afk");

        if (configManager.isBroadcastOnAfkEnabled()) {
            String broadcastPath = "command.afk.on_afk_broadcast";
            String rawTemplate = lang.getRawMessage(broadcastPath);
            if (rawTemplate != null && !rawTemplate.isEmpty()) {
                String messageWithPlayer = rawTemplate.replace("%player_displayname%", player.getDisplayName());
                String finalMessage = messageWithPlayer.replace("%reason%", finalReason);
                lang.broadcastFormattedMessage(finalMessage);
            }
        }
    }

    /**
     * Bir oyuncuyu TÜM AFK durumlarından (manuel, otomatik, sistem) çıkarır.
     * ÖNEMLİ: Bu metot, oyuncunun "Şüpheli" durumunu ETKİLEMEZ. Bir oyuncu
     * AFK olmayabilir ama hala şüpheli olabilir.
     * @param player Durumu kaldırılacak oyuncu.
     */
    public void unsetAfkStatus(Player player) {
        UUID uuid = player.getUniqueId();
        if (!isEffectivelyAfk(player)) return;

        boolean wasManual = manualAFKPlayers.remove(uuid);

        autoSetAfkPlayers.remove(uuid);
        autonomousPlayers.remove(uuid);
        systemPunishedPlayers.remove(uuid);

        afkReasons.remove(uuid);

        lang.sendMessage(player, "command.afk.not_afk_now");

        if (wasManual && configManager.isBroadcastOnReturnEnabled()) {
            String broadcastPath = "command.afk.on_return_broadcast";
            String rawTemplate = lang.getRawMessage(broadcastPath);
            if (rawTemplate != null && !rawTemplate.isEmpty()) {

                lang.broadcastMessage(broadcastPath,
                        "%player_displayname%", player.getDisplayName()
                );
            }
        }
    }

    public void executeActions(Player player, List<Map<String, String>> actions) {
        long maxAfkTimeForAction;
        RegionOverride override = configManager.getRegionOverrideForPlayer(player);
        if(override != null && override.getMaxAfkTime() > 0) {
            maxAfkTimeForAction = override.getMaxAfkTime();
        } else {
            maxAfkTimeForAction = configManager.getMaxAfkTimeSeconds();
        }

        for (Map<String, String> action : actions) {
            String type = action.get("type");

            String command = applyPlaceholders(player, action.get("command"), 0, maxAfkTimeForAction);

            if ("PLAYER".equalsIgnoreCase(type)) {
                Bukkit.getScheduler().runTask(plugin, () -> player.performCommand(command));
            } else if ("CONSOLE".equalsIgnoreCase(type)) {
                String cleanCommand = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', command));
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cleanCommand));
            }
        }
    }

    private String applyPlaceholders(Player player, String text, long timeLeft, long maxAfkTime) {
        if (text == null || text.isEmpty()) return "";

        String currentDisplayName = originalDisplayNames.getOrDefault(player.getUniqueId(), player.getDisplayName());

        text = text.replace("%player%", player.getName())
                .replace("%player_displayname%", currentDisplayName)
                .replace("%world%", player.getWorld().getName())
                .replace("%time_left%", TimeUtil.formatTime(timeLeft))
                .replace("%max_time%", TimeUtil.formatTime(maxAfkTime));

        if (plugin.isPlaceholderApiEnabled()) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }

    /**
     * Bir oyuncunun Davranış Analizi tarafından otonom olarak işaretlenip işaretlenmediğini kontrol eder.
     * @param player Kontrol edilecek oyuncu.
     * @return Otonom olarak işaretlenmişse true.
     */
    public boolean isMarkedAsAutonomous(Player player) {
        return autonomousPlayers.contains(player.getUniqueId());
    }

    public void trackClick(Player player) {
        long currentTime = System.currentTimeMillis();

        PointlessActivityData data = botDetectionData.get(player.getUniqueId(), k -> new PointlessActivityData());

        if (data == null) return;

        if (data.getLastClickTime() > 0) {
            long interval = currentTime - data.getLastClickTime();
            data.getClickIntervals().add(interval);

            while (data.getClickIntervals().size() > configManager.getAutoClickerCheckAmount()) {
                data.getClickIntervals().remove(0);
            }

            if (data.getClickIntervals().size() == configManager.getAutoClickerCheckAmount()) {
                if (isConsistentClickPattern(data.getClickIntervals())) {
                    data.incrementAutoClickerDetections();
                    if (data.getAutoClickerDetections() >= configManager.getAutoClickerDetectionsToPunish()) {
                        setManualAFK(player, "behavior.autoclicker_detected");

                        data.resetAutoClickerDetections();
                        data.getClickIntervals().clear();
                    }
                } else {

                    data.resetAutoClickerDetections();
                }
            }
        }
        data.setLastClickTime(currentTime);
    }

    /**
     * Verilen tıklama aralıklarının tutarlı bir desen oluşturup oluşturmadığını kontrol eder.
     */
    private boolean isConsistentClickPattern(List<Long> intervals) {
        if (intervals.isEmpty()) return false;

        long total = 0;
        for (long interval : intervals) {
            total += interval;
        }
        long average = total / intervals.size();

        for (long interval : intervals) {
            if (Math.abs(interval - average) > configManager.getAutoClickerMaxDeviation()) {
                return false;
            }
        }
        return true;
    }

    public PointlessActivityData getBotDetectionData(UUID uuid) {
        return botDetectionData.get(uuid, k -> new PointlessActivityData());
    }

    public void resetBotDetectionData(UUID uuid) {
        botDetectionData.invalidate(uuid);
    }

    /**
     * Bir oyuncu Turing Testi'ni başarıyla geçtiğinde, onunla ilgili
     * tüm şüpheye dayalı verileri ve durumları sıfırlar.
     * @param player Testi geçen oyuncu.
     */
    public void resetSuspicion(Player player) {

        suspiciousPlayers.remove(player.getUniqueId());

        resetBotDetectionData(player.getUniqueId());

        if (plugin.getBehaviorAnalysisManager().isEnabled()) {
            plugin.getBehaviorAnalysisManager().getPlayerData(player).reset();
        }
    }

    /**
     * Bir oyuncunun şüpheli modda olup olmadığını kontrol eder.
     * @param player Kontrol edilecek oyuncu.
     * @return Oyuncu şüpheliyse true.
     */
    public boolean isSuspicious(Player player) {
        return suspiciousPlayers.contains(player.getUniqueId());
    }

    /**
     * Bir oyuncuyu şüpheli moda sokar. Bu, CaptchaManager tarafından çağrılır.
     * @param player Şüpheli moda sokulacak oyuncu.
     */
    public void setSuspicious(Player player) {
        suspiciousPlayers.add(player.getUniqueId());
    }

    /**
     * Sunucudaki AFK olan tüm oyuncuların bir listesini döndürür.
     * @return AFK olan oyuncuların listesi.
     */
    public List<Player> getAfkPlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(this::isEffectivelyAfk)
                .collect(Collectors.toList());
    }
}