"""
UPSTAGE_API_KEY, SERPAPI_API_KEY를 환경 변수로 설정하는 유틸리티.

- Google Colab: userdata.get() (Secrets)에서 불러오기
- 그 외: getpass로 터미널에서 입력받기

참고:
- Upstage Console: https://console.upstage.ai
- SerpAPI: https://serpapi.com
"""

import os
import sys


def _is_colab():
    """Google Colab 환경 여부 확인."""
    try:
        import google.colab
        return True
    except ImportError:
        return False


def set_api_key(key_name: str, display_name: str) -> bool:
    """
    API 키를 환경 변수에 설정하는 범용 함수.

    - Colab: userdata.get(display_name)으로 Secret에서 불러옴
    - 그 외: getpass로 비밀 입력받아 설정

    Args:
        key_name: 환경 변수 이름 (예: "UPSTAGE_API_KEY")
        display_name: Colab Secret 이름 / 사용자에게 보여줄 이름 (예: "UPSTAGE_API_KEY")

    Returns:
        설정 성공 여부
    """
    if _is_colab():
        try:
            from google.colab import userdata
            value = userdata.get(display_name)
            if value and value.strip():
                os.environ[key_name] = value.strip()
                print(f"[OK] {display_name} 설정 완료 (Colab Secret)")
                return True
            else:
                print(f"[경고] {display_name} Secret이 비어 있습니다.")
                return False
        except Exception as e:
            print(f"[실패] {display_name} Secret 로드 오류: {e}")
            return False
    else:
        try:
            import getpass
            value = getpass.getpass(f"{display_name} 입력 (비공개): ").strip()
            if value:
                os.environ[key_name] = value
                print(f"[OK] {display_name} 설정 완료")
                return True
            else:
                print(f"[경고] {display_name}가 비어 있어 건너뜁니다.")
                return False
        except Exception as e:
            print(f"[실패] {display_name} 입력 오류: {e}")
            return False


def _mask_key(key: str) -> str:
    """앞 8자리만 보이고 나머지는 마스킹."""
    if not key or len(key) < 8:
        return "(비어 있음)" if not key else key[:4] + "****"
    return key[:8] + "..." + key[-2:] if len(key) > 10 else key[:8] + "***"


def print_key_status():
    """설정된 모든 API 키 상태를 출력 (설정됨/미설정, 앞 8자리 미리보기)."""
    keys = [
        ("UPSTAGE_API_KEY", "Upstage API Key"),
        ("SERPAPI_API_KEY", "SerpAPI Key"),
    ]
    print("\n" + "=" * 50)
    print("API 키 설정 상태")
    print("=" * 50)
    for env_name, label in keys:
        value = os.environ.get(env_name)
        if value:
            print(f"  {label} ({env_name}): 설정됨 — {_mask_key(value)}")
        else:
            print(f"  {label} ({env_name}): 미설정")
    print("=" * 50)


def main():
    """UPSTAGE_API_KEY, SERPAPI_API_KEY 두 개를 설정하고 상태를 출력."""
    print("API 키를 환경 변수에 설정합니다.\n")

    set_api_key("UPSTAGE_API_KEY", "UPSTAGE_API_KEY")
    set_api_key("SERPAPI_API_KEY", "SERPAPI_API_KEY")

    print_key_status()


if __name__ == "__main__":
    main()
