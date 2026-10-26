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
import java.util.stream.Stream;

import org.h2.tools.Csv;

import com.vaadin.flow.component.ai.tests.endpoints.SampleData;

/**
 * A provider for data that exists only as CSV exports: every {@code .csv} file
 * of a directory is loaded into a {@link SqlMirror} as a table named after the
 * file, with column types inferred from the text values, and the LLM queries
 * the tables with SQL.
 */
public class CsvDatabaseProvider extends MirrorDatabaseProvider {

    private final String directory;

    /**
     * Creates a provider for the CSV files in a directory.
     *
     * @param directory
     *            the directory holding the files
     */
    public CsvDatabaseProvider(Path directory) {
        this.directory = directory.toString();
    }

    @Override
    protected SqlMirror copy() {
        var mirror = SqlMirror.create(
                "csv_mirror_" + Integer.toHexString(directory.hashCode()),
                true);
        try (Stream<Path> files = Files.list(Path.of(directory))) {
            files.filter(file -> file.toString().endsWith(".csv")).sorted()
                    .forEach(file -> mirror.load(tableName(file), read(file)));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to list the CSV files in " + directory, e);
        }
        return mirror;
    }

    @Override
    protected String notes() {
        return """
                - This database was loaded from the CSV files in %s. Each file is a table named after the file and each header a column, in upper snake_case; the column types were inferred from the values.
                """
                .formatted(directory) + SampleData.NOTES;
    }

    private static String tableName(Path file) {
        var name = file.getFileName().toString();
        return name.substring(0, name.length() - ".csv".length());
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
                    row.put(metaData.getColumnLabel(i), resultSet.getString(i));
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to read " + file, e);
        }
        return rows;
    }
}
