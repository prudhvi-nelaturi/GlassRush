package com.prudhvinelaturi.glassrush.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.prudhvinelaturi.glassrush.GlassRushGame;
import com.prudhvinelaturi.glassrush.config.GameConfig;

public class SplashScreen implements Screen {
    private static final float DURATION = 1.1f;

    private final GlassRushGame game;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private float elapsed = 0f;

    public SplashScreen(GlassRushGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
    }

    @Override
    public void render(float delta) {
        elapsed += delta;
        Gdx.gl.glClearColor(0.04f, 0.04f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        GlyphLayout layout = new GlyphLayout(game.font, "GlassRush");
        game.font.getData().setScale(2.4f);
        layout.setText(game.font, "GlassRush", Color.CYAN, GameConfig.WORLD_WIDTH, com.badlogic.gdx.utils.Align.center, false);
        game.font.draw(game.batch, layout, 0, GameConfig.WORLD_HEIGHT / 2f + layout.height / 2f);
        game.font.getData().setScale(1f);
        game.batch.end();

        if (elapsed >= DURATION || Gdx.input.justTouched()) {
            game.setScreen(new MenuScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {}
}
