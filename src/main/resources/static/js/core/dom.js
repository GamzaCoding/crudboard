// src/main/resources/static/js/core/dom.js
export function fmtDate(iso) {
    if (!iso) return "-";
    return String(iso).replace("T", " ").slice(0, 19);
}

export function escapeHtml(str) {
    return String(str)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

export function safeTrim(v) {
    return (v ?? "").toString().trim();
}