package com.bentahsin.antiafk.gui.factory;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.book.BookInputManager;
import com.bentahsin.antiafk.gui.menus.*;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Guice'ın AssistedInject kullanarak GUI nesneleri oluşturmasını sağlayan fabrika arayüzü.
 */
public interface GUIFactory {

    BehavioralAnalysisGUI createBehavioralAnalysisGUI(PlayerMenuUtility playerMenuUtility);
    SettingsGUI createSettingsGUI(PlayerMenuUtility playerMenuUtility);
    AdminPanelGUI createAdminPanelGUI(PlayerMenuUtility playerMenuUtility);

    ConfirmationGUI createConfirmationGUI(
            PlayerMenuUtility playerMenuUtility,
            String title,
            ItemStack confirmationItem,
            Consumer<InventoryClickEvent> onConfirm,
            Consumer<InventoryClickEvent> onCancel
    );


    PlayerActionGUI createPlayerActionGUI(PlayerMenuUtility playerMenuUtility);

    PlayerListGUI createPlayerListGUI(PlayerMenuUtility playerMenuUtility, int page);

    RegionActionEditGUI createRegionActionEditGUI(PlayerMenuUtility playerMenuUtility);

    RegionActionsListGUI createRegionActionsListGUI(PlayerMenuUtility playerMenuUtility);

    RegionEditGUI createRegionEditGUI(PlayerMenuUtility playerMenuUtility);


    RegionListGUI createRegionListGUI(PlayerMenuUtility playerMenuUtility);
}