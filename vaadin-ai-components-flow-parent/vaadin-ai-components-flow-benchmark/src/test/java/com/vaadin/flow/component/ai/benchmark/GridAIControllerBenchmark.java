/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.benchmark;

import java.util.List;

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
    AIBenchmark bench = new AIBenchmark();

    @Test
    void filtersAndSortsInOneRequest() {
        bench.score(() -> {
            try (var db = BenchmarkDatabase.customers()) {
                var grid = new Grid<AIDataRow>();
                var controller = new GridAIController(grid, db);
                try (var conversation = bench.conversation(grid, controller)) {
                    conversation.say(
                            "Show only the European customers, highest revenue first");
                }
                Assertions
                        .assertEquals(
                                List.of("Nordic Traders", "Alpine Foods",
                                        "Iberia Textiles"),
                                names(db, controller));
            }
        });
    }

    @Test
    void followUpNarrowsThePreviousResult() {
        bench.score(() -> {
            try (var db = BenchmarkDatabase.customers()) {
                var grid = new Grid<AIDataRow>();
                var controller = new GridAIController(grid, db);
                try (var conversation = bench.conversation(grid, controller)) {
                    conversation.say(
                            "Show only the European customers, highest revenue first");
                    conversation.say("Only keep the top two");
                }
                Assertions.assertEquals(
                        List.of("Nordic Traders", "Alpine Foods"),
                        names(db, controller));
            }
        });
    }

    private static List<Object> names(BenchmarkDatabase db,
            GridAIController controller) {
        var query = controller.getState().query();
        Assertions.assertNotNull(query,
                "the model never called update_grid_data");
        return BenchmarkDatabase.column(db.executeQuery(query), "name");
    }
}
