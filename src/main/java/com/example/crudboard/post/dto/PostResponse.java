package com.example.crudboard.post.dto;

import com.example.crudboard.post.Post;
import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}

/*
왜 엔티티(Post)를 그대로 반환하지 않나?
이유
엔티티는 DB 구조/도메인 규칙에 민감해서 API 응답으로 직접 노출하면 변경에 취약해짐
또 나중에 보안/노출 필드 제어도 어려움
DTO는 필요한 필드만 노출할 수 있음
 */
