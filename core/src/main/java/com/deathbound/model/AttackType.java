package com.deathbound.model;

public enum AttackType {
    PUNCH(8, 44f, 28f, 48f, 0.08f, 0.18f, 0.32f, 0.36f, 95f),
    KICK(13, 62f, 30f, 36f, 0.12f, 0.26f, 0.46f, 0.48f, 145f);

    private final int damage;
    private final float range;
    private final float height;
    private final float offsetY;
    private final float activeStart;
    private final float activeEnd;
    private final float duration;
    private final float cooldown;
    private final float knockback;

    AttackType(int damage, float range, float height, float offsetY, float activeStart, float activeEnd, float duration, float cooldown, float knockback) {
        this.damage = damage;
        this.range = range;
        this.height = height;
        this.offsetY = offsetY;
        this.activeStart = activeStart;
        this.activeEnd = activeEnd;
        this.duration = duration;
        this.cooldown = cooldown;
        this.knockback = knockback;
    }

    public int getDamage() {
        return damage;
    }

    public float getRange() {
        return range;
    }

    public float getHeight() {
        return height;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getActiveStart() {
        return activeStart;
    }

    public float getActiveEnd() {
        return activeEnd;
    }

    public float getDuration() {
        return duration;
    }

    public float getCooldown() {
        return cooldown;
    }

    public float getKnockback() {
        return knockback;
    }
}
