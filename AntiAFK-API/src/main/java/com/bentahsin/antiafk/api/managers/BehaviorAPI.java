package com.bentahsin.antiafk.api.managers;

import com.bentahsin.antiafk.api.learning.MovementVector;
import org.bukkit.entity.Player;
import java.util.List;

public interface BehaviorAPI {
    /**
     * Oyuncunun son X ticklik hareket geçmişini vektör listesi olarak döndürür.
     * Bu veri, görselleştirme veya harici analiz (BenthAC) için altındır.
     */
    List<MovementVector> getTrajectory(Player player, int ticks);

    /**
     * Oyuncunun anlık "Anlamsız Aktivite" (Pointless Activity) sayacını döndürür.
     */
    int getPointlessActivityCount(Player player);

    /**
     * Oyuncunun davranışsal verilerini (history) temizler.
     */
    void resetBehaviorData(Player player);
}