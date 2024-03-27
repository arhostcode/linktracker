package edu.java.persitence.jpa.repository;

import edu.java.persitence.jpa.entity.TgChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaChatRepository extends JpaRepository<TgChatEntity, Long> {
}
