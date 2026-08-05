# Architecture — GlassRush 💎

An endless vertical arcade smasher built in Java on LibGDX. Drag to steer through gaps
in oncoming glass panels, tap to shatter glass blocking the way. Built to be tuned by
editing numbers, not rewriting logic — same philosophy as ChaiTapriTycoon's `config.js`.

---

## Tech stack

| Layer | Choice | Why |
|---|---|---|
| Language | Java 17 (Gradle toolchain, isolated from the system's JDK 24 used by NexusHub) | Android Gradle Plugin requires JDK 17; Prudhvi's strongest language |
| Engine | LibGDX 1.13.1 | Mature, free, cross-platform 2D engine — handles the game loop, input, audio, rendering |
| Build | Gradle 8.10.2 wrapper + Android Gradle Plugin 8.7.3 | Standard multi-module Android/Java toolchain |
| Persistence | LibGDX `Preferences` | Local save; no backend needed for v1 (SharedPreferences on Android) |
| UI | Scene2D (`Stage`/`Table`/`TextButton`) with a runtime-built `Skin` | No external UI atlas/skin files — buttons are solid-color pixmaps generated in code |
| Desktop launcher | LWJGL3 | Fast iteration/playtesting without an Android emulator |

No backend, no accounts, no third-party ad/billing SDK wired yet (see stubs below).

---

## Module layout

```
GlassRush/
├── core/       shared game logic + rendering — platform-agnostic, the actual game
├── android/    Android launcher, manifest, icons, release signing config
├── desktop/    LWJGL3 desktop launcher for fast iteration
├── scripts/    gen_assets.py — procedural icon/feature-graphic/screenshot generator
└── store/      generated Play Store listing graphics
```

### `core/config/GameConfig.java` — the design surface
Every tunable number: virtual viewport size, scroll speed + its difficulty ramp, panel
spawn interval + gap width curves, ammo/pickup values, scoring, combo multiplier rules,
continue-token cost, shop prices. **To rebalance the game, edit this file — not the logic.**

### `core/logic/` — pure functions, no rendering
- `GameEngine.update(RunState, float delta)` — the entire per-frame simulation:
  difficulty ramp → spawn panels/pickups → move projectiles + resolve collisions →
  move panels + resolve player collision → move pickups + resolve collection. Returns
  a `List<GameEvent>` (`PANEL_SHATTERED`, `PANEL_DODGED`, `AMMO_PICKUP`, `SHARD_PICKUP`,
  `PLAYER_HIT`, `GAME_OVER`) so the rendering layer can trigger juice (particles/SFX)
  without the engine knowing anything about rendering.
- `GameEngine.movePlayer` / `fireProjectile` / `continueRun` — the three player actions.
- `RunState` — everything that exists for one run: player, panels, projectiles,
  pickups, distance, score, ammo, combo multiplier, alive/dead.
- `PlayerProfile` — persistent cross-run data: total shards, best score/distance,
  ammo upgrade level, continue tokens, owned/selected skins.
- Fully unit-testable on a plain JVM — no LibGDX `Gdx.*` static context required
  (see `core/src/test/java/...` and `./gradlew core:test`).

### `core/entities/`
`Player` (position + lerped drag-follow), `GlassPanel` (one gap, hp, broken/scored
flags, `blocksAt`/`gapContains` geometry helpers), `Projectile` (straight-up mover),
`Pickup` (ammo or shard, collected flag).

### `core/screens/`
Standard LibGDX `Game`/`Screen` pattern. Navigation convention: every screen takes
`GlassRushGame game` in its constructor and calls `game.setScreen(new XScreen(game))`
to transition — see `GlassRushGame.java`, the shared hub holding the `SpriteBatch`,
`ShapeRenderer`, `BitmapFont`, `Skin`, `SaveManager`, `SfxManager`, `AdManager`,
`BillingManager`, and the loaded `PlayerProfile`.

- `SplashScreen` → `MenuScreen` (auto after ~1.1s or on tap)
- `MenuScreen` → Play (`GameScreen`) / Shop (`ShopScreen`) / Settings (`SettingsScreen`)
- `GameScreen` — the core loop. Custom `InputAdapter` (not Scene2D) for precise
  drag-to-steer / tap-to-fire; drag distance under a threshold = "tap" = fire, otherwise
  it's steering. Renders via `ShapeRenderer` (panels/player/projectiles/pickups) +
  `SpriteBatch` (HUD text). On `GAME_OVER`, either shows a Scene2D continue-prompt
  overlay (if a token or rewarded ad is available) or advances to `GameOverScreen`.
- `GameOverScreen` — final score/shards, Retry or Menu.
- `ShopScreen` — skins (buy/equip) + ammo upgrade level + continue-token purchase,
  all spending `PlayerProfile.totalShards`, saved immediately via `SaveManager`.
- `SettingsScreen` — mute toggle, reset-save with a confirm step.

### `core/ui/UiFactory.java`
Builds a minimal Scene2D `Skin` at runtime from 1×1 solid-color pixmaps — no external
skin atlas/JSON needed for buttons/labels. Created once in `GlassRushGame.create()`,
disposed in `GlassRushGame.dispose()`.

### `core/save/SaveManager.java`
Loads/saves a flat set of `PlayerProfile` fields via LibGDX `Preferences`
(`glassrush-save`). `load()` always returns a valid profile (defaults if nothing saved
yet — `ownedSkins` always contains `"default"`). `reset()` clears everything.

### `core/ads/`, `core/billing/`
`AdManager`/`BillingManager` interfaces + `NoOpAdManager`/`NoOpBillingManager` — what
ships in v1. Real AdMob/Play Billing implementations are a launch-blocker TODO (need
Prudhvi's own AdMob account + a configured in-app product — can't be created on his
behalf). See [TODO.md](TODO.md).

### `core/audio/SfxManager.java`
Lazily loads short SFX from `sfx/<name>.ogg`; missing files are silently skipped
rather than crashing, so gameplay code can call `play("shatter")` etc. before real
audio assets exist.

---

## Core loop & data flow

```
touch input → GameEngine.movePlayer / fireProjectile (mutate RunState)
      ↓
render(delta) → GameEngine.update(state, delta) → List<GameEvent>
      ↓                                                ↓
draw panels/player/projectiles/pickups (ShapeRenderer)   trigger SFX/particles per event
      ↓
draw HUD (SpriteBatch + BitmapFont)
      ↓
GAME_OVER event → continue prompt (token/ad) or → GameOverScreen
                                                        ↓
                                    profile.totalShards/bestScore updated + saved
```

---

## Notable decisions

- **Combo resets on dodge, builds on shatter** — deliberately creates a risk/reward
  choice (safe dodge vs. risky-but-rewarding shooting), rather than combo just being
  "consecutive panels passed," which would be automatic and not a real decision.
- **Difficulty is fully config-driven** (`GameConfig.difficultyLerp` ramps in
  `GameEngine`) — speed, gap width, and spawn interval all lerp from a base value to a
  min/max over a configurable number of meters, then hold. Tune the climb by editing
  three ramp constants, not the engine.
- **One continue per run, either via token or rewarded ad** — matches the genre's
  standard monetization pattern (Smash Hit / Bus Escape) without requiring a live ad
  network for v1 (ads are stubbed; tokens are shard-purchasable so the loop works
  even before AdMob is wired in).
- **Scene2D `Skin` built at runtime from solid pixmaps** — avoids needing an external
  UI texture atlas for a v1 that's otherwise fully procedural (no sprite art).
- **JDK 17 pinned via `gradle.properties` `org.gradle.java.home`**, isolated from the
  machine's default JDK 24 (used by NexusHub/Spring Boot) — Android Gradle Plugin
  does not support JDK 24.

---

## Run & verify

```bash
./gradlew core:test          # pure-logic unit tests, no emulator needed
./gradlew desktop:run        # interactive playtest via LWJGL3 desktop window
./gradlew android:installDebug   # install to a connected device/emulator
```
