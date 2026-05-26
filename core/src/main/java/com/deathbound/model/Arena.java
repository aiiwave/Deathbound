package com.deathbound.model;

public class Arena {
    private float width;
    private float height;
    private final float floorY;

    public Arena(float width, float height, float floorY) {
        this.width = width;
        this.height = height;
        this.floorY = floorY;
    }

    public void resize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getFloorY() {
        return floorY;
    }
}
