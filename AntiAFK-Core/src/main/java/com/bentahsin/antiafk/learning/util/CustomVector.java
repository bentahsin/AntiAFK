package com.bentahsin.antiafk.learning.util;

import java.io.Serializable;

public class CustomVector implements Serializable {
    private double x;
    private double y;

    public CustomVector() {
        this(0, 0);
    }

    public CustomVector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    public double distance(CustomVector other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return "{" + x + "; " + y + "}";
    }
}