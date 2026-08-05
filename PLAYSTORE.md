# Play Store Launch Kit — GlassRush

Everything needed to ship. Build is produced locally via
`./gradlew android:bundleRelease` (see [ARCHITECTURE.md](ARCHITECTURE.md)); the console
steps below are done at [play.google.com/console](https://play.google.com/console) with
`prudhvinelaturi29@gmail.com` (same account as NexusHub and ChaiTapriTycoon).

## ⚠️ The path to production (same as NexusHub / ChaiTapriTycoon)

Personal dev accounts must pass a closed test **per app** before production:

1. **Create app** in Play Console → upload the `.aab` to **Internal testing** (instant,
   no review wait) → sanity-check on a real device.
2. Promote to **Closed testing** → recruit **12 testers** who stay opted in **14
   consecutive days**. (Reuse the tester group from NexusHub/ChaiTapriTycoon.)
3. After 14 days → **Apply for production** → full rollout.

## App details

| Field | Value |
|---|---|
| App name | `GlassRush` |
| Package | `com.prudhvinelaturi.glassrush` |
| Default language | English (India) — `en-IN` |
| App or game | Game |
| Category | Arcade |
| Free/paid | Free |
| Contains ads | **No** (none integrated yet — update this when AdMob lands, see TODO.md) |
| In-app purchases | No |
| Privacy policy URL | **TODO — pending GitHub repo + Pages setup** (same pattern as ChaiTapriTycoon's `prudhvi-nelaturi.github.io/chaiTapriTycoon/privacy.html`) |
| Contact email | `prudhvinelaturi29@gmail.com` |

## Store listing copy

**Short description** (70/80 chars):
> Drag to dodge, tap to shatter. How far can you smash?

**Full description:**

> 💎 One marble. A wall of glass. Nowhere to go but through.
>
> GlassRush is a fast, one-hand arcade smasher — drag to steer through gaps in
> oncoming glass panels, tap to fire a ball and shatter the glass blocking your way.
> Speed builds. Gaps narrow. How far can you push it?
>
> 🔫 SHATTER OR DODGE
> Every panel is a choice: thread the gap safely, or shoot to shatter it for bonus
> shards and a growing combo multiplier. Dodge and your combo resets — shoot and it
> climbs. Play it safe, or play for the high score.
>
> ⚡ IT NEVER SLOWS DOWN
> The further you go, the faster it gets and the narrower the gaps get. One hit on
> unbroken glass ends your run — unless you've got a continue token saved up.
>
> 💠 COLLECT & UPGRADE
> Grab ammo and shard pickups mid-run. Spend shards in the shop on new ball skins,
> a bigger starting ammo count, or continue tokens to keep a great run alive.
>
> ✨ FREE & OFFLINE
> No account. No internet needed. Just you, the glass, and how far you can push it.
>
> How far can you make it before the glass wins?

## Graphics checklist

| Asset | Spec | File |
|---|---|---|
| App icon | 512×512 PNG | `store/icon-512.png` |
| Feature graphic | 1024×500 PNG | `store/feature-graphic.png` |
| Phone screenshots | ≥2, PNG/JPG, 16:9 or 9:16 | `store/screenshot-*.png` (stylized mockups — swap for real gameplay captures once tested on a real device, see TODO.md) |

## Console questionnaires

**Content rating (IARC):** questionnaire → Game. Answer **No** to everything: no
violence (shattering glass panels, not characters), no sexuality, no language, no
controlled substances, no gambling (shards are earned in-game only, can't be cashed
out), no user interaction/UGC, no data sharing, no location. Expected rating:
Everyone / 3+.

**Data safety:** "Does your app collect or share any of the required user data types?"
→ **No**. All progress is stored locally on-device via LibGDX `Preferences`; no
analytics, no ads, no accounts. (Matches the privacy policy. Update BOTH when
ads/analytics land.)

**Target audience:** 13+ (do NOT tick under-13 — opts into the Families program and
its extra requirements).

**App access:** All functionality is available without special access (no login).

**Ads declaration:** No ads.

## Upload path

Console UI: Internal testing → Create release → upload the `.aab` from
`android/build/outputs/bundle/release/android-release.aab` → roll out.

## Release notes (v1.0.0)

> First release! Drag to steer through glass panels, tap to shatter the ones blocking
> your way, and see how far you can push your combo before the glass wins.

## Post-launch reminders

- When AdMob is added: flip "Contains ads" to Yes, update data safety + privacy policy.
- Play Console → Statistics is the retention dashboard: **D7 > ~10% = keep pushing;
  below = tune or move to the next portfolio experiment.**
- versionCode must be bumped manually per release (`android/build.gradle`
  `defaultConfig.versionCode`) — no CI auto-increment set up for this project.
