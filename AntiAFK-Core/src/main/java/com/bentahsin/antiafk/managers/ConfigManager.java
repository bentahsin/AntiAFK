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

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Singleton
@ConfigVersion(2)
@Backup(
        enabled = true,
        path = "backups",
        onFailure = true,
        onMigration = true
)
@ConfigHeader({
        "===================================================================",
        "                 AntiAFK by BenTahsin - Main Configuration         ",
        "===================================================================",
        "You can edit all plugin messages and texts from the 'messages.yml' file.",
        "Need help? Check our GitHub or Discord."
})
@SuppressWarnings({"FieldCanBeLocal", "unused", "unchecked"})
public class ConfigManager {

    @Ignore private final AntiAFKPlugin plugin;
    @Ignore private final Configuration configuration;
    @Ignore private final List<IRegionProvider> regionProviders = new CopyOnWriteArrayList<>();
    @Ignore private final LoadingCache<UUID, Optional<RegionOverride>> regionCache;

    // --- BASIC SETTINGS ---
    @ConfigPath("lang")
    @Comment({
            "==================================",
            "        LANGUAGE SETTINGS",
            "==================================",
            "1. System Messages (Console):",
            "   Changes the language of console logs, startup messages, and internal errors.",
            "   Controlled by the 'lang' setting below.",
            "",
            "2. Player Messages (In-Game):",
            "   Stored in 'messages.yml'. Changing the setting below DOES NOT automatically",
            "   translate 'messages.yml' to preserve your custom edits.",
            "   -> You must edit 'messages.yml' manually for in-game player translations.",
            "",
            "Available System Languages: Turkish, English, Spanish, German, French, Russian, Polish"
    })
    private String languageName = "English";
    @Ignore private SupportedLanguage language;

    @ConfigPath("max_afk_time")
    @Comment({
            "==================================",
            "        MAIN AFK SYSTEM",
            "==================================",
            "The maximum time a player can stay inactive before final actions (kick/ban) are taken.",
            "Note: This value can be overridden by WorldGuard region settings below.",
            "Format: 10s (seconds), 15m (minutes), 1h (hours), 1d (days)."
    })
    @Transform(TimeConverter.class)
    @Validate(min = 1)
    private long maxAfkTimeSeconds = 900;

    @ConfigPath("warnings")
    @Comment({
            "Warnings sent to the player based on the 'Time Remaining' until punishment.",
            "The plugin checks these from largest time to smallest.",
            "Available Types: CHAT, ACTION_BAR, TITLE, BOSS_BAR"
    })
    private List<Map<String, Object>> warningsRaw = new ArrayList<>();

    @Ignore
    private List<Map<String, Object>> warnings = new ArrayList<>();

    @ConfigPath("actions")
    @Comment({
            "Actions to execute when the 'max_afk_time' runs out.",
            "Available Types: CONSOLE, PLAYER, DISCORD_WEBHOOK"
    })
    private List<Map<String, String>> actions = new ArrayList<>();

    // --- DETECTION SETTINGS ---
    @ConfigPath("detection.auto_set_afk_after")
    @Comment({
            "==================================",
            "        ACTIVITY DETECTION",
            "==================================",
            "After how long of inactivity should a player automatically get the [AFK] tag?",
            "This serves as a visual indicator before the actual punishment.",
            "Type 'disabled' to disable this feature."
    })
    @Transform(TimeConverter.class)
    private long autoSetAfkSeconds = 60;

    @ConfigPath("detection.max-pointless-activities")
    @Comment({
            "If a player performs actions (jumping, hitting air, dropping items) while",
            "standing on the SAME BLOCK more than this amount, they are considered a bot."
    })
    @Validate(min = 1)
    private int maxPointlessActivities = 15;

    @ConfigPath("detection.check_camera_movement")
    @Comment("Define which player actions reset the AFK timer.")
    private boolean checkCamera = true;
    @ConfigPath("detection.check_chat_activity") private boolean checkChat = true;
    @ConfigPath("detection.check_interaction") private boolean checkInteraction = true;
    @ConfigPath("detection.check_toggle_sneak") private boolean checkToggleSneak = true;
    @ConfigPath("detection.check_item_drop") private boolean checkItemDrop = true;
    @ConfigPath("detection.check_inventory_activity") private boolean checkInventoryActivity = true;
    @ConfigPath("detection.check_item_consume") private boolean checkItemConsume = true;
    @ConfigPath("detection.check_player_attack") private boolean checkPlayerAttack = true;

    @ConfigPath("detection.check_held_item_change")
    @Comment({"The following are disabled by default to prevent false positives."})
    private boolean checkHeldItemChange = true;
    @ConfigPath("detection.check_book_activity") private boolean checkBookActivity = true;

    // --- AUTO CLICKER ---
    @ConfigPath("detection.auto-clicker.enabled")
    @Comment({
            "Analyzes the statistical variance (standard deviation) of click intervals.",
            "NOTE: This is automatically disabled for Bedrock players to prevent false positives."
    })
    private boolean autoClickerEnabled = true;
    @ConfigPath("detection.auto-clicker.check-amount") @Validate(min = 5) private int autoClickerCheckAmount = 20;
    @ConfigPath("detection.auto-clicker.max-deviation") @Validate(min = 0) private long autoClickerMaxDeviation = 10;
    @ConfigPath("detection.auto-clicker.detections-to-punish") @Validate(min = 1) private int autoClickerDetectionsToPunish = 3;

    // --- WORLD CHANGE ---
    @ConfigPath("detection.check-world-change.enabled")
    @Comment("Prevents players from resetting their AFK status by rapidly switching worlds.")
    private boolean checkWorldChangeEnabled = true;
    @ConfigPath("detection.check-world-change.cooldown") @Validate(min = 1) private int worldChangeCooldown = 20;
    @ConfigPath("detection.check-world-change.max-changes") @Validate(min = 1) private int maxWorldChanges = 5;

    // --- REJOIN PROTECTION ---
    @ConfigPath("rejoin_protection.enabled")
    @Comment({
            "==================================",
            "        REJOIN PROTECTION",
            "==================================",
            "If a player is kicked for AFK, they cannot rejoin for this duration."
    })
    private boolean rejoinProtectionEnabled = true;
    @ConfigPath("rejoin_protection.cooldown") @Transform(TimeConverter.class) private long rejoinCooldownSeconds = 300;

    // --- TURING TEST (CAPTCHA) ---
    @ConfigPath("turing_test.enabled")
    @Comment({
            "==================================",
            "   TURING TEST (CAPTCHA)",
            "==================================",
            "If a player is flagged as 'Suspicious', they will be presented with a Captcha test."
    })
    private boolean turingTestEnabled = true;

    @ConfigPath("turing_test.palettes")
    @Comment("Which captcha types are active and their probability weights.")
    private Map<String, Map<String, Object>> captchaPalettesRaw = new HashMap<>();

    @ConfigPath("turing_test.question_answer.answer_timeout_seconds") @Validate(min = 5) private int qaCaptchaTimeoutSeconds = 20;

    @ConfigPath("turing_test.on_failure_actions")
    @Comment("Commands to execute if player FAILS the test or runs out of time.")
    private List<Map<String, String>> captchaFailureActions = new ArrayList<>();

    @ConfigPath("turing_test.color_palette.gui_rows") @Validate(min = 1, max = 6) private int colorPaletteGuiRows = 3;
    @ConfigPath("turing_test.color_palette.time_limit_seconds") @Validate(min = 5) private int colorPaletteTimeLimit = 15;
    @ConfigPath("turing_test.color_palette.correct_item_count") @Validate(min = 1) private int colorPaletteCorrectCount = 5;
    @ConfigPath("turing_test.color_palette.distractor_color_count") @Validate(min = 1) private int colorPaletteDistractorColorCount = 2;
    @ConfigPath("turing_test.color_palette.distractor_item_count_per_color") @Validate(min = 1) private int colorPaletteDistractorItemCount = 4;
    @ConfigPath("turing_test.color_palette.available_colors") private List<String> colorPaletteAvailableColors = new ArrayList<>();

    // --- LEARNING MODE ---
    @ConfigPath("learning_mode.enabled")
    @Comment({
            "==================================",
            "    LEARNING MODE (PATTERN AI)",
            "==================================",
            "This module compares live player movements against recorded bot patterns."
    })
    private boolean learningModeEnabled = true;
    @ConfigPath("learning_mode.analysis_task_period_ticks") @Validate(min = 1) private long analysisTaskPeriodTicks = 40L;
    @ConfigPath("learning_mode.similarity_threshold") private double learningSimilarityThreshold = 25.0;
    @ConfigPath("learning_mode.search_radius") private int learningSearchRadius = 10;

    @ConfigPath("learning_mode.security.max_pattern_file_size_kb")
    @Comment("Security limits to prevent memory overflows.")
    private long maxPatternFileSizeKb = 1024;
    @ConfigPath("learning_mode.security.max_vectors_per_pattern") private int maxVectorsPerPattern = 12000;
    @ConfigPath("learning_mode.security.pre_filter_size_ratio") private double preFilterSizeRatio = 0.5;

    // --- BEHAVIORAL ANALYSIS ---
    @ConfigPath("behavioral-analysis.enabled")
    @Comment({
            "==================================",
            "    ADVANCED BEHAVIOR ANALYSIS",
            "==================================",
            "This system records player movement vectors and looks for repetitive loops."
    })
    private boolean behaviorAnalysisEnabled = false;
    @ConfigPath("behavioral-analysis.history-size-ticks") @Validate(min = 20) private int behaviorHistorySizeTicks = 600;

    // --- DISCORD ---
    @ConfigPath("discord_webhook.enabled")
    @Comment({
            "==================================",
            "           INTEGRATIONS",
            "==================================",
            "Send notifications like kick/ban to Discord."
    })
    private boolean discordWebhookEnabled = false;
    @ConfigPath("discord_webhook.webhook_url") private String discordWebhookUrl = "PASTE_YOUR_WEBHOOK_URL_HERE";
    @ConfigPath("discord_webhook.bot_name") private String discordBotName = "AntiAFK Guard";
    @ConfigPath("discord_webhook.avatar_url") private String discordAvatarUrl = "";

    // --- PERMISSIONS & EXEMPTIONS ---
    @ConfigPath("exemptions.permissions.bypass_all")
    @Comment({
            "==================================",
            "      EXEMPTIONS & PERMISSIONS",
            "=================================="
    })
    private String permBypassAll = "antiafk.bypass.all";
    @ConfigPath("exemptions.permissions.bypass_classic") private String permBypassClassic = "antiafk.bypass.classic";
    @ConfigPath("exemptions.permissions.bypass_behavioral") private String permBypassBehavioral = "antiafk.bypass.behavioral";
    @ConfigPath("exemptions.permissions.bypass_pointless") private String permBypassPointless = "antiafk.bypass.pointless";
    @ConfigPath("exemptions.permissions.bypass_autoclicker") private String permBypassAutoclicker = "antiafk.bypass.autoclicker";
    @ConfigPath("afk_command.permission") private String permAfkCommandUse = "antiafk.command.afk";

    @ConfigPath("exemptions.disabled_worlds")
    @Comment("Worlds where AntiAFK is completely disabled.")
    private List<String> disabledWorlds = new ArrayList<>();

    @ConfigPath("exemptions.exempt_gamemodes")
    @Comment("GameModes that are ignored by AntiAFK.")
    private List<String> exemptGameModes = new ArrayList<>();

    // --- AFK COMMAND ---
    @ConfigPath("afk_command.enabled")
    @Comment({
            "==================================",
            "       MANUAL /AFK COMMAND",
            "=================================="
    })
    private boolean afkCommandEnabled = true;
    @ConfigPath("afk_command.on_afk.default_reason") private String afkDefaultReason = "No reason specified";
    @ConfigPath("afk_command.on_afk.set_invulnerable") private boolean setInvulnerable = true;
    @ConfigPath("afk_command.on_afk.tag_format") private String afkTagFormat = "&7[AFK] ";
    @ConfigPath("afk_command.on_afk.broadcast") private boolean broadcastOnAfk = true;
    @ConfigPath("afk_command.on_return.broadcast") private boolean broadcastOnReturn = true;

    // --- COMMAND SETTINGS ---
    @ConfigPath("commands.main.command")
    @Comment({
            "==================================",
            "         COMMAND SETTINGS",
            "==================================",
            "You can rename the plugin commands here to avoid conflicts.",
            "Restart required."
    })
    private String mainCommandName = "antiafk";
    @ConfigPath("commands.main.aliases") private List<String> mainCommandAliases = new ArrayList<>(Arrays.asList("aafk", "afkadmin"));

    @ConfigPath("commands.afk.command") private String afkCommandName = "afk";
    @ConfigPath("commands.afk.aliases") private List<String> afkCommandAliases = new ArrayList<>(Arrays.asList("away", "brb"));

    @ConfigPath("commands.afktest.command") private String afkTestCommandName = "afktest";
    @ConfigPath("commands.afktest.aliases") private List<String> afkTestCommandAliases = new ArrayList<>(Collections.singletonList("testafk"));

    @ConfigPath("commands.afkcevap.command") private String afkCevapCommandName = "afkcevap";
    @ConfigPath("commands.afkcevap.aliases") private List<String> afkCevapCommandAliases = new ArrayList<>(Arrays.asList("answer", "reply"));

    // --- WORLDGUARD ---
    @ConfigPath("worldguard_integration.enabled")
    @Comment({
            "==================================",
            "      WORLDGUARD INTEGRATION",
            "=================================="
    })
    private boolean worldGuardEnabled = false;

    @ConfigPath("worldguard_integration.region_overrides")
    @Comment({
            "Define custom AFK rules for specific regions.",
            "Keys ('0', '1') are just identifiers."
    })
    private Map<String, Map<String, Object>> regionOverridesRaw = new HashMap<>();

    @Ignore private List<RegionOverride> regionOverrides = new ArrayList<>();
    @Ignore private Map<String, RegionOverride> regionOverrideMap = new HashMap<>();

    // --- PROGRESSIVE PUNISHMENT ---
    @ConfigPath("progressive_punishment.enabled")
    @Comment({
            "==================================",
            "      PROGRESSIVE PUNISHMENT",
            "==================================",
            "This system tracks how many times a player has been punished.",
            "You can increase the severity based on the offense count."
    })
    private boolean progressivePunishmentEnabled = true;

    @ConfigPath("progressive_punishment.reset_after")
    @Comment("How long should the punishment counter be remembered? (e.g. 30d)")
    private String punishmentResetAfterStr = "30d";

    @ConfigPath("progressive_punishment.punishments")
    private List<Map<String, Object>> punishmentLevelsRaw = new ArrayList<>();

    @Ignore private long punishmentResetMillis;
    @Ignore private List<PunishmentLevel> punishmentLevels = new ArrayList<>();
    @Ignore private int highestPunishmentCount = 0;

    public ConfigManager() {
        this.plugin = null;
        this.configuration = null;
        this.regionCache = null;
    }

    @Inject
    public ConfigManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.configuration = new Configuration(plugin);

        initializeDefaults();

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

    /**
     * Fills default values in English.
     */
    private void initializeDefaults() {
        // Renkler
        colorPaletteAvailableColors = new ArrayList<>(Arrays.asList(
                "RED", "BLUE", "LIME", "YELLOW", "ORANGE", "CYAN", "MAGENTA"
        ));

        // Exemptions
        disabledWorlds = new ArrayList<>(Collections.singletonList("creative_world"));
        exemptGameModes = new ArrayList<>(Arrays.asList("SPECTATOR", "CREATIVE"));

        // Default Action (Kick)
        Map<String, String> defaultAction = new HashMap<>();
        defaultAction.put("type", "CONSOLE");
        defaultAction.put("command", "kick %player% &cAFK time expired.");
        actions = new ArrayList<>(Collections.singletonList(defaultAction));

        // Default Warnings
        warningsRaw = new ArrayList<>();

        Map<String, Object> warn1 = new HashMap<>();
        warn1.put("time", "5m");
        warn1.put("type", "CHAT");
        warn1.put("message", "&e[AntiAFK] &7You seem inactive. You will be punished if you don't move in &c%time_left%&7.");
        warn1.put("bar_color", "RED");
        warn1.put("bar_style", "SOLID");
        warningsRaw.add(warn1);

        Map<String, Object> warn2 = new HashMap<>();
        warn2.put("time", "1m");
        warn2.put("type", "TITLE");
        warn2.put("title", "&c&lWARNING");
        warn2.put("subtitle", "&eYou must move within 1 minute!");
        warn2.put("sound", "BLOCK_NOTE_BLOCK_PLING");
        warn2.put("bar_color", "RED");
        warn2.put("bar_style", "SOLID");
        warningsRaw.add(warn2);

        Map<String, Object> warn3 = new HashMap<>();
        warn3.put("time", "10s");
        warn3.put("type", "ACTION_BAR");
        warn3.put("message", "&4&lKicking in: %time_left%");
        warn3.put("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        warn3.put("bar_color", "RED");
        warn3.put("bar_style", "SOLID");
        warningsRaw.add(warn3);

        // Progressive Punishments
        punishmentLevelsRaw = new ArrayList<>();

        // Level 1
        Map<String, Object> lvl1 = new HashMap<>();
        lvl1.put("count", 1);
        List<Map<String, String>> lvl1Actions = new ArrayList<>();
        Map<String, String> act1 = new HashMap<>();
        act1.put("type", "CONSOLE");
        act1.put("command", "warn %player% Please avoid staying AFK on the server.");
        lvl1Actions.add(act1);
        Map<String, String> act2 = new HashMap<>();
        act2.put("type", "PLAYER");
        act2.put("command", "spawn");
        lvl1Actions.add(act2);
        lvl1.put("actions", lvl1Actions);
        punishmentLevelsRaw.add(lvl1);

        // Level 3
        Map<String, Object> lvl3 = new HashMap<>();
        lvl3.put("count", 3);
        List<Map<String, String>> lvl3Actions = new ArrayList<>();
        Map<String, String> act3 = new HashMap<>();
        act3.put("type", "CONSOLE");
        act3.put("command", "kick %player% &cKicked due to repeated AFK behavior.");
        lvl3Actions.add(act3);
        lvl3.put("actions", lvl3Actions);
        punishmentLevelsRaw.add(lvl3);

        // Level 5
        Map<String, Object> lvl5 = new HashMap<>();
        lvl5.put("count", 5);
        List<Map<String, String>> lvl5Actions = new ArrayList<>();
        Map<String, String> act5 = new HashMap<>();
        act5.put("type", "CONSOLE");
        act5.put("command", "tempban %player% 10m &cTemporarily banned for habitual AFK behavior.");
        lvl5Actions.add(act5);
        lvl5.put("actions", lvl5Actions);
        punishmentLevelsRaw.add(lvl5);

        // Captcha Failure Actions
        captchaFailureActions = new ArrayList<>();
        Map<String, String> failAct1 = new HashMap<>();
        failAct1.put("type", "CONSOLE");
        failAct1.put("command", "kick %player% &cBot test failed!");
        captchaFailureActions.add(failAct1);

        // Captcha Palettes
        captchaPalettesRaw = new HashMap<>();
        Map<String, Object> qa = new HashMap<>();
        qa.put("enabled", true);
        qa.put("weight", 40);
        captchaPalettesRaw.put("QUESTION_ANSWER", qa);

        Map<String, Object> cp = new HashMap<>();
        cp.put("enabled", true);
        cp.put("weight", 60);
        captchaPalettesRaw.put("COLOR_PALETTE", cp);

        Map<String, Object> bf = new HashMap<>();
        bf.put("enabled", true);
        bf.put("weight", 100);
        captchaPalettesRaw.put("BEDROCK_FORM", bf);

        // WorldGuard Regions
        regionOverridesRaw = new HashMap<>();

        Map<String, Object> reg0 = new HashMap<>();
        reg0.put("region", "pvp_arena");
        reg0.put("max_afk_time", "3m");
        List<Map<String, String>> reg0Acts = new ArrayList<>();
        Map<String, String> regAct1 = new HashMap<>();
        regAct1.put("type", "PLAYER");
        regAct1.put("command", "spawn");
        reg0Acts.add(regAct1);
        reg0.put("actions", reg0Acts);
        regionOverridesRaw.put("0", reg0);

        Map<String, Object> reg1 = new HashMap<>();
        reg1.put("region", "afk_room");
        reg1.put("max_afk_time", "disabled");
        regionOverridesRaw.put("1", reg1);

        Map<String, Object> reg2 = new HashMap<>();
        reg2.put("region", "trade_area");
        reg2.put("max_afk_time", "1h");
        regionOverridesRaw.put("2", reg2);
    }

    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        plugin.reloadConfig();
        configuration.init(this, "config.yml");
    }

    @PostLoad
    public void postLoad() {
        this.language = SupportedLanguage.fromConfigName(languageName);
        TimeConverter timeConverter = new TimeConverter();

        warnings.clear();
        if (warningsRaw != null) {
            for (Map<String, Object> raw : warningsRaw) {
                if (raw == null || raw.isEmpty() || !raw.containsKey("time")) continue;

                try {
                    Map<String, Object> processed = new HashMap<>(raw);
                    String timeStr = String.valueOf(raw.getOrDefault("time", "0s"));
                    processed.put("time", timeConverter.convertToField(timeStr));

                    processed.put("bar_color", String.valueOf(raw.getOrDefault("bar_color", "RED")).toUpperCase());
                    processed.put("bar_style", String.valueOf(raw.getOrDefault("bar_style", "SOLID")).toUpperCase());

                    warnings.add(processed);
                } catch (Exception e) {
                    if (plugin != null) plugin.getLogger().warning("Warning processing error: " + e.getMessage());
                }
            }

            warnings.sort((w1, w2) -> {
                Number n1 = (Number) w1.get("time");
                Number n2 = (Number) w2.get("time");
                if (n1 == null || n2 == null) return 0;
                return Long.compare(n2.longValue(), n1.longValue());
            });
        }

        if (punishmentResetAfterStr != null && punishmentResetAfterStr.equalsIgnoreCase("never")) {
            punishmentResetMillis = -1;
        } else if (punishmentResetAfterStr != null) {
            punishmentResetMillis = timeConverter.convertToField(punishmentResetAfterStr) * 1000;
        } else {
            punishmentResetMillis = 0;
        }

        punishmentLevels.clear();
        highestPunishmentCount = 0;
        if (punishmentLevelsRaw != null) {
            for (Map<String, Object> raw : punishmentLevelsRaw) {
                if (raw == null || raw.isEmpty()) continue;

                try {
                    int count = 0;
                    if (raw.get("count") instanceof Number) {
                        count = ((Number) raw.get("count")).intValue();
                    }

                    List<Map<String, String>> actions = new ArrayList<>();
                    Object actionsObj = raw.get("actions");
                    if (actionsObj instanceof List) {
                        for (Object obj : (List<?>) actionsObj) {
                            if (obj instanceof Map) {
                                actions.add((Map<String, String>) obj);
                            }
                        }
                    }

                    if (count > highestPunishmentCount) highestPunishmentCount = count;
                    punishmentLevels.add(new PunishmentLevel(count, actions));
                } catch (Exception e) {
                    if (plugin != null) plugin.getLogger().warning("Punishment level processing error: " + e.getMessage());
                }
            }
            Collections.sort(punishmentLevels);
        }

        regionOverrides.clear();
        regionOverrideMap.clear();

        if (worldGuardEnabled && regionOverridesRaw != null) {
            for (Map.Entry<String, Map<String, Object>> entry : regionOverridesRaw.entrySet()) {
                Map<String, Object> raw = entry.getValue();
                if (raw == null || raw.isEmpty()) continue;

                String regionName = (String) raw.get("region");
                if (regionName == null || regionName.isEmpty()) continue;

                String maxAfkTimeStr = String.valueOf(raw.getOrDefault("max_afk_time", "15m"));
                long time = maxAfkTimeStr.equalsIgnoreCase("disabled") ? -1 : timeConverter.convertToField(maxAfkTimeStr);

                List<Map<String, String>> regionActions = new ArrayList<>();
                Object actionsObj = raw.get("actions");

                if (actionsObj instanceof List) {
                    for (Object obj : (List<?>) actionsObj) {
                        if (obj instanceof Map) {
                            regionActions.add((Map<String, String>) obj);
                        }
                    }
                } else {
                    regionActions = this.actions;
                }

                RegionOverride override = new RegionOverride(regionName, time, regionActions);
                regionOverrides.add(override);
                regionOverrideMap.put(regionName.toLowerCase(), override);
            }
        }

        if (regionCache != null) regionCache.invalidateAll();
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
    public boolean isDiscordWebhookEnabled() { return discordWebhookEnabled; }
    public String getDiscordWebhookUrl() { return discordWebhookUrl; }
    public String getDiscordBotName() { return discordBotName; }
    public String getDiscordAvatarUrl() { return discordAvatarUrl; }
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