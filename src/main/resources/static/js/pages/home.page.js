import { safeTrim } from "../core/dom.js";
import { getMeOrNull, login, signup, logout } from "../api/auth.api.js";
import { toUserMessage } from "../core/errors.js";

const card = document.getElementById("card");
const errorBox = document.getElementById("errorBox");

const subtext = document.getElementById("subtext");
const loggedInBox = document.getElementById("loggedInBox");
const guestBox = document.getElementById("guestBox");
const meName = document.getElementById("meName");

const loginEmail = document.getElementById("loginEmail");
const loginPassword = document.getElementById("loginPassword");
const loginBtn = document.getElementById("loginBtn");

const signupEmail = document.getElementById("signupEmail");
const signupPassword = document.getElementById("signupPassword");
const signupBtn = document.getElementById("signupBtn");

const logoutBtn = document.getElementById("logoutBtn");
const enterAsGuestBtn = document.getElementById("enterAsGuestBtn");

// -------------------- UI helpers --------------------
function setLoading(isLoading) {
    card.classList.toggle("loading", isLoading);
    loginBtn.disabled = isLoading;
    signupBtn.disabled = isLoading;
    if (logoutBtn) logoutBtn.disabled = isLoading;
}

function showError(message) {
    errorBox.style.display = "block";
    errorBox.textContent = message;
}

function clearError() {
    errorBox.style.display = "none";
    errorBox.textContent = "";
}

function showLoggedIn(user) {
    guestBox.style.display = "none";
    loggedInBox.style.display = "block";

    const email = user?.email ?? "user";
    const id = user?.id ?? "-";
    const role = user?.role ?? "-";
    meName.textContent = `${email} (id=${id}, role=${role})`;

    subtext.textContent = "환영합니다!";
}

function showGuest() {
    loggedInBox.style.display = "none";
    guestBox.style.display = "block";
    subtext.textContent = "로그인/회원가입 또는 게스트로 입장하세요.";
}

// -------------------- Events --------------------
loginBtn.addEventListener("click", async () => {
    clearError();
    setLoading(true);
    try {
        const email = safeTrim(loginEmail.value);
        const password = safeTrim(loginPassword.value);

        if (!email) return showError("로그인 email을 입력해주세요.");
        if (!password) return showError("로그인 password를 입력해주세요.");

        await login(email, password);
        location.href = "/posts";
    } catch (e) {
        showError(toUserMessage(e));
    } finally {
        setLoading(false);
    }
});

signupBtn.addEventListener("click", async () => {
    clearError();
    setLoading(true);
    try {
        const email = safeTrim(signupEmail.value);
        const password = safeTrim(signupPassword.value);

        if (!email) return showError("회원가입 email을 입력해주세요.");
        if (!password) return showError("회원가입 password를 입력해주세요.");

        await signup(email, password);

        // UX: 가입 후 바로 로그인해주는 흐름
        await login(email, password);
        location.href = "/posts";
    } catch (e) {
        showError(toUserMessage(e));
    } finally {
        setLoading(false);
    }
});

logoutBtn?.addEventListener("click", async () => {
    clearError();
    setLoading(true);
    try {
        await logout();
        location.reload();
    } catch (e) {
        showError(toUserMessage(e));
    } finally {
        setLoading(false);
    }
});

enterAsGuestBtn.addEventListener("click", () => {
    location.href = "/posts";
});

// Enter 키 편의 (원하면 삭제 가능)
loginPassword.addEventListener("keydown", (e) => {
    if (e.key === "Enter") loginBtn.click();
});
signupPassword.addEventListener("keydown", (e) => {
    if (e.key === "Enter") signupBtn.click();
});

// -------------------- init --------------------
async function init() {
    clearError();
    setLoading(true);
    try {
        const me = await getMeOrNull();
        if (me) showLoggedIn(me);
        else showGuest();
    } catch (e) {
        // /me가 500 등으로 터지면 여기로
        showGuest();
        showError(toUserMessage(e));
    } finally {
        setLoading(false);
    }
}

init();