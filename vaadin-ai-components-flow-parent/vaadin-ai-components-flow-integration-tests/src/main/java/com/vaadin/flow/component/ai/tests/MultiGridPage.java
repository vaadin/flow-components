/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.ai.tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.ai.grid.GridAIController;
import com.vaadin.flow.component.ai.grid.GridAITools;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.DatabaseProviderAITools;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.SpringAILLMProvider;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.router.Route;

/**
 * Integration test page for multi-grid support via {@code gridId}. Two grids
 * share a single orchestrator — the LLM uses {@code gridId} to target specific
 * grids.
 * <p>
 * Navigate to: {@code http://localhost:8080/vaadin-ai/multi-grid}
 * </p>
 */
@Route("vaadin-ai/multi-grid")
public class MultiGridPage extends Div {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MultiGridPage.class);

    private static final List<String> EXAMPLE_PROMPTS = List.of(
            // Target specific grid by name
            "Show all employees with department and salary in the employees grid",
            "Show all products with price and stock in the products grid",
            // Both grids at once
            "Show employees in the employees grid and products in the products grid",
            // Update one, leave the other
            "Show only active employees in the employees grid",
            "Show products cheaper than 100 in the products grid",
            // Aggregations per grid
            "Show employee count by department in the employees grid, and product count by category in the products grid",
            // Joins — single grid
            "Show orders with employee name and product name in the employees grid",
            // Explicit gridId
            "In the products grid, show the top 3 most expensive products",
            // Cross-grid: use one grid's context to update the other
            "Show employees in the employees grid, then show the products ordered by those employees in the products grid",
            "Check what department is shown in the employees grid and show products ordered by that department in the products grid");

    public MultiGridPage() {
        setSizeFull();
        getStyle().set("display", "flex").set("flex-direction", "column")
                .set("padding", "10px").set("gap", "10px")
                .set("box-sizing", "border-box");

        var apiKey = System.getenv("OPENAI_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            add(new Span("Error: OPENAI_KEY environment variable is not set."));
            return;
        }

        var dbProvider = createDatabaseProvider();

        // --- Two grids ---
        var employeesGrid = new Grid<Map<String, Object>>();
        employeesGrid.setWidthFull();
        employeesGrid.getStyle().set("flex", "1").set("min-height", "200px");

        var productsGrid = new Grid<Map<String, Object>>();
        productsGrid.setWidthFull();
        productsGrid.getStyle().set("flex", "1").set("min-height", "200px");

        // --- Two controllers (one per grid, for rendering) ---
        var employeesController = new GridAIController(employeesGrid,
                dbProvider);
        var productsController = new GridAIController(productsGrid, dbProvider);

        // Map of gridId -> controller
        var controllers = Map.of("employees", employeesController, "products",
                productsController);

        // --- Multi-grid AIController using GridAITools.Callbacks ---
        var multiGridController = new AIController() {
            @Override
            public List<LLMProvider.ToolSpec> getTools() {
                var tools = new ArrayList<LLMProvider.ToolSpec>();
                tools.addAll(DatabaseProviderAITools.createAll(dbProvider));
                tools.addAll(GridAITools.createAll(new GridAITools.Callbacks() {
                    @Override
                    public String getState(String gridId) {
                        var ctrl = controllers.get(gridId);
                        if (ctrl == null) {
                            throw new IllegalArgumentException(
                                    "Unknown grid: " + gridId);
                        }
                        var state = ctrl.getState();
                        if (state == null) {
                            return "{\"gridId\":\"" + gridId
                                    + "\",\"status\":\"empty\"}";
                        }
                        return "{\"gridId\":\"" + gridId + "\",\"query\":\""
                                + state.query().replace("\"", "\\\"") + "\"}";
                    }

                    @Override
                    public void updateData(String gridId, String query) {
                        var ctrl = controllers.get(gridId);
                        if (ctrl == null) {
                            throw new IllegalArgumentException(
                                    "Unknown grid: " + gridId);
                        }
                        var node = JacksonUtils.createObjectNode();
                        node.put("query", query);
                        var updateTool = ctrl.getTools().stream().filter(
                                t -> t.getName().equals("update_grid_data"))
                                .findFirst().orElseThrow();
                        updateTool.execute(node.toString());
                    }

                    @Override
                    public Set<String> getGridIds() {
                        return controllers.keySet();
                    }
                }));
                return tools;
            }

            @Override
            public void onRequestCompleted() {
                controllers.values()
                        .forEach(GridAIController::onRequestCompleted);
            }
        };

        // --- Chat UI ---
        var messageList = new MessageList();
        messageList.getStyle().set("flex", "1").set("overflow", "auto");
        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        var systemPrompt = """
                You have two grids to populate with data:
                - gridId "employees" — for employee/department/order data
                - gridId "products" — for product data

                ALWAYS specify the gridId parameter when calling get_grid_state or update_grid_data.

                """
                + GridAIController.getSystemPrompt() + "\n\nDatabase schema:\n"
                + dbProvider.getSchema();

        var openAiApi = new OpenAiApi.Builder().apiKey(apiKey).build();
        var chatOptions = OpenAiChatOptions.builder().model("gpt-4.1-mini")
                .build();
        var chatModel = OpenAiChatModel.builder().openAiApi(openAiApi)
                .defaultOptions(chatOptions).build();
        var llmProvider = new SpringAILLMProvider(chatModel);

        // --- Prompt selector ---
        var promptSelect = new NativeSelect(EXAMPLE_PROMPTS);
        promptSelect.getStyle().set("padding", "5px").set("font-size", "14px")
                .set("flex", "1").set("min-width", "0");

        var orchestratorRef = new AtomicReference<AIOrchestrator>();
        var sendButton = new NativeButton("Send", e -> {
            var prompt = promptSelect.getValue();
            var orch = orchestratorRef.get();
            if (!prompt.isBlank() && orch != null) {
                LOGGER.info("Sending: {}", prompt);
                orch.prompt(prompt);
            }
        });
        sendButton.getStyle().set("padding", "5px 15px");

        var promptBar = new Div(new Span("Prompt: "), promptSelect, sendButton);
        promptBar.getStyle().set("display", "flex").set("align-items", "center")
                .set("gap", "8px");

        // --- Layout ---
        var chatPanel = new Div(messageList, messageInput);
        chatPanel.getStyle().set("display", "flex")
                .set("flex-direction", "column").set("width", "400px")
                .set("min-width", "300px").set("gap", "5px");

        var employeesPanel = new Div(new H3("Employees Grid"), employeesGrid);
        employeesPanel.getStyle().set("display", "flex")
                .set("flex-direction", "column").set("flex", "1");

        var productsPanel = new Div(new H3("Products Grid"), productsGrid);
        productsPanel.getStyle().set("display", "flex")
                .set("flex-direction", "column").set("flex", "1");

        var gridsPanel = new Div(employeesPanel, productsPanel);
        gridsPanel.getStyle().set("display", "flex")
                .set("flex-direction", "column").set("flex", "1")
                .set("gap", "10px").set("min-height", "0")
                .set("overflow", "auto");

        var mainLayout = new Div(chatPanel, gridsPanel);
        mainLayout.getStyle().set("display", "flex").set("flex", "1")
                .set("gap", "10px").set("min-height", "0");

        add(promptBar, mainLayout);

        var orchestrator = AIOrchestrator.builder(llmProvider, systemPrompt)
                .withInput(messageInput).withMessageList(messageList)
                .withController(multiGridController)
                .withResponseCompleteListener(
                        event -> LOGGER.info("LLM response complete"))
                .build();
        orchestratorRef.set(orchestrator);
    }

    // --- NativeSelect ---

    @Tag("select")
    private static class NativeSelect extends Component {

        NativeSelect(List<String> options) {
            for (var option : options) {
                var optionElement = new Element("option");
                optionElement.setAttribute("value", option);
                optionElement.setText(option);
                getElement().appendChild(optionElement);
            }
            getElement().addPropertyChangeListener("value", "change", e -> {
            });
        }

        String getValue() {
            return getElement().getProperty("value", "");
        }
    }

    // --- Database ---

    private static DatabaseProvider createDatabaseProvider() {
        try {
            var conn = DriverManager.getConnection(
                    "jdbc:h2:mem:multigridtest;DB_CLOSE_DELAY=-1");
            createTables(conn);
            return new H2DatabaseProvider(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize H2", e);
        }
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    private static void createTables(Connection conn) throws SQLException {
        try (var stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");

            stmt.execute(
                    "CREATE TABLE departments (dept_id INT PRIMARY KEY, dept_name VARCHAR)");
            stmt.execute("INSERT INTO departments VALUES "
                    + "(1, 'Engineering'), (2, 'Sales'), (3, 'Marketing'), (4, 'Support')");

            stmt.execute(
                    "CREATE TABLE employees (emp_id INT PRIMARY KEY, emp_name VARCHAR, dept_id INT, salary DECIMAL, hire_date DATE, active BOOLEAN)");
            stmt.execute("INSERT INTO employees VALUES "
                    + "(1, 'Alice Johnson', 1, 95000, '2020-03-15', true), "
                    + "(2, 'Bob Smith', 1, 88000, '2021-07-01', true), "
                    + "(3, 'Carol White', 2, 72000, '2019-11-20', true), "
                    + "(4, 'Dave Brown', 2, 68000, '2022-01-10', false), "
                    + "(5, 'Eve Davis', 3, 75000, '2020-06-01', true), "
                    + "(6, 'Frank Miller', 1, 102000, '2018-09-15', true), "
                    + "(7, 'Grace Lee', 4, 65000, '2023-02-28', true), "
                    + "(8, 'Henry Wilson', 3, 71000, '2021-04-12', false), "
                    + "(9, 'Ivy Chen', 2, 78000, '2020-08-05', true), "
                    + "(10, 'Jack Taylor', 4, 62000, '2022-11-30', true)");

            stmt.execute(
                    "CREATE TABLE products (product_id INT PRIMARY KEY, product_name VARCHAR, category VARCHAR, price DECIMAL, stock INT, available BOOLEAN)");
            stmt.execute("INSERT INTO products VALUES "
                    + "(1, 'Laptop Pro', 'Electronics', 1299.99, 45, true), "
                    + "(2, 'Wireless Mouse', 'Electronics', 29.99, 200, true), "
                    + "(3, 'Standing Desk', 'Furniture', 549.00, 30, true), "
                    + "(4, 'Monitor 27\"', 'Electronics', 399.99, 75, true), "
                    + "(5, 'Ergonomic Chair', 'Furniture', 449.00, 40, true), "
                    + "(6, 'Keyboard Mech', 'Electronics', 89.99, 150, false), "
                    + "(7, 'Desk Lamp', 'Furniture', 34.99, 100, true), "
                    + "(8, 'USB-C Hub', 'Electronics', 49.99, 120, true)");

            stmt.execute(
                    "CREATE TABLE orders (order_id INT PRIMARY KEY, emp_id INT, product_id INT, quantity INT, order_date DATE)");
            stmt.execute("INSERT INTO orders VALUES "
                    + "(1, 1, 1, 1, '2024-01-15'), "
                    + "(2, 3, 2, 2, '2024-01-20'), "
                    + "(3, 5, 3, 1, '2024-02-01'), "
                    + "(4, 2, 4, 1, '2024-02-10'), "
                    + "(5, 1, 6, 1, '2024-02-15'), "
                    + "(6, 4, 5, 1, '2024-03-01'), "
                    + "(7, 6, 1, 1, '2024-03-10'), "
                    + "(8, 7, 7, 3, '2024-03-15'), "
                    + "(9, 8, 2, 1, '2024-03-20'), "
                    + "(10, 9, 8, 2, '2024-04-01')");
        }
    }

    private record H2DatabaseProvider(
            Connection connection) implements DatabaseProvider {

        @Override
        public String getSchema() {
            return """
                    H2 SQL dialect. Tables:
                    - departments(dept_id INT PK, dept_name VARCHAR)
                    - employees(emp_id INT PK, emp_name VARCHAR, dept_id INT FK, salary DECIMAL, hire_date DATE, active BOOLEAN)
                    - products(product_id INT PK, product_name VARCHAR, category VARCHAR, price DECIMAL, stock INT, available BOOLEAN)
                    - orders(order_id INT PK, emp_id INT FK→employees, product_id INT FK→products, quantity INT, order_date DATE)
                    """;
        }

        @Override
        public List<Map<String, Object>> executeQuery(String sql) {
            var results = new ArrayList<Map<String, Object>>();
            try (var stmt = connection.createStatement();
                    var rs = stmt.executeQuery(sql)) {
                var meta = rs.getMetaData();
                var columnCount = meta.getColumnCount();
                while (rs.next()) {
                    var row = new LinkedHashMap<String, Object>();
                    for (var i = 1; i <= columnCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Query failed: " + sql, e);
            }
            return results;
        }
    }
}
