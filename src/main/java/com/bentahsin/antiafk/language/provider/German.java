package com.bentahsin.antiafk.language.provider;

import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.TranslationProvider;

import java.util.EnumMap;
import java.util.Map;

public class German implements TranslationProvider {
    private final Map<Lang, String> translations = new EnumMap<>(Lang.class);
    public German() {
        add(Lang.COMMAND_REGISTERED_SUCCESS, "Der Befehl /%s wurde erfolgreich registriert.");
        add(Lang.COMMAND_REGISTER_ERROR, "Beim Registrieren des Befehls /%s ist ein Fehler aufgetreten.");

        add(Lang.BEHAVIOR_ANALYSIS_ENABLED_AND_INIT, "Verhaltensanalyse für AFK ist aktiviert. Initialisiere...");
        add(Lang.BEHAVIOR_ANALYSIS_TASK_STOPPED, "Die Aufgabe der Verhaltensanalyse für AFK wurde gestoppt.");
        add(Lang.BEHAVIOR_ANALYSIS_DATA_CLEARED, "Die Spielerdaten der Verhaltensanalyse wurden gelöscht.");

        add(Lang.CAPTCHA_QUESTIONS_FILE_EMPTY_OR_INVALID, "Die Datei questions.yml ist leer oder ungültig! Die Turing-Test-Funktion wird nicht funktionieren.");

        add(Lang.ANTIAFK_COMMAND_NOT_IN_YML, "Der AntiAFK-Befehl (antiafk) wurde in der plugin.yml nicht gefunden oder ist falsch konfiguriert!");
        add(Lang.PLUGIN_COMMANDS_WILL_NOT_WORK, "Die Befehle des Plugins werden nicht funktionieren. Bitte überprüfe deine plugin.yml.");
        add(Lang.AFK_COMMAND_NOT_IN_YML, "Der Befehl /afk wurde in der plugin.yml nicht gefunden!");
        add(Lang.AFKCEVAP_COMMAND_NOT_IN_YML, "Der Befehl /afkcevap wurde in der plugin.yml nicht gefunden!");
        add(Lang.PROTOCOLLIB_FOUND, "ProtocolLib gefunden, die Funktion zur Bearbeitung von Büchern ist aktiviert.");
        add(Lang.PROTOCOLLIB_NOT_FOUND, "ProtocolLib nicht gefunden! Die Funktion zur Befehlsbearbeitung für Regionsaktionen ist deaktiviert.");
        add(Lang.PLACEHOLDERAPI_FOUND, "PlaceholderAPI gefunden und Integration ist aktiviert.");
        add(Lang.PLACEHOLDERAPI_NOT_FOUND, "PlaceholderAPI nicht gefunden, Platzhalterfunktionen werden eingeschränkt sein.");
        add(Lang.WORLDGUARD_FOUND, "WorldGuard gefunden und Integration ist aktiviert.");
        add(Lang.WORLDGUARD_ENABLED_BUT_NOT_FOUND, "Die WorldGuard-Integration ist in der Konfiguration aktiviert, aber WorldGuard wurde auf dem Server nicht gefunden.");
        add(Lang.PLUGIN_ENABLED_SUCCESSFULLY, "Das AntiAFK-Plugin wurde erfolgreich aktiviert!");
        add(Lang.PLUGIN_DISABLED, "Das AntiAFK-Plugin wurde deaktiviert.");

        add(Lang.MESSAGES_YML_LOADED_SUCCESSFULLY, "messages.yml erfolgreich geladen.");
        add(Lang.MESSAGES_YML_LOAD_ERROR, "Die Datei messages.yml konnte nicht geladen werden! Dies ist ein kritischer Fehler.");
        add(Lang.PLUGIN_DISABLE_CRITICAL_ERROR, "Das Plugin wird deaktiviert, um weitere Fehler zu verhindern.");

        add(Lang.SKIPPING_PATTERN_FILE_TOO_LARGE, "Überspringe Musterdatei '%s', da sie das maximale Dateigrößenlimit (%d KB) überschreitet.");
        add(Lang.SKIPPING_PATTERN_TOO_MANY_VECTORS, "Überspringe Muster '%s', da es das maximale Vektoranzahllimit (%d) überschreitet.");
        add(Lang.LOADED_KNOWN_PATTERN, "Bekanntes Muster geladen: %s");
        add(Lang.COULD_NOT_LOAD_PATTERN_FILE, "Musterdatei konnte nicht geladen werden: %s");

        add(Lang.DB_CONNECTION_SUCCESS, "Datenbankverbindung hergestellt und Tabelle verifiziert.");
        add(Lang.DB_CONNECTION_ERROR, "Konnte keine Verbindung zur Datenbank herstellen!");
        add(Lang.DB_DISCONNECT_SUCCESS, "Datenbankverbindung geschlossen.");
        add(Lang.DB_DISCONNECT_ERROR, "Die Datenbankverbindung konnte nicht geschlossen werden!");
        add(Lang.DB_RESETTING_PUNISHMENT_COUNT_INACTIVITY, "Setze Strafenanzahl für Spieler %s wegen Inaktivität zurück.");
        add(Lang.DB_GET_STATS_ERROR, "Statistiken für %s konnten nicht abgerufen werden.");
        add(Lang.DB_RESET_PUNISHMENT_TO_ZERO_ERROR, "Strafenanzahl für %s konnte nicht auf null zurückgesetzt werden.");
        add(Lang.DB_CREATE_ENTRY_ERROR, "Asynchroner Eintrag für %s konnte nicht erstellt werden.");
        add(Lang.DB_GET_PUNISHMENT_COUNT_ERROR, "Strafenanzahl für %s konnte nicht abgerufen werden.");
        add(Lang.DB_RESET_PUNISHMENT_COUNT_ERROR, "Asynchrone Strafenanzahl für %s konnte nicht zurückgesetzt werden.");
        add(Lang.DB_INCREMENT_PUNISHMENT_COUNT_ERROR, "Asynchrone Strafenanzahl für %s konnte nicht erhöht werden.");
        add(Lang.DB_UPDATE_AFK_TIME_ERROR, "Asynchrone gesamte AFK-Zeit für %s konnte nicht aktualisiert werden.");
        add(Lang.DB_INCREMENT_TESTS_PASSED_ERROR, "Anzahl der asynchron bestandenen Tests für %s konnte nicht erhöht werden.");
        add(Lang.DB_INCREMENT_TESTS_FAILED_ERROR, "Anzahl der asynchron fehlgeschlagenen Tests für %s konnte nicht erhöht werden.");
        add(Lang.DB_GET_TOP_PLAYERS_INVALID_COLUMN, "Ungültiger Spaltenname an getTopPlayers übergeben: %s");
        add(Lang.DB_GET_TOP_PLAYERS_ERROR, "Asynchrone Top-Spieler für %s konnten nicht abgerufen werden.");

        add(Lang.INVALID_SOUND_IN_CONFIG, "Ungültiger Sound-Name in den Warnungen der config.yml: %s");

        add(Lang.PATTERN_TRANSFORM_FILE_ERROR, "Musterdatei konnte nicht transformiert werden: %s");

        add(Lang.PATTERN_SAVED_SUCCESSFULLY, "Muster '%s' erfolgreich in %s gespeichert.");
        add(Lang.PATTERN_SAVE_ERROR, "Muster '%s' konnte nicht gespeichert werden.");

        add(Lang.VECTOR_POOL_BORROW_ERROR, "Konnte MovementVector nicht aus dem Pool ausleihen. Erstelle als Fallback eine neue Instanz.");

        add(Lang.PATTERN_MATCH_FOUND, "Musterübereinstimmung für Spieler %s mit Muster '%s' gefunden. Distanz: %.2f");
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