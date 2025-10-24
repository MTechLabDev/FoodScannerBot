package dev.todoforever.foodscannerbot.bot.handlers;

import dev.todoforever.foodscannerbot.bot.MessageSender;
import dev.todoforever.foodscannerbot.food.EanDbClient;
import dev.todoforever.foodscannerbot.food.ProductInfo;
import dev.todoforever.foodscannerbot.utils.BarCodeReader;
import dev.todoforever.foodscannerbot.utils.EnvUtil;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.photo.PhotoSize;

import java.io.File;
import java.util.Comparator;
import java.util.List;

@Service
public class MessageHandler {

    private final OkHttpTelegramClient client;
    private final MessageSender sender;
    private final EanDbClient eanDb;

    public MessageHandler(OkHttpTelegramClient client, MessageSender sender) {
        this.client = client;
        this.sender = sender;
        this.eanDb = new EanDbClient(EnvUtil.getOrDefault("EANDB_TOKEN", ""));
    }

    public void handle(Update update) {
        if (update.getMessage() == null) return;
        var msg = update.getMessage();
        String chatId = msg.getChatId().toString();

        List<PhotoSize> photos = msg.getPhoto();
        if (photos != null && !photos.isEmpty()) {
            var largest = photos.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize, Comparator.nullsFirst(Integer::compareTo)))
                    .orElse(null);
            try {
                var tgFile = client.execute(new GetFile(largest.getFileId()));
                File downloaded = client.downloadFile(tgFile);
                String code = BarCodeReader.read(downloaded.getAbsolutePath());
                if (code == null) {
                    sender.send(chatId, "❌ Не вдалося розпізнати штрихкод.");
                    return;
                }

                var infoOpt = eanDb.byBarcode(code);
                if (infoOpt.isEmpty()) {
                    sender.send(chatId, "📦 Штрихкод: " + code + "\nДані про товар не знайдені.");
                    return;
                }

                ProductInfo p = infoOpt.get();
                StringBuilder sb = new StringBuilder();
                if (p.name() != null) sb.append("🛍️ ").append(p.name()).append('\n');
                sb.append("📦 Штрихкод: ").append(code).append('\n');
                if (p.grams() != null) sb.append("⚖️ Вага: ").append(p.grams()).append(" г\n");
                sb.append("🍽️ На 100 г:\n")
                        .append(" • Ккал: ").append(fmt(p.kcal100g())).append('\n')
                        .append(" • Білки: ").append(fmt(p.protein100g())).append(" г\n")
                        .append(" • Жири: ").append(fmt(p.fat100g())).append(" г\n")
                        .append(" • Вуглеводи: ").append(fmt(p.carbs100g())).append(" г");
                sender.send(chatId, sb.toString());
                return;
            } catch (Exception e) {
                sender.send(chatId, "⚠️ Помилка обробки фото: " + e.getMessage());
                return;
            }
        }

        String text = msg.getText();
        if (text != null && text.startsWith("/start")) {
            sender.send(chatId, "Привіт! Надішли фото зі штрихкодом продукта 📷");
        }
    }

    private static String fmt(Double d) {
        return d == null ? "—" :
                (d % 1 == 0 ? String.valueOf(d.intValue()) : String.format("%.1f", d));
    }
}
