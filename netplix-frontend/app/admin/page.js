'use client';
import React, { useState, useEffect } from "react";
import Link from "next/link";
import { useTranslation } from "react-i18next";
import { motion, AnimatePresence } from "framer-motion";
import {
  Lock,
  Mail,
  Eye,
  EyeOff,
  Users,
  Activity,
  Shield,
  TrendingUp,
  Search,
  MoreHorizontal,
  LogOut,
  LayoutDashboard,
} from "lucide-react";
import axios from "@/lib/axiosConfig";

function LoginForm({ onLogin }) {
  const { t } = useTranslation();
  const [adminId, setAdminId] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    if (!adminId.trim() || !password.trim()) {
      setError(t("admin.enterIdAndPassword"));
      return;
    }
    setLoading(true);
    try {
      const res = await axios.post("/api/v1/admin/login", {
        adminId: adminId.trim(),
        password,
      });
      if (res.data?.success && res.data?.data?.token) {
        localStorage.setItem("adminToken", res.data.data.token);
        onLogin();
      } else {
        setError(t("admin.loginFailed"));
      }
    } catch (err) {
      const msg = err.response?.data?.message || t("admin.loginFailed");
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="min-h-screen w-full flex items-center justify-center"
      style={{
        background: "linear-gradient(135deg, #ecfdf5 0%, #d1fae5 50%, #a7f3d0 100%)",
      }}
    >
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        style={{ width: "100%", maxWidth: 420 }}
      >
        <div
          style={{
            background: "#fff",
            borderRadius: "16px",
            boxShadow: "0 25px 50px -12px rgba(0,0,0,0.15)",
            border: "2px solid rgba(16, 185, 129, 0.2)",
            overflow: "hidden",
          }}
        >
          <div style={{ padding: "32px 24px", textAlign: "center", borderBottom: "1px solid #e5e7eb" }}>
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ delay: 0.2, type: "spring", stiffness: 200 }}
              style={{
                width: 64,
                height: 64,
                borderRadius: "16px",
                background: "linear-gradient(135deg, #10b981, #059669)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                margin: "0 auto 20px",
                boxShadow: "0 4px 14px rgba(16, 185, 129, 0.4)",
              }}
            >
              <Shield size={32} color="#fff" />
            </motion.div>
            <h1 style={{ fontSize: "24px", fontWeight: 700, color: "#047857", marginBottom: "8px" }}>
              {t("admin.ownerOnly")}
            </h1>
            <p style={{ fontSize: "14px", color: "#6b7280" }}>{t("admin.loginTitle")}</p>
          </div>
          <form onSubmit={handleLogin} style={{ padding: "24px" }}>
            <div style={{ marginBottom: "16px" }}>
              <div style={{ position: "relative" }}>
                <Mail
                  size={20}
                  style={{
                    position: "absolute",
                    left: 14,
                    top: "50%",
                    transform: "translateY(-50%)",
                    color: "#059669",
                  }}
                />
                <input
                  type="text"
                  placeholder={t("admin.id")}
                  value={adminId}
                  onChange={(e) => setAdminId(e.target.value)}
                  style={{
                    width: "100%",
                    height: 48,
                    padding: "0 14px 0 44px",
                    border: "2px solid #a7f3d0",
                    borderRadius: "10px",
                    fontSize: "15px",
                    outline: "none",
                    color: "#111827",
                    background: "#fff",
                  }}
                />
              </div>
            </div>
            <div style={{ marginBottom: "20px" }}>
              <div style={{ position: "relative" }}>
                <Lock
                  size={20}
                  style={{
                    position: "absolute",
                    left: 14,
                    top: "50%",
                    transform: "translateY(-50%)",
                    color: "#059669",
                  }}
                />
                <input
                  type={showPassword ? "text" : "password"}
                  placeholder={t("admin.password")}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  style={{
                    width: "100%",
                    height: 48,
                    padding: "0 44px 0 44px",
                    border: "2px solid #a7f3d0",
                    borderRadius: "10px",
                    fontSize: "15px",
                    outline: "none",
                    color: "#111827",
                    background: "#fff",
                  }}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  style={{
                    position: "absolute",
                    right: 12,
                    top: "50%",
                    transform: "translateY(-50%)",
                    background: "none",
                    border: "none",
                    cursor: "pointer",
                    color: "#9ca3af",
                  }}
                >
                  {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                </button>
              </div>
            </div>
            {error && (
              <motion.p
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                style={{ color: "#dc2626", fontSize: "13px", marginBottom: "12px", textAlign: "center" }}
              >
                {error}
              </motion.p>
            )}
            <button
              type="submit"
              disabled={loading}
              style={{
                width: "100%",
                height: 48,
                background: "linear-gradient(135deg, #10b981, #059669)",
                color: "#fff",
                border: "none",
                borderRadius: "10px",
                fontSize: "16px",
                fontWeight: 600,
                cursor: loading ? "not-allowed" : "pointer",
                opacity: loading ? 0.7 : 1,
                boxShadow: "0 4px 14px rgba(16, 185, 129, 0.35)",
              }}
            >
              {loading ? t("admin.loggingIn") : t("admin.loginBtn")}
            </button>
          </form>
        </div>
        <p style={{ textAlign: "center", marginTop: "20px", fontSize: "12px", color: "#6b7280" }}>
          <Link href="/" style={{ color: "#059669", textDecoration: "none" }}>
            {t("admin.goHome")}
          </Link>
        </p>
      </motion.div>
    </div>
  );
}

function Dashboard({ onLogout }) {
  const { t } = useTranslation();
  const [activeTab, setActiveTab] = useState("admins");
  const [searchQuery, setSearchQuery] = useState("");
  const [admins, setAdmins] = useState([]);
  const [users, setUsers] = useState([]);
  const [logs, setLogs] = useState([]);
  const [stats, setStats] = useState([]);
  const [loading, setLoading] = useState(true);

  React.useEffect(() => {
    const token = localStorage.getItem("adminToken");
    if (!token) return;
    const fetchAll = async () => {
      setLoading(true);
      try {
        const [a, u, l, s] = await Promise.allSettled([
          axios.get("/api/v1/admin/admins"),
          axios.get("/api/v1/admin/users"),
          axios.get("/api/v1/admin/access-logs"),
          axios.get("/api/v1/admin/daily-stats"),
        ]);
        const has401 = [a, u, l, s].some(
          (p) => p.status === "rejected" && p.reason?.response?.status === 401
        );
        if (has401) {
          localStorage.removeItem("adminToken");
          onLogout();
          return;
        }
        if (a.status === "fulfilled" && a.value?.data?.success) setAdmins(a.value.data.data || []);
        if (u.status === "fulfilled" && u.value?.data?.success) setUsers(u.value.data.data || []);
        if (l.status === "fulfilled" && l.value?.data?.success) setLogs(l.value.data.data || []);
        if (s.status === "fulfilled" && s.value?.data?.success) setStats(s.value.data.data || []);
      } catch (e) {
        if (e?.response?.status === 401) {
          localStorage.removeItem("adminToken");
          onLogout();
        } else {
          console.error("대시보드 데이터 로드 실패:", e);
        }
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, [onLogout]);

  const emailCount = users.filter(u => u.provider === "email").length;
  const kakaoCount = users.filter(u => u.provider === "kakao").length;
  const appleCount = users.filter(u => u.provider === "apple").length;

  const statsCards = [
    { title: t("admin.admins"), value: admins.length, icon: Shield, color: "linear-gradient(135deg, #10b981, #059669)" },
    { title: t("admin.users"), value: users.length, icon: Users, color: "linear-gradient(135deg, #3b82f6, #06b6d4)", sub: `Email ${emailCount} / Kakao ${kakaoCount} / Apple ${appleCount}` },
    { title: t("admin.accessLogs"), value: logs.length, icon: Activity, color: "linear-gradient(135deg, #8b5cf6, #ec4899)" },
    { title: t("admin.dailyStats"), value: stats.length, icon: TrendingUp, color: "linear-gradient(135deg, #f59e0b, #ef4444)" },
  ];

  const filterList = (list, keys) => {
    if (!searchQuery.trim()) return list;
    const q = searchQuery.toLowerCase();
    return list.filter((row) =>
      keys.some((k) => String(row[k] || "").toLowerCase().includes(q))
    );
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        background: "linear-gradient(135deg, #ecfdf5 0%, #d1fae5 50%, #a7f3d0 100%)",
      }}
    >
      <header
        style={{
          position: "sticky",
          top: 0,
          zIndex: 10,
          background: "rgba(255,255,255,0.9)",
          backdropFilter: "blur(12px)",
          borderBottom: "1px solid rgba(16, 185, 129, 0.2)",
          padding: "16px 24px",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          flexWrap: "wrap",
          gap: 12,
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <div
            style={{
              width: 40,
              height: 40,
              borderRadius: "12px",
              background: "linear-gradient(135deg, #10b981, #059669)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            <LayoutDashboard size={22} color="#fff" />
          </div>
          <h1 style={{ fontSize: "20px", fontWeight: 700, color: "#047857" }}>{t("admin.dashboard")}</h1>
        </div>
        <button
          onClick={onLogout}
          style={{
            display: "flex",
            alignItems: "center",
            gap: 8,
            padding: "10px 16px",
            border: "2px solid #a7f3d0",
            borderRadius: "10px",
            background: "#fff",
            color: "#047857",
            fontWeight: 600,
            cursor: "pointer",
          }}
        >
          <LogOut size={18} />
          {t("admin.logout")}
        </button>
      </header>

      <main style={{ maxWidth: 1200, margin: "0 auto", padding: "24px" }}>
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: 16, marginBottom: 24 }}
        >
          {statsCards.map((card, i) => (
            <motion.div
              key={card.title}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.05 }}
              style={{
                background: "#fff",
                borderRadius: "12px",
                padding: 20,
                border: "2px solid rgba(16, 185, 129, 0.2)",
                boxShadow: "0 4px 14px rgba(0,0,0,0.04)",
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
              }}
            >
              <div>
                <p style={{ fontSize: "13px", color: "#6b7280", marginBottom: 4 }}>{card.title}</p>
                <p style={{ fontSize: "24px", fontWeight: 700, color: "#111" }}>{card.value}</p>
                {card.sub && <p style={{ fontSize: "11px", color: "#9ca3af", marginTop: 2 }}>{card.sub}</p>}
              </div>
              <div
                style={{
                  width: 44,
                  height: 44,
                  borderRadius: "12px",
                  background: card.color,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                }}
              >
                <card.icon size={22} color="#fff" />
              </div>
            </motion.div>
          ))}
        </motion.div>

        <div
          style={{
            background: "#fff",
            borderRadius: "16px",
            border: "2px solid rgba(16, 185, 129, 0.2)",
            boxShadow: "0 4px 20px rgba(0,0,0,0.06)",
            overflow: "hidden",
          }}
        >
          <div
            style={{
              padding: "20px 24px",
              display: "flex",
              flexWrap: "wrap",
              gap: 12,
              alignItems: "center",
              justifyContent: "space-between",
              borderBottom: "1px solid #e5e7eb",
            }}
          >
            <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
              {["admins", "users", "logs", "stats"].map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab)}
                  style={{
                    padding: "10px 16px",
                    borderRadius: "10px",
                    border: activeTab === tab ? "none" : "2px solid #d1fae5",
                    background: activeTab === tab ? "linear-gradient(135deg, #10b981, #059669)" : "#fff",
                    color: activeTab === tab ? "#fff" : "#047857",
                    fontWeight: 600,
                    cursor: "pointer",
                  }}
                >
                  {tab === "admins" && t("admin.admins")}
                  {tab === "users" && t("admin.users")}
                  {tab === "logs" && t("admin.accessLogs")}
                  {tab === "stats" && t("admin.dailyStats")}
                </button>
              ))}
            </div>
            <div style={{ position: "relative", flex: "1 1 200px", maxWidth: 280 }}>
              <Search
                size={18}
                style={{ position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", color: "#9ca3af" }}
              />
              <input
                type="text"
                placeholder={t("admin.searchPlaceholder")}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                style={{
                  width: "100%",
                  height: 40,
                  padding: "0 12px 0 40px",
                  border: "2px solid #d1fae5",
                  borderRadius: "10px",
                  fontSize: "14px",
                  outline: "none",
                }}
              />
            </div>
          </div>

          <div style={{ padding: 24, overflowX: "auto" }}>
            {loading ? (
              <p style={{ textAlign: "center", color: "#6b7280", padding: 40 }}>{t("admin.loading")}</p>
            ) : (
              <AnimatePresence mode="wait">
                <motion.div
                  key={activeTab}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0 }}
                  transition={{ duration: 0.2 }}
                >
                  {activeTab === "admins" && (
                    <Table
                      cols={[t("admin.adminId"), t("admin.email"), t("admin.name"), t("admin.role")]}
                      fieldMap={{ [t("admin.adminId")]: "adminId", [t("admin.email")]: "adminEmail", [t("admin.name")]: "adminName", [t("admin.role")]: "role" }}
                      rows={filterList(admins, ["adminId", "adminEmail", "adminName", "role"])}
                    />
                  )}
                  {activeTab === "users" && (
                    <Table
                      cols={[t("admin.provider"), t("admin.email"), t("admin.name"), t("admin.phone"), t("admin.status"), t("admin.lastLogin")]}
                      fieldMap={{ [t("admin.provider")]: "provider", [t("admin.email")]: "email", [t("admin.name")]: "username", [t("admin.phone")]: "phone", [t("admin.status")]: "status", [t("admin.lastLogin")]: "lastLoginAt" }}
                      rows={filterList(users, ["provider", "email", "username", "phone", "status"])}
                    />
                  )}
                  {activeTab === "logs" && (
                    <Table
                      cols={[t("admin.logId"), t("admin.userId"), t("admin.platform"), t("admin.ip"), t("admin.accessTime")]}
                      fieldMap={{ [t("admin.logId")]: "accessLogId", [t("admin.userId")]: "userId", [t("admin.platform")]: "platform", [t("admin.ip")]: "reqIp", [t("admin.accessTime")]: "createdAt" }}
                      rows={filterList(logs, ["accessLogId", "userId", "platform", "reqIp"])}
                    />
                  )}
                  {activeTab === "stats" && (
                    <Table
                      cols={[t("admin.date"), t("admin.activeUsers"), t("admin.totalUsers"), "Android", "iOS", "Web"]}
                      fieldMap={{ [t("admin.date")]: "statDate", [t("admin.activeUsers")]: "activeUserCount", [t("admin.totalUsers")]: "totalUserCount", "Android": "androidCount", "iOS": "iosCount", "Web": "webCount" }}
                      rows={filterList(stats, ["statDate"])}
                    />
                  )}
                </motion.div>
              </AnimatePresence>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

function Table({ cols, rows, fieldMap }) {
  const { t } = useTranslation();
  const getField = (col) => (fieldMap && fieldMap[col]) || col;

  return (
    <table style={{ width: "100%", borderCollapse: "collapse", fontSize: "14px" }}>
      <thead>
        <tr style={{ borderBottom: "2px solid #e5e7eb" }}>
          {cols.map((c) => (
            <th
              key={c}
              style={{
                padding: "12px 16px",
                textAlign: "left",
                fontWeight: 600,
                color: "#374151",
              }}
            >
              {c}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {rows.length === 0 ? (
          <tr>
            <td colSpan={cols.length} style={{ padding: 40, textAlign: "center", color: "#6b7280" }}>
              {t("admin.noData")}
            </td>
          </tr>
        ) : (
          rows.map((row, i) => (
            <tr key={i} style={{ borderBottom: "1px solid #f3f4f6" }}>
              {cols.map((col) => {
                const key = getField(col);
                const val = row[key] ?? "-";
                return (
                  <td key={col} style={{ padding: "12px 16px", color: "#111" }}>
                    {typeof val === "object" && val !== null ? JSON.stringify(val) : String(val)}
                  </td>
                );
              })}
            </tr>
          ))
        )}
      </tbody>
    </table>
  );
}

function Admin() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    setIsLoggedIn(!!localStorage.getItem("adminToken"));
  }, []);

  const handleLogin = () => setIsLoggedIn(true);
  const handleLogout = () => {
    localStorage.removeItem("adminToken");
    setIsLoggedIn(false);
  };

  return (
    <>
      {!isLoggedIn ? <LoginForm onLogin={handleLogin} /> : <Dashboard onLogout={handleLogout} />}
    </>
  );
}

export default Admin;
