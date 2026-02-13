// /static/js/api/comments.api.js
import { getJson, postJson } from "../core/http.js";

export async function listComments(postId, page, size) {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    return await getJson(`/api/posts/${postId}/comments?${params.toString()}`);
}

export async function createComment(postId, content) {
    const res = await postJson(`/api/posts/${postId}/comments`, { content });

    // 서버가 201/204로 응답하는 케이스 모두 OK 처리
    if (!(res.status === 201 || res.status === 204 || res.ok)) {
        const text = await res.text().catch(() => "");
        throw new Error(`댓글 등록 실패 (${res.status})\n${text}`);
    }
}