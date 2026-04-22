'use client';

import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import {
  Accessibility,
  UtensilsCrossed,
  Hotel,
  MapPin,
  Phone,
  Eye,
  Ear,
  Baby,
} from 'lucide-react';
import axios from '@/lib/axiosConfig';

/**
 * CineTrip 상세 모달용 "이 지역 무장애 스팟" 스트립.
 *
 * <p>한 번의 API 호출({@code GET /api/v1/cine-trip/region/:areaCode/accessible})로
 * 관광지/음식점/숙박 3 버킷을 받아와 탭 UI 로 전환 노출.
 *
 * <p>접근성 칩:
 * - 휠체어/엘리베이터 (hasPhysicalAccess)
 * - 시각장애 편의 (hasVisualAccess)
 * - 청각장애 편의 (hasHearingAccess)
 * - 가족/유아 동반 (hasFamilyAccess)
 *
 * 백엔드가 빈 버킷(KorWithService2 미설정/응답 0건)을 반환하면 컴포넌트 자체를 숨긴다.
 */
const BUCKET_META = {
  attractions: { label: '관광지', icon: MapPin, color: '#10b981' },
  restaurants: { label: '음식점', icon: UtensilsCrossed, color: '#f97316' },
  accommodations: { label: '숙박', icon: Hotel, color: '#6366f1' },
};
const BUCKET_ORDER = ['attractions', 'restaurants', 'accommodations'];

export default function AccessibleSpotsStrip({ areaCode, regionLabel = '' }) {
  const [buckets, setBuckets] = useState({});
  const [activeBucket, setActiveBucket] = useState('attractions');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!areaCode) return;
    let alive = true;
    (async () => {
      setLoading(true);
      try {
        const res = await axios.get(
          `/api/v1/cine-trip/region/${encodeURIComponent(areaCode)}/accessible?perBucket=6`
        );
        const payload = res?.data?.data ?? {};
        if (alive) setBuckets(payload || {});
      } catch (e) {
        console.error('[accessible-spots] fetch failed:', e?.message || e);
        if (alive) setBuckets({});
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => {
      alive = false;
    };
  }, [areaCode]);

  const totalCount = BUCKET_ORDER.reduce(
    (acc, k) => acc + ((buckets?.[k] || []).length || 0),
    0
  );
  if (!loading && totalCount === 0) {
    return null;
  }

  const activeList = buckets?.[activeBucket] || [];

  return (
    <section style={{ marginTop: 16, marginBottom: 8 }}>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 10,
          marginBottom: 10,
          flexWrap: 'wrap',
        }}
      >
        <Accessibility size={18} style={{ color: '#22d3ee' }} />
        <h4 style={{ fontSize: 15, fontWeight: 700, color: '#fff', margin: 0 }}>
          {regionLabel ? `${regionLabel} 무장애 여행 스팟` : '무장애 여행 스팟'}
        </h4>
        <span style={{ fontSize: 11, color: '#888' }}>한국관광공사 무장애 여행정보</span>
      </div>

      {/* 탭 */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 12, flexWrap: 'wrap' }}>
        {BUCKET_ORDER.map((k) => {
          const meta = BUCKET_META[k];
          const Icon = meta.icon;
          const count = (buckets?.[k] || []).length;
          const active = activeBucket === k;
          return (
            <button
              key={k}
              type="button"
              onClick={() => setActiveBucket(k)}
              disabled={count === 0 && !loading}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: 6,
                padding: '6px 12px',
                borderRadius: 18,
                border: active
                  ? `1px solid ${meta.color}`
                  : '1px solid rgba(255,255,255,0.12)',
                background: active
                  ? `${meta.color}22`
                  : count === 0
                  ? 'rgba(255,255,255,0.02)'
                  : 'rgba(255,255,255,0.05)',
                color: count === 0 && !loading ? '#555' : '#e5e7eb',
                fontSize: 12,
                fontWeight: 600,
                cursor: count === 0 && !loading ? 'not-allowed' : 'pointer',
                transition: 'all 0.15s',
              }}
            >
              <Icon size={13} style={{ color: active ? meta.color : undefined }} />
              {meta.label}
              <span style={{ fontSize: 10, opacity: 0.7 }}>({count})</span>
            </button>
          );
        })}
      </div>

      {loading ? (
        <div
          style={{
            display: 'flex',
            gap: 10,
            overflowX: 'auto',
            paddingBottom: 6,
          }}
        >
          {Array.from({ length: 4 }).map((_, i) => (
            <div
              key={i}
              style={{
                flex: '0 0 auto',
                width: 220,
                height: 150,
                borderRadius: 12,
                background:
                  'linear-gradient(90deg, #1a1a1a 0%, #2a2a2a 50%, #1a1a1a 100%)',
                backgroundSize: '200% 100%',
                animation: 'cinetrip-shimmer 1.5s infinite',
              }}
            />
          ))}
        </div>
      ) : activeList.length === 0 ? (
        <div
          style={{
            color: '#666',
            fontSize: 13,
            padding: '12px 0',
            textAlign: 'center',
          }}
        >
          이 카테고리에 등록된 무장애 정보가 없어요.
        </div>
      ) : (
        <div
          style={{
            display: 'flex',
            gap: 10,
            overflowX: 'auto',
            paddingBottom: 6,
            scrollbarWidth: 'thin',
          }}
        >
          {activeList.map((poi, idx) => (
            <PoiCard key={poi.contentId || idx} poi={poi} bucket={activeBucket} />
          ))}
        </div>
      )}
    </section>
  );
}

function PoiCard({ poi, bucket }) {
  const meta = BUCKET_META[bucket];
  const Icon = meta.icon;
  const addr = poi.addr1 || poi.addr2 || '';

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      style={{
        flex: '0 0 auto',
        width: 240,
        borderRadius: 12,
        border: '1px solid rgba(255,255,255,0.08)',
        background: '#0f0f0f',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <div
        style={{
          width: '100%',
          height: 110,
          background: '#1a1a1a',
          position: 'relative',
        }}
      >
        {poi.firstImageThumb || poi.firstImage ? (
          <img
            src={poi.firstImageThumb || poi.firstImage}
            alt={poi.title || 'poi'}
            loading="lazy"
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
              display: 'block',
            }}
            onError={(e) => {
              e.target.style.display = 'none';
            }}
          />
        ) : (
          <div
            style={{
              width: '100%',
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#444',
            }}
          >
            <Icon size={32} />
          </div>
        )}
        <span
          style={{
            position: 'absolute',
            top: 8,
            left: 8,
            fontSize: 10,
            fontWeight: 700,
            padding: '3px 8px',
            borderRadius: 10,
            background: meta.color,
            color: '#fff',
          }}
        >
          {meta.label}
        </span>
      </div>
      <div style={{ padding: '10px 12px', flex: 1, display: 'flex', flexDirection: 'column' }}>
        <h5
          style={{
            margin: 0,
            fontSize: 13,
            fontWeight: 700,
            color: '#fff',
            lineHeight: 1.35,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
          }}
        >
          {poi.title || '이름 없음'}
        </h5>
        {addr && (
          <p
            style={{
              margin: '4px 0 0',
              fontSize: 11,
              color: '#9ca3af',
              display: 'flex',
              alignItems: 'center',
              gap: 4,
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            }}
          >
            <MapPin size={11} style={{ color: '#6b7280', flexShrink: 0 }} />
            {addr}
          </p>
        )}
        {poi.tel && (
          <p
            style={{
              margin: '2px 0 0',
              fontSize: 11,
              color: '#6ee7b7',
              display: 'flex',
              alignItems: 'center',
              gap: 4,
            }}
          >
            <Phone size={11} style={{ flexShrink: 0 }} /> {poi.tel}
          </p>
        )}
        <AccessibilityChips poi={poi} />
      </div>
    </motion.div>
  );
}

function AccessibilityChips({ poi }) {
  const chips = [];
  if (poi.physicalAccess)
    chips.push({ key: 'phys', label: '휠체어', icon: Accessibility, color: '#22d3ee' });
  if (poi.visualAccess)
    chips.push({ key: 'vis', label: '시각', icon: Eye, color: '#a855f7' });
  if (poi.hearingAccess)
    chips.push({ key: 'hear', label: '청각', icon: Ear, color: '#f472b6' });
  if (poi.familyAccess)
    chips.push({ key: 'fam', label: '가족', icon: Baby, color: '#fbbf24' });

  if (chips.length === 0) {
    return (
      <p style={{ margin: '8px 0 0', fontSize: 10, color: '#555' }}>접근성 정보 없음</p>
    );
  }

  return (
    <div
      style={{
        marginTop: 8,
        display: 'flex',
        flexWrap: 'wrap',
        gap: 4,
      }}
    >
      {chips.map((c) => {
        const Icon = c.icon;
        return (
          <span
            key={c.key}
            title={`${c.label} 편의 제공`}
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: 3,
              fontSize: 10,
              fontWeight: 600,
              padding: '2px 6px',
              borderRadius: 8,
              background: `${c.color}22`,
              color: c.color,
              border: `1px solid ${c.color}44`,
            }}
          >
            <Icon size={9} />
            {c.label}
          </span>
        );
      })}
    </div>
  );
}
