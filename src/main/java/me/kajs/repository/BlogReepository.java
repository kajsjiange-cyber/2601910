package me.kajs.repository;

import me.kajs.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogReepository extends JpaRepository<Article, Long> {
}
