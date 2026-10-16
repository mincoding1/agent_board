package com.example.board.controller;

import com.example.board.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WordController {

    private final PostService service;

    public WordController(PostService service) {
        this.service = service;
    }

    // 단어 검색기: 모든 글의 title/content에서 word를 검색해 글 id 목록 반환 (word=kfc)
    @PostMapping("/wordSearch")
    public ResponseEntity<List<Long>> wordSearch(@RequestParam String word) {
        if (word.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.searchIdsByWord(word));
    }

    // 단어 변경기: 전달받은 글 id들의 title/content에서 before를 after로 변경 (ids=1,2,3&before=kfc&after=bhc)
    @PostMapping("/testReplace")
    public ResponseEntity<String> testReplace(@RequestParam List<Long> ids,
                                              @RequestParam String before,
                                              @RequestParam String after) {
        if (before.isEmpty()) {
            return ResponseEntity.badRequest().body("before 단어를 입력하세요.");
        }
        int changed = service.replaceWord(ids, before, after);
        return ResponseEntity.ok("OK (" + changed + "건 변경)");
    }
}
