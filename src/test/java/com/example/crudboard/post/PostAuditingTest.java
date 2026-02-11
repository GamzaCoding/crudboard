package com.example.crudboard.post;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.crudboard.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class PostAuditingTest {

    @Autowired
    PostRepository postRepository;

    @Test
    @DisplayName("Post 엔티티 내 created_at, updated_at이 null이 아니다.")
    void auditingFieldsAreSet() {
        Post post = postRepository.save(new Post("t", "c"));
        assertThat(post.getCreatedAt()).isNotNull();
        assertThat(post.getUpdatedAt()).isNotNull();
    }
}
