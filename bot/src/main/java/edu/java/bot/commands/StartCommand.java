package edu.java.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.service.BotService;
import edu.java.bot.util.TextResolver;
import java.util.Map;

public class StartCommand extends AbstractCommand {

    private final BotService botService;

    public StartCommand(TextResolver textResolver, BotService botService) {
        super(textResolver);
        this.botService = botService;
    }

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String description() {
        return "command.start.description";
    }

    @Override
    public SendMessage handle(Update update) {
        var answer = botService.registerUser(update.message().chat().id());
        if (answer != null && answer.isError()) {
            return new SendMessage(
                update.message().chat().id(),
                textResolver.resolve(
                    "command.start.error",
                    Map.of("error", answer.apiErrorResponse().description())
                )
            );
        }
        return new SendMessage(
            update.message().chat().id(),
            textResolver.resolve(
                "command.start.hello_message",
                Map.of("user_name", update.message().chat().firstName())
            )
        );
    }
}
