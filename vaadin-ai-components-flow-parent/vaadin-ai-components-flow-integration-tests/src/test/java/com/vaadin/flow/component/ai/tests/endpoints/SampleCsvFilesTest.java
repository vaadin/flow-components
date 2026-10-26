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
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SampleCsvFilesTest {

    @Test
    void exportsOneFilePerTable() throws IOException {
        var directory = SampleCsvFiles.directory();
        for (var table : SampleData.TABLES) {
            var file = directory.resolve(table.sqlName() + ".csv");
            Assertions.assertTrue(Files.exists(file), file.toString());
            var lines = Files.readAllLines(file);
            Assertions.assertEquals(table.rows().size() + 1, lines.size(),
                    table.sqlName());
        }
    }

    @Test
    void writesSnakeCaseHeadersAndTextValues() throws IOException {
        var lines = Files.readAllLines(
                SampleCsvFiles.directory().resolve("stock_prices.csv"));
        Assertions.assertEquals(
                "id,trade_date,ticker,open_price,high_price,low_price,close_price,volume",
                lines.getFirst());
        Assertions.assertEquals(
                "1,2025-01-06,ACME,142.50,148.20,141.00,147.80,52000",
                lines.get(1));
    }

    @Test
    void writesNullsAsEmptyFields() throws IOException {
        var lines = Files.readAllLines(
                SampleCsvFiles.directory().resolve("project_tasks.csv"));
        Assertions.assertEquals(
                "1,Requirements,Planning,2025-01-01,2025-01-15,1.00,",
                lines.get(1));
        Assertions.assertEquals(
                "2,Design,Planning,2025-01-10,2025-01-25,0.80,1", lines.get(2));
    }

    @Test
    void quotesFieldsWithCommas() {
        var csv = SampleCsvFiles.toCsv(
                new SampleData.Table<>("t", "t", SampleData.BudgetItem.class,
                        List.of(new SampleData.BudgetItem(1, "Rent, office", -1,
                                null))));
        Assertions.assertEquals(
                "id,item_name,amount,item_type\n" + "1,\"Rent, office\",-1,\n",
                csv);
    }
}
