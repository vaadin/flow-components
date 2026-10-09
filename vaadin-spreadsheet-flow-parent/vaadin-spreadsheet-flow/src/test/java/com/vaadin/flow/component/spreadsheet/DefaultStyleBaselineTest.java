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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Capturing the workbook's default style with {@link DefaultStyleBaseline}.
 *
 * <p>
 * Pins the dual nature of {@link DefaultStyleBaseline#captureDefaultStyle}: it
 * both <em>captures</em> the workbook's default font into the baseline fields
 * AND <em>emits</em> the CSS for that font into the passed
 * {@link StringBuilder}.
 *
 * <p>
 * Uses an in-memory {@link XSSFWorkbook} for the real POI {@link CellStyle} +
 * {@link Font} fixture. Mocks the parent {@link Spreadsheet} just enough to
 * return the workbook.
 */
class DefaultStyleBaselineTest {

    @Mock
    private Spreadsheet spreadsheet;

    private XSSFWorkbook workbook;
    private DefaultStyleBaseline baseline;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workbook = new XSSFWorkbook();
        when(spreadsheet.getWorkbook()).thenReturn(workbook);
        baseline = new DefaultStyleBaseline(spreadsheet);
    }

    @AfterEach
    void tearDown() throws Exception {
        workbook.close();
    }

    @Test
    void freshBaseline_allGettersReturnDefaults() {
        // Pin: never-captured baseline returns Java field defaults
        // (null Object, 0 short).
        assertNull(baseline.getDefaultFont());
        assertNull(baseline.getDefaultTextAlign());
        assertEquals((short) 0, baseline.getDefaultFontHeightInPoints());
        assertNull(baseline.getDefaultFontFamily());
    }

    @Test
    void setDefaultTextAlign_storesValue() {
        baseline.setDefaultTextAlign(HorizontalAlignment.CENTER);
        assertEquals(HorizontalAlignment.CENTER,
                baseline.getDefaultTextAlign());
    }

    @Test
    void captureDefaultStyle_onDefaultStyle_capturesFontAndEmitsCSS() {
        // Pin: when called with cellStyle.getIndex() == 0, capturing the
        // workbook's default font into the baseline fields AND emitting CSS
        // into the StringBuilder happen in one call. Both side effects are
        // expected.
        CellStyle defaultStyle = workbook.getCellStyleAt((short) 0);
        StringBuilder sb = new StringBuilder();

        baseline.captureDefaultStyle(defaultStyle, sb);

        // Capture side: baseline fields populated.
        assertEquals(workbook.getFontAt(defaultStyle.getFontIndexAsInt()),
                baseline.getDefaultFont());
        assertTrue(baseline.getDefaultFontHeightInPoints() > 0,
                "Default font height should be captured as positive points");
        assertTrue(
                baseline.getDefaultFontFamily() != null
                        && !baseline.getDefaultFontFamily().isEmpty(),
                "Default font family CSS should be captured");

        // Emit side: CSS appended to sb. Default fonts in POI typically
        // include font-family + font-size declarations.
        String css = sb.toString();
        assertTrue(css.contains("font-family"),
                "Emitted CSS should contain font-family declaration, got: "
                        + css);
        assertTrue(css.contains("font-size"),
                "Emitted CSS should contain font-size declaration, got: "
                        + css);
    }

    @Test
    void captureDefaultStyle_onNonDefaultStyle_isNoOp() {
        // Pin: only style index 0 triggers capture. Any other style index
        // leaves both the baseline fields and the StringBuilder untouched.
        CellStyle nonDefaultStyle = workbook.createCellStyle();
        // The newly-created style has a non-zero index.
        assertTrue(nonDefaultStyle.getIndex() != 0,
                "Test precondition: created style must have non-zero index");

        StringBuilder sb = new StringBuilder("preexisting");

        baseline.captureDefaultStyle(nonDefaultStyle, sb);

        // No capture happened.
        assertNull(baseline.getDefaultFont());
        assertNull(baseline.getDefaultFontFamily());
        assertEquals((short) 0, baseline.getDefaultFontHeightInPoints());

        // StringBuilder untouched.
        assertEquals("preexisting", sb.toString());
    }

    @Test
    void captureDefaultStyle_boldDefaultFont_emitsFontWeightBold() {
        // Pin: when the default font has bold=true, captureDefaultStyle
        // emits "font-weight:bold;" into the StringBuilder.
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        workbook.getCellStyleAt((short) 0).setFont(boldFont);

        StringBuilder sb = new StringBuilder();
        baseline.captureDefaultStyle(workbook.getCellStyleAt((short) 0), sb);

        assertTrue(sb.toString().contains("font-weight:bold;"),
                "Bold default font should emit font-weight:bold; got: "
                        + sb.toString());
    }

    @Test
    void captureDefaultStyle_italicDefaultFont_emitsFontStyleItalic() {
        Font italicFont = workbook.createFont();
        italicFont.setItalic(true);
        workbook.getCellStyleAt((short) 0).setFont(italicFont);

        StringBuilder sb = new StringBuilder();
        baseline.captureDefaultStyle(workbook.getCellStyleAt((short) 0), sb);

        assertTrue(sb.toString().contains("font-style:italic;"),
                "Italic default font should emit font-style:italic; got: "
                        + sb.toString());
    }
}
