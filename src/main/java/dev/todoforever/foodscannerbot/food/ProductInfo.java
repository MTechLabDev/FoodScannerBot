package dev.todoforever.foodscannerbot.food;

public record ProductInfo(
        String name,
        Integer grams,
        Double kcal100g,
        Double protein100g,
        Double fat100g,
        Double carbs100g
) {}