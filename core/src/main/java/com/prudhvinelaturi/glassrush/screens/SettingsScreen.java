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

public class SettingsScreen implements Screen {
    private final GlassRushGame game;
    private final Stage stage;
    private boolean confirmingReset = false;

    public SettingsScreen(GlassRushGame game) {
        this.game = game;
        this.stage = new Stage(new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT));
        rebuild();
    }

    private void rebuild() {
        stage.clear();
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        table.add(new Label("SETTINGS", game.skin)).padBottom(40f).row();

        TextButton sound = new TextButton(game.sfxManager.isMuted() ? "SOUND: OFF" : "SOUND: ON", game.skin);
        sound.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.sfxManager.setMuted(!game.sfxManager.isMuted());
                rebuild();
                Gdx.input.setInputProcessor(stage);
            }
        });
        table.add(sound).width(240f).height(60f).padBottom(20f).row();

        if (!confirmingReset) {
            TextButton reset = new TextButton("RESET SAVE", game.skin);
            reset.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    confirmingReset = true;
                    rebuild();
                    Gdx.input.setInputProcessor(stage);
                }
            });
            table.add(reset).width(240f).height(60f).padBottom(20f).row();
        } else {
            table.add(new Label("Erase all progress?", game.skin)).padBottom(10f).row();
            TextButton confirm = new TextButton("YES, ERASE", game.skin);
            confirm.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.saveManager.reset();
                    game.profile = game.saveManager.load();
                    confirmingReset = false;
                    rebuild();
                    Gdx.input.setInputProcessor(stage);
                }
            });
            table.add(confirm).width(240f).height(50f).padBottom(10f).row();

            TextButton cancel = new TextButton("CANCEL", game.skin);
            cancel.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    confirmingReset = false;
                    rebuild();
                    Gdx.input.setInputProcessor(stage);
                }
            });
            table.add(cancel).width(240f).height(50f).padBottom(20f).row();
        }

        TextButton back = new TextButton("BACK", game.skin);
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
                dispose();
            }
        });
        table.add(back).width(200f).height(50f).row();
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
