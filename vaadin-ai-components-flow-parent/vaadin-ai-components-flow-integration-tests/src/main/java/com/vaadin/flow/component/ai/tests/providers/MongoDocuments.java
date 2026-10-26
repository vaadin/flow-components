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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

/**
 * Converts MongoDB documents to the flat column maps the controllers render:
 * subdocuments become dotted keys, BSON dates become Java dates, and other
 * BSON-only types become their Java or text equivalents.
 */
final class MongoDocuments {

    private MongoDocuments() {
    }

    /**
     * Flattens a document into a row map with dotted keys for nested fields.
     *
     * @param document
     *            the document to flatten
     * @return the row
     */
    static Map<String, Object> flatten(Document document) {
        var row = new LinkedHashMap<String, Object>();
        flattenInto(row, "", document);
        return row;
    }

    /**
     * Describes the fields of a document as {@code path (type)} entries, one
     * per leaf field, for a schema description.
     *
     * @param document
     *            a sample document
     * @return the field descriptions
     */
    static List<String> describeFields(Document document) {
        var fields = new LinkedHashMap<String, Object>();
        flattenInto(fields, "", document);
        return fields.entrySet().stream().map(entry -> entry.getKey() + " ("
                + typeName(entry.getValue()) + ")").toList();
    }

    private static void flattenInto(Map<String, Object> row, String prefix,
            Map<?, ?> document) {
        document.forEach((key, value) -> {
            var name = prefix + key;
            if (value instanceof Map<?, ?> nested) {
                flattenInto(row, name + ".", nested);
            } else {
                row.put(name, convert(value));
            }
        });
    }

    private static Object convert(Object value) {
        return switch (value) {
        case null -> null;
        case Date date -> {
            var dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(date.getTime()), ZoneOffset.UTC);
            yield dateTime.toLocalTime().equals(LocalTime.MIDNIGHT)
                    ? dateTime.toLocalDate()
                    : dateTime;
        }
        case Decimal128 decimal -> decimal.bigDecimalValue();
        case ObjectId id -> id.toHexString();
        case List<?> list -> list.stream().map(String::valueOf)
                .collect(Collectors.joining(", "));
        default -> value;
        };
    }

    private static String typeName(Object value) {
        return switch (value) {
        case null -> "null";
        case Integer i -> "int";
        case Long l -> "long";
        case Double d -> "double";
        case Boolean b -> "bool";
        case String s -> "string";
        case java.time.LocalDate d -> "date";
        case LocalDateTime t -> "date";
        default -> value.getClass().getSimpleName().toLowerCase();
        };
    }
}
