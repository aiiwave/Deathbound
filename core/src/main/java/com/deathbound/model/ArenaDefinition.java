package com.deathbound.model;

import com.badlogic.gdx.graphics.Color;

public enum ArenaDefinition {
    VOLCANIC_RUINS(
        "VOLCANIC RUINS",
        "The Ash Altar",
        new Color(0.18f, 0.12f, 0.22f, 1f),
        new Color(0.24f, 0.16f, 0.14f, 1f),
        new Color(0.12f, 0.08f, 0.08f, 1f),
        new Color(0.9f, 0.24f, 0.15f, 1f),
        "backgrounds/location1.png"
    ),

    SHADOW_GROVE(
        "SHADOW GROVE",
        "Moonless Forest Path",
        new Color(0.03f, 0.04f, 0.08f, 1f),
        new Color(0.12f, 0.22f, 0.24f, 1f),
        new Color(0.02f, 0.03f, 0.05f, 1f),
        new Color(0.45f, 0.78f, 0.78f, 1f),
        "backgrounds/location2.jpg"
    );

    private final String displayName;
    private final String subtitle;
    private final Color bgColor;
    private final Color floorColor;
    private final Color wallColor;
    private final Color accentColor;
    private final String backgroundImagePath;

    ArenaDefinition(String displayName, String subtitle, Color bgColor,
                    Color floorColor, Color wallColor, Color accentColor, String backgroundImagePath) {
        this.displayName = displayName;
        this.subtitle = subtitle;
        this.bgColor = bgColor;
        this.floorColor = floorColor;
        this.wallColor = wallColor;
        this.accentColor = accentColor;
        this.backgroundImagePath = backgroundImagePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public Color getFloorColor() {
        return floorColor;
    }

    public Color getWallColor() {
        return wallColor;
    }

    public Color getAccentColor() {
        return accentColor;
    }

    public boolean hasImageBackground() {
        return backgroundImagePath != null;
    }

    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }
}
