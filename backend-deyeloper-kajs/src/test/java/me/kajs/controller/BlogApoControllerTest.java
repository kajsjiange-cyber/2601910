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

    @DisplayName("1. POST /api/articles : 成功添加博客文章")
    @Test
    void addArticle() throws Exception {
        String title = "11/4周三老登的课";
        String content = "进行了将数据储存到DB的练习";
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

    @DisplayName("2. GET /api/articles : 成功查询全部文章")
    @Test
    void getAllArticles() throws Exception {
        saveArticle("标题1", "内容1");
        saveArticle("标题2", "内容2");

        ResultActions result = mockMvc.perform(get("/api/articles"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("标题1"))
                .andExpect(jsonPath("$[0].content").value("内容1"))
                .andExpect(jsonPath("$[1].title").value("标题2"))
                .andExpect(jsonPath("$[1].content").value("内容2"));
    }

    @DisplayName("3. GET /api/articles/{id} : 成功查询单篇文章")
    @Test
    void getArticle() throws Exception {
        Article article = saveArticle("查询用的标题", "查询用的内容");
        Long id = article.getId();

        ResultActions result = mockMvc.perform(get("/api/articles/{id}", id));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("查询用的标题"))
                .andExpect(jsonPath("$.content").value("查询用的内容"));
    }

    @DisplayName("4. DELETE /api/articles/{id} : 成功删除单篇文章")
    @Test
    void deleteArticle() throws Exception {
        Article article = saveArticle("要被删除的标题", "要被删除的内容");
        Long id = article.getId();
        assertThat(blogReepository.existsById(id)).isTrue();

        ResultActions result = mockMvc.perform(delete("/api/articles/{id}", id));

        result.andExpect(status().isOk());
        assertThat(blogReepository.existsById(id)).isFalse();
        assertThat(blogReepository.findAll()).isEmpty();
    }

    @DisplayName("5. PUT /api/articles/{id} : 成功修改单篇文章")
    @Test
    void updateArticle() throws Exception {
        Article article = saveArticle("修改前的标题", "修改前的内容");
        Long id = article.getId();
        String newTitle = "修改后的标题";
        String newContent = "修改后的内容";
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
