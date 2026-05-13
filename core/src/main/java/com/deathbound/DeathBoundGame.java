package com.deathbound;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.deathbound.screens.MainMenuScreen;
import java.util.ArrayList;
import java.util.List;

public class DeathBoundGame extends Game {
    public SpriteBatch batch;
    private final List<Screen> screensToDispose = new ArrayList<>();

    @Override
    public void create() {
        batch = new SpriteBatch();
        // Устанавливаем начальный экран (Главное меню)
        this.setScreen(new MainMenuScreen(this));
    }

    public void changeScreen(Screen nextScreen) {
        Screen currentScreen = getScreen();
        setScreen(nextScreen);
        if (currentScreen != null) {
            screensToDispose.add(currentScreen);
        }
    }

    @Override
    public void render() {
        // Обязательно вызываем render() родительского класса, 
        // чтобы он отрисовывал текущий активный Screen
        super.render();
        disposeOldScreens();
    }

    @Override
    public void dispose() {
        Screen currentScreen = getScreen();
        if (currentScreen != null) {
            currentScreen.dispose();
        }
        disposeOldScreens();
        batch.dispose();
    }

    private void disposeOldScreens() {
        for (Screen oldScreen : screensToDispose) {
            if (oldScreen != getScreen()) {
                oldScreen.dispose();
            }
        }
        screensToDispose.clear();
    }
}
