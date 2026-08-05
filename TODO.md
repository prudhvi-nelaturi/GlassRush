# TODO — GlassRush 💎

Status: **playable core loop.** Steer/shoot/dodge/shatter, difficulty ramp, scoring,
combo, shop (skins/ammo upgrade/continue tokens), local save/load, and a full screen
flow (Splash → Menu → Game → GameOver, Shop, Settings) all work end-to-end. What's
left, roughly in priority order.

---

## 🔴 Before any real launch (the stubs)

- [ ] **Rewarded ads (AdMob).** `core/ads/NoOpAdManager` currently always reports "not
      ready" so the continue-via-ad path never triggers. Needs Prudhvi's own AdMob
      account + a GlassRush app entry + real ad unit IDs, then an
      `AndroidAdManager implements AdManager` wired into `AndroidLauncher`.
- [ ] **In-app purchases.** `core/billing/NoOpBillingManager` — no real SKUs. Needs at
      least one in-app product configured in Play Console (e.g. a shard pack or
      "remove ads" if ads get added), then a Play Billing-backed implementation.
- [ ] **Real device testing.** All testing so far is via the LWJGL3 desktop launcher
      (no Android emulator installed in the dev environment) — confirm touch feel,
      performance, and screen-size scaling on an actual phone before shipping.

## 🟠 Retention (highest-impact next feature)

- [ ] **Push notifications** for the free-continue-token-ready / daily-streak style
      hooks, if a daily reward loop gets added (currently there isn't one — GlassRush
      is purely a skill/score loop, unlike ChaiTapriTycoon's idle-income model).
- [ ] **Daily challenge or streak bonus** — GlassRush has no "come back tomorrow" hook
      yet beyond wanting to beat your best score. Consider a daily bonus-shard run or
      rotating challenge modifier.

## 🌐 Community & social (learn it here too, per the portfolio's shared playbook)

- [ ] **Leaderboard** (global + friends) — arcade high-score games live or die on this.
      Cheapest hook: Firebase/Supabase free tier (same choice ChaiTapriTycoon is
      weighing — pick one across the portfolio, don't hand-roll servers).
- [ ] **Weekly tournament / seasonal reset** — recurring reason to come back and compete.
- [ ] **Friend invites / referral** via WhatsApp share of your score/replay.

**Sequencing:** prove single-player retention first (D7 > ~10%) before building any
of the above — a leaderboard nobody checks is wasted effort.

## 🟡 Game design & balance

- [ ] **Tune the difficulty ramp.** `GameConfig.SPEED_RAMP_PER_METER`,
      `PANEL_SPAWN_RAMP_METERS`, `GAP_WIDTH_RAMP_METERS` all need real playtesting —
      current values are a reasonable first guess, not validated against real players.
- [ ] **More panel variety.** Currently every panel takes exactly 1 hit
      (`GameConfig.PANEL_MAX_HP`) — consider "reinforced" panels (2+ hits) later for
      variety, or moving/diagonal gaps.
- [ ] **Prestige / daily-best chase** loop for long-term retention (later).
- [ ] **Screen shake + hit-stop** on shatter/death for extra juice (easy win once the
      particle effects from `android/assets/particles/` are wired into `GameScreen`).

## 🟢 Polish

- [ ] **Wire the generated particle effects** (`android/assets/particles/*.p`) into
      `GameScreen` — play `shatter.p` on `PANEL_SHATTERED`, `pickup.p` on pickup
      events, `hit.p` on `PLAYER_HIT`. Files exist; the `ParticleEffect` load+play
      calls into `GameScreen.render`/`handleEvent` are the remaining wiring.
- [ ] **Real launcher icon / feature graphic polish pass** once there's a settled
      final visual style (current icon is a generated glass-shard motif via
      `scripts/gen_assets.py` — good enough to ship, but revisit after a few runs of
      actual gameplay footage exist for reference).
- [ ] **Capture real gameplay screenshots** for the Play Store listing once the game
      is playable on a real device — the current `store/screenshot-*.png` are
      stylized promotional mockups, not actual captures.

## 📋 Ship & measure

- [ ] Build signed release `.aab` (`./gradlew android:bundleRelease`).
- [ ] Play Console: Internal testing → Closed testing (12 testers / 14 days, same
      per-app requirement as NexusHub and ChaiTapriTycoon) → Production. See
      [PLAYSTORE.md](PLAYSTORE.md).
- [ ] Drive ~100-300 installs from Reels/WhatsApp.
- [ ] Watch retention — **D7 is the go/no-go**: >~10% = tune & push, <~10% = kill and
      try the next portfolio concept.

## ⚠️ Policy

- [ ] **Avoid real-money/wagering mechanics** — India restricted/banned online
      money-gaming (~2025). Shards are earned in-game only; verify current official
      rules before adding anything money-game-adjacent (e.g. cash-out mechanics).

---

See [ARCHITECTURE.md](ARCHITECTURE.md) for how the code is organized and
[README.md](README.md) for run instructions + the retention framework.
