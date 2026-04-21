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
package com.vaadin.flow.component.ai.grid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AIDataRowTest {

    @Test
    void constructor_nullMap_throwsNullPointerException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new AIDataRow(null));
    }

    @Test
    void get_existingColumn_returnsValue() {
        var row = new AIDataRow(Map.of("name", "Alice", "age", 30));
        Assertions.assertEquals("Alice", row.get("name"));
        Assertions.assertEquals(30, row.get("age"));
    }

    @Test
    void get_missingColumn_returnsNull() {
        var row = new AIDataRow(Map.of("name", "Alice"));
        Assertions.assertNull(row.get("missing"));
    }

    @Test
    void get_columnWithNullValue_returnsNull() {
        var source = new HashMap<String, Object>();
        source.put("nullable", null);
        var row = new AIDataRow(source);
        Assertions.assertNull(row.get("nullable"));
    }

    @Test
    void get_nullColumnName_returnsNull() {
        var row = new AIDataRow(Map.of("a", 1));
        Assertions.assertNull(row.get(null));
    }

    @Test
    void entries_returnsAllEntries() {
        var source = new LinkedHashMap<String, Object>();
        source.put("a", 1);
        source.put("b", 2);
        var row = new AIDataRow(source);

        var entries = row.entries();
        Assertions.assertEquals(2, entries.size());
        var keys = entries.stream().map(Map.Entry::getKey).toList();
        Assertions.assertEquals(List.of("a", "b"), keys);
    }

    @Test
    void entries_preservesInsertionOrder() {
        var source = new LinkedHashMap<String, Object>();
        source.put("z", 1);
        source.put("a", 2);
        source.put("m", 3);
        var row = new AIDataRow(source);

        var keys = row.entries().stream().map(Map.Entry::getKey).toList();
        Assertions.assertEquals(List.of("z", "a", "m"), keys);
    }

    @Test
    void constructor_defensiveCopy_sourceMutationDoesNotAffectRow() {
        var source = new HashMap<String, Object>();
        source.put("name", "Alice");
        var row = new AIDataRow(source);

        source.put("name", "Bob");
        source.put("extra", "value");

        Assertions.assertEquals("Alice", row.get("name"));
        Assertions.assertNull(row.get("extra"));
        Assertions.assertEquals(1, row.entries().size());
    }

    @Test
    void serialization_roundTrip_preservesValues() throws Exception {
        var source = new LinkedHashMap<String, Object>();
        source.put("text", "hello");
        source.put("number", 42);
        source.put("date", LocalDate.of(2024, 6, 15));
        var row = new AIDataRow(source);

        var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(row);
        }
        AIDataRow restored;
        try (var ois = new ObjectInputStream(
                new ByteArrayInputStream(baos.toByteArray()))) {
            restored = (AIDataRow) ois.readObject();
        }

        Assertions.assertEquals("hello", restored.get("text"));
        Assertions.assertEquals(42, restored.get("number"));
        Assertions.assertEquals(LocalDate.of(2024, 6, 15),
                restored.get("date"));
    }
}
