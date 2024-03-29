package edu.java.persitence.jdbc.repository;

import edu.java.domain.dto.TgChat;
import edu.java.persitence.common.repository.TgChatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JdbcChatRepository implements TgChatRepository {

    private final JdbcClient client;

    @Override
    public List<TgChat> findAll() {
        return client.sql("SELECT (id) FROM tg_chat")
            .query(TgChat.class)
            .list();
    }

    @Override
    public void add(long chatId) {
        client.sql("INSERT INTO tg_chat(id) VALUES (:chat_id)")
            .param("chat_id", chatId)
            .update();
    }

    @Override
    public void remove(long chatId) {
        client.sql("DELETE FROM tg_chat WHERE id = :id")
            .param("id", chatId)
            .update();
    }

    @Override
    public boolean isExists(long chatId) {
        return client.sql("SELECT id FROM tg_chat WHERE id = :id").param("id", chatId).query(TgChat.class)
            .optional()
            .isPresent();
    }
}
