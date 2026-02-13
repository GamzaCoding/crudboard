// src/main/resources/static/js/pages/post-edit.page.js
import { safeTrim } from "../core/dom.js";
import { getPost, updatePost } from "../api/posts.api.js";
import { getMeOrNull } from "../api/auth.api.js";

const card = document.getElementById("card");
const form = document.getElementById("form");
const titleEl = document.getElementById("title");
const contentEl = document.getElementById("content");
const submitBtn = document.getElementById("submitBtn");
const errorBox = document.getElementById("errorBox");
const idPill = document.getElementById("idPill");
const detailLink = document.getElementById("detailLink");

const authPill = document.getElementById("authPill");
const loginBtn = document.getElementById("loginBtn");

function setLoading(isLoading) {
    card.classList.toggle("loading", isLoading);
    submitBtn.disabled = isLoading;
}

function showError(message) {
    errorBox.style.display = "block";
    errorBox.textContent = message;
}

function clearError() {
    errorBox.style.display = "none";
    errorBox.textContent = "";
}

function setFormEnabled(enabled) {
    titleEl.disabled = !enabled;
    contentEl.disabled = !enabled;
    submitBtn.disabled = !enabled;
    loginBtn.style.display = enabled ? "none" : "inline-flex";
}

function getIdFromPath() {
    // /posts/{id}/edit
    const parts = location.pathname.split("/").filter(Boolean);
    const idx = parts.indexOf("posts");
    if (idx === -1 || parts.length < idx + 2) return null;
    const id = Number(parts[idx + 1]);
    return Number.isFinite(id) ? id : null;
}

const postId = getIdFromPath();
if (postId == null) {
    showError("잘못된 접근입니다. (id를 찾을 수 없음)");
    setFormEnabled(false);
} else {
    idPill.textContent = `ID ${postId}`;
    detailLink.href = `/posts/${postId}`;
}

async function initAuth() {
    const me = await getMeOrNull();
    if (me) {
        authPill.textContent = "로그인됨";
        setFormEnabled(true);
    } else {
        authPill.textContent = "비로그인";
        setFormEnabled(false);
        showError("로그인 후 수정할 수 있어요. (홈에서 로그인)");
    }
}

async function loadPost() {
    if (postId == null) return;
    const post = await getPost(postId);
    titleEl.value = post?.title ?? "";
    contentEl.value = post?.content ?? "";
}

form.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (postId == null) return;

    clearError();

    const title = safeTrim(titleEl.value);
    const content = safeTrim(contentEl.value);
    if (!title) return showError("title은 필수입니다.");
    if (!content) return showError("content는 필수입니다.");

    setLoading(true);
    try {
        await updatePost(postId, { title, content });
        location.href = `/posts/${postId}`;
    } catch (err) {
        if (err?.status === 401 || err?.status === 403) {
            showError("로그인 후 수정할 수 있어요. (홈에서 로그인)");
            setFormEnabled(false);
            return;
        }
        showError(err?.message ?? String(err));
    } finally {
        setLoading(false);
    }
});

(async function start() {
    if (postId == null) return;

    clearError();
    setLoading(true);
    try {
        await initAuth();
        if (submitBtn.disabled) return; // 비로그인이면 여기서 멈춤
        await loadPost();
    } catch (e) {
        showError(e?.message ?? String(e));
        setFormEnabled(false);
    } finally {
        setLoading(false);
    }
})();