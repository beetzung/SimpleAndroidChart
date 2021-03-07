package com.beetzung.simpleandroidchart;

public class Entry {
    float y, x;

    public Entry(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Entry(long x, float y) {
        this.x = x;
        this.y = y;
    }

    public Entry(float x, long y) {
        this.x = x;
        this.y = y;
    }

    public Entry(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "y=" + y +
                ", x=" + x +
                '}';
    }
}
