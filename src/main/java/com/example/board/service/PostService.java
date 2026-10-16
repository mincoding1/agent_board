package com.example.board.service;

import com.example.board.entity.Post;
import com.example.board.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PostService {

    private static final int PAGE_SIZE = 10;

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    public Page<Post> list(int page) {
        return repository.findAll(PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));
    }

    public Post get(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("게시글이 없습니다."));
    }

    public void create(Post post) {
        repository.save(post);
    }

    public void update(Long id, Post form) {
        Post post = get(id);
        post.setTitle(form.getTitle());
        post.setName(form.getName());
        post.setContent(form.getContent());
        repository.save(post);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    /** title 또는 content에 word가 포함된 글 id 목록 */
    public List<Long> searchIdsByWord(String word) {
        return repository.findIdsByWord(word);
    }

    /**
     * 지정한 글들의 title/content에서 before를 after로 변경.
     * 검색(findIdsByWord)과 동일하게 대소문자를 무시하고 치환하며,
     * 없는 id와 실제로 바뀐 내용이 없는 글은 건너뛰고 "실제로 변경된 글 수"를 반환한다.
     */
    public int replaceWord(List<Long> ids, String before, String after) {
        Pattern pattern = Pattern.compile(Pattern.quote(before), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        String replacement = Matcher.quoteReplacement(after);
        int changed = 0;
        for (Long id : ids) {
            Optional<Post> found = repository.findById(id);
            if (found.isEmpty()) {
                continue;
            }
            Post post = found.get();
            String newTitle = pattern.matcher(post.getTitle()).replaceAll(replacement);
            String newContent = pattern.matcher(post.getContent()).replaceAll(replacement);
            if (newTitle.equals(post.getTitle()) && newContent.equals(post.getContent())) {
                continue;
            }
            post.setTitle(newTitle);
            post.setContent(newContent);
            repository.save(post);
            changed++;
        }
        return changed;
    }

    public Optional<Post> findLatest() {
        return repository.findFirstByOrderByIdDesc();
    }

    /** 이미 삭제된 글이면 false */
    public boolean deleteIfExists(Long id) {
        return repository.deleteByPostId(id) > 0;
    }

    /** 최신 글 삭제. 글이 없거나 이미 삭제되었으면 false */
    public boolean deleteLatest() {
        return findLatest().map(post -> deleteIfExists(post.getId())).orElse(false);
    }
}
