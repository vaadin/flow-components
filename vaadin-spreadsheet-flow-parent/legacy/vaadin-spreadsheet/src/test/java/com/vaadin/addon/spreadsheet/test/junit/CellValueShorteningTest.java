package com.vaadin.addon.spreadsheet.test.junit;

import static org.junit.Assert.assertEquals;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.CellValueFormatter;

public class CellValueShorteningTest {

    private CellValueFormatter cellValueFormatter;

    @Before
    public void setUp() throws Exception {
        cellValueFormatter = new CellValueFormatter();
        cellValueFormatter.setLocaleDecimalSymbols(DecimalFormatSymbols
                .getInstance(Locale.US));
    }

    @Test
    public void cellValueShortening_differentInputValues_InputRoundedAccordingToColumnWidth() {
        assertCorrectRounding("10", "10", 2);
        assertCorrectRounding("15", "15", 2);
        assertCorrectRounding("19", "19", 2);

        assertCorrectRounding("#", "15", 1);
        assertCorrectRounding("#", "10", 1);
        assertCorrectRounding("#", "19", 1);
        assertCorrectRounding("##", "199", 2);
        assertCorrectRounding("###", "1999", 3);
        assertCorrectRounding("####", "19999", 4);
        assertCorrectRounding("19999", "19999", 5);

        assertCorrectRounding("0.0000345", "0.0000345", 9);
        assertCorrectRounding("3.45E-5", "0.0000345", 7);
        assertCorrectRounding("3.4E-5", "0.0000345", 6);
        assertCorrectRounding("3E-5", "0.0000345", 5);

        assertCorrectRounding("0.1", "0.10", 3);
        assertCorrectRounding("0", "0.10", 2);

        assertCorrectRounding("0.55", "0.55", 4);
        assertCorrectRounding("0.6", "0.55", 3);
        assertCorrectRounding("1", "0.55", 2);

        assertCorrectRounding("0.99", "0.99", 4);
        assertCorrectRounding("1", "0.99", 3);
        assertCorrectRounding("1", "0.99", 2);

        assertCorrectRounding("0.09", "0.09", 4);
        assertCorrectRounding("0.1", "0.09", 3);
        assertCorrectRounding("0", "0.09", 2);

        assertCorrectRounding("10000", "10000", 6);
        assertCorrectRounding("10000", "10000", 5);

        assertCorrectRounding("110000", "110000", 6);
        assertCorrectRounding("1E5", "110000", 5);
        assertCorrectRounding("####", "110000", 4);

        assertCorrectRounding("11.11111111", "11.1111111119", 11);
        assertCorrectRounding("11.111111112", "11.1111111119", 12);
        assertCorrectRounding("11.1111111119", "11.1111111119", 13);

        assertCorrectRounding("999999999999", "999999999999", 12);
        assertCorrectRounding("1E12", "999999999999", 11);

        assertCorrectRounding("99999999999.9", "99999999999.9", 13);
        assertCorrectRounding("100000000000", "99999999999.9", 12);

        assertCorrectRounding("9999999999.99", "9999999999.99", 13);
        assertCorrectRounding("10000000000", "9999999999.99", 12);

        assertCorrectRounding("999999999.999", "999999999.999", 13);
        assertCorrectRounding("1000000000", "999999999.999", 12);

        assertCorrectRounding("1000000000", "999999999.9999", 10);
        assertCorrectRounding("1000000000", "999999999.9999", 9);

        assertCorrectRounding("123456789199.999", "123456789199.999", 16);
        assertCorrectRounding("123456789200", "123456789199.999", 15);
        assertCorrectRounding("123456789200", "123456789199.999", 12);
        assertCorrectRounding("1.234568E11", "123456789199.999", 11);

        assertCorrectRounding("1234567.89", "1234567.89", 13);
        assertCorrectRounding("1234567.89", "1234567.89", 12);
        assertCorrectRounding("1234567.89", "1234567.89", 11);
        assertCorrectRounding("1234567.89", "1234567.89", 10);
        assertCorrectRounding("1234567.9", "1234567.89", 9);
        assertCorrectRounding("1234568", "1234567.89", 8);
        assertCorrectRounding("1234568", "1234567.89", 7);
        assertCorrectRounding("1.2E6", "1234567.89", 6); // differs from Excel,
                                                         // but acceptable
        assertCorrectRounding("1E6", "1234567.89", 5);
        assertCorrectRounding("1E6", "1234567.89", 4);
        assertCorrectRounding("1E6", "1234567.89", 3);
        assertCorrectRounding("##", "1234567.89", 2);
    }

    private void assertCorrectRounding(String expected, String input, int width) {
        String result = cellValueFormatter
                .getScientificNotationStringForNumericCell(
                        Double.parseDouble(input), input, 1, width);

        assertEquals(String.format(
                "Rounding of %s to %s was wrong, expected %s", input, result,
                expected), expected, result);
    }
}