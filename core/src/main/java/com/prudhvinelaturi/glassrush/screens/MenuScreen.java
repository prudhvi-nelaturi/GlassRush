package com.prudhvinelaturi.glassrush.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.prudhvinelaturi.glassrush.GlassRushGame;
import com.prudhvinelaturi.glassrush.config.GameConfig;

public class MenuScreen implements Screen {
    private final GlassRushGame game;
    private final Stage stage;

    public MenuScreen(GlassRushGame game) {
        this.game = game;
        this.stage = new Stage(new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT));

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        Label title = new Label("GlassRush", game.skin);
        title.setFontScale(2.2f);
        table.add(title).padBottom(40f).row();

        Label best = new Label("Best: " + game.profile.bestScore + "   Shards: " + game.profile.totalShards, game.skin);
        table.add(best).padBottom(50f).row();

        TextButton playButton = new TextButton("PLAY", game.skin);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });
        table.add(playButton).width(220f).height(70f).padBottom(20f).row();

        TextButton shopButton = new TextButton("SHOP", game.skin);
        shopButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ShopScreen(game));
                dispose();
            }
        });
        table.add(shopButton).width(220f).height(60f).padBottom(20f).row();

        TextButton settingsButton = new TextButton("SETTINGS", game.skin);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game));
                dispose();
            }
        });
        table.add(settingsButton).width(220f).height(60f).row();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.04f, 0.04f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
