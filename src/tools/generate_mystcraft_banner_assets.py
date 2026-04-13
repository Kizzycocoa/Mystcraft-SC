#!/usr/bin/env python3
from __future__ import annotations

import json
import random
import re
from pathlib import Path
from typing import Dict, List

from PIL import Image

MODID = "mystcraft-sc"

ROOT = Path(__file__).resolve().parents[1]
WORDS_JAVA = ROOT / "main/java/myst/synthetic/page/word/MystcraftPageWords.java"
SYMBOLS_PNG = ROOT / "main/resources/assets/mystcraft-sc/textures/page/symbolcomponents.png"

DATA_DIR = ROOT / "main/resources/data/mystcraft-sc"
ASSET_DIR = ROOT / "main/resources/assets/mystcraft-sc"

BANNER_PATTERN_DIR = DATA_DIR / "banner_pattern"
BANNER_TAG_DIR = DATA_DIR / "tags/banner_pattern"
BANNER_TEX_DIR = ASSET_DIR / "textures/entity/banner"
SHIELD_TEX_DIR = ASSET_DIR / "textures/entity/shield"

COMPONENT_SIZE = 64
COMPONENTS_PER_ROW = 8

WORDS_ONLY = [
    "balance", "believe", "change", "chaos", "civilization", "constraint", "contradict",
    "control", "convey", "creativity", "cycle", "dependence", "discover", "dynamic",
    "elevate", "encourage", "energy", "entropy", "ethereal", "exist", "explore", "flow",
    "force", "form", "future", "growth", "harmony", "honor", "infinite", "inhibit",
    "intelligence", "love", "machine", "merge", "momentum", "motion", "mutual", "nature",
    "nurture", "possibility", "power", "question", "rebirth", "remember", "resilience",
    "resurrect", "sacrifice", "society", "spur", "static", "stimulate", "survival",
    "sustain", "system", "time", "tradition", "transform", "weave", "wisdom", "void",
    "chain", "celestial", "image", "terrain", "order",
]

NUMBERS = [str(i) for i in range(26)]

def parse_registered_words(java_text: str) -> Dict[str, List[int]]:
    pattern = re.compile(r'register\("([^"]+)",\s*([^;]+?)\);', re.DOTALL)
    results: Dict[str, List[int]] = {}

    for name, body in pattern.findall(java_text):
        if name == "DEBUG":
            continue

        ints = [int(x) for x in re.findall(r'\b\d+\b', body)]
        if ints:
            results[name.lower()] = ints

    return results

def java_string_hashcode(s: str) -> int:
    h = 0
    for ch in s:
        h = (31 * h + ord(ch)) & 0xFFFFFFFF
    if h & 0x80000000:
        h -= 0x100000000
    return h

class JavaRandom:
    def __init__(self, seed: int):
        self.seed = (seed ^ 0x5DEECE66D) & ((1 << 48) - 1)

    def next(self, bits: int) -> int:
        self.seed = (self.seed * 25214903917 + 11) & ((1 << 48) - 1)
        return self.seed >> (48 - bits)

    def next_int(self, bound: int) -> int:
        if bound <= 0:
            raise ValueError("bound must be positive")
        if (bound & (bound - 1)) == 0:
            return (bound * self.next(31)) >> 31
        while True:
            bits = self.next(31)
            value = bits % bound
            if bits - value + (bound - 1) >= 0:
                return value

def fallback_word_components(raw_key: str) -> List[int]:
    normalized = raw_key.strip().lower()
    rand = JavaRandom(java_string_hashcode(normalized))
    max_component_index = 20
    count = rand.next_int(10) + 3

    if normalized.startswith("easter"):
        count = 4
        max_component_index = 8

    return [rand.next_int(max_component_index) + 4 for _ in range(count)]

def ensure_dirs() -> None:
    BANNER_PATTERN_DIR.mkdir(parents=True, exist_ok=True)
    BANNER_TAG_DIR.mkdir(parents=True, exist_ok=True)
    BANNER_TEX_DIR.mkdir(parents=True, exist_ok=True)
    SHIELD_TEX_DIR.mkdir(parents=True, exist_ok=True)

def write_json(path: Path, data: dict) -> None:
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")

def make_pattern_jsons() -> None:
    for name in WORDS_ONLY + NUMBERS:
        write_json(
            BANNER_PATTERN_DIR / f"{name}.json",
            {
                "asset_id": f"{MODID}:{name}",
                "translation_key": f"item.banner.mystcraft_{name}",
            },
        )

def make_tag_jsons() -> None:
    write_json(
        BANNER_TAG_DIR / "myst_poetry_pattern_item.json",
        {
            "replace": False,
            "values": [f"{MODID}:{name}" for name in WORDS_ONLY],
        },
    )
    write_json(
        BANNER_TAG_DIR / "myst_numerology_pattern_item.json",
        {
            "replace": False,
            "values": [f"{MODID}:{name}" for name in NUMBERS],
        },
    )

def extract_component(atlas: Image.Image, index: int) -> Image.Image:
    x = (index % COMPONENTS_PER_ROW) * COMPONENT_SIZE
    y = (index // COMPONENTS_PER_ROW) * COMPONENT_SIZE
    return atlas.crop((x, y, x + COMPONENT_SIZE, y + COMPONENT_SIZE))

def compose_pattern(atlas: Image.Image, components: List[int]) -> Image.Image:
    # 64x64 grayscale/alpha mask. This is intentionally simple and stable.
    canvas = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    for index in components:
        glyph = extract_component(atlas, index).convert("RGBA")
        glyph = glyph.resize((58, 58), Image.Resampling.BILINEAR)

        # White silhouette from alpha only, since banner dyes supply final color.
        out = Image.new("RGBA", glyph.size, (255, 255, 255, 0))
        alpha = glyph.getchannel("A")
        out.putalpha(alpha)

        canvas.alpha_composite(out, (3, 3))

    return canvas

def main() -> None:
    ensure_dirs()

    java_text = WORDS_JAVA.read_text(encoding="utf-8")
    word_map = parse_registered_words(java_text)
    atlas = Image.open(SYMBOLS_PNG).convert("RGBA")

    make_pattern_jsons()
    make_tag_jsons()

    for name in WORDS_ONLY:
        components = word_map.get(name)
        if not components:
            raise RuntimeError(f"Missing components for legacy word pattern: {name}")

        image = compose_pattern(atlas, components)
        image.save(BANNER_TEX_DIR / f"{name}.png")
        image.save(SHIELD_TEX_DIR / f"{name}.png")

    for name in NUMBERS:
        components = fallback_word_components(name)
        image = compose_pattern(atlas, components)
        image.save(BANNER_TEX_DIR / f"{name}.png")
        image.save(SHIELD_TEX_DIR / f"{name}.png")

    print("Generated Mystcraft banner patterns:")
    print(f"  {len(WORDS_ONLY)} poetry patterns")
    print(f"  {len(NUMBERS)} numerology patterns")
    print("Done.")

if __name__ == "__main__":
    main()