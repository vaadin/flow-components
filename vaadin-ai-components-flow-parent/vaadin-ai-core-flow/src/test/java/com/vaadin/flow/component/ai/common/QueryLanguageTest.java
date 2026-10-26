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
package com.vaadin.flow.component.ai.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QueryLanguageTest {

    @Test
    void sql_isNamedSql() {
        Assertions.assertEquals("SQL", QueryLanguage.SQL.getName());
    }

    @Test
    void sql_rulesForbidSelectStar() {
        Assertions.assertTrue(
                QueryLanguage.SQL.getRules().contains("NEVER use SELECT *"));
    }

    @Test
    void sql_rendersGridShapeExamples() {
        Assertions.assertEquals(
                "SELECT ColumnA AS \"Readable A\", ColumnB AS \"Readable B\" FROM TableName",
                QueryLanguage.SQL.exampleQuery("TableName",
                        List.of("ColumnA", "ColumnB"),
                        List.of("Readable A", "Readable B")));
        Assertions.assertEquals(
                "SELECT ColumnA AS \"GroupName.Readable A\", ColumnB AS \"GroupName.Readable B\" FROM TableName",
                QueryLanguage.SQL.exampleQuery("TableName",
                        List.of("ColumnA", "ColumnB"),
                        List.of("GroupName.Readable A",
                                "GroupName.Readable B")));
    }

    @Test
    void sql_rendersChartExamples() {
        Assertions
                .assertEquals("SELECT temperature AS _x, sales AS _y FROM data",
                        QueryLanguage.SQL.exampleQuery("data",
                                List.of("temperature", "sales"),
                                List.of("_x", "_y")));
        Assertions.assertEquals(
                "SELECT date AS _x, open AS _open, high AS _high, low AS _low, close AS _close FROM stock_prices",
                QueryLanguage.SQL.exampleQuery("stock_prices",
                        List.of("date", "open", "high", "low", "close"),
                        List.of("_x", "_open", "_high", "_low", "_close")));
    }

    @Test
    void sql_escapesQuotesInQuotedAliases() {
        Assertions.assertEquals("SELECT a AS \"Say \"\"hi\"\"\" FROM t",
                QueryLanguage.SQL.exampleQuery("t", List.of("a"),
                        List.of("Say \"hi\"")));
    }

    @Test
    void sql_rejectsMismatchedAliases() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> QueryLanguage.SQL.exampleQuery("t", List.of("a", "b"),
                        List.of("x")));
    }

    @Test
    void sql_rejectsEmptyColumns() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> QueryLanguage.SQL.exampleQuery("t", List.of(),
                        List.of()));
    }

    @Test
    void sql_staysTheSameInstanceAfterSerialization()
            throws IOException, ClassNotFoundException {
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(QueryLanguage.SQL);
        }
        try (var in = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            Assertions.assertSame(QueryLanguage.SQL, in.readObject());
        }
    }
}
