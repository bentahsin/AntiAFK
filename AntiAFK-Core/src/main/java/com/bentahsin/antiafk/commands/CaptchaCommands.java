package com.bentahsin.antiafk.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.turing.CaptchaManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;

/**
 * Captcha (Turing Testi) ile ilgili oyuncu komutlarını yöneten sınıf.
 * /afkcevap ve /afktest komutlarını işler.
 */
@Singleton
@SuppressWarnings("unused")
public class CaptchaCommands extends BaseCommand {

    private final Provider<CaptchaManager> captchaManagerProvider;
    private final PlayerLanguageManager lang;

    @Inject
    public CaptchaCommands(Provider<CaptchaManager> captchaManagerProvider, PlayerLanguageManager lang) {
        this.captchaManagerProvider = captchaManagerProvider;
        this.lang = lang;
    }

    /**
     * /afkcevap <cevap>
     * Oyuncunun chat tabanlı captcha sorularına cevap vermesini sağlar.
     * Komut ismi config'den dinamik olarak gelir (%afkcevap_cmd%).
     *
     * @param player Komutu kullanan oyuncu.
     * @param answer Oyuncunun verdiği cevap (tek kelime veya cümle olabilir, ACF birleştirir).
     */
    @CommandAlias("%afkcevap_cmd")
    @Description("Aktif bot doğrulama testine cevap verir.")
    @Syntax("<cevap>")
    public void onAnswer(Player player, String answer) {
        CaptchaManager manager = captchaManagerProvider.get();

        if (manager == null) {
            lang.sendMessage(player, "turing_test.no_active_test");
            return;
        }

        manager.submitAnswer(player, answer);
    }

    /**
     * /afktest
     * Oyuncunun yanlışlıkla kapattığı Captcha arayüzünü (GUI) yeniden açar
     * veya chat testini hatırlatır.
     * Komut ismi config'den dinamik olarak gelir (%afktest_cmd%).
     *
     * @param player Komutu kullanan oyuncu.
     */
    @CommandAlias("%afktest_cmd")
    @Description("Kapatılan bot doğrulama penceresini yeniden açar.")
    public void onTest(Player player) {
        CaptchaManager manager = captchaManagerProvider.get();

        if (manager != null) {
            manager.reopenChallenge(player);
        } else {
            lang.sendMessage(player, "turing_test.test_command.not_in_test");
        }
    }
}