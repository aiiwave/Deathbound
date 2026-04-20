package com.deathbound.model;

import com.badlogic.gdx.math.Rectangle;
import com.deathbound.state.AttackingState;
import com.deathbound.state.BlockingState;
import com.deathbound.state.CrouchingState;
import com.deathbound.state.FighterState;
import com.deathbound.state.HitState;
import com.deathbound.state.IdleState;
import com.deathbound.state.JumpingState;
import com.deathbound.state.WalkingState;

public class Fighter {
    private static final float GRAVITY = -1800f;
    private static final float JUMP_VELOCITY = 720f;
    private static final float HIT_STUN_TIME = 0.28f;
    private static final float BLOCK_STUN_TIME = 0.12f;
    private static final float BLOCK_DAMAGE_MULTIPLIER = 0.5f;
    private static final float BLOCK_KNOCKBACK_MULTIPLIER = 0.12f;
    private static final float CROUCH_HEIGHT_SCALE = 0.62f;

    private final String name;
    private final float width;
    private final float height;
    private final float speed;
    private final int maxHealth;

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private boolean facingRight;
    private boolean onGround;
    private int health;
    private FighterState state;
    private AttackType currentAttack;
    private float attackTimer;
    private float attackCooldownTimer;
    private boolean attackAlreadyHit;
    private boolean blockRequested;
    private boolean crouchRequested;
    private float hitStunTimer;

    public Fighter(String name, float x, float y, float width, float height, float speed, int maxHealth, boolean facingRight) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.facingRight = facingRight;
        this.onGround = true;
        changeState(new IdleState());
    }

    public void update(float delta, Arena arena) {
        updateTimers(delta);

        x += velocityX * delta;

        if (!onGround || velocityY != 0f) {
            velocityY += GRAVITY * delta;
            y += velocityY * delta;
        }

        if (y <= arena.getFloorY()) {
            y = arena.getFloorY();
            velocityY = 0f;
            onGround = true;
        } else {
            onGround = false;
        }

        clampToArena(arena);
        refreshState();
        state.update(this, delta);
    }

    public void moveLeft() {
        if (!canControlMovement()) {
            return;
        }

        velocityX = -speed;
        facingRight = false;
    }

    public void moveRight() {
        if (!canControlMovement()) {
            return;
        }

        velocityX = speed;
        facingRight = true;
    }

    public void stopMoving() {
        if (!canControlMovement()) {
            return;
        }

        velocityX = 0f;
    }

    public void jump() {
        if (!onGround || !canControlMovement()) {
            return;
        }

        velocityY = JUMP_VELOCITY;
        onGround = false;
        changeState(new JumpingState());
    }

    public void takeDamage(int damage, float knockbackVelocity) {
        boolean blocked = isBlocking();
        int finalDamage = blocked ? Math.max(1, Math.round(damage * BLOCK_DAMAGE_MULTIPLIER)) : damage;
        health = Math.max(0, health - finalDamage);
        hitStunTimer = blocked ? BLOCK_STUN_TIME : HIT_STUN_TIME;
        currentAttack = null;
        attackTimer = 0f;
        attackAlreadyHit = false;
        velocityX = blocked ? knockbackVelocity * BLOCK_KNOCKBACK_MULTIPLIER : knockbackVelocity;
        changeState(new HitState());
    }

    public void attack(AttackType attackType) {
        if (!canStartAttack()) {
            return;
        }

        currentAttack = attackType;
        attackTimer = 0f;
        attackAlreadyHit = false;
        attackCooldownTimer = attackType.getCooldown();
        changeState(new AttackingState());
    }

    public void setBlockRequested(boolean blockRequested) {
        this.blockRequested = blockRequested;
    }

    public void setCrouchRequested(boolean crouchRequested) {
        this.crouchRequested = crouchRequested;
    }

    public Rectangle getHurtBox() {
        return new Rectangle(x, y, width, getBodyHeight());
    }

    public Rectangle getAttackHitBox() {
        if (!isAttackActive()) {
            return null;
        }

        float hitBoxX = facingRight ? x + width : x - currentAttack.getRange();
        float hitBoxY = y + currentAttack.getOffsetY();
        return new Rectangle(hitBoxX, hitBoxY, currentAttack.getRange(), currentAttack.getHeight());
    }

    public void markAttackHit() {
        attackAlreadyHit = true;
    }

    public void resetForRound(float x, float y, boolean facingRight) {
        this.x = x;
        this.y = y;
        this.velocityX = 0f;
        this.velocityY = 0f;
        this.facingRight = facingRight;
        this.onGround = true;
        this.health = maxHealth;
        this.currentAttack = null;
        this.attackTimer = 0f;
        this.attackCooldownTimer = 0f;
        this.attackAlreadyHit = false;
        this.blockRequested = false;
        this.crouchRequested = false;
        this.hitStunTimer = 0f;
        changeState(new IdleState());
    }

    public void stopAction() {
        velocityX = 0f;
        currentAttack = null;
        attackTimer = 0f;
        attackAlreadyHit = false;
        blockRequested = false;
        crouchRequested = false;
        if (isAlive()) {
            changeState(new IdleState());
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getBodyHeight() {
        return isCrouching() ? height * CROUCH_HEIGHT_SCALE : height;
    }

    public float getCenterX() {
        return x + width / 2f;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public String getStateName() {
        return state.getName();
    }

    public AttackType getCurrentAttack() {
        return currentAttack;
    }

    public float getAttackTimer() {
        return attackTimer;
    }

    public boolean isAttackActive() {
        return currentAttack != null
            && attackTimer >= currentAttack.getActiveStart()
            && attackTimer <= currentAttack.getActiveEnd();
    }

    public boolean canDamageOpponent() {
        return currentAttack != null && isAttackActive() && !attackAlreadyHit;
    }

    public boolean isBlocking() {
        return state instanceof BlockingState;
    }

    public boolean isCrouching() {
        return state instanceof CrouchingState;
    }

    public boolean isAttacking() {
        return state instanceof AttackingState;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void changeState(FighterState nextState) {
        state = nextState;
        state.enter(this);
    }

    private void clampToArena(Arena arena) {
        if (x < 0f) {
            x = 0f;
        }

        float maxX = arena.getWidth() - width;
        if (x > maxX) {
            x = maxX;
        }
    }

    private void updateTimers(float delta) {
        if (attackCooldownTimer > 0f) {
            attackCooldownTimer = Math.max(0f, attackCooldownTimer - delta);
        }

        if (hitStunTimer > 0f) {
            hitStunTimer = Math.max(0f, hitStunTimer - delta);
            if (hitStunTimer == 0f) {
                velocityX = 0f;
            }
        }

        if (currentAttack != null) {
            attackTimer += delta;
            if (attackTimer >= currentAttack.getDuration()) {
                currentAttack = null;
                attackTimer = 0f;
                attackAlreadyHit = false;
            }
        }
    }

    private void refreshState() {
        if (hitStunTimer > 0f) {
            if (!(state instanceof HitState)) {
                changeState(new HitState());
            }
            return;
        }

        if (blockRequested && onGround && currentAttack == null) {
            velocityX = 0f;
            if (!(state instanceof BlockingState)) {
                changeState(new BlockingState());
            }
            return;
        }

        if (currentAttack != null) {
            velocityX = 0f;
            if (!(state instanceof AttackingState)) {
                changeState(new AttackingState());
            }
            return;
        }

        if (crouchRequested && onGround) {
            velocityX = 0f;
            if (!(state instanceof CrouchingState)) {
                changeState(new CrouchingState());
            }
            return;
        }

        if (!onGround) {
            if (!(state instanceof JumpingState)) {
                changeState(new JumpingState());
            }
            return;
        }

        if (Math.abs(velocityX) > 0.01f) {
            if (!(state instanceof WalkingState)) {
                changeState(new WalkingState());
            }
            return;
        }

        if (!(state instanceof IdleState)) {
            changeState(new IdleState());
        }
    }

    private boolean canStartAttack() {
        return currentAttack == null && attackCooldownTimer == 0f && hitStunTimer == 0f && !crouchRequested && isAlive();
    }

    private boolean canControlMovement() {
        return currentAttack == null && hitStunTimer == 0f && !blockRequested && !crouchRequested && isAlive();
    }
}
