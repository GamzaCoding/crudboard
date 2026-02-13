export function toUserMessage(err) {
    // http.js에서 만든 Error: err.status, err.body를 최대한 활용
    const status = err?.status;

    // 1) 서버가 ApiError(JSON)로 내려준 경우
    const body = err?.body;
    if (body) {
        try {
            const json = JSON.parse(body);

            // ApiError 포맷: { code, message, fieldViolations, ... }
            if (json?.message) {
                const violations = json?.fieldViolations;
                if (Array.isArray(violations) && violations.length > 0) {
                    const detail = violations
                        .map(v => v?.field ? `${v.field}: ${v.message}` : v?.message)
                        .filter(Boolean)
                        .join("\n");
                    return `${json.message}\n${detail}`.trim();
                }
                return String(json.message);
            }
        } catch {
            // body가 JSON이 아니면(HTML 에러 페이지 등) 아래로
            if (String(body).trim().length > 0) return String(body);
        }
    }

    // 2) 상태코드 기반 기본 메시지(백업)
    if (status === 401) return "로그인이 필요합니다.";
    if (status === 403) return "권한이 없습니다.";
    if (status === 404) return "요청한 리소스를 찾을 수 없습니다.";
    if (status >= 500) return "서버 오류가 발생했습니다.";

    // 3) 마지막 fallback
    return err?.message ?? "알 수 없는 오류가 발생했습니다.";
}