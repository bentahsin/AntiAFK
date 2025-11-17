package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.SupportedLanguage;
import com.bentahsin.antiafk.models.PunishmentLevel;
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

    private SupportedLanguage language;

    private long maxAfkTimeSeconds;
    private boolean checkCamera, checkChat, checkInteraction, checkToggleSneak, checkItemDrop;
    private boolean checkInventoryActivity, checkItemConsume, checkHeldItemChange, checkPlayerAttack, checkBookActivity;
    private List<Map<String, String>> captchaFailureActions;
    private List<Map<String, String>> actions;
    private List<Map<String, Object>> warnings;

    private String permBypassAll, permBypassClassic, permBypassBehavioral, permBypassPointless, permBypassAutoclicker;
    private String permAfkCommandUse;

    private List<String> disabledWorlds;
    private List<String> exemptGameModes;

    private boolean worldGuardEnabled;
    private List<RegionOverride> regionOverrides;
    private Map<String, RegionOverride> regionOverrideMap;

    private boolean afkCommandEnabled;
    private String afkDefaultReason;
    private boolean setInvulnerable;
    private String afkTagFormat;
    private boolean broadcastOnAfk, broadcastOnReturn;

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
    private int qaCaptchaTimeoutSeconds;
    private int colorPaletteGuiRows;
    private int colorPaletteTimeLimit;
    private int colorPaletteCorrectCount;
    private int colorPaletteDistractorColorCount;
    private int colorPaletteDistractorItemCount;
    private List<String> colorPaletteAvailableColors;

    private boolean progressivePunishmentEnabled;
    private long punishmentResetMillis;
    private List<PunishmentLevel> punishmentLevels;
    private int highestPunishmentCount;

    private boolean learningModeEnabled;
    private long analysisTaskPeriodTicks;
    private double learningSimilarityThreshold;
    private int learningSearchRadius;

    private long maxPatternFileSizeKb;
    private int maxVectorsPerPattern;
    private double preFilterSizeRatio;

    private final LoadingCache<UUID, Optional<RegionOverride>> regionCache;

    public ConfigManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.regionCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) return Optional.empty();
                    return findRegionOverrideForPlayer(player);
                });

        loadConfig();
    }

    private Map<String, String> convertToStringMap(Map<?, ?> rawMap) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return result;
    }

    private Optional<RegionOverride> findRegionOverrideForPlayer(Player player) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = regionContainer.createQuery();
        ApplicableRegionSet applicableRegions = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));

        for (ProtectedRegion region : applicableRegions) {
            RegionOverride override = regionOverrideMap.get(region.getId().toLowerCase());
            if (override != null) return Optional.of(override);
        }
        return Optional.empty();
    }

    private Object getOrDefault(Map<?, ?> map, String key, Object defaultValue) {
        return map.containsKey(key) ? map.get(key) : defaultValue;
    }

    public void loadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        String langFrConf = config.getString("lang", "Turkish");
        this.language = SupportedLanguage.fromConfigName(langFrConf);

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
        qaCaptchaTimeoutSeconds = config.getInt("turing_test.question_answer.answer_timeout_seconds", 20);
        colorPaletteGuiRows = config.getInt("turing_test.color_palette.gui_rows", 3);
        colorPaletteTimeLimit = config.getInt("turing_test.color_palette.time_limit_seconds", 15);
        colorPaletteCorrectCount = config.getInt("turing_test.color_palette.correct_item_count", 5);
        colorPaletteDistractorColorCount = config.getInt("turing_test.color_palette.distractor_color_count", 2);
        colorPaletteDistractorItemCount = config.getInt("turing_test.color_palette.distractor_item_count_per_color", 4);
        colorPaletteAvailableColors = config.getStringList("turing_test.color_palette.available_colors");

        learningModeEnabled = config.getBoolean("learning_mode.enabled", true);
        analysisTaskPeriodTicks = config.getLong("learning_mode.analysis_task_period_ticks", 40L);
        learningSimilarityThreshold = config.getDouble("learning_mode.similarity_threshold", 25.0);
        learningSearchRadius = config.getInt("learning_mode.search_radius", 10);


        maxPatternFileSizeKb = config.getLong("learning_mode.security.max_pattern_file_size_kb", 1024);
        maxVectorsPerPattern = config.getInt("learning_mode.security.max_vectors_per_pattern", 12000);
        preFilterSizeRatio = config.getDouble("learning_mode.security.pre_filter_size_ratio", 0.5);

        captchaFailureActions = new ArrayList<>();
        for (Map<?, ?> rawMap : config.getMapList("turing_test.on_failure_actions")) {
            captchaFailureActions.add(convertToStringMap(rawMap));
        }

        actions = new ArrayList<>();
        if (config.isList("actions")) {
            for (Map<?, ?> rawMap : config.getMapList("actions")) {
                actions.add(convertToStringMap(rawMap));
            }
        }

        warnings = new ArrayList<>();
        if (config.isList("warnings")) {
            for (Map<?, ?> rawMap : config.getMapList("warnings")) {
                Map<String, Object> warningMap = new HashMap<>();
                warningMap.put("time", TimeUtil.parseTime(String.valueOf(getOrDefault(rawMap, "time", "0s"))));
                warningMap.put("type", String.valueOf(getOrDefault(rawMap, "type", "CHAT")).toUpperCase());
                warningMap.put("message", String.valueOf(getOrDefault(rawMap, "message", "")));
                warningMap.put("title", String.valueOf(getOrDefault(rawMap, "title", "")));
                warningMap.put("subtitle", String.valueOf(getOrDefault(rawMap, "subtitle", "")));
                warningMap.put("sound", String.valueOf(getOrDefault(rawMap, "sound", "")).toUpperCase());
                warnings.add(warningMap);
            }
        }
        warnings.sort((w1, w2) -> Long.compare(((Number) w2.get("time")).longValue(), ((Number) w1.get("time")).longValue()));

        worldGuardEnabled = config.getBoolean("worldguard_integration.enabled", false);
        regionOverrides = new ArrayList<>();
        if (worldGuardEnabled) {
            ConfigurationSection section = config.getConfigurationSection("worldguard_integration.region_overrides");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    String path = "worldguard_integration.region_overrides." + key;
                    String regionName = config.getString(path + ".region");
                    if (regionName == null || regionName.isEmpty()) continue;

                    long time = TimeUtil.parseTime(config.getString(path + ".max_afk_time", "disabled"));

                    List<Map<String, String>> regionActions = new ArrayList<>();
                    if (config.isList(path + ".actions")) {
                        for (Map<?, ?> rawMap : config.getMapList(path + ".actions")) {
                            regionActions.add(convertToStringMap(rawMap));
                        }
                    } else {
                        regionActions = this.actions;
                    }

                    regionOverrides.add(new RegionOverride(regionName, time, regionActions));
                }
            }
            rebuildRegionOverrideMap();
        }

        afkCommandEnabled = config.getBoolean("afk_command.enabled", true);
        afkDefaultReason = config.getString("afk_command.on_afk.default_reason", "No reason specified");
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

        progressivePunishmentEnabled = config.getBoolean("progressive_punishment.enabled", true);
        String resetAfterStr = config.getString("progressive_punishment.reset_after", "30d");

        punishmentResetMillis = (resetAfterStr != null && resetAfterStr.equalsIgnoreCase("never"))
                ? -1
                : TimeUtil.parseTime(resetAfterStr) * 1000;

        punishmentLevels = new ArrayList<>();
        for (Map<?, ?> levelMap : config.getMapList("progressive_punishment.punishments")) {
            Object countObj = levelMap.get("count");
            int count = (countObj instanceof Number) ? ((Number) countObj).intValue() : 0;

            if (count > highestPunishmentCount) {
                highestPunishmentCount = count;
            }

            List<Map<String, String>> levelActions = new ArrayList<>();
            Object actionsObj = levelMap.get("actions");
            if (actionsObj instanceof List<?>) {
                for (Object obj : (List<?>) actionsObj) {
                    if (obj instanceof Map<?, ?>) {
                        levelActions.add(convertToStringMap((Map<?, ?>) obj));
                    }
                }
            }

            punishmentLevels.add(new PunishmentLevel(count, levelActions));
        }
        Collections.sort(punishmentLevels);

        regionCache.invalidateAll();
    }

    private void rebuildRegionOverrideMap() {
        regionOverrideMap = new HashMap<>();
        for (RegionOverride ro : regionOverrides) {
            regionOverrideMap.put(ro.getRegionName().toLowerCase(), ro);
        }
    }

    public void clearPlayerCache(Player player) {
        regionCache.invalidate(player.getUniqueId());
    }

    public RegionOverride getRegionOverrideForPlayer(Player player) {
        if (!worldGuardEnabled || !plugin.isWorldGuardHooked()) return null;
        return Objects.requireNonNull(regionCache.get(player.getUniqueId())).orElse(null);
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
    public List<RegionOverride> getRegionOverrides() { return regionOverrides; }
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
    public int getQaCaptchaTimeoutSeconds() { return qaCaptchaTimeoutSeconds; }
    public int getColorPaletteGuiRows() { return colorPaletteGuiRows; }
    public int getColorPaletteTimeLimit() { return colorPaletteTimeLimit; }
    public int getColorPaletteCorrectCount() { return colorPaletteCorrectCount; }
    public int getColorPaletteDistractorColorCount() { return colorPaletteDistractorColorCount; }
    public int getColorPaletteDistractorItemCount() { return colorPaletteDistractorItemCount; }
    public List<String> getColorPaletteAvailableColors() { return Collections.unmodifiableList(colorPaletteAvailableColors); }
    public List<Map<String, String>> getCaptchaFailureActions() { return captchaFailureActions; }
    public boolean isBroadcastOnAfkEnabled() { return broadcastOnAfk; }
    public boolean isBroadcastOnReturnEnabled() { return broadcastOnReturn; }
    public boolean isProgressivePunishmentEnabled() { return progressivePunishmentEnabled; }
    public long getPunishmentResetMillis() { return punishmentResetMillis; }
    public int getHighestPunishmentCount() { return highestPunishmentCount; }
    public List<PunishmentLevel> getPunishmentLevels() { return punishmentLevels; }
    public boolean isLearningModeEnabled() { return learningModeEnabled; }
    public long getAnalysisTaskPeriodTicks() { return analysisTaskPeriodTicks; }
    public double getLearningSimilarityThreshold() { return learningSimilarityThreshold; }
    public int getLearningSearchRadius() { return learningSearchRadius; }
    public long getMaxPatternFileSizeBytes() { return maxPatternFileSizeKb * 1024; }
    public int getMaxVectorsPerPattern() { return maxVectorsPerPattern; }
    public double getPreFilterSizeRatio() { return preFilterSizeRatio; }
    public SupportedLanguage getLang() { return language; }
}