/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;

/**
 * Utility class for formatting cell values
 */
public class CellValueFormatter implements Serializable {

    private DecimalFormatSymbols localeDecimalSymbols = DecimalFormatSymbols
            .getInstance();

    public String getScientificNotationStringForNumericCell(double numericValue,
            String formattedValue, float cellWidthRatio, int width) {
        BigDecimal ratio = BigDecimal.valueOf(cellWidthRatio);
        BigDecimal columnWidth = BigDecimal.valueOf(width);
        int numberOfDigits = columnWidth.divide(ratio, RoundingMode.DOWN)
                .intValue();
        if (numberOfDigits < 2) {
            return "#";
        } else {
            int integerPartLength = formattedValue
                    .indexOf(localeDecimalSymbols.getDecimalSeparator());
            if (integerPartLength == -1) {
                integerPartLength = formattedValue.length();
            }
            StringBuilder format = new StringBuilder("0");

            if (integerPartLength == formattedValue.length()
                    && numberOfDigits <= 4
                    && integerPartLength > numberOfDigits) {
                return createFillString(numberOfDigits);
            }

            // Needs scientific if integer part doesn't fit or if it's only
            // decimals and all decimal don't fit
            boolean needsScientific = integerPartLength > numberOfDigits
                    || (Math.abs(numericValue) < 1
                            && formattedValue.length() > numberOfDigits
                            && numberOfDigits > 4);
            int numberOfDecimals = 0;
            if (needsScientific) {
                // 0.#E10
                numberOfDecimals = numberOfDigits - 5;
            } else {
                numberOfDecimals = numberOfDigits - (integerPartLength + 1);
            }

            if (numberOfDecimals > 0) {
                format.append('.');
                for (int i = 0; i < numberOfDecimals; i++) {
                    format.append('#');
                }
            }
            if (needsScientific) {
                format.append("E0");
            }

            if (format.length() > numberOfDigits) {
                return createFillString(numberOfDigits);
            }

            return new DecimalFormat(format.toString(), localeDecimalSymbols)
                    .format(numericValue);
        }
    }

    private String createFillString(int numberOfDigits) {
        char[] filling = new char[numberOfDigits];
        Arrays.fill(filling, '#');
        return new String(filling);
    }

    public void setLocaleDecimalSymbols(
            DecimalFormatSymbols localeDecimalSymbols) {
        this.localeDecimalSymbols = localeDecimalSymbols;
    }
}
