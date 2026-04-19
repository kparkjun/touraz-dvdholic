"""
구글 플레이 스토어용 휴대폰 스크린샷 리사이즈 스크립트
raw/ 폴더의 이미지를 1080x1920 (세로) 규격으로 변환하여 phone/에 저장
- 프로모션 이용: 각 변 최소 1,080px, 4개 이상 필요
"""
import os
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    print("Pillow가 필요합니다: pip install Pillow")
    exit(1)

# Play Store 휴대폰 권장 크기 (세로)
TARGET_W, TARGET_H = 1080, 1920

BASE = Path(__file__).parent
RAW_DIR = BASE / "raw"
OUT_DIR = BASE / "phone"

def main():
    RAW_DIR.mkdir(exist_ok=True)
    OUT_DIR.mkdir(exist_ok=True)

    paths = [p for p in RAW_DIR.iterdir() if p.suffix.lower() in (".png", ".jpg", ".jpeg")]
    if not paths:
        print(f"raw/ 폴더에 PNG 또는 JPG 이미지를 넣어주세요.")
        print(f"  경로: {RAW_DIR}")
        return

    paths = sorted(paths)
    for i, path in enumerate(paths):
        img = Image.open(path).convert("RGB")
        resized = img.resize((TARGET_W, TARGET_H), Image.Resampling.LANCZOS)
        out_path = OUT_DIR / f"phone-screenshot-{i+1:02d}.png"
        resized.save(out_path)
        print(f"저장: {out_path.name}")

    print(f"\n완료! phone/ 폴더에서 결과를 확인하세요.")
    if len(paths) < 4:
        print(f"※ 프로모션 이용 시 4개 이상 권장 (현재 {len(paths)}개)")

if __name__ == "__main__":
    main()
