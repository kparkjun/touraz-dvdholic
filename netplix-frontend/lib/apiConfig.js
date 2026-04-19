/**
 * API Base URL - Capacitor iOS에서 capacitor://localhost 사용 시 Heroku URL 필요
 * (App Store 리젝 2.1a: OAuth/데모 로그인 실패, 무한 로딩 원인)
 */
import { Capacitor } from "@capacitor/core";

const HEROKU_API_URL = "https://touraz-dvdholic-2507bcb348dd.herokuapp.com";

function isNativeIOS() {
  try {
    return Capacitor?.isNativePlatform?.() && Capacitor.getPlatform() === "ios";
  } catch {
    return false;
  }
}

export function getApiBaseUrl() {
  if (typeof window === "undefined") {
    const env = process.env.NEXT_PUBLIC_API_URL;
    return env && env !== "" ? env : "";
  }
  const env = process.env.NEXT_PUBLIC_API_URL;
  if (env && env !== "") return env;

  // 1) Capacitor 네이티브 iOS: 가장 확실한 감지 (origin 타이밍 이슈 회피)
  if (isNativeIOS()) return HEROKU_API_URL;

  const origin = (window.location?.origin || "").toLowerCase();
  // 2) capacitor://, ionic://, file:// 등 로컬 스킴 → Heroku 사용
  if (
    origin.startsWith("capacitor://") ||
    origin.startsWith("ionic://") ||
    origin.startsWith("file://")
  ) {
    return HEROKU_API_URL;
  }
  // 3) WKWebView 등에서 origin이 비정상인 경우 (예: "null", "about:blank")
  if (!origin || origin === "null" || !origin.startsWith("http")) {
    if (typeof navigator !== "undefined" && /iPhone|iPad|iPod/.test(navigator.userAgent)) {
      return HEROKU_API_URL;
    }
  }

  return process.env.NODE_ENV === "production" ? "" : "http://localhost:8080";
}
