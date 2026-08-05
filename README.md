# GlassRush 💎

An endless vertical arcade smasher. Portfolio experiment #2 in the "earn money by
leveraging AI" / game-portfolio plan (experiment #1 is
[Chai Tapri Tycoon](https://github.com/prudhvi-nelaturi/chaiTapriTycoon), an idle-merge
game). Built in **Java on LibGDX** — different genre, different stack, same $0-hosting
philosophy.

## The loop

Drag to **steer** your marble through gaps in oncoming glass walls. Tap to **fire** a
ball and **shatter** glass blocking your lane. Shattering builds a **combo multiplier**
(more shards); dodging through a gap without shooting is safe but resets it. Distance
traveled = score. Speed and panel density ramp up the longer you survive. Hit unbroken
glass and it's game over — unless you spend a continue token or watch a rewarded ad.

Collect **shards** mid-run, spend them in the Shop on ball skins, starting-ammo
upgrades, and continue tokens.

## Run it

Fastest way to iterate — no Android emulator needed:

```bash
cd "GlassRush"
./gradlew desktop:run     # opens a desktop window; drag/click stands in for touch
```

Build + install to a connected Android device or emulator:

```bash
./gradlew android:installDebug
```

Run the unit test suite (pure game logic, no emulator needed):

```bash
./gradlew core:test
```

## Code map

- `core/config/GameConfig.java` — **all tuning lives here** (speeds, spawn rates,
  rewards, difficulty curve, shop costs). Balance the game by editing numbers, not logic.
- `core/logic/GameEngine.java` — pure game logic (collision, scoring, combo,
  difficulty ramp). No LibGDX rendering deps, no storage — unit-testable on a plain JVM.
- `core/logic/RunState.java` / `PlayerProfile.java` — transient per-run state vs.
  persistent cross-run save data.
- `core/entities/` — Player, GlassPanel, Projectile, Pickup.
- `core/screens/` — Splash, Menu, Game, GameOver, Shop, Settings (LibGDX `Screen`s).
- `core/save/SaveManager.java` — local save/load via LibGDX `Preferences`
  (SharedPreferences on Android). No backend, no accounts.
- `core/ads/`, `core/billing/` — `AdManager`/`BillingManager` interfaces + `NoOp`
  implementations. **Stubbed for v1** — see [TODO.md](TODO.md).
- `android/` — Android launcher module, manifest, icons, release signing config.
- `desktop/` — LWJGL3 desktop launcher, used for fast iteration/playtesting.
- `scripts/gen_assets.py` — generates launcher icons, store graphics, and screenshots
  procedurally (no external art dependencies).

## What's stubbed (wire before real launch)

See [TODO.md](TODO.md) for the full list — rewarded ads, IAP, and real device testing
are the big ones, same "flag before launch" discipline as ChaiTapriTycoon.

## The only metric that matters: retention

Ship to a Play Store closed track, drive installs from Reels/WhatsApp, watch the curve.
Same D1/D7/D30 retention bar as the rest of the portfolio — see
[ChaiTapriTycoon's README](https://github.com/prudhvi-nelaturi/chaiTapriTycoon) for the
target numbers. **If a few tuning passes can't get D7 over ~10%, kill it and try the
next concept** — that's the portfolio plan working, not failing.

## India policy note

No real-money/wagering mechanics — India restricted/banned online money-gaming
(~2025 legislation). Shards are earned in-game only, never purchasable for real money
directly in a way that resembles wagering; verify current official rules before
designing anything money-game-adjacent.
