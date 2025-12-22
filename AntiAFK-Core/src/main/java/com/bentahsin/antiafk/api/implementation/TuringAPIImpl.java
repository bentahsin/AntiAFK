package com.bentahsin.antiafk.api.implementation;

import com.bentahsin.antiafk.api.managers.TuringAPI;
import com.bentahsin.antiafk.turing.CaptchaManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;

@Singleton
public class TuringAPIImpl implements TuringAPI {

    private final Provider<CaptchaManager> captchaManagerProvider;

    @Inject
    public TuringAPIImpl(Provider<CaptchaManager> captchaManagerProvider) {
        this.captchaManagerProvider = captchaManagerProvider;
    }

    @Override
    public void openCaptcha(Player player, String captchaType) {
        CaptchaManager cm = captchaManagerProvider.get();
        if (cm != null) cm.startChallenge(player);
    }

    @Override
    public void forcePass(Player player) {
        CaptchaManager cm = captchaManagerProvider.get();
        if (cm != null && cm.isBeingTested(player)) cm.passChallenge(player);
    }

    @Override
    public void forceFail(Player player, String reason) {
        CaptchaManager cm = captchaManagerProvider.get();
        if (cm != null && cm.isBeingTested(player)) cm.failChallenge(player, reason);
    }

    @Override
    public boolean isBeingTested(Player player) {
        CaptchaManager cm = captchaManagerProvider.get();
        return cm != null && cm.isBeingTested(player);
    }
}