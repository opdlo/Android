package com.danceflow.repository;

import com.danceflow.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.content LIKE %:search% ORDER BY p.createdAt DESC")
    Page<Post> searchPosts(@Param("search") String search, Pageable pageable);

    Page<Post> findAllByOrderByIdDesc(Pageable pageable);

    Page<Post> findByAuthorId(Long authorId, Pageable pageable);
}

