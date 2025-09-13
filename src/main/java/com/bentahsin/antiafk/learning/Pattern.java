package com.bentahsin.antiafk.learning;
import java.io.Serializable;
import java.util.List;
/**
 Kaydedilmiş veya belleğe yüklenmiş tam bir hareket desenini temsil eder.
 */
public class Pattern implements Serializable {
    private String name;
    private List<MovementVector> vectors;

    @SuppressWarnings("unused")
    private Pattern() {}

    public Pattern(String name, List<MovementVector> vectors) {
        this.name = name;
        this.vectors = vectors;
    }

    public String getName() {
        return name;
    }

    public List<MovementVector> getVectors() {
        return vectors;
    }
}