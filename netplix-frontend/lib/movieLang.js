import i18n from "@/lib/i18n";

const isEn = () => i18n.language === "en";

export const getMovieTitle = (m) =>
  isEn() && m?.movieNameEn ? m.movieNameEn : m?.movieName;

export const getPosterPath = (m) =>
  isEn() && m?.posterPathEn ? m.posterPathEn : m?.posterPath;

export const getBackdropPath = (m) =>
  isEn() && m?.backdropPathEn ? m.backdropPathEn : m?.backdropPath;

export const getOverview = (m) =>
  isEn() && m?.overviewEn ? m.overviewEn : m?.overview;

export const getTagline = (m) =>
  isEn() && m?.taglineEn ? m.taglineEn : m?.tagline;
