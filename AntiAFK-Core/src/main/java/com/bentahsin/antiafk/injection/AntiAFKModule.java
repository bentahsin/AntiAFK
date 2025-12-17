package com.bentahsin.antiafk.injection;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.book.BookInputManager;
import com.bentahsin.antiafk.gui.factory.GUIFactory;
import com.bentahsin.antiafk.platform.IInputCompatibility;
import com.bentahsin.antiafk.platform.PlatformInputProxy;
import com.bentahsin.antiafk.turing.captcha.ColorPaletteCaptcha;
import com.bentahsin.antiafk.api.turing.ICaptcha;
import com.bentahsin.antiafk.turing.captcha.QuestionAnswerCaptcha;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;

import java.util.Optional;

/**
 * Guice için bağımlılık bağlamalarını (bindings) tanımlayan ana modül.
 * Hangi arayüzün hangi implementasyona karşılık geldiğini ve nesnelerin
 * yaşam döngülerini (örn: Singleton) burada belirtiriz.
 */
public class AntiAFKModule extends AbstractModule {

    private final AntiAFKPlugin plugin;

    public AntiAFKModule(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(AntiAFKPlugin.class).toInstance(plugin);
        bind(IInputCompatibility.class).to(PlatformInputProxy.class).in(Scopes.SINGLETON);

        Multibinder<ICaptcha> captchaBinder = Multibinder.newSetBinder(binder(), ICaptcha.class);
        captchaBinder.addBinding().to(QuestionAnswerCaptcha.class);
        captchaBinder.addBinding().to(ColorPaletteCaptcha.class);


        install(new FactoryModuleBuilder().build(GUIFactory.class));
    }

    @Provides
    @Singleton
    Optional<BookInputManager> provideBookInputManager(AntiAFKPlugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            return Optional.of(new BookInputManager(plugin));
        } else {
            return Optional.empty();
        }
    }
}