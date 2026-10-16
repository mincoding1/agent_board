package com.example.board.repository;

import com.example.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findFirstByOrderByIdDesc();

    // 제목 또는 내용에 word가 포함된 글 id (대소문자 무시, %/_ 는 일반 문자로 취급)
    @Query("select p.id from Post p where locate(lower(:word), lower(p.title)) > 0 "
            + "or locate(lower(:word), lower(p.content)) > 0 order by p.id")
    List<Long> findIdsByWord(@Param("word") String word);

    // 삭제된 행 수 반환 (이미 삭제되었으면 0)
    @Modifying
    @Transactional
    @Query("delete from Post p where p.id = :id")
    int deleteByPostId(@Param("id") Long id);
}
