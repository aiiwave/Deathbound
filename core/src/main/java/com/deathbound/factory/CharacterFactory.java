package com.deathbound.factory;

import com.deathbound.config.GameSettings;
import com.deathbound.model.Fighter;
import com.deathbound.model.FighterDefinition;

public class CharacterFactory {
    private final GameSettings settings;

    public CharacterFactory() {
        this.settings = GameSettings.getInstance();
    }

    public Fighter createFighter(FighterDefinition definition, String ownerLabel, float x, float y, boolean facingRight) {
        return new Fighter(
            ownerLabel + " " + definition.getDisplayName(),
            x,
            y,
            settings.getFighterWidth(),
            settings.getFighterHeight(),
            settings.getFighterSpeed(),
            settings.getFighterHealth(),
            facingRight
        );
    }
}
