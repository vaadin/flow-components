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
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.vaadin.flow.component.ai.provider.TableSource;

/**
 * A {@link TableSource} over one CSV file. Every value in a CSV file is text,
 * so the column types are inferred from the values: a column whose non-empty
 * values all parse as integers is an integer column, and so on down to text.
 * The file is read again on every fetch; nothing is pushed down.
 */
public class CsvTableSource implements TableSource {

    private final String file;
    private final String name;
    private final List<Column> columns;

    /**
     * Creates a source over a file, reading it once to infer the columns.
     *
     * @param file
     *            the CSV file, with a header row
     */
    public CsvTableSource(Path file) {
        this.file = file.toAbsolutePath().toString();
        var fileName = file.getFileName().toString();
        this.name = fileName.endsWith(".csv")
                ? fileName.substring(0, fileName.length() - 4)
                : fileName;
        this.columns = inferColumns(read());
    }

    /**
     * Creates a source for every {@code .csv} file in a directory.
     *
     * @param directory
     *            the directory
     * @return the sources, in file name order
     */
    public static List<CsvTableSource> inDirectory(Path directory) {
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(file -> file.toString().endsWith(".csv"))
                    .sorted().map(CsvTableSource::new).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Column> getColumns() {
        return columns;
    }

    @Override
    public String getDescription() {
        return "CSV file " + Path.of(file).getFileName();
    }

    @Override
    public List<Map<String, Object>> fetch(Pushdown pushdown) {
        var records = read();
        var rows = new ArrayList<Map<String, Object>>();
        for (var record : records.subList(1, records.size())) {
            var row = new LinkedHashMap<String, Object>();
            for (int i = 0; i < columns.size(); i++) {
                var text = i < record.size() ? record.get(i) : "";
                row.put(columns.get(i).name(), text.isEmpty() ? null
                        : parser(columns.get(i).type()).apply(text));
            }
            rows.add(row);
        }
        return rows;
    }

    private List<List<String>> read() {
        try {
            var records = new ArrayList<List<String>>();
            for (var line : Files.readAllLines(Path.of(file),
                    StandardCharsets.UTF_8)) {
                if (!line.isEmpty()) {
                    records.add(split(line));
                }
            }
            return records;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Splits one CSV line: fields separated by commas, a field with commas or
     * quotes wrapped in double quotes with inner quotes doubled.
     */
    static List<String> split(String line) {
        var fields = new ArrayList<String>();
        var field = new StringBuilder();
        var quoted = false;
        for (int i = 0; i < line.length(); i++) {
            var c = line.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields;
    }

    private static List<Column> inferColumns(List<List<String>> records) {
        var columns = new ArrayList<Column>();
        if (records.isEmpty()) {
            return columns;
        }
        var header = records.get(0);
        for (int i = 0; i < header.size(); i++) {
            var values = new ArrayList<String>();
            for (var record : records.subList(1, records.size())) {
                if (i < record.size() && !record.get(i).isEmpty()) {
                    values.add(record.get(i));
                }
            }
            columns.add(new Column(header.get(i), inferType(values)));
        }
        return columns;
    }

    private static final List<Class<?>> TYPE_PRECEDENCE = List.of(Integer.class,
            Long.class, BigDecimal.class, LocalDate.class, LocalDateTime.class,
            Boolean.class);

    private static Class<?> inferType(List<String> values) {
        if (values.isEmpty()) {
            return String.class;
        }
        for (var type : TYPE_PRECEDENCE) {
            var parser = parser(type);
            if (values.stream().allMatch(value -> parses(parser, value))) {
                return type;
            }
        }
        return String.class;
    }

    private static boolean parses(Function<String, Object> parser,
            String value) {
        try {
            parser.apply(value);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static Function<String, Object> parser(Class<?> type) {
        if (type == Integer.class) {
            return Integer::valueOf;
        } else if (type == Long.class) {
            return Long::valueOf;
        } else if (type == BigDecimal.class) {
            return BigDecimal::new;
        } else if (type == LocalDate.class) {
            return LocalDate::parse;
        } else if (type == LocalDateTime.class) {
            return LocalDateTime::parse;
        } else if (type == Boolean.class) {
            return text -> {
                if (text.equalsIgnoreCase("true")) {
                    return Boolean.TRUE;
                } else if (text.equalsIgnoreCase("false")) {
                    return Boolean.FALSE;
                }
                throw new IllegalArgumentException("Not a boolean: " + text);
            };
        }
        return text -> text;
    }
}
