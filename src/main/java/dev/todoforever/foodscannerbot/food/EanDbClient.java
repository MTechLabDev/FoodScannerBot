package dev.todoforever.foodscannerbot.food;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.Optional;

public final class EanDbClient {

    private static final ObjectMapper M = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final String API = "https://ean-db.com/api/v2/product/";

    private final String jwt;

    public EanDbClient(String jwt) {
        this.jwt = jwt;
    }

    public Optional<ProductInfo> byBarcode(String ean13) {
        try {
            var req = HttpRequest.newBuilder(URI.create(API + ean13))
                    .header("Authorization", "Bearer " + jwt)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            var res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return Optional.empty();

            JsonNode root = M.readTree(res.body());
            JsonNode product = root.path("product");
            if (product.isMissingNode() || product.isNull()) return Optional.empty();

            String name = bestTitle(product.path("titles"));
            if (name == null) {
                String brand = product.path("manufacturer").path("titles").path("uk").asText(null);
                if (brand == null) brand = product.path("manufacturer").path("titles").path("ru").asText(null);
                name = (brand != null ? brand + " " : "") + ean13;
            }

            JsonNode nutr = product.path("metadata").path("food").path("nutrimentsPer100Grams");
            Double kcal = readKcal(nutr);
            Double protein = readDouble(nutr, "proteins");
            Double fat = readDouble(nutr, "fat");
            Double carb = firstPresent(nutr, "carbohydrates", "carbs", "carbohydrate");

            if (carb == null && kcal != null) {
                double p = protein != null ? protein : 0.0;
                double f = fat != null ? fat : 0.0;
                double c = (kcal - 9.0 * f - 4.0 * p) / 4.0;
                if (c > 0 && Double.isFinite(c)) carb = round1(c);
            }

            return Optional.of(new ProductInfo(name, 100, kcal, protein, fat, carb));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String bestTitle(JsonNode titles) {
        if (titles.isMissingNode() || titles.isNull()) return null;
        for (String k : new String[]{"uk", "ru", "en"}) {
            String v = titles.path(k).asText(null);
            if (v != null && !v.isBlank()) return v;
        }
        var it = titles.fieldNames();
        while (it.hasNext()) {
            String k = it.next();
            String v = titles.path(k).asText(null);
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private static Double readKcal(JsonNode nutr100) {
        Double kcal = readEq(nutr100.path("energy"), "kcal");
        if (kcal != null) return kcal;
        Double kJ = readEq(nutr100.path("energy"), "kj");
        return kJ != null ? kJ / 4.184d : null;
    }

    private static Double readDouble(JsonNode nutr100, String key) {
        JsonNode n = nutr100.path(key);
        if (n.isMissingNode() || n.isNull()) return null;
        return readEq(n, "grams");
    }

    private static Double firstPresent(JsonNode nutr100, String... keys) {
        for (String k : keys) {
            Double v = readDouble(nutr100, k);
            if (v != null) return v;
        }
        return null;
    }

    private static Double readEq(JsonNode node, String expectedUnit) {
        if (node.isMissingNode() || node.isNull()) return null;
        JsonNode eq = node.path("equals");
        if (eq.isMissingNode() || eq.isNull()) return null;
        String unit = eq.path("unit").asText(null);
        if (unit == null) return null;
        if (!unit.equalsIgnoreCase(expectedUnit) &&
                !normalizeUnit(unit).equals(normalizeUnit(expectedUnit))) return null;
        String val = eq.path("value").asText(null);
        if (val == null) return null;
        try { return Double.valueOf(val); } catch (NumberFormatException e) { return null; }
    }

    private static String normalizeUnit(String u) {
        return u.toLowerCase(Locale.ROOT).replace("_", "");
    }

    private static Double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
