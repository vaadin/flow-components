/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.masterdetaillayout.tests.demo;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class DemoData {
    public record Product(long id, String name, String category, double price,
            LocalDate dateAdded) {
    }

    public static final List<Product> PRODUCTS = List.of(
            new Product(1, "Apple", "Fruit", 1.99, LocalDate.of(2019, 1, 1)),
            new Product(2, "Banana", "Fruit", 2.99, LocalDate.of(2019, 1, 2)),
            new Product(3, "Carrot", "Vegetable", 3.99,
                    LocalDate.of(2019, 1, 3)),
            new Product(4, "Eggplant", "Vegetable", 5.99,
                    LocalDate.of(2019, 1, 5)),
            new Product(5, "Orange", "Fruit", 2.49, LocalDate.of(2019, 1, 6)),
            new Product(6, "Broccoli", "Vegetable", 4.29,
                    LocalDate.of(2019, 1, 7)),
            new Product(7, "Grapes", "Fruit", 3.79, LocalDate.of(2019, 1, 8)),
            new Product(8, "Cucumber", "Vegetable", 2.89,
                    LocalDate.of(2019, 1, 9)),
            new Product(9, "Strawberry", "Fruit", 4.99,
                    LocalDate.of(2019, 1, 10)),
            new Product(10, "Tomato", "Vegetable", 2.19,
                    LocalDate.of(2019, 1, 11)),
            new Product(11, "Pineapple", "Fruit", 3.69,
                    LocalDate.of(2019, 1, 12)),
            new Product(12, "Spinach", "Vegetable", 2.79,
                    LocalDate.of(2019, 1, 13)),
            new Product(13, "Blueberry", "Fruit", 6.49,
                    LocalDate.of(2019, 1, 14)),
            new Product(14, "Bell Pepper", "Vegetable", 1.99,
                    LocalDate.of(2019, 1, 15)),
            new Product(15, "Watermelon", "Fruit", 7.99,
                    LocalDate.of(2019, 1, 16)),
            new Product(16, "Zucchini", "Vegetable", 2.49,
                    LocalDate.of(2019, 1, 17)),
            new Product(17, "Mango", "Fruit", 4.79, LocalDate.of(2019, 1, 18)),
            new Product(18, "Asparagus", "Vegetable", 3.29,
                    LocalDate.of(2019, 1, 19)),
            new Product(19, "Cherry", "Fruit", 3.99, LocalDate.of(2019, 1, 20)),
            new Product(20, "Potato", "Vegetable", 1.49,
                    LocalDate.of(2019, 1, 21)),
            new Product(21, "Pear", "Fruit", 2.59, LocalDate.of(2019, 1, 22)),
            new Product(22, "Cantaloupe", "Fruit", 3.99,
                    LocalDate.of(2019, 1, 23)),
            new Product(23, "Cauliflower", "Vegetable", 2.19,
                    LocalDate.of(2019, 1, 24)),
            new Product(24, "Lemon", "Fruit", 1.79, LocalDate.of(2019, 1, 25)),
            new Product(25, "Cabbage", "Vegetable", 1.69,
                    LocalDate.of(2019, 1, 26)),
            new Product(26, "Raspberry", "Fruit", 5.29,
                    LocalDate.of(2019, 1, 27)),
            new Product(27, "Onion", "Vegetable", 1.29,
                    LocalDate.of(2019, 1, 28)),
            new Product(28, "Kiwi", "Fruit", 2.69, LocalDate.of(2019, 1, 29)),
            new Product(29, "Green Bean", "Vegetable", 2.39,
                    LocalDate.of(2019, 1, 30)),
            new Product(30, "Blackberry", "Fruit", 4.49,
                    LocalDate.of(2019, 1, 31)),
            new Product(31, "Sweet Potato", "Vegetable", 1.99,
                    LocalDate.of(2019, 2, 1)),
            new Product(32, "Peach", "Fruit", 3.49, LocalDate.of(2019, 2, 2)),
            new Product(33, "Celery", "Vegetable", 1.89,
                    LocalDate.of(2019, 2, 3)),
            new Product(34, "Grapefruit", "Fruit", 2.99,
                    LocalDate.of(2019, 2, 4)),
            new Product(35, "Radish", "Vegetable", 1.29,
                    LocalDate.of(2019, 2, 5)),
            new Product(36, "Apricot", "Fruit", 3.99, LocalDate.of(2019, 2, 6)),
            new Product(37, "Brussels Sprout", "Vegetable", 2.49,
                    LocalDate.of(2019, 2, 7)),
            new Product(38, "Artichoke", "Vegetable", 3.99,
                    LocalDate.of(2019, 2, 9)),
            new Product(39, "Lime", "Fruit", 1.49, LocalDate.of(2019, 2, 10)),
            new Product(40, "Beet", "Vegetable", 1.99,
                    LocalDate.of(2019, 2, 11)),
            new Product(41, "Plum", "Fruit", 2.99, LocalDate.of(2019, 2, 12)),
            new Product(42, "Corn", "Vegetable", 1.49,
                    LocalDate.of(2019, 2, 13)),
            new Product(43, "Pomegranate", "Fruit", 3.99,
                    LocalDate.of(2019, 2, 14)),
            new Product(44, "Garlic", "Vegetable", 1.29,
                    LocalDate.of(2019, 2, 15)),
            new Product(45, "Papaya", "Fruit", 4.99, LocalDate.of(2019, 2, 16)),
            new Product(46, "Green Onion", "Vegetable", 1.49,
                    LocalDate.of(2019, 2, 17)),
            new Product(47, "Ginger", "Vegetable", 1.99,
                    LocalDate.of(2019, 2, 19)),
            new Product(48, "Parsley", "Vegetable", 1.29,
                    LocalDate.of(2019, 2, 20)),
            new Product(49, "Parsnip", "Vegetable", 1.99,
                    LocalDate.of(2019, 2, 21)),
            new Product(50, "Peas", "Vegetable", 1.99,
                    LocalDate.of(2019, 2, 24)));

    public static List<Product> getByCategory(String category) {
        return PRODUCTS.stream()
                .filter(product -> product.category.equals(category)).toList();
    }

    public static Product getById(Long id) {
        return PRODUCTS.stream()
                .filter(product -> Objects.equals(product.id, id)).findFirst()
                .orElse(null);
    }
}
