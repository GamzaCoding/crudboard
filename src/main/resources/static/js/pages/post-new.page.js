// src/main/resources/static/js/pages/post-new.page.js
import { safeTrim } from "../core/dom.js";
import { createPost } from "../api/posts.api.js";
import { getMeOrNull } from "../api/auth.api.js";

const card = document.getElementById("card");
const form = document.getElementById("form");
const titleEl = document.getElementById("title");
const contentEl = document.getElementById("content");
const submitBtn = document.getElementById("submitBtn");
const errorBox = document.getElementById("errorBox");
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

async function initAuth() {
    try {
        const me = await getMeOrNull();
        if (me) {
            authPill.textContent = "로그인됨";
            setFormEnabled(true);
        } else {
            authPill.textContent = "비로그인";
            setFormEnabled(false);
            showError("로그인 후 글쓰기가 가능합니다. (홈에서 로그인)");
        }
    } catch (e) {
        authPill.textContent = "로그인 확인 실패";
        setFormEnabled(false);
        showError(e?.message ?? String(e));
    }
}

form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearError();

    const title = safeTrim(titleEl.value);
    const content = safeTrim(contentEl.value);

    if (!title) return showError("title은 필수입니다.");
    if (!content) return showError("content는 필수입니다.");

    setLoading(true);
    try {
        const { location } = await createPost({ title, content });
        if (!location) {
            showError("생성은 성공했지만 Location 헤더가 없습니다.");
            return;
        }
        const id = location.split("/").pop();
        location.href = `/posts/${id}`;
    } catch (err) {
        // 401이면 보통 여기로도 올 수 있음
        if (err?.status === 401 || err?.status === 403) {
            showError("로그인 후 글쓰기가 가능합니다. (홈에서 로그인)");
            setFormEnabled(false);
            return;
        }
        showError(err?.message ?? String(err));
    } finally {
        setLoading(false);
    }
});

initAuth();