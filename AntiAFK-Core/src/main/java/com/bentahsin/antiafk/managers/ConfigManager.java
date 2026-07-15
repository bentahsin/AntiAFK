package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.api.region.IRegionProvider;
import com.bentahsin.antiafk.config.AbstractYamlConfig;
import com.bentahsin.antiafk.language.SupportedLanguage;
import com.bentahsin.antiafk.models.PunishmentLevel;
import com.bentahsin.antiafk.models.RegionOverride;
import com.bentahsin.antiafk.utils.TimeUtil;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Singleton
@SuppressWarnings({"FieldCanBeLocal", "unused", "unchecked"})
public class ConfigManager extends AbstractYamlConfig {

    private final AntiAFKPlugin plugin;
    private final List<IRegionProvider> regionProviders = new CopyOnWriteArrayList<>();
    private final LoadingCache<UUID, Optional<RegionOverride>> regionCache;

    private String languageName;
    private SupportedLanguage language;

    private long maxAfkTimeSeconds;
    private List<Map<String, Object>> warnings = new ArrayList<>();
    private List<Map<String, String>> actions = new ArrayList<>();

    private long autoSetAfkSeconds;
    private int maxPointlessActivities;
    private boolean checkCamera;
    private boolean checkChat;
    private boolean checkInteraction;
    private boolean checkToggleSneak;
    private boolean checkItemDrop;
    private boolean checkInventoryActivity;
    private boolean checkItemConsume;
    private boolean checkPlayerAttack;
    private boolean checkHeldItemChange;
    private boolean checkBookActivity;

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
    private List<Map<String, String>> captchaFailureActions = new ArrayList<>();
    private int colorPaletteGuiRows;
    private int colorPaletteTimeLimit;
    private int colorPaletteCorrectCount;
    private int colorPaletteDistractorColorCount;
    private int colorPaletteDistractorItemCount;
    private List<String> colorPaletteAvailableColors = new ArrayList<>();

    private boolean learningModeEnabled;
    private long analysisTaskPeriodTicks;
    private double learningSimilarityThreshold;
    private int learningSearchRadius;
    private long maxPatternFileSizeKb;
    private int maxVectorsPerPattern;
    private double preFilterSizeRatio;

    private boolean behaviorAnalysisEnabled;
    private int behaviorHistorySizeTicks;
    private boolean confinementCheckEnabled;
    private long confinementCheckDurationSeconds;
    private double confinementRadius;
    private double confinementMinDistance;

    private boolean discordWebhookEnabled;
    private String discordWebhookUrl;
    private String discordBotName;
    private String discordAvatarUrl;

    private boolean essentialsSync;

    private boolean economyEnabled;
    private boolean taxEnabled;
    private double taxAmount;
    private int taxInterval;
    private boolean salaryEnabled;
    private Map<String, Double> salaryRegions = new HashMap<>();
    private int salaryInterval;

    private boolean farmLimiterEnabled;
    private int farmLimiterRadius;
    private boolean farmLimiterCheckFriends;

    private boolean itemPickupBlockerEnabled;

    private String permBypassAll;
    private String permBypassClassic;
    private String permBypassBehavioral;
    private String permBypassPointless;
    private String permBypassAutoclicker;
    private String permAfkCommandUse;
    private List<String> disabledWorlds = new ArrayList<>();
    private List<String> exemptGameModes = new ArrayList<>();

    private boolean afkCommandEnabled;
    private String afkDefaultReason;
    private boolean setInvulnerable;
    private String afkTagFormat;
    private boolean broadcastOnAfk;
    private boolean broadcastOnReturn;

    private String mainCommandName;
    private List<String> mainCommandAliases = new ArrayList<>();
    private String afkCommandName;
    private List<String> afkCommandAliases = new ArrayList<>();
    private String afkTestCommandName;
    private List<String> afkTestCommandAliases = new ArrayList<>();
    private String afkCevapCommandName;
    private List<String> afkCevapCommandAliases = new ArrayList<>();

    private boolean worldGuardEnabled;
    private List<RegionOverride> regionOverrides = new ArrayList<>();
    private Map<String, RegionOverride> regionOverrideMap = new HashMap<>();

    private boolean progressivePunishmentEnabled;
    private String punishmentResetAfterStr;
    private long punishmentResetMillis;
    private List<PunishmentLevel> punishmentLevels = new ArrayList<>();
    private int highestPunishmentCount = 0;

    public ConfigManager() {
        super(null, "config.yml");
        this.plugin = null;
        this.regionCache = null;
    }

    @Inject
    public ConfigManager(AntiAFKPlugin plugin) {
        super(plugin, "config.yml");
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

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        reload();
    }

    @Override
    protected void onLoad() {
        this.languageName = getString("lang", "English");
        this.language = SupportedLanguage.fromConfigName(languageName);

        this.maxAfkTimeSeconds = TimeUtil.parseTime(getString("max_afk_time", "15m"));
        loadWarnings();
        this.actions = getStringMapList("actions");

        this.autoSetAfkSeconds = TimeUtil.parseTime(getString("detection.auto_set_afk_after", "1m"));
        this.maxPointlessActivities = getInt("detection.max-pointless-activities", 15);
        this.checkCamera = getBoolean("detection.check_camera_movement", true);
        this.checkChat = getBoolean("detection.check_chat_activity", true);
        this.checkInteraction = getBoolean("detection.check_interaction", true);
        this.checkToggleSneak = getBoolean("detection.check_toggle_sneak", true);
        this.checkItemDrop = getBoolean("detection.check_item_drop", true);
        this.checkInventoryActivity = getBoolean("detection.check_inventory_activity", true);
        this.checkItemConsume = getBoolean("detection.check_item_consume", true);
        this.checkPlayerAttack = getBoolean("detection.check_player_attack", true);
        this.checkHeldItemChange = getBoolean("detection.check_held_item_change", true);
        this.checkBookActivity = getBoolean("detection.check_book_activity", true);

        this.autoClickerEnabled = getBoolean("detection.auto-clicker.enabled", true);
        this.autoClickerCheckAmount = getInt("detection.auto-clicker.check-amount", 20);
        this.autoClickerMaxDeviation = getLong("detection.auto-clicker.max-deviation", 10);
        this.autoClickerDetectionsToPunish = getInt("detection.auto-clicker.detections-to-punish", 3);

        this.checkWorldChangeEnabled = getBoolean("detection.check-world-change.enabled", true);
        this.worldChangeCooldown = getInt("detection.check-world-change.cooldown", 20);
        this.maxWorldChanges = getInt("detection.check-world-change.max-changes", 5);

        this.rejoinProtectionEnabled = getBoolean("rejoin_protection.enabled", true);
        this.rejoinCooldownSeconds = TimeUtil.parseTime(getString("rejoin_protection.cooldown", "5m"));

        this.turingTestEnabled = getBoolean("turing_test.enabled", true);
        this.qaCaptchaTimeoutSeconds = getInt("turing_test.question_answer.answer_timeout_seconds", 20);
        this.captchaFailureActions = getStringMapList("turing_test.on_failure_actions");
        this.colorPaletteGuiRows = getInt("turing_test.color_palette.gui_rows", 3);
        this.colorPaletteTimeLimit = getInt("turing_test.color_palette.time_limit_seconds", 15);
        this.colorPaletteCorrectCount = getInt("turing_test.color_palette.correct_item_count", 5);
        this.colorPaletteDistractorColorCount = getInt("turing_test.color_palette.distractor_color_count", 2);
        this.colorPaletteDistractorItemCount = getInt("turing_test.color_palette.distractor_item_count_per_color", 4);
        this.colorPaletteAvailableColors = getStringList("turing_test.color_palette.available_colors");

        this.learningModeEnabled = getBoolean("learning_mode.enabled", true);
        this.analysisTaskPeriodTicks = getLong("learning_mode.analysis_task_period_ticks", 40L);
        this.learningSimilarityThreshold = getDouble("learning_mode.similarity_threshold", 25.0);
        this.learningSearchRadius = getInt("learning_mode.search_radius", 10);
        this.maxPatternFileSizeKb = getLong("learning_mode.security.max_pattern_file_size_kb", 1024);
        this.maxVectorsPerPattern = getInt("learning_mode.security.max_vectors_per_pattern", 12000);
        this.preFilterSizeRatio = getDouble("learning_mode.security.pre_filter_size_ratio", 0.5);

        this.behaviorAnalysisEnabled = getBoolean("behavioral-analysis.enabled", false);
        this.behaviorHistorySizeTicks = getInt("behavioral-analysis.history-size-ticks", 600);
        this.confinementCheckEnabled = getBoolean("behavioral-analysis.confinement-check.enabled", true);
        this.confinementCheckDurationSeconds = TimeUtil.parseTime(getString("behavioral-analysis.confinement-check.check-duration", "20m"));
        this.confinementRadius = getDouble("behavioral-analysis.confinement-check.radius", 3.0);
        this.confinementMinDistance = getDouble("behavioral-analysis.confinement-check.min-distance-traveled", 100.0);

        this.discordWebhookEnabled = getBoolean("discord_webhook.enabled", false);
        this.discordWebhookUrl = getString("discord_webhook.webhook_url", "PASTE_YOUR_WEBHOOK_URL_HERE");
        this.discordBotName = getString("discord_webhook.bot_name", "AntiAFK Guard");
        this.discordAvatarUrl = getString("discord_webhook.avatar_url", "");

        this.essentialsSync = getBoolean("integrations.essentials.sync_status", true);

        this.economyEnabled = getBoolean("integrations.economy.enabled", false);
        this.taxEnabled = getBoolean("integrations.economy.tax.enabled", false);
        this.taxAmount = getDouble("integrations.economy.tax.amount", 10.0);
        this.taxInterval = getInt("integrations.economy.tax.interval_minutes", 5);
        this.salaryEnabled = getBoolean("integrations.economy.salary.enabled", false);
        this.salaryRegions = loadSalaryRegions();
        this.salaryInterval = getInt("integrations.economy.salary.interval_minutes", 10);

        this.farmLimiterEnabled = getBoolean("gameplay_control.farm_limiter.enabled", false);
        this.farmLimiterRadius = getInt("gameplay_control.farm_limiter.radius", 32);
        this.farmLimiterCheckFriends = getBoolean("gameplay_control.farm_limiter.allow_if_active_player_nearby", true);

        this.itemPickupBlockerEnabled = getBoolean("gameplay_control.item_pickup_blocker.enabled", false);

        this.permBypassAll = getString("exemptions.permissions.bypass_all", "antiafk.bypass.all");
        this.permBypassClassic = getString("exemptions.permissions.bypass_classic", "antiafk.bypass.classic");
        this.permBypassBehavioral = getString("exemptions.permissions.bypass_behavioral", "antiafk.bypass.behavioral");
        this.permBypassPointless = getString("exemptions.permissions.bypass_pointless", "antiafk.bypass.pointless");
        this.permBypassAutoclicker = getString("exemptions.permissions.bypass_autoclicker", "antiafk.bypass.autoclicker");
        this.permAfkCommandUse = getString("afk_command.permission", "antiafk.command.afk");
        this.disabledWorlds = getStringList("exemptions.disabled_worlds");
        this.exemptGameModes = getStringList("exemptions.exempt_gamemodes");

        this.afkCommandEnabled = getBoolean("afk_command.enabled", true);
        this.afkDefaultReason = getString("afk_command.on_afk.default_reason", "No reason specified");
        this.setInvulnerable = getBoolean("afk_command.on_afk.set_invulnerable", true);
        this.afkTagFormat = getString("afk_command.on_afk.tag_format", "&7[AFK] ");
        this.broadcastOnAfk = getBoolean("afk_command.on_afk.broadcast", true);
        this.broadcastOnReturn = getBoolean("afk_command.on_return.broadcast", true);

        this.mainCommandName = getString("commands.main.command", "antiafk");
        this.mainCommandAliases = getStringList("commands.main.aliases");
        this.afkCommandName = getString("commands.afk.command", "afk");
        this.afkCommandAliases = getStringList("commands.afk.aliases");
        this.afkTestCommandName = getString("commands.afktest.command", "afktest");
        this.afkTestCommandAliases = getStringList("commands.afktest.aliases");
        this.afkCevapCommandName = getString("commands.afkcevap.command", "afkcevap");
        this.afkCevapCommandAliases = getStringList("commands.afkcevap.aliases");

        this.worldGuardEnabled = getBoolean("worldguard_integration.enabled", false);
        loadRegionOverrides();

        this.progressivePunishmentEnabled = getBoolean("progressive_punishment.enabled", true);
        this.punishmentResetAfterStr = getString("progressive_punishment.reset_after", "30d");
        loadPunishmentLevels();

        if (regionCache != null) regionCache.invalidateAll();
    }

    private void loadWarnings() {
        List<Map<String, Object>> raw = getMapList("warnings");
        List<Map<String, Object>> processed = new ArrayList<>();

        for (Map<String, Object> entry : raw) {
            if (entry == null || entry.isEmpty() || !entry.containsKey("time")) continue;

            try {
                Map<String, Object> warning = new HashMap<>(entry);
                String timeStr = String.valueOf(entry.getOrDefault("time", "0s"));
                warning.put("time", TimeUtil.parseTime(timeStr));
                warning.put("bar_color", String.valueOf(entry.getOrDefault("bar_color", "RED")).toUpperCase());
                warning.put("bar_style", String.valueOf(entry.getOrDefault("bar_style", "SOLID")).toUpperCase());
                processed.add(warning);
            } catch (Exception e) {
                if (plugin != null) plugin.getLogger().warning("Warning processing error: " + e.getMessage());
            }
        }

        processed.sort((w1, w2) -> {
            Number n1 = (Number) w1.get("time");
            Number n2 = (Number) w2.get("time");
            if (n1 == null || n2 == null) return 0;
            return Long.compare(n2.longValue(), n1.longValue());
        });

        this.warnings = processed;
    }

    private void loadPunishmentLevels() {
        List<Map<String, Object>> raw = getMapList("progressive_punishment.punishments");
        List<PunishmentLevel> levels = new ArrayList<>();
        int highest = 0;

        for (Map<String, Object> entry : raw) {
            if (entry == null || entry.isEmpty()) continue;

            try {
                int count = 0;
                if (entry.get("count") instanceof Number) {
                    count = ((Number) entry.get("count")).intValue();
                }

                List<Map<String, String>> levelActions = new ArrayList<>();
                Object actionsObj = entry.get("actions");
                if (actionsObj instanceof List) {
                    for (Object obj : (List<?>) actionsObj) {
                        if (obj instanceof Map) {
                            levelActions.add((Map<String, String>) obj);
                        }
                    }
                }

                if (count > highest) highest = count;
                levels.add(new PunishmentLevel(count, levelActions));
            } catch (Exception e) {
                if (plugin != null) plugin.getLogger().warning("Punishment level processing error: " + e.getMessage());
            }
        }

        Collections.sort(levels);

        if (punishmentResetAfterStr != null && punishmentResetAfterStr.equalsIgnoreCase("never")) {
            punishmentResetMillis = -1;
        } else if (punishmentResetAfterStr != null) {
            punishmentResetMillis = TimeUtil.parseTime(punishmentResetAfterStr) * 1000;
        } else {
            punishmentResetMillis = 0;
        }

        this.punishmentLevels = levels;
        this.highestPunishmentCount = highest;
    }

    private void loadRegionOverrides() {
        List<RegionOverride> overrides = new ArrayList<>();
        Map<String, RegionOverride> overrideMap = new HashMap<>();

        if (worldGuardEnabled) {
            Section regionsSection = getSection("worldguard_integration.region_overrides");
            if (regionsSection != null) {
                for (Object key : regionsSection.getKeys()) {
                    Section regionSection = regionsSection.getSection(String.valueOf(key));
                    if (regionSection == null) continue;

                    String regionName = regionSection.getString("region");
                    if (regionName == null || regionName.isEmpty()) continue;

                    String maxAfkTimeStr = regionSection.getString("max_afk_time", "15m");
                    long time = maxAfkTimeStr.equalsIgnoreCase("disabled") ? -1 : TimeUtil.parseTime(maxAfkTimeStr);

                    List<Map<String, String>> regionActions = regionSection.contains("actions")
                            ? getStringMapListFrom(regionSection, "actions")
                            : this.actions;

                    RegionOverride override = new RegionOverride(regionName, time, regionActions);
                    overrides.add(override);
                    overrideMap.put(regionName.toLowerCase(), override);
                }
            }
        }

        this.regionOverrides = overrides;
        this.regionOverrideMap = overrideMap;
    }

    private List<Map<String, String>> getStringMapListFrom(Section section, String path) {
        List<Map<String, String>> result = new ArrayList<>();
        for (Map<?, ?> map : section.getMapList(path)) {
            result.add((Map<String, String>) map);
        }
        return result;
    }

    private Map<String, Double> loadSalaryRegions() {
        Map<String, Double> regions = new HashMap<>();
        Section section = getSection("integrations.economy.salary.regions");
        if (section != null) {
            for (Object key : section.getKeys()) {
                String name = String.valueOf(key);
                regions.put(name, section.getDouble(name, 0.0));
            }
        }
        return regions;
    }

    public FileConfiguration getRawConfig() { return plugin.getConfig(); }
    public void saveConfig() { plugin.saveConfig(); loadConfig(); }
    public AntiAFKPlugin getPlugin() { return plugin; }
    public void clearPlayerCache(Player player) { if (regionCache != null) regionCache.invalidate(player.getUniqueId()); }

    public void registerRegionProvider(IRegionProvider provider) {
        if (provider == null) throw new IllegalArgumentException("Region provider cannot be null");
        regionProviders.add(provider);
        plugin.getLogger().info("Registered new region provider: " + provider.getName());
    }

    public RegionOverride getRegionOverrideForPlayer(Player player) {
        if (!worldGuardEnabled && regionProviders.isEmpty()) return null;
        if (regionCache == null) return null;
        return Objects.requireNonNull(regionCache.get(player.getUniqueId())).orElse(null);
    }

    private Optional<RegionOverride> findRegionOverrideForPlayer(Player player) {
        if (worldGuardEnabled && plugin.isWorldGuardHooked()) {
            Optional<RegionOverride> wgOverride = checkWorldGuardRegion(player);
            if (wgOverride.isPresent()) return wgOverride;
        }
        for (IRegionProvider provider : regionProviders) {
            try {
                List<String> regionNames = provider.getRegionNames(player.getLocation());
                if (regionNames != null) {
                    for (String regionName : regionNames) {
                        RegionOverride override = regionOverrideMap.get(regionName.toLowerCase());
                        if (override != null) return Optional.of(override);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Region provider error: " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    private Optional<RegionOverride> checkWorldGuardRegion(Player player) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = regionContainer.createQuery();
        ApplicableRegionSet applicableRegions = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
        for (ProtectedRegion region : applicableRegions) {
            RegionOverride override = regionOverrideMap.get(region.getId().toLowerCase());
            if (override != null) return Optional.of(override);
        }
        return Optional.empty();
    }

    public String getLanguageName() { return languageName; }
    public SupportedLanguage getLang() { return language; }
    public long getMaxAfkTimeSeconds() { return maxAfkTimeSeconds; }
    public List<Map<String, String>> getActions() { return actions; }
    public List<Map<String, Object>> getWarnings() { return warnings; }
    public long getAutoSetAfkSeconds() { return autoSetAfkSeconds; }
    public int getMaxPointlessActivities() { return maxPointlessActivities; }
    public boolean isCheckCamera() { return checkCamera; }
    public boolean isCheckChat() { return checkChat; }
    public boolean isCheckInteraction() { return checkInteraction; }
    public boolean isCheckToggleSneak() { return checkToggleSneak; }
    public boolean isCheckItemDrop() { return checkItemDrop; }
    public boolean isCheckInventoryActivity() { return checkInventoryActivity; }
    public boolean isCheckItemConsume() { return checkItemConsume; }
    public boolean isCheckHeldItemChange() { return checkHeldItemChange; }
    public boolean isCheckPlayerAttack() { return checkPlayerAttack; }
    public boolean isCheckBookActivity() { return checkBookActivity; }
    public boolean isAutoClickerEnabled() { return autoClickerEnabled; }
    public int getAutoClickerCheckAmount() { return autoClickerCheckAmount; }
    public long getAutoClickerMaxDeviation() { return autoClickerMaxDeviation; }
    public int getAutoClickerDetectionsToPunish() { return autoClickerDetectionsToPunish; }
    public boolean isCheckWorldChangeEnabled() { return checkWorldChangeEnabled; }
    public int getWorldChangeCooldown() { return worldChangeCooldown; }
    public int getMaxWorldChanges() { return maxWorldChanges; }
    public boolean isRejoinProtectionEnabled() { return rejoinProtectionEnabled; }
    public long getRejoinCooldownSeconds() { return rejoinCooldownSeconds; }
    public boolean isTuringTestEnabled() { return turingTestEnabled; }
    public int getQaCaptchaTimeoutSeconds() { return qaCaptchaTimeoutSeconds; }
    public List<Map<String, String>> getCaptchaFailureActions() { return captchaFailureActions; }
    public int getColorPaletteGuiRows() { return colorPaletteGuiRows; }
    public int getColorPaletteTimeLimit() { return colorPaletteTimeLimit; }
    public int getColorPaletteCorrectCount() { return colorPaletteCorrectCount; }
    public int getColorPaletteDistractorColorCount() { return colorPaletteDistractorColorCount; }
    public int getColorPaletteDistractorItemCount() { return colorPaletteDistractorItemCount; }
    public List<String> getColorPaletteAvailableColors() { return Collections.unmodifiableList(colorPaletteAvailableColors); }
    public boolean isLearningModeEnabled() { return learningModeEnabled; }
    public long getAnalysisTaskPeriodTicks() { return analysisTaskPeriodTicks; }
    public double getLearningSimilarityThreshold() { return learningSimilarityThreshold; }
    public int getLearningSearchRadius() { return learningSearchRadius; }
    public long getMaxPatternFileSizeBytes() { return maxPatternFileSizeKb * 1024; }
    public int getMaxVectorsPerPattern() { return maxVectorsPerPattern; }
    public double getPreFilterSizeRatio() { return preFilterSizeRatio; }
    public boolean isBehaviorAnalysisEnabled() { return behaviorAnalysisEnabled; }
    public int getBehaviorHistorySizeTicks() { return behaviorHistorySizeTicks; }
    public boolean isConfinementCheckEnabled() { return confinementCheckEnabled; }
    public long getConfinementCheckDurationMillis() { return confinementCheckDurationSeconds * 1000L; }
    public double getConfinementRadius() { return confinementRadius; }
    public double getConfinementMinDistance() { return confinementMinDistance; }
    public boolean isDiscordWebhookEnabled() { return discordWebhookEnabled; }
    public String getDiscordWebhookUrl() { return discordWebhookUrl; }
    public String getDiscordBotName() { return discordBotName; }
    public String getDiscordAvatarUrl() { return discordAvatarUrl; }
    public boolean isEssentialsSync() { return essentialsSync;}
    public boolean isEconomyEnabled() { return economyEnabled; }
    public boolean isTaxEnabled() { return taxEnabled; }
    public double getTaxAmount() { return taxAmount; }
    public int getTaxInterval() { return taxInterval; }
    public boolean isSalaryEnabled() { return salaryEnabled; }
    public Map<String, Double> getSalaryRegions() { return salaryRegions; }
    public int getSalaryInterval() { return salaryInterval; }
    public boolean isFarmLimiterEnabled() { return farmLimiterEnabled; }
    public int getFarmLimiterRadius() { return farmLimiterRadius; }
    public boolean isFarmLimiterCheckFriends() { return farmLimiterCheckFriends; }
    public boolean isItemPickupBlockerEnabled() { return itemPickupBlockerEnabled; }
    public String getPermBypassAll() { return permBypassAll; }
    public String getPermBypassClassic() { return permBypassClassic; }
    public String getPermBypassBehavioral() { return permBypassBehavioral; }
    public String getPermBypassPointless() { return permBypassPointless; }
    public String getPermBypassAutoclicker() { return permBypassAutoclicker; }
    public String getPermAfkCommandUse() { return permAfkCommandUse; }
    public List<String> getDisabledWorlds() { return disabledWorlds; }
    public List<String> getExemptGameModes() { return exemptGameModes; }
    public boolean isAfkCommandEnabled() { return afkCommandEnabled; }
    public String getAfkDefaultReason() { return afkDefaultReason; }
    public boolean isSetInvulnerable() { return setInvulnerable; }
    public String getAfkTagFormat() { return afkTagFormat; }
    public boolean isBroadcastOnAfkEnabled() { return broadcastOnAfk; }
    public boolean isBroadcastOnReturnEnabled() { return broadcastOnReturn; }
    public boolean isWorldGuardEnabled() { return worldGuardEnabled; }
    public List<RegionOverride> getRegionOverrides() { return regionOverrides; }
    public boolean isProgressivePunishmentEnabled() { return progressivePunishmentEnabled; }
    public long getPunishmentResetMillis() { return punishmentResetMillis; }
    public List<PunishmentLevel> getPunishmentLevels() { return punishmentLevels; }
    public int getHighestPunishmentCount() { return highestPunishmentCount; }
    public String getMainCommandName() { return mainCommandName; }
    public List<String> getMainCommandAliases() { return mainCommandAliases; }
    public String getAfkCommandName() { return afkCommandName; }
    public List<String> getAfkCommandAliases() { return afkCommandAliases; }
    public String getAfkTestCommandName() { return afkTestCommandName; }
    public List<String> getAfkTestCommandAliases() { return afkTestCommandAliases; }
    public String getAfkCevapCommandName() { return afkCevapCommandName; }
    public List<String> getAfkCevapCommandAliases() { return afkCevapCommandAliases; }
}
