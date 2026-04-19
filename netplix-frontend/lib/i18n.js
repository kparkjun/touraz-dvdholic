import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import ko from "@/locales/ko.json";
import en from "@/locales/en.json";

i18n.use(initReactI18next).init({
  resources: { ko: { translation: ko }, en: { translation: en } },
  lng: "ko",
  fallbackLng: "ko",
  interpolation: { escapeValue: false },
});

export function detectAndApplyLanguage() {
  if (typeof window === "undefined") return;
  const userChoice = localStorage.getItem("user_lang");
  if (userChoice && (userChoice === "en" || userChoice === "ko")) {
    i18n.changeLanguage(userChoice);
  }
}

export function setUserLanguage(lang) {
  localStorage.setItem("user_lang", lang);
  i18n.changeLanguage(lang);
}

export default i18n;
