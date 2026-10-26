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
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Turns text values, as CSV files and some JSON fields carry them, into the
 * typed values the controllers render: ISO dates and timestamps become
 * {@code java.time} values and, when asked, numbers and booleans become numbers
 * and booleans.
 */
final class TextValues {

    private static final Pattern INTEGER = Pattern.compile("-?\\d{1,18}");
    private static final Pattern DECIMAL = Pattern
            .compile("-?\\d{1,15}\\.\\d{1,10}");

    private TextValues() {
    }

    /**
     * Parses a text value.
     *
     * @param text
     *            the text, not {@code null}
     * @param parseNumbers
     *            {@code true} to turn text that looks like a number or a
     *            boolean into one, {@code false} to keep such text as it is
     * @return {@code null} for empty text, otherwise the typed value or the
     *         text itself
     */
    static Object parse(String text, boolean parseNumbers) {
        if (text.isEmpty()) {
            return null;
        }
        if (text.length() == 10 && text.charAt(4) == '-') {
            try {
                return LocalDate.parse(text);
            } catch (DateTimeParseException e) {
                // Not a date after all, keep the text
            }
        }
        if (text.length() >= 19 && text.charAt(10) == 'T') {
            try {
                return LocalDateTime.parse(text);
            } catch (DateTimeParseException e) {
                // Not a timestamp after all, keep the text
            }
        }
        if (parseNumbers) {
            if (INTEGER.matcher(text).matches()) {
                return Long.parseLong(text);
            }
            if (DECIMAL.matcher(text).matches()) {
                return new BigDecimal(text);
            }
            if (text.equalsIgnoreCase("true")
                    || text.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(text);
            }
        }
        return text;
    }
}
