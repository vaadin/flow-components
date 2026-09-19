/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

/**
 * Characterization tests for {@link CellNumericFormatter}.
 *
 * <p>
 * Pins:
 * <ul>
 * <li>{@code updateLocale} rebuilds all four formatting fields together
 * (formatter, decimal format, locale symbols, embedded value formatter
 * symbols).</li>
 * <li>{@code getOriginalCellValue} dispatches on {@link CellType} per branch —
 * formula source / date-formatted numeric / numeric / string with-and-without
 * leading quote / boolean / error / blank / default.</li>
 * <li>{@code getCachedFormulaCellValue} dispatches on the formula's cached
 * result type and routes numeric values through the
 * {@link DataFormatter#formatRawCellContents} call.</li>
 * <li>{@code valueContainsOnlyNumbers} and {@code isGeneralCell}
 * predicates.</li>
 * </ul>
 *
 * <p>
 * Uses {@link MockedStatic} for {@link DateUtil#isCellDateFormatted} and
 * {@link SpreadsheetUtil#needsLeadingQuote} so the branches can be exercised
 * without constructing real POI cells with the exact style XML those helpers
 * inspect.
 */
class CellNumericFormatterTest {

    @Mock
    private Cell cell;
    @Mock
    private CellStyle style;

    private CellNumericFormatter formatter;
    private MockedStatic<DateUtil> dateUtilStatic;
    private MockedStatic<SpreadsheetUtil> utilStatic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        formatter = new CellNumericFormatter();
        dateUtilStatic = mockStatic(DateUtil.class);
        utilStatic = mockStatic(SpreadsheetUtil.class);
        lenient().when(cell.getCellStyle()).thenReturn(style);
    }

    @AfterEach
    void tearDown() {
        dateUtilStatic.close();
        utilStatic.close();
    }

    // ----- constructor defaults --------------------------------------------

    @Test
    void freshFormatter_exposesNonNullFormatterAndDecimalAndValueFormatter() {
        // Pin: out-of-the-box the four fields are populated. Any future
        // change that lazy-inits them would break consumers calling
        // getDataFormatter() before updateLocale().
        assertTrue(formatter.getDataFormatter() instanceof CustomDataFormatter,
                "Default DataFormatter must be the locale-aware CustomDataFormatter");
        // Round-trip Pi to verify "###.################" precision (15+
        // fractional digits). A regression to "#.##" would round Pi to
        // "3.14". The separator depends on the JVM default locale, so
        // assert digit content rather than the literal string.
        String formatted = formatter.getOriginalValueDecimalFormat()
                .format(Math.PI);
        assertEquals("3141592653589793", formatted.replaceAll("\\D", ""),
                "DecimalFormat MUST preserve all 15 fractional digits of Pi");
        org.junit.jupiter.api.Assertions
                .assertNotNull(formatter.getCellValueFormatter());
    }

    // ----- updateLocale -----------------------------------------------------

    @Test
    void updateLocale_replacesAllFourFields_withNewLocaleVariants() {
        // Pin: updateLocale MUST replace the DataFormatter, the
        // originalValueDecimalFormat and (transitively) the
        // CellValueFormatter's locale symbols all together. Re-using the
        // pre-locale-update instances would silently leave the formula bar
        // formatting English numbers while the cell display uses German
        // ones.
        DataFormatter before = formatter.getDataFormatter();
        DecimalFormat decimalBefore = formatter.getOriginalValueDecimalFormat();

        formatter.updateLocale(Locale.GERMANY);

        assertNotSame(before, formatter.getDataFormatter(),
                "DataFormatter must be replaced (new locale instance)");
        assertNotSame(decimalBefore, formatter.getOriginalValueDecimalFormat(),
                "originalValueDecimalFormat must be replaced");
        // German uses comma as decimal separator. Pin the actual locale
        // behavior, not just instance identity — confirms the new format
        // really USES the locale, instead of being a fresh copy of the
        // default one.
        assertEquals("3,141592653589793",
                formatter.getOriginalValueDecimalFormat().format(Math.PI));
    }

    @Test
    void updateLocale_localeSymbolsFlipDecimalSeparatorAcceptance() {
        // Pin: the localeDecimalSymbols rebuild during updateLocale is the
        // same instance used by valueContainsOnlyNumbers. Switching from
        // US (dot separator) to Germany (comma separator) MUST change
        // what the predicate accepts under the comma-only branch.
        //
        // Use a comma-vs-dot pair where only ONE form is unambiguously
        // numeric. "1,5x" with a trailing non-digit fails under both
        // locales; "1,5" alone is matched under Germany. Under US the
        // regex's '.' meta-character is "any char", which masks the
        // separator change for single-char alternatives — see
        // valueContainsOnlyNumbers_commaUnderUSLocale_pinsRegexMetacharQuirk.
        formatter.updateLocale(Locale.US);
        assertFalse(formatter.valueContainsOnlyNumbers("1,5a"),
                "Trailing letter must never match");

        formatter.updateLocale(Locale.GERMANY);

        assertTrue(formatter.valueContainsOnlyNumbers("1,5"),
                "Comma must match AFTER switching to a comma-separator locale");
    }

    // ----- DataFormatter accessors ------------------------------------------

    @Test
    void setDataFormatter_replacesTheGetterValue() {
        DataFormatter custom = new DataFormatter();

        formatter.setDataFormatter(custom);

        assertSame(custom, formatter.getDataFormatter(),
                "setDataFormatter must replace the field, not wrap or copy");
    }

    // ----- getOriginalCellValue: cell-type dispatch ------------------------

    @Test
    void getOriginalCellValue_nullCell_returnsEmptyString() {
        // Pin: null cell ALWAYS returns "" — protects formula-bar code
        // from NPE when a sheet position has no underlying cell.
        assertEquals("", formatter.getOriginalCellValue(null));
    }

    @Test
    void getOriginalCellValue_formulaCell_returnsCellFormula() {
        when(cell.getCellType()).thenReturn(CellType.FORMULA);
        when(cell.getCellFormula()).thenReturn("SUM(A1:A10)");

        assertEquals("SUM(A1:A10)", formatter.getOriginalCellValue(cell));
    }

    @Test
    void getOriginalCellValue_numericNonDate_formatsViaDecimalFormat() {
        // Pin: NUMERIC without date format must run through the
        // originalValueDecimalFormat — preserves up to 16 fractional
        // digits in the formula bar so the user sees raw precision. Pin
        // the locale to US so the assertion is deterministic across hosts.
        formatter.updateLocale(Locale.US);
        when(cell.getCellType()).thenReturn(CellType.NUMERIC);
        dateUtilStatic.when(() -> DateUtil.isCellDateFormatted(cell))
                .thenReturn(false);
        when(cell.getNumericCellValue()).thenReturn(3.141592653589793);

        assertEquals("3.141592653589793", formatter.getOriginalCellValue(cell));
    }

    @Test
    void getOriginalCellValue_numericDateFormatted_formatsInTheSpreadsheetLocale() {
        // Pin: NUMERIC with a date format runs through the locale's short
        // date+time form - the same shape a no-arg SimpleDateFormat gives,
        // but for the locale the spreadsheet was given rather than the JVM's.
        formatter.updateLocale(Locale.US);
        when(cell.getCellType()).thenReturn(CellType.NUMERIC);
        dateUtilStatic.when(() -> DateUtil.isCellDateFormatted(cell))
                .thenReturn(true);
        Date date = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 30)
                .getTime();
        when(cell.getDateCellValue()).thenReturn(date);

        String expected = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT, Locale.US).format(date);
        assertEquals(expected, formatter.getOriginalCellValue(cell));
    }

    @Test
    void getOriginalCellValue_numericDateFormatted_localeChanged_followsTheNewLocale() {
        // The formula bar has to speak the spreadsheet's language. Before
        // this was wired up the date branch always used the JVM default,
        // so setLocale had no effect on it.
        when(cell.getCellType()).thenReturn(CellType.NUMERIC);
        dateUtilStatic.when(() -> DateUtil.isCellDateFormatted(cell))
                .thenReturn(true);
        Date date = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 30)
                .getTime();
        when(cell.getDateCellValue()).thenReturn(date);

        formatter.updateLocale(Locale.US);
        String us = formatter.getOriginalCellValue(cell);
        formatter.updateLocale(Locale.GERMANY);
        String germany = formatter.getOriginalCellValue(cell);

        assertEquals(
                DateFormat.getDateTimeInstance(DateFormat.SHORT,
                        DateFormat.SHORT, Locale.GERMANY).format(date),
                germany);
        assertNotEquals(us, germany,
                "US and German short date+time forms differ");
    }

    @Test
    void getOriginalCellValue_numericDateFormatted_formatsInPoisTimeZone() {
        // getDateCellValue reads the cell's serial number as a wall clock in
        // POI's user time zone. Formatting in the JVM's zone instead would
        // move the date whenever an application sets one on POI.
        formatter.updateLocale(Locale.US);
        when(cell.getCellType()).thenReturn(CellType.NUMERIC);
        dateUtilStatic.when(() -> DateUtil.isCellDateFormatted(cell))
                .thenReturn(true);
        Date date = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 30)
                .getTime();
        when(cell.getDateCellValue()).thenReturn(date);

        TimeZone originalUserZone = LocaleUtil.getUserTimeZone();
        try {
            LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("UTC"));
            DateFormat inUtc = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                    DateFormat.SHORT, Locale.US);
            inUtc.setTimeZone(TimeZone.getTimeZone("UTC"));

            assertEquals(inUtc.format(date),
                    formatter.getOriginalCellValue(cell));
        } finally {
            LocaleUtil.setUserTimeZone(originalUserZone);
        }
    }

    @Test
    void getOriginalCellValue_numericDateFormatted_nullDate_returnsEmptyString() {
        // Pin: the guard against a null Date inside the date branch —
        // removing it would NPE inside SimpleDateFormat.format(null).
        when(cell.getCellType()).thenReturn(CellType.NUMERIC);
        dateUtilStatic.when(() -> DateUtil.isCellDateFormatted(cell))
                .thenReturn(true);
        when(cell.getDateCellValue()).thenReturn(null);

        assertEquals("", formatter.getOriginalCellValue(cell));
    }

    @Test
    void getOriginalCellValue_stringWithoutLeadingQuote_returnsRawString() {
        when(cell.getCellType()).thenReturn(CellType.STRING);
        when(cell.getStringCellValue()).thenReturn("hello");
        utilStatic.when(() -> SpreadsheetUtil.needsLeadingQuote(cell))
                .thenReturn(false);

        assertEquals("hello", formatter.getOriginalCellValue(cell));
    }

    @Test
    void getOriginalCellValue_stringNeedsLeadingQuote_prependsSingleQuote() {
        // Pin: when the cell's quote-prefix style demands a leading quote
        // in the formula bar, the value is prefixed with '. Forgetting
        // the prefix would lose the user's intent to escape a leading "=".
        when(cell.getCellType()).thenReturn(CellType.STRING);
        when(cell.getStringCellValue()).thenReturn("=hello");
        utilStatic.when(() -> SpreadsheetUtil.needsLeadingQuote(cell))
                .thenReturn(true);

        assertEquals("'=hello", formatter.getOriginalCellValue(cell));
    }

    @Test
    void getOriginalCellValue_booleanTrue_returnsLowercaseString() {
        when(cell.getCellType()).thenReturn(CellType.BOOLEAN);
        when(cell.getBooleanCellValue()).thenReturn(true);

        assertEquals("true", formatter.getOriginalCellValue(cell));
    }

    @Test
    void getOriginalCellValue_booleanFalse_returnsLowercaseString() {
        when(cell.getCellType()).thenReturn(CellType.BOOLEAN);
        when(cell.getBooleanCellValue()).thenReturn(false);

        assertEquals("false", formatter.getOriginalCellValue(cell));
    }

    @Test
    void getOriginalCellValue_blankCell_returnsEmptyString_andQueriesNoValueGetters() {
        // Pin: BLANK returns "" via the explicit case — NOT via fall-
        // through to default. Verify no value-extraction getters were
        // consulted; a regression that fell through to STRING/NUMERIC/
        // BOOLEAN/ERROR would invoke one of those getters and this
        // verification would fail. Removing the explicit BLANK case alone
        // is intentionally NOT caught here — default also returns ""
        // by contract, and that case is pinned by the _NONE test.
        when(cell.getCellType()).thenReturn(CellType.BLANK);

        assertEquals("", formatter.getOriginalCellValue(cell));

        org.mockito.Mockito.verify(cell, org.mockito.Mockito.never())
                .getCellFormula();
        org.mockito.Mockito.verify(cell, org.mockito.Mockito.never())
                .getStringCellValue();
        org.mockito.Mockito.verify(cell, org.mockito.Mockito.never())
                .getBooleanCellValue();
        org.mockito.Mockito.verify(cell, org.mockito.Mockito.never())
                .getNumericCellValue();
        org.mockito.Mockito.verify(cell, org.mockito.Mockito.never())
                .getErrorCellValue();
        org.mockito.Mockito.verify(cell, org.mockito.Mockito.never())
                .getDateCellValue();
    }

    @Test
    void getOriginalCellValue_errorCell_returnsErrorCodeAsString() {
        // Pin: ERROR returns the BYTE error code as a string — NOT the
        // human-readable error text (that's reserved for the cached-
        // formula path). Confusing the two would change formula-bar UX.
        when(cell.getCellType()).thenReturn(CellType.ERROR);
        when(cell.getErrorCellValue()).thenReturn((byte) 7); // #DIV/0!

        assertEquals("7", formatter.getOriginalCellValue(cell));
    }

    @Test
    void getOriginalCellValue_underscoreNoneCellType_returnsEmptyString() {
        // Pin: any cell type not in the switch (here _NONE) falls into
        // the default arm and returns "". Removing the default arm would
        // break the formula bar for placeholder cells.
        when(cell.getCellType()).thenReturn(CellType._NONE);

        assertEquals("", formatter.getOriginalCellValue(cell));
    }

    // ----- getCachedFormulaCellValue: cached-type dispatch -----------------

    @Test
    void getCachedFormulaCellValue_cachedString_returnsStringCellValue() {
        when(cell.getCachedFormulaResultType()).thenReturn(CellType.STRING);
        when(cell.getStringCellValue()).thenReturn("cached-text");

        assertEquals("cached-text", formatter.getCachedFormulaCellValue(cell));
    }

    @Test
    void getCachedFormulaCellValue_cachedBlank_returnsStringCellValue() {
        // Pin: BLANK / _NONE / FORMULA all share the STRING arm —
        // they read getStringCellValue(). Splitting them up would change
        // behavior for blank formula cells.
        when(cell.getCachedFormulaResultType()).thenReturn(CellType.BLANK);
        when(cell.getStringCellValue()).thenReturn("");

        assertEquals("", formatter.getCachedFormulaCellValue(cell));
    }

    @Test
    void getCachedFormulaCellValue_cachedBoolean_returnsLowercaseString() {
        when(cell.getCachedFormulaResultType()).thenReturn(CellType.BOOLEAN);
        when(cell.getBooleanCellValue()).thenReturn(true);

        assertEquals("true", formatter.getCachedFormulaCellValue(cell));
    }

    @Test
    void getCachedFormulaCellValue_cachedError_returnsErrorEvalText() {
        // Pin: cached-formula ERROR uses ErrorEval.getText (human-
        // readable "#DIV/0!"), distinguishing it from the raw branch
        // that returns the byte. Cell.error 7 = #DIV/0!.
        when(cell.getCachedFormulaResultType()).thenReturn(CellType.ERROR);
        when(cell.getErrorCellValue()).thenReturn((byte) 7);

        assertEquals("#DIV/0!", formatter.getCachedFormulaCellValue(cell));
    }

    @Test
    void getCachedFormulaCellValue_cachedNumeric_callsDataFormatterFormatRawCellContents() {
        // Pin: NUMERIC cached result must route through
        // formatter.formatRawCellContents(value, format, formatString) —
        // skipping the formatter would leave un-formatted raw doubles in
        // the client cache.
        DataFormatter stub = new DataFormatter() {
            @Override
            public String formatRawCellContents(double v, int format,
                    String formatString) {
                return "FMT(" + v + "," + format + "," + formatString + ")";
            }
        };
        formatter.setDataFormatter(stub);

        when(cell.getCachedFormulaResultType()).thenReturn(CellType.NUMERIC);
        when(cell.getNumericCellValue()).thenReturn(42.5);
        when(style.getDataFormat()).thenReturn((short) 9);
        when(style.getDataFormatString()).thenReturn("0.00");

        assertEquals("FMT(42.5,9,0.00)",
                formatter.getCachedFormulaCellValue(cell));
    }

    // ----- valueContainsOnlyNumbers ----------------------------------------

    @Test
    void valueContainsOnlyNumbers_pureInteger_true() {
        assertTrue(formatter.valueContainsOnlyNumbers("123"));
    }

    @Test
    void valueContainsOnlyNumbers_negativeInteger_true() {
        assertTrue(formatter.valueContainsOnlyNumbers("-42"));
    }

    @Test
    void valueContainsOnlyNumbers_decimalWithDotSeparatorUnderUSLocale_true() {
        // Pin under US locale — '.' is the decimal separator there.
        formatter.updateLocale(Locale.US);

        assertTrue(formatter.valueContainsOnlyNumbers("1.5"));
    }

    @Test
    void valueContainsOnlyNumbers_separatorIsALiteral_notARegex() {
        // The separator used to be concatenated into the regex unquoted,
        // so under US - where it is '.' - it meant "any character" and
        // "1,5" and "1X5" counted as numbers. It is quoted now, so only
        // the separator the locale actually uses is accepted.
        formatter.updateLocale(Locale.US);

        assertFalse(formatter.valueContainsOnlyNumbers("1,5"),
                "A comma is not a US decimal separator");
        assertFalse(formatter.valueContainsOnlyNumbers("1X5"),
                "Nor is any other character");
        assertTrue(formatter.valueContainsOnlyNumbers("1.5"),
                "The US separator still is");
    }

    @Test
    void valueContainsOnlyNumbers_localeMinusSign_true() {
        // Finnish writes a negative number with U+2212, not the hyphen.
        // Reading only the hyphen left every negative number in such a
        // locale looking like something other than a number, so it was
        // never shortened to fit its column - it just overflowed.
        formatter.updateLocale(new Locale("fi", "FI"));

        assertTrue(formatter.valueContainsOnlyNumbers("\u221242"),
                "The locale's own minus sign");
        assertTrue(formatter.valueContainsOnlyNumbers("\u22121,5"),
                "And with a decimal part");
    }

    @Test
    void valueContainsOnlyNumbers_hyphenStillAcceptedInAnyLocale() {
        // A value can reach here formatted either way.
        formatter.updateLocale(new Locale("fi", "FI"));

        assertTrue(formatter.valueContainsOnlyNumbers("-42"));
    }

    @Test
    void valueContainsOnlyNumbers_twoMinusSigns_false() {
        formatter.updateLocale(new Locale("fi", "FI"));

        assertFalse(formatter.valueContainsOnlyNumbers("\u2212-42"));
    }

    @Test
    void valueContainsOnlyNumbers_commaUnderGermanLocale_true_dotFalse() {
        // Pin: under German locale the separator is ',' (NOT a regex
        // metacharacter), so the predicate behaves as intuitively named —
        // only comma matches, dot does not.
        formatter.updateLocale(Locale.GERMANY);

        assertTrue(formatter.valueContainsOnlyNumbers("1,5"));
        assertFalse(formatter.valueContainsOnlyNumbers("1.5"),
                "Dot must not match under a comma-separator locale");
    }

    @Test
    void valueContainsOnlyNumbers_alphanumericMix_false() {
        assertFalse(formatter.valueContainsOnlyNumbers("12a3"));
        assertFalse(formatter.valueContainsOnlyNumbers("a"));
    }

    @Test
    void valueContainsOnlyNumbers_emptyString_false() {
        assertFalse(formatter.valueContainsOnlyNumbers(""));
    }

    // ----- isGeneralCell ---------------------------------------------------

    @Test
    void isGeneralCell_numericGeneralFormat_true() {
        // Pin the SUT contract: NUMERIC AND format string CONTAINS
        // "General". The substring match means "General Format" or any
        // localized variant containing "General" also passes — that's
        // intentional per the source.
        when(cell.getCellType()).thenReturn(CellType.NUMERIC);
        when(style.getDataFormatString()).thenReturn("General");

        assertTrue(formatter.isGeneralCell(cell));
    }

    @Test
    void isGeneralCell_stringEvenWithGeneralFormat_false() {
        // Pin: STRING cells are never "general" — even if the format
        // string says "General". The cell-type check happens FIRST via
        // short-circuit AND. Stub the format string with "General" so a
        // regression that flipped the AND order would now fail cleanly
        // on the assertion (instead of NPE'ing on a null format string).
        when(cell.getCellType()).thenReturn(CellType.STRING);
        org.mockito.Mockito.lenient().when(style.getDataFormatString())
                .thenReturn("General");

        assertFalse(formatter.isGeneralCell(cell));
    }

    @Test
    void isGeneralCell_numericWithSpecificFormat_false() {
        when(cell.getCellType()).thenReturn(CellType.NUMERIC);
        when(style.getDataFormatString()).thenReturn("0.00");

        assertFalse(formatter.isGeneralCell(cell));
    }
}
