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
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composes the font-related portion of a cell's CSS, emitting only the
 * properties that differ from the {@link DefaultStyleBaseline}.
 *
 * <p>
 * Holds no per-cell state — it's an instance only because the comparison
 * baseline travels with the parent factory. {@link #styleFontFamily(Font)} is
 * intentionally {@code static}: it has no field touches and
 * {@link DefaultStyleBaseline} also calls it during baseline capture (before
 * any composer instance exists for the workbook).
 */
final class FontStyleComposer implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(FontStyleComposer.class);

    private final Spreadsheet spreadsheet;
    private final DefaultStyleBaseline baseline;

    FontStyleComposer(Spreadsheet spreadsheet, DefaultStyleBaseline baseline) {
        this.spreadsheet = spreadsheet;
        this.baseline = baseline;
    }

    /**
     * Appends font-related CSS for {@code cellStyle} to {@code sb}, skipping
     * any property that matches the workbook default. Catches and warn-logs an
     * {@link IndexOutOfBoundsException} from POI when the workbook's font table
     * is missing an index the cell points to.
     */
    void appendFontStyle(StringBuilder sb, CellStyle cellStyle) {
        try {
            Font font = spreadsheet.getWorkbook()
                    .getFontAt(cellStyle.getFontIndexAsInt());
            Font defaultFont = baseline.getDefaultFont();
            if (font.getIndexAsInt() == defaultFont.getIndexAsInt()) {
                // uses default font, no need to add styles
                return;
            }
            String fontFamily = styleFontFamily(font);
            if (!fontFamily.equals(baseline.getDefaultFontFamily())) {
                sb.append(fontFamily);
            }
            if (font.getBold()) {
                sb.append("font-weight:bold;");
            }
            if (font.getItalic()) {
                sb.append("font-style:italic;");
            }
            final int fontheight = font.getFontHeightInPoints();
            if (fontheight != baseline.getDefaultFontHeightInPoints()) {
                sb.append("font-size:");
                sb.append(fontheight);
                sb.append("pt;");
            }
            sb.append(textDecoration(font.getUnderline() != Font.U_NONE,
                    font.getStrikeout()));
        } catch (IndexOutOfBoundsException ioobe) {
            // somehow workbook doesn't have all the fonts the cells have???
            LOGGER.warn(
                    "Font missing, " + cellStyle.getFontIndexAsInt() + " / "
                            + cellStyle.getClass() + ", " + ioobe.getMessage(),
                    ioobe);
        }
    }

    /**
     * CSS rule for a font's family. Static because it has no field touches and
     * is also called from {@link DefaultStyleBaseline#captureDefaultStyle}
     * before any composer instance exists.
     * <p>
     * A font with no name, which POI reports for a {@code <name/>} without a
     * value, sets no family at all: its cells keep the family they would have
     * without it, the workbook default's or the theme's.
     */
    static String styleFontFamily(Font font) {
        String name = font.getFontName();
        if (name == null || name.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("font-family: var(--default-font-family);");
        sb.append("--default-font-family:");
        sb.append(cssFontName(name));
        sb.append(",");
        if (font instanceof XSSFFont xssfFont) {
            FontFamily family = FontFamily.valueOf(xssfFont.getFamily());
            switch (family) {
            case ROMAN:
                sb.append("roman,");
                break;
            case SWISS:
                sb.append("swiss,");
                break;
            case MODERN:
                sb.append("modern,");
                break;
            case SCRIPT:
                sb.append("script,");
                break;
            case DECORATIVE:
                sb.append("decorative,");
                break;
            case NOT_APPLICABLE:
                break;
            default:
                break;
            }
        }
        sb.append("Helvetica,arial;");
        return sb.toString();
    }

    /**
     * A plain font name: letters and digits of any script, spaces, hyphens and
     * underscores.
     */
    private static final Pattern PLAIN_FONT_NAME = Pattern
            .compile("[\\p{L}\\p{N} _-]+");

    /**
     * A font's name as CSS. A workbook names its own fonts, and the client puts
     * the rule the name is written into in a style block. A plain name goes in
     * as it is, quoted when it has a space. Any other is quoted, with what
     * could end the string or the style block escaped, so that it cannot add
     * rules or markup of its own.
     */
    static String cssFontName(String name) {
        if (PLAIN_FONT_NAME.matcher(name).matches()) {
            return name.contains(" ") ? "\"" + name + "\"" : name;
        }
        StringBuilder quoted = new StringBuilder("\"");
        name.codePoints().forEach(c -> {
            if (c < 0x20 || c == 0x7f || c == '"' || c == '\\' || c == '<'
                    || c == '>') {
                quoted.append('\\').append(Integer.toHexString(c)).append(' ');
            } else {
                quoted.appendCodePoint(c);
            }
        });
        return quoted.append('"').toString();
    }

    /**
     * The {@code text-decoration} declaration for an underline, a
     * strikethrough, both or neither (then empty). Both together are one
     * declaration: a second one would replace the first.
     */
    static String textDecoration(boolean underline, boolean strikeout) {
        if (underline && strikeout) {
            return "text-decoration:underline line-through;";
        }
        if (underline) {
            return "text-decoration:underline;";
        }
        return strikeout ? "text-decoration:line-through;" : "";
    }
}
