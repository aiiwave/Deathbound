package com.deathbound.command;

import com.deathbound.model.Fighter;

public class MoveRightCommand implements FighterCommand {
    @Override
    public void execute(Fighter fighter) {
        fighter.moveRight();
    }
}
