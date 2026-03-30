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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.ai.provider.SpringAILLMProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.grid.GridAIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

/**
 * Integration test page for GridAIController with session-based state
 * persistence. Orchestrator history and grid state are saved to the
 * VaadinSession after each LLM response and restored on page refresh.
 * <p>
 * Navigate to: {@code http://localhost:8080/vaadin-ai/grid}
 * </p>
 */
@Route("vaadin-ai/grid")
public class GridDataPage extends Div {

    private enum Model {
        GPT_4O_MINI("gpt-4o-mini"),
        GPT_4_1_MINI("gpt-4.1-mini"),
        GPT_4_1_NANO("gpt-4.1-nano"),
        GPT_5_4_MINI("gpt-5.4-mini"),
        GPT_5_4_NANO("gpt-5.4-nano");

        private final String name;

        Model(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GridDataPage.class);

    private static final String SESSION_KEY_GRID_STATE = "gridDataPage.gridState";
    private static final String SESSION_KEY_HISTORY = "gridDataPage.history";
    private static final String SESSION_KEY_ATTACHMENTS = "gridDataPage.attachments";

    private static final List<String> EXAMPLE_PROMPTS = List.of(
            // --- Basic queries ---
            "Show all employees with their department name and salary",
            "Show all products sorted by price descending",

            // --- Joins ---
            "Show orders with employee name, product name, quantity, and total price",

            // --- Aggregations ---
            "Show employee count and average salary by department",
            "Show total revenue per product (quantity * price)",

            // --- Filtering (LLM adds WHERE) ---
            "Show employees in the Engineering department",
            "Show products that cost more than 50",
            "Show active employees only",

            // --- Sorting (LLM adds ORDER BY) ---
            "Show the top 3 highest-paid employees",
            "Show all departments with employee count, sorted by count descending",

            // --- Built-in renderers: LocalDateRenderer ---
            "Show employee name and hire date",

            // --- Built-in renderers: NumberRenderer (right-aligned) ---
            "Show product name, price, and stock",

            // --- Built-in renderers: Boolean (Yes/No) ---
            "Show employee name, department, and active status",

            // --- Mixed types (date + number + boolean + text) ---
            "Show employee name, hire date, salary, active status, and department",
            "Show product name, category, price, stock, and available status",

            // --- Empty result (tests empty state message) ---
            "Show employees with salary over 200000",
            "Show products in the 'Automotive' category",

            // --- Column grouping ---
            "Show employees grouped by Employee info (name, department) and Compensation info (salary, hire date)",
            "Show orders with columns grouped by Person, Product, and Order details",

            // --- Column resizing (verify columns are draggable) ---
            "Show all columns from the employees table",

            // --- Custom renderer (SALARY column renders as currency) ---
            "Show employee name and salary",

            // --- Multi-table aggregations ---
            "Show each department with total number of orders placed by its employees",
            "Show each employee with the total amount they spent on orders (sum of quantity * product price)",
            "Show the most popular product category by total quantity ordered, with average product price",
            "Show departments where total order spending exceeds 1000, with employee count and total spent",
            "Show each product with how many different employees ordered it and total quantity");

    private final String apiKey;
    private DatabaseProvider dbProvider;
    private String systemPrompt;
    private Grid<Map<String, Object>> grid;
    private MessageList messageList;
    private MessageInput messageInput;
    private AIOrchestrator orchestrator;
    private GridAIController gridController;

    public GridDataPage() {
        setSizeFull();
        getStyle().set("display", "flex").set("flex-direction", "column")
                .set("padding", "10px").set("gap", "10px")
                .set("box-sizing", "border-box");

        apiKey = System.getenv("OPENAI_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            add(new Span("Error: OPENAI_KEY environment variable is not set."));
            return;
        }

        dbProvider = createDatabaseProvider();
        systemPrompt = GridAIController.getSystemPrompt()
                + "\n\nDatabase schema:\n" + dbProvider.getSchema();

        grid = new Grid<>();
        grid.setWidthFull();
        grid.getStyle().set("flex", "1").set("min-height", "300px");

        messageList = new MessageList();
        messageList.getStyle().set("flex", "1").set("overflow", "auto");
        messageInput = new MessageInput();
        messageInput.setWidthFull();

        // --- Prompt selector ---
        var promptSelect = new NativeSelect(EXAMPLE_PROMPTS);
        promptSelect.getStyle().set("padding", "5px").set("font-size", "14px")
                .set("flex", "1").set("min-width", "0");

        var sendButton = new NativeButton("Send", e -> {
            var prompt = promptSelect.getValue();
            if (!prompt.isBlank() && orchestrator != null) {
                LOGGER.info("Sending: {}", prompt);
                orchestrator.prompt(prompt);
            }
        });
        sendButton.getStyle().set("padding", "5px 15px");

        var promptBar = new Div(new Span("Prompt: "), promptSelect, sendButton);
        promptBar.getStyle().set("display", "flex").set("align-items", "center")
                .set("gap", "8px");

        var chatPanel = new Div(messageList, messageInput);
        chatPanel.getStyle().set("display", "flex")
                .set("flex-direction", "column").set("width", "400px")
                .set("min-width", "300px").set("gap", "5px");

        var mainLayout = new Div(chatPanel, grid);
        mainLayout.getStyle().set("display", "flex").set("flex", "1")
                .set("gap", "10px").set("min-height", "0");

        add(promptBar, mainLayout);

        // Restore or build fresh
        buildOrchestrator(Model.GPT_5_4_MINI);
    }

    @SuppressWarnings("unchecked")
    private void buildOrchestrator(Model model) {
        LOGGER.info("Building orchestrator with model: {}", model.getName());

        var openAiApi = new OpenAiApi.Builder().apiKey(apiKey).build();
        var chatOptions = OpenAiChatOptions.builder().model(model.getName())
                .build();
        var chatModel = OpenAiChatModel.builder().openAiApi(openAiApi)
                .defaultOptions(chatOptions).build();
        var llmProvider = new SpringAILLMProvider(chatModel);

        gridController = new GridAIController(grid, dbProvider);

        // Check for saved state in session
        var session = VaadinSession.getCurrent();
        var savedGridState = (GridAIController.GridState) session
                .getAttribute(SESSION_KEY_GRID_STATE);
        var savedHistory = (List<ChatMessage>) session
                .getAttribute(SESSION_KEY_HISTORY);
        var savedAttachments = (Map<String, List<AIAttachment>>) session
                .getAttribute(SESSION_KEY_ATTACHMENTS);

        var builder = AIOrchestrator.builder(llmProvider, systemPrompt)
                .withInput(messageInput).withMessageList(messageList)
                .withController(gridController)
                .withResponseCompleteListener(event -> saveState(session));

        if (savedHistory != null) {
            LOGGER.info("Restoring conversation history ({} messages)",
                    savedHistory.size());
            builder.withHistory(savedHistory,
                    savedAttachments != null ? savedAttachments
                            : new HashMap<>());
        }

        orchestrator = builder.build();

        // Restore grid state after orchestrator is built
        if (savedGridState != null) {
            LOGGER.info("Restoring grid state: {}", savedGridState.query());
            gridController.restoreState(savedGridState);
        }

        LOGGER.info("Orchestrator built (restored={})", savedHistory != null);
    }

    private void saveState(VaadinSession session) {
        session.access(() -> {
            var gridState = gridController.getState();
            session.setAttribute(SESSION_KEY_GRID_STATE, gridState);
            session.setAttribute(SESSION_KEY_HISTORY,
                    orchestrator.getHistory());
            session.setAttribute(SESSION_KEY_ATTACHMENTS, new HashMap<>());

            LOGGER.info("State saved to session (gridQuery={})",
                    gridState != null ? gridState.query() : "null");
        });
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

    // --- Database setup ---

    private static DatabaseProvider createDatabaseProvider() {
        try {
            var conn = DriverManager
                    .getConnection("jdbc:h2:mem:gridtest;DB_CLOSE_DELAY=-1");
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
