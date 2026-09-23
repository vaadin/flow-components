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
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
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

    @Test
    void followUpNarrowsThePreviousResult() {
        var db = BenchmarkDatabase.customers();
        var grid = new Grid<AIDataRow>();
        var controller = new GridAIController(grid, db);
        var conversation = bench.conversation(grid, controller);
        conversation
                .say("Show only the European customers, highest revenue first");
        conversation.say("Only keep the top two");
        Assertions.assertEquals(List.of("Nordic Traders", "Alpine Foods"),
                names(db, controller));
    }

    @Test
    void aggregatesRegionsAboveThreshold() {
        var db = BenchmarkDatabase.customers();
        var grid = new Grid<AIDataRow>();
        var controller = new GridAIController(grid, db);
        bench.conversation(grid, controller).say("""
                Which regions have a combined customer revenue \
                above one million? Show the region and its total, \
                largest total first.""");
        var rows = rows(db, controller);
        Assertions.assertEquals(List.of("North America", "Europe"),
                BenchmarkDatabase.column(rows, "region"));
        Assertions.assertEquals(List.of(1_600_000.0, 1_400_000.0),
                numbers(rows, "total"));
    }

    @Test
    void findsCustomersWithoutRecentOrders() {
        var db = BenchmarkDatabase.customers();
        var grid = new Grid<AIDataRow>();
        var controller = new GridAIController(grid, db);
        bench.conversation(grid, controller).say(
                "List the customers that have not placed any order in 2026");
        Assertions.assertEquals(Set.of("Iberia Textiles", "Sakura Robotics"),
                new HashSet<>(names(db, controller)));
    }

    @Test
    void groupsContactColumnsUnderHeading() {
        var db = BenchmarkDatabase.customers();
        var grid = new Grid<AIDataRow>();
        var controller = new GridAIController(grid, db);
        bench.conversation(grid, controller).say("""
                Show every customer's name, email and phone. Put \
                the email and phone columns under a shared \
                column group called Contact.""");
        var rows = rows(db, controller);
        Assertions.assertEquals(6, rows.size());
        var grouped = rows.getFirst().keySet().stream()
                .filter(label -> label.startsWith("contact.")).toList();
        Assertions.assertEquals(2, grouped.size(),
                () -> "expected two Contact.* columns, got "
                        + rows.getFirst().keySet());
    }

    private static List<Map<String, Object>> rows(BenchmarkDatabase db,
            GridAIController controller) {
        var query = controller.getState().query();
        Assertions.assertNotNull(query,
                "the model never called update_grid_data");
        return db.executeQuery(query);
    }

    private static List<Double> numbers(List<Map<String, Object>> rows,
            String column) {
        return BenchmarkDatabase.column(rows, column).stream()
                .map(value -> ((Number) value).doubleValue()).toList();
    }

    private static List<Object> names(BenchmarkDatabase db,
            GridAIController controller) {
        return BenchmarkDatabase.column(rows(db, controller), "name");
    }
}
