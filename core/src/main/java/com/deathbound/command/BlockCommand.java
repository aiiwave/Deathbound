package com.deathbound.command;

import com.deathbound.model.Fighter;

public class BlockCommand implements FighterCommand {
    private final boolean blocking;

    public BlockCommand(boolean blocking) {
        this.blocking = blocking;
    }

    @Override
    public void execute(Fighter fighter) {
        fighter.setBlockRequested(blocking);
    }
}
