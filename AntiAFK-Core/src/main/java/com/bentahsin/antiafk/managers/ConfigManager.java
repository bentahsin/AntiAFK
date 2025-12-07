package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.api.region.IRegionProvider;
import com.bentahsin.antiafk.language.SupportedLanguage;
import com.bentahsin.antiafk.models.PunishmentLevel;
import com.bentahsin.antiafk.models.RegionOverride;
import com.bentahsin.configuration.Configuration;
import com.bentahsin.configuration.annotation.*;
import com.bentahsin.configuration.converter.impl.TimeConverter;
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
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Singleton
@ConfigHeader({
        "===================================================================",
        "                 AntiAFK by BenTahsin - Ana Konfigürasyon          ",
        "===================================================================",
        "Plugin ile ilgili tüm mesajları ve metinleri 'messages.yml' dosyasından düzenleyebilirsiniz."
})
@SuppressWarnings("FieldCanBeLocal")
public class ConfigManager {

    @Ignore
    private final AntiAFKPlugin plugin;
    @Ignore
    private final Configuration configuration;
    @Ignore
    private final List<IRegionProvider> regionProviders = new CopyOnWriteArrayList<>();
    @Ignore
    private final LoadingCache<UUID, Optional<RegionOverride>> regionCache;

    @ConfigPath("lang")
    @Comment({"Eklentinin kullanacağı dili belirleyin.", "Seçenekler: Turkish, English, Spanish, German, French, Russian, Polish"})
    private String languageName = "Turkish";

    @Ignore
    private SupportedLanguage language;

    @ConfigPath("max_afk_time")
    @Transform(TimeConverter.class)
    @Comment("Bir oyuncunun nihai eylemlere maruz kalmadan önce AFK kalabileceği süre.")
    private long maxAfkTimeSeconds = 900;

    @ConfigPath("detection.auto_set_afk_after")
    @Transform(TimeConverter.class)
    @Comment("Bir oyuncu ne kadar süre hareketsiz kaldıktan sonra otomatik olarak [AFK] tag'ı alacak?")
    private long autoSetAfkSeconds = 60;

    @ConfigPath("detection.check_camera_movement")
    private boolean checkCamera = true;
    @ConfigPath("detection.check_chat_activity")
    private boolean checkChat = true;
    @ConfigPath("detection.check_interaction")
    private boolean checkInteraction = true;
    @ConfigPath("detection.check_toggle_sneak")
    private boolean checkToggleSneak = true;
    @ConfigPath("detection.check_item_drop")
    private boolean checkItemDrop = true;
    @ConfigPath("detection.check_inventory_activity")
    private boolean checkInventoryActivity = true;
    @ConfigPath("detection.check_item_consume")
    private boolean checkItemConsume = true;
    @ConfigPath("detection.check_held_item_change")
    private boolean checkHeldItemChange = true;
    @ConfigPath("detection.check_player_attack")
    private boolean checkPlayerAttack = true;
    @ConfigPath("detection.check_book_activity")
    private boolean checkBookActivity = true;

    @ConfigPath("detection.max-pointless-activities")
    private int maxPointlessActivities = 15;

    @ConfigPath("detection.auto-clicker.enabled")
    private boolean autoClickerEnabled = true;
    @ConfigPath("detection.auto-clicker.check-amount")
    private int autoClickerCheckAmount = 20;
    @ConfigPath("detection.auto-clicker.max-deviation")
    private long autoClickerMaxDeviation = 10;
    @ConfigPath("detection.auto-clicker.detections-to-punish")
    private int autoClickerDetectionsToPunish = 3;

    @ConfigPath("detection.check-world-change.enabled")
    private boolean checkWorldChangeEnabled = true;
    @ConfigPath("detection.check-world-change.cooldown")
    private int worldChangeCooldown = 20;
    @ConfigPath("detection.check-world-change.max-changes")
    private int maxWorldChanges = 5;

    @ConfigPath("rejoin_protection.enabled")
    private boolean rejoinProtectionEnabled = true;
    @ConfigPath("rejoin_protection.cooldown")
    @Transform(TimeConverter.class)
    private long rejoinCooldownSeconds = 300;

    @ConfigPath("turing_test.enabled")
    private boolean turingTestEnabled = true;
    @ConfigPath("turing_test.question_answer.answer_timeout_seconds")
    private int qaCaptchaTimeoutSeconds = 20;
    @ConfigPath("turing_test.color_palette.gui_rows")
    private int colorPaletteGuiRows = 3;
    @ConfigPath("turing_test.color_palette.time_limit_seconds")
    private int colorPaletteTimeLimit = 15;
    @ConfigPath("turing_test.color_palette.correct_item_count")
    private int colorPaletteCorrectCount = 5;
    @ConfigPath("turing_test.color_palette.distractor_color_count")
    private int colorPaletteDistractorColorCount = 2;
    @ConfigPath("turing_test.color_palette.distractor_item_count_per_color")
    private int colorPaletteDistractorItemCount = 4;
    @ConfigPath("turing_test.color_palette.available_colors")
    private List<String> colorPaletteAvailableColors = new ArrayList<>();

    @ConfigPath("turing_test.on_failure_actions")
    private List<Map<String, String>> captchaFailureActions = new ArrayList<>();

    @ConfigPath("learning_mode.enabled")
    private boolean learningModeEnabled = true;
    @ConfigPath("learning_mode.analysis_task_period_ticks")
    private long analysisTaskPeriodTicks = 40L;
    @ConfigPath("learning_mode.similarity_threshold")
    private double learningSimilarityThreshold = 25.0;
    @ConfigPath("learning_mode.search_radius")
    private int learningSearchRadius = 10;
    @ConfigPath("learning_mode.security.max_pattern_file_size_kb")
    private long maxPatternFileSizeKb = 1024;
    @ConfigPath("learning_mode.security.max_vectors_per_pattern")
    private int maxVectorsPerPattern = 12000;
    @ConfigPath("learning_mode.security.pre_filter_size_ratio")
    private double preFilterSizeRatio = 0.5;

    @ConfigPath("behavioral-analysis.enabled")
    private boolean behaviorAnalysisEnabled = false;
    @ConfigPath("behavioral-analysis.history-size-ticks")
    private int behaviorHistorySizeTicks = 600;

    @ConfigPath("discord_webhook.enabled")
    private boolean discordWebhookEnabled = false;
    @ConfigPath("discord_webhook.webhook_url")
    private String discordWebhookUrl = "";
    @ConfigPath("discord_webhook.bot_name")
    private String discordBotName = "AntiAFK Guard";
    @ConfigPath("discord_webhook.avatar_url")
    private String discordAvatarUrl = "";

    @ConfigPath("actions")
    private List<Map<String, String>> actions = new ArrayList<>();

    /**
     * Raw warnings data loaded from configuration.
     * This field is only intended to be accessed during configuration loading (e.g., in postLoad()).
     */
    @ConfigPath("warnings")
    private List<Map<String, Object>> warningsRaw = new ArrayList<>();

    /**
     * Processed warnings data, to be used by all runtime logic.
     */
    @Ignore
    private List<Map<String, Object>> warnings = new ArrayList<>();

    @ConfigPath("exemptions.permissions.bypass_all")
    private String permBypassAll = "antiafk.bypass.all";
    @ConfigPath("exemptions.permissions.bypass_classic")
    private String permBypassClassic = "antiafk.bypass.classic";
    @ConfigPath("exemptions.permissions.bypass_behavioral")
    private String permBypassBehavioral = "antiafk.bypass.behavioral";
    @ConfigPath("exemptions.permissions.bypass_pointless")
    private String permBypassPointless = "antiafk.bypass.pointless";
    @ConfigPath("exemptions.permissions.bypass_autoclicker")
    private String permBypassAutoclicker = "antiafk.bypass.autoclicker";
    @ConfigPath("afk_command.permission")
    private String permAfkCommandUse = "antiafk.command.afk";

    @ConfigPath("exemptions.disabled_worlds")
    private List<String> disabledWorlds = new ArrayList<>();
    @ConfigPath("exemptions.exempt_gamemodes")
    private List<String> exemptGameModes = new ArrayList<>();

    @ConfigPath("afk_command.enabled")
    private boolean afkCommandEnabled = true;
    @ConfigPath("afk_command.on_afk.default_reason")
    private String afkDefaultReason = "No reason specified";
    @ConfigPath("afk_command.on_afk.set_invulnerable")
    private boolean setInvulnerable = true;
    @ConfigPath("afk_command.on_afk.tag_format")
    private String afkTagFormat = "&7[AFK] ";
    @ConfigPath("afk_command.on_afk.broadcast")
    private boolean broadcastOnAfk = true;
    @ConfigPath("afk_command.on_return.broadcast")
    private boolean broadcastOnReturn = true;

    @ConfigPath("worldguard_integration.enabled")
    private boolean worldGuardEnabled = false;

    @ConfigPath("worldguard_integration.region_overrides")
    private Map<String, RegionConfigDTO> regionOverridesRaw = new HashMap<>();

    @Ignore
    private List<RegionOverride> regionOverrides = new ArrayList<>();
    @Ignore
    private Map<String, RegionOverride> regionOverrideMap = new HashMap<>();

    @ConfigPath("progressive_punishment.enabled")
    private boolean progressivePunishmentEnabled = true;
    @ConfigPath("progressive_punishment.reset_after")
    private String punishmentResetAfterStr = "30d";
    @Ignore
    private long punishmentResetMillis;

    @ConfigPath("progressive_punishment.punishments")
    private List<PunishmentLevelDTO> punishmentLevelsRaw = new ArrayList<>();

    @Ignore
    private List<PunishmentLevel> punishmentLevels = new ArrayList<>();
    @Ignore
    private int highestPunishmentCount = 0;


    @Inject
    public ConfigManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.configuration = new Configuration(plugin);
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
        plugin.reloadConfig();
        configuration.init(this, "config.yml");
    }

    /**
     * Called automatically after configuration is loaded, via the {@code @PostLoad} annotation.
     */
    @PostLoad
    @SuppressWarnings("unused")
    public void postLoad() {
        this.language = SupportedLanguage.fromConfigName(languageName);
        TimeConverter timeConverter = new TimeConverter();

        warnings.clear();
        for (Map<String, Object> raw : warningsRaw) {
            Map<String, Object> processed = new HashMap<>(raw);
            String timeStr = String.valueOf(raw.getOrDefault("time", "0s"));
            processed.put("time", timeConverter.convertToField(timeStr));
            processed.put("bar_color", String.valueOf(raw.getOrDefault("bar_color", "RED")).toUpperCase());
            processed.put("bar_style", String.valueOf(raw.getOrDefault("bar_style", "SOLID")).toUpperCase());

            warnings.add(processed);
        }
        warnings.sort((w1, w2) -> Long.compare(((Number) w2.get("time")).longValue(), ((Number) w1.get("time")).longValue()));

        if (punishmentResetAfterStr != null && punishmentResetAfterStr.equalsIgnoreCase("never")) {
            punishmentResetMillis = -1;
        } else if (punishmentResetAfterStr != null) {
            punishmentResetMillis = timeConverter.convertToField(punishmentResetAfterStr) * 1000;
        } else {
            punishmentResetMillis = 0;
        }

        punishmentLevels.clear();
        highestPunishmentCount = 0;
        for (PunishmentLevelDTO dto : punishmentLevelsRaw) {
            if (dto.count > highestPunishmentCount) highestPunishmentCount = dto.count;
            punishmentLevels.add(new PunishmentLevel(dto.count, dto.actions));
        }
        Collections.sort(punishmentLevels);

        regionOverrides.clear();
        regionOverrideMap.clear();

        if (worldGuardEnabled) {
            for (RegionConfigDTO dto : regionOverridesRaw.values()) {
                if (dto.region == null || dto.region.isEmpty()) continue;
                long time = dto.max_afk_time.equalsIgnoreCase("disabled") ? -1 : timeConverter.convertToField(dto.max_afk_time);
                List<Map<String, String>> regionActions = (dto.actions == null || dto.actions.isEmpty()) ? this.actions : dto.actions;

                RegionOverride override = new RegionOverride(dto.region, time, regionActions);
                regionOverrides.add(override);
                regionOverrideMap.put(dto.region.toLowerCase(), override);
            }
        }

        regionCache.invalidateAll();
    }

    public static class RegionConfigDTO {
        public String region;
        public String max_afk_time = "15m";
        public List<Map<String, String>> actions;
    }

    public static class PunishmentLevelDTO {
        public int count;
        public List<Map<String, String>> actions;
    }

    public void registerRegionProvider(IRegionProvider provider) {
        if (provider == null) throw new IllegalArgumentException("Region provider cannot be null");
        regionProviders.add(provider);
        plugin.getLogger().info("Registered new region provider: " + provider.getName());
    }

    public FileConfiguration getRawConfig() {
        return plugin.getConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
        loadConfig();
    }

    public AntiAFKPlugin getPlugin() {
        return plugin;
    }

    public void clearPlayerCache(Player player) {
        regionCache.invalidate(player.getUniqueId());
    }

    public RegionOverride getRegionOverrideForPlayer(Player player) {
        if (!worldGuardEnabled && regionProviders.isEmpty()) {
            return null;
        }
        return Objects.requireNonNull(regionCache.get(player.getUniqueId())).orElse(null);
    }

    private Optional<RegionOverride> findRegionOverrideForPlayer(Player player) {
        if (worldGuardEnabled && plugin.isWorldGuardHooked()) {
            Optional<RegionOverride> wgOverride = checkWorldGuardRegion(player);
            if (wgOverride.isPresent()) {
                return wgOverride;
            }
        }

        for (IRegionProvider provider : regionProviders) {
            try {
                List<String> regionNames = provider.getRegionNames(player.getLocation());
                if (regionNames == null || regionNames.isEmpty()) continue;

                for (String regionName : regionNames) {
                    RegionOverride override = regionOverrideMap.get(regionName.toLowerCase());
                    if (override != null) {
                        return Optional.of(override);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Region provider '" + provider.getName() + "' threw an error: " + e.getMessage());
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
    public int getBehaviorHistorySizeTicks() { return behaviorHistorySizeTicks; }
    public boolean isBehaviorAnalysisEnabled() { return behaviorAnalysisEnabled; }
    public boolean isDiscordWebhookEnabled() { return discordWebhookEnabled; }
    public String getDiscordWebhookUrl() { return discordWebhookUrl; }
    public String getDiscordBotName() { return discordBotName; }
    public String getDiscordAvatarUrl() { return discordAvatarUrl; }
}