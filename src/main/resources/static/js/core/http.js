// src/main/resources/static/js/core/http.js
async function readTextSafe(res) {
    try { return await res.text(); } catch { return ""; }
}

export async function request(url, options = {}) {
    const res = await fetch(url, {
        credentials: "same-origin", // ✅ 세션 쿠키 포함
        ...options,
        headers: {
            Accept: "application/json",
            ...(options.headers ?? {}),
        },
    });

    if (!res.ok) {
        const body = await readTextSafe(res);
        const err = new Error(`HTTP ${res.status} ${res.statusText}\n${body}`.trim());
        err.status = res.status;
        err.body = body;
        throw err;
    }

    return res;
}

export async function getJson(url, options = {}) {
    const res = await request(url, options);
    if (res.status === 204) return null;
    try { return await res.json(); } catch { return null; }
}

export async function postJson(url, data, options = {}) {
    return request(url, {
        method: "POST",
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(options.headers ?? {}),
        },
        body: JSON.stringify(data),
    });
}

export async function putJson(url, data, options = {}) {
    return request(url, {
        method: "PUT",
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(options.headers ?? {}),
        },
        body: JSON.stringify(data),
    });
}

export async function del(url, options = {}) {
    return request(url, { method: "DELETE", ...options });
}