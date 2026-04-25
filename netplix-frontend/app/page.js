'use client';

import React, { useEffect, useState, useRef } from "react";
import { useRouter } from "next/navigation";
import { useTranslation } from "react-i18next";
import { motion } from "framer-motion";
import { Film, Info, Star, Sparkles, Crown, ArrowRight, MapPin } from "lucide-react";

function Main() {
  const router = useRouter();
  const { t } = useTranslation();
  const [gradientIndex, setGradientIndex] = useState(0);

  const gradients = [
    "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
    "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)",
    "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)",
    "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)",
  ];

  // 로그인 여부와 무관하게 메인(랜딩) 페이지를 항상 첫 화면으로 노출한다.
  // 로그인된 사용자는 아래의 "둘러보기" 버튼을 통해 /dashboard 로 진입할 수 있고,
  // 비로그인 사용자는 로그인/회원가입 버튼을 통해 진입할 수 있다.

  // Animated gradient background cycle
  useEffect(() => {
    const interval = setInterval(() => {
      setGradientIndex((prev) => (prev + 1) % gradients.length);
    }, 4000);
    return () => clearInterval(interval);
  }, []);

  const [particles, setParticles] = useState([]);
  useEffect(() => {
    setParticles([...Array(50)].map((_, i) => ({
      id: i,
      left: Math.random() * 100,
      top: Math.random() * 100,
      size: Math.random() * 3 + 1,
      duration: Math.random() * 3 + 2,
      delay: Math.random() * 2,
    })));
  }, []);

  return (
    <div style={{ width: "100%", position: "relative", minHeight: "100vh", overflow: "hidden" }}>
      {/* Animated CSS */}
      <style>{`
        @keyframes animate-gradient {
          0%, 100% { background-position: 0% 50%; }
          50% { background-position: 100% 50%; }
        }
        @keyframes twinkle {
          0%, 100% { opacity: 0; transform: scale(0.5); }
          50% { opacity: 1; transform: scale(1); }
        }
        @keyframes float {
          0%, 100% { transform: translateY(0px); }
          50% { transform: translateY(-10px); }
        }
        .gradient-text {
          background: linear-gradient(90deg, #a855f7, #ec4899, #6366f1, #a855f7);
          background-size: 300% 100%;
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          animation: animate-gradient 6s ease infinite;
        }
        .btn-hover:hover {
          transform: translateY(-2px);
          box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3) !important;
        }
      `}</style>

      {/* Animated Gradient Background */}
      <motion.div
        style={{
          position: "absolute",
          inset: 0,
          background: gradients[gradientIndex],
        }}
        animate={{ background: gradients[gradientIndex] }}
        transition={{ duration: 2, ease: "easeInOut" }}
      />

      {/* Dark Overlay */}
      <div style={{ position: "absolute", inset: 0, background: "rgba(0, 0, 0, 0.2)" }} />

      {/* Sparkle Particles */}
      {particles.map((particle) => (
        <motion.div
          key={particle.id}
          style={{
            position: "absolute",
            left: `${particle.left}%`,
            top: `${particle.top}%`,
            width: `${particle.size}px`,
            height: `${particle.size}px`,
            background: "#fff",
            borderRadius: "50%",
            boxShadow: "0 0 6px 2px rgba(255, 255, 255, 0.5)",
          }}
          animate={{
            opacity: [0, 1, 0],
            scale: [0.5, 1, 0.5],
          }}
          transition={{
            duration: particle.duration,
            repeat: Infinity,
            delay: particle.delay,
            ease: "easeInOut",
          }}
        />
      ))}

      {/* Owner Shortcut Button (top-right) */}
      <motion.button
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, delay: 0.3 }}
        whileHover={{ scale: 1.05, y: -2 }}
        whileTap={{ scale: 0.95 }}
        onClick={() => router.push("/admin")}
        aria-label={t("nav.owner")}
        style={{
          position: "fixed",
          top: "28px",
          right: "80px",
          zIndex: 50,
          display: "inline-flex",
          alignItems: "center",
          gap: "6px",
          padding: "8px 14px",
          background: "linear-gradient(135deg, rgba(251, 191, 36, 0.95), rgba(245, 158, 11, 0.95))",
          color: "#3f2d00",
          border: "1px solid rgba(255, 255, 255, 0.35)",
          borderRadius: "999px",
          fontSize: "13px",
          fontWeight: 700,
          cursor: "pointer",
          backdropFilter: "blur(8px)",
          WebkitBackdropFilter: "blur(8px)",
          boxShadow: "0 6px 18px rgba(245, 158, 11, 0.35)",
        }}
      >
        <Crown size={14} strokeWidth={2.5} />
        <span>{t("nav.owner")}</span>
      </motion.button>

      {/* Main Content */}
      <div
        style={{
          position: "relative",
          zIndex: 10,
          display: "flex",
          minHeight: "100vh",
          alignItems: "center",
          justifyContent: "center",
          padding: "16px",
        }}
      >
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          style={{ width: "100%", maxWidth: "640px" }}
        >
          {/* Glass Card */}
          <div
            style={{
              backdropFilter: "blur(20px)",
              background: "rgba(255, 255, 255, 0.1)",
              border: "1px solid rgba(255, 255, 255, 0.2)",
              borderRadius: "24px",
              boxShadow: "0 25px 50px -12px rgba(0, 0, 0, 0.5)",
              padding: "40px 32px",
            }}
          >
            <div style={{ display: "flex", flexDirection: "column", gap: "32px" }}>
              {/* Logo Section */}
              <motion.div
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  gap: "12px",
                }}
                initial={{ scale: 0.8 }}
                animate={{ scale: 1 }}
                transition={{ duration: 0.5, delay: 0.2 }}
              >
                <div
                  style={{
                    width: "56px",
                    height: "56px",
                    borderRadius: "16px",
                    background: "rgba(255, 255, 255, 0.15)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    animation: "float 3s ease-in-out infinite",
                  }}
                >
                  <Film size={36} color="#fff" />
                </div>
                <span
                  className="gradient-text"
                  style={{
                    fontSize: "clamp(36px, 8vw, 52px)",
                    fontWeight: 800,
                    letterSpacing: "-1px",
                  }}
                >
                  Touraz Holic
                </span>
              </motion.div>

              {/* Tagline */}
              <motion.div
                style={{ textAlign: "center" }}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.4 }}
              >
                <h2
                  style={{
                    fontSize: "clamp(20px, 5vw, 28px)",
                    fontWeight: 600,
                    color: "#fff",
                    marginBottom: "12px",
                  }}
                >
                  {t("main.tagline")}
                </h2>
                <p
                  style={{
                    color: "rgba(255, 255, 255, 0.8)",
                    fontSize: "16px",
                    lineHeight: 1.6,
                  }}
                >
                  {t("main.subtitle")}
                </p>
              </motion.div>

              {/* Buttons */}
              <motion.div
                style={{ display: "flex", flexDirection: "column", gap: "14px" }}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.6 }}
              >
                {/* Browse Button */}
                <div style={{ width: "100%" }}>
                  <motion.button
                    className="btn-hover"
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    onClick={() => router.push("/dashboard")}
                    style={{
                      width: "100%",
                      height: "56px",
                      fontSize: "17px",
                      fontWeight: 700,
                      background: "linear-gradient(135deg, #0ea5e9, #06b6d4)",
                      color: "#fff",
                      border: "none",
                      borderRadius: "14px",
                      cursor: "pointer",
                      boxShadow: "0 4px 20px rgba(14, 165, 233, 0.4)",
                      transition: "all 0.3s ease",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      gap: "8px",
                    }}
                  >
                    {t("main.browseWithoutLogin")}
                  </motion.button>
                  <p style={{ margin: "6px 0 0", fontSize: "12px", color: "rgba(255,255,255,0.7)", textAlign: "center" }}>
                    {t("main.browseDesc")}
                  </p>
                </div>

                {/* Login Button */}
                <motion.button
                  className="btn-hover"
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  onClick={() => router.push("/login")}
                  style={{
                    width: "100%",
                    height: "56px",
                    fontSize: "17px",
                    fontWeight: 700,
                    background: "linear-gradient(135deg, #ec4899, #db2777)",
                    color: "#fff",
                    border: "none",
                    borderRadius: "14px",
                    cursor: "pointer",
                    boxShadow: "0 4px 20px rgba(236, 72, 153, 0.4)",
                    transition: "all 0.3s ease",
                  }}
                >
                  {t("main.login")}
                </motion.button>

                {/* Signup Button */}
                <motion.button
                  className="btn-hover"
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  onClick={() => router.push("/signup")}
                  style={{
                    width: "100%",
                    height: "56px",
                    fontSize: "17px",
                    fontWeight: 700,
                    background: "linear-gradient(135deg, #6366f1, #8b5cf6)",
                    color: "#fff",
                    border: "none",
                    borderRadius: "14px",
                    cursor: "pointer",
                    boxShadow: "0 4px 20px rgba(99, 102, 241, 0.4)",
                    transition: "all 0.3s ease",
                  }}
                >
                  {t("main.signup")}
                </motion.button>
              </motion.div>

              {/* Feature Icons */}
              <motion.div
                style={{
                  display: "grid",
                  gridTemplateColumns: "repeat(3, 1fr)",
                  gap: "16px",
                  paddingTop: "24px",
                  borderTop: "1px solid rgba(255, 255, 255, 0.2)",
                }}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.8 }}
              >
                <div
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    gap: "10px",
                    color: "#fff",
                  }}
                >
                  <div
                    style={{
                      width: "52px",
                      height: "52px",
                      borderRadius: "50%",
                      background: "rgba(255, 255, 255, 0.1)",
                      backdropFilter: "blur(8px)",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    <Info size={24} />
                  </div>
                  <span style={{ fontSize: "14px", fontWeight: 500 }}>{t("main.ottInfo")}</span>
                </div>

                <div
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    gap: "10px",
                    color: "#fff",
                  }}
                >
                  <div
                    style={{
                      width: "52px",
                      height: "52px",
                      borderRadius: "50%",
                      background: "rgba(255, 255, 255, 0.1)",
                      backdropFilter: "blur(8px)",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    <Star size={24} />
                  </div>
                  <span style={{ fontSize: "14px", fontWeight: 500 }}>{t("main.rating")}</span>
                </div>

                <div
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    gap: "10px",
                    color: "#fff",
                  }}
                >
                  <div
                    style={{
                      width: "52px",
                      height: "52px",
                      borderRadius: "50%",
                      background: "rgba(255, 255, 255, 0.1)",
                      backdropFilter: "blur(8px)",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    <Sparkles size={24} />
                  </div>
                  <span style={{ fontSize: "14px", fontWeight: 500 }}>{t("main.recommend")}</span>
                </div>
              </motion.div>
            </div>
          </div>

          {/* CTA — TarRlteTarService1 기반 /related-spots 진입 (랜딩 푸터 직전) */}
          <motion.section
            aria-label="조용한 명소 옆, 사람들은 어디로 갔을까"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.9 }}
            style={{
              marginTop: 28,
              position: "relative",
              borderRadius: 24,
              overflow: "hidden",
              border: "1px solid rgba(165,180,252,0.28)",
              background:
                "radial-gradient(120% 140% at 0% 0%, rgba(99,102,241,0.28), transparent 60%), radial-gradient(120% 140% at 100% 100%, rgba(236,72,153,0.22), transparent 60%), linear-gradient(160deg, rgba(15,23,42,0.92), rgba(8,12,28,0.96))",
              boxShadow:
                "0 24px 60px rgba(0,0,0,0.45), 0 0 0 1px rgba(255,255,255,0.05) inset",
            }}
          >
            <div
              aria-hidden
              style={{
                position: "absolute",
                inset: 0,
                backgroundImage:
                  "radial-gradient(rgba(255,255,255,0.08) 1px, transparent 1px)",
                backgroundSize: "20px 20px",
                opacity: 0.4,
                pointerEvents: "none",
              }}
            />
            <div
              aria-hidden
              style={{
                position: "absolute",
                top: -110,
                left: "50%",
                transform: "translateX(-50%)",
                width: 460,
                height: 220,
                borderRadius: "50%",
                background:
                  "radial-gradient(closest-side, rgba(165,180,252,0.32), transparent)",
                filter: "blur(8px)",
                pointerEvents: "none",
              }}
            />
            <div
              style={{
                position: "relative",
                zIndex: 1,
                padding: "clamp(36px, 7vw, 56px) clamp(20px, 4vw, 36px)",
                textAlign: "center",
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: 14,
              }}
            >
              <div
                style={{
                  display: "inline-flex",
                  alignItems: "center",
                  gap: 8,
                  padding: "8px 16px",
                  borderRadius: 999,
                  background:
                    "linear-gradient(135deg, rgba(99,102,241,0.22), rgba(236,72,153,0.22))",
                  border: "1px solid rgba(165,180,252,0.4)",
                  color: "#c7d2fe",
                  fontSize: 12,
                  fontWeight: 700,
                  letterSpacing: 0.6,
                  textTransform: "uppercase",
                }}
              >
                <Sparkles size={14} />
                한국관광공사 빅데이터 · 함께 다녀간 곳
              </div>

              <h3
                style={{
                  margin: "8px 0 0",
                  fontSize: "clamp(24px, 5.6vw, 36px)",
                  fontWeight: 900,
                  lineHeight: 1.25,
                  letterSpacing: "-0.02em",
                  background:
                    "linear-gradient(120deg, #fef3c7 0%, #fda4af 45%, #c4b5fd 100%)",
                  WebkitBackgroundClip: "text",
                  WebkitTextFillColor: "transparent",
                  backgroundClip: "text",
                }}
              >
                조용한 명소 옆,
                <br />
                사람들은 어디로 갔을까
              </h3>

              <p
                style={{
                  margin: 0,
                  color: "#cbd5e1",
                  fontSize: "clamp(13px, 2.2vw, 15px)",
                  lineHeight: 1.8,
                  maxWidth: 480,
                }}
              >
                한 곳을 떠올려 보세요.
                <br />
                그 곁을 거닐던 사람들이 다음으로 향한 자리들을
                <br />
                데이터가 잔잔히 보여드릴게요.
              </p>

              <div
                style={{
                  display: "flex",
                  flexWrap: "wrap",
                  justifyContent: "center",
                  gap: 8,
                  marginTop: 2,
                }}
              >
                {[
                  "키워드로 따라가기",
                  "광역 17곳 빠른 탐색",
                  "함께 방문 빈도 1위부터",
                ].map((tag) => (
                  <span
                    key={tag}
                    style={{
                      display: "inline-flex",
                      alignItems: "center",
                      gap: 6,
                      padding: "6px 12px",
                      borderRadius: 999,
                      background: "rgba(255,255,255,0.06)",
                      border: "1px solid rgba(255,255,255,0.12)",
                      color: "#e2e8f0",
                      fontSize: 12,
                    }}
                  >
                    <MapPin size={12} />
                    {tag}
                  </span>
                ))}
              </div>

              <motion.button
                type="button"
                whileHover={{ scale: 1.03, y: -2 }}
                whileTap={{ scale: 0.98 }}
                onClick={() => router.push("/related-spots")}
                style={{
                  marginTop: 16,
                  padding: "16px 32px",
                  borderRadius: 999,
                  border: "none",
                  cursor: "pointer",
                  fontSize: "clamp(15px, 2.4vw, 17px)",
                  fontWeight: 800,
                  color: "#fff",
                  display: "inline-flex",
                  alignItems: "center",
                  gap: 10,
                  background:
                    "linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%)",
                  boxShadow:
                    "0 14px 40px rgba(139,92,246,0.5), 0 6px 16px rgba(236,72,153,0.32)",
                }}
              >
                잔잔히 둘러보기
                <ArrowRight size={18} />
              </motion.button>

              <div
                style={{
                  marginTop: 4,
                  fontSize: 11,
                  color: "rgba(203,213,225,0.65)",
                  letterSpacing: 0.2,
                }}
              >
                데이터 출처 · 한국관광공사 TarRlteTarService1
              </div>
            </div>
          </motion.section>

          {/* Footer */}
          <motion.p
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 1 }}
            style={{
              textAlign: "center",
              color: "rgba(255, 255, 255, 0.5)",
              fontSize: "12px",
              marginTop: "24px",
            }}
          >
            © 2025 Touraz Holic. All rights reserved.
          </motion.p>
        </motion.div>
      </div>
    </div>
  );
}

export default Main;
