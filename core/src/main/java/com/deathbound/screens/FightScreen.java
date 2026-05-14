package com.deathbound.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deathbound.DeathBoundGame;
import com.deathbound.config.GameSettings;
import com.deathbound.factory.CharacterFactory;
import com.deathbound.input.PlayerInputController;
import com.deathbound.model.Arena;
import com.deathbound.model.ArenaDefinition;
import com.deathbound.model.CollisionManager;
import com.deathbound.model.Fighter;
import com.deathbound.model.FighterDefinition;
import com.deathbound.model.RoundManager;
import com.deathbound.ui.PixelBackground;

public class FightScreen implements Screen {
    private static final float FLOOR_Y = 80f;

    private final DeathBoundGame game;
    private final FightMode mode;
    private final GameSettings settings;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;
    private final PixelBackground arenaBackground;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final Arena arena;
    private final CollisionManager collisionManager;
    private final RoundManager roundManager;
    private final FighterDefinition playerOneDefinition;
    private final FighterDefinition playerTwoDefinition;
    private final ArenaDefinition arenaDefinition;
    private final CharacterFactory characterFactory;
    private final Fighter playerOne;
    private final Fighter playerTwo;
    private final PlayerInputController playerOneInput;
    private final PlayerInputController playerTwoInput;
    private final Texture playerOneSprite;
    private final Texture playerTwoSprite;

    public FightScreen(DeathBoundGame game, FightMode mode) {
        this(game, mode, FighterDefinition.BLAZE, FighterDefinition.VEX, ArenaDefinition.VOLCANIC_RUINS);
    }

    public FightScreen(DeathBoundGame game, FightMode mode,
                       FighterDefinition playerOneDefinition,
                       FighterDefinition playerTwoDefinition,
                       ArenaDefinition arenaDefinition) {
        this.game = game;
        this.mode = mode;
        this.playerOneDefinition = playerOneDefinition;
        this.playerTwoDefinition = playerTwoDefinition;
        this.arenaDefinition     = arenaDefinition;
        this.settings = GameSettings.getInstance();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(settings.getVirtualWidth(), settings.getVirtualHeight(), camera);
        this.shapeRenderer = new ShapeRenderer();
        this.arenaBackground = arenaDefinition.hasImageBackground()
            ? new PixelBackground(arenaDefinition.getBackgroundImagePath())
            : null;
        this.font = new BitmapFont();
        this.font.getData().setScale(1.25f);
        this.layout = new GlyphLayout();
        this.arena = new Arena(settings.getVirtualWidth(), settings.getVirtualHeight(), FLOOR_Y);
        this.collisionManager = new CollisionManager();
        this.roundManager = new RoundManager();
        this.characterFactory = new CharacterFactory();
        this.playerOne = characterFactory.createFighter(playerOneDefinition, "P1", 110f, FLOOR_Y, true);
        String secondOwnerLabel = mode == FightMode.PLAYER_VS_BOT ? "BOT" : "P2";
        this.playerTwo = characterFactory.createFighter(playerTwoDefinition, secondOwnerLabel, arena.getWidth() - 156f, FLOOR_Y, false);
        this.playerOneInput = new PlayerInputController(playerOne, Input.Keys.A, Input.Keys.D, Input.Keys.W, Input.Keys.S, Input.Keys.E, Input.Keys.R, Input.Keys.F);
        this.playerTwoInput = new PlayerInputController(playerTwo, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.UP, Input.Keys.DOWN, Input.Keys.NUMPAD_1, Input.Keys.NUMPAD_2, Input.Keys.NUMPAD_3);
        this.playerOneSprite = loadSprite(playerOneDefinition);
        this.playerTwoSprite = loadSprite(playerTwoDefinition);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        if (update(delta)) {
            return;
        }
        draw();
    }

    private boolean update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.changeScreen(new MainMenuScreen(game));
            return true;
        }

        if (roundManager.isWaitingForContinue()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                roundManager.continueAfterRound(playerOne, playerTwo, arena);
            }
            return false;
        }

        playerOneInput.update();
        if (mode == FightMode.PLAYER_VS_PLAYER) {
            playerTwoInput.update();
        } else {
            playerTwo.stopMoving();
        }

        playerOne.update(delta, arena);
        playerTwo.update(delta, arena);
        keepFightersApart();
        collisionManager.processAttack(playerOne, playerTwo);
        collisionManager.processAttack(playerTwo, playerOne);
        roundManager.update(delta, playerOne, playerTwo);
        return false;
    }

    private void draw() {
        viewport.apply();
        shapeRenderer.setProjectionMatrix(camera.combined);
        game.batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0.06f, 0.07f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (arenaBackground != null) {
            game.batch.begin();
            arenaBackground.draw(game.batch, arena.getWidth(), arena.getHeight());
            game.batch.end();
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawArena();
        drawHealthBarShape(30f, arena.getHeight() - 35f, 230f, playerOne, false);
        drawHealthBarShape(arena.getWidth() - 260f, arena.getHeight() - 35f, 230f, playerTwo, true);
        drawRoundMarkers(42f, arena.getHeight() - 58f, roundManager.getPlayerOneRoundWins(), false);
        drawRoundMarkers(arena.getWidth() - 42f, arena.getHeight() - 58f, roundManager.getPlayerTwoRoundWins(), true);
        drawFighterShadow(playerOne);
        drawFighterShadow(playerTwo);
        drawFighterBody(playerOne, playerOneDefinition.getColor(), playerOneSprite != null);
        drawFighterBody(playerTwo, playerTwoDefinition.getColor(), playerTwoSprite != null);
        shapeRenderer.end();

        game.batch.begin();
        drawFighterSprite(playerOne, playerOneSprite);
        drawFighterSprite(playerTwo, playerTwoSprite);
        game.batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawBlockShield(playerOne);
        drawBlockShield(playerTwo);
        drawAttackHitBox(playerOne);
        drawAttackHitBox(playerTwo);
        drawRoundOverlay();
        shapeRenderer.end();

        game.batch.begin();
        drawUi();
        game.batch.end();
    }

    private void drawArena() {
        if (arenaDefinition.hasImageBackground()) {
            return;
        }

        shapeRenderer.setColor(arenaDefinition.getBgColor());
        shapeRenderer.rect(0f, arena.getFloorY(), arena.getWidth(), arena.getHeight() - arena.getFloorY());
        shapeRenderer.setColor(arenaDefinition.getWallColor());
        shapeRenderer.rect(0f, 0f, arena.getWidth(), arena.getFloorY());
        shapeRenderer.setColor(arenaDefinition.getFloorColor());
        shapeRenderer.rect(0f, arena.getFloorY(), arena.getWidth(), 4f);
        shapeRenderer.setColor(arenaDefinition.getAccentColor());
        shapeRenderer.rect(0f, arena.getFloorY() + 4f, arena.getWidth(), 2f);
    }

    private Texture loadSprite(FighterDefinition definition) {
        if (!definition.hasSprite()) {
            return null;
        }

        Texture texture = new Texture(Gdx.files.internal(definition.getSpritePath()));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    private void drawFighterShadow(Fighter fighter) {
        shapeRenderer.setColor(0f, 0f, 0f, 0.3f);
        shapeRenderer.ellipse(fighter.getX() - 10f, arena.getFloorY() - 10f, fighter.getWidth() + 20f, 16f);
    }

    private void drawFighterBody(Fighter fighter, Color bodyColor, boolean hasVisualSprite) {
        if (hasVisualSprite) {
            return;
        }

        shapeRenderer.setColor(bodyColor);
        shapeRenderer.rect(fighter.getX(), fighter.getY(), fighter.getWidth(), fighter.getBodyHeight());
        shapeRenderer.setColor(Color.WHITE);
        float eyeX = fighter.isFacingRight() ? fighter.getX() + fighter.getWidth() - 12f : fighter.getX() + 8f;
        shapeRenderer.rect(eyeX, fighter.getY() + fighter.getBodyHeight() - 25f, 8f, 8f);
    }

    private void drawFighterSprite(Fighter fighter, Texture sprite) {
        if (sprite == null) {
            return;
        }

        float visualHeight = fighter.getBodyHeight() * 1.35f;
        float visualWidth = visualHeight * sprite.getWidth() / (float) sprite.getHeight();
        float x = fighter.getCenterX() - visualWidth / 2f;
        float y = fighter.getY();
        boolean flipX = !fighter.isFacingRight();

        game.batch.draw(sprite, x, y, visualWidth, visualHeight,
            0, 0, sprite.getWidth(), sprite.getHeight(), flipX, false);
    }

    private void drawBlockShield(Fighter fighter) {
        if (fighter.isBlocking()) {
            shapeRenderer.setColor(0.25f, 0.75f, 1f, 1f);
            float shieldX = fighter.isFacingRight() ? fighter.getX() + fighter.getWidth() + 5f : fighter.getX() - 13f;
            shapeRenderer.rect(shieldX, fighter.getY() + 18f, 8f, fighter.getBodyHeight() - 30f);
        }
    }

    private void drawAttackHitBox(Fighter fighter) {
        Rectangle attackHitBox = fighter.getAttackHitBox();
        if (attackHitBox == null) {
            return;
        }

        shapeRenderer.setColor(1f, 0.82f, 0.08f, 1f);
        shapeRenderer.rect(attackHitBox.x, attackHitBox.y, attackHitBox.width, attackHitBox.height);
    }

    private void drawRoundMarkers(float x, float y, int wins, boolean alignRight) {
        for (int i = 0; i < 2; i++) {
            shapeRenderer.setColor(i < wins ? Color.YELLOW : new Color(0.18f, 0.18f, 0.2f, 1f));
            float markerX = alignRight ? x - i * 18f : x + i * 18f;
            shapeRenderer.circle(markerX, y, 6f);
        }
    }

    private void drawRoundOverlay() {
        if (roundManager.isFighting()) {
            return;
        }

        shapeRenderer.setColor(0.02f, 0.02f, 0.03f, 1f);
        shapeRenderer.rect(0f, arena.getHeight() / 2f - 65f, arena.getWidth(), 120f);
    }

    private void drawUi() {
        font.setColor(Color.WHITE);
        font.draw(game.batch, playerOne.getName() + " HP " + playerOne.getHealth(), 30f, arena.getHeight() - 42f);

        String p2Hp = playerTwo.getName() + " HP " + playerTwo.getHealth();
        layout.setText(font, p2Hp);
        font.draw(game.batch, p2Hp, arena.getWidth() - layout.width - 30f, arena.getHeight() - 42f);

        drawCenteredText("ROUND " + roundManager.getRoundNumber() + " | TIME " + roundManager.getTimerSeconds(), arena.getWidth() / 2f, arena.getHeight() - 22f);

        if (roundManager.isWaitingForContinue()) {
            drawCenteredText(roundManager.getResultMessage(), arena.getWidth() / 2f, arena.getHeight() / 2f + 10f);
        }
    }

    private void drawHealthBarShape(float x, float y, float width, Fighter fighter, boolean alignRight) {
        float height = 18f;
        float healthPercent = fighter.getHealth() / (float) fighter.getMaxHealth();
        float filledWidth = width * healthPercent;

        shapeRenderer.setColor(0.03f, 0.03f, 0.04f, 1f);
        shapeRenderer.rect(x - 2f, y - 2f, width + 4f, height + 4f);
        shapeRenderer.setColor(0.25f, 0.03f, 0.04f, 1f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(0.0f, 0.75f, 0.24f, 1f);
        if (alignRight) {
            shapeRenderer.rect(x + width - filledWidth, y, filledWidth, height);
        } else {
            shapeRenderer.rect(x, y, filledWidth, height);
        }
    }

    private void drawCenteredText(String text, float centerX, float y) {
        layout.setText(font, text);
        font.draw(game.batch, text, centerX - layout.width / 2f, y);
    }

    private void keepFightersApart() {
        Rectangle p1Bounds = playerOne.getBounds();
        Rectangle p2Bounds = playerTwo.getBounds();
        if (!p1Bounds.overlaps(p2Bounds)) {
            return;
        }

        float overlap = Math.min(p1Bounds.x + p1Bounds.width - p2Bounds.x, p2Bounds.x + p2Bounds.width - p1Bounds.x);
        if (playerOne.getCenterX() < playerTwo.getCenterX()) {
            playerOne.setX(Math.max(0f, playerOne.getX() - overlap / 2f));
            playerTwo.setX(Math.min(arena.getWidth() - playerTwo.getWidth(), playerTwo.getX() + overlap / 2f));
        } else {
            playerOne.setX(Math.min(arena.getWidth() - playerOne.getWidth(), playerOne.getX() + overlap / 2f));
            playerTwo.setX(Math.max(0f, playerTwo.getX() - overlap / 2f));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        arena.resize(viewport.getWorldWidth(), viewport.getWorldHeight());
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (arenaBackground != null) {
            arenaBackground.dispose();
        }
        if (playerOneSprite != null) {
            playerOneSprite.dispose();
        }
        if (playerTwoSprite != null) {
            playerTwoSprite.dispose();
        }
        shapeRenderer.dispose();
        font.dispose();
    }
}
