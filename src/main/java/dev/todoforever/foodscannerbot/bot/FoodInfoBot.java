package dev.todoforever.foodscannerbot.bot;

import dev.todoforever.foodscannerbot.bot.handlers.MessageHandler;
import dev.todoforever.foodscannerbot.utils.EnvUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@Component
public class FoodInfoBot implements SpringLongPollingBot {

    private final MessageHandler messageHandler;
    private final String token;

    public FoodInfoBot(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        this.token = EnvUtil.getOrDefault("TELEGRAM_BOT_TOKEN", null);
        if (this.token == null || this.token.isBlank()) {
            throw new IllegalStateException("Missing TELEGRAM_BOT_TOKEN");
        }
    }

    @Override public String getBotToken() { return token; }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updates -> updates.forEach(messageHandler::handle);
    }
}
