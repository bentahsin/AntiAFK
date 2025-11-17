package com.bentahsin.antiafk.models;

/**

 Liderlik tablolarındaki tek bir girişi temsil eden basit bir veri sınıfı.
 */
public final class TopEntry {
    private final String username;
    private final long value;

    public TopEntry(String username, long value) {
        this.username = username;
        this.value = value;
    }

    public String getUsername() {
        return username;
    }

    public long getValue() {
        return value;
    }
}