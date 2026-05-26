package com.deathbound.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deathbound.DeathBoundGame;
import com.deathbound.config.GameSettings;
import com.deathbound.model.FighterDefinition;
import com.deathbound.ui.PixelBackground;
import com.deathbound.screens.ArenaSelectScreen;

public class CharacterSelectScreen implements Screen {
    private static final float TITLE_Y = 380f;

    private enum SelectStep {
        PLAYER,
        OPPONENT
    }

    private final DeathBoundGame game;
    private final FightMode mode;
    private final GameSettings settings;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;
    private final PixelBackground background;
    private final BitmapFont titleFont;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final FighterDefinition[] fighters;
    private final Texture[] fighterSprites;

    private int playerOneSelection;
    private int opponentSelection = 1;
    private SelectStep step = SelectStep.PLAYER;

    public CharacterSelectScreen(DeathBoundGame game) {
        this(game, FightMode.PLAYER_VS_PLAYER);
    }

    public CharacterSelectScreen(DeathBoundGame game, FightMode mode) {
        this.game = game;
        this.mode = mode;
        this.settings = GameSettings.getInstance();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(settings.getVirtualWidth(), settings.getVirtualHeight(), camera);
        this.shapeRenderer = new ShapeRenderer();
        this.background = new PixelBackground(settings.getArenaBackgroundPath());
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(3f);
        this.font = new BitmapFont();
        this.font.getData().setScale(1.35f);
        this.layout = new GlyphLayout();
        this.fighters = FighterDefinition.values();
        this.fighterSprites = new Texture[fighters.length];
        for (int i = 0; i < fighters.length; i++) {
            if (fighters[i].hasSprite()) {
                fighterSprites[i] = new Texture(Gdx.files.internal(fighters[i].getSpritePath()));
                fighterSprites[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
        }
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        if (update()) {
            return;
        }
        draw();
    }

    private boolean update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.changeScreen(new MainMenuScreen(game));
            return true;
        }

        if (mode == FightMode.PLAYER_VS_BOT) {
            return updatePlayerVsBotSelection();
        }

        return updatePlayerVsPlayerSelection();
    }

    private boolean updatePlayerVsPlayerSelection() {
        if (step == SelectStep.PLAYER) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                playerOneSelection = previousIndex(playerOneSelection);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                playerOneSelection = nextIndex(playerOneSelection);
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                step = SelectStep.OPPONENT;
            }
            return false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            opponentSelection = previousIndex(opponentSelection);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            opponentSelection = nextIndex(opponentSelection);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.changeScreen(new ArenaSelectScreen(game, FightMode.PLAYER_VS_PLAYER,
                fighters[playerOneSelection], fighters[opponentSelection]));
            return true;
        }
        return false;
    }

    private boolean updatePlayerVsBotSelection() {
        if (step == SelectStep.PLAYER) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                playerOneSelection = previousIndex(playerOneSelection);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                playerOneSelection = nextIndex(playerOneSelection);
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (opponentSelection == playerOneSelection) {
                    opponentSelection = nextIndex(opponentSelection);
                }
                step = SelectStep.OPPONENT;
            }
            return false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            opponentSelection = previousOpponentIndex();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            opponentSelection = nextOpponentIndex();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            opponentSelection = randomOpponentIndex();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.changeScreen(new ArenaSelectScreen(game, FightMode.PLAYER_VS_BOT,
                fighters[playerOneSelection], fighters[opponentSelection]));
            return true;
        }
        return false;
    }

    private void draw() {
        viewport.apply();
        shapeRenderer.setProjectionMatrix(camera.combined);
        game.batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        background.draw(game.batch, getWorldWidth(), getWorldHeight());
        game.batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawSidePreview(58f, 118f, fighters[playerOneSelection], new Color(0.15f, 0.45f, 0.95f, 1f), playerOneSelection, isLeftPanelActive());
        drawSidePreview(getWorldWidth() - 218f, 118f, fighters[opponentSelection], new Color(0.9f, 0.18f, 0.18f, 1f), opponentSelection, isRightPanelActive());
        drawFighterGrid();
        shapeRenderer.end();

        game.batch.begin();
        drawFighterSprites();
        drawText();
        game.batch.end();
    }

    private void drawSidePreview(float x, float y, FighterDefinition fighter, Color accentColor, int selectedIndex, boolean active) {
        shapeRenderer.setColor(0.08f, 0.08f, 0.11f, 1f);
        shapeRenderer.rect(x - 18f, y - 24f, 178f, 252f);
        shapeRenderer.setColor(accentColor);
        shapeRenderer.rect(x - 18f, y + 214f, 178f, 8f);
        if (active) {
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(x - 24f, y + 228f, 190f, 6f);
            shapeRenderer.rect(x - 24f, y - 34f, 190f, 6f);
        }
        if (!fighter.hasSprite()) {
            shapeRenderer.setColor(fighter.getColor());
            shapeRenderer.rect(x + 48f, y + 54f, 54f, 116f);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(x + 88f, y + 142f, 8f, 8f);
        }
        drawSelectionPips(x - 4f, y - 8f, selectedIndex, accentColor);
    }

    private void drawFighterGrid() {
        float centerX = getWorldWidth() / 2f;
        float startX = centerX - 150f;
        float y = getWorldHeight() / 2f - 58f;

        for (int i = 0; i < fighters.length; i++) {
            float x = startX + i * 100f;
            shapeRenderer.setColor(0.1f, 0.1f, 0.14f, 1f);
            shapeRenderer.rect(x - 16f, y - 16f, 72f, 112f);

            if (i == playerOneSelection) {
                shapeRenderer.setColor(0.15f, 0.45f, 0.95f, 1f);
                shapeRenderer.rect(x - 22f, y + 98f, 84f, 6f);
            }
            if (i == opponentSelection) {
                shapeRenderer.setColor(0.9f, 0.18f, 0.18f, 1f);
                shapeRenderer.rect(x - 22f, y - 24f, 84f, 6f);
            }

            if (!fighters[i].hasSprite()) {
                shapeRenderer.setColor(fighters[i].getColor());
                shapeRenderer.rect(x, y, 40f, 78f);
                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.rect(x + 28f, y + 58f, 7f, 7f);
            }
        }
    }

    private void drawFighterSprites() {
        drawSideSprite(58f, 118f, fighterSprites[playerOneSelection], true);
        drawSideSprite(getWorldWidth() - 218f, 118f, fighterSprites[opponentSelection], false);

        float centerX = getWorldWidth() / 2f;
        float startX = centerX - 150f;
        float y = getWorldHeight() / 2f - 58f;
        for (int i = 0; i < fighters.length; i++) {
            Texture sprite = fighterSprites[i];
            if (sprite == null) {
                continue;
            }

            drawSpriteInBox(sprite, startX + i * 100f - 12f, y, 64f, 78f, true);
        }
    }

    private void drawSideSprite(float x, float y, Texture sprite, boolean facingRight) {
        if (sprite == null) {
            return;
        }

        drawSpriteInBox(sprite, x + 10f, y + 42f, 120f, 150f, facingRight);
    }

    private void drawSpriteInBox(Texture sprite, float x, float y, float maxWidth, float maxHeight, boolean facingRight) {
        float scale = Math.min(maxWidth / sprite.getWidth(), maxHeight / sprite.getHeight());
        float width = sprite.getWidth() * scale;
        float height = sprite.getHeight() * scale;
        float drawX = x + (maxWidth - width) / 2f;
        boolean flipX = !facingRight;

        game.batch.draw(sprite, drawX, y, width, height,
            0, 0, sprite.getWidth(), sprite.getHeight(), flipX, false);
    }

    private void drawSelectionPips(float x, float y, int selectedIndex, Color accentColor) {
        for (int i = 0; i < fighters.length; i++) {
            shapeRenderer.setColor(i == selectedIndex ? accentColor : new Color(0.22f, 0.22f, 0.25f, 1f));
            shapeRenderer.circle(x + i * 22f, y, 6f);
        }
    }

    private void drawText() {
        titleFont.setColor(Color.RED);
        drawCenteredText(titleFont, getTitleText(), getWorldWidth() / 2f, TITLE_Y);

        font.setColor(Color.WHITE);

        drawCenteredText(font, "P1", 129f, 156f);
        drawCenteredText(font, fighters[playerOneSelection].getDisplayName(), 129f, 128f);
        drawCenteredText(font, mode == FightMode.PLAYER_VS_BOT ? "BOT" : "P2", getWorldWidth() - 129f, 156f);
        drawCenteredText(font, fighters[opponentSelection].getDisplayName(), getWorldWidth() - 129f, 128f);

        float centerX = getWorldWidth() / 2f;
        float startX = centerX - 150f;
        float y = getWorldHeight() / 2f - 88f;
        for (int i = 0; i < fighters.length; i++) {
            drawCenteredText(font, fighters[i].getDisplayName(), startX + i * 100f + 20f, y);
        }
    }

    private void drawCenteredText(BitmapFont targetFont, String text, float centerX, float y) {
        layout.setText(targetFont, text);
        targetFont.draw(game.batch, text, centerX - layout.width / 2f, y);
    }

    private int previousIndex(int index) {
        return index == 0 ? fighters.length - 1 : index - 1;
    }

    private int nextIndex(int index) {
        return (index + 1) % fighters.length;
    }

    private int previousOpponentIndex() {
        int index = opponentSelection;
        do {
            index = previousIndex(index);
        } while (index == playerOneSelection);
        return index;
    }

    private int nextOpponentIndex() {
        int index = opponentSelection;
        do {
            index = nextIndex(index);
        } while (index == playerOneSelection);
        return index;
    }

    private int randomOpponentIndex() {
        int index;
        do {
            index = MathUtils.random(fighters.length - 1);
        } while (index == playerOneSelection);
        return index;
    }

    private boolean isLeftPanelActive() {
        return step == SelectStep.PLAYER;
    }

    private boolean isRightPanelActive() {
        return step == SelectStep.OPPONENT;
    }

    private String getTitleText() {
        if (mode == FightMode.PLAYER_VS_PLAYER) {
            return step == SelectStep.PLAYER ? "PLAYER 1 SELECT" : "PLAYER 2 SELECT";
        }
        return step == SelectStep.PLAYER ? "SELECT YOUR FIGHTER" : "SELECT OPPONENT";
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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
        background.dispose();
        for (Texture fighterSprite : fighterSprites) {
            if (fighterSprite != null) {
                fighterSprite.dispose();
            }
        }
        shapeRenderer.dispose();
        titleFont.dispose();
        font.dispose();
    }

    private float getWorldWidth() {
        return viewport.getWorldWidth();
    }

    private float getWorldHeight() {
        return viewport.getWorldHeight();
    }
}
