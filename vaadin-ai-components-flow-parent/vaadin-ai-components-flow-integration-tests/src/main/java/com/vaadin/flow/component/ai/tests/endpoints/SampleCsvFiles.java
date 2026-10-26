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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes {@link SampleData} as CSV files, one per table, the way data arrives
 * when there is no database at all, only exports: a header row of snake_case
 * column names, ISO dates, plain decimals, and empty fields for nulls. Every
 * value is text, so whoever loads the files has to infer the column types.
 */
public final class SampleCsvFiles {

    private static Path sharedDirectory;

    private SampleCsvFiles() {
    }

    /**
     * Returns a directory with the exported files, writing them to a temporary
     * directory on the first call.
     *
     * @return the directory holding one CSV file per table
     */
    public static synchronized Path directory() {
        if (sharedDirectory == null) {
            try {
                sharedDirectory = Files.createTempDirectory("sample-data-csv");
                export(sharedDirectory);
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Failed to export the sample CSV files", e);
            }
        }
        return sharedDirectory;
    }

    /**
     * Writes one CSV file per table into the given directory, named after the
     * table, for example {@code sales.csv}.
     *
     * @param directory
     *            an existing directory
     * @return the written files, in table order
     * @throws IOException
     *             if writing fails
     */
    public static List<Path> export(Path directory) throws IOException {
        var files = new ArrayList<Path>();
        for (var table : SampleData.TABLES) {
            var file = directory.resolve(table.sqlName() + ".csv");
            Files.writeString(file, toCsv(table), StandardCharsets.UTF_8);
            files.add(file);
        }
        return files;
    }

    /**
     * Renders a table as CSV text.
     *
     * @param table
     *            the table to render
     * @return the CSV text with a header row
     */
    public static String toCsv(SampleData.Table<?> table) {
        var csv = new StringBuilder();
        csv.append(
                String.join(",",
                        table.componentNames().stream()
                                .map(SampleData::toSnakeCase).toList()))
                .append('\n');
        for (var row : table.rowMaps()) {
            var fields = new ArrayList<String>();
            for (var value : row.values()) {
                fields.add(field(value));
            }
            csv.append(String.join(",", fields)).append('\n');
        }
        return csv.toString();
    }

    private static String field(Object value) {
        if (value == null) {
            return "";
        }
        var text = value instanceof BigDecimal decimal ? decimal.toPlainString()
                : value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
