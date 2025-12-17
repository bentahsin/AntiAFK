package com.bentahsin.antiafk.platform;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Bu sınıf, Java ve Geyser (Bedrock) implementasyonları arasında dinamik bir köprü görevi görür.
 * Guice tarafından tekil (Singleton) olarak yönetilir ancak her çağrıda
 * Bukkit ServiceManager'ı kontrol ederek doğru implementasyonu seçer.
 */
@Singleton
public class PlatformInputProxy implements IInputCompatibility {

    private final JavaInputCompatibility javaImplementation;
    private final AntiAFKPlugin plugin;

    @Inject
    public PlatformInputProxy(AntiAFKPlugin plugin, JavaInputCompatibility javaImplementation) {
        this.plugin = plugin;
        this.javaImplementation = javaImplementation;
    }

    /**
     * Aktif olan implementasyonu bulur.
     * Eğer Geyser modülü yüklenmiş ve servisi kaydetmişse onu döndürür.
     * Aksi takdirde varsayılan Java implementasyonunu döndürür.
     */
    private IInputCompatibility getDelegate() {
        RegisteredServiceProvider<IInputCompatibility> rsp = Bukkit.getServicesManager().getRegistration(IInputCompatibility.class);
        if (rsp != null) {
            return rsp.getProvider();
        }
        return javaImplementation;
    }

    @Override
    public void promptForInput(Player player, String title, String initialText, Consumer<String> onConfirm, Runnable onCancel) {
        getDelegate().promptForInput(player, title, initialText, onConfirm, onCancel);
    }

    @Override
    public void promptForInput(Player player, String title, Consumer<String> onConfirm, Runnable onCancel) {
        getDelegate().promptForInput(player, title, onConfirm, onCancel);
    }

    @Override
    public boolean isBedrockPlayer(UUID playerUUID) {
        return getDelegate().isBedrockPlayer(playerUUID);
    }
}