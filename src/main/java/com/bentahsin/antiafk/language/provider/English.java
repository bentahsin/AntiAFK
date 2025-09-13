package com.bentahsin.antiafk.language.provider;

import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.TranslationProvider;

import java.util.EnumMap;
import java.util.Map;

public class English implements TranslationProvider {
    private final Map<Lang, String> translations = new EnumMap<>(Lang.class);
    public English() {
        add(Lang.COMMAND_UNREGISTERED_SUCCESS, "Command /%s was successfully unregistered.");
        add(Lang.COMMAND_UNREGISTER_ERROR, "An error occurred while unregistering command /%s.");

        add(Lang.BEHAVIOR_ANALYSIS_ENABLED_AND_INIT, "Behavioral AFK Analysis is enabled. Initializing...");
        add(Lang.BEHAVIOR_ANALYSIS_TASK_STOPPED, "Behavioral AFK Analysis task has been stopped.");
        add(Lang.BEHAVIOR_ANALYSIS_DATA_CLEARED, "Behavioral analysis player data has been cleared.");

        add(Lang.CAPTCHA_QUESTIONS_FILE_EMPTY_OR_INVALID, "questions.yml file is empty or invalid! The Turing Test feature will not work.");

        add(Lang.ANTIAFK_COMMAND_NOT_IN_YML, "AntiAFK command (antiafk) not found in plugin.yml or is misconfigured!");
        add(Lang.PLUGIN_COMMANDS_WILL_NOT_WORK, "Plugin commands will not work. Please check your plugin.yml.");
        add(Lang.AFK_COMMAND_NOT_IN_YML, "/afk command not found in plugin.yml!");
        add(Lang.AFKCEVAP_COMMAND_NOT_IN_YML, "/afkcevap command not found in plugin.yml!");
        add(Lang.PROTOCOLLIB_FOUND, "ProtocolLib found, book editing feature is enabled.");
        add(Lang.PROTOCOLLIB_NOT_FOUND, "ProtocolLib not found! Command editing feature for region actions is disabled.");
        add(Lang.PLACEHOLDERAPI_FOUND, "PlaceholderAPI found and integration is enabled.");
        add(Lang.PLACEHOLDERAPI_NOT_FOUND, "PlaceholderAPI not found, placeholder features will be limited.");
        add(Lang.WORLDGUARD_FOUND, "WorldGuard found and integration is enabled.");
        add(Lang.WORLDGUARD_ENABLED_BUT_NOT_FOUND, "WorldGuard integration is enabled in config, but WorldGuard was not found on the server.");
        add(Lang.PLUGIN_ENABLED_SUCCESSFULLY, "AntiAFK plugin has been successfully enabled!");
        add(Lang.PLUGIN_DISABLED, "AntiAFK plugin has been disabled.");

        add(Lang.MESSAGES_YML_LOADED_SUCCESSFULLY, "messages.yml loaded successfully.");
        add(Lang.MESSAGES_YML_LOAD_ERROR, "Could not load messages.yml file! This is a critical error.");
        add(Lang.PLUGIN_DISABLE_CRITICAL_ERROR, "The plugin will be disabled to prevent further errors.");

        add(Lang.SKIPPING_PATTERN_FILE_TOO_LARGE, "Skipping pattern file '%s' because it exceeds the max file size limit (%d KB).");
        add(Lang.SKIPPING_PATTERN_TOO_MANY_VECTORS, "Skipping pattern '%s' because it exceeds the max vector count limit (%d).");
        add(Lang.LOADED_KNOWN_PATTERN, "Loaded known pattern: %s");
        add(Lang.COULD_NOT_LOAD_PATTERN_FILE, "Could not load pattern file: %s");

        add(Lang.DB_CONNECTION_SUCCESS, "Database connection established and table verified.");
        add(Lang.DB_CONNECTION_ERROR, "Could not connect to the database!");
        add(Lang.DB_DISCONNECT_SUCCESS, "Database connection closed.");
        add(Lang.DB_DISCONNECT_ERROR, "Could not close the database connection!");
        add(Lang.DB_RESETTING_PUNISHMENT_COUNT_INACTIVITY, "Resetting punishment count for player %s due to inactivity.");
        add(Lang.DB_GET_STATS_ERROR, "Could not retrieve stats for %s");
        add(Lang.DB_RESET_PUNISHMENT_TO_ZERO_ERROR, "Could not reset punishment count to zero for %s");
        add(Lang.DB_CREATE_ENTRY_ERROR, "Could not create async entry for %s");
        add(Lang.DB_GET_PUNISHMENT_COUNT_ERROR, "Could not retrieve punishment count for %s");
        add(Lang.DB_RESET_PUNISHMENT_COUNT_ERROR, "Could not reset async punishment count for %s");
        add(Lang.DB_INCREMENT_PUNISHMENT_COUNT_ERROR, "Could not increment async punishment count for %s");
        add(Lang.DB_UPDATE_AFK_TIME_ERROR, "Could not update async total AFK time for %s");
        add(Lang.DB_INCREMENT_TESTS_PASSED_ERROR, "Could not increment async tests passed for %s");
        add(Lang.DB_INCREMENT_TESTS_FAILED_ERROR, "Could not increment async tests failed for %s");
        add(Lang.DB_GET_TOP_PLAYERS_INVALID_COLUMN, "Invalid column name passed to getTopPlayers: %s");
        add(Lang.DB_GET_TOP_PLAYERS_ERROR, "Could not retrieve async top players for %s");

        add(Lang.INVALID_SOUND_IN_CONFIG, "Invalid sound name in config.yml warnings: %s");

        add(Lang.PATTERN_TRANSFORM_FILE_ERROR, "Could not transform pattern file: %s");

        add(Lang.PATTERN_SAVED_SUCCESSFULLY, "Pattern '%s' successfully saved to %s");
        add(Lang.PATTERN_SAVE_ERROR, "Could not save pattern '%s'");

        add(Lang.VECTOR_POOL_BORROW_ERROR, "Could not borrow MovementVector from pool. Creating a new instance as a fallback.");

        add(Lang.PATTERN_MATCH_FOUND, "Pattern match found for player %s with pattern '%s'. Distance: %.2f");
    }
    private void add(Lang key, String message) {
        translations.put(key, message);
    }
    private void addLines(Lang key, String... lines) {
        translations.put(key, String.join("\n", lines));
    }

    @Override
    public String get(Lang key) {
        return translations.getOrDefault(key, "MISSING_TRANSLATION: " + key.name());
    }
}
