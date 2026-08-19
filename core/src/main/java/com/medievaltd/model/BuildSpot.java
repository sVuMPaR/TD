package com.medievaltd.model;

public class BuildSpot {
    public final float x;
    public final float y;
    public boolean occupied;

    public BuildSpot(float x, float y) {
        this.x = x;
        this.y = y;
        this.occupied = false;
    }
}
