/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.benchmark;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ai.grid.AIDataRow;
import com.vaadin.flow.component.ai.grid.GridAIController;
import com.vaadin.flow.component.grid.Grid;

/**
 * Benchmarks {@link GridAIController}: the query the model writes is run
 * against the same in-memory database and scored by the rows it returns.
 */
@EnabledIfEnvironmentVariable(named = AIBenchmark.MODEL_VARIABLE, matches = ".+")
class GridAIControllerBenchmark {

    @RegisterExtension
    static AIBenchmark bench = new AIBenchmark();

    private BenchmarkDatabase db;
    private GridAIController controller;
    private AIBenchmark.Conversation conversation;

    @BeforeEach
    void startConversation() {
        db = BenchmarkDatabase.create();
        var grid = new Grid<AIDataRow>();
        controller = new GridAIController(grid, db);
        conversation = bench.conversation(grid, controller);
    }

    @Test
    void addsColumnKeepingTheEarlierFilter() {
        conversation.say(
                "Show the European customers and their revenue, highest first");
        conversation.say("Add their email address to the table");
        var rows = rows();
        Assertions.assertEquals(
                List.of("Nordic Traders", "Alpine Foods", "Iberia Textiles"),
                BenchmarkDatabase.column(rows, "name"),
                "the filter and the order of the first turn have to survive");
        var emailColumn = rows.getFirst().keySet().stream().filter(
                label -> label.toLowerCase(Locale.ROOT).contains("mail"))
                .findFirst();
        Assertions.assertTrue(emailColumn.isPresent(),
                () -> "no email column, got " + rows.getFirst().keySet());
        Assertions.assertEquals("orders@nordic.example",
                rows.getFirst().get(emailColumn.get()));
    }

    @Test
    void findsCustomersWithoutRecentOrders() {
        // The order dates and the customer names are in different tables, so
        // the grid needs a join or a subquery
        conversation.say(
                "List the customers that have not placed any order in 2026");
        Assertions.assertEquals(Set.of("Iberia Textiles", "Sakura Robotics"),
                new HashSet<>(names()));
    }

    @Test
    void groupsContactColumnsUnderHeading() {
        conversation.say("""
                Show every customer's name, email and phone. Put \
                the email and phone columns under a shared \
                column group called Contact.""");
        var rows = rows();
        Assertions.assertEquals(6, rows.size());
        var grouped = rows.getFirst().keySet().stream()
                .filter(label -> label.startsWith("contact.")).toList();
        Assertions.assertEquals(2, grouped.size(),
                () -> "expected two Contact.* columns, got "
                        + rows.getFirst().keySet());
    }

    @Test
    void showsAllRowsWithReadableColumns() {
        conversation.say("Show all the orders.");
        var rows = rows();
        var query = controller.getState().query();
        Assertions.assertEquals(8, rows.size(),
                () -> "every order, with " + query);
        Assertions.assertFalse(
                query.matches("(?is).*\\bselect\\s+(\\w+\\.)?\\*.*"),
                () -> "SELECT * instead of listed columns: " + query);
        Assertions.assertFalse(query.matches("(?is).*\\b(limit|offset)\\b.*"),
                () -> "the grid pages the rows itself: " + query);
        var labels = rows.getFirst().keySet();
        Assertions.assertTrue(
                labels.stream().noneMatch(label -> label.contains("_")),
                () -> "raw column names instead of readable aliases: "
                        + labels);
        Assertions.assertTrue(
                labels.stream().noneMatch(label -> label.contains(".")),
                () -> "column groups nobody asked for: " + labels);
    }

    private List<Map<String, Object>> rows() {
        var query = controller.getState().query();
        Assertions.assertNotNull(query,
                "the model never called update_grid_data");
        return db.executeQuery(query);
    }

    private List<Object> names() {
        return BenchmarkDatabase.column(rows(), "name");
    }
}
