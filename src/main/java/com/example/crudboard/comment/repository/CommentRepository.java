package com.example.crudboard.comment.repository;

import com.example.crudboard.comment.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostId(Long postId, Pageable pageable);
    boolean existsByIdAndPostId(Long id, Long postId);
    void deleteByIdAndPostId(Long id, Long postId);
}
