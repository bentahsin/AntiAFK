package com.bentahsin.antiafk.language.provider;

import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.TranslationProvider;

import java.util.EnumMap;
import java.util.Map;

public class French implements TranslationProvider {
    private final Map<Lang, String> translations = new EnumMap<>(Lang.class);
    public French() {
        add(Lang.BEHAVIOR_ANALYSIS_ENABLED_AND_INIT, "L'analyse comportementale AFK est activée. Initialisation...");
        add(Lang.BEHAVIOR_ANALYSIS_TASK_STOPPED, "La tâche d'analyse comportementale AFK a été arrêtée.");
        add(Lang.BEHAVIOR_ANALYSIS_DATA_CLEARED, "Les données des joueurs de l'analyse comportementale ont été effacées.");

        add(Lang.CAPTCHA_QUESTIONS_FILE_EMPTY_OR_INVALID, "Le fichier questions.yml est vide ou invalide ! La fonctionnalité du test de Turing ne fonctionnera pas.");

        add(Lang.ANTIAFK_COMMAND_NOT_IN_YML, "La commande AntiAFK (antiafk) n'est pas trouvée dans plugin.yml ou est mal configurée !");
        add(Lang.PLUGIN_COMMANDS_WILL_NOT_WORK, "Les commandes du plugin ne fonctionneront pas. Veuillez vérifier votre fichier plugin.yml.");
        add(Lang.AFK_COMMAND_NOT_IN_YML, "La commande /afk n'a pas été trouvée dans plugin.yml !");
        add(Lang.AFKCEVAP_COMMAND_NOT_IN_YML, "La commande /afkcevap n'a pas été trouvée dans plugin.yml !");
        add(Lang.PROTOCOLLIB_FOUND, "ProtocolLib trouvé, la fonctionnalité d'édition de livre est activée.");
        add(Lang.PROTOCOLLIB_NOT_FOUND, "ProtocolLib non trouvé ! La fonctionnalité d'édition de commande pour les actions de région est désactivée.");
        add(Lang.PLACEHOLDERAPI_FOUND, "PlaceholderAPI trouvé et l'intégration est activée.");
        add(Lang.PLACEHOLDERAPI_NOT_FOUND, "PlaceholderAPI non trouvé, les fonctionnalités de placeholders seront limitées.");
        add(Lang.WORLDGUARD_FOUND, "WorldGuard trouvé et l'intégration est activée.");
        add(Lang.WORLDGUARD_ENABLED_BUT_NOT_FOUND, "L'intégration de WorldGuard est activée dans la configuration, mais WorldGuard n'a pas été trouvé sur le serveur.");
        add(Lang.PLUGIN_ENABLED_SUCCESSFULLY, "Le plugin AntiAFK a été activé avec succès !");
        add(Lang.PLUGIN_DISABLED, "Le plugin AntiAFK a été désactivé.");

        add(Lang.MESSAGES_YML_LOADED_SUCCESSFULLY, "messages.yml chargé avec succès.");
        add(Lang.MESSAGES_YML_LOAD_ERROR, "Impossible de charger le fichier messages.yml ! C'est une erreur critique.");
        add(Lang.PLUGIN_DISABLE_CRITICAL_ERROR, "Le plugin va être désactivé pour éviter d'autres erreurs.");

        add(Lang.SKIPPING_PATTERN_FILE_TOO_LARGE, "Ignorance du fichier de patron '%s' car il dépasse la taille maximale autorisée (%d Ko).");
        add(Lang.SKIPPING_PATTERN_TOO_MANY_VECTORS, "Ignorance du patron '%s' car il dépasse le nombre maximum de vecteurs (%d).");
        add(Lang.LOADED_KNOWN_PATTERN, "Patron connu chargé : %s");
        add(Lang.COULD_NOT_LOAD_PATTERN_FILE, "Impossible de charger le fichier de patron : %s");

        add(Lang.DB_CONNECTION_SUCCESS, "Connexion à la base de données établie et table vérifiée.");
        add(Lang.DB_CONNECTION_ERROR, "Impossible de se connecter à la base de données !");
        add(Lang.DB_DISCONNECT_SUCCESS, "Connexion à la base de données fermée.");
        add(Lang.DB_DISCONNECT_ERROR, "Impossible de fermer la connexion à la base de données !");
        add(Lang.DB_RESETTING_PUNISHMENT_COUNT_INACTIVITY, "Réinitialisation du nombre de sanctions pour le joueur %s en raison de l'inactivité.");
        add(Lang.DB_GET_STATS_ERROR, "Impossible de récupérer les statistiques pour %s");
        add(Lang.DB_RESET_PUNISHMENT_TO_ZERO_ERROR, "Impossible de réinitialiser le nombre de sanctions à zéro pour %s");
        add(Lang.DB_CREATE_ENTRY_ERROR, "Impossible de créer une entrée asynchrone pour %s");
        add(Lang.DB_GET_PUNISHMENT_COUNT_ERROR, "Impossible de récupérer le nombre de sanctions pour %s");
        add(Lang.DB_RESET_PUNISHMENT_COUNT_ERROR, "Impossible de réinitialiser le nombre de sanctions asynchrones pour %s");
        add(Lang.DB_INCREMENT_PUNISHMENT_COUNT_ERROR, "Impossible d'incrémenter le nombre de sanctions asynchrones pour %s");
        add(Lang.DB_UPDATE_AFK_TIME_ERROR, "Impossible de mettre à jour le temps AFK total asynchrone pour %s");
        add(Lang.DB_INCREMENT_TESTS_PASSED_ERROR, "Impossible d'incrémenter les tests réussis asynchrones pour %s");
        add(Lang.DB_INCREMENT_TESTS_FAILED_ERROR, "Impossible d'incrémenter les tests échoués asynchrones pour %s");
        add(Lang.DB_GET_TOP_PLAYERS_INVALID_COLUMN, "Nom de colonne invalide passé à getTopPlayers : %s");
        add(Lang.DB_GET_TOP_PLAYERS_ERROR, "Impossible de récupérer les meilleurs joueurs asynchrones pour %s");

        add(Lang.INVALID_SOUND_IN_CONFIG, "Nom de son invalide dans les avertissements de config.yml : %s");

        add(Lang.PATTERN_TRANSFORM_FILE_ERROR, "Impossible de transformer le fichier de patron : %s");

        add(Lang.PATTERN_SAVED_SUCCESSFULLY, "Patron '%s' enregistré avec succès dans %s");
        add(Lang.PATTERN_SAVE_ERROR, "Impossible d'enregistrer le patron '%s'");

        add(Lang.VECTOR_POOL_BORROW_ERROR, "Impossible d'emprunter un MovementVector du pool. Création d'une nouvelle instance en guise de solution de secours.");

        add(Lang.PATTERN_MATCH_FOUND, "Correspondance de patron trouvée pour le joueur %s avec le patron '%s'. Distance : %.2f");
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