/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Computes the constant arithmetic increment of a sequence of values, or
 * returns {@code null} when the values are not a regular arithmetic
 * progression. Two overloads:
 *
 * <ul>
 * <li>{@code Double[]} — pure numeric progression (e.g. {@code 2, 4, 6} →
 * {@code 2}); any null entry aborts.</li>
 * <li>{@code String[]} — values that share a constant text prefix and end in
 * digits (e.g. {@code "A1", "A2", "A3"} → {@code 1}); the prefix must match
 * exactly across values, the digits must form an arithmetic progression. A
 * single value returns {@code 1d} as a convenience.</li>
 * </ul>
 *
 * <p>
 * Pure functions on the inputs — no POI access, no Spreadsheet dependency. Used
 * by {@link RangeSequenceDetector} when a selection is extended (Excel autofill
 * semantics).
 */
final class SequenceIncrement {

    /**
     * Trailing-digit pattern. Used by this class's string-sequence overload,
     * and also by {@link CellValueShifter} when extending a string-with-digits
     * sequence onto a new cell — same regex serves both detection (here) and
     * application (there), so they cannot drift.
     */
    static final Pattern TRAILING_DIGITS_PATTERN = Pattern.compile("\\d+$");

    private SequenceIncrement() {
    }

    /**
     * Increment between consecutive numeric values, or {@code null} if the
     * differences are not all equal or the array contains a null entry.
     */
    static Double compute(Double[] values) {
        Double result = null;
        for (int i = 1; i < values.length; i++) {
            if (values[i] == null || values[i - 1] == null) {
                return null;
            }
            Double diff = values[i] - values[i - 1];
            if (result == null) {
                result = diff;
            } else if (!result.equals(diff)) {
                return null;
            }
        }
        return result;
    }

    /**
     * Increment between consecutive string-with-trailing-digits values,
     * requiring all entries to share an identical pre-digit prefix and their
     * digit suffixes to form an arithmetic progression.
     *
     * <p>
     * Returns {@code 1d} when there is only one value (Excel autofill default
     * when the user has a single cell to extend), and {@code null} when the
     * values don't match the pattern (no trailing digits, mixed prefixes,
     * non-constant differences).
     */
    static Double compute(String[] values) {
        Double result = null;
        String previousConstant;
        Double previousValue;
        Matcher matcher = TRAILING_DIGITS_PATTERN.matcher(values[0]);
        if (matcher.find()) {
            previousConstant = values[0].substring(0, matcher.start());
            previousValue = Double.parseDouble(matcher.group());
        } else {
            return null;
        }
        if (values.length == 1) {
            return 1d;
        }
        for (int i = 1; i < values.length; i++) {
            String currentValue = values[i];
            if (currentValue == null
                    || !currentValue.startsWith(previousConstant)) {
                return null;
            }
            matcher = TRAILING_DIGITS_PATTERN.matcher(currentValue);
            if (!matcher.find()) {
                return null;
            }
            String constant = currentValue.substring(0, matcher.start());
            if (!previousConstant.equals(constant)) {
                return null;
            }
            Double value = Double.parseDouble(matcher.group());
            Double diff = value - previousValue;
            if (result == null) {
                result = diff;
            } else if (!result.equals(diff)) {
                return null;
            }
            previousValue = value;
        }
        return result;
    }
}
