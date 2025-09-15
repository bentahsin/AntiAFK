package com.bentahsin.antiafk.language.provider;

import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.TranslationProvider;

import java.util.EnumMap;
import java.util.Map;

public class Russian implements TranslationProvider {
    private final Map<Lang, String> translations = new EnumMap<>(Lang.class);
    public Russian() {
        add(Lang.COMMAND_REGISTERED_SUCCESS, "Команда /%s успешно зарегистрирована.");
        add(Lang.COMMAND_REGISTER_ERROR, "Произошла ошибка при регистрации команды /%s.");

        add(Lang.BEHAVIOR_ANALYSIS_ENABLED_AND_INIT, "Поведенческий анализ АФК включен. Инициализация...");
        add(Lang.BEHAVIOR_ANALYSIS_TASK_STOPPED, "Задача поведенческого анализа АФК была остановлена.");
        add(Lang.BEHAVIOR_ANALYSIS_DATA_CLEARED, "Данные игроков для поведенческого анализа были очищены.");

        add(Lang.CAPTCHA_QUESTIONS_FILE_EMPTY_OR_INVALID, "Файл questions.yml пуст или некорректен! Функция теста Тьюринга работать не будет.");

        add(Lang.ANTIAFK_COMMAND_NOT_IN_YML, "Команда AntiAFK (antiafk) не найдена в plugin.yml или настроена неверно!");
        add(Lang.PLUGIN_COMMANDS_WILL_NOT_WORK, "Команды плагина не будут работать. Пожалуйста, проверьте ваш plugin.yml.");
        add(Lang.AFK_COMMAND_NOT_IN_YML, "Команда /afk не найдена в plugin.yml!");
        add(Lang.AFKCEVAP_COMMAND_NOT_IN_YML, "Команда /afkcevap не найдена в plugin.yml!");
        add(Lang.PROTOCOLLIB_FOUND, "ProtocolLib найден, функция редактирования книг включена.");
        add(Lang.PROTOCOLLIB_NOT_FOUND, "ProtocolLib не найден! Функция редактирования команд для действий в регионах отключена.");
        add(Lang.PLACEHOLDERAPI_FOUND, "PlaceholderAPI найден, интеграция включена.");
        add(Lang.PLACEHOLDERAPI_NOT_FOUND, "PlaceholderAPI не найден, функции плейсхолдеров будут ограничены.");
        add(Lang.WORLDGUARD_FOUND, "WorldGuard найден, интеграция включена.");
        add(Lang.WORLDGUARD_ENABLED_BUT_NOT_FOUND, "Интеграция с WorldGuard включена в конфиге, но WorldGuard не найден на сервере.");
        add(Lang.PLUGIN_ENABLED_SUCCESSFULLY, "Плагин AntiAFK был успешно включен!");
        add(Lang.PLUGIN_DISABLED, "Плагин AntiAFK был отключен.");

        add(Lang.MESSAGES_YML_LOADED_SUCCESSFULLY, "Файл messages.yml успешно загружен.");
        add(Lang.MESSAGES_YML_LOAD_ERROR, "Не удалось загрузить файл messages.yml! Это критическая ошибка.");
        add(Lang.PLUGIN_DISABLE_CRITICAL_ERROR, "Плагин будет отключен, чтобы предотвратить дальнейшие ошибки.");

        add(Lang.SKIPPING_PATTERN_FILE_TOO_LARGE, "Пропуск файла паттерна '%s', так как он превышает максимальный лимит размера файла (%d КБ).");
        add(Lang.SKIPPING_PATTERN_TOO_MANY_VECTORS, "Пропуск паттерна '%s', так как он превышает максимальный лимит векторов (%d).");
        add(Lang.LOADED_KNOWN_PATTERN, "Загружен известный паттерн: %s");
        add(Lang.COULD_NOT_LOAD_PATTERN_FILE, "Не удалось загрузить файл паттерна: %s");

        add(Lang.DB_CONNECTION_SUCCESS, "Соединение с базой данных установлено, таблица проверена.");
        add(Lang.DB_CONNECTION_ERROR, "Не удалось подключиться к базе данных!");
        add(Lang.DB_DISCONNECT_SUCCESS, "Соединение с базой данных закрыто.");
        add(Lang.DB_DISCONNECT_ERROR, "Не удалось закрыть соединение с базой данных!");
        add(Lang.DB_RESETTING_PUNISHMENT_COUNT_INACTIVITY, "Сброс счетчика наказаний для игрока %s из-за неактивности.");
        add(Lang.DB_GET_STATS_ERROR, "Не удалось получить статистику для %s");
        add(Lang.DB_RESET_PUNISHMENT_TO_ZERO_ERROR, "Не удалось сбросить счетчик наказаний до нуля для %s");
        add(Lang.DB_CREATE_ENTRY_ERROR, "Не удалось создать асинхронную запись для %s");
        add(Lang.DB_GET_PUNISHMENT_COUNT_ERROR, "Не удалось получить счетчик наказаний для %s");
        add(Lang.DB_RESET_PUNISHMENT_COUNT_ERROR, "Не удалось сбросить асинхронный счетчик наказаний для %s");
        add(Lang.DB_INCREMENT_PUNISHMENT_COUNT_ERROR, "Не удалось увеличить асинхронный счетчик наказаний для %s");
        add(Lang.DB_UPDATE_AFK_TIME_ERROR, "Не удалось обновить общее асинхронное время АФК для %s");
        add(Lang.DB_INCREMENT_TESTS_PASSED_ERROR, "Не удалось увеличить количество асинхронно пройденных тестов для %s");
        add(Lang.DB_INCREMENT_TESTS_FAILED_ERROR, "Не удалось увеличить количество асинхронно проваленных тестов для %s");
        add(Lang.DB_GET_TOP_PLAYERS_INVALID_COLUMN, "Недопустимое имя столбца передано в getTopPlayers: %s");
        add(Lang.DB_GET_TOP_PLAYERS_ERROR, "Не удалось получить асинхронный топ игроков для %s");

        add(Lang.INVALID_SOUND_IN_CONFIG, "Недопустимое имя звука в предупреждениях config.yml: %s");

        add(Lang.PATTERN_TRANSFORM_FILE_ERROR, "Не удалось преобразовать файл паттерна: %s");

        add(Lang.PATTERN_SAVED_SUCCESSFULLY, "Паттерн '%s' успешно сохранен в %s");
        add(Lang.PATTERN_SAVE_ERROR, "Не удалось сохранить паттерн '%s'");

        add(Lang.VECTOR_POOL_BORROW_ERROR, "Не удалось заимствовать MovementVector из пула. Создание нового экземпляра в качестве запасного варианта.");

        add(Lang.PATTERN_MATCH_FOUND, "Найдено совпадение паттерна для игрока %s с паттерном '%s'. Расстояние: %.2f");
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