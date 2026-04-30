package com.deathbound.state;

import com.deathbound.model.Fighter;

public class HitState implements FighterState {
    @Override
    public void enter(Fighter fighter) {
    }

    @Override
    public void update(Fighter fighter, float delta) {
    }

    @Override
    public String getName() {
        return "HIT";
    }
}
