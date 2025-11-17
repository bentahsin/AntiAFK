package com.bentahsin.antiafk.models;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Kademeli ceza sistemindeki bir ceza seviyesini temsil eden, Java 8 uyumlu,
 * değişmez (immutable) bir veri sınıfı.
 */
public final class PunishmentLevel implements Comparable<PunishmentLevel> {

    private final int count;
    private final List<Map<String, String>> actions;

    public PunishmentLevel(int count, List<Map<String, String>> actions) {
        this.count = count;
        this.actions = actions;
    }

    /**
     * Bu seviyenin tetiklenmesi için gereken minimum ceza sayısını döndürür.
     */
    public int getCount() {
        return count;
    }

    /**
     * Bu seviyede uygulanacak eylemlerin listesini döndürür.
     */
    public List<Map<String, String>> getActions() {
        return actions;
    }

    /**
     * Ceza seviyelerini 'count' değerine göre büyükten küçüğe sıralamak için kullanılır.
     */
    @Override
    public int compareTo(PunishmentLevel other) {
        return Integer.compare(other.count, this.count);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PunishmentLevel that = (PunishmentLevel) o;
        return count == that.count && Objects.equals(actions, that.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, actions);
    }

    @Override
    public String toString() {
        return "PunishmentLevel{" +
                "count=" + count +
                ", actions=" + actions +
                '}';
    }
}