package com.bentahsin.antiafk.language.provider;

import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.TranslationProvider;

import java.util.EnumMap;
import java.util.Map;

public class Spanish implements TranslationProvider {
    private final Map<Lang, String> translations = new EnumMap<>(Lang.class);
    public Spanish() {
        add(Lang.COMMAND_REGISTERED_SUCCESS, "El comando /%s se ha registrado correctamente.");
        add(Lang.COMMAND_REGISTER_ERROR, "Ocurrió un error al registrar el comando /%s.");

        add(Lang.BEHAVIOR_ANALYSIS_ENABLED_AND_INIT, "El Análisis de Comportamiento AFK está activado. Inicializando...");
        add(Lang.BEHAVIOR_ANALYSIS_TASK_STOPPED, "La tarea de Análisis de Comportamiento AFK ha sido detenida.");
        add(Lang.BEHAVIOR_ANALYSIS_DATA_CLEARED, "Los datos de jugadores del análisis de comportamiento han sido limpiados.");

        add(Lang.CAPTCHA_QUESTIONS_FILE_EMPTY_OR_INVALID, "¡El archivo questions.yml está vacío o es inválido! La función de Test de Turing no funcionará.");

        add(Lang.ANTIAFK_COMMAND_NOT_IN_YML, "¡El comando de AntiAFK (antiafk) no se encuentra en plugin.yml o está mal configurado!");
        add(Lang.AFK_TEST_COMMAND_NOT_IN_YML, "¡El comando AFKTest (afktest) no se encontró en plugin.yml o está configurado incorrectamente!");
        add(Lang.PLUGIN_COMMANDS_WILL_NOT_WORK, "Los comandos del plugin no funcionarán. Por favor, revisa tu plugin.yml.");
        add(Lang.AFK_COMMAND_NOT_IN_YML, "¡El comando /afk no se encuentra en plugin.yml!");
        add(Lang.AFKCEVAP_COMMAND_NOT_IN_YML, "¡El comando /afkcevap no se encuentra en plugin.yml!");
        add(Lang.PROTOCOLLIB_FOUND, "ProtocolLib encontrado, la función de edición de libros está activada.");
        add(Lang.PROTOCOLLIB_NOT_FOUND, "¡ProtocolLib no encontrado! La función de edición de comandos para acciones de región está desactivada.");
        add(Lang.PLACEHOLDERAPI_FOUND, "PlaceholderAPI encontrado y la integración está activada.");
        add(Lang.PLACEHOLDERAPI_NOT_FOUND, "PlaceholderAPI no encontrado, las funciones de placeholders serán limitadas.");
        add(Lang.WORLDGUARD_FOUND, "WorldGuard encontrado y la integración está activada.");
        add(Lang.WORLDGUARD_ENABLED_BUT_NOT_FOUND, "La integración con WorldGuard está activada en la config, pero WorldGuard no se encontró en el servidor.");
        add(Lang.PLUGIN_ENABLED_SUCCESSFULLY, "¡El plugin AntiAFK ha sido activado exitosamente!");
        add(Lang.PLUGIN_DISABLED, "El plugin AntiAFK ha sido desactivado.");

        add(Lang.MESSAGES_YML_LOADED_SUCCESSFULLY, "messages.yml cargado exitosamente.");
        add(Lang.MESSAGES_YML_LOAD_ERROR, "¡No se pudo cargar el archivo messages.yml! Este es un error crítico.");
        add(Lang.PLUGIN_DISABLE_CRITICAL_ERROR, "El plugin se desactivará para prevenir más errores.");

        add(Lang.SKIPPING_PATTERN_FILE_TOO_LARGE, "Omitiendo archivo de patrón '%s' porque excede el límite máximo de tamaño de archivo (%d KB).");
        add(Lang.SKIPPING_PATTERN_TOO_MANY_VECTORS, "Omitiendo patrón '%s' porque excede el límite máximo de vectores (%d).");
        add(Lang.LOADED_KNOWN_PATTERN, "Patrón conocido cargado: %s");
        add(Lang.COULD_NOT_LOAD_PATTERN_FILE, "No se pudo cargar el archivo de patrón: %s");

        add(Lang.DB_CONNECTION_SUCCESS, "Conexión a la base de datos establecida y tabla verificada.");
        add(Lang.DB_CONNECTION_ERROR, "¡No se pudo conectar a la base de datos!");
        add(Lang.DB_DISCONNECT_SUCCESS, "Conexión a la base de datos cerrada.");
        add(Lang.DB_DISCONNECT_ERROR, "¡No se pudo cerrar la conexión a la base de datos!");
        add(Lang.DB_RESETTING_PUNISHMENT_COUNT_INACTIVITY, "Restableciendo el conteo de castigos para el jugador %s debido a inactividad.");
        add(Lang.DB_GET_STATS_ERROR, "No se pudieron obtener las estadísticas para %s");
        add(Lang.DB_RESET_PUNISHMENT_TO_ZERO_ERROR, "No se pudo restablecer a cero el conteo de castigos para %s");
        add(Lang.DB_CREATE_ENTRY_ERROR, "No se pudo crear la entrada asíncrona para %s");
        add(Lang.DB_GET_PUNISHMENT_COUNT_ERROR, "No se pudo obtener el conteo de castigos para %s");
        add(Lang.DB_RESET_PUNISHMENT_COUNT_ERROR, "No se pudo restablecer el conteo de castigos asíncrono para %s");
        add(Lang.DB_INCREMENT_PUNISHMENT_COUNT_ERROR, "No se pudo incrementar el conteo de castigos asíncrono para %s");
        add(Lang.DB_UPDATE_AFK_TIME_ERROR, "No se pudo actualizar el tiempo total de AFK asíncrono para %s");
        add(Lang.DB_INCREMENT_TESTS_PASSED_ERROR, "No se pudo incrementar los tests pasados asíncronos para %s");
        add(Lang.DB_INCREMENT_TESTS_FAILED_ERROR, "No se pudo incrementar los tests fallados asíncronos para %s");
        add(Lang.DB_GET_TOP_PLAYERS_INVALID_COLUMN, "Nombre de columna inválido pasado a getTopPlayers: %s");
        add(Lang.DB_GET_TOP_PLAYERS_ERROR, "No se pudieron obtener los mejores jugadores asíncronos para %s");

        add(Lang.INVALID_SOUND_IN_CONFIG, "Nombre de sonido inválido en las advertencias de config.yml: %s");

        add(Lang.PATTERN_TRANSFORM_FILE_ERROR, "No se pudo transformar el archivo de patrón: %s");

        add(Lang.PATTERN_SAVED_SUCCESSFULLY, "Patrón '%s' guardado exitosamente en %s");
        add(Lang.PATTERN_SAVE_ERROR, "No se pudo guardar el patrón '%s'");

        add(Lang.VECTOR_POOL_BORROW_ERROR, "No se pudo tomar un MovementVector del pool. Creando una nueva instancia como alternativa.");

        add(Lang.PATTERN_MATCH_FOUND, "Coincidencia de patrón encontrada para el jugador %s con el patrón '%s'. Distancia: %.2f");
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