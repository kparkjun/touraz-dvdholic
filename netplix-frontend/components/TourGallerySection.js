"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import axios from "@/src/axiosConfig";
import { useTranslation } from "react-i18next";
import { X, ChevronLeft, ChevronRight } from "lucide-react";

/**
 * 관광사진갤러리 섹션 (공용 컴포넌트).
 *
 * <p>백엔드: GET /api/v1/tour-gallery?q=<keyword>&limit=<n>
 *  - 영화 상세: keyword = 촬영지/지역명
 *  - /cine-trip 지역 상세: keyword = 지역명
 *  - DVD 매장 상세: keyword = 시·도명
 *
 * <p>정책:
 *  - 응답이 빈 배열이면 섹션 자체를 렌더링하지 않아 UX 공백을 없앰
 *  - 라이트박스: ESC 닫기 / ← → 이동 / 카드 클릭 열기
 *  - 반응형 그리드: 2(모바일) / 3(태블릿) / 4(데스크톱) 열
 *  - 스타일은 self-contained (외부 CSS 파일 오염 최소화)
 */
export default function TourGallerySection({
  keyword,
  title,
  subtitle,
  limit = 24,
  apiBase = "/api/v1/tour-gallery",
  accent = "#e50914", // netplix 레드 포인트
}) {
  const { t } = useTranslation();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errored, setErrored] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(null);

  useEffect(() => {
    let cancelled = false;
    async function run() {
      if (!keyword || !keyword.trim()) {
        setItems([]);
        setLoading(false);
        return;
      }
      try {
        setLoading(true);
        setErrored(false);
        const res = await axios.get(apiBase, {
          params: { q: keyword, limit },
        });
        if (cancelled) return;
        const data = res?.data?.data ?? res?.data ?? [];
        setItems(Array.isArray(data) ? data : []);
      } catch (e) {
        if (!cancelled) {
          setErrored(true);
          setItems([]);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    run();
    return () => {
      cancelled = true;
    };
  }, [keyword, limit, apiBase]);

  const handleClose = useCallback(() => setSelectedIndex(null), []);
  const handlePrev = useCallback(
    () => setSelectedIndex((p) => (p > 0 ? p - 1 : p)),
    []
  );
  const handleNext = useCallback(
    () =>
      setSelectedIndex((p) =>
        p !== null && p < items.length - 1 ? p + 1 : p
      ),
    [items.length]
  );

  useEffect(() => {
    if (selectedIndex === null) return undefined;
    const onKey = (e) => {
      if (e.key === "Escape") handleClose();
      else if (e.key === "ArrowLeft") handlePrev();
      else if (e.key === "ArrowRight") handleNext();
    };
    document.addEventListener("keydown", onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = prev;
    };
  }, [selectedIndex, handleClose, handlePrev, handleNext]);

  const totalLabel = useMemo(() => {
    if (loading) return null;
    return t("tourGallery.totalCount", { count: items.length });
  }, [items.length, loading, t]);

  // 데이터가 없고 로딩/에러도 끝났다면 섹션 전체 숨김
  if (!loading && !errored && items.length === 0) return null;
  // 로딩 중이지만 keyword 가 없거나 비어 있으면 숨김
  if (!keyword || !keyword.trim()) return null;

  return (
    <section className="tg-section" aria-label={title || t("tourGallery.defaultTitle")}>
      <style>{cssBlock}</style>
      <div className="tg-header">
        <h2 className="tg-title">
          {title || t("tourGallery.defaultTitle")}
          {totalLabel && (
            <span className="tg-total" style={{ color: accent }}>
              ({totalLabel})
            </span>
          )}
        </h2>
        {subtitle && <p className="tg-sub">{subtitle}</p>}
      </div>

      <div className="tg-grid">
        {loading
          ? Array.from({ length: 8 }).map((_, i) => (
              <div key={`sk-${i}`} className="tg-card tg-skeleton">
                <div className="tg-img tg-sk-img" />
                <div className="tg-body">
                  <div className="tg-sk-line tg-sk-line-lg" />
                  <div className="tg-sk-line" />
                  <div className="tg-sk-line tg-sk-line-sm" />
                </div>
              </div>
            ))
          : items.map((item, index) => (
              <button
                type="button"
                key={`${item.galContentId || index}`}
                className="tg-card"
                onClick={() => setSelectedIndex(index)}
                aria-label={`${item.title || ""} ${item.photoLocation || ""}`.trim()}
              >
                <div className="tg-img">
                  {(item.thumbnailUrl || item.imageUrl) && (
                    <img
                      src={item.thumbnailUrl || item.imageUrl}
                      alt={item.title || ""}
                      loading="lazy"
                      referrerPolicy="no-referrer"
                    />
                  )}
                </div>
                <div className="tg-body">
                  <div className="tg-ctitle" title={item.title || ""}>
                    {item.title || t("tourGallery.untitled")}
                  </div>
                  {item.photoLocation && (
                    <div className="tg-meta">📍 {item.photoLocation}</div>
                  )}
                  {(item.photoMonth || item.photographer) && (
                    <div className="tg-meta tg-meta-sub">
                      {item.photoMonth ? `📅 ${formatMonth(item.photoMonth)} ` : ""}
                      {item.photographer ? `· 📷 ${item.photographer}` : ""}
                    </div>
                  )}
                </div>
              </button>
            ))}
      </div>

      {selectedIndex !== null && items[selectedIndex] && (
        <Lightbox
          item={items[selectedIndex]}
          hasPrev={selectedIndex > 0}
          hasNext={selectedIndex < items.length - 1}
          onClose={handleClose}
          onPrev={handlePrev}
          onNext={handleNext}
        />
      )}
    </section>
  );
}

function Lightbox({ item, hasPrev, hasNext, onClose, onPrev, onNext }) {
  const { t } = useTranslation();
  return (
    <div
      className="tg-lb-overlay"
      role="dialog"
      aria-modal="true"
      aria-label={item.title || t("tourGallery.lightboxLabel")}
      onClick={onClose}
    >
      <div className="tg-lb-inner" onClick={(e) => e.stopPropagation()}>
        <img
          src={item.imageUrl || item.thumbnailUrl}
          alt={item.title || ""}
          className="tg-lb-img"
          referrerPolicy="no-referrer"
        />
        <div className="tg-lb-caption">
          <div className="tg-lb-title">{item.title}</div>
          <div className="tg-lb-meta">
            {item.photoLocation ? `📍 ${item.photoLocation}` : ""}
            {item.photoMonth ? ` · 📅 ${formatMonth(item.photoMonth)}` : ""}
            {item.photographer ? ` · 📷 ${item.photographer}` : ""}
          </div>
        </div>
        <button
          className="tg-lb-btn tg-lb-close"
          onClick={onClose}
          aria-label={t("tourGallery.close")}
        >
          <X size={22} />
        </button>
        {hasPrev && (
          <button
            className="tg-lb-btn tg-lb-prev"
            onClick={onPrev}
            aria-label={t("tourGallery.prev")}
          >
            <ChevronLeft size={24} />
          </button>
        )}
        {hasNext && (
          <button
            className="tg-lb-btn tg-lb-next"
            onClick={onNext}
            aria-label={t("tourGallery.next")}
          >
            <ChevronRight size={24} />
          </button>
        )}
      </div>
    </div>
  );
}

// yyyyMM → "2024.06" 형태. 원본이 형식이 다르면 그대로 반환.
function formatMonth(ym) {
  if (!ym) return "";
  if (/^\d{6}$/.test(ym)) return `${ym.slice(0, 4)}.${ym.slice(4, 6)}`;
  return ym;
}

const cssBlock = `
.tg-section {
  width: 100%;
  margin: 24px 0 8px;
}
.tg-header {
  padding: 0 4px 12px;
}
.tg-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 1.15rem;
  font-weight: 700;
  color: inherit;
}
.tg-total {
  font-size: 0.9rem;
  font-weight: 700;
}
.tg-sub {
  margin: 4px 0 0;
  font-size: 0.85rem;
  color: #9aa0a6;
}
.tg-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
@media (min-width: 640px) {
  .tg-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (min-width: 1024px) {
  .tg-grid { grid-template-columns: repeat(4, minmax(0, 1fr)); }
}
.tg-card {
  display: flex;
  flex-direction: column;
  text-align: left;
  background: rgba(20, 20, 20, 0.85);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  color: #f1f1f1;
  padding: 0;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.25);
}
.tg-card:hover {
  transform: translateY(-2px) scale(1.01);
  border-color: rgba(255, 255, 255, 0.16);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.45);
}
.tg-card:focus-visible {
  outline: 2px solid #e50914;
  outline-offset: 2px;
}
.tg-img {
  position: relative;
  width: 100%;
  padding-top: 66.67%;
  background: #0e0e0e;
  overflow: hidden;
}
.tg-img img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.tg-body {
  padding: 10px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.tg-ctitle {
  font-size: 0.95rem;
  font-weight: 600;
  line-height: 1.3;
  color: #f5f5f5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.tg-meta {
  font-size: 0.78rem;
  color: #bdbdbd;
}
.tg-meta-sub {
  color: #9a9a9a;
}

.tg-skeleton { cursor: default; }
.tg-sk-img, .tg-sk-line {
  background: linear-gradient(90deg, #2a2a2a 0%, #3a3a3a 50%, #2a2a2a 100%);
  background-size: 200% 100%;
  animation: tg-shine 1.4s linear infinite;
  border-radius: 6px;
}
.tg-sk-img {
  position: absolute;
  inset: 0;
}
.tg-sk-line {
  height: 10px;
  margin-top: 6px;
  width: 70%;
}
.tg-sk-line-lg { height: 14px; width: 85%; }
.tg-sk-line-sm { width: 45%; }
@keyframes tg-shine {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.tg-lb-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.92);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.tg-lb-inner {
  position: relative;
  max-width: 92vw;
  max-height: 92vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.tg-lb-img {
  max-width: 92vw;
  max-height: 80vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.6);
}
.tg-lb-caption {
  margin-top: 10px;
  text-align: center;
  color: #e6e6e6;
  max-width: 92vw;
}
.tg-lb-title {
  font-size: 1rem;
  font-weight: 700;
}
.tg-lb-meta {
  margin-top: 2px;
  font-size: 0.85rem;
  color: #bdbdbd;
}
.tg-lb-btn {
  position: absolute;
  background: rgba(20, 20, 20, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #fff;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.15s ease, transform 0.15s ease;
}
.tg-lb-btn:hover {
  background: rgba(60, 60, 60, 0.95);
  transform: scale(1.05);
}
.tg-lb-close { top: -48px; right: 0; }
.tg-lb-prev  { left: -6px; top: 50%; transform: translateY(-50%); }
.tg-lb-next  { right: -6px; top: 50%; transform: translateY(-50%); }
@media (max-width: 640px) {
  .tg-lb-close { top: 4px; right: 4px; }
  .tg-lb-prev  { left: 4px; }
  .tg-lb-next  { right: 4px; }
}
`;
