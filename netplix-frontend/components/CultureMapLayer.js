'use client';
import React, { useEffect, useMemo, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import axios from "@/lib/axiosConfig";

let CircleMarker, Popup, Tooltip;
if (typeof window !== "undefined") {
  const rl = require("react-leaflet");
  CircleMarker = rl.CircleMarker;
  Popup = rl.Popup;
  Tooltip = rl.Tooltip;
}

// 지역별 사진 프리뷰 캐시 (탭 생명주기 동안 유지). popup 이 열릴 때마다 재호출하지 않는다.
const photoCache = new Map();

function RegionPhotoMini({ areaCode }) {
  const [photos, setPhotos] = useState(() => photoCache.get(areaCode) || null);
  const fetched = useRef(false);

  useEffect(() => {
    if (photos != null) return;
    if (fetched.current) return;
    fetched.current = true;
    (async () => {
      try {
        const res = await axios.get(
          `/api/v1/cine-trip/photos?areaCode=${encodeURIComponent(areaCode)}&limit=3`
        );
        const list = Array.isArray(res?.data?.data) ? res.data.data : [];
        photoCache.set(areaCode, list);
        setPhotos(list);
      } catch {
        photoCache.set(areaCode, []);
        setPhotos([]);
      }
    })();
  }, [areaCode, photos]);

  if (photos == null) {
    return (
      <div style={{ display: "flex", gap: 4, marginTop: 6 }}>
        {[0, 1, 2].map((i) => (
          <div
            key={i}
            style={{
              flex: "1 1 0",
              height: 44,
              borderRadius: 4,
              background: "linear-gradient(90deg, #eee 0%, #f6f6f6 50%, #eee 100%)",
            }}
          />
        ))}
      </div>
    );
  }
  if (photos.length === 0) return null;
  return (
    <div style={{ display: "flex", gap: 4, marginTop: 6 }}>
      {photos.slice(0, 3).map((p, idx) => (
        <a
          key={p.contentId || idx}
          href={`/cine-trip?area=${encodeURIComponent(areaCode)}`}
          title={`${p.title || ""}${p.filmSite ? ` · ${p.filmSite}` : ""}`}
          style={{
            flex: "1 1 0",
            height: 44,
            borderRadius: 4,
            overflow: "hidden",
            display: "block",
            background: "#111",
          }}
        >
          <img
            src={p.thumbnailUrl || p.imageUrl}
            alt={p.title || "관광 사진"}
            loading="lazy"
            style={{ width: "100%", height: "100%", objectFit: "cover", display: "block" }}
            onError={(e) => {
              e.target.style.display = "none";
            }}
          />
        </a>
      ))}
    </div>
  );
}

/**
 * 문화 × 관광 지도 오버레이 레이어
 * - /api/v1/dvd-stores/stats/by-region 집계(매장 밀도) + /api/v1/tour/regions 지자체 지표를 결합
 * - layer: 'stores' | 'tourDemand' | 'cultural' | 'search' 중 하나
 */
export default function CultureMapLayer({ layer = "stores" }) {
  const { t } = useTranslation();
  const [stats, setStats] = useState([]);
  const [tour, setTour] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        setLoading(true);
        const [a, b] = await Promise.all([
          axios.get("/api/v1/dvd-stores/stats/by-region"),
          axios.get("/api/v1/tour/regions").catch(() => ({ data: { data: [] } })),
        ]);
        if (cancelled) return;
        setStats(a.data?.data || []);
        setTour(b.data?.data || []);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const tourByArea = useMemo(() => {
    const m = new Map();
    for (const r of tour) m.set(r.areaCode, r);
    return m;
  }, [tour]);

  const maxStores = useMemo(
    () => stats.reduce((m, r) => Math.max(m, Number(r.totalCount || 0)), 1),
    [stats]
  );

  const maxSearch = useMemo(
    () => tour.reduce((m, r) => Math.max(m, Number(r.searchVolume || 0)), 1),
    [tour]
  );

  const points = useMemo(() => {
    return stats
      .filter((r) => r.avgLatitude != null && r.avgLongitude != null)
      .map((r) => {
        const tr = tourByArea.get(r.areaCode);
        return {
          areaCode: r.areaCode,
          lat: Number(r.avgLatitude),
          lon: Number(r.avgLongitude),
          totalCount: Number(r.totalCount || 0),
          operatingCount: Number(r.operatingCount || 0),
          closedCount: Number(r.closedCount || 0),
          regionName: tr?.regionName || r.areaCode,
          tourDemandIdx: tr?.tourDemandIdx,
          culturalResourceDemand: tr?.culturalResourceDemand,
          searchVolume: tr?.searchVolume,
        };
      });
  }, [stats, tourByArea]);

  if (typeof window === "undefined" || !CircleMarker) return null;
  if (loading) return null;

  return (
    <>
      {points.map((p) => {
        const { radius, color, fill } = computeStyle(p, layer, maxStores, maxSearch);
        return (
          <CircleMarker
            key={`culture-${p.areaCode}`}
            center={[p.lat, p.lon]}
            radius={radius}
            pathOptions={{ color, fillColor: fill, fillOpacity: 0.45, weight: 1 }}
          >
            {Tooltip && (
              <Tooltip direction="top" offset={[0, -2]} opacity={0.95}>
                <strong>{p.regionName}</strong>
              </Tooltip>
            )}
            {Popup && (
              <Popup>
                <div style={{ minWidth: 180, fontSize: 12 }}>
                  <div style={{ fontWeight: 700, fontSize: 14, marginBottom: 6 }}>{p.regionName}</div>
                  <div>{t("cultureMap.regionTotal")}: <b>{p.totalCount}</b></div>
                  <div>{t("cultureMap.regionOperating")}: {p.operatingCount}</div>
                  <div>{t("cultureMap.regionClosed")}: {p.closedCount}</div>
                  {p.tourDemandIdx != null && (
                    <div>{t("cultureMap.regionTourIdx")}: {p.tourDemandIdx.toFixed(1)}</div>
                  )}
                  {p.culturalResourceDemand != null && (
                    <div>{t("cultureMap.regionCulture")}: {p.culturalResourceDemand.toFixed(1)}</div>
                  )}
                  {p.searchVolume != null && (
                    <div>{t("cultureMap.regionSearch")}: {p.searchVolume.toLocaleString()}</div>
                  )}
                  <RegionPhotoMini areaCode={p.areaCode} />
                </div>
              </Popup>
            )}
          </CircleMarker>
        );
      })}
    </>
  );
}

function computeStyle(p, layer, maxStores, maxSearch) {
  switch (layer) {
    case "tourDemand": {
      const v = p.tourDemandIdx ?? 0;
      return {
        radius: 10 + Math.min(1, p.totalCount / maxStores) * 18,
        color: "#f97316",
        fill: heatColor(v / 100),
      };
    }
    case "cultural": {
      const v = p.culturalResourceDemand ?? 0;
      return {
        radius: 10 + Math.min(1, p.totalCount / maxStores) * 18,
        color: "#8b5cf6",
        fill: heatColorPurple(v / 100),
      };
    }
    case "search": {
      const v = p.searchVolume ?? 0;
      return {
        radius: 8 + Math.min(1, v / maxSearch) * 24,
        color: "#0ea5e9",
        fill: heatColorBlue(v / maxSearch),
      };
    }
    case "stores":
    default: {
      const ratio = Math.min(1, p.totalCount / maxStores);
      return {
        radius: 8 + ratio * 24,
        color: "#ea580c",
        fill: heatColor(ratio),
      };
    }
  }
}

function heatColor(v) {
  const r = Math.round(255 * Math.min(1, 0.2 + v));
  const g = Math.round(120 * (1 - v));
  return `rgb(${r}, ${g}, 30)`;
}
function heatColorPurple(v) {
  const r = Math.round(120 + 100 * v);
  const b = Math.round(200 + 55 * v);
  return `rgb(${r}, 60, ${b})`;
}
function heatColorBlue(v) {
  const b = Math.round(200 + 55 * v);
  const g = Math.round(120 + 120 * v);
  return `rgb(30, ${g}, ${b})`;
}
