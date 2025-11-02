package com.bentahsin.antiafk.language.provider;

import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.TranslationProvider;

import java.util.EnumMap;
import java.util.Map;

public class Polish implements TranslationProvider {
    private final Map<Lang, String> translations = new EnumMap<>(Lang.class);
    public Polish() {
        add(Lang.COMMAND_REGISTERED_SUCCESS, "Polecenie /%s zostało pomyślnie rejestrowane.");
        add(Lang.COMMAND_REGISTER_ERROR, "Wystąpił błąd podczas rejestrowania polecenia /%s.");

        add(Lang.BEHAVIOR_ANALYSIS_ENABLED_AND_INIT, "Behawioralna analiza AFK jest włączona. Inicjalizacja...");
        add(Lang.BEHAVIOR_ANALYSIS_TASK_STOPPED, "Zadanie behawioralnej analizy AFK zostało zatrzymane.");
        add(Lang.BEHAVIOR_ANALYSIS_DATA_CLEARED, "Dane graczy z analizy behawioralnej zostały wyczyszczone.");

        add(Lang.CAPTCHA_QUESTIONS_FILE_EMPTY_OR_INVALID, "Plik questions.yml jest pusty lub nieprawidłowy! Funkcja testu Turinga nie będzie działać.");

        add(Lang.ANTIAFK_COMMAND_NOT_IN_YML, "Komenda AntiAFK (antiafk) nie została znaleziona w plugin.yml lub jest źle skonfigurowana!");
        add(Lang.AFK_TEST_COMMAND_NOT_IN_YML, "Komenda AFKTest (afktest) nie została znaleziona w pliku plugin.yml lub jest nieprawidłowo skonfigurowana!");
        add(Lang.PLUGIN_COMMANDS_WILL_NOT_WORK, "Komendy pluginu nie będą działać. Sprawdź plik plugin.yml.");
        add(Lang.AFK_COMMAND_NOT_IN_YML, "Komenda /afk nie została znaleziona w plugin.yml!");
        add(Lang.AFKCEVAP_COMMAND_NOT_IN_YML, "Komenda /afkcevap nie została znaleziona w plugin.yml!");
        add(Lang.PROTOCOLLIB_FOUND, "Znaleziono ProtocolLib, funkcja edycji książek jest włączona.");
        add(Lang.PROTOCOLLIB_NOT_FOUND, "Nie znaleziono ProtocolLib! Funkcja edycji komend dla akcji w regionach jest wyłączona.");
        add(Lang.PLACEHOLDERAPI_FOUND, "Znaleziono PlaceholderAPI, integracja jest włączona.");
        add(Lang.PLACEHOLDERAPI_NOT_FOUND, "Nie znaleziono PlaceholderAPI, funkcje placeholderów będą ograniczone.");
        add(Lang.WORLDGUARD_FOUND, "Znaleziono WorldGuard, integracja jest włączona.");
        add(Lang.WORLDGUARD_ENABLED_BUT_NOT_FOUND, "Integracja z WorldGuard jest włączona w konfiguracji, ale nie znaleziono WorldGuard na serwerze.");
        add(Lang.PLUGIN_ENABLED_SUCCESSFULLY, "Plugin AntiAFK został pomyślnie włączony!");
        add(Lang.PLUGIN_DISABLED, "Plugin AntiAFK został wyłączony.");

        add(Lang.MESSAGES_YML_LOADED_SUCCESSFULLY, "Plik messages.yml załadowano pomyślnie.");
        add(Lang.MESSAGES_YML_LOAD_ERROR, "Nie można załadować pliku messages.yml! To jest krytyczny błąd.");
        add(Lang.PLUGIN_DISABLE_CRITICAL_ERROR, "Plugin zostanie wyłączony, aby zapobiec dalszym błędom.");

        add(Lang.SKIPPING_PATTERN_FILE_TOO_LARGE, "Pomijanie pliku wzoru '%s', ponieważ przekracza on maksymalny limit rozmiaru pliku (%d KB).");
        add(Lang.SKIPPING_PATTERN_TOO_MANY_VECTORS, "Pomijanie wzoru '%s', ponieważ przekracza on maksymalny limit wektorów (%d).");
        add(Lang.LOADED_KNOWN_PATTERN, "Załadowano znany wzór: %s");
        add(Lang.COULD_NOT_LOAD_PATTERN_FILE, "Nie można załadować pliku wzoru: %s");

        add(Lang.DB_CONNECTION_SUCCESS, "Nawiązano połączenie z bazą danych, a tabela została zweryfikowana.");
        add(Lang.DB_CONNECTION_ERROR, "Nie można połączyć się z bazą danych!");
        add(Lang.DB_DISCONNECT_SUCCESS, "Połączenie z bazą danych zostało zamknięte.");
        add(Lang.DB_DISCONNECT_ERROR, "Nie można zamknąć połączenia z bazą danych!");
        add(Lang.DB_RESETTING_PUNISHMENT_COUNT_INACTIVITY, "Resetowanie licznika kar dla gracza %s z powodu nieaktywności.");
        add(Lang.DB_GET_STATS_ERROR, "Nie można pobrać statystyk dla %s");
        add(Lang.DB_RESET_PUNISHMENT_TO_ZERO_ERROR, "Nie można zresetować licznika kar do zera dla %s");
        add(Lang.DB_CREATE_ENTRY_ERROR, "Nie można utworzyć asynchronicznego wpisu dla %s");
        add(Lang.DB_GET_PUNISHMENT_COUNT_ERROR, "Nie można pobrać licznika kar dla %s");
        add(Lang.DB_RESET_PUNISHMENT_COUNT_ERROR, "Nie można zresetować asynchronicznego licznika kar dla %s");
        add(Lang.DB_INCREMENT_PUNISHMENT_COUNT_ERROR, "Nie można zwiększyć asynchronicznego licznika kar dla %s");
        add(Lang.DB_UPDATE_AFK_TIME_ERROR, "Nie można zaktualizować całkowitego asynchronicznego czasu AFK dla %s");
        add(Lang.DB_INCREMENT_TESTS_PASSED_ERROR, "Nie można zwiększyć liczby asynchronicznie zdanych testów dla %s");
        add(Lang.DB_INCREMENT_TESTS_FAILED_ERROR, "Nie można zwiększyć liczby asynchronicznie niezdanych testów dla %s");
        add(Lang.DB_GET_TOP_PLAYERS_INVALID_COLUMN, "Nieprawidłowa nazwa kolumny przekazana do getTopPlayers: %s");
        add(Lang.DB_GET_TOP_PLAYERS_ERROR, "Nie można pobrać asynchronicznej listy topowych graczy dla %s");

        add(Lang.INVALID_SOUND_IN_CONFIG, "Nieprawidłowa nazwa dźwięku w ostrzeżeniach pliku config.yml: %s");

        add(Lang.PATTERN_TRANSFORM_FILE_ERROR, "Nie można przekształcić pliku wzoru: %s");

        add(Lang.PATTERN_SAVED_SUCCESSFULLY, "Wzór '%s' został pomyślnie zapisany w %s");
        add(Lang.PATTERN_SAVE_ERROR, "Nie można zapisać wzoru '%s'");

        add(Lang.VECTOR_POOL_BORROW_ERROR, "Nie można pożyczyć MovementVector z puli. Tworzenie nowej instancji jako obejście.");

        add(Lang.PATTERN_MATCH_FOUND, "Znaleziono dopasowanie wzoru dla gracza %s ze wzorem '%s'. Odległość: %.2f");
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