/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

/**
 * Cached default font / alignment baseline captured from a workbook's
 * cell-style index 0.
 *
 * <p>
 * Held read-only after capture and used as a comparison reference: when
 * composing CSS for any other cell style, only the differences from the
 * baseline are emitted. Captures from {@code defaultFontStyle(CellStyle, sb)} —
 * appends the default font's CSS rules into the supplied {@link StringBuilder}
 * as a side effect of the capture (the rules are needed by the default style's
 * CSS row, which is composed at workbook-style-reload time).
 *
 * <p>
 * The {@code defaultTextAlign} field is captured separately in
 * {@code reloadWorkbookStyles} before the font capture, because the alignment
 * isn't a font property and gets used by alignment-comparison code paths.
 */
final class DefaultStyleBaseline implements Serializable {

    private final Spreadsheet spreadsheet;

    private Font defaultFont;
    private HorizontalAlignment defaultTextAlign;
    private short defaultFontHeightInPoints;
    private String defaultFontFamily;

    DefaultStyleBaseline(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    Font getDefaultFont() {
        return defaultFont;
    }

    HorizontalAlignment getDefaultTextAlign() {
        return defaultTextAlign;
    }

    short getDefaultFontHeightInPoints() {
        return defaultFontHeightInPoints;
    }

    String getDefaultFontFamily() {
        return defaultFontFamily;
    }

    void setDefaultTextAlign(HorizontalAlignment defaultTextAlign) {
        this.defaultTextAlign = defaultTextAlign;
    }

    /**
     * If {@code cellStyle} is the workbook's default style (index 0), captures
     * its font into this baseline and appends the default font's CSS rules into
     * {@code sb}. Otherwise does nothing. Mirrors the original
     * {@code defaultFontStyle} behavior exactly — captures and emits in one
     * call.
     */
    void captureDefaultStyle(CellStyle cellStyle, StringBuilder sb) {
        if (cellStyle.getIndex() == 0) {
            defaultFont = spreadsheet.getWorkbook()
                    .getFontAt(cellStyle.getFontIndexAsInt());
            defaultFontFamily = FontStyleComposer.styleFontFamily(defaultFont);
            sb.append(defaultFontFamily);
            if (defaultFont.getBold()) {
                sb.append("font-weight:bold;");
            }
            if (defaultFont.getItalic()) {
                sb.append("font-style:italic;");
            }
            defaultFontHeightInPoints = defaultFont.getFontHeightInPoints();
            sb.append("font-size: var(--default-font-size);");
            sb.append("--default-font-size:");
            sb.append(defaultFontHeightInPoints);
            sb.append("pt;");
            sb.append(FontStyleComposer.textDecoration(
                    defaultFont.getUnderline() != Font.U_NONE,
                    defaultFont.getStrikeout()));
        }
    }
}
