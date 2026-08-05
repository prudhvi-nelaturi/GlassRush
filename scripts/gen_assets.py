#!/usr/bin/env python3
"""
GlassRush asset generator — procedural icon/feature-graphic art + synthesized SFX.
Pure Python stdlib for image/audio synthesis (no PIL/numpy); shells out to `oggenc`
(vorbis-tools) to encode the final .ogg files. Re-run any time the art needs tweaking —
this is the design surface for visuals/audio, same philosophy as config/GameConfig.java
for gameplay balance.

Usage: python3 scripts/gen_assets.py
"""
import math
import os
import struct
import subprocess
import sys
import wave
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

BG = (7, 9, 18)
SHARD_LIGHT = (140, 245, 255)
SHARD_MID = (0, 200, 230)
SHARD_DARK = (0, 90, 130)


# ---------------------------------------------------------------------------
# PNG writer (stdlib-only: struct + zlib)
# ---------------------------------------------------------------------------

def write_png(path, w, h, pixel_fn):
    raw = bytearray()
    for y in range(h):
        raw.append(0)  # filter type 0 (none)
        for x in range(w):
            r, g, b, a = pixel_fn(x, y, w, h)
            raw += bytes((r, g, b, a))

    def chunk(tag, data):
        return (struct.pack('>I', len(data)) + tag + data +
                struct.pack('>I', zlib.crc32(tag + data) & 0xffffffff))

    sig = b'\x89PNG\r\n\x1a\n'
    ihdr = struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)
    idat = zlib.compress(bytes(raw), 9)
    png = sig + chunk(b'IHDR', ihdr) + chunk(b'IDAT', idat) + chunk(b'IEND', b'')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(png)


def lerp(a, b, t):
    return a + (b - a) * t


def lerp_color(c1, c2, t):
    return tuple(int(lerp(c1[i], c2[i], t)) for i in range(3))


def point_in_diamond(dx, dy, r, squash=0.72):
    """Diamond (rotated square) distance field: 0 at center, 1 at the edge."""
    return (abs(dx) + abs(dy) / squash) / r


def shard_icon_pixel(x, y, w, h):
    cx, cy = w / 2.0, h / 2.0
    dx, dy = x - cx, y - cy
    dist = math.sqrt(dx * dx + dy * dy)
    corner_dist = max(abs(dx), abs(dy))

    # rounded-square dark background
    edge = w * 0.5
    if corner_dist > edge - w * 0.06:
        # soften corners slightly
        pass

    r = w * 0.40
    d = point_in_diamond(dx, dy, r)

    if d < 1.0:
        # faceted highlight: angle-based banding gives a "cut gem" look
        angle = math.atan2(dy, dx)
        facet = (math.sin(angle * 3.0 + 0.6) + 1.0) / 2.0
        base = lerp_color(SHARD_MID, SHARD_DARK, d)
        lit = lerp_color(SHARD_LIGHT, base, 1.0 - facet * 0.5)
        t = min(1.0, d * 1.15)
        col = lerp_color(lit, base, t)
        # inner highlight near the top-left facet
        highlight_dx, highlight_dy = dx + r * 0.35, dy + r * 0.35
        hl = math.sqrt(highlight_dx ** 2 + highlight_dy ** 2)
        if hl < r * 0.35:
            col = lerp_color((255, 255, 255), col, hl / (r * 0.35))
        return (col[0], col[1], col[2], 255)

    # soft glow just outside the gem before hitting flat background
    glow = max(0.0, 1.0 - (d - 1.0) * 3.0)
    if glow > 0:
        col = lerp_color(BG, SHARD_DARK, glow * 0.5)
        return (col[0], col[1], col[2], 255)

    return (BG[0], BG[1], BG[2], 255)


def gen_icons():
    sizes = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
    res_dir = os.path.join(ROOT, "android", "res")
    for density, size in sizes.items():
        path = os.path.join(res_dir, f"mipmap-{density}", "ic_launcher.png")
        write_png(path, size, size, shard_icon_pixel)
        round_path = os.path.join(res_dir, f"mipmap-{density}", "ic_launcher_round.png")
        write_png(round_path, size, size, shard_icon_pixel)
        print(f"icon  {density:8s} {size}x{size}")

    store_path = os.path.join(ROOT, "store", "icon-512.png")
    write_png(store_path, 512, 512, shard_icon_pixel)
    print("icon  store    512x512")


def feature_graphic_pixel(x, y, w, h):
    t = y / h
    top = (5, 8, 20)
    bottom = (10, 30, 45)
    col = lerp_color(top, bottom, t)

    # a row of faint diagonal shards for texture
    band = (x * 0.6 + y) % 140
    if band < 3:
        col = lerp_color(col, SHARD_DARK, 0.35)

    # central gem motif, right-of-center
    cx, cy = w * 0.20, h * 0.5
    dx, dy = x - cx, y - cy
    r = h * 0.34
    d = point_in_diamond(dx, dy, r)
    if d < 1.0:
        angle = math.atan2(dy, dx)
        facet = (math.sin(angle * 3.0 + 0.6) + 1.0) / 2.0
        base = lerp_color(SHARD_MID, SHARD_DARK, d)
        lit = lerp_color(SHARD_LIGHT, base, 1.0 - facet * 0.5)
        col = lerp_color(lit, base, min(1.0, d * 1.15))

    return (col[0], col[1], col[2], 255)


def gen_feature_graphic():
    path = os.path.join(ROOT, "store", "feature-graphic.png")
    write_png(path, 1024, 500, feature_graphic_pixel)
    print("feature-graphic 1024x500")


# ---------------------------------------------------------------------------
# SFX synthesis (stdlib wave module -> oggenc)
# ---------------------------------------------------------------------------

SAMPLE_RATE = 44100


def synth_samples(duration, fn):
    n = int(SAMPLE_RATE * duration)
    samples = []
    for i in range(n):
        t = i / SAMPLE_RATE
        v = fn(t, i / n)
        v = max(-1.0, min(1.0, v))
        samples.append(int(v * 32000))
    return samples


def write_wav(path, samples):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with wave.open(path, 'w') as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(SAMPLE_RATE)
        f.writeframes(b''.join(struct.pack('<h', s) for s in samples))


def envelope_decay(progress, sharpness=6.0):
    return math.exp(-progress * sharpness)


def shatter_fn(t, progress):
    # bright noise burst with fast decay + a few high-frequency "tinkle" partials
    noise = (hash((int(t * SAMPLE_RATE), 1)) % 2000 / 1000.0 - 1.0)
    tinkle = 0.25 * math.sin(2 * math.pi * 3200 * t) + 0.2 * math.sin(2 * math.pi * 5200 * t)
    return (noise * 0.8 + tinkle) * envelope_decay(progress, 9.0)


def hit_fn(t, progress):
    thud = math.sin(2 * math.pi * 90 * t) * 0.8
    noise = (hash((int(t * SAMPLE_RATE), 2)) % 2000 / 1000.0 - 1.0) * 0.3
    return (thud + noise) * envelope_decay(progress, 7.0)


def pickup_fn(t, progress):
    freq = lerp(660, 990, min(1.0, progress * 2.2))
    tone = math.sin(2 * math.pi * freq * t)
    return tone * envelope_decay(progress, 4.5)


def gen_sfx():
    tmp_dir = os.path.join(ROOT, "build", "tmp-sfx")
    out_dir = os.path.join(ROOT, "android", "assets", "sfx")
    os.makedirs(out_dir, exist_ok=True)

    specs = {
        "shatter": (0.35, shatter_fn),
        "hit": (0.30, hit_fn),
        "pickup": (0.22, pickup_fn),
    }
    for name, (duration, fn) in specs.items():
        samples = synth_samples(duration, fn)
        wav_path = os.path.join(tmp_dir, f"{name}.wav")
        write_wav(wav_path, samples)
        ogg_path = os.path.join(out_dir, f"{name}.ogg")
        result = subprocess.run(
            ["oggenc", "-q", "5", "-o", ogg_path, wav_path],
            capture_output=True, text=True,
        )
        if result.returncode != 0:
            print(f"oggenc failed for {name}: {result.stderr}", file=sys.stderr)
            sys.exit(1)
        print(f"sfx   {name}.ogg")


# ---------------------------------------------------------------------------
# Particle effect (LibGDX text format, hand-authored — no binary tooling needed)
# ---------------------------------------------------------------------------

SHATTER_PARTICLE = """\
shatter
- Delay -
active: false
- Duration -
lowMin: 250.0
lowMax: 250.0
- Count -
min: 8
max: 16
- Emission -
lowMin: 0.0
lowMax: 0.0
highMin: 200.0
highMax: 200.0
relative: false
scalingCount: 2
scaling0: 1.0
scaling1: 0.0
timelineCount: 2
timeline0: 0.0
timeline1: 1.0
- Life -
lowMin: 0.0
lowMax: 0.0
highMin: 300.0
highMax: 500.0
relative: false
scalingCount: 2
scaling0: 1.0
scaling1: 0.0
timelineCount: 2
timeline0: 0.0
timeline1: 1.0
independent: false
- Life Offset -
active: false
independent: false
- X Offset -
active: false
- Y Offset -
active: false
- Spawn Shape -
shape: point
- Spawn Width -
lowMin: 0.0
lowMax: 0.0
highMin: 0.0
highMax: 0.0
relative: false
scalingCount: 1
scaling0: 1.0
timelineCount: 1
timeline0: 0.0
- Spawn Height -
lowMin: 0.0
lowMax: 0.0
highMin: 0.0
highMax: 0.0
relative: false
scalingCount: 1
scaling0: 1.0
timelineCount: 1
timeline0: 0.0
- Scale -
lowMin: 0.0
lowMax: 0.0
highMin: 6.0
highMax: 10.0
relative: false
scalingCount: 2
scaling0: 1.0
scaling1: 0.3
timelineCount: 2
timeline0: 0.0
timeline1: 1.0
- Velocity -
active: true
lowMin: 0.0
lowMax: 0.0
highMin: 120.0
highMax: 260.0
relative: false
scalingCount: 2
scaling0: 1.0
scaling1: 0.2
timelineCount: 2
timeline0: 0.0
timeline1: 1.0
- Angle -
active: true
lowMin: 0.0
lowMax: 360.0
highMin: 0.0
highMax: 360.0
relative: false
scalingCount: 1
scaling0: 1.0
timelineCount: 1
timeline0: 0.0
- Rotation -
active: false
- Wind -
active: false
- Gravity -
active: true
lowMin: 0.0
lowMax: 0.0
highMin: 300.0
highMax: 300.0
relative: false
scalingCount: 1
scaling0: 1.0
timelineCount: 1
timeline0: 0.0
- Tint -
colorsCount: 6
colors0: 0.6
colors1: 0.95
colors2: 1.0
colors3: 1.0
colors4: 1.0
colors5: 1.0
timelineCount: 2
timeline0: 0.0
timeline1: 1.0
- Transparency -
lowMin: 0.0
lowMax: 0.0
highMin: 1.0
highMax: 1.0
relative: false
scalingCount: 3
scaling0: 1.0
scaling1: 0.8
scaling2: 0.0
timelineCount: 3
timeline0: 0.0
timeline1: 0.6
timeline2: 1.0
- Options -
attached: false
continuous: false
aligned: false
additive: true
behind: false
premultipliedAlpha: false
spriteMode: single
imagePaths:
- particle.png
"""


def gen_particle_pixel(x, y, w, h):
    cx, cy = w / 2.0, h / 2.0
    dist = math.sqrt((x - cx) ** 2 + (y - cy) ** 2) / (w / 2.0)
    alpha = max(0, int(255 * max(0.0, 1.0 - dist)))
    return (255, 255, 255, alpha)


def gen_particles():
    particles_dir = os.path.join(ROOT, "android", "assets", "particles")
    os.makedirs(particles_dir, exist_ok=True)
    with open(os.path.join(particles_dir, "shatter.p"), "w") as f:
        f.write(SHATTER_PARTICLE)
    write_png(os.path.join(particles_dir, "particle.png"), 16, 16, gen_particle_pixel)
    print("particle shatter.p + particle.png")


if __name__ == "__main__":
    gen_icons()
    gen_feature_graphic()
    gen_particles()
    gen_sfx()
    print("Done.")
