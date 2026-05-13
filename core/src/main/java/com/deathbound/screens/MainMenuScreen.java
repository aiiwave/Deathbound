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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deathbound.DeathBoundGame;
import com.deathbound.config.GameSettings;
import com.deathbound.ui.PixelBackground;

public class MainMenuScreen implements Screen {
    private static final float MENU_CELL_W = 440f;
    private static final float MENU_CELL_H = 54f;
    private static final float MENU_CELL_STEP = 66f;
    private static final float MODE_TITLE_Y = 330f;

    private final DeathBoundGame game;
    private final GameSettings settings;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final PixelBackground background;
    private final ShapeRenderer shapeRenderer;
    private final Vector2 mousePosition;
    private final GlyphLayout layout;
    private final BitmapFont fontTitle;
    private final BitmapFont fontMenu;

    private enum MenuState {
        MAIN, PLAY_MODE
    }

    private final String[] mainOptions = {"PLAY", "OPTIONS", "EXIT"};
    private final String[] playOptions = {"1 PLAYER (VS BOT)", "2 PLAYERS", "BACK"};

    private MenuState currentState = MenuState.MAIN;
    private int currentSelection = 0;

    public MainMenuScreen(DeathBoundGame game) {
        this.game = game;
        this.settings = GameSettings.getInstance();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(settings.getVirtualWidth(), settings.getVirtualHeight(), camera);
        this.background = new PixelBackground(settings.getArenaBackgroundPath());
        this.shapeRenderer = new ShapeRenderer();
        this.mousePosition = new Vector2();
        this.layout = new GlyphLayout();

        this.fontTitle = new BitmapFont();
        this.fontTitle.getData().setScale(3f);

        this.fontMenu = new BitmapFont();
        this.fontMenu.getData().setScale(2f);

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        handleInput();

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        background.draw(game.batch, getWorldWidth(), getWorldHeight());
        game.batch.end();

        String[] currentOptions = getCurrentOptions();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawMenuCells(currentOptions);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.batch.begin();
        drawMenuTitle();
        drawMenuText(currentOptions);
        game.batch.end();
    }

    private void drawMenuCells(String[] currentOptions) {
        for (int i = 0; i < currentOptions.length; i++) {
            float x = getMenuCellX();
            float y = getMenuCellY(i);
            boolean selected = i == currentSelection;
            Color accent = selected ? new Color(1f, 1f, 0f, 0.92f) : new Color(0.73f, 0.52f, 0.22f, 0.62f);

            shapeRenderer.setColor(selected ? new Color(0.18f, 0.12f, 0.08f, 0.42f)
                                            : new Color(0.055f, 0.05f, 0.07f, 0.26f));
            shapeRenderer.rect(x, y, MENU_CELL_W, MENU_CELL_H);

            shapeRenderer.setColor(accent);
            shapeRenderer.rect(x, y, MENU_CELL_W, 3f);
            shapeRenderer.rect(x, y + MENU_CELL_H - 3f, MENU_CELL_W, 3f);
            shapeRenderer.rect(x, y, 3f, MENU_CELL_H);
            shapeRenderer.rect(x + MENU_CELL_W - 3f, y, 3f, MENU_CELL_H);

        }
    }

    private void drawMenuTitle() {
        if (currentState == MenuState.MAIN) {
            return;
        }

        fontTitle.setColor(Color.RED);
        String titleText = "SELECT MODE";
        layout.setText(fontTitle, titleText);
        fontTitle.draw(game.batch, titleText, (getWorldWidth() - layout.width) / 2f, MODE_TITLE_Y);
    }

    private void drawMenuText(String[] currentOptions) {
        for (int i = 0; i < currentOptions.length; i++) {
            fontMenu.setColor(i == currentSelection ? Color.YELLOW : Color.WHITE);
            String text = currentOptions[i];
            layout.setText(fontMenu, text);
            float x = getMenuCellX() + (MENU_CELL_W - layout.width) / 2f;
            float y = getMenuCellY(i) + (MENU_CELL_H + layout.height) / 2f;
            fontMenu.draw(game.batch, text, x, y);
        }
    }

    private void handleInput() {
        handleKeyboardInput();
        handleMouseInput();
    }

    private void handleKeyboardInput() {
        String[] currentOptions = getCurrentOptions();

        if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            currentSelection--;
            if (currentSelection < 0) {
                currentSelection = currentOptions.length - 1;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            currentSelection++;
            if (currentSelection >= currentOptions.length) {
                currentSelection = 0;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            selectOption();
        }
    }

    private void handleMouseInput() {
        String[] currentOptions = getCurrentOptions();
        mousePosition.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePosition);

        boolean mouseMoved = Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0;

        for (int i = 0; i < currentOptions.length; i++) {
            float x = getMenuCellX();
            float y = getMenuCellY(i);
            boolean hovered = mousePosition.x >= x
                && mousePosition.x <= x + MENU_CELL_W
                && mousePosition.y >= y
                && mousePosition.y <= y + MENU_CELL_H;

            if (hovered) {
                if (mouseMoved) {
                    currentSelection = i;
                }
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    currentSelection = i;
                    selectOption();
                }
                return;
            }
        }
    }

    private void selectOption() {
        if (currentState == MenuState.MAIN) {
            if (currentSelection == 0) {
                currentState = MenuState.PLAY_MODE;
                currentSelection = 0;
            } else if (currentSelection == 1) {
                System.out.println("Settings opened (not implemented yet)");
            } else if (currentSelection == 2) {
                Gdx.app.exit();
            }
            return;
        }

        if (currentSelection == 0) {
            game.changeScreen(new CharacterSelectScreen(game, FightMode.PLAYER_VS_BOT));
        } else if (currentSelection == 1) {
            game.changeScreen(new CharacterSelectScreen(game, FightMode.PLAYER_VS_PLAYER));
        } else if (currentSelection == 2) {
            currentState = MenuState.MAIN;
            currentSelection = 0;
        }
    }

    private String[] getCurrentOptions() {
        return currentState == MenuState.MAIN ? mainOptions : playOptions;
    }

    private float getMenuCellX() {
        return (getWorldWidth() - MENU_CELL_W) / 2f;
    }

    private float getMenuCellY(int index) {
        float firstY = currentState == MenuState.MAIN ? 190f : 220f;
        return firstY - index * MENU_CELL_STEP;
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
        shapeRenderer.dispose();
        fontTitle.dispose();
        fontMenu.dispose();
    }

    private float getWorldWidth() {
        return viewport.getWorldWidth();
    }

    private float getWorldHeight() {
        return viewport.getWorldHeight();
    }
}
