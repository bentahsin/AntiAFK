package com.bentahsin.antiafk.geyser.captcha;

import com.bentahsin.antiafk.api.AntiAFKAPI;
import com.bentahsin.antiafk.api.turing.ICaptcha;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.*;

public class BedrockFormCaptcha implements ICaptcha {

    private final AntiAFKAPI api;
    private final PlayerLanguageManager lang;
    private final Random random = new Random();

    public BedrockFormCaptcha(AntiAFKAPI api, PlayerLanguageManager lang) {
        this.api = api;
        this.lang = lang;
    }

    @Override
    public String getTypeName() {
        return "BEDROCK_FORM"; // Config'de bu isimle geçecek
    }

    @Override
    public void start(Player player) {
        sendForm(player);
    }

    @Override
    public void reopen(Player player) {
        sendForm(player);
    }

    @Override
    public void cleanUp(Player player) {
    }

    private void sendForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) {
            return;
        }

        List<CaptchaButton> buttons = new ArrayList<>();

        buttons.add(new CaptchaButton(lang.getRawMessage("turing_test.bedrock_form.button_verify"), true));

        String failText = lang.getRawMessage("turing_test.bedrock_form.button_fake");
        buttons.add(new CaptchaButton(failText, false));
        buttons.add(new CaptchaButton(failText, false));
        buttons.add(new CaptchaButton(failText, false));

        Collections.shuffle(buttons);

        SimpleForm.Builder builder = SimpleForm.builder()
                .title(lang.getRawMessage("turing_test.bedrock_form.title"))
                .content(lang.getRawMessage("turing_test.bedrock_form.content"));

        for (CaptchaButton btn : buttons) {
            builder.button(btn.text);
        }

        builder.validResultHandler(response -> {
            int index = response.clickedButtonId();
            if (index >= 0 && index < buttons.size()) {
                CaptchaButton clicked = buttons.get(index);
                Bukkit.getScheduler().runTask(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("AntiAFK")), () -> {
                    api.submitCaptchaResult(player, clicked.isCorrect, clicked.isCorrect ? null : "Wrong Button Clicked");
                });
            }
        });

        // Formu kapatırsa veya iptal ederse
        builder.closedOrInvalidResultHandler(() -> {
            Bukkit.getScheduler().runTask(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("AntiAFK")), () -> {
                api.submitCaptchaResult(player, false, "Form Closed");
            });
        });

        FloodgateApi.getInstance().sendForm(uuid, builder.build());
    }

    private static class CaptchaButton {
        final String text;
        final boolean isCorrect;

        public CaptchaButton(String text, boolean isCorrect) {
            this.text = text;
            this.isCorrect = isCorrect;
        }
    }
}