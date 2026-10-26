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
package com.vaadin.flow.component.ai.tests.endpoints;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The sample data set behind every data endpoint: one record type per table and
 * the rows of each table. The endpoints load these rows into their own stores
 * (SQL tables, documents, JSON, CSV files, JPA entities), so a question asked
 * through any endpoint is answered from the same data.
 * <p>
 * Record component names are camelCase; SQL-based endpoints derive snake_case
 * column names from them with {@link #toSnakeCase(String)}.
 * </p>
 */
public final class SampleData {

    /** Monthly revenue of a sales region. */
    public record Sale(int id, String month, int monthOrder, int revenue,
            String region) {
    }

    /** An employee with department, salary and age. */
    public record Employee(int id, String name, String department, int salary,
            int age) {
    }

    /** A product with its category, units sold and unit price. */
    public record Product(int id, String productName, String category,
            int unitsSold, BigDecimal price) {
    }

    /** Daily open, high, low and close prices of a ticker. */
    public record StockPrice(int id, LocalDate tradeDate, String ticker,
            BigDecimal openPrice, BigDecimal highPrice, BigDecimal lowPrice,
            BigDecimal closePrice, int volume) {
    }

    /**
     * A project task; {@code dependsOn} is the id of the task it depends on, or
     * {@code null}.
     */
    public record ProjectTask(int id, String taskName, String phase,
            LocalDate startDate, LocalDate endDate, BigDecimal progress,
            Integer dependsOn) {
    }

    /**
     * A member of the organization; {@code managerId} is the id of the manager,
     * or {@code null} for the top of the hierarchy.
     */
    public record OrgMember(int id, String empName, String jobTitle,
            Integer managerId, String teamColor) {
    }

    /** An energy flow from a source to a target. */
    public record EnergyFlow(int id, String sourceName, String targetName,
            BigDecimal amount) {
    }

    /**
     * Website visitors in one hour of one weekday. Both indices are 0-based:
     * day 0 is Monday and hour 0 is 9am.
     */
    public record TrafficCell(int id, int dayOfWeek, int hourOfDay,
            int visitors) {
    }

    /**
     * A budget line; {@code itemType} is {@code "intermediate"} or
     * {@code "sum"} for computed lines and {@code null} otherwise.
     */
    public record BudgetItem(int id, String itemName, int amount,
            String itemType) {
    }

    /** A stage of the sales pipeline with the number of prospects in it. */
    public record PipelineStage(int id, String stage, int prospects,
            int sortOrder) {
    }

    /** A KPI with its current value, target and value range. */
    public record KpiMetric(int id, String metricName, BigDecimal currentVal,
            BigDecimal targetVal, BigDecimal minVal, BigDecimal maxVal) {
    }

    /**
     * An expense category; {@code parentCategory} names the parent category, or
     * is {@code null} for a top-level category.
     */
    public record ExpenseCategory(int id, String categoryName,
            String parentCategory, BigDecimal amount) {
    }

    /**
     * A table of the data set: its snake_case SQL name, its camelCase resource
     * name used by the document and web endpoints, the record type and the
     * rows.
     */
    public record Table<T extends Record>(String sqlName, String resourceName,
            Class<T> type, List<T> rows) {

        /**
         * Returns the rows as maps from camelCase component name to value.
         *
         * @return the rows as maps, in table order
         */
        public List<Map<String, Object>> rowMaps() {
            var maps = new ArrayList<Map<String, Object>>(rows.size());
            for (var row : rows) {
                maps.add(toMap(row));
            }
            return maps;
        }

        /**
         * Returns the camelCase component names of the record type, in
         * declaration order.
         *
         * @return the component names
         */
        public List<String> componentNames() {
            var names = new ArrayList<String>();
            for (var component : type.getRecordComponents()) {
                names.add(component.getName());
            }
            return names;
        }
    }

    public static final List<Sale> SALES = List.of(
            new Sale(1, "January", 1, 45000, "North"),
            new Sale(2, "February", 2, 52000, "North"),
            new Sale(3, "March", 3, 58000, "North"),
            new Sale(4, "April", 4, 61000, "North"),
            new Sale(5, "May", 5, 55000, "North"),
            new Sale(6, "June", 6, 67000, "North"),
            new Sale(7, "January", 1, 38000, "South"),
            new Sale(8, "February", 2, 42000, "South"),
            new Sale(9, "March", 3, 48000, "South"),
            new Sale(10, "April", 4, 53000, "South"),
            new Sale(11, "May", 5, 49000, "South"),
            new Sale(12, "June", 6, 57000, "South"));

    public static final List<Employee> EMPLOYEES = List.of(
            new Employee(1, "John Doe", "Engineering", 85000, 32),
            new Employee(2, "Jane Smith", "Sales", 72000, 28),
            new Employee(3, "Bob Johnson", "Engineering", 95000, 41),
            new Employee(4, "Alice Williams", "Marketing", 68000, 35),
            new Employee(5, "Charlie Brown", "Sales", 75000, 29));

    public static final List<Product> PRODUCTS = List.of(
            new Product(1, "Laptop", "Electronics", 150, decimal("899.99")),
            new Product(2, "Mouse", "Accessories", 450, decimal("29.99")),
            new Product(3, "Keyboard", "Accessories", 320, decimal("79.99")),
            new Product(4, "Monitor", "Electronics", 89, decimal("299.99")),
            new Product(5, "Headphones", "Accessories", 210,
                    decimal("149.99")));

    public static final List<StockPrice> STOCK_PRICES = List.of(
            stock(1, "2025-01-06", "142.50", "148.20", "141.00", "147.80",
                    52000),
            stock(2, "2025-01-07", "147.80", "151.30", "146.50", "149.10",
                    48000),
            stock(3, "2025-01-08", "149.10", "152.00", "147.20", "148.50",
                    55000),
            stock(4, "2025-01-09", "148.50", "153.80", "148.00", "153.20",
                    63000),
            stock(5, "2025-01-10", "153.20", "155.50", "150.00", "151.40",
                    47000),
            stock(6, "2025-01-13", "151.40", "154.60", "149.80", "154.00",
                    51000),
            stock(7, "2025-01-14", "154.00", "156.20", "152.50", "155.80",
                    58000),
            stock(8, "2025-01-15", "155.80", "158.00", "154.30", "157.20",
                    62000),
            stock(9, "2025-01-16", "157.20", "159.40", "155.80", "156.50",
                    45000),
            stock(10, "2025-01-17", "156.50", "157.80", "153.00", "153.90",
                    53000));

    public static final List<ProjectTask> PROJECT_TASKS = List.of(
            task(1, "Requirements", "Planning", "2025-01-01", "2025-01-15",
                    "1.00", null),
            task(2, "Design", "Planning", "2025-01-10", "2025-01-25", "0.80",
                    1),
            task(3, "Backend Dev", "Development", "2025-01-20", "2025-02-20",
                    "0.50", 2),
            task(4, "Frontend Dev", "Development", "2025-01-25", "2025-02-25",
                    "0.30", 2),
            task(5, "Testing", "QA", "2025-02-15", "2025-03-10", "0.10", null),
            task(6, "Deployment", "Release", "2025-03-05", "2025-03-15", "0.00",
                    5));

    public static final List<OrgMember> ORG_CHART = List.of(
            new OrgMember(1, "Sarah CEO", "CEO", null, "#e74c3c"),
            new OrgMember(2, "Tom VP Eng", "VP Engineering", 1, "#3498db"),
            new OrgMember(3, "Lisa VP Sales", "VP Sales", 1, "#2ecc71"),
            new OrgMember(4, "Mike Lead", "Tech Lead", 2, "#3498db"),
            new OrgMember(5, "Anna Dev", "Developer", 4, "#3498db"),
            new OrgMember(6, "Dan Sales", "Sales Manager", 3, "#2ecc71"));

    public static final List<EnergyFlow> ENERGY_FLOW = List.of(
            new EnergyFlow(1, "Solar", "Electricity", decimal("120.00")),
            new EnergyFlow(2, "Wind", "Electricity", decimal("80.00")),
            new EnergyFlow(3, "Coal", "Electricity", decimal("200.00")),
            new EnergyFlow(4, "Electricity", "Residential", decimal("180.00")),
            new EnergyFlow(5, "Electricity", "Industrial", decimal("150.00")),
            new EnergyFlow(6, "Electricity", "Commercial", decimal("70.00")));

    public static final List<TrafficCell> WEBSITE_TRAFFIC = trafficCells();

    public static final List<BudgetItem> BUDGET_ITEMS = List.of(
            new BudgetItem(1, "Revenue", 420000, null),
            new BudgetItem(2, "Cost of Goods", -180000, null),
            new BudgetItem(3, "Gross Profit", 0, "intermediate"),
            new BudgetItem(4, "Salaries", -120000, null),
            new BudgetItem(5, "Marketing", -35000, null),
            new BudgetItem(6, "Rent", -25000, null),
            new BudgetItem(7, "Net Profit", 0, "sum"));

    public static final List<PipelineStage> SALES_PIPELINE = List.of(
            new PipelineStage(1, "Leads", 1200, 1),
            new PipelineStage(2, "Qualified", 800, 2),
            new PipelineStage(3, "Proposal", 450, 3),
            new PipelineStage(4, "Negotiation", 200, 4),
            new PipelineStage(5, "Closed Won", 120, 5));

    public static final List<KpiMetric> KPI_METRICS = List.of(
            kpi(1, "Customer Satisfaction", "78", "90", "0", "100"),
            kpi(2, "Revenue Target %", "72", "100", "0", "100"),
            kpi(3, "Server Uptime %", "99.7", "99.9", "95", "100"));

    public static final List<ExpenseCategory> EXPENSE_CATEGORIES = List.of(
            new ExpenseCategory(1, "Operating", null, decimal("0.00")),
            new ExpenseCategory(2, "Salaries", "Operating",
                    decimal("120000.00")),
            new ExpenseCategory(3, "Rent", "Operating", decimal("25000.00")),
            new ExpenseCategory(4, "Utilities", "Operating",
                    decimal("8000.00")),
            new ExpenseCategory(5, "Marketing", null, decimal("0.00")),
            new ExpenseCategory(6, "Digital Ads", "Marketing",
                    decimal("18000.00")),
            new ExpenseCategory(7, "Events", "Marketing", decimal("12000.00")),
            new ExpenseCategory(8, "Content", "Marketing", decimal("5000.00")));

    /** All tables of the data set, in a fixed order. */
    public static final List<Table<?>> TABLES = List.of(
            new Table<>("sales", "sales", Sale.class, SALES),
            new Table<>("employees", "employees", Employee.class, EMPLOYEES),
            new Table<>("products", "products", Product.class, PRODUCTS),
            new Table<>("stock_prices", "stockPrices", StockPrice.class,
                    STOCK_PRICES),
            new Table<>("project_tasks", "projectTasks", ProjectTask.class,
                    PROJECT_TASKS),
            new Table<>("org_chart", "orgChart", OrgMember.class, ORG_CHART),
            new Table<>("energy_flow", "energyFlow", EnergyFlow.class,
                    ENERGY_FLOW),
            new Table<>("website_traffic", "websiteTraffic", TrafficCell.class,
                    WEBSITE_TRAFFIC),
            new Table<>("budget_items", "budgetItems", BudgetItem.class,
                    BUDGET_ITEMS),
            new Table<>("sales_pipeline", "salesPipeline", PipelineStage.class,
                    SALES_PIPELINE),
            new Table<>("kpi_metrics", "kpiMetrics", KpiMetric.class,
                    KPI_METRICS),
            new Table<>("expense_categories", "expenseCategories",
                    ExpenseCategory.class, EXPENSE_CATEGORIES));

    /**
     * Notes about the data that help an LLM query it correctly, independent of
     * the endpoint. Endpoints append this to their schema description.
     */
    public static final String NOTES = """
            - SALES table: use month_order column for chronological sorting (ORDER BY month_order)
            - WEBSITE_TRAFFIC table: both columns are 0-based indices. day_of_week: 0=Monday, 1=Tuesday, 2=Wednesday, 3=Thursday, 4=Friday. hour_of_day: 0=9am, 1=10am, ..., 7=4pm. Use xAxis/yAxis categories in configuration to set the display labels.
            """;

    private SampleData() {
    }

    /**
     * Finds a table by its snake_case SQL name or its camelCase resource name.
     *
     * @param name
     *            the SQL or resource name, case-insensitive
     * @return the table, or {@code null} if there is none by that name
     */
    public static Table<?> table(String name) {
        for (var table : TABLES) {
            if (table.sqlName().equalsIgnoreCase(name)
                    || table.resourceName().equalsIgnoreCase(name)) {
                return table;
            }
        }
        return null;
    }

    /**
     * Converts a record to a map from camelCase component name to value, in
     * declaration order.
     *
     * @param record
     *            the record to convert
     * @return the record as a map
     */
    public static Map<String, Object> toMap(Record record) {
        var map = new LinkedHashMap<String, Object>();
        for (RecordComponent component : record.getClass()
                .getRecordComponents()) {
            try {
                map.put(component.getName(),
                        component.getAccessor().invoke(record));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "Cannot read record component " + component.getName(),
                        e);
            }
        }
        return map;
    }

    /**
     * Converts a camelCase name to lower snake_case, for example
     * {@code monthOrder} to {@code month_order}.
     *
     * @param camelCase
     *            the camelCase name
     * @return the snake_case name
     */
    public static String toSnakeCase(String camelCase) {
        var snake = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0 && camelCase.charAt(i - 1) != '_') {
                    snake.append('_');
                }
                snake.append(Character.toLowerCase(c));
            } else {
                snake.append(c);
            }
        }
        return snake.toString();
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    private static StockPrice stock(int id, String date, String open,
            String high, String low, String close, int volume) {
        return new StockPrice(id, LocalDate.parse(date), "ACME", decimal(open),
                decimal(high), decimal(low), decimal(close), volume);
    }

    private static ProjectTask task(int id, String name, String phase,
            String start, String end, String progress, Integer dependsOn) {
        return new ProjectTask(id, name, phase, LocalDate.parse(start),
                LocalDate.parse(end), decimal(progress), dependsOn);
    }

    private static KpiMetric kpi(int id, String name, String current,
            String target, String min, String max) {
        return new KpiMetric(id, name, decimal(current), decimal(target),
                decimal(min), decimal(max));
    }

    private static List<TrafficCell> trafficCells() {
        int[][] traffic = { { 0, 0, 120 }, { 0, 1, 180 }, { 0, 2, 220 },
                { 0, 3, 150 }, { 0, 4, 200 }, { 0, 5, 250 }, { 0, 6, 190 },
                { 0, 7, 160 }, { 1, 0, 130 }, { 1, 1, 200 }, { 1, 2, 240 },
                { 1, 3, 170 }, { 1, 4, 210 }, { 1, 5, 260 }, { 1, 6, 200 },
                { 1, 7, 170 }, { 2, 0, 110 }, { 2, 1, 170 }, { 2, 2, 210 },
                { 2, 3, 160 }, { 2, 4, 190 }, { 2, 5, 230 }, { 2, 6, 180 },
                { 2, 7, 150 }, { 3, 0, 140 }, { 3, 1, 210 }, { 3, 2, 250 },
                { 3, 3, 180 }, { 3, 4, 220 }, { 3, 5, 270 }, { 3, 6, 210 },
                { 3, 7, 175 }, { 4, 0, 100 }, { 4, 1, 150 }, { 4, 2, 180 },
                { 4, 3, 130 }, { 4, 4, 160 }, { 4, 5, 190 }, { 4, 6, 140 },
                { 4, 7, 110 } };
        var cells = new ArrayList<TrafficCell>(traffic.length);
        for (int i = 0; i < traffic.length; i++) {
            cells.add(new TrafficCell(i + 1, traffic[i][0], traffic[i][1],
                    traffic[i][2]));
        }
        return List.copyOf(cells);
    }
}
