import { getJson, postJson, request } from "../core/http.js";

export async function getMeOrNull() {
    try {
        return await getJson("/api/auth/me");
    } catch (e) {
        if (e?.status === 401 || e?.status === 403) return null;
        throw e;
    }
}

export async function login(email, password) {
    // 204 No Content (성공), 실패 시 401
    await postJson("/api/auth/login", { email, password });
}

export async function signup(email, password) {
    // 201 Created (성공)
    await postJson("/api/auth/signup", { email, password });
}

export async function logout() {
    // 204 No Content (성공)
    await request("/api/auth/logout", { method: "POST" });
}