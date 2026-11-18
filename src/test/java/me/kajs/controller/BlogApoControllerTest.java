package me.kajs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kajs.domain.Article;
import me.kajs.dto.AddArticleRequest;
import me.kajs.dto.UpdateArticleRequest;
import me.kajs.repository.BlogReepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BlogApoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BlogReepository blogReepository;

    @BeforeEach
    void cleanUp() {
        blogReepository.deleteAll();
    }

    private Article saveArticle(String title, String content) {
        return blogReepository.save(new Article(title, content));
    }

    @DisplayName("1. POST /api/articles : 블로그 글 등록에 성공한다")
    @Test
    void addArticle() throws Exception {
        String title = "첫 번째 글의 제목";
        String content = "첫 번째 글의 내용입니다.";
        AddArticleRequest request = new AddArticleRequest(title, content);
        String requestBody = objectMapper.writeValueAsString(request);
        String url = "/api/articles";

        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.content").value(content));

        List<Article> articles = blogReepository.findAll();
        assertThat(articles).hasSize(1);
        assertThat(articles.get(0).getTitle()).isEqualTo(title);
        assertThat(articles.get(0).getContent()).isEqualTo(content);
    }

    @DisplayName("2. GET /api/articles : 전체 글 목록 조회에 성공한다")
    @Test
    void getAllArticles() throws Exception {
        saveArticle("제목1", "내용1");
        saveArticle("제목2", "내용2");

        ResultActions result = mockMvc.perform(get("/api/articles"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("제목1"))
                .andExpect(jsonPath("$[0].content").value("내용1"))
                .andExpect(jsonPath("$[1].title").value("제목2"))
                .andExpect(jsonPath("$[1].content").value("내용2"));
    }

    @DisplayName("3. GET /api/articles/{id} : 글 단건 조회에 성공한다")
    @Test
    void getArticle() throws Exception {
        Article article = saveArticle("조회용 제목", "조회용 내용");
        Long id = article.getId();

        ResultActions result = mockMvc.perform(get("/api/articles/{id}", id));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("조회용 제목"))
                .andExpect(jsonPath("$.content").value("조회용 내용"));
    }

    @DisplayName("4. DELETE /api/articles/{id} : 글 삭제에 성공한다")
    @Test
    void deleteArticle() throws Exception {
        Article article = saveArticle("삭제할 제목", "삭제할 내용");
        Long id = article.getId();
        assertThat(blogReepository.existsById(id)).isTrue();

        ResultActions result = mockMvc.perform(delete("/api/articles/{id}", id));

        result.andExpect(status().isOk());
        assertThat(blogReepository.existsById(id)).isFalse();
        assertThat(blogReepository.findAll()).isEmpty();
    }

    @DisplayName("5. PUT /api/articles/{id} : 글 수정에 성공한다")
    @Test
    void updateArticle() throws Exception {
        Article article = saveArticle("수정 전 제목", "수정 전 내용");
        Long id = article.getId();
        String newTitle = "수정 후 제목";
        String newContent = "수정 후 내용";
        UpdateArticleRequest request = new UpdateArticleRequest(newTitle, newContent);
        String requestBody = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(put("/api/articles/{id}", id)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(newTitle))
                .andExpect(jsonPath("$.content").value(newContent));

        Article updated = blogReepository.findById(id).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo(newTitle);
        assertThat(updated.getContent()).isEqualTo(newContent);
    }
}
