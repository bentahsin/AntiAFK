package com.bentahsin.antiafk.learning;

import com.bentahsin.antiafk.api.learning.MovementVector;
import com.bentahsin.antiafk.api.learning.Pattern;
import com.bentahsin.antiafk.learning.serialization.KryoPatternSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatternSerializationTest {

    @Test
    @DisplayName("Kryo: Pattern Kaydedip Geri Yükleme Testi")
    void testSerializationCycle() throws Exception {
        List<MovementVector> vectors = new ArrayList<>();
        vectors.add(new MovementVector(1.5, 2.5, 90.0, 10.0, MovementVector.PlayerAction.JUMP, 5));
        vectors.add(new MovementVector(0.0, 0.0, 45.0, 0.0, MovementVector.PlayerAction.IDLE, 20));

        Pattern originalPattern = new Pattern("test_pattern", vectors);

        KryoPatternSerializer serializer = new KryoPatternSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(originalPattern, outputStream);

        byte[] data = outputStream.toByteArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        Pattern loadedPattern = serializer.deserialize(inputStream);

        assertNotNull(loadedPattern);
        assertEquals("test_pattern", loadedPattern.getName());
        assertEquals(2, loadedPattern.getVectors().size());

        MovementVector v1 = loadedPattern.getVectors().get(0);
        assertEquals(1.5, v1.getPositionChange().getX(), 0.001);
        assertEquals(2.5, v1.getPositionChange().getY(), 0.001);
        assertEquals(MovementVector.PlayerAction.JUMP, v1.getAction());
    }
}