/**
 * 전역 한 번 터치로 즉시 반응하도록 하는 polyfill.
 * iOS/Android에서 300ms 터치 지연을 제거하고, 탭 시 즉시 click 이벤트를 발생시킵니다.
 */
let touchStartX = 0;
let touchStartY = 0;
let touchStartT = 0;

const TAP_THRESHOLD_PX = 30;
const TAP_THRESHOLD_MS = 500;

function isClickable(el) {
  if (!el || !el.closest) return false;
  const tag = el.tagName?.toLowerCase();
  const role = el.getAttribute?.("role");
  const clickable = el.closest?.(
    "button, a[href], [role='button'], [onclick], .ds-btn, .app-chip, .tab-content, .js-fast-tap, [tabindex='0']"
  );
  return (
    tag === "button" ||
    tag === "a" ||
    role === "button" ||
    el.onclick != null ||
    !!clickable
  );
}

function handleTouchStart(e) {
  if (e.touches?.length) {
    touchStartX = e.touches[0].clientX;
    touchStartY = e.touches[0].clientY;
    touchStartT = Date.now();
  }
}

function handleTouchEnd(e) {
  const target = e.target;
  if (!isClickable(target)) return;

  const touch = e.changedTouches?.[0];
  if (!touch) return;

  const dx = Math.abs(touch.clientX - touchStartX);
  const dy = Math.abs(touch.clientY - touchStartY);
  const dt = Date.now() - touchStartT;

  if (dx <= TAP_THRESHOLD_PX && dy <= TAP_THRESHOLD_PX && dt <= TAP_THRESHOLD_MS) {
    const clickable = target.closest?.(
      "button, a[href], [role='button'], [onclick], .ds-btn, .app-chip, .tab-content, .js-fast-tap, [tabindex='0']"
    ) || target;
    if (clickable && !clickable.disabled) {
      e.preventDefault();
      clickable.click();
    }
  }
}

export function initFastTap() {
  document.addEventListener("touchstart", handleTouchStart, { passive: true });
  document.addEventListener("touchend", handleTouchEnd, { passive: false });
  return () => {
    document.removeEventListener("touchstart", handleTouchStart);
    document.removeEventListener("touchend", handleTouchEnd);
  };
}
