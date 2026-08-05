package com.prudhvinelaturi.glassrush.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.prudhvinelaturi.glassrush.GlassRushGame;
import com.prudhvinelaturi.glassrush.config.GameConfig;
import com.prudhvinelaturi.glassrush.entities.GlassPanel;
import com.prudhvinelaturi.glassrush.entities.Pickup;
import com.prudhvinelaturi.glassrush.entities.Projectile;
import com.prudhvinelaturi.glassrush.logic.GameEngine;
import com.prudhvinelaturi.glassrush.logic.GameEvent;
import com.prudhvinelaturi.glassrush.logic.RunState;

import java.util.List;

/**
 * The core gameplay loop: drag to steer, tap to fire. Glass panels scroll down from the
 * top; shatter the ones blocking your lane or dodge through the gap.
 */
public class GameScreen implements Screen {
    private static final float TAP_MAX_DRAG = 14f;

    private final GlassRushGame game;
    private final GameEngine engine;
    private final RunState state;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Vector3 touchWorld = new Vector3();

    private float touchDownX, touchDownY;
    private boolean dragging = false;

    private Stage overlayStage;
    private boolean showingContinuePrompt = false;

    public GameScreen(GlassRushGame game) {
        this.game = game;
        this.engine = new GameEngine();
        this.state = new RunState(game.profile);
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        camera.position.set(GameConfig.WORLD_WIDTH / 2f, GameConfig.WORLD_HEIGHT / 2f, 0f);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (showingContinuePrompt) return false;
                touchWorld.set(screenX, screenY, 0);
                viewport.unproject(touchWorld);
                touchDownX = touchWorld.x;
                touchDownY = touchWorld.y;
                dragging = false;
                engine.movePlayer(state, touchWorld.x);
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (showingContinuePrompt) return false;
                touchWorld.set(screenX, screenY, 0);
                viewport.unproject(touchWorld);
                if (Math.abs(touchWorld.x - touchDownX) + Math.abs(touchWorld.y - touchDownY) > TAP_MAX_DRAG) {
                    dragging = true;
                }
                engine.movePlayer(state, touchWorld.x);
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (showingContinuePrompt) return false;
                if (!dragging && state.alive) {
                    engine.fireProjectile(state);
                }
                dragging = false;
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        if (state.alive) {
            List<GameEvent> events = engine.update(state, delta);
            for (GameEvent e : events) {
                handleEvent(e);
            }
        }

        Gdx.gl.glClearColor(0.03f, 0.03f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        ShapeRenderer sr = game.shapeRenderer;
        sr.setProjectionMatrix(camera.combined);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        drawPanels(sr);
        drawPickups(sr);
        drawProjectiles(sr);
        drawPlayer(sr);
        sr.end();

        drawHud();

        if (showingContinuePrompt && overlayStage != null) {
            overlayStage.act(delta);
            overlayStage.draw();
        }
    }

    private void handleEvent(GameEvent e) {
        switch (e) {
            case PANEL_SHATTERED:
                game.sfxManager.play("shatter");
                break;
            case PLAYER_HIT:
                game.sfxManager.play("hit");
                break;
            case AMMO_PICKUP:
            case SHARD_PICKUP:
                game.sfxManager.play("pickup");
                break;
            case GAME_OVER:
                onGameOver();
                break;
            default:
                break;
        }
    }

    private void onGameOver() {
        boolean canContinue = !state.usedContinue
                && (game.profile.continueTokens > 0 || game.adManager.isRewardedAdReady());
        if (canContinue) {
            showContinuePrompt();
        } else {
            goToGameOverScreen();
        }
    }

    private void showContinuePrompt() {
        showingContinuePrompt = true;
        overlayStage = new Stage(new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(overlayStage);

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        overlayStage.addActor(table);

        table.add(new Label("Run over!", game.skin)).padBottom(30f).row();

        if (game.adManager.isRewardedAdReady()) {
            TextButton watchAd = new TextButton("WATCH AD TO CONTINUE", game.skin);
            watchAd.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.adManager.showRewardedAd(
                            () -> resumeAfterContinue(true),
                            GameScreen.this::goToGameOverScreen);
                }
            });
            table.add(watchAd).width(280f).height(60f).padBottom(16f).row();
        }

        if (game.profile.continueTokens > 0) {
            TextButton useToken = new TextButton("USE CONTINUE TOKEN (" + game.profile.continueTokens + ")", game.skin);
            useToken.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    resumeAfterContinue(false);
                }
            });
            table.add(useToken).width(280f).height(60f).padBottom(16f).row();
        }

        TextButton noThanks = new TextButton("END RUN", game.skin);
        noThanks.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                goToGameOverScreen();
            }
        });
        table.add(noThanks).width(280f).height(50f).row();
    }

    private void resumeAfterContinue(boolean viaAd) {
        engine.continueRun(state, game.profile, viaAd);
        showingContinuePrompt = false;
        overlayStage.dispose();
        overlayStage = null;
        show();
    }

    private void goToGameOverScreen() {
        game.profile.totalShards += state.shardsThisRun;
        if (state.score > game.profile.bestScore) game.profile.bestScore = state.score;
        if (state.distanceMeters > game.profile.bestDistanceMeters) game.profile.bestDistanceMeters = state.distanceMeters;
        game.saveManager.save(game.profile);
        game.setScreen(new GameOverScreen(game, state));
        dispose();
    }

    private void drawPanels(ShapeRenderer sr) {
        sr.setColor(0.1f, 0.6f, 0.75f, 1f);
        for (GlassPanel p : state.panels) {
            if (p.broken) continue;
            if (p.gapX > 0) {
                sr.rect(0, p.y - GameConfig.PANEL_HEIGHT / 2f, p.gapX, GameConfig.PANEL_HEIGHT);
            }
            float rightStart = p.gapX + p.gapWidth;
            if (rightStart < GameConfig.WORLD_WIDTH) {
                sr.rect(rightStart, p.y - GameConfig.PANEL_HEIGHT / 2f, GameConfig.WORLD_WIDTH - rightStart, GameConfig.PANEL_HEIGHT);
            }
        }
    }

    private void drawPickups(ShapeRenderer sr) {
        for (Pickup pk : state.pickups) {
            if (pk.collected) continue;
            if (pk.type == Pickup.Type.AMMO) {
                sr.setColor(Color.YELLOW);
            } else {
                sr.setColor(Color.MAGENTA);
            }
            sr.circle(pk.x, pk.y, GameConfig.PICKUP_RADIUS);
        }
    }

    private void drawProjectiles(ShapeRenderer sr) {
        sr.setColor(Color.WHITE);
        for (Projectile p : state.projectiles) {
            sr.circle(p.x, p.y, GameConfig.PROJECTILE_RADIUS);
        }
    }

    private void drawPlayer(ShapeRenderer sr) {
        sr.setColor(Color.CYAN);
        sr.circle(state.player.x, state.player.y, state.player.radius);
    }

    private void drawHud() {
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.font.draw(game.batch, "Score " + state.score, 16, GameConfig.WORLD_HEIGHT - 16);
        game.font.draw(game.batch, "Ammo " + state.ammo, 16, GameConfig.WORLD_HEIGHT - 40);
        game.font.draw(game.batch, "Shards " + state.shardsThisRun, 16, GameConfig.WORLD_HEIGHT - 64);
        if (state.comboMultiplier > 1.01f) {
            game.font.draw(game.batch, String.format("Combo x%.1f", state.comboMultiplier), 16, GameConfig.WORLD_HEIGHT - 88);
        }
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (overlayStage != null) overlayStage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        if (overlayStage != null) overlayStage.dispose();
    }
}
