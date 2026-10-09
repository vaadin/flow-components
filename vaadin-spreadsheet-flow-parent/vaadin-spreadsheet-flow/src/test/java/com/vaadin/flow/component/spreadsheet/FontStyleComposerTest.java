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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Characterization tests for {@link FontStyleComposer}.
 *
 * <p>
 * Two-axis pinning: (a) {@link FontStyleComposer#appendFontStyle} emits CSS
 * deltas vs the {@link DefaultStyleBaseline} (same-font case is a no-op,
 * different bold/italic/size emit explicit rules); (b) the static
 * {@link FontStyleComposer#styleFontFamily} produces a uniform CSS family
 * declaration usable from both {@link FontStyleComposer} and
 * {@link DefaultStyleBaseline} (the baseline calls it during capture).
 */
class FontStyleComposerTest {

    @Mock
    private Spreadsheet spreadsheet;

    private XSSFWorkbook workbook;
    private DefaultStyleBaseline baseline;
    private FontStyleComposer composer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workbook = new XSSFWorkbook();
        when(spreadsheet.getWorkbook()).thenReturn(workbook);
        baseline = new DefaultStyleBaseline(spreadsheet);
        composer = new FontStyleComposer(spreadsheet, baseline);

        // Prime the baseline with workbook style 0's font.
        baseline.captureDefaultStyle(workbook.getCellStyleAt((short) 0),
                new StringBuilder());
    }

    @AfterEach
    void tearDown() throws Exception {
        workbook.close();
    }

    @Test
    void appendFontStyle_sameAsBaseline_emitsNothing() {
        // Pin: when the cell's font index equals the baseline's, the early
        // return skips all emission. The StringBuilder stays empty.
        StringBuilder sb = new StringBuilder();
        composer.appendFontStyle(sb, workbook.getCellStyleAt((short) 0));
        assertEquals("", sb.toString());
    }

    @Test
    void appendFontStyle_boldFont_emitsFontWeightBold() {
        Font bold = workbook.createFont();
        bold.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(bold);

        StringBuilder sb = new StringBuilder();
        composer.appendFontStyle(sb, style);

        assertTrue(sb.toString().contains("font-weight:bold;"),
                "Bold font should emit font-weight:bold; got: " + sb);
    }

    @Test
    void appendFontStyle_italicFont_emitsFontStyleItalic() {
        Font italic = workbook.createFont();
        italic.setItalic(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(italic);

        StringBuilder sb = new StringBuilder();
        composer.appendFontStyle(sb, style);

        assertTrue(sb.toString().contains("font-style:italic;"),
                "Italic font should emit font-style:italic; got: " + sb);
    }

    @Test
    void appendFontStyle_differentSize_emitsFontSizePt() {
        Font large = workbook.createFont();
        large.setFontHeightInPoints((short) 24);
        CellStyle style = workbook.createCellStyle();
        style.setFont(large);

        StringBuilder sb = new StringBuilder();
        composer.appendFontStyle(sb, style);

        assertTrue(sb.toString().contains("font-size:24pt;"),
                "Different font height should emit font-size:24pt; got: " + sb);
    }

    @Test
    void appendFontStyle_underline_emitsTextDecorationUnderline() {
        Font underlined = workbook.createFont();
        underlined.setUnderline(Font.U_SINGLE);
        CellStyle style = workbook.createCellStyle();
        style.setFont(underlined);

        StringBuilder sb = new StringBuilder();
        composer.appendFontStyle(sb, style);

        assertTrue(sb.toString().contains("text-decoration:underline;"),
                "Underlined font should emit text-decoration:underline; got: "
                        + sb);
    }

    @Test
    void appendFontStyle_strikeout_emitsTextDecorationOverline() {
        // Pin: the helper treats strikethrough as "overline" (a quirk of the
        // original CSS scheme — pin it, don't fix it). Underline takes
        // precedence if both are set.
        Font strike = workbook.createFont();
        strike.setStrikeout(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(strike);

        StringBuilder sb = new StringBuilder();
        composer.appendFontStyle(sb, style);

        assertTrue(sb.toString().contains("text-decoration:overline;"),
                "Strikeout font should emit text-decoration:overline; got: "
                        + sb);
    }

    @Test
    void appendFontStyle_underlineAndStrikeout_underlineWins() {
        // Pin precedence: underline check happens first, strikeout is
        // mutually exclusive in the emit path.
        Font font = workbook.createFont();
        font.setUnderline(Font.U_SINGLE);
        font.setStrikeout(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);

        StringBuilder sb = new StringBuilder();
        composer.appendFontStyle(sb, style);

        assertTrue(sb.toString().contains("text-decoration:underline;"),
                "Underline should win over strikeout; got: " + sb);
        assertFalse(sb.toString().contains("text-decoration:overline;"),
                "Strikeout overline should not appear when underline is set; got: "
                        + sb);
    }

    @Test
    void styleFontFamily_emitsCSSVariableAndFontName() {
        // Pin: the static helper produces a deterministic shape:
        // "font-family:
        // var(--default-font-family);--default-font-family:<name>,...Helvetica,arial;"
        Font font = workbook.createFont();
        font.setFontName("Verdana");

        String css = FontStyleComposer.styleFontFamily(font);

        assertTrue(css.startsWith("font-family: var(--default-font-family);"),
                "Should start with the CSS variable declaration; got: " + css);
        assertTrue(css.contains("Verdana"),
                "Should include the font name; got: " + css);
        assertTrue(css.endsWith("Helvetica,arial;"),
                "Should end with the fallback stack; got: " + css);
    }

    @Test
    void styleFontFamily_quotesNamesWithSpaces() {
        // Pin: font names containing a space get wrapped in double quotes —
        // CSS would otherwise treat the space as a delimiter.
        Font font = workbook.createFont();
        font.setFontName("Comic Sans MS");

        String css = FontStyleComposer.styleFontFamily(font);

        assertTrue(css.contains("\"Comic Sans MS\","),
                "Multi-word font names should be quoted; got: " + css);
    }

    @Test
    void styleFontFamily_keepsMarkupInANameText() {
        // A workbook names its own fonts, and the client puts the rule in a
        // style block, so markup in a name has to come out as an escaped
        // string.
        Font font = workbook.createFont();
        font.setFontName("X</style><img src=x onerror=\"alert(1)\">");

        String css = FontStyleComposer.styleFontFamily(font);

        assertTrue(css.contains(
                "--default-font-family:\"X\\3c /style\\3e \\3c img src=x onerror=\\22 alert(1)\\22 \\3e \","),
                "The name should be a quoted, escaped string; got: " + css);
        assertFalse(css.contains("<") || css.contains(">"),
                "No markup should be left; got: " + css);
    }

    @Test
    void styleFontFamily_quotesANameThatWouldEndTheRule() {
        // Unquoted, this name ended the declaration and the rule and added a
        // rule of its own; quoted, it is just a name.
        Font font = workbook.createFont();
        font.setFontName("X;}.cell{color:red}");

        String css = FontStyleComposer.styleFontFamily(font);

        assertTrue(
                css.contains("--default-font-family:\"X;}.cell{color:red}\","),
                "The name should be quoted; got: " + css);
    }

    @Test
    void styleFontFamily_keepsAPlainNameInAnyScriptAsItIs() {
        Font font = workbook.createFont();
        font.setFontName("游ゴシック");

        String css = FontStyleComposer.styleFontFamily(font);

        assertTrue(css.contains("--default-font-family:游ゴシック,"),
                "A plain name should not be quoted; got: " + css);
    }

    @Test
    void styleFontFamily_xssfRoman_emitsRomanBeforeFallbackStack() {
        // Pin the FULL CSS shape for ROMAN — including ordering. A
        // regression that emitted "roman" AFTER the fallback stack
        // ("Helvetica,arial,roman;") would still pass a loose contains()
        // assertion. Assert on an exact expected string.
        assertEquals(
                "font-family: var(--default-font-family);"
                        + "--default-font-family:Arial,roman,Helvetica,arial;",
                familyCssWith("Arial", FontFamily.ROMAN));
    }

    @Test
    void styleFontFamily_xssfSwiss_emitsSwissBeforeFallbackStack() {
        assertEquals(
                "font-family: var(--default-font-family);"
                        + "--default-font-family:Arial,swiss,Helvetica,arial;",
                familyCssWith("Arial", FontFamily.SWISS));
    }

    @Test
    void styleFontFamily_xssfModern_emitsModernBeforeFallbackStack() {
        assertEquals(
                "font-family: var(--default-font-family);"
                        + "--default-font-family:Arial,modern,Helvetica,arial;",
                familyCssWith("Arial", FontFamily.MODERN));
    }

    @Test
    void styleFontFamily_xssfScript_emitsScriptBeforeFallbackStack() {
        assertEquals(
                "font-family: var(--default-font-family);"
                        + "--default-font-family:Arial,script,Helvetica,arial;",
                familyCssWith("Arial", FontFamily.SCRIPT));
    }

    @Test
    void styleFontFamily_xssfDecorative_emitsDecorativeBeforeFallbackStack() {
        assertEquals("font-family: var(--default-font-family);"
                + "--default-font-family:Arial,decorative,Helvetica,arial;",
                familyCssWith("Arial", FontFamily.DECORATIVE));
    }

    @Test
    void styleFontFamily_xssfNotApplicable_omitsFamilyKeyword() {
        // Pin: NOT_APPLICABLE is the empty case — only the font name and
        // the Helvetica/arial fallback are emitted, no generic family
        // keyword between them.
        assertEquals(
                "font-family: var(--default-font-family);"
                        + "--default-font-family:Arial,Helvetica,arial;",
                familyCssWith("Arial", FontFamily.NOT_APPLICABLE));
    }

    /**
     * Build an XSSF font with the given name and family and return the CSS
     * styleFontFamily emits for it. Keeps the per-FontFamily tests above tight
     * and identically shaped so a future contributor reading the file can spot
     * the per-case constant at a glance.
     */
    private String familyCssWith(String name, FontFamily family) {
        XSSFFont font = workbook.createFont();
        font.setFontName(name);
        font.setFamily(family);
        return FontStyleComposer.styleFontFamily(font);
    }
}
