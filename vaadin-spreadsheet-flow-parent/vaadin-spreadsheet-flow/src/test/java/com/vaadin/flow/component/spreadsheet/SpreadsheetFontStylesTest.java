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

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.tests.CapturedLogs;
import com.vaadin.flow.component.spreadsheet.tests.ExpectWarnings;

/**
 * The fonts, the alignment, the wrapping and the indentation of the cell
 * styles, as CSS for the client. The default style, style 0, sets the font of
 * the sheet; every other style sets only what differs from it.
 */
class SpreadsheetFontStylesTest {

    /** What every style sets: Excel's vertical alignment, at the bottom. */
    private static final String BOTTOM = "justify-content:flex-end;";

    private XSSFWorkbook workbook;

    @BeforeEach
    void workbook() {
        workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(0).createCell(0);
    }

    /** The CSS of the style, as a spreadsheet on the workbook gives it. */
    private String css(CellStyle style) {
        return new Spreadsheet(workbook).getCellStyleToCSSStyle()
                .get((int) style.getIndex());
    }

    private String defaultCss() {
        return css(workbook.getCellStyleAt(0));
    }

    /**
     * A style of a new font like the default one, Calibri of the Swiss family
     * at 11 points, set up as given.
     */
    private XSSFCellStyle styleOfFont(Consumer<XSSFFont> setUp) {
        XSSFFont font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFamily(FontFamily.SWISS);
        font.setFontHeightInPoints((short) 11);
        setUp.accept(font);
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private void defaultFont(Consumer<XSSFFont> setUp) {
        setUp.accept(workbook.getFontAt(0));
    }

    /**
     * A font whose {@code <name/>} has no value, which POI reports as a
     * {@code null} name.
     */
    private static XSSFFont withoutName(XSSFFont font) {
        font.getCTFont().removeName(0);
        font.getCTFont().addNewName();
        return font;
    }

    @Test
    void theDefaultStyle_setsTheFontOfTheSheet() {
        // POI's default font: Calibri, of the Swiss family, 11 points.
        String css = defaultCss();

        assertTrue(css.startsWith("font-family: var(--default-font-family);"
                + "--default-font-family:Calibri,swiss,Helvetica,arial;"
                + "font-size: var(--default-font-size);"
                + "--default-font-size:11pt;"), css);
        assertFalse(css.contains("font-weight"), css);
        assertFalse(css.contains("font-style"), css);
        assertFalse(css.contains("text-decoration"), css);
    }

    @Test
    void theDefaultStyleOfABoldItalicUnderlinedFont_setsThemAll() {
        defaultFont(font -> {
            font.setBold(true);
            font.setItalic(true);
            font.setUnderline(Font.U_SINGLE);
            font.setStrikeout(true);
        });

        String css = defaultCss();

        assertTrue(css.contains("font-weight:bold;"), css);
        assertTrue(css.contains("font-style:italic;"), css);
        assertTrue(css.contains("text-decoration:underline line-through;"),
                css);
    }

    @Test
    void theDefaultStyleOfAFontWithoutName_setsNoFamily() {
        withoutName(workbook.getFontAt(0));

        assertFalse(defaultCss().contains("font-family"), defaultCss());
    }

    @Test
    void aStyleOfTheDefaultFont_setsNoFont() {
        String css = css(workbook.createCellStyle());

        assertFalse(css.contains("font"), css);
    }

    @Test
    void aStyleOfABoldDefaultFont_setsNoFont() {
        defaultFont(font -> font.setBold(true));

        String css = css(workbook.createCellStyle());

        assertFalse(css.contains("font"), css);
    }

    @Test
    void aStyleOfAFontLikeTheDefaultOne_setsNoFont() {
        assertEquals(BOTTOM, css(styleOfFont(font -> {
        })));
    }

    @Test
    void aStyleOfABoldFont_setsBoldOnly() {
        assertEquals("font-weight:bold;" + BOTTOM,
                css(styleOfFont(font -> font.setBold(true))));
    }

    @Test
    void aStyleOfAnItalicFont_setsItalicOnly() {
        assertEquals("font-style:italic;" + BOTTOM,
                css(styleOfFont(font -> font.setItalic(true))));
    }

    @Test
    void aStyleOfAFontOfAnotherSize_setsTheSize() {
        assertEquals("font-size:24pt;" + BOTTOM, css(
                styleOfFont(font -> font.setFontHeightInPoints((short) 24))));
    }

    @Test
    void aStyleOfAnotherFontFamily_setsTheFamily() {
        assertEquals(
                "font-family: var(--default-font-family);"
                        + "--default-font-family:Verdana,swiss,Helvetica,arial;"
                        + BOTTOM,
                css(styleOfFont(font -> font.setFontName("Verdana"))));
    }

    @Test
    void aStyleOfAFontWithoutName_keepsTheFamilyOfTheDefaultStyle() {
        XSSFCellStyle style = styleOfFont(font -> {
            withoutName(font);
            font.setBold(true);
        });

        assertEquals("font-weight:bold;" + BOTTOM, css(style));
    }

    @Test
    void aStyleOfAFontOfABlankName_keepsTheFamilyOfTheDefaultStyle() {
        XSSFCellStyle style = styleOfFont(font -> {
            font.setFontName(" ");
            font.setBold(true);
        });

        assertEquals("font-weight:bold;" + BOTTOM, css(style));
    }

    @Test
    void aStyleOfAnUnderlinedOrStruckFont_setsOneDeclarationForBoth() {
        // One declaration also when both are set: a second one would replace
        // the first.
        XSSFCellStyle underlined = styleOfFont(
                font -> font.setUnderline(Font.U_SINGLE));
        XSSFCellStyle struck = styleOfFont(font -> font.setStrikeout(true));
        XSSFCellStyle both = styleOfFont(font -> {
            font.setUnderline(Font.U_SINGLE);
            font.setStrikeout(true);
        });

        assertEquals("text-decoration:underline;" + BOTTOM, css(underlined));
        assertEquals("text-decoration:line-through;" + BOTTOM, css(struck));
        assertEquals("text-decoration:underline line-through;" + BOTTOM,
                css(both));
    }

    @Test
    void theFamilyOfAFont_comesBeforeTheFallbackFonts() {
        List<FontFamily> families = List.of(FontFamily.ROMAN, FontFamily.SWISS,
                FontFamily.MODERN, FontFamily.SCRIPT, FontFamily.DECORATIVE,
                FontFamily.NOT_APPLICABLE);
        List<String> keywords = List.of("roman,", "swiss,", "modern,",
                "script,", "decorative,", "");
        for (int i = 0; i < families.size(); i++) {
            FontFamily family = families.get(i);
            XSSFCellStyle style = styleOfFont(font -> {
                font.setFontName("Arial");
                font.setFamily(family);
            });

            assertEquals(
                    "font-family: var(--default-font-family);"
                            + "--default-font-family:Arial," + keywords.get(i)
                            + "Helvetica,arial;" + BOTTOM,
                    css(style), family.toString());
        }
    }

    /** The font name as the CSS of a style of a font of that name has it. */
    private String nameInCss(String name) {
        String css = css(styleOfFont(font -> font.setFontName(name)));
        String family = "--default-font-family:";
        return css.substring(css.indexOf(family) + family.length(),
                css.indexOf(",swiss,"));
    }

    @Test
    void aFontNameWithASpace_isQuoted() {
        assertEquals("\"Comic Sans MS\"", nameInCss("Comic Sans MS"));
    }

    @Test
    void aPlainFontNameInAnyScript_isAsItIs() {
        assertEquals("游ゴシック", nameInCss("游ゴシック"));
    }

    @Test
    void aFontNameWithMarkup_isAnEscapedString() {
        // A workbook names its own fonts, and the client puts the rule in a
        // style block.
        assertEquals(
                "\"X\\3c /style\\3e \\3c img src=x onerror=\\22 alert(1)\\22 \\3e \"",
                nameInCss("X</style><img src=x onerror=\"alert(1)\">"));
    }

    @Test
    void aFontNameWithABackslashOrControlCharacters_isAnEscapedString() {
        assertEquals("\"A\\5c B\\1 C\\7f D\"", nameInCss("A\\B\u0001C\u007fD"));
    }

    @Test
    void aFontNameThatWouldEndTheRule_isQuoted() {
        assertEquals("\"X;}.cell{color:red}\"",
                nameInCss("X;}.cell{color:red}"));
    }

    @Test
    @ExpectWarnings
    void aStyleOfAFontTheWorkbookHasNot_isShownInTheDefaultFont_withAWarning() {
        // A file can give a style a font it has not. Loading it threw.
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.getCoreXf().setFontId(99);
        workbook.getSheetAt(0).getRow(0).getCell(0).setCellStyle(style);

        String css;
        List<CapturedLogs.Entry> logged;
        try (CapturedLogs.Capture logs = CapturedLogs.capture()) {
            css = css(style);
            logged = logs.entries();
        }

        assertFalse(css.contains("font"), css);
        assertFalse(css.replace("background-color:", "").contains("color:"),
                css);
        assertTrue(css.contains("background-color:"), css);
        assertEquals(List.of("Cell style " + style.getIndex()
                + " has font 99, which the workbook has not: its cells are shown in the font of the default style"),
                logged.stream().map(CapturedLogs.Entry::message).toList());
    }

    @Test
    @ExpectWarnings
    void anXlsStyleOfAFontTheWorkbookHasNot_isShownInTheDefaultFont()
            throws ReflectiveOperationException {
        HSSFWorkbook xls = new HSSFWorkbook();
        xls.createSheet().createRow(0).createCell(0);
        HSSFCellStyle style = xls.createCellStyle();
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // POI gives a style only fonts the workbook has.
        Field format = HSSFCellStyle.class.getDeclaredField("_format");
        format.setAccessible(true);
        ((ExtendedFormatRecord) format.get(style)).setFontIndex((short) 99);
        xls.getSheetAt(0).getRow(0).getCell(0).setCellStyle(style);

        String css;
        try (CapturedLogs.Capture logs = CapturedLogs.capture()) {
            css = new Spreadsheet(xls).getCellStyleToCSSStyle()
                    .get((int) style.getIndex());
            assertEquals(1, logs.entries().size(), logs.entries().toString());
        }

        assertFalse(css.contains("font"), css);
        assertFalse(css.replace("background-color:", "").contains("color:"),
                css);
        assertTrue(css.contains("background-color:"), css);
    }

    @Test
    void aStyleAlignedAsTheDefaultStyle_setsNoAlignment() {
        workbook.getCellStyleAt(0).setAlignment(HorizontalAlignment.CENTER);
        XSSFCellStyle centered = workbook.createCellStyle();
        centered.setAlignment(HorizontalAlignment.CENTER);
        XSSFCellStyle left = workbook.createCellStyle();
        left.setAlignment(HorizontalAlignment.LEFT);

        assertFalse(css(centered).contains("text-align"), css(centered));
        assertTrue(css(left).contains("text-align:left;"), css(left));
    }

    @Test
    void aStyleAlignedOtherwiseThanTheDefaultStyle_setsItsAlignment() {
        XSSFCellStyle centered = workbook.createCellStyle();
        centered.setAlignment(HorizontalAlignment.CENTER);

        assertTrue(css(centered).contains("text-align:center;"), css(centered));
    }

    @Test
    void aStyleWrappingItsText_setsItToWrap() {
        XSSFCellStyle wrapped = workbook.createCellStyle();
        wrapped.setWrapText(true);

        assertTrue(css(wrapped).contains(
                "overflow:hidden;white-space:normal;word-wrap:break-word;"),
                css(wrapped));
        assertFalse(css(workbook.createCellStyle()).contains("white-space"));
    }

    @Test
    void anIndentedStyle_setsItsIndentation() {
        XSSFCellStyle indented = workbook.createCellStyle();
        indented.setIndention((short) 2);

        assertTrue(css(indented).contains("padding-left: 2em;"), css(indented));
    }

    @Test
    void theStyleOfALink_hasTheDefaultFontUnderlinedInBlue() {
        defaultFont(font -> {
            font.setFontName("Verdana");
            font.setFontHeightInPoints((short) 14);
        });
        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        CellStyle link = spreadsheet.getSpreadsheetStyleFactory()
                .createHyperlinkCellStyle();

        Font font = workbook.getFontAt(link.getFontIndex());
        assertEquals("Verdana", font.getFontName());
        assertEquals(14, font.getFontHeightInPoints());
        assertEquals(Font.U_SINGLE, font.getUnderline());
        assertEquals(IndexedColors.BLUE.getIndex(), font.getColor());
    }
}
