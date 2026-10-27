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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.TableSource.Column;
import com.vaadin.flow.component.ai.provider.TableSource.Pushdown;
import com.vaadin.flow.component.ai.tests.endpoints.SampleCsvFiles;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;

class CsvTableSourceTest {

    private static Map<String, Class<?>> columnTypes(CsvTableSource source) {
        return source.getColumns().stream()
                .collect(Collectors.toMap(Column::name, Column::type));
    }

    private static CsvTableSource source(String table) {
        return new CsvTableSource(
                SampleCsvFiles.directory().resolve(table + ".csv"));
    }

    @Test
    void inDirectory_createsOneSourcePerFile() {
        var sources = CsvTableSource.inDirectory(SampleCsvFiles.directory());

        Assertions.assertEquals(SampleData.TABLES.size(), sources.size());
        Assertions.assertTrue(sources.stream().map(CsvTableSource::getName)
                .anyMatch("website_traffic"::equals));
        Assertions.assertEquals("CSV file sales.csv",
                source("sales").getDescription());
    }

    @Test
    void columns_areInferredFromTheText() {
        var tasks = columnTypes(source("project_tasks"));

        Assertions.assertEquals(Integer.class, tasks.get("id"));
        Assertions.assertEquals(String.class, tasks.get("task_name"));
        Assertions.assertEquals(LocalDate.class, tasks.get("start_date"));
        Assertions.assertEquals(BigDecimal.class, tasks.get("progress"));
        Assertions.assertEquals(Integer.class, tasks.get("depends_on"));
    }

    @Test
    void fetch_returnsTypedValuesWithNullsForEmptyFields() {
        var rows = source("project_tasks").fetch(Pushdown.NONE);

        Assertions.assertEquals(SampleData.PROJECT_TASKS.size(), rows.size());
        var first = SampleData.PROJECT_TASKS.get(0);
        Assertions.assertEquals(first.taskName(), rows.get(0).get("task_name"));
        Assertions.assertEquals(first.startDate(),
                rows.get(0).get("start_date"));
        Assertions.assertEquals(first.dependsOn(),
                rows.get(0).get("depends_on"));
        Assertions.assertTrue(
                rows.stream().anyMatch(row -> row.get("depends_on") == null));
    }

    @Test
    void split_handlesQuotedFields() {
        Assertions.assertEquals(List.of("a", "b, c", "say \"hi\"", ""),
                CsvTableSource.split("a,\"b, c\",\"say \"\"hi\"\"\","));
    }
}
