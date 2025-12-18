package com.bentahsin.antiafk.learning.serialization;

import com.bentahsin.antiafk.learning.Pattern;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class KryoPatternSerializer implements ISerializer {

    private final Kryo kryo;

    public KryoPatternSerializer() {
        this.kryo = new Kryo();
        kryo.register(Pattern.class, 10);
        kryo.register(java.util.ArrayList.class, 11);
        kryo.register(com.bentahsin.antiafk.learning.MovementVector.class, 12);
        kryo.register(com.bentahsin.antiafk.learning.MovementVector.PlayerAction.class, 14);
    }

    @Override
    public void serialize(Pattern pattern, OutputStream outputStream) throws IOException {
        try (Output output = new Output(outputStream)) {
            kryo.writeObject(output, pattern);
        } catch (Exception e) {
            throw new IOException("Kryo serialization failed", e);
        }
    }

    @Override
    public Pattern deserialize(InputStream inputStream) throws IOException {
        try (Input input = new Input(inputStream)) {
            return kryo.readObject(input, Pattern.class);
        } catch (Exception e) {
            throw new IOException("Kryo deserialization failed. Your pattern files might be outdated or corrupt.", e);
        }
    }

    @Override
    public String getFileExtension() {
        return "kryo";
    }
}