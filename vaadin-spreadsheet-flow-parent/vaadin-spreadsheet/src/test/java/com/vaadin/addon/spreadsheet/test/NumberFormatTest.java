package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

/**
 * Tests for number formatting
 *
 */
public class NumberFormatTest extends AbstractSpreadsheetTestCase {

    private SpreadsheetPage spreadsheetPage;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        spreadsheetPage = headerPage.loadFile("number_format.xlsx", this);
        setDefaultLocale();
    }

    @Test
    public void numberFormat_sheetWithNumberFormatRuleForNumericCells_contentsFormattedAccordingToLocale() {
        assertTest(Type.CHECK_DEFAULTS, Expected.values());
    }

    @Test
    public void onCellValueChange_sheetWithNumberFormatRuleForNumericCells_noNumberFormatWhenNumberReplacedWithStringThatStartsWithNumber() {
        assertTest(Type.REPLACE_NUMBER_WITH_STRING, Expected.STRING_3RD,
                Expected.STRING_3TH_PLACE, Expected.STRING_3_DL);
    }

    @Test
    public void numberFormat_sheetWithNumberFormatRuleForNumericCells_formulaFieldHasNoDecimalsForIntegers() {
        spreadsheetPage.clickOnCell(Expected.INTEGER_INTEGER.getCell());
        assertEquals("Unexpected formula field value,",
                Expected.INTEGER_INTEGER.getValue(),
                spreadsheetPage.getFormulaFieldValue());
    }

    @Test
    public void numberFormat_sheetWithNumberFormatRuleForNumericCells_formulaFieldHasDecimalsForRoundedDoubles() {
        spreadsheetPage.clickOnCell(Expected.INTEGER_DECIMAL_FORMAT1.getCell());
        assertEquals("Unexpected formula field value,",
                Expected.INTEGER_DECIMAL.getValue(),
                spreadsheetPage.getFormulaFieldValue());
    }

    @Test
    public void numberFormat_sheetWithNumberFormatRuleForNumericCells_formulaFieldHasLocalizedDecimalSeparatorForDoubles() {
        Locale locale = new Locale("fi", "FI");
        //TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        //When https://github.com/vaadin/framework8-issues/issues/477 is fixed
        setLocale(locale);
        spreadsheetPage.clickOnCell(Expected.INTEGER_DECIMAL_FORMAT1.getCell());
        assertEquals("Unexpected formula field value for Finnish locale,",
                Expected.INTEGER_DECIMAL.getValue().replace(".", ","),
                spreadsheetPage.getFormulaFieldValue());
    }

    private void assertTest(Type type, Expected... expected) {
        List<AssertionError> errors = new ArrayList<AssertionError>();
        for (Expected e : expected) {
            try {
                switch (type) {
                case CHECK_DEFAULTS:
                    assertEquals(e.toString(), e.getValue(),
                            spreadsheetPage.getCellValue(e.getCell()));
                    break;
                case REPLACE_NUMBER_WITH_STRING:
                    replaceNumberWithStringThatStartsWithNumber(e);
                    break;
                default:
                    break;
                }
            } catch (AssertionError err) {
                errors.add(err);
            }
        }
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (AssertionError err : errors) {
                sb.append(err.getMessage());
                sb.append(" ");
            }
            fail(errors.size() + " of " + expected.length + " tests failed: "
                    + sb.toString());
        }
    }

    private void replaceNumberWithStringThatStartsWithNumber(Expected e) {
        SheetCellElement cell = spreadsheetPage.getCellAt(3, 3);
        try {
            // check the default value
            assertEquals("Unexpected initial value,",
                    Expected.INTEGER_INTEGER_FORMAT2.getValue(),
                    cell.getValue());

            // replace with String that start with number
            cell.setValue(e.getValue());

            cell = spreadsheetPage.getCellAt(3, 3);
            assertEquals("Unexpected updated value,", e.getValue(),
                    cell.getValue());
        } finally {
            // set back the default value
            cell.setValue(Expected.INTEGER_INTEGER_FORMAT2.getValue());
        }
    }

    private void setDefaultLocale() {
        //TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        //When https://github.com/vaadin/framework8-issues/issues/477 is fixed
        setLocale(Locale.US);
    }

    public enum Type {
        CHECK_DEFAULTS, REPLACE_NUMBER_WITH_STRING;
    }

    public enum Expected {
        INTEGER_INTEGER("A3", "3333"), //
        INTEGER_DECIMAL("A4", "3333.333"), //
        INTEGER_INTEGER_FORMAT1("B3", "3333"), //
        INTEGER_DECIMAL_FORMAT1("B4", "3333"), //
        INTEGER_INTEGER_FORMAT2("C3", "3,333"), //
        INTEGER_DECIMAL_FORMAT2("C4", "3,333"), //
        DECIMAL_FORMAT1_3DIGIT("E3", "3333.333"), //
        DECIMAL_FORMAT1_2DIGIT("E4", "3333.33"), //
        DECIMAL_FORMAT1_1DIGIT("E5", "3333.3"), //
        DECIMAL_FORMAT2_3DIGIT("F3", "3,333.333"), //
        DECIMAL_FORMAT2_2DIGIT("F4", "3,333.33"), //
        DECIMAL_FORMAT2_1DIGIT("F5", "3,333.3"), //
        CURRENCY_EUR_FI("H3", "3,333.33 €"), //
        CURRENCY_GPD("I3", "£3,333.33"), //
        CURRENCY_USD("J3", "$3,333.33"), //
        CURRENCY_JPY("K3", "\u00A53,333.33"), //
        STRING_3RD("M3", "3rd"), //
        STRING_3TH_PLACE("M4", "3th place"), //
        STRING_3_DL("M5", "3 dl"); //

        private String cell;
        private String value;

        private Expected(String cell, String value) {
            this.cell = cell;
            this.value = value;
        }

        public String getCell() {
            return cell;
        }

        public String getValue() {
            return value;
        }
    }
}
