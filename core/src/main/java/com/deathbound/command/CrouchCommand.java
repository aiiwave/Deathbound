package com.deathbound.command;

import com.deathbound.model.Fighter;

public class CrouchCommand implements FighterCommand {
    private final boolean crouching;

    public CrouchCommand(boolean crouching) {
        this.crouching = crouching;
    }

    @Override
    public void execute(Fighter fighter) {
        fighter.setCrouchRequested(crouching);
    }
}
