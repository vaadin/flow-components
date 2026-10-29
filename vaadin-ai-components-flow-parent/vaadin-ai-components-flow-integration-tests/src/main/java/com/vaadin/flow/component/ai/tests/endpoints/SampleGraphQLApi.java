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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.vaadin.flow.internal.JacksonUtils;

import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeRuntimeWiring;

/**
 * Serves {@link SampleData} as a GraphQL API: one list field per table on the
 * {@code Query} type with simple filter arguments, and object fields for the
 * relations the SQL tables express as foreign keys ({@code dependsOn},
 * {@code manager}, {@code parent}). Like many such APIs, a list field also
 * takes {@code orderBy}, a list of scalar fields with a direction, and
 * {@code offset} and {@code limit} for a page, and has a {@code <field>Count}
 * beside it with the same filter arguments. Accepts {@code POST} requests with
 * a JSON body of {@code {"query": ..., "variables": ..., "operationName": ...}}
 * and {@code GET} requests with a {@code query} parameter.
 */
final class SampleGraphQLApi implements HttpHandler {

    static final String SDL = """
            "Monthly revenue of a sales region."
            type Sale {
              id: Int!
              "Month name, January to June."
              month: String!
              "1 = January; order by it for a chronological series."
              monthOrder: Int!
              "Revenue of the month in EUR."
              revenue: Int!
              "North or South."
              region: String!
            }

            "An employee."
            type Employee {
              id: Int!
              name: String!
              "Engineering, Sales or Marketing."
              department: String!
              "Annual salary in EUR."
              salary: Int!
              age: Int!
            }

            "A product in the catalog."
            type Product {
              id: Int!
              productName: String!
              "Electronics or Accessories."
              category: String!
              unitsSold: Int!
              "Unit price in EUR."
              price: Float!
            }

            "Open, high, low and close prices of one trading day."
            type StockPrice {
              id: Int!
              "ISO-8601 date, for example 2025-01-06."
              tradeDate: String!
              ticker: String!
              openPrice: Float!
              highPrice: Float!
              lowPrice: Float!
              closePrice: Float!
              volume: Int!
            }

            "A task of the project plan."
            type ProjectTask {
              id: Int!
              taskName: String!
              "Planning, Development, QA or Release."
              phase: String!
              "ISO-8601 date."
              startDate: String!
              "ISO-8601 date."
              endDate: String!
              "Share of the work done, 0.0 to 1.0."
              progress: Float!
              "The task this one depends on, if any."
              dependsOn: ProjectTask
            }

            "A member of the organization chart."
            type OrgMember {
              id: Int!
              empName: String!
              jobTitle: String!
              "Hex color of the member's team."
              teamColor: String
              "The member's manager, absent for the CEO."
              manager: OrgMember
              "The members reporting to this one."
              reports: [OrgMember!]!
            }

            "Energy flowing from a source to a target, for a Sankey diagram."
            type EnergyFlow {
              id: Int!
              sourceName: String!
              targetName: String!
              amount: Float!
            }

            "Website visitors in one hour of one weekday."
            type TrafficCell {
              id: Int!
              "0 = Monday ... 4 = Friday."
              dayOfWeek: Int!
              "0 = 9am ... 7 = 4pm."
              hourOfDay: Int!
              visitors: Int!
            }

            "A line of the budget, for a waterfall chart."
            type BudgetItem {
              id: Int!
              itemName: String!
              "Negative for costs; 0 for computed lines."
              amount: Int!
              "intermediate or sum for computed lines, absent otherwise."
              itemType: String
            }

            "A stage of the sales pipeline."
            type PipelineStage {
              id: Int!
              stage: String!
              prospects: Int!
              "1 = first stage."
              sortOrder: Int!
            }

            "A key performance indicator with its value range."
            type KpiMetric {
              id: Int!
              metricName: String!
              currentVal: Float!
              targetVal: Float!
              minVal: Float!
              maxVal: Float!
            }

            "An expense category; top-level categories have no parent and amount 0."
            type ExpenseCategory {
              id: Int!
              categoryName: String!
              amount: Float!
              parent: ExpenseCategory
              children: [ExpenseCategory!]!
            }

            "Sort direction. Null values sort after all others when ascending and before them when descending."
            enum SortDirection {
              ASC
              DESC
            }

            "One sort key: a scalar field of the listed type and its direction."
            input OrderBy {
              field: String!
              direction: SortDirection = ASC
            }

            "List fields apply their filter, then orderBy key by key, then offset and limit."
            type Query {
              "Monthly sales, optionally of one region."
              sales(region: String, orderBy: [OrderBy!], offset: Int, limit: Int): [Sale!]!
              "The number of monthly sales, optionally of one region."
              salesCount(region: String): Int!
              "Employees, optionally of one department."
              employees(department: String, orderBy: [OrderBy!], offset: Int, limit: Int): [Employee!]!
              "The number of employees, optionally of one department."
              employeesCount(department: String): Int!
              "Products, optionally of one category."
              products(category: String, orderBy: [OrderBy!], offset: Int, limit: Int): [Product!]!
              "The number of products, optionally of one category."
              productsCount(category: String): Int!
              "Daily prices, optionally of one ticker."
              stockPrices(ticker: String, orderBy: [OrderBy!], offset: Int, limit: Int): [StockPrice!]!
              "The number of daily prices, optionally of one ticker."
              stockPricesCount(ticker: String): Int!
              "Project tasks, optionally of one phase."
              projectTasks(phase: String, orderBy: [OrderBy!], offset: Int, limit: Int): [ProjectTask!]!
              "The number of project tasks, optionally of one phase."
              projectTasksCount(phase: String): Int!
              "All members of the organization."
              orgMembers(orderBy: [OrderBy!], offset: Int, limit: Int): [OrgMember!]!
              "The number of members of the organization."
              orgMembersCount: Int!
              "All energy flows."
              energyFlows(orderBy: [OrderBy!], offset: Int, limit: Int): [EnergyFlow!]!
              "The number of energy flows."
              energyFlowsCount: Int!
              "Hourly website traffic, optionally of one weekday."
              websiteTraffic(dayOfWeek: Int, orderBy: [OrderBy!], offset: Int, limit: Int): [TrafficCell!]!
              "The number of hourly traffic cells, optionally of one weekday."
              websiteTrafficCount(dayOfWeek: Int): Int!
              "All budget lines in statement order."
              budgetItems(orderBy: [OrderBy!], offset: Int, limit: Int): [BudgetItem!]!
              "The number of budget lines."
              budgetItemsCount: Int!
              "All pipeline stages."
              salesPipeline(orderBy: [OrderBy!], offset: Int, limit: Int): [PipelineStage!]!
              "The number of pipeline stages."
              salesPipelineCount: Int!
              "All KPIs."
              kpiMetrics(orderBy: [OrderBy!], offset: Int, limit: Int): [KpiMetric!]!
              "The number of KPIs."
              kpiMetricsCount: Int!
              "Expense categories, optionally the children of one parent category."
              expenseCategories(parent: String, orderBy: [OrderBy!], offset: Int, limit: Int): [ExpenseCategory!]!
              "The number of expense categories, optionally the children of one parent category."
              expenseCategoriesCount(parent: String): Int!
            }
            """;

    /**
     * A list field of the {@code Query} type: the table it lists, and the
     * argument that filters it by a field of the rows, if any.
     */
    private record RootField(String name, String resource, String argument,
            String field) {
    }

    private static final List<RootField> ROOT_FIELDS = List.of(
            new RootField("sales", "sales", "region", "region"),
            new RootField("employees", "employees", "department", "department"),
            new RootField("products", "products", "category", "category"),
            new RootField("stockPrices", "stockPrices", "ticker", "ticker"),
            new RootField("projectTasks", "projectTasks", "phase", "phase"),
            new RootField("orgMembers", "orgChart", null, null),
            new RootField("energyFlows", "energyFlow", null, null),
            new RootField("websiteTraffic", "websiteTraffic", "dayOfWeek",
                    "dayOfWeek"),
            new RootField("budgetItems", "budgetItems", null, null),
            new RootField("salesPipeline", "salesPipeline", null, null),
            new RootField("kpiMetrics", "kpiMetrics", null, null),
            new RootField("expenseCategories", "expenseCategories", "parent",
                    "parentCategory"));

    private final GraphQL graphQL;

    SampleGraphQLApi() {
        graphQL = GraphQL.newGraphQL(buildSchema()).build();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query;
        Map<String, Object> variables = Map.of();
        String operationName = null;
        if ("POST".equals(exchange.getRequestMethod())) {
            var body = new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);
            Map<String, Object> request = readRequest(body);
            if (request == null) {
                SampleApiServer.sendError(exchange, 400,
                        "The request body is not a JSON object");
                return;
            }
            query = (String) request.get("query");
            if (request.get("variables") instanceof Map<?, ?> map) {
                variables = castVariables(map);
            }
            operationName = (String) request.get("operationName");
        } else if ("GET".equals(exchange.getRequestMethod())) {
            query = SampleApiServer.queryParameters(exchange).get("query");
        } else {
            SampleApiServer.sendError(exchange, 405,
                    "Only GET and POST are supported");
            return;
        }
        if (query == null || query.isBlank()) {
            SampleApiServer.sendError(exchange, 400, "Missing query");
            return;
        }
        var input = ExecutionInput.newExecutionInput().query(query)
                .variables(variables).operationName(operationName).build();
        SampleApiServer.sendJson(exchange,
                exchange.getRequestMethod() + " "
                        + SampleApiServer.serverUrl(exchange)
                        + exchange.getRequestURI().getPath() + " "
                        + describe(query, variables),
                200, graphQL.execute(input).toSpecification());
    }

    /**
     * Describes a query on one line for the log, with its variables if there
     * are any. The introspection query, hundreds of lines long, is only named.
     */
    private static String describe(String query,
            Map<String, Object> variables) {
        if (query.contains("__schema")) {
            return "<introspection query>";
        }
        var text = query.strip().replaceAll("\\s+", " ");
        return variables.isEmpty() ? text
                : text + " " + JacksonUtils.getMapper()
                        .writeValueAsString(variables);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readRequest(String body) {
        try {
            var parsed = JacksonUtils.getMapper().readValue(body, Object.class);
            return parsed instanceof Map<?, ?> map ? (Map<String, Object>) map
                    : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castVariables(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }

    private static GraphQLSchema buildSchema() {
        var registry = new SchemaParser().parse(SDL);
        var query = TypeRuntimeWiring.newTypeWiring("Query");
        for (var field : ROOT_FIELDS) {
            query.dataFetcher(field.name(),
                    list(field.resource(), field.argument(), field.field()));
            query.dataFetcher(field.name() + "Count",
                    count(field.resource(), field.argument(), field.field()));
        }
        var wiring = RuntimeWiring.newRuntimeWiring().type(query)
                .type(TypeRuntimeWiring.newTypeWiring("ProjectTask")
                        .dataFetcher("dependsOn",
                                environment -> findById("projectTasks",
                                        source(environment).get("dependsOn"))))
                .type(TypeRuntimeWiring.newTypeWiring("OrgMember")
                        .dataFetcher("manager",
                                environment -> findById("orgChart",
                                        source(environment).get("managerId")))
                        .dataFetcher("reports",
                                environment -> filter("orgChart", "managerId",
                                        source(environment).get("id"))))
                .type(TypeRuntimeWiring.newTypeWiring("ExpenseCategory")
                        .dataFetcher("parent", environment -> {
                            var parent = source(environment)
                                    .get("parentCategory");
                            return parent == null ? null
                                    : filter("expenseCategories",
                                            "categoryName", parent).stream()
                                            .findFirst().orElse(null);
                        })
                        .dataFetcher("children", environment -> filter(
                                "expenseCategories", "parentCategory",
                                source(environment).get("categoryName"))))
                .build();
        return new SchemaGenerator().makeExecutableSchema(registry, wiring);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> source(
            DataFetchingEnvironment environment) {
        return (Map<String, Object>) environment.getSource();
    }

    /**
     * A fetcher returning the rows of a table as maps, filtered by one argument
     * when it is given, then sorted and paged.
     */
    private static DataFetcher<List<Map<String, Object>>> list(String resource,
            String argument, String field) {
        return environment -> page(
                filtered(resource, argument, field, environment), environment);
    }

    /**
     * A fetcher returning the number of rows of a table, filtered by one
     * argument when it is given.
     */
    private static DataFetcher<Integer> count(String resource, String argument,
            String field) {
        return environment -> filtered(resource, argument, field, environment)
                .size();
    }

    private static List<Map<String, Object>> filtered(String resource,
            String argument, String field,
            DataFetchingEnvironment environment) {
        Object value = argument == null ? null
                : environment.getArgument(argument);
        return value == null ? rows(resource) : filter(resource, field, value);
    }

    /**
     * Sorts the rows by the {@code orderBy} argument, key by key, and cuts the
     * page of the {@code offset} and {@code limit} arguments.
     */
    private static List<Map<String, Object>> page(
            List<Map<String, Object>> rows,
            DataFetchingEnvironment environment) {
        List<Map<String, Object>> orderBy = environment
                .getArgumentOrDefault("orderBy", List.of());
        var sortable = sortableFields(environment);
        Comparator<Map<String, Object>> order = null;
        for (var key : orderBy) {
            var field = String.valueOf(key.get("field"));
            if (!sortable.contains(field)) {
                throw new IllegalArgumentException("Unknown sort field: "
                        + field + ". Fields: " + sortable);
            }
            var byField = SampleRestApi.byField(field,
                    "DESC".equals(String.valueOf(key.get("direction"))));
            order = order == null ? byField : order.thenComparing(byField);
        }
        var result = new ArrayList<>(rows);
        if (order != null) {
            result.sort(order);
        }
        Integer offset = environment.getArgument("offset");
        Integer limit = environment.getArgument("limit");
        if ((offset != null && offset < 0) || (limit != null && limit < 0)) {
            throw new IllegalArgumentException(
                    "offset and limit must not be negative");
        }
        var from = Math.min(result.size(), offset == null ? 0 : offset);
        var to = limit == null ? result.size()
                : Math.min(result.size(), from + limit);
        return List.copyOf(result.subList(from, to));
    }

    /**
     * Returns the scalar fields of the type a list field lists, which are the
     * fields its rows can be sorted by.
     */
    private static List<String> sortableFields(
            DataFetchingEnvironment environment) {
        var type = (GraphQLObjectType) GraphQLTypeUtil
                .unwrapAll(environment.getFieldType());
        return type.getFieldDefinitions().stream()
                .filter(definition -> GraphQLTypeUtil.unwrapAll(
                        definition.getType()) instanceof GraphQLScalarType)
                .map(GraphQLFieldDefinition::getName).toList();
    }

    private static List<Map<String, Object>> rows(String resource) {
        var table = Objects.requireNonNull(SampleData.table(resource));
        var rows = new ArrayList<Map<String, Object>>();
        for (var row : table.rows()) {
            rows.add(jsonMap(row));
        }
        return rows;
    }

    private static List<Map<String, Object>> filter(String resource,
            String field, Object value) {
        Predicate<Map<String, Object>> matches = row -> Objects
                .equals(row.get(field), value);
        return rows(resource).stream().filter(matches).toList();
    }

    private static Map<String, Object> findById(String resource, Object id) {
        return id == null ? null
                : filter(resource, "id", id).stream().findFirst().orElse(null);
    }

    /**
     * Converts a record to the map a JSON API would serve: dates as ISO strings
     * and decimals as numbers.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> jsonMap(Record record) {
        return JacksonUtils.getMapper().convertValue(record, Map.class);
    }
}
