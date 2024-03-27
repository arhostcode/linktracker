package edu.java.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import edu.java.bot.commands.Command;
import edu.java.bot.executor.RequestExecutor;
import edu.java.bot.listener.BotMessagesListener;
import edu.java.bot.processor.UserMessagesProcessor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class LinkTrackerBot implements Bot {

    private final TelegramBot telegramBot;
    private final BotMessagesListener botMessagesListener;
    private final UserMessagesProcessor userMessagesProcessor;
    private final RequestExecutor requestExecutor;

    @Override
    @PostConstruct
    public void start() {
        requestExecutor.execute(
            new SetMyCommands(userMessagesProcessor.commands().stream()
                .map(Command::toApiCommand)
                .toList()
                .toArray(new BotCommand[0])
            )
        );
        telegramBot.setUpdatesListener(botMessagesListener);
        log.info("Telegram bot was started");
    }

    @Override
    public void close() {
        telegramBot.shutdown();
        log.info("Telegram bot was stopped");
    }

}
