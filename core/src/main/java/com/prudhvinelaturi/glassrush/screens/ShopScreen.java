package com.prudhvinelaturi.glassrush.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.prudhvinelaturi.glassrush.GlassRushGame;
import com.prudhvinelaturi.glassrush.config.GameConfig;

public class ShopScreen implements Screen {
    private final GlassRushGame game;
    private final Stage stage;
    private Table content;

    public ShopScreen(GlassRushGame game) {
        this.game = game;
        this.stage = new Stage(new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT));
        rebuild();
    }

    private void rebuild() {
        stage.clear();
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label header = new Label("SHOP  -  Shards: " + game.profile.totalShards, game.skin);
        root.add(header).padTop(20f).padBottom(20f).row();

        content = new Table();
        buildSkinsSection();
        buildAmmoUpgradeSection();

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setScrollingDisabled(true, false);
        root.add(scrollPane).expand().fill().row();

        TextButton back = new TextButton("BACK", game.skin);
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
                dispose();
            }
        });
        root.add(back).width(200f).height(60f).padTop(20f).padBottom(20f);
    }

    private void buildSkinsSection() {
        content.add(new Label("SKINS", game.skin)).padBottom(10f).row();
        for (int i = 0; i < GameConfig.SKIN_IDS.length; i++) {
            String skinId = GameConfig.SKIN_IDS[i];
            int cost = GameConfig.SKIN_COST[i];
            boolean owned = game.profile.ownedSkins.contains(skinId);
            boolean selected = game.profile.selectedSkin.equals(skinId);

            Table row = new Table();
            row.add(new Label(skinId, game.skin)).width(140f);

            String label;
            if (selected) {
                label = "EQUIPPED";
            } else if (owned) {
                label = "EQUIP";
            } else {
                label = cost + " shards";
            }
            TextButton actionButton = new TextButton(label, game.skin);
            actionButton.setDisabled(selected);
            actionButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (selected) return;
                    if (owned) {
                        game.profile.selectedSkin = skinId;
                        game.saveManager.save(game.profile);
                        rebuild();
                        Gdx.input.setInputProcessor(stage);
                    } else if (game.profile.totalShards >= cost) {
                        game.profile.totalShards -= cost;
                        game.profile.ownedSkins.add(skinId);
                        game.profile.selectedSkin = skinId;
                        game.saveManager.save(game.profile);
                        rebuild();
                        Gdx.input.setInputProcessor(stage);
                    }
                }
            });
            row.add(actionButton).width(160f).height(50f);
            content.add(row).padBottom(10f).row();
        }
    }

    private void buildAmmoUpgradeSection() {
        content.add(new Label("STARTING AMMO", game.skin)).padTop(20f).padBottom(10f).row();
        int level = game.profile.ammoUpgradeLevel;
        Table row = new Table();
        row.add(new Label("Level " + level + " (+" + level + " ammo)", game.skin)).width(200f);

        if (level < GameConfig.AMMO_UPGRADE_MAX_LEVEL) {
            int cost = GameConfig.AMMO_UPGRADE_COST[level];
            TextButton upgrade = new TextButton("UPGRADE (" + cost + ")", game.skin);
            upgrade.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (game.profile.totalShards >= cost) {
                        game.profile.totalShards -= cost;
                        game.profile.ammoUpgradeLevel++;
                        game.saveManager.save(game.profile);
                        rebuild();
                        Gdx.input.setInputProcessor(stage);
                    }
                }
            });
            row.add(upgrade).width(160f).height(50f);
        } else {
            row.add(new Label("MAX", game.skin)).width(160f);
        }
        content.add(row).row();

        content.add(new Label("CONTINUE TOKENS: " + game.profile.continueTokens, game.skin)).padTop(20f).padBottom(10f).row();
        TextButton buyToken = new TextButton("BUY TOKEN (" + GameConfig.CONTINUE_TOKEN_COST + ")", game.skin);
        buyToken.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.profile.totalShards >= GameConfig.CONTINUE_TOKEN_COST) {
                    game.profile.totalShards -= GameConfig.CONTINUE_TOKEN_COST;
                    game.profile.continueTokens++;
                    game.saveManager.save(game.profile);
                    rebuild();
                    Gdx.input.setInputProcessor(stage);
                }
            }
        });
        content.add(buyToken).width(220f).height(50f).row();
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
