package com.example.crudboard.post;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

/**
 * Post 동적 검색 조건을 Specification으로 변환한다.
 */
public final class PostSpecifications {

    private PostSpecifications() {}

    public static Specification<Post> byCondition(PostSearchCondition condition) {
        return (root, query, cb) -> {
            if (condition == null) {
                return cb.conjunction();
            }

            Predicate predicate = cb.conjunction();

            // 1) keyword 조건
            String keyword = condition.keyword();
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";

                PostSearchType type = condition.type() == null ? PostSearchType.TITLE_CONTENT : condition.type();

                Predicate keywordPredicate;
                switch (type) {
                    case TITLE -> keywordPredicate = cb.like(cb.lower(root.get("title")), like);
                    case CONTENT -> keywordPredicate = cb.like(cb.lower(root.get("content")), like);
                    case TITLE_CONTENT -> {
                        Predicate titleLike = cb.like(cb.lower(root.get("title")), like);
                        Predicate contentLike = cb.like(cb.lower(root.get("content")), like);
                        keywordPredicate = cb.or(titleLike, contentLike);
                    }
                    default -> keywordPredicate = cb.conjunction();
                }
                predicate = cb.and(predicate, keywordPredicate);
            }
            // 2) createAt 범위 조건
            LocalDateTime from = condition.createdFrom();
            LocalDateTime to = condition.createdTo();

            if (from != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return predicate;
        };
    }
}
