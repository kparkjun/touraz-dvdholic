import React from "react";
import { useTranslation } from "react-i18next";
import { motion } from "framer-motion";

function FilmReel({ size = 64, color = "#ec4899" }) {
  return (
    <svg width={size} height={size} viewBox="0 0 64 64" fill="none">
      <circle cx="32" cy="32" r="28" stroke={color} strokeWidth="3" strokeDasharray="6 4" opacity="0.3" />
      <circle cx="32" cy="32" r="20" stroke={color} strokeWidth="2.5" opacity="0.6" />
      <circle cx="32" cy="32" r="5" fill={color} opacity="0.9" />
      {[0, 60, 120, 180, 240, 300].map((angle) => {
        const rad = (angle * Math.PI) / 180;
        const cx = 32 + 14 * Math.cos(rad);
        const cy = 32 + 14 * Math.sin(rad);
        return <circle key={angle} cx={cx} cy={cy} r="3.5" fill={color} opacity="0.5" />;
      })}
    </svg>
  );
}

export default function OAuthLoadingOverlay() {
  const { t } = useTranslation();
  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        zIndex: 9999,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        background:
          "radial-gradient(ellipse 900px 500px at 30% 20%, rgba(236,72,153,0.15), transparent)," +
          "radial-gradient(ellipse 800px 400px at 80% 80%, rgba(59,130,246,0.12), transparent)," +
          "#09090b",
      }}
    >
      <style>{`
        @keyframes reelSpin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
        @keyframes pulse-glow {
          0%, 100% { box-shadow: 0 0 20px rgba(236,72,153,0.2), 0 0 60px rgba(139,92,246,0.1); }
          50% { box-shadow: 0 0 30px rgba(236,72,153,0.35), 0 0 80px rgba(139,92,246,0.2); }
        }
      `}</style>

      <motion.div
        initial={{ opacity: 0, scale: 0.8 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.5 }}
        style={{
          position: "relative",
          width: 120,
          height: 120,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          marginBottom: 28,
        }}
      >
        <div
          style={{
            position: "absolute",
            inset: 0,
            borderRadius: "50%",
            animation: "pulse-glow 2s ease-in-out infinite",
          }}
        />
        <div style={{ animation: "reelSpin 3s linear infinite" }}>
          <FilmReel size={80} color="#ec4899" />
        </div>
        <div
          style={{
            position: "absolute",
            animation: "reelSpin 5s linear infinite reverse",
            opacity: 0.3,
          }}
        >
          <FilmReel size={110} color="#8b5cf6" />
        </div>
      </motion.div>

      <motion.span
        initial={{ opacity: 0 }}
        animate={{ opacity: [0.4, 1, 0.4] }}
        transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
        style={{
          color: "#a1a1aa",
          fontSize: 14,
          fontWeight: 500,
          letterSpacing: "0.03em",
        }}
      >
        {t("login.processingLogin")}
      </motion.span>
    </div>
  );
}
