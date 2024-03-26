package edu.java.scrapper.database.service;

import edu.java.exception.ChatAlreadyRegisteredException;
import edu.java.exception.ChatNotFoundException;
import edu.java.persitence.jdbc.repository.JdbcChatRepository;
import edu.java.scrapper.IntegrationEnvironment;
import edu.java.service.ChatService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class JdbcChatServiceIntegrationTest extends IntegrationEnvironment {

    @Autowired
    private ChatService chatService;
    @Autowired
    private JdbcChatRepository chatRepository;

    @Test
    @Transactional
    @Rollback
    void registerChatShouldAddChatInDatabase() {
        chatService.registerChat(123L);
        Assertions.assertThat(chatRepository.isExists(123L)).isTrue();
    }

    @Test
    @Transactional
    @Rollback
    void registerChatShouldThrowExceptionWhenChatAlreadyExists() {
        chatService.registerChat(123L);
        Assertions.assertThatThrownBy(() -> chatService.registerChat(123L))
            .isInstanceOf(ChatAlreadyRegisteredException.class);
    }

    @Test
    @Transactional
    @Rollback
    void removeChatShouldRemoveChatFromDatabase() {
        chatService.registerChat(123L);
        chatService.deleteChat(123L);
        Assertions.assertThat(chatRepository.isExists(123L)).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void removeChatShouldThrowExceptionWhenChatNotExists() {
        Assertions.assertThatThrownBy(() -> chatService.deleteChat(123L))
            .isInstanceOf(ChatNotFoundException.class);
    }

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jdbc");
    }
}
