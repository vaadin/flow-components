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
package com.vaadin.flow.component.ai.tests.providers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Stream;

import org.h2.tools.Csv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.provider.QueryableProvider;
import com.vaadin.flow.component.ai.provider.ToolException;

/**
 * A {@link QueryableProvider} over a directory of CSV files that lets the LLM
 * ask for what a file can give, in the JSON language of
 * {@link CollectionQuery}: the columns of one file under chosen names, equality
 * filters and a sort order, applied while the file is read. Text values that
 * look like numbers or dates become numbers and dates. Compare with
 * {@link CsvDatabaseProvider}, which loads the files into SQL instead.
 */
public class CsvQueryableProvider implements QueryableProvider {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CsvQueryableProvider.class);

    private final String directory;
    private final String notes;

    /**
     * Creates a provider for the CSV files in a directory.
     *
     * @param directory
     *            the directory holding the files
     * @param notes
     *            notes about the data appended to the schema, or {@code null}
     */
    public CsvQueryableProvider(Path directory, String notes) {
        this.directory = directory.toString();
        this.notes = notes == null ? "" : notes;
    }

    @Override
    public QueryLanguage getQueryLanguage() {
        return CollectionQueryLanguage.CSV;
    }

    @Override
    public String getSchema() {
        var schema = new StringBuilder("CSV FILES in ").append(directory)
                .append(". Files (named without .csv in queries) and their columns, typed from the first row:\n");
        for (var file : files()) {
            var rows = read(file);
            var columns = new ArrayList<String>();
            var header = header(file);
            for (var column : header) {
                var sample = rows.isEmpty() ? null
                        : rows.getFirst().get(column);
                columns.add(column + " (" + typeName(sample) + ")");
            }
            schema.append("- ").append(sourceName(file)).append(": ")
                    .append(String.join(", ", columns)).append('\n');
        }
        if (!notes.isBlank()) {
            schema.append("\nNOTES:\n").append(notes);
        }
        ProviderLog.schema(LOGGER, schema.toString());
        return schema.toString();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String query) {
        ProviderLog.query(LOGGER, getQueryLanguage(), query);
        var request = CollectionQuery.parse(query);
        var items = items(request);
        var rows = new ArrayList<Map<String, Object>>(items.size());
        for (var item : items) {
            rows.add(request.project(item));
        }
        ProviderLog.rows(LOGGER, rows.size());
        return rows;
    }

    @Override
    public OptionalInt count(String query) {
        ProviderLog.query(LOGGER, getQueryLanguage(), query);
        var count = items(CollectionQuery.parse(query)).size();
        ProviderLog.count(LOGGER, count);
        return OptionalInt.of(count);
    }

    /**
     * Reads the file of a query and applies its filter and sort.
     */
    private List<Map<String, Object>> items(CollectionQuery query) {
        var file = Path.of(directory, query.source() + ".csv");
        if (!query.source().matches("[A-Za-z0-9_-]+") || !Files.exists(file)) {
            throw relayed(new ToolException("Unknown file " + query.source()
                    + ". Available: " + files().stream()
                            .map(CsvQueryableProvider::sourceName).toList()));
        }
        ProviderLog.backend(LOGGER, "reading " + file);
        var header = header(file);
        for (var field : query.fields()) {
            if (!header.contains(field)) {
                throw relayed(new ToolException("File " + query.source()
                        + " has no column " + field + ". Columns: " + header));
            }
        }
        var rows = read(file);
        try {
            query.filter()
                    .forEach((field, value) -> CollectionQuery
                            .checkFilterValue(field, value,
                                    CollectionQuery.FieldKind.of(rows.stream()
                                            .map(row -> row.get(field))
                                            .filter(Objects::nonNull)
                                            .findFirst().orElse(null))));
        } catch (ToolException e) {
            throw relayed(e);
        }
        var items = new ArrayList<>(
                rows.stream().filter(query::matches).toList());
        items.sort(query.comparator());
        return items;
    }

    private static ToolException relayed(ToolException e) {
        ProviderLog.relayed(LOGGER, e);
        return e;
    }

    private List<Path> files() {
        try (Stream<Path> files = Files.list(Path.of(directory))) {
            return files.filter(file -> file.toString().endsWith(".csv"))
                    .sorted().toList();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to list the CSV files in " + directory, e);
        }
    }

    private static String sourceName(Path file) {
        var name = file.getFileName().toString();
        return name.substring(0, name.length() - ".csv".length());
    }

    private static List<String> header(Path file) {
        try {
            var firstLine = Files.lines(file).findFirst().orElse("");
            return List.of(firstLine.split(","));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + file, e);
        }
    }

    private static List<Map<String, Object>> read(Path file) {
        var rows = new ArrayList<Map<String, Object>>();
        var csv = new Csv();
        csv.setCaseSensitiveColumnNames(true);
        try (var resultSet = csv.read(file.toString(), null, "UTF-8")) {
            var metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                var row = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    var text = resultSet.getString(i);
                    row.put(metaData.getColumnLabel(i),
                            text == null ? null : TextValues.parse(text, true));
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to read " + file, e);
        }
        return rows;
    }

    private static String typeName(Object value) {
        return switch (value) {
        case null -> "empty";
        case Long l -> "integer";
        case java.math.BigDecimal d -> "number";
        case java.time.LocalDate d -> "date";
        case Boolean b -> "boolean";
        default -> "text";
        };
    }
}
