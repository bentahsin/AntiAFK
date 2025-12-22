package com.bentahsin.antiafk.learning;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MovementVectorPoolTest {

    @Test
    @DisplayName("Havuz Mantığı: Reinitialize Metodu Verileri Ezmeli")
    void testReinitialization() {
        MovementVector vector = new MovementVector(999, 999, 999, 999, MovementVector.PlayerAction.SNEAK_ON, 100);
        vector.reinitialize(1.0, 2.0, 3.0, 4.0, MovementVector.PlayerAction.IDLE, 10);

        assertAll("Vektör verileri güncellenmiş olmalı",
                () -> assertEquals(1.0, vector.getPositionChange().getX()),
                () -> assertEquals(2.0, vector.getPositionChange().getY()),
                () -> assertEquals(3.0, vector.getRotationChange().getX()),
                () -> assertEquals(4.0, vector.getRotationChange().getY()),
                () -> assertEquals(MovementVector.PlayerAction.IDLE, vector.getAction()),
                () -> assertEquals(10, vector.getDurationTicks())
        );
    }
}