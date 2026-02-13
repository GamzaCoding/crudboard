// src/main/resources/static/js/pages/posts-list.page.js
import { fmtDate, escapeHtml } from "../core/dom.js";
import { listPosts } from "../api/posts.api.js";
import { getMeOrNull } from "../api/auth.api.js";

const card = document.getElementById("card");
const tbody = document.getElementById("tbody");
const summary = document.getElementById("summary");
const pageInfo = document.getElementById("pageInfo");
const errorBox = document.getElementById("errorBox");

const keywordInput = document.getElementById("keyword");
const typeSelect = document.getElementById("type");
const createdFromInput = document.getElementById("createdFrom");
const createdToInput = document.getElementById("createdTo");
const sortSelect = document.getElementById("sort");

const sizeSelect = document.getElementById("size");
const searchBtn = document.getElementById("searchBtn");
const resetBtn = document.getElementById("resetBtn");
const prevBtn = document.getElementById("prevBtn");
const nextBtn = document.getElementById("nextBtn");

const authPill = document.getElementById("authPill");
const authHint = document.getElementById("authHint");
const writeBtn = document.getElementById("writeBtn");

// ---------- UI helpers ----------
function setLoading(isLoading) {
    card.classList.toggle("loading", isLoading);
    searchBtn.disabled = isLoading;
    resetBtn.disabled = isLoading;
    prevBtn.disabled = isLoading;
    nextBtn.disabled = isLoading;
}

function showError(message) {
    errorBox.style.display = "block";
    errorBox.textContent = message;
}

function clearError() {
    errorBox.style.display = "none";
    errorBox.textContent = "";
}

function toLocalDateTimeParam(datetimeLocalValue) {
    if (!datetimeLocalValue) return "";
    return datetimeLocalValue.length === 16 ? `${datetimeLocalValue}:00` : datetimeLocalValue;
}

function toDatetimeLocalInputValue(localDateTime) {
    if (!localDateTime) return "";
    return String(localDateTime).slice(0, 16);
}

// ---------- Auth ----------
function setWriteEnabled(enabled) {
    if (enabled) {
        writeBtn.classList.remove("disabled");
        writeBtn.setAttribute("aria-disabled", "false");
        authHint.style.display = "none";
    } else {
        writeBtn.classList.add("disabled");
        writeBtn.setAttribute("aria-disabled", "true");
        authHint.style.display = "inline";
    }
}

async function checkAuth() {
    try {
        const me = await getMeOrNull();
        if (me) {
            authPill.textContent = "로그인됨";
            setWriteEnabled(true);
        } else {
            authPill.textContent = "비로그인";
            setWriteEnabled(false);
        }
    } catch (e) {
        authPill.textContent = "로그인 확인 실패";
        setWriteEnabled(false);
        console.error(e);
    }
}

writeBtn.addEventListener("click", (e) => {
    if (writeBtn.classList.contains("disabled")) {
        e.preventDefault();
        alert("로그인 후 글쓰기가 가능합니다.");
    }
});

// ---------- URL state ----------
function readStateFromUrl() {
    const params = new URLSearchParams(location.search);

    const page = parseInt(params.get("page") ?? "0", 10);
    const size = parseInt(params.get("size") ?? "5", 10);

    const keyword = params.get("keyword") ?? "";
    const type = params.get("type") ?? "";
    const createdFrom = params.get("createdFrom") ?? "";
    const createdTo = params.get("createdTo") ?? "";
    const sort = params.get("sort") ?? "createdAt,desc";

    return {
        page: Number.isFinite(page) && page >= 0 ? page : 0,
        size: [5, 10, 20, 50].includes(size) ? size : 5,
        keyword,
        type,
        createdFrom,
        createdTo,
        sort,
    };
}

function writeStateToUrl(state) {
    const params = new URLSearchParams();
    params.set("page", String(state.page));
    params.set("size", String(state.size));

    if (state.keyword?.trim()) params.set("keyword", state.keyword.trim());
    if (state.type) params.set("type", state.type);
    if (state.createdFrom) params.set("createdFrom", state.createdFrom);
    if (state.createdTo) params.set("createdTo", state.createdTo);
    if (state.sort) params.set("sort", state.sort);

    history.replaceState(null, "", `${location.pathname}?${params.toString()}`);
}

// ---------- Render ----------
function renderTable(pageResponse) {
    tbody.innerHTML = "";
    const rows = pageResponse?.content ?? [];

    if (rows.length === 0) {
        const tr = document.createElement("tr");
        tr.innerHTML = `<td colspan="4" class="muted" style="padding:16px;">게시글이 없습니다.</td>`;
        tbody.appendChild(tr);
        return;
    }

    for (const post of rows) {
        const tr = document.createElement("tr");
        tr.innerHTML = `
      <td class="muted">${post.id ?? "-"}</td>
      <td>
        <div style="font-weight:600; margin-bottom:4px;">${escapeHtml(post.title ?? "")}</div>
        <div class="muted" style="max-width: 520px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
          ${escapeHtml(post.content ?? "")}
        </div>
      </td>
      <td class="muted">${fmtDate(post.createdAt)}</td>
      <td class="muted">${fmtDate(post.updatedAt)}</td>
    `;
        tr.addEventListener("click", () => {
            if (post.id == null) return;
            location.href = `/posts/${post.id}`;
        });
        tbody.appendChild(tr);
    }
}

function renderMeta(pageResponse, state) {
    const totalElements = pageResponse?.totalElements ?? 0;
    const totalPages = pageResponse?.totalPages ?? 0;
    const page = pageResponse?.page ?? state.page;
    const size = pageResponse?.size ?? state.size;
    const numberOfElements = pageResponse?.content?.length ?? 0;

    summary.textContent = `총 ${totalElements}개 · 현재 ${numberOfElements}개 표시`;
    pageInfo.textContent = `page ${page + 1} / ${Math.max(totalPages, 1)} · size ${size}`;

    const first = pageResponse?.first ?? (page <= 0);
    const last = pageResponse?.last ?? (totalPages > 0 ? page >= totalPages - 1 : true);

    prevBtn.disabled = first;
    nextBtn.disabled = last;
}

// ---------- Page logic ----------
let state = readStateFromUrl();

// input에 반영
keywordInput.value = state.keyword;
typeSelect.value = state.type;
createdFromInput.value = toDatetimeLocalInputValue(state.createdFrom);
createdToInput.value = toDatetimeLocalInputValue(state.createdTo);
sortSelect.value = state.sort;
sizeSelect.value = String(state.size);

function syncStateFromInputs(resetPage = false) {
    state.keyword = keywordInput.value;
    state.type = typeSelect.value;
    state.createdFrom = toLocalDateTimeParam(createdFromInput.value);
    state.createdTo = toLocalDateTimeParam(createdToInput.value);
    state.sort = sortSelect.value;
    state.size = parseInt(sizeSelect.value, 10);
    if (resetPage) state.page = 0;
}

async function load() {
    clearError();
    setLoading(true);
    writeStateToUrl(state);

    try {
        const data = await listPosts(state);
        renderTable(data);
        renderMeta(data, state);
    } catch (e) {
        showError(e?.message ?? String(e));
        prevBtn.disabled = true;
        nextBtn.disabled = true;
        summary.textContent = "-";
        pageInfo.textContent = "page - / -";
        tbody.innerHTML = "";
    } finally {
        setLoading(false);
    }
}

// events
searchBtn.addEventListener("click", () => {
    syncStateFromInputs(true);
    load();
});

resetBtn.addEventListener("click", () => {
    keywordInput.value = "";
    typeSelect.value = "";
    createdFromInput.value = "";
    createdToInput.value = "";
    sortSelect.value = "createdAt,desc";
    sizeSelect.value = "5";

    state = {
        page: 0,
        size: 5,
        keyword: "",
        type: "",
        createdFrom: "",
        createdTo: "",
        sort: "createdAt,desc",
    };
    load();
});

keywordInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") searchBtn.click();
});

typeSelect.addEventListener("change", () => { syncStateFromInputs(true); load(); });
sortSelect.addEventListener("change", () => { syncStateFromInputs(true); load(); });
createdFromInput.addEventListener("change", () => { syncStateFromInputs(true); load(); });
createdToInput.addEventListener("change", () => { syncStateFromInputs(true); load(); });

sizeSelect.addEventListener("change", () => {
    syncStateFromInputs(true);
    load();
});

prevBtn.addEventListener("click", () => {
    state.page = Math.max(0, state.page - 1);
    load();
});

nextBtn.addEventListener("click", () => {
    state.page = state.page + 1;
    load();
});

// start
checkAuth();
load();