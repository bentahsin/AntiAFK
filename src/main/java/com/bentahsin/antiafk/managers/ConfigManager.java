package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.models.RegionOverride;
import com.bentahsin.antiafk.utils.TimeUtil;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConfigManager {

    private final AntiAFKPlugin plugin;

    private long maxAfkTimeSeconds;
    private boolean checkCamera, checkChat, checkInteraction, checkToggleSneak, checkItemDrop;
    private boolean checkInventoryActivity, checkItemConsume, checkHeldItemChange, checkPlayerAttack, checkBookActivity;
    private List<Map<String, String>> captchaFailureActions;
    private List<Map<String, String>> actions;
    private List<Map<String, Object>> warnings;
    private String permBypassAll;
    private String permBypassClassic;
    private String permBypassBehavioral;
    private String permBypassPointless;
    private String permBypassAutoclicker;
    private String permAfkCommandUse;
    private List<String> disabledWorlds;
    private List<String> exemptGameModes;
    private boolean worldGuardEnabled;
    private List<RegionOverride> regionOverrides;
    private boolean afkCommandEnabled;
    private String afkDefaultReason;
    private boolean setInvulnerable;
    private String afkTagFormat;
    private long autoSetAfkSeconds;
    private int maxPointlessActivities;
    private boolean autoClickerEnabled;
    private int autoClickerCheckAmount;
    private long autoClickerMaxDeviation;
    private int autoClickerDetectionsToPunish;
    private boolean checkWorldChangeEnabled;
    private int worldChangeCooldown;
    private int maxWorldChanges;
    private boolean rejoinProtectionEnabled;
    private long rejoinCooldownSeconds;
    private boolean turingTestEnabled;
    private int answerTimeoutSeconds;
    private int triggerOnPointlessActivityCount;
    private int triggerOnBehavioralRepeatCount;
    private boolean broadcastOnAfk;
    private boolean broadcastOnReturn;

    private final LoadingCache<UUID, Optional<RegionOverride>> regionCache;

    public ConfigManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;

        this.regionCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) {
                        return Optional.empty();
                    }
                    return findRegionOverrideForPlayer(player);
                });

        loadConfig();
    }

    private Optional<RegionOverride> findRegionOverrideForPlayer(Player player) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = regionContainer.createQuery();
        ApplicableRegionSet applicableRegions = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));

        for (RegionOverride override : regionOverrides) {
            for (ProtectedRegion region : applicableRegions) {
                if (override.getRegionName().equalsIgnoreCase(region.getId())) {
                    return Optional.of(override);
                }
            }
        }
        return Optional.empty();
    }

    public void loadConfig() {
        plugin.reloadConfig();
        final FileConfiguration config = plugin.getConfig();

        rejoinProtectionEnabled = config.getBoolean("rejoin_protection.enabled", true);
        rejoinCooldownSeconds = TimeUtil.parseTime(config.getString("rejoin_protection.cooldown", "5m"));
        maxAfkTimeSeconds = TimeUtil.parseTime(config.getString("max_afk_time", "15m"));
        autoSetAfkSeconds = TimeUtil.parseTime(config.getString("detection.auto_set_afk_after", "disabled"));
        checkCamera = config.getBoolean("detection.check_camera_movement", true);
        checkChat = config.getBoolean("detection.check_chat_activity", true);
        checkInteraction = config.getBoolean("detection.check_interaction", true);
        checkToggleSneak = config.getBoolean("detection.check_toggle_sneak", true);
        checkItemDrop = config.getBoolean("detection.check_item_drop", true);
        checkInventoryActivity = config.getBoolean("detection.check_inventory_activity", true);
        checkItemConsume = config.getBoolean("detection.check_item_consume", true);
        checkHeldItemChange = config.getBoolean("detection.check_held_item_change", true);
        checkPlayerAttack = config.getBoolean("detection.check_player_attack", true);
        checkBookActivity = config.getBoolean("detection.check_book_activity", true);
        maxPointlessActivities = config.getInt("detection.max-pointless-activities", 15);
        autoClickerEnabled = config.getBoolean("detection.auto-clicker.enabled", true);
        autoClickerCheckAmount = config.getInt("detection.auto-clicker.check-amount", 20);
        autoClickerMaxDeviation = config.getLong("detection.auto-clicker.max-deviation", 10);
        autoClickerDetectionsToPunish = config.getInt("detection.auto-clicker.detections-to-punish", 3);
        checkWorldChangeEnabled = config.getBoolean("detection.check-world-change.enabled", true);
        worldChangeCooldown = config.getInt("detection.check-world-change.cooldown", 20);
        maxWorldChanges = config.getInt("detection.check-world-change.max-changes", 5);
        turingTestEnabled = config.getBoolean("turing_test.enabled", true);
        answerTimeoutSeconds = config.getInt("turing_test.answer_timeout_seconds", 20);
        triggerOnPointlessActivityCount = config.getInt("turing_test.trigger_on_pointless_activity_count", 10);
        triggerOnBehavioralRepeatCount = config.getInt("turing_test.trigger_on_behavioral_repeat_count", 1);

        this.captchaFailureActions = new ArrayList<>();
        if (config.isList("turing_test.on_failure_actions")) {
            List<Map<?, ?>> rawActionList = config.getMapList("turing_test.on_failure_actions");
            for (Map<?, ?> rawMap : rawActionList) {
                Map<String, String> actionMap = new HashMap<>();
                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    actionMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
                this.captchaFailureActions.add(actionMap);
            }
        }

        this.actions = new ArrayList<>();
        if (config.isList("actions")) {
            List<Map<?, ?>> rawActionList = config.getMapList("actions");
            for (Map<?, ?> rawMap : rawActionList) {
                Map<String, String> actionMap = new HashMap<>();
                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    actionMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
                this.actions.add(actionMap);
            }
        } else {
            plugin.getLogger().warning("'actions' bölümü config.yml'de bulunamadı veya liste formatında değil.");
        }

        this.warnings = new ArrayList<>();
        if (config.isList("warnings")) {
            List<Map<?, ?>> rawWarnings = config.getMapList("warnings");
            for (Map<?, ?> rawMap : rawWarnings) {
                Map<String, Object> warningMap = new HashMap<>();
                Object timeObj = rawMap.get("time");
                warningMap.put("time", TimeUtil.parseTime(timeObj != null ? timeObj.toString() : "0s"));
                Object typeObj = rawMap.get("type");
                warningMap.put("type", (typeObj != null ? typeObj.toString() : "CHAT").toUpperCase());
                Object messageObj = rawMap.get("message");
                warningMap.put("message", messageObj != null ? messageObj.toString() : "");
                Object titleObj = rawMap.get("title");
                warningMap.put("title", titleObj != null ? titleObj.toString() : "");
                Object subtitleObj = rawMap.get("subtitle");
                warningMap.put("subtitle", subtitleObj != null ? subtitleObj.toString() : "");
                Object soundObj = rawMap.get("sound");
                warningMap.put("sound", (soundObj != null ? soundObj.toString() : "").toUpperCase());
                this.warnings.add(warningMap);
            }
        } else {
            plugin.getLogger().warning("'warnings' bölümü config.yml'de bulunamadı veya liste formatında değil.");
        }

        worldGuardEnabled = config.getBoolean("worldguard_integration.enabled", false);
        regionOverrides = new ArrayList<>();
        if (worldGuardEnabled) {
            ConfigurationSection regionSection = config.getConfigurationSection("worldguard_integration.region_overrides");
            if (regionSection != null) {
                for (String key : regionSection.getKeys(false)) {
                    String path = "worldguard_integration.region_overrides." + key;
                    String regionName = config.getString(path + ".region");
                    if (regionName == null || regionName.isEmpty()) continue;

                    long time = TimeUtil.parseTime(config.getString(path + ".max_afk_time", "disabled"));
                    List<Map<String, String>> regionActions;

                    if (config.isList(path + ".actions")) {
                        List<Map<String, String>> tempRegionActions = new ArrayList<>();
                        List<Map<?, ?>> rawRegionActions = config.getMapList(path + ".actions");
                        for (Map<?, ?> rawMap : rawRegionActions) {
                            Map<String, String> actionMap = new HashMap<>();
                            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                                actionMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                            }
                            tempRegionActions.add(actionMap);
                        }
                        regionActions = tempRegionActions;
                    } else {
                        regionActions = this.actions;
                    }

                    regionOverrides.add(new RegionOverride(regionName, time, regionActions));
                }
            }
        }

        afkCommandEnabled = config.getBoolean("afk_command.enabled", true);
        afkDefaultReason = config.getString("afk_command.on_afk.default_reason", "Sebep belirtilmedi");
        setInvulnerable = config.getBoolean("afk_command.on_afk.set_invulnerable", true);
        afkTagFormat = config.getString("afk_command.on_afk.tag_format", "&7[AFK] ");
        broadcastOnAfk = config.getBoolean("afk_command.on_afk.broadcast", true);
        broadcastOnReturn = config.getBoolean("afk_command.on_return.broadcast", true);
        permBypassAll = config.getString("exemptions.permissions.bypass_all", "antiafk.bypass.all");
        permBypassClassic = config.getString("exemptions.permissions.bypass_classic", "antiafk.bypass.classic");
        permBypassBehavioral = config.getString("exemptions.permissions.bypass_behavioral", "antiafk.bypass.behavioral");
        permBypassPointless = config.getString("exemptions.permissions.bypass_pointless", "antiafk.bypass.pointless");
        permBypassAutoclicker = config.getString("exemptions.permissions.bypass_autoclicker", "antiafk.bypass.autoclicker");
        permAfkCommandUse = config.getString("afk_command.permission", "antiafk.command.afk");
        disabledWorlds = config.getStringList("exemptions.disabled_worlds");
        exemptGameModes = config.getStringList("exemptions.exempt_gamemodes");
        warnings.sort((w1, w2) -> Long.compare((long) w2.get("time"), (long) w1.get("time")));

        this.regionCache.invalidateAll();
    }

    /**
     * Oyuncu önbellekten çıktığında veya konumu değiştiğinde veriyi geçersiz kılar.
     * @param player Önbelleği temizlenecek oyuncu.
     */
    public void clearPlayerCache(Player player) {
        regionCache.invalidate(player.getUniqueId());
    }

    public long getMaxAfkTimeSeconds() { return maxAfkTimeSeconds; }
    public boolean isCheckCamera() { return checkCamera; }
    public boolean isCheckChat() { return checkChat; }
    public boolean isCheckInteraction() { return checkInteraction; }
    public List<Map<String, String>> getActions() { return actions; }
    public List<Map<String, Object>> getWarnings() { return warnings; }
    public String getPermBypassAll() { return permBypassAll; }
    public String getPermBypassClassic() { return permBypassClassic; }
    public String getPermBypassBehavioral() { return permBypassBehavioral; }
    public String getPermBypassPointless() { return permBypassPointless; }
    public String getPermBypassAutoclicker() { return permBypassAutoclicker; }
    public List<String> getDisabledWorlds() { return disabledWorlds; }
    public List<String> getExemptGameModes() { return exemptGameModes; }
    public String getAfkTagFormat() { return afkTagFormat; }
    public boolean isSetInvulnerable() { return setInvulnerable; }
    public boolean isWorldGuardEnabled() { return worldGuardEnabled; }
    public boolean isAfkCommandEnabled() { return afkCommandEnabled; }
    public String getAfkDefaultReason() { return afkDefaultReason; }
    public List<RegionOverride> getRegionOverrides() { return regionOverrides;}
    public long getAutoSetAfkSeconds() { return autoSetAfkSeconds; }
    public boolean isCheckToggleSneak() { return checkToggleSneak; }
    public boolean isCheckItemDrop() { return checkItemDrop; }
    public boolean isCheckInventoryActivity() { return checkInventoryActivity; }
    public boolean isCheckItemConsume() { return checkItemConsume; }
    public boolean isCheckHeldItemChange() { return checkHeldItemChange; }
    public boolean isCheckPlayerAttack() { return checkPlayerAttack; }
    public boolean isCheckBookActivity() { return checkBookActivity; }
    public int getMaxPointlessActivities() { return maxPointlessActivities; }
    public boolean isAutoClickerEnabled() { return autoClickerEnabled; }
    public int getAutoClickerCheckAmount() { return autoClickerCheckAmount; }
    public long getAutoClickerMaxDeviation() { return autoClickerMaxDeviation; }
    public int getAutoClickerDetectionsToPunish() { return autoClickerDetectionsToPunish; }
    public String getPermAfkCommandUse() { return permAfkCommandUse; }
    public boolean isCheckWorldChangeEnabled() { return checkWorldChangeEnabled; }
    public int getWorldChangeCooldown() { return worldChangeCooldown; }
    public int getMaxWorldChanges() { return maxWorldChanges; }
    public boolean isRejoinProtectionEnabled() { return rejoinProtectionEnabled; }
    public long getRejoinCooldownSeconds() { return rejoinCooldownSeconds; }
    public boolean isTuringTestEnabled() { return turingTestEnabled; }
    public int getAnswerTimeoutSeconds() { return answerTimeoutSeconds; }
    public int getTriggerOnPointlessActivityCount() { return triggerOnPointlessActivityCount; }
    public int getTriggerOnBehavioralRepeatCount() { return triggerOnBehavioralRepeatCount; }

    /**
     * Oyuncu Turing Testi'nde başarısız olduğunda çalıştırılacak olan
     * özel komut eylemlerinin listesini döndürür.
     * @return Eylemlerin bir listesi. Eğer tanımlanmamışsa boş bir liste döner.
     */
    public List<Map<String, String>> getCaptchaFailureActions() {

        return captchaFailureActions != null ? captchaFailureActions : Collections.emptyList();
    }

    /**
     * Bir oyuncu için geçerli olan Bölge Kuralını (RegionOverride) döndürür.
     * Bu metot, sonucu Caffeine önbelleğinden alır.
     * @param player Kuralın aranacağı oyuncu.
     * @return Oyuncunun bölgesine ait kural, yoksa null.
     */
    public RegionOverride getRegionOverrideForPlayer(Player player) {
        if (!worldGuardEnabled || !plugin.isWorldGuardHooked()) {
            return null;
        }

        return Objects.requireNonNull(regionCache.get(player.getUniqueId())).orElse(null);
    }

    public boolean isBroadcastOnAfkEnabled() {
        return broadcastOnAfk;
    }
    public boolean isBroadcastOnReturnEnabled() {
        return broadcastOnReturn;
    }
}