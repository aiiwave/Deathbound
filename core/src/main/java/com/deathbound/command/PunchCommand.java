package com.deathbound.command;

import com.deathbound.model.AttackType;
import com.deathbound.model.Fighter;

public class PunchCommand implements FighterCommand {
    @Override
    public void execute(Fighter fighter) {
        fighter.attack(AttackType.PUNCH);
    }
}
