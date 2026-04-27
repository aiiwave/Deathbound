package com.deathbound.state;

import com.deathbound.model.Fighter;

public interface FighterState {
    void enter(Fighter fighter);

    void update(Fighter fighter, float delta);

    String getName();
}
