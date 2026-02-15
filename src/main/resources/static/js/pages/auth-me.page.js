import { getMeOrNull } from "../api/auth.api.js";
import { toUserMessage } from "../core/errors.js";

const card = document.getElementById("card");
const errorBox = document.getElementById("errorBox");
const mePanel = document.getElementById("mePanel");
const guestPanel = document.getElementById("guestPanel");

const meId = document.getElementById("meId");
const meEmail = document.getElementById("meEmail");
const meRole = document.getElementById("meRole");

function setLoading(isLoading) {
    card.classList.toggle("loading", isLoading);
}

function showError(message) {
    errorBox.style.display = "block";
    errorBox.textContent = message;
}

function clearError() {
    errorBox.style.display = "none";
    errorBox.textContent = "";
}

function showMe(user) {
    guestPanel.style.display = "none";
    mePanel.style.display = "block";

    meId.textContent = String(user.id ?? "-");
    meEmail.textContent = user.email ?? "-";
    meRole.textContent = user.role ?? "-";
}

function showGuest() {
    mePanel.style.display = "none";
    guestPanel.style.display = "block";
}

async function init() {
    clearError();
    setLoading(true);
    try {
        const me = await getMeOrNull();
        if (me) showMe(me);
        else showGuest();
    } catch (e) {
        showGuest();
        showError(toUserMessage(e));
    } finally {
        setLoading(false);
    }
}

init();
