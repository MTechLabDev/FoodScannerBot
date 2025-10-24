package dev.todoforever.foodscannerbot.config;

import dev.todoforever.foodscannerbot.utils.EnvUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;

@Configuration
public class TelegramClientConfig {

    @Bean
    public OkHttpTelegramClient telegramClient() {
        String token = EnvUtil.getOrDefault("TELEGRAM_BOT_TOKEN", null);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Missing TELEGRAM_BOT_TOKEN");
        }
        return new OkHttpTelegramClient(token);
    }
}
