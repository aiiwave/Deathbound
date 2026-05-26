package com.deathbound.command;

import com.deathbound.model.Fighter;

public class JumpCommand implements FighterCommand {
    @Override
    public void execute(Fighter fighter) {
        fighter.jump();
    }
}
