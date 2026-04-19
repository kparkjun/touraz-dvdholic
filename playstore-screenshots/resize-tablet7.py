"""
태블릿 스크린샷 리사이즈
바탕화면의 holic_img1~4.png를 태블릿 규격으로 변환 후 바탕화면에 저장
- 7인치: 1200x1920
- 10인치: 1600x2560
"""
import os
import sys
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    print("Pillow가 필요합니다: pip install Pillow")
    exit(1)

_desktop = Path(os.environ.get("USERPROFILE", "")) / "Desktop"
_one = Path(os.environ.get("USERPROFILE", "")) / "OneDrive" / "Desktop"
DESKTOP = _desktop if _desktop.exists() else (_one if _one.exists() else Path("C:/Users/USER/Desktop"))

# 기본: 10인치 태블릿 (1600x2560)
TABLET_SIZES = {"7": (1200, 1920), "10": (1600, 2560)}
TARGET_W, TARGET_H = TABLET_SIZES.get(os.environ.get("TABLET", "10"), TABLET_SIZES["10"])

SOURCES = ["holic_img1.png", "holic_img2.png", "holi_img3.png", "holi_img4.png"]

def main():
    saved = 0
    for name in SOURCES:
        path = DESKTOP / name
        if not path.exists():
            print(f"없음: {name}")
            continue

        img = Image.open(path).convert("RGB")
        resized = img.resize((TARGET_W, TARGET_H), Image.Resampling.LANCZOS)
        out_name = path.stem + "-tablet10.png"
        out_path = DESKTOP / out_name
        resized.save(out_path)
        print(f"저장: {out_path.name}")
        saved += 1

    if saved == 0:
        print(f"바탕화면에서 파일을 찾을 수 없습니다: {SOURCES}")
        print(f"  경로: {DESKTOP}")
    else:
        print(f"\n완료! 바탕화면에 *-tablet10.png 로 {saved}개 저장되었습니다.")

if __name__ == "__main__":
    main()
