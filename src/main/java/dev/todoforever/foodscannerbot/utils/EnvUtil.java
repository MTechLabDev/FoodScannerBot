package dev.todoforever.foodscannerbot.utils;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Optional;

public final class EnvUtil {
    private static final Dotenv DOTENV =
            Dotenv.configure().ignoreIfMissing().load();

    private EnvUtil() { }

    public static Optional<String> get(String key) {
        return Optional.ofNullable(System.getenv(key))
                .or(() -> Optional.ofNullable(System.getProperty(key)))
                .or(() -> Optional.ofNullable(DOTENV.get(key)));
    }

    public static String getOrDefault(String key, String defaultValue) {
        return get(key).orElse(defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return get(key)
                .map(String::trim)
                .filter(v -> v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false"))
                .map(Boolean::parseBoolean)
                .orElse(defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return get(key)
                .map(String::trim)
                .flatMap(v -> {
                    try { return Optional.of(Integer.parseInt(v)); }
                    catch (NumberFormatException e) { return Optional.empty(); }
                })
                .orElse(defaultValue);
    }

    public static long getLong(String key, long defaultValue) {
        return get(key)
                .map(String::trim)
                .flatMap(v -> {
                    try { return Optional.of(Long.parseLong(v)); }
                    catch (NumberFormatException e) { return Optional.empty(); }
                })
                .orElse(defaultValue);
    }
}
