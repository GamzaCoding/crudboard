// src/main/resources/static/js/api/posts.api.js
import { getJson, postJson, putJson, del } from "../core/http.js";

export async function listPosts(params) {
    const qs = new URLSearchParams();
    qs.set("page", String(params.page));
    qs.set("size", String(params.size));

    if (params.keyword?.trim()) qs.set("keyword", params.keyword.trim());
    if (params.type) qs.set("type", params.type);
    if (params.createdFrom) qs.set("createdFrom", params.createdFrom);
    if (params.createdTo) qs.set("createdTo", params.createdTo);
    if (params.sort) qs.set("sort", params.sort);

    return await getJson(`/api/posts?${qs.toString()}`);
}

export async function getPost(id) {
    return await getJson(`/api/posts/${id}`);
}

export async function createPost(payload) {
    const res = await postJson("/api/posts", payload);

    // PostController.create는 201 + Location 헤더
    if (res.status === 201) {
        return { location: res.headers.get("Location") };
    }
    // request()에서 ok가 아니면 throw 되므로 여기까지 보통 안 옴
    return { location: res.headers.get("Location") };
}

export async function updatePost(id, payload) {
    // PostController.update는 204
    await putJson(`/api/posts/${id}`, payload);
}

export async function deletePost(id) {
    await del(`/api/posts/${id}`);
}