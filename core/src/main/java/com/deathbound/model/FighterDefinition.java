package com.deathbound.model;

import com.badlogic.gdx.graphics.Color;

public enum FighterDefinition {
    BLAZE("Aitolkyn", new Color(0.15f, 0.45f, 0.95f, 1f), null),
    VEX("Aidahar", new Color(0.9f, 0.18f, 0.18f, 1f), null),
    NOVA("Zhans", new Color(0.18f, 0.75f, 0.36f, 1f), null),
    SHADE("Er-tostik", new Color(0.58f, 0.28f, 0.88f, 1f), null);

    private final String displayName;
    private final Color color;
    private final String spritePath;

    FighterDefinition(String displayName, Color color, String spritePath) {
        this.displayName = displayName;
        this.color = color;
        this.spritePath = spritePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getColor() {
        return color;
    }

    public boolean hasSprite() {
        return spritePath != null;
    }

    public String getSpritePath() {
        return spritePath;
    }
}
