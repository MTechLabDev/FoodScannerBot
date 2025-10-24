package dev.todoforever.foodscannerbot.bot;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class MessageSender {
    private final OkHttpTelegramClient client;

    public MessageSender(OkHttpTelegramClient client) {
        this.client = client;
    }

    public void send(String chatId, String text) {
        try {
            client.execute(new SendMessage(chatId, text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}