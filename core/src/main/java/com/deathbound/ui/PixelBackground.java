package com.deathbound.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PixelBackground {
    private final Texture texture;

    public PixelBackground(String path) {
        texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
    }

    public void draw(SpriteBatch batch, float width, float height) {
        draw(batch, 0f, 0f, width, height);
    }

    public void draw(SpriteBatch batch, float x, float y, float width, float height) {
        float targetRatio = width / height;
        float sourceRatio = texture.getWidth() / (float) texture.getHeight();

        int sourceX = 0;
        int sourceY = 0;
        int sourceW = texture.getWidth();
        int sourceH = texture.getHeight();

        if (sourceRatio > targetRatio) {
            sourceW = Math.round(sourceH * targetRatio);
            sourceX = (texture.getWidth() - sourceW) / 2;
        } else if (sourceRatio < targetRatio) {
            sourceH = Math.round(sourceW / targetRatio);
            sourceY = (texture.getHeight() - sourceH) / 2;
        }

        batch.draw(texture, x, y, width, height, sourceX, sourceY, sourceW, sourceH, false, false);
    }

    public void dispose() {
        texture.dispose();
    }
}
