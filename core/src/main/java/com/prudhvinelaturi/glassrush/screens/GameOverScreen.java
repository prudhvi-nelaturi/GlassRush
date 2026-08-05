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
import com.prudhvinelaturi.glassrush.logic.RunState;

public class GameOverScreen implements Screen {
    private final GlassRushGame game;
    private final Stage stage;

    public GameOverScreen(GlassRushGame game, RunState finishedRun) {
        this.game = game;
        this.stage = new Stage(new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT));

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        Label title = new Label("RUN OVER", game.skin);
        title.setFontScale(1.6f);
        table.add(title).padBottom(30f).row();

        table.add(new Label("Score: " + finishedRun.score, game.skin)).padBottom(8f).row();
        table.add(new Label("Shards earned: " + finishedRun.shardsThisRun, game.skin)).padBottom(8f).row();
        table.add(new Label("Total shards: " + game.profile.totalShards, game.skin)).padBottom(40f).row();

        TextButton retry = new TextButton("RETRY", game.skin);
        retry.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });
        table.add(retry).width(220f).height(70f).padBottom(20f).row();

        TextButton menu = new TextButton("MENU", game.skin);
        menu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
                dispose();
            }
        });
        table.add(menu).width(220f).height(60f).row();
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
