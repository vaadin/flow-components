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

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Formatting utilities for grid column headers and cell values.
 *
 * @author Vaadin Ltd
 */
final class GridFormatting implements Serializable {

    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd");
    static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm");

    private GridFormatting() {
    }

    /**
     * Formats a value for display based on its Java type. Dates are formatted
     * as {@code yyyy-MM-dd} or {@code yyyy-MM-dd HH:mm}, booleans as
     * {@code Yes}/{@code No}, and decimal numbers have trailing zeros stripped.
     *
     * @param value
     *            the raw value, not {@code null}
     * @return the formatted string, never {@code null}
     */
    static String formatValue(Object value) {
        return switch (value) {
        case LocalDate d -> d.format(DATE_FORMATTER);
        case LocalDateTime dt -> dt.format(DATETIME_FORMATTER);
        case Instant i ->
            i.atZone(ZoneId.systemDefault()).format(DATETIME_FORMATTER);
        case Timestamp ts -> ts.toLocalDateTime().format(DATETIME_FORMATTER);
        case java.sql.Date d -> d.toLocalDate().format(DATE_FORMATTER);
        case Date d -> d.toInstant().atZone(ZoneId.systemDefault())
                .format(DATETIME_FORMATTER);
        case Boolean b -> b ? "Yes" : "No";
        case BigDecimal bd -> bd.stripTrailingZeros().toPlainString();
        case Double d ->
            BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
        case Float f ->
            BigDecimal.valueOf(f).stripTrailingZeros().toPlainString();
        default -> value.toString();
        };
    }

    /**
     * Formats a column name into a readable header. Replaces underscores with
     * spaces and capitalizes each word. Names already containing spaces are
     * returned as-is.
     *
     * @param columnName
     *            the raw column name, not {@code null}
     * @return the formatted header, never {@code null}
     */
    static String formatHeader(String columnName) {
        if (columnName.contains(" ")) {
            return columnName;
        }
        var words = columnName.replace('_', ' ').split("\\s+");
        return Arrays.stream(words).filter(s -> !s.isBlank()).map(String::trim)
                .map(GridFormatting::capitalizeFirst)
                .collect(Collectors.joining(" "));
    }

    /**
     * Returns the group prefix from a dot-separated column name. Returns an
     * empty string if no dot is present, which sorts ungrouped columns first.
     *
     * @param columnName
     *            the column name, not {@code null}
     * @return the prefix before the first dot, or empty string
     */
    static String groupPrefix(String columnName) {
        var dotIndex = columnName.indexOf('.');
        return dotIndex > 0 ? columnName.substring(0, dotIndex) : "";
    }

    /**
     * Strips the group prefix from a dot-separated column name. Returns the
     * part after the first dot, or the full name if no dot is present.
     *
     * @param columnName
     *            the column name, not {@code null}
     * @return the name without group prefix, never {@code null}
     */
    static String stripGroupPrefix(String columnName) {
        var dotIndex = columnName.indexOf('.');
        return dotIndex > 0 ? columnName.substring(dotIndex + 1) : columnName;
    }

    private static String capitalizeFirst(String str) {
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        return str.substring(0, 1).toUpperCase()
                + str.substring(1).toLowerCase();
    }
}
