package com.deathbound.model;

import com.badlogic.gdx.math.Rectangle;

public class CollisionManager {
    public void processAttack(Fighter attacker, Fighter defender) {
        if (!attacker.canDamageOpponent() || attacker.getCurrentAttack() == null || !defender.isAlive()) {
            return;
        }

        Rectangle hitBox = attacker.getAttackHitBox();
        if (hitBox != null && hitBox.overlaps(defender.getHurtBox())) {
            AttackType attack = attacker.getCurrentAttack();
            if (attack == AttackType.PUNCH && defender.isCrouching()) {
                return;
            }

            float direction = attacker.getCenterX() < defender.getCenterX() ? 1f : -1f;
            defender.takeDamage(attack.getDamage(), direction * attack.getKnockback());
            attacker.markAttackHit();
        }
    }
}
