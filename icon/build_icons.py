"""
Generate Android launcher icons from icon-1.png (white background + purple ribbon).
- Keeps the original white background (matches the reference image)
- Resizes to 5 density buckets (mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi)
- Produces both ic_launcher.png (square) and ic_launcher_round.png (circular)
"""
import os
from PIL import Image, ImageDraw

SRC = r"Q:\large_program\liu-flow\icon\icon-1.png"
OUT_BASE = r"Q:\large_program\liu-flow\android\app\src\main\res"

SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}


def make_round(square_img: Image.Image) -> Image.Image:
    """Apply a circular alpha mask to make the round variant."""
    w, h = square_img.size
    rnd = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    mask = Image.new("L", (w, h), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, w, h), fill=255)
    rnd.paste(square_img, (0, 0), mask)
    return rnd


def main():
    src = Image.open(SRC).convert("RGBA")
    print(f"Source: {SRC}  size={src.size}")

    for folder, size in SIZES.items():
        out_dir = os.path.join(OUT_BASE, folder)
        os.makedirs(out_dir, exist_ok=True)

        sq = src.resize((size, size), Image.LANCZOS)
        sq.save(os.path.join(out_dir, "ic_launcher.png"), "PNG", optimize=True)

        rnd = make_round(sq)
        rnd.save(os.path.join(out_dir, "ic_launcher_round.png"), "PNG", optimize=True)

        print(f"  {folder:18s}  {size}x{size}  ->  ic_launcher.png  +  ic_launcher_round.png")


if __name__ == "__main__":
    main()
