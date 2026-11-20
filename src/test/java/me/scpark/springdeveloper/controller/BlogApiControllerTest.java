
package me.scpark.springdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.scpark.springdeveloper.domain.Article;
import me.scpark.springdeveloper.dto.AddArticleRequest;
import me.scpark.springdeveloper.dto.UpdateArticleRequest;
import me.scpark.springdeveloper.repository.BlogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BlogApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    BlogRepository blogRepository;

    @BeforeEach
    void setUp() {
        blogRepository.deleteAll();
    }

    @Test
    @DisplayName("addArticle: 글 생성에 성공한다")
    void addArticle() throws Exception {
        // Given
        String title = "테스트 제목";
        String content = "테스트 내용";

        AddArticleRequest request =
                new AddArticleRequest(title, content);

        String requestBody =
                objectMapper.writeValueAsString(request);

        // When
        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.content").value(content));

        List<Article> articles = blogRepository.findAll();

        assertThat(articles).hasSize(1);
        assertThat(articles.get(0).getTitle()).isEqualTo(title);
        assertThat(articles.get(0).getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("findArticle: 글 조회에 성공한다")
    void findArticle() throws Exception {
        // Given
        String title = "조회 제목";
        String content = "조회 내용";

        Article savedArticle = blogRepository.save(
                Article.builder()
                        .title(title)
                        .content(content)
                        .build()
        );

        Long articleId = savedArticle.getId();

        // When
        mockMvc.perform(get("/api/articles/{id}", articleId))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.content").value(content));

        Article article = blogRepository.findById(articleId)
                .orElseThrow();

        assertThat(article.getTitle()).isEqualTo(title);
        assertThat(article.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("updateArticle: 글 수정에 성공한다")
    void updateArticle() throws Exception {
        // Given
        Article savedArticle = blogRepository.save(
                Article.builder()
                        .title("기존 제목")
                        .content("기존 내용")
                        .build()
        );

        Long articleId = savedArticle.getId();

        String updatedTitle = "수정된 제목";
        String updatedContent = "수정된 내용";

        UpdateArticleRequest request =
                new UpdateArticleRequest(updatedTitle, updatedContent);

        String requestBody =
                objectMapper.writeValueAsString(request);

        // When
        mockMvc.perform(put("/api/articles/{id}", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(updatedTitle))
                .andExpect(jsonPath("$.content").value(updatedContent));

        Article updatedArticle = blogRepository.findById(articleId)
                .orElseThrow();

        assertThat(updatedArticle.getTitle()).isEqualTo(updatedTitle);
        assertThat(updatedArticle.getContent()).isEqualTo(updatedContent);
    }

    @Test
    @DisplayName("deleteArticle: 글 삭제에 성공한다")
    void deleteArticle() throws Exception {
        // Given
        Article savedArticle = blogRepository.save(
                Article.builder()
                        .title("삭제 제목")
                        .content("삭제 내용")
                        .build()
        );

        Long articleId = savedArticle.getId();

        // When
        mockMvc.perform(delete("/api/articles/{id}", articleId))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        List<Article> articles = blogRepository.findAll();

        assertThat(articles).isEmpty();
        assertThat(blogRepository.findById(articleId)).isEmpty();
    }

    @Test
    @DisplayName("findAllArticles: 전체 글 목록 조회에 성공한다")
    void findAllArticles() throws Exception {
        // Given
        Article article1 = blogRepository.save(
                Article.builder()
                        .title("첫 번째 제목")
                        .content("첫 번째 내용")
                        .build()
        );

        Article article2 = blogRepository.save(
                Article.builder()
                        .title("두 번째 제목")
                        .content("두 번째 내용")
                        .build()
        );

        // When
        mockMvc.perform(get("/api/articles"))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].title",
                        containsInAnyOrder("첫 번째 제목", "두 번째 제목")))
                .andExpect(jsonPath("$[*].content",
                        containsInAnyOrder("첫 번째 내용", "두 번째 내용")));

        List<Article> articles = blogRepository.findAll();

        assertThat(articles).hasSize(2);

        Article saved1 = blogRepository.findById(article1.getId())
                .orElseThrow();

        Article saved2 = blogRepository.findById(article2.getId())
                .orElseThrow();

        assertThat(saved1.getTitle()).isEqualTo("첫 번째 제목");
        assertThat(saved1.getContent()).isEqualTo("첫 번째 내용");
        assertThat(saved2.getTitle()).isEqualTo("두 번째 제목");
        assertThat(saved2.getContent()).isEqualTo("두 번째 내용");
    }
}
