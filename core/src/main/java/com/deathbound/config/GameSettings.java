package com.deathbound.config;

public class GameSettings {
    private static final GameSettings INSTANCE = new GameSettings();

    private final float virtualWidth = 960f;
    private final float virtualHeight = 540f;
    private final String arenaBackgroundPath = "backgrounds/main_back.png";
    private final float fighterWidth = 46f;
    private final float fighterHeight = 96f;
    private final float fighterSpeed = 270f;
    private final int fighterHealth = 100;
    private final float roundTime = 99f;
    private final int roundsToWin = 2;

    private GameSettings() {
    }

    public static GameSettings getInstance() {
        return INSTANCE;
    }

    public float getVirtualWidth() {
        return virtualWidth;
    }

    public float getVirtualHeight() {
        return virtualHeight;
    }

    public String getArenaBackgroundPath() {
        return arenaBackgroundPath;
    }

    public float getFighterWidth() {
        return fighterWidth;
    }

    public float getFighterHeight() {
        return fighterHeight;
    }

    public float getFighterSpeed() {
        return fighterSpeed;
    }

    public int getFighterHealth() {
        return fighterHealth;
    }

    public float getRoundTime() {
        return roundTime;
    }

    public int getRoundsToWin() {
        return roundsToWin;
    }
}
