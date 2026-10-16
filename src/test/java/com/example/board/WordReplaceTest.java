package com.example.board;

import com.example.board.entity.Post;
import com.example.board.repository.PostRepository;
import com.example.board.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * P1: 검색은 대소문자를 무시하지만 교체는 구분해서, 바뀌지 않은 글도 "변경됨"으로 보고되던 문제.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
class WordReplaceTest {

    @Autowired
    PostRepository repository;

    @Autowired
    PostService service;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private Post save(String title, String content) {
        Post p = new Post();
        p.setTitle(title);
        p.setName("kim");
        p.setContent(content);
        return repository.save(p);
    }

    @Test
    void 대소문자가_달라도_검색된_글은_실제로_교체된다() {
        Post upper = save("KFC 신메뉴", "오늘 Kfc 갈 사람");

        List<Long> found = service.searchIdsByWord("kfc");
        assertThat(found).containsExactly(upper.getId());

        int changed = service.replaceWord(found, "kfc", "bhc");

        assertThat(changed).isEqualTo(1);
        Post after = repository.findById(upper.getId()).orElseThrow();
        assertThat(after.getTitle()).isEqualTo("bhc 신메뉴");
        assertThat(after.getContent()).isEqualTo("오늘 bhc 갈 사람");
        // 교체 후에는 같은 단어로 다시 검색해도 나오지 않아야 한다
        assertThat(service.searchIdsByWord("kfc")).isEmpty();
    }

    @Test
    void 검색_결과와_변경_건수가_일치한다() {
        save("KFC 쿠폰", "선착순");
        save("회식 투표", "2안 kfc 치킨");
        save("주차 안내", "지하 2층 청소");

        List<Long> found = service.searchIdsByWord("kfc");
        assertThat(found).hasSize(2);

        int changed = service.replaceWord(found, "kfc", "bhc");

        assertThat(changed).isEqualTo(found.size());
        assertThat(service.searchIdsByWord("kfc")).isEmpty();
        assertThat(service.searchIdsByWord("bhc")).hasSize(2);
    }

    @Test
    void 단어가_없는_글은_변경_건수에_포함되지_않고_내용도_그대로다() {
        Post none = save("주차 안내", "지하 2층 청소");

        int changed = service.replaceWord(List.of(none.getId()), "kfc", "bhc");

        assertThat(changed).isZero();
        Post after = repository.findById(none.getId()).orElseThrow();
        assertThat(after.getTitle()).isEqualTo("주차 안내");
        assertThat(after.getContent()).isEqualTo("지하 2층 청소");
    }

    @Test
    void 없는_id는_건너뛴다() {
        Post p = save("kfc 후기", "맛있다");

        int changed = service.replaceWord(List.of(p.getId(), 999_999L), "kfc", "bhc");

        assertThat(changed).isEqualTo(1);
    }

    @Test
    void 특수문자가_포함된_단어도_글자_그대로_교체된다() {
        Post p = save("가격 100% 인상", "a.b.c 테스트");

        assertThat(service.replaceWord(List.of(p.getId()), "100%", "50%")).isEqualTo(1);
        assertThat(service.replaceWord(List.of(p.getId()), "a.b.c", "x$y")).isEqualTo(1);

        Post after = repository.findById(p.getId()).orElseThrow();
        assertThat(after.getTitle()).isEqualTo("가격 50% 인상");
        assertThat(after.getContent()).isEqualTo("x$y 테스트");
    }
}
