package com.bentahsin.antiafk.learning.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Kryo'ya, boş constructor'ı olmayan Apache Commons Math'ın Vector2D sınıfını
 * nasıl serileştireceğini ve deserialize edeceğini öğreten özel bir serileştirici.
 */
public class Vector2DSerializer extends Serializer<Vector2D> {

    @Override
    public void write(Kryo kryo, Output output, Vector2D vector) {
        output.writeDouble(vector.getX());
        output.writeDouble(vector.getY());
    }

    @Override
    public Vector2D read(Kryo kryo, Input input, Class<? extends Vector2D> type) {
        double x = input.readDouble();
        double y = input.readDouble();
        return new Vector2D(x, y);
    }
}