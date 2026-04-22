"use client";
import { useEffect } from "react";

/**
 * containerRef 하위의 가로 스크롤 레일에 "마우스 드래그/터치 스와이프"를 자동 바인딩.
 *
 * 대상 셀렉터:
 *   .dashboard-scroll-row (legacy), .cinetrip-scroll-row, .js-drag-scroll
 *
 * 동작:
 *  - 왼쪽 마우스 버튼 / 터치 / 펜 으로 좌우 드래그 → 관성 스크롤 포함
 *  - 6px 이상 이동해야 드래그로 인식(→ 가벼운 클릭은 버튼/링크로 전달)
 *  - pointerdown 에서 네이티브 이미지/링크 drag 즉시 차단
 *  - 컨테이너의 dragstart 도 차단하여 브라우저 ghost drag 방지
 *
 * 동적 마운트 대응:
 *  - MutationObserver 로 containerRef 하위에 새로 추가되는 레일도 즉시 바인딩
 *    (예: 모달이 열리면서 mount 되는 PhotoGalleryStrip/AccessibleSpotsStrip)
 */
const SELECTOR = ".dashboard-scroll-row, .cinetrip-scroll-row, .js-drag-scroll";

export default function useDragScrollAll(containerRef) {
  useEffect(() => {
    const container = containerRef?.current ?? (typeof document !== "undefined" ? document : null);
    if (!container) return;

    const cleanups = [];

    const bindRow = (el) => {
      if (!el || el._dragBound) return;
      el._dragBound = true;

      const s = {
        isDown: false,
        dragging: false,
        startX: 0,
        scrollLeft: 0,
        velX: 0,
        prevX: 0,
        prevTime: 0,
        pointerId: null,
        rafId: null,
      };

      const stopMomentum = () => {
        if (s.rafId) {
          cancelAnimationFrame(s.rafId);
          s.rafId = null;
        }
      };

      const momentum = () => {
        if (Math.abs(s.velX) < 0.1) {
          s.rafId = null;
          return;
        }
        el.scrollLeft += s.velX;
        s.velX *= 0.96;
        s.rafId = requestAnimationFrame(momentum);
      };

      const onDown = (e) => {
        if (e.pointerType === "mouse" && e.button !== 0) return;

        if (e.cancelable) {
          try { e.preventDefault(); } catch {}
        }

        stopMomentum();
        s.isDown = true;
        s.dragging = false;
        s.startX = e.clientX;
        s.prevX = e.clientX;
        s.prevTime = Date.now();
        s.scrollLeft = el.scrollLeft;
        s.velX = 0;
        s.pointerId = e.pointerId;
      };

      const onMove = (e) => {
        if (!s.isDown) return;
        const dx = e.clientX - s.startX;

        if (!s.dragging && Math.abs(dx) > 6) {
          s.dragging = true;
          try { el.setPointerCapture(s.pointerId); } catch {}
          el.style.cursor = "grabbing";
        }

        if (!s.dragging) return;

        const now = Date.now();
        const dt = now - s.prevTime || 1;
        const instantVel = (s.prevX - e.clientX) / dt * 16;
        s.velX = s.velX * 0.4 + instantVel * 0.6;
        s.prevX = e.clientX;
        s.prevTime = now;

        el.scrollLeft = s.scrollLeft - dx;
      };

      const onUp = () => {
        if (!s.isDown) return;
        const wasDragging = s.dragging;
        s.isDown = false;
        s.dragging = false;

        if (wasDragging) {
          try { el.releasePointerCapture(s.pointerId); } catch {}
          el.style.cursor = "grab";
          if (Math.abs(s.velX) > 0.3) {
            s.velX *= 1.8;
            const maxVel = 60;
            if (Math.abs(s.velX) > maxVel) s.velX = s.velX > 0 ? maxVel : -maxVel;
            s.rafId = requestAnimationFrame(momentum);
          }
        }
      };

      const onClick = (e) => {
        if (s.dragging) {
          e.preventDefault();
          e.stopPropagation();
        }
      };

      const onDragStart = (e) => {
        e.preventDefault();
      };

      el.style.cursor = "grab";
      el.style.touchAction = "pan-y";

      el.addEventListener("pointerdown", onDown);
      el.addEventListener("pointermove", onMove);
      el.addEventListener("pointerup", onUp);
      el.addEventListener("pointercancel", onUp);
      el.addEventListener("pointerleave", onUp);
      el.addEventListener("click", onClick, true);
      el.addEventListener("dragstart", onDragStart);

      cleanups.push(() => {
        stopMomentum();
        el.removeEventListener("pointerdown", onDown);
        el.removeEventListener("pointermove", onMove);
        el.removeEventListener("pointerup", onUp);
        el.removeEventListener("pointercancel", onUp);
        el.removeEventListener("pointerleave", onUp);
        el.removeEventListener("click", onClick, true);
        el.removeEventListener("dragstart", onDragStart);
        el._dragBound = false;
      });
    };

    const queryRoot = (root) => {
      if (root.nodeType !== 1 && root !== document) return;
      if (root.matches && root.matches(SELECTOR)) bindRow(root);
      const list = root.querySelectorAll ? root.querySelectorAll(SELECTOR) : [];
      list.forEach(bindRow);
    };

    queryRoot(container);

    // 동적으로 추가되는 .js-drag-scroll 레일도 즉시 바인딩
    // (예: 여행 코스 모달이 열리면서 mount 되는 PhotoGalleryStrip / AccessibleSpotsStrip)
    let observer = null;
    if (typeof MutationObserver !== "undefined") {
      observer = new MutationObserver((mutations) => {
        for (const m of mutations) {
          m.addedNodes.forEach((node) => queryRoot(node));
        }
      });
      observer.observe(container, { childList: true, subtree: true });
    }

    return () => {
      if (observer) observer.disconnect();
      cleanups.forEach((fn) => fn());
    };
  }, [containerRef]);
}
