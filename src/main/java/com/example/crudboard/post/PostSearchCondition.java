package com.example.crudboard.post;

import java.time.LocalDateTime;
/**
 * 동적 검색을 위한 조건 객체
 * keyword: 검색어(없으면 키워드 미적용)
 * type: 키워드를 어디에 적용할지(TITLE, CONTENT, TITLE_CONTENT)
 * createdFrom, createdTo: 작성일 범위(없으면 날짜 조건 미적용)
 */
public record PostSearchCondition(
        String keyword,
        PostSearchType type,
        LocalDateTime createdFrom,
        LocalDateTime createdTo
) {
}
