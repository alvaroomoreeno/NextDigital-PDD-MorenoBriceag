package com.breadhardit.travelagencykata;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StructuralPatternExercices {

    /*
     * Composite pattern:
     * A menu can contain individual foods and dishes. A dish can itself contain
     * menu components, so callers calculate calories through one abstraction.
     */

    interface MenuComponent {
        String name();

        long getCalories();
    }

    record Food(String name, long caloriesPer100g, long weightInGrams) implements MenuComponent {
        Food {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Food name is required");
            }
            if (caloriesPer100g < 0 || weightInGrams < 0) {
                throw new IllegalArgumentException("Calories and weight cannot be negative");
            }
        }

        @Override
        public long getCalories() {
            return caloriesPer100g * weightInGrams / 100;
        }
    }

    static class Dish implements MenuComponent {
        private final String name;
        private final List<MenuComponent> components;

        Dish(String name, List<MenuComponent> components) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Dish name is required");
            }
            this.name = name;
            this.components = List.copyOf(components);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public long getCalories() {
            return components.stream().mapToLong(MenuComponent::getCalories).sum();
        }
    }

    static class Menu implements MenuComponent {
        private final String name;
        private final List<MenuComponent> components;

        Menu(String name, List<MenuComponent> components) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Menu name is required");
            }
            this.name = name;
            this.components = List.copyOf(components);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public long getCalories() {
            return components.stream().mapToLong(MenuComponent::getCalories).sum();
        }
    }

    @Test
    void calculatesCaloriesForAnIndividualFood() {
        Food apple = new Food("APPLE", 34, 180);

        assertEquals(61, apple.getCalories());
    }

    @Test
    void calculatesCaloriesForADishContainingFoods() {
        Food potato = new Food("POTATO", 80, 300);
        Food bread = new Food("BREAD", 320, 100);
        Food burger = new Food("BURGER", 340, 120);
        Dish completeBurger = new Dish("COMPLETE BURGER", List.of(potato, bread, burger));

        assertEquals(1_048, completeBurger.getCalories());
    }

    @Test
    void calculatesCaloriesForAMenuContainingDishesAndIndividualFoods() {
        Food potato = new Food("POTATO", 80, 300);
        Food bread = new Food("BREAD", 320, 100);
        Food burger = new Food("BURGER", 340, 120);
        Food lettuce = new Food("LETTUCE", 16, 120);
        Food tomato = new Food("TOMATO", 85, 50);
        Food ketchup = new Food("KETCHUP", 180, 15);
        Food apple = new Food("APPLE", 34, 180);

        Dish completeBurger = new Dish("COMPLETE BURGER", List.of(potato, bread, burger));
        Dish greenSalad = new Dish("GREEN SALAD", List.of(lettuce, tomato));
        Menu menu = new Menu("LUNCH MENU", List.of(greenSalad, completeBurger, ketchup, apple));

        assertEquals(1_161, menu.getCalories());
    }

    @Test
    void supportsNestedDishes() {
        Food tomato = new Food("TOMATO", 85, 100);
        Food lettuce = new Food("LETTUCE", 16, 100);
        Dish salad = new Dish("SALAD", List.of(tomato, lettuce));
        Menu nestedMenu = new Menu("NESTED MENU", List.of(salad, new Food("APPLE", 34, 100)));

        assertEquals(135, nestedMenu.getCalories());
    }

    @Test
    void rejectsInvalidFoodData() {
        assertThrows(IllegalArgumentException.class, () -> new Food(" ", 10, 100));
        assertThrows(IllegalArgumentException.class, () -> new Food("APPLE", -1, 100));
    }
}
