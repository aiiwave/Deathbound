package com.deathbound.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deathbound.DeathBoundGame;
import com.deathbound.config.GameSettings;
import com.deathbound.model.ArenaDefinition;
import com.deathbound.model.FighterDefinition;
import com.deathbound.ui.PixelBackground;

public class ArenaSelectScreen implements Screen {
    private static final float CARD_W = 390f;
    private static final float CARD_H = 128f;
    private static final float CARD_GAP = 24f;
    private static final float CARD_Y = 64f;
    private static final float THUMB_W = 160f;
    private static final float THUMB_H = 90f;
    private static final float CARD_TEXT_X = 190f;
    private static final float CARD_TEXT_RIGHT_PADDING = 16f;
    private static final float PREVIEW_W = 760f;
    private static final float PREVIEW_H = 427.5f;
    private static final float TITLE_Y = 330f;

    private final DeathBoundGame game;
    private final FightMode mode;
    private final FighterDefinition p1Def;
    private final FighterDefinition p2Def;
    private final GameSettings settings;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapes;
    private final PixelBackground screenBackground;
    private final PixelBackground[] arenaBackgrounds;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final BitmapFont smallFont;
    private final GlyphLayout layout;
    private final ArenaDefinition[] arenas;

    private int selected = 0;
    private boolean previewOpen = false;
    private float blinkTimer = 0f;
    private boolean blinkVisible = true;

    public ArenaSelectScreen(DeathBoundGame game, FightMode mode,
                             FighterDefinition p1Def, FighterDefinition p2Def) {
        this.game = game;
        this.mode = mode;
        this.p1Def = p1Def;
        this.p2Def = p2Def;
        this.settings = GameSettings.getInstance();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(settings.getVirtualWidth(), settings.getVirtualHeight(), camera);
        this.shapes = new ShapeRenderer();
        this.screenBackground = new PixelBackground(settings.getArenaBackgroundPath());

        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(3f);

        this.bodyFont = new BitmapFont();
        this.bodyFont.getData().setScale(1.55f);

        this.smallFont = new BitmapFont();
        this.smallFont.getData().setScale(1.1f);

        this.layout = new GlyphLayout();
        this.arenas = ArenaDefinition.values();
        this.arenaBackgrounds = new PixelBackground[arenas.length];
        for (int i = 0; i < arenas.length; i++) {
            if (arenas[i].hasImageBackground()) {
                arenaBackgrounds[i] = new PixelBackground(arenas[i].getBackgroundImagePath());
            }
        }

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        blinkTimer += delta;
        if (blinkTimer >= 0.5f) {
            blinkTimer = 0f;
            blinkVisible = !blinkVisible;
        }

        handleInput();
        draw();
    }

    private void handleInput() {
        boolean confirm = Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        if (previewOpen) {
            if (confirm) {
                startFight();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                previewOpen = false;
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.A)
            || Gdx.input.isKeyJustPressed(Input.Keys.W)
            || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selected--;
            if (selected < 0) {
                selected = arenas.length - 1;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.D)
            || Gdx.input.isKeyJustPressed(Input.Keys.S)
            || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)
            || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selected++;
            if (selected >= arenas.length) {
                selected = 0;
            }
        }

        if (confirm) {
            previewOpen = true;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.changeScreen(new CharacterSelectScreen(game, mode));
        }
    }

    private void startFight() {
        game.changeScreen(new FightScreen(game, mode, p1Def, p2Def, arenas[selected]));
    }

    private void draw() {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        game.batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0.05f, 0.04f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        screenBackground.draw(game.batch, width, height);
        game.batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawArenaCards(width);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.batch.begin();
        drawTitle(width);
        drawCardImages(width);
        drawCardText(width);
        game.batch.end();

        if (previewOpen) {
            drawPreviewModal(width);
        }
    }

    private void drawArenaCards(float width) {
        float startX = getCardsStartX(width);

        for (int i = 0; i < arenas.length; i++) {
            float x = startX + i * (CARD_W + CARD_GAP);
            boolean isSelected = i == selected;
            ArenaDefinition arena = arenas[i];
            Color accent = new Color(arena.getAccentColor().r, arena.getAccentColor().g, arena.getAccentColor().b,
                isSelected ? 0.92f : 0.58f);

            shapes.setColor(isSelected ? new Color(0.16f, 0.11f, 0.08f, 0.42f)
                                       : new Color(0.055f, 0.05f, 0.07f, 0.24f));
            shapes.rect(x, CARD_Y, CARD_W, CARD_H);

            shapes.setColor(accent);
            shapes.rect(x, CARD_Y, CARD_W, 3f);
            shapes.rect(x, CARD_Y + CARD_H - 3f, CARD_W, 3f);
            shapes.rect(x, CARD_Y, 3f, CARD_H);
            shapes.rect(x + CARD_W - 3f, CARD_Y, 3f, CARD_H);

            if (isSelected && blinkVisible) {
                shapes.rect(x + 8f, CARD_Y + CARD_H - 10f, CARD_W - 16f, 4f);
            }

        }
    }

    private void drawCardImages(float width) {
        float startX = getCardsStartX(width);

        for (int i = 0; i < arenas.length; i++) {
            PixelBackground background = arenaBackgrounds[i];
            if (background == null) {
                continue;
            }

            float x = startX + i * (CARD_W + CARD_GAP) + 18f;
            float y = CARD_Y + 19f;
            background.draw(game.batch, x, y, THUMB_W, THUMB_H);
        }
    }

    private void drawCardText(float width) {
        float startX = getCardsStartX(width);

        for (int i = 0; i < arenas.length; i++) {
            float x = startX + i * (CARD_W + CARD_GAP);
            ArenaDefinition arena = arenas[i];
            boolean isSelected = i == selected;
            float textX = x + CARD_TEXT_X;
            float textMaxW = CARD_W - CARD_TEXT_X - CARD_TEXT_RIGHT_PADDING;

            drawFittedText(bodyFont, arena.getDisplayName(), textX, CARD_Y + 88f, textMaxW,
                isSelected ? arena.getAccentColor() : Color.WHITE);

            drawFittedText(smallFont, arena.getSubtitle(), textX, CARD_Y + 61f, textMaxW,
                isSelected ? Color.LIGHT_GRAY : new Color(0.62f, 0.62f, 0.65f, 1f));

        }
    }

    private void drawPreviewModal(float width) {
        ArenaDefinition arena = arenas[selected];
        PixelBackground background = arenaBackgrounds[selected];
        float x = (width - PREVIEW_W) / 2f;
        float y = 72f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.02f, 0.018f, 0.025f, 1f);
        shapes.rect(0f, 0f, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapes.setColor(0.055f, 0.05f, 0.07f, 1f);
        shapes.rect(x - 14f, y - 46f, PREVIEW_W + 28f, PREVIEW_H + 72f);
        shapes.setColor(arena.getAccentColor());
        shapes.rect(x - 14f, y - 46f, PREVIEW_W + 28f, 4f);
        shapes.rect(x - 14f, y + PREVIEW_H + 22f, PREVIEW_W + 28f, 4f);
        shapes.rect(x - 14f, y - 46f, 4f, PREVIEW_H + 72f);
        shapes.rect(x + PREVIEW_W + 10f, y - 46f, 4f, PREVIEW_H + 72f);
        shapes.end();

        game.batch.begin();
        if (background != null) {
            background.draw(game.batch, x, y, PREVIEW_W, PREVIEW_H);
        }
        bodyFont.setColor(arena.getAccentColor());
        drawCenteredText(bodyFont, arena.getDisplayName(), width / 2f, y - 12f);
        game.batch.end();
    }

    private void drawTitle(float width) {
        titleFont.setColor(Color.RED);
        String title = "SELECT ARENA";
        layout.setText(titleFont, title);
        titleFont.draw(game.batch, title, (width - layout.width) / 2f, TITLE_Y);
    }

    private void drawCenteredText(BitmapFont font, String text, float centerX, float y) {
        layout.setText(font, text);
        font.draw(game.batch, text, centerX - layout.width / 2f, y);
    }

    private void drawFittedText(BitmapFont font, String text, float x, float y, float maxWidth, Color color) {
        float originalScaleX = font.getData().scaleX;
        float originalScaleY = font.getData().scaleY;

        font.setColor(color);
        layout.setText(font, text);
        if (layout.width > maxWidth) {
            float scale = maxWidth / layout.width;
            font.getData().setScale(originalScaleX * scale, originalScaleY * scale);
        }

        font.draw(game.batch, text, x, y);
        font.getData().setScale(originalScaleX, originalScaleY);
    }

    private float getCardsStartX(float width) {
        float totalW = arenas.length * CARD_W + (arenas.length - 1) * CARD_GAP;
        return (width - totalW) / 2f;
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
        screenBackground.dispose();
        for (PixelBackground arenaBackground : arenaBackgrounds) {
            if (arenaBackground != null) {
                arenaBackground.dispose();
            }
        }
        shapes.dispose();
        titleFont.dispose();
        bodyFont.dispose();
        smallFont.dispose();
    }
}
