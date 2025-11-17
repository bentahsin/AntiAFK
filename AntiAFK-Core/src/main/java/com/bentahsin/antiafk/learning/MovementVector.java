package com.bentahsin.antiafk.learning;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.io.Serializable;

/**
 * Bir oyuncunun tek bir tick'teki göreli (relative) hareketini, eylemini ve süresini
 * temsil eden, optimize edilmiş bir veri sınıfı.
 * Bu sınıf, diske kaydedilebilmesi için Serializable arayüzünü uygular.
 */
public final class MovementVector implements Serializable {

    public enum PlayerAction {
        NONE, JUMP, SNEAK_ON, SNEAK_OFF, IDLE
    }

    private Vector2D positionChange;
    private Vector2D rotationChange;
    private PlayerAction action;
    private int durationTicks;

    /**
     * Bu constructor, Kryo serileştirme kütüphanesinin nesneyi yeniden
     * oluşturabilmesi (deserialization) için gereklidir. Doğrudan kullanılmamalıdır.
     */
    @SuppressWarnings("unused")
    private MovementVector() {}

    /**
     * Yeni bir MovementVector nesnesi oluşturur. Bu genellikle nesne havuzu (Object Pooling)
     * içinde, ilk nesneler oluşturulurken veya acil durum senaryolarında çağrılır.
     */
    public MovementVector(Vector2D positionChange, Vector2D rotationChange, PlayerAction action, int durationTicks) {
        this.positionChange = positionChange;
        this.rotationChange = rotationChange;
        this.action = action;
        this.durationTicks = durationTicks;
    }

    /**
     * Nesne havuzundan yeniden ödünç alındığında, nesneyi yeni değerlerle
     * ve daha az bellek ayırmayla (allocation) yeniden başlatmak için kullanılır.
     * @param positionChange Yeni pozisyonel değişiklik.
     * @param rotationChange Yeni rotasyonel değişiklik.
     * @param action         Yeni eylem.
     * @param durationTicks  Yeni eylemin/hareketin süresi (tick cinsinden).
     */
    public void reinitialize(Vector2D positionChange, Vector2D rotationChange, PlayerAction action, int durationTicks) {
        this.positionChange = positionChange;
        this.rotationChange = rotationChange;
        this.action = action;
        this.durationTicks = durationTicks;
    }


    public Vector2D getPositionChange() {
        return positionChange;
    }

    public Vector2D getRotationChange() {
        return rotationChange;
    }

    /**
     * Bu hareket vektörüyle ilişkilendirilen özel eylemi döndürür.
     * @return PlayerAction enum değeri.
     */
    public PlayerAction getAction() {
        return action;
    }

    /**
     * Bu hareketin veya eylemin kaç tick sürdüğünü döndürür.
     * @return Süre (tick cinsinden).
     */
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public String toString() {
        if (positionChange == null || rotationChange == null) {
            return "MovementVector[uninitialized]";
        }
        return String.format("Pos:[%.2f, %.2f] Rot:[%.2f, %.2f] Act:%s Dur:%d",
                positionChange.getX(), positionChange.getY(),
                rotationChange.getX(), rotationChange.getY(),
                action.name(),
                durationTicks);
    }
}