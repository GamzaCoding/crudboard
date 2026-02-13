// src/main/resources/static/js/pages/post-detail.page.js

import { fmtDate, escapeHtml, safeTrim } from "../core/dom.js";
import { getMeOrNull } from "../api/auth.api.js";
import { getPost, deletePost } from "../api/posts.api.js";
import { listComments, createComment } from "../api/comments.api.js";

// =======================================================
// DOM
// =======================================================
const card = document.getElementById("card");

const titleEl = document.getElementById("title");
const contentEl = document.getElementById("content");
const idPill = document.getElementById("idPill");
const createdAtEl = document.getElementById("createdAt");
const updatedAtEl = document.getElementById("updatedAt");

const authBadge = document.getElementById("authBadge");
const loginBtn = document.getElementById("loginBtn");

const editLink = document.getElementById("editLink");
const deleteBtn = document.getElementById("deleteBtn");
const errorBox = document.getElementById("errorBox");

// comments DOM
const commentInput = document.getElementById("commentInput");
const commentCreateBtn = document.getElementById("commentCreateBtn");
const commentSizeSelect = document.getElementById("commentSize");
const commentPrevBtn = document.getElementById("commentPrevBtn");
const commentNextBtn = document.getElementById("commentNextBtn");
const commentList = document.getElementById("commentList");
const commentSummary = document.getElementById("commentSummary");
const commentPageInfo = document.getElementById("commentPageInfo");

// =======================================================
// UI helpers
// =======================================================
function setLoading(isLoading) {
    card.classList.toggle("loading", isLoading);

    // 로딩 중에는 조작 막기
    deleteBtn.disabled = isLoading;
    commentCreateBtn.disabled = isLoading;
    commentPrevBtn.disabled = isLoading;
    commentNextBtn.disabled = isLoading;
}

function showError(message) {
    errorBox.style.display = "block";
    errorBox.textContent = message;
}

function clearError() {
    errorBox.style.display = "none";
    errorBox.textContent = "";
}

function renderPost(post) {
    if (!post) {
        titleEl.textContent = "-";
        contentEl.textContent = "";
        idPill.textContent = "ID -";
        createdAtEl.textContent = "created -";
        updatedAtEl.textContent = "updated -";
        return;
    }

    titleEl.textContent = post.title ?? "-";
    contentEl.textContent = post.content ?? "";
    idPill.textContent = `ID ${post.id ?? "-"}`;
    createdAtEl.textContent = `created ${fmtDate(post.createdAt)}`;
    updatedAtEl.textContent = `updated ${fmtDate(post.updatedAt)}`;
}

function renderComments(page) {
    const items = page?.content ?? [];
    const totalElements = page?.totalElements ?? 0;
    const totalPages = page?.totalPages ?? 0;
    const pageNumber = page?.page ?? state.comments.page;

    commentSummary.textContent = `댓글 ${totalElements}개`;
    commentPageInfo.textContent = `page ${pageNumber + 1} / ${Math.max(totalPages, 1)}`;

    // 페이징 버튼
    commentPrevBtn.disabled = pageNumber <= 0;
    commentNextBtn.disabled = pageNumber + 1 >= totalPages;

    if (items.length === 0) {
        commentList.innerHTML = `<div class="muted">아직 댓글이 없어요.</div>`;
        return;
    }

    commentList.innerHTML = items
        .map((c) => {
            const created = fmtDate(c.createdAt);
            const updated = fmtDate(c.updatedAt);
            const content = escapeHtml(c.content ?? "");
            return `
        <div class="card" style="margin:10px 0; box-shadow:none; border:1px solid #eef1f4;">
          <div class="muted" style="display:flex; gap:8px; flex-wrap:wrap; margin-bottom:8px;">
            <span class="pill">#${c.id ?? "-"}</span>
            <span class="pill">created ${created}</span>
            <span class="pill">updated ${updated}</span>
          </div>
          <div style="white-space:pre-wrap; word-break:break-word;">${content}</div>
        </div>
      `;
        })
        .join("");
}

// =======================================================
// Route / State
// =======================================================
function getPostIdFromPath() {
    // /posts/{id}
    const parts = location.pathname.split("/").filter(Boolean);
    const last = parts[parts.length - 1];
    const id = Number(last);
    return Number.isFinite(id) ? id : null;
}

const state = {
    postId: getPostIdFromPath(),
    auth: {
        isLoggedIn: false,
        me: null,
    },
    comments: {
        page: 0,
        size: parseInt(commentSizeSelect.value, 10),
    },
};

// 초기 링크 세팅
if (state.postId != null) {
    editLink.href = `/posts/${state.postId}/edit`;
} else {
    showError("잘못된 접근입니다. (id를 찾을 수 없음)");
}

// =======================================================
// Auth
// =======================================================
async function loadAuth() {
    const me = await getMeOrNull(); // 401이면 null 반환하도록 만들었지
    state.auth.isLoggedIn = !!me;
    state.auth.me = me;
}

function applyAuthUi() {
    if (state.auth.isLoggedIn) {
        authBadge.textContent = "login";
        loginBtn.style.display = "none";

        // edit/delete/comment enable
        editLink.classList.remove("disabled");
        if (state.postId != null) editLink.href = `/posts/${state.postId}/edit`;
        deleteBtn.disabled = false;
        commentCreateBtn.disabled = false;
    } else {
        authBadge.textContent = "guest";
        loginBtn.style.display = "inline-flex";

        // edit/delete/comment disable
        editLink.classList.add("disabled");
        editLink.href = "#";
        deleteBtn.disabled = true;
        commentCreateBtn.disabled = true;
    }
}

// =======================================================
// Loaders
// =======================================================
async function loadPost() {
    const post = await getPost(state.postId);
    renderPost(post);
}

async function loadComments() {
    const page = await listComments(state.postId, state.comments.page, state.comments.size);
    renderComments(page);
}

async function loadPage() {
    if (state.postId == null) return;

    clearError();
    setLoading(true);

    try {
        // 1) 로그인 여부 확인 (실패해도 페이지 자체는 뜨게)
        try {
            await loadAuth();
        } catch {
            state.auth.isLoggedIn = false;
            state.auth.me = null;
        }
        applyAuthUi();

        // 2) 게시글/댓글 로드 (GET은 비로그인도 가능)
        await loadPost();
        await loadComments();
    } catch (e) {
        showError(e?.message ?? String(e));
        renderPost(null);

        commentList.innerHTML = "";
        commentSummary.textContent = "-";
        commentPageInfo.textContent = "page - / -";

        deleteBtn.disabled = true;
        commentCreateBtn.disabled = true;
        commentPrevBtn.disabled = true;
        commentNextBtn.disabled = true;
    } finally {
        setLoading(false);
        applyAuthUi(); // 로딩 때문에 disabled 된 상태를 최종 auth 상태로 복구
    }
}

// =======================================================
// Events
// =======================================================
deleteBtn.addEventListener("click", async () => {
    if (state.postId == null) return;

    if (!state.auth.isLoggedIn) {
        alert("로그인 후 삭제할 수 있어요.");
        location.href = "/";
        return;
    }

    const ok = confirm("정말 게시글을 삭제할까요?");
    if (!ok) return;

    clearError();
    setLoading(true);
    try {
        await deletePost(state.postId);
        location.href = "/posts";
    } catch (e) {
        showError(e?.message ?? String(e));
    } finally {
        setLoading(false);
        applyAuthUi();
    }
});

commentCreateBtn.addEventListener("click", async () => {
    if (state.postId == null) return;

    if (!state.auth.isLoggedIn) {
        alert("로그인 후 댓글을 작성할 수 있어요.");
        location.href = "/";
        return;
    }

    const content = safeTrim(commentInput.value);
    if (!content) {
        alert("댓글 내용을 입력해주세요.");
        return;
    }
    if (content.length > 1000) {
        alert("댓글은 최대 1000자까지 입력할 수 있어요.");
        return;
    }

    clearError();
    setLoading(true);
    try {
        await createComment(state.postId, content);
        commentInput.value = "";
        state.comments.page = 0;
        await loadComments();
    } catch (e) {
        // 401/403이면 세션 만료 등일 수 있음
        if (e?.status === 401 || e?.status === 403) {
            state.auth.isLoggedIn = false;
            applyAuthUi();
            showError("로그인 후 댓글을 작성할 수 있어요. (세션이 만료되었을 수 있어요)");
            return;
        }
        showError(e?.message ?? String(e));
    } finally {
        setLoading(false);
        applyAuthUi();
    }
});

commentInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && (e.ctrlKey || e.metaKey)) {
        commentCreateBtn.click();
    }
});

commentSizeSelect.addEventListener("change", async () => {
    state.comments.size = parseInt(commentSizeSelect.value, 10);
    state.comments.page = 0;

    clearError();
    setLoading(true);
    try {
        await loadComments();
    } catch (e) {
        showError(e?.message ?? String(e));
    } finally {
        setLoading(false);
        applyAuthUi();
    }
});

commentPrevBtn.addEventListener("click", async () => {
    state.comments.page = Math.max(0, state.comments.page - 1);

    clearError();
    setLoading(true);
    try {
        await loadComments();
    } catch (e) {
        showError(e?.message ?? String(e));
    } finally {
        setLoading(false);
        applyAuthUi();
    }
});

commentNextBtn.addEventListener("click", async () => {
    state.comments.page = state.comments.page + 1;

    clearError();
    setLoading(true);
    try {
        await loadComments();
    } catch (e) {
        showError(e?.message ?? String(e));
    } finally {
        setLoading(false);
        applyAuthUi();
    }
});

// start
loadPage();