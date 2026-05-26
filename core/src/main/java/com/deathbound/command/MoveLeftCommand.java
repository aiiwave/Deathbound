package com.deathbound.command;

import com.deathbound.model.Fighter;

public class MoveLeftCommand implements FighterCommand {
    @Override
    public void execute(Fighter fighter) {
        fighter.moveLeft();
    }
}
