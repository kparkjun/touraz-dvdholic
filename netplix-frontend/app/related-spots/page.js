'use client';

import React, { Suspense, useEffect, useMemo, useRef, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { motion, AnimatePresence } from 'framer-motion';
import { Sparkles, MapPin, Wind, Loader2, ArrowRight, Hash } from 'lucide-react';
import axios from '@/lib/axiosConfig';

// "조용한 명소 + 함께 가는 명소" — 잔잔한 데이터 산책 화면.
// TarRlteTarService1 의 두 가지 모드를 한 화면에서 제공:
//  1) 키워드 검색 (/api/v1/tour/related/grouped/keyword) — 사용자가 떠올린 작은 동네 이름
//  2) 광역 빠른 선택 (/api/v1/tour/related/area)        — 무엇을 검색할지 모를 때의 길잡이

const AREA_OPTIONS = [
  { code: '1',  name: '서울' },
  { code: '6',  name: '부산' },
  { code: '4',  name: '대구' },
  { code: '2',  name: '인천' },
  { code: '5',  name: '광주' },
  { code: '3',  name: '대전' },
  { code: '7',  name: '울산' },
  { code: '8',  name: '세종' },
  { code: '31', name: '경기' },
  { code: '32', name: '강원' },
  { code: '33', name: '충북' },
  { code: '34', name: '충남' },
  { code: '35', name: '경북' },
  { code: '36', name: '경남' },
  { code: '37', name: '전북' },
  { code: '38', name: '전남' },
  { code: '39', name: '제주' },
];

const SUGGESTED_KEYWORDS = ['한라산', '경주', '강릉', '여수', '담양', '안동', '통영', '양양'];

function RelatedSpotsInner() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const initialKeyword = searchParams.get('q') || '';
  const initialArea = searchParams.get('area') || '';

  const [mode, setMode] = useState(initialKeyword ? 'keyword' : (initialArea ? 'area' : 'keyword'));
  const [keyword, setKeyword] = useState(initialKeyword);
  const [areaCode, setAreaCode] = useState(initialArea);

  const [groups, setGroups] = useState([]);
  const [areaItems, setAreaItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [touched, setTouched] = useState(Boolean(initialKeyword || initialArea));
  const [error, setError] = useState(null);
  const lastReqRef = useRef(0);

  const runKeywordSearch = async (q) => {
    const trimmed = (q || '').trim();
    if (!trimmed) return;
    const reqId = ++lastReqRef.current;
    setLoading(true);
    setError(null);
    setTouched(true);
    setMode('keyword');
    try {
      const res = await axios.get(`/api/v1/tour/related/grouped/keyword`, {
        params: { q: trimmed, limit: 60 },
      });
      if (reqId !== lastReqRef.current) return;
      const data = res?.data?.data ?? [];
      setGroups(Array.isArray(data) ? data : []);
      setAreaItems([]);
    } catch (e) {
      if (reqId !== lastReqRef.current) return;
      console.error('[related-spots] keyword failed', e?.message || e);
      setError('데이터를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
      setGroups([]);
    } finally {
      if (reqId === lastReqRef.current) setLoading(false);
    }
  };

  const runAreaSearch = async (code) => {
    if (!code) return;
    const reqId = ++lastReqRef.current;
    setLoading(true);
    setError(null);
    setTouched(true);
    setMode('area');
    try {
      const res = await axios.get(`/api/v1/tour/related/area`, {
        params: { areaCode: code, limit: 40 },
      });
      if (reqId !== lastReqRef.current) return;
      const data = res?.data?.data ?? [];
      setAreaItems(Array.isArray(data) ? data : []);
      setGroups([]);
    } catch (e) {
      if (reqId !== lastReqRef.current) return;
      console.error('[related-spots] area failed', e?.message || e);
      setError('데이터를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
      setAreaItems([]);
    } finally {
      if (reqId === lastReqRef.current) setLoading(false);
    }
  };

  useEffect(() => {
    if (initialKeyword) runKeywordSearch(initialKeyword);
    else if (initialArea) runAreaSearch(initialArea);
  // 초기 1회만 실행. 이후엔 사용자 액션으로 호출.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const totalRelated = useMemo(
    () => groups.reduce((acc, g) => acc + (g.related?.length || 0), 0),
    [groups]
  );

  return (
    <div
      style={{
        minHeight: '100vh',
        width: '100%',
        background:
          'radial-gradient(900px 540px at 15% -10%, rgba(99,102,241,0.18), transparent), radial-gradient(900px 540px at 100% 10%, rgba(236,72,153,0.12), transparent), radial-gradient(140% 140% at 50% 110%, #04060c 40%, #0c1330 100%)',
        color: '#f5f5f5',
        padding: '36px 16px 80px',
      }}
    >
      <div style={{ maxWidth: 980, margin: '0 auto' }}>
        {/* Hero */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          style={{ textAlign: 'center', marginBottom: 28 }}
        >
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: 8,
              padding: '6px 14px',
              borderRadius: 999,
              background: 'rgba(255,255,255,0.04)',
              border: '1px solid rgba(255,255,255,0.08)',
              fontSize: 12,
              color: '#a5b4fc',
              marginBottom: 14,
            }}
          >
            <Sparkles size={14} />
            한국관광공사 빅데이터 · 함께 다녀간 곳
          </div>
          <h1
            style={{
              fontSize: 'clamp(24px, 5vw, 34px)',
              fontWeight: 800,
              lineHeight: 1.25,
              margin: 0,
              background: 'linear-gradient(120deg, #fef3c7 0%, #fda4af 50%, #c4b5fd 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              backgroundClip: 'text',
            }}
          >
            조용한 명소 옆,<br />사람들은 어디로 갔을까
          </h1>
          <p style={{ marginTop: 10, color: '#cbd5e1', fontSize: 14, lineHeight: 1.7 }}>
            한 곳을 떠올려 보세요. 그 곁을 거닐던 사람들이<br />다음으로 향한 자리들을 데이터가 잔잔히 보여드릴게요.
          </p>
        </motion.div>

        {/* Mode tabs */}
        <div style={{ display: 'flex', gap: 8, justifyContent: 'center', marginBottom: 14 }}>
          <ModeTab active={mode === 'keyword'} onClick={() => setMode('keyword')}>
            <Wind size={14} /> 한 곳에서 시작
          </ModeTab>
          <ModeTab active={mode === 'area'} onClick={() => setMode('area')}>
            <MapPin size={14} /> 지역에서 시작
          </ModeTab>
        </div>

        {/* Inputs */}
        <motion.div
          key={mode}
          initial={{ opacity: 0, y: 6 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35 }}
          style={{
            background: 'rgba(255,255,255,0.04)',
            border: '1px solid rgba(255,255,255,0.08)',
            borderRadius: 16,
            padding: 14,
            marginBottom: 18,
          }}
        >
          {mode === 'keyword' ? (
            <div>
              <div style={{ display: 'flex', gap: 8, alignItems: 'stretch' }}>
                <input
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  onKeyDown={(e) => { if (e.key === 'Enter') runKeywordSearch(keyword); }}
                  placeholder="조용한 동네 이름이나 떠오르는 한 곳을 적어주세요"
                  style={{
                    flex: 1,
                    minWidth: 0,
                    padding: '12px 14px',
                    borderRadius: 12,
                    border: '1px solid rgba(255,255,255,0.1)',
                    background: 'rgba(0,0,0,0.25)',
                    color: '#fff',
                    fontSize: 14,
                    outline: 'none',
                  }}
                />
                <button
                  type="button"
                  onClick={() => runKeywordSearch(keyword)}
                  disabled={loading || !keyword.trim()}
                  style={{
                    padding: '0 18px',
                    borderRadius: 12,
                    border: 'none',
                    cursor: loading || !keyword.trim() ? 'not-allowed' : 'pointer',
                    background: keyword.trim()
                      ? 'linear-gradient(135deg, #6366f1, #ec4899)'
                      : 'rgba(255,255,255,0.06)',
                    color: keyword.trim() ? '#fff' : 'rgba(255,255,255,0.4)',
                    fontWeight: 700,
                    fontSize: 14,
                    whiteSpace: 'nowrap',
                  }}
                >
                  {loading ? <Loader2 size={14} className="anim-spin" /> : '잔잔히 찾기'}
                </button>
              </div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginTop: 10 }}>
                {SUGGESTED_KEYWORDS.map((k) => (
                  <button
                    key={k}
                    type="button"
                    onClick={() => { setKeyword(k); runKeywordSearch(k); }}
                    style={{
                      fontSize: 12,
                      padding: '4px 10px',
                      borderRadius: 999,
                      border: '1px solid rgba(255,255,255,0.1)',
                      background: 'rgba(255,255,255,0.04)',
                      color: '#cbd5e1',
                      cursor: 'pointer',
                    }}
                  >
                    # {k}
                  </button>
                ))}
              </div>
            </div>
          ) : (
            <div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                {AREA_OPTIONS.map((opt) => (
                  <button
                    key={opt.code}
                    type="button"
                    onClick={() => { setAreaCode(opt.code); runAreaSearch(opt.code); }}
                    style={{
                      fontSize: 12,
                      padding: '6px 12px',
                      borderRadius: 999,
                      border: areaCode === opt.code
                        ? '1px solid rgba(165,180,252,0.7)'
                        : '1px solid rgba(255,255,255,0.1)',
                      background: areaCode === opt.code
                        ? 'linear-gradient(135deg, rgba(99,102,241,0.35), rgba(236,72,153,0.25))'
                        : 'rgba(255,255,255,0.04)',
                      color: areaCode === opt.code ? '#fff' : '#cbd5e1',
                      cursor: 'pointer',
                      fontWeight: 600,
                    }}
                  >
                    {opt.name}
                  </button>
                ))}
              </div>
            </div>
          )}
        </motion.div>

        {/* Empty / loading / error */}
        {loading && (
          <div style={{ textAlign: 'center', color: '#a5b4fc', padding: '24px 0' }}>
            <Loader2 size={20} className="anim-spin" /> 데이터로 길을 잇는 중…
          </div>
        )}
        {!loading && error && (
          <div style={{ textAlign: 'center', color: '#fca5a5', padding: '16px 0' }}>{error}</div>
        )}
        {!loading && !error && !touched && (
          <EmptyHint />
        )}
        {!loading && !error && touched && mode === 'keyword' && groups.length === 0 && (
          <NoResult onFallback={() => router.push('/cine-trip')} />
        )}
        {!loading && !error && touched && mode === 'area' && areaItems.length === 0 && (
          <NoResult onFallback={() => router.push('/cine-trip')} />
        )}

        {/* Keyword groups */}
        <AnimatePresence>
          {!loading && !error && mode === 'keyword' && groups.length > 0 && (
            <motion.div
              key="kw-result"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              style={{ display: 'flex', flexDirection: 'column', gap: 14 }}
            >
              <div style={{ color: '#94a3b8', fontSize: 12, textAlign: 'center' }}>
                기준 명소 {groups.length}곳 · 함께 다녀간 자리 {totalRelated}곳을 찾았어요
              </div>
              {groups.map((g, idx) => (
                <GroupCard key={`${g.baseSpot}-${idx}`} group={g} />
              ))}
            </motion.div>
          )}
        </AnimatePresence>

        {/* Area items */}
        <AnimatePresence>
          {!loading && !error && mode === 'area' && areaItems.length > 0 && (
            <motion.div
              key="area-result"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 12 }}
            >
              {areaItems.map((item, idx) => (
                <AreaCard key={`${item.baseSpot}-${item.relatedSpot}-${idx}`} item={item} />
              ))}
            </motion.div>
          )}
        </AnimatePresence>

        <div style={{ marginTop: 36, textAlign: 'center' }}>
          <button
            type="button"
            onClick={() => router.push('/cine-trip')}
            style={{
              padding: '10px 18px',
              borderRadius: 999,
              border: '1px solid rgba(255,255,255,0.12)',
              background: 'rgba(255,255,255,0.04)',
              color: '#cbd5e1',
              fontSize: 13,
              cursor: 'pointer',
              display: 'inline-flex',
              alignItems: 'center',
              gap: 6,
            }}
          >
            영화로 떠나는 여행으로 가기 <ArrowRight size={14} />
          </button>
        </div>
      </div>

      <style jsx>{`
        .anim-spin {
          display: inline-block;
          animation: spin 1s linear infinite;
          vertical-align: -3px;
        }
        @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
      `}</style>
    </div>
  );
}

function ModeTab({ active, onClick, children }) {
  return (
    <button
      type="button"
      onClick={onClick}
      style={{
        padding: '8px 14px',
        borderRadius: 999,
        border: active ? '1px solid rgba(165,180,252,0.7)' : '1px solid rgba(255,255,255,0.1)',
        background: active
          ? 'linear-gradient(135deg, rgba(99,102,241,0.35), rgba(236,72,153,0.25))'
          : 'rgba(255,255,255,0.04)',
        color: active ? '#fff' : '#cbd5e1',
        fontSize: 13,
        fontWeight: 700,
        cursor: 'pointer',
        display: 'inline-flex',
        alignItems: 'center',
        gap: 6,
      }}
    >
      {children}
    </button>
  );
}

function GroupCard({ group }) {
  const router = useRouter();
  const region = [group.areaName, group.signguName].filter(Boolean).join(' · ');
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4 }}
      style={{
        background: 'linear-gradient(160deg, rgba(255,255,255,0.05), rgba(255,255,255,0.02))',
        border: '1px solid rgba(255,255,255,0.08)',
        borderRadius: 16,
        padding: 16,
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10 }}>
        <span style={{ fontSize: 11, color: '#94a3b8', letterSpacing: 0.4 }}>여기서 출발</span>
        <span style={{ fontSize: 17, fontWeight: 800, color: '#fef3c7' }}>{group.baseSpot}</span>
        {region && <span style={{ fontSize: 11, color: '#a5b4fc' }}>· {region}</span>}
      </div>
      <p style={{ margin: '0 0 12px', fontSize: 12, color: '#94a3b8' }}>
        이 곳을 다녀간 사람들이 함께 / 이어서 향한 자리들이에요.
      </p>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: 8 }}>
        {(group.related || []).map((r, i) => (
          <div
            key={`${r.relatedSpot}-${i}`}
            onClick={() => router.push(`/related-spots?q=${encodeURIComponent(r.relatedSpot || '')}`)}
            style={{
              cursor: 'pointer',
              padding: '10px 12px',
              borderRadius: 12,
              border: '1px solid rgba(255,255,255,0.08)',
              background: 'rgba(0,0,0,0.25)',
              display: 'flex',
              flexDirection: 'column',
              gap: 4,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <RankBadge rank={r.rank} />
              <span style={{ fontSize: 14, fontWeight: 700, color: '#f5f5f5' }}>{r.relatedSpot}</span>
            </div>
            <div style={{ fontSize: 11, color: '#94a3b8' }}>
              {[r.relatedAreaName, r.relatedSignguName].filter(Boolean).join(' · ') || '—'}
            </div>
            {r.category && (
              <div style={{ fontSize: 11, color: '#fca5a5', display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                <Hash size={10} /> {r.category}
              </div>
            )}
          </div>
        ))}
      </div>
    </motion.div>
  );
}

function AreaCard({ item }) {
  const router = useRouter();
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      onClick={() => router.push(`/related-spots?q=${encodeURIComponent(item.relatedSpot || item.baseSpot || '')}`)}
      style={{
        cursor: 'pointer',
        padding: 14,
        borderRadius: 14,
        border: '1px solid rgba(255,255,255,0.08)',
        background: 'linear-gradient(160deg, rgba(255,255,255,0.05), rgba(255,255,255,0.02))',
        display: 'flex',
        flexDirection: 'column',
        gap: 6,
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <RankBadge rank={item.rank} />
        <span style={{ fontSize: 11, color: '#94a3b8' }}>{item.baseSpot} 와 함께</span>
      </div>
      <div style={{ fontSize: 16, fontWeight: 800, color: '#fef3c7' }}>{item.relatedSpot}</div>
      <div style={{ fontSize: 12, color: '#a5b4fc' }}>
        {[item.relatedAreaName, item.relatedSignguName].filter(Boolean).join(' · ') || [item.areaName, item.signguName].filter(Boolean).join(' · ')}
      </div>
      {item.category && (
        <div style={{ fontSize: 11, color: '#fca5a5', display: 'inline-flex', alignItems: 'center', gap: 4 }}>
          <Hash size={10} /> {item.category}
        </div>
      )}
    </motion.div>
  );
}

function RankBadge({ rank }) {
  if (rank == null) return null;
  const palette =
    rank === 1 ? 'linear-gradient(135deg, #fbbf24, #f59e0b)' :
    rank === 2 ? 'linear-gradient(135deg, #cbd5e1, #94a3b8)' :
    rank === 3 ? 'linear-gradient(135deg, #fb923c, #f97316)' :
                 'rgba(255,255,255,0.1)';
  const fg = rank <= 3 ? '#0b1020' : '#cbd5e1';
  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        minWidth: 22,
        height: 22,
        padding: '0 6px',
        borderRadius: 999,
        background: palette,
        color: fg,
        fontSize: 11,
        fontWeight: 800,
        letterSpacing: 0.3,
      }}
      aria-label={`연관 순위 ${rank}`}
    >
      #{rank}
    </span>
  );
}

function EmptyHint() {
  return (
    <div
      style={{
        textAlign: 'center',
        color: '#94a3b8',
        padding: '24px 12px',
        fontSize: 13,
        lineHeight: 1.7,
      }}
    >
      먼저 떠올린 한 곳을 입력하거나,<br />지역을 살짝 골라 보세요.
    </div>
  );
}

function NoResult({ onFallback }) {
  return (
    <div style={{ textAlign: 'center', padding: '20px 0' }}>
      <p style={{ color: '#cbd5e1', fontSize: 13, marginBottom: 10 }}>
        그 자리에 대한 데이터는 아직 잠잠해요.
      </p>
      <button
        type="button"
        onClick={onFallback}
        style={{
          padding: '8px 14px',
          borderRadius: 999,
          border: '1px solid rgba(255,255,255,0.12)',
          background: 'rgba(255,255,255,0.04)',
          color: '#cbd5e1',
          fontSize: 12,
          cursor: 'pointer',
        }}
      >
        영화로 떠나는 여행에서 다른 길 보기
      </button>
    </div>
  );
}

export default function RelatedSpotsPage() {
  return (
    <Suspense>
      <RelatedSpotsInner />
    </Suspense>
  );
}
