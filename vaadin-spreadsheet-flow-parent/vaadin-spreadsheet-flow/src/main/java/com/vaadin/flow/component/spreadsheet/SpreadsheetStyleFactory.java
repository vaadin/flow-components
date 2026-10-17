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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.shared.internal.BeforeClientResponseAction;

/**
 * SpreadsheetStyleFactory is an utility class for the Spreadsheet component.
 * This class handles converting Apache POI CellStyles to CSS styles.
 *
 * @author Vaadin Ltd.
 * @since 23.1
 */
public class SpreadsheetStyleFactory implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SpreadsheetStyleFactory.class);

    /**
     * Styling for cell borders
     *
     * @author Vaadin Ltd.
     */
    public enum BorderStyle {
        SOLID_THIN("solid", 1, 1),
        DOTTED_THIN("dotted", 1, 1),
        DASHED_THIN("dashed", 1, 1),
        SOLID_MEDIUM("solid", 2, 2),
        DASHED_MEDIUM("dashed", 2, 2),
        SOLID_THICK("solid", 3, 4),
        DOUBLE("double", 3, 4),
        NONE("none", 0, 0);

        private final int size;
        private final String borderStyle;

        BorderStyle(String borderStyle, int size, int adjustment) {
            this.borderStyle = borderStyle;
            this.size = size;
        }

        /**
         * Returns the CSS name of this border style
         *
         * @return CSS name of border style
         */
        public String getValue() {
            return borderStyle;
        }

        /**
         * Returns the thickness of this border
         *
         * @return Border thickness in PT
         */
        public int getSize() {
            return size;
        }

        /**
         * Returns the complete border attribute value for CSS
         *
         * @return Complete border attribute value
         */
        public String getBorderAttributeValue() {
            return borderStyle + " " + size + "pt;";
        }

    }

    private static final Map<HorizontalAlignment, String> ALIGN = mapFor(
            HorizontalAlignment.LEFT, "left", HorizontalAlignment.CENTER,
            "center", HorizontalAlignment.RIGHT, "right",
            HorizontalAlignment.FILL, "left", HorizontalAlignment.JUSTIFY,
            "left", HorizontalAlignment.CENTER_SELECTION, "center");

    private static final Map<VerticalAlignment, String> VERTICAL_ALIGN = mapFor(
            VerticalAlignment.BOTTOM, "flex-end", VerticalAlignment.CENTER,
            "center", VerticalAlignment.TOP, "flex-start");

    static final Map<org.apache.poi.ss.usermodel.BorderStyle, BorderStyle> BORDER = mapFor(
            org.apache.poi.ss.usermodel.BorderStyle.DASH_DOT,
            BorderStyle.DASHED_THIN,
            org.apache.poi.ss.usermodel.BorderStyle.DASH_DOT_DOT,
            BorderStyle.DASHED_THIN,
            org.apache.poi.ss.usermodel.BorderStyle.DASHED,
            BorderStyle.DASHED_THIN,
            org.apache.poi.ss.usermodel.BorderStyle.DOTTED,
            BorderStyle.DOTTED_THIN,
            org.apache.poi.ss.usermodel.BorderStyle.DOUBLE, BorderStyle.DOUBLE,
            org.apache.poi.ss.usermodel.BorderStyle.HAIR,
            BorderStyle.SOLID_THIN,
            org.apache.poi.ss.usermodel.BorderStyle.MEDIUM,
            BorderStyle.SOLID_MEDIUM,
            org.apache.poi.ss.usermodel.BorderStyle.MEDIUM_DASH_DOT,
            BorderStyle.DASHED_MEDIUM,
            org.apache.poi.ss.usermodel.BorderStyle.MEDIUM_DASH_DOT_DOT,
            BorderStyle.DASHED_MEDIUM,
            org.apache.poi.ss.usermodel.BorderStyle.MEDIUM_DASHED,
            BorderStyle.DASHED_MEDIUM,
            org.apache.poi.ss.usermodel.BorderStyle.NONE, BorderStyle.NONE,
            null, BorderStyle.NONE,
            org.apache.poi.ss.usermodel.BorderStyle.SLANTED_DASH_DOT,
            BorderStyle.DASHED_MEDIUM,
            org.apache.poi.ss.usermodel.BorderStyle.THICK,
            BorderStyle.SOLID_THICK,
            org.apache.poi.ss.usermodel.BorderStyle.THIN,
            BorderStyle.SOLID_THIN);

    private ColorConverter colorConverter;

    private Spreadsheet spreadsheet;

    private final CellBorders borders;

    /**
     * The font and the alignment of the default style, style 0: the other
     * styles set only what differs from them.
     */
    private Font defaultFont;
    private String defaultFontFamily;
    private short defaultFontHeightInPoints;
    private HorizontalAlignment defaultTextAlign;

    /** Gives the client the borders drawn on other cells, once a response. */
    private final BeforeClientResponseAction sendBordersAction;

    /** Whether the styles of the workbook have been loaded once. */
    private boolean stylesLoaded;

    /**
     * Constructs a new SpreadsheetStyleFactory for the given Spreadsheet
     *
     * @param spreadsheet
     *            Target Spreadsheet
     */
    public SpreadsheetStyleFactory(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
        setupColorMap();
        borders = new CellBorders(spreadsheet, colorConverter);
        sendBordersAction = new BeforeClientResponseAction(spreadsheet,
                this::sendBorders);
    }

    /**
     * Reloads all sheet and cell styles from the current Workbook.
     */
    public void reloadWorkbookStyles() {
        final Workbook workbook = spreadsheet.getWorkbook();
        spreadsheet.cellStyleToCSSStyle = spreadsheet.getCellStyleToCSSStyle();
        if (spreadsheet.cellStyleToCSSStyle == null) {
            spreadsheet.cellStyleToCSSStyle = new HashMap<Integer, String>(
                    workbook.getNumCellStyles());
        } else {
            spreadsheet.cellStyleToCSSStyle.clear();
        }
        Map<Integer, Integer> bordersBefore = borders.sidesOfStyles();
        borders.resetStyles();

        // create default style (cell style 0)
        CellStyle cellStyle = workbook.getCellStyleAt(0);
        defaultTextAlign = cellStyle.getAlignment();
        StringBuilder sb = new StringBuilder();
        borders.appendBorderStyles(sb, cellStyle);
        defaultFontStyle(cellStyle, sb);
        colorConverter.defaultColorStyles(cellStyle, sb);
        spreadsheet.cellStyleToCSSStyle.put((int) cellStyle.getIndex(),
                sb.toString());

        // 0 is default style, create all styles indexed from 1 and upwards
        for (int i = 1; i < workbook.getNumCellStyles(); i++) {
            cellStyle = workbook.getCellStyleAt(i);
            addCellStyleCSS(cellStyle);
        }
        spreadsheet.setCellStyleToCSSStyle(spreadsheet.cellStyleToCSSStyle);
        reloadActiveSheetColumnRowStyles();
        // Styles changed in place change what their cells draw on other
        // cells. Only then, as finding that goes through every cell, and not
        // the first time: the sheet finds it when it is shown.
        if (stylesLoaded && !bordersBefore.equals(borders.sidesOfStyles())) {
            borders.load();
        }
        stylesLoaded = true;
        sendBordersAction.schedule();
    }

    public void reloadActiveSheetColumnRowStyles() {
        final Workbook workbook = spreadsheet.getWorkbook();

        HashMap<Integer, Integer> _rowIndexToStyleIndex = spreadsheet
                .getRowIndexToStyleIndex();
        if (_rowIndexToStyleIndex == null) {
            _rowIndexToStyleIndex = new HashMap<Integer, Integer>(
                    workbook.getNumCellStyles());
        } else {
            _rowIndexToStyleIndex.clear();
        }
        HashMap<Integer, Integer> _columnIndexToStyleIndex = spreadsheet
                .getColumnIndexToStyleIndex();
        if (_columnIndexToStyleIndex == null) {
            _columnIndexToStyleIndex = new HashMap<Integer, Integer>(
                    workbook.getNumCellStyles());
        } else {
            _columnIndexToStyleIndex.clear();
        }
        Set<Integer> _lockedColumnIndexes = spreadsheet
                .getLockedColumnIndexes();
        if (_lockedColumnIndexes == null) {
            _lockedColumnIndexes = new HashSet<Integer>();
        } else {
            _lockedColumnIndexes.clear();
        }
        Set<Integer> _lockedRowIndexes = spreadsheet.getLockedRowIndexes();
        if (_lockedRowIndexes == null) {
            _lockedRowIndexes = new HashSet<Integer>();
        } else {
            _lockedRowIndexes.clear();
        }

        Sheet activeSheet = spreadsheet.getActiveSheet();
        for (int i = 0; i < spreadsheet.getRows(); i++) {
            Row row = activeSheet.getRow(i);
            if (row != null && row.getRowStyle() != null) {
                int styleIndex = row.getRowStyle().getIndex();
                _rowIndexToStyleIndex.put(i + 1, styleIndex);
                if (row.getRowStyle().getLocked()) {
                    _lockedRowIndexes.add(i + 1);
                }
            } else {
                if (spreadsheet.isActiveSheetProtected()) {
                    _lockedRowIndexes.add(i + 1);
                }
            }
        }

        for (int i = 0; i < spreadsheet.getColumns(); i++) {
            if (activeSheet.getColumnStyle(i) != null) {
                int styleIndex = activeSheet.getColumnStyle(i).getIndex();
                _columnIndexToStyleIndex.put(i + 1, styleIndex);
                if (activeSheet.getColumnStyle(i).getLocked()) {
                    _lockedColumnIndexes.add(i + 1);
                }
            }
        }

        spreadsheet.setLockedRowIndexes(_lockedRowIndexes);
        spreadsheet.setLockedColumnIndexes(_lockedColumnIndexes);
        spreadsheet.setColumnIndexToStyleIndex(_columnIndexToStyleIndex);
        spreadsheet.setRowIndexToStyleIndex(_rowIndexToStyleIndex);
    }

    /**
     * Creates a CellStyle to be used with hyperlinks
     *
     * @return A new hyperlink CellStyle
     */
    public CellStyle createHyperlinkCellStyle() {
        Workbook wb = spreadsheet.getWorkbook();
        CellStyle hlink_style = wb.createCellStyle();
        Font hlink_font = wb.createFont();
        hlink_font.setFontName(defaultFont.getFontName());
        hlink_font.setFontHeightInPoints(defaultFontHeightInPoints);
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        hlink_style.setFont(hlink_font);
        return hlink_style;
    }

    /**
     * Clears all styles for the given cell. Should be used when i.e. a cell has
     * been shifted (the old location is cleared of all styles).
     * <p>
     * The borders the cell drew on other cells are taken off. Those other cells
     * draw on it stay, as they are drawn whether it is there or not.
     *
     * @param oldRowIndex
     *            0-based
     * @param oldColumnIndex
     *            0-based
     */
    public void clearCellStyle(int oldRowIndex, int oldColumnIndex) {
        borders.cellRemoved(oldRowIndex, oldColumnIndex);
        sendBordersAction.schedule();
    }

    /**
     * This should be called when a Cell's styling has been changed. This will
     * tell the Spreadsheet to send the change to the client side.
     *
     * @param cell
     *            Target cell
     * @param updateCustomBorders
     *            not used: the borders the cell draws on other cells reach the
     *            client before the next response either way
     */
    public void cellStyleUpdated(Cell cell, boolean updateCustomBorders) {
        CellStyle cellStyle = cell.getCellStyle();
        int sides = borders.sides(cellStyle.getIndex());
        // Most cells are given a style the client has already: only a style
        // whose CSS is new or has changed goes out again.
        if (addCellStyleCSS(cellStyle)) {
            spreadsheet.setCellStyleToCSSStyle(
                    spreadsheet.getCellStyleToCSSStyle());
        }
        if (sides == borders.sides(cellStyle.getIndex())) {
            borders.cellChanged(cell);
        } else {
            // A style changed in place changes what all its cells draw. Only
            // then, as finding that goes through every cell.
            borders.load();
        }
        sendBordersAction.schedule();
    }

    /**
     * Does nothing: the borders the cells draw on other cells than their own
     * reach the client before the next response after each change to them.
     */
    public void loadCustomBorderStylesToState() {
        // nothing to do
    }

    /**
     * Reloads all styles for the currently active sheet.
     */
    public void reloadActiveSheetCellStyles() {
        borders.load();
        sendBordersAction.schedule();
        // conditional formatting
        spreadsheet.getConditionalFormatter().createConditionalFormatterRules();
    }

    /**
     * Finds again what every cell draws on other cells, after a change that
     * moves or removes cells, rows or merged regions.
     */
    void reloadBorders() {
        borders.load();
        sendBordersAction.schedule();
    }

    /**
     * The rules of the borders the cells draw on other cells than their own.
     */
    ArrayList<String> borderRules() {
        return borders.rules();
    }

    private void sendBorders() {
        spreadsheet.setShiftedCellBorderStyles(borderRules());
    }

    @SuppressWarnings({ "unchecked" })
    private static <K, V> Map<K, V> mapFor(Object... mapping) {
        Map<K, V> map = new HashMap<K, V>();
        for (int i = 0; i < mapping.length; i += 2) {
            map.put((K) mapping[i], (V) mapping[i + 1]);
        }
        return map;
    }

    /**
     * Puts the style's CSS in the style map. Leaves sending the map to the
     * caller: each send serializes the whole map, and a workbook's styles are
     * added one by one - 20,000 of them took the server half a minute when each
     * was sent on its own.
     *
     * @return whether the map changed: the style is new, or its CSS is
     */
    private boolean addCellStyleCSS(CellStyle cellStyle) {

        if (cellStyle.getIndex() == 0) {
            // default cell style, do not change.
            return false;
        }

        StringBuilder sb = new StringBuilder();

        fontStyle(sb, cellStyle);
        colorConverter.colorStyles(cellStyle, sb);
        borders.appendBorderStyles(sb, cellStyle);
        if (cellStyle.getAlignment() != defaultTextAlign) {
            styleOut(sb, "text-align", cellStyle.getAlignment(), ALIGN);
            // TODO For correct overflow, rtl should be used for right align
            // if (cellStyle.getAlignment() == ALIGN_RIGHT) {
            // sb.append("direction:rtl;");
            // }
        }

        // excel default is bottom, so that is what we have in the CSS base
        // files.
        // TODO This only works on modern (10+) IE.
        styleOut(sb, "justify-content", cellStyle.getVerticalAlignment(),
                VERTICAL_ALIGN);

        if (cellStyle.getWrapText()) { // default is to overflow
            sb.append(
                    "overflow:hidden;white-space:normal;word-wrap:break-word;");
        }

        if (cellStyle.getIndention() > 0) {
            sb.append("padding-left: " + cellStyle.getIndention() + "em;");
        }

        String css = sb.toString();
        return !css.equals(spreadsheet.getCellStyleToCSSStyle()
                .put((int) cellStyle.getIndex(), css));
    }

    /**
     * The font of the default style, as its CSS. Kept to compare the fonts of
     * the other styles with.
     */
    private void defaultFontStyle(CellStyle cellStyle, StringBuilder sb) {
        defaultFont = spreadsheet.getWorkbook()
                .getFontAt(cellStyle.getFontIndexAsInt());
        defaultFontFamily = fontFamily(defaultFont);
        defaultFontHeightInPoints = defaultFont.getFontHeightInPoints();
        sb.append(defaultFontFamily);
        if (defaultFont.getBold()) {
            sb.append("font-weight:bold;");
        }
        if (defaultFont.getItalic()) {
            sb.append("font-style:italic;");
        }
        sb.append("font-size: var(--default-font-size);");
        sb.append("--default-font-size:");
        sb.append(defaultFontHeightInPoints);
        sb.append("pt;");
        sb.append(textDecoration(defaultFont.getUnderline() != Font.U_NONE,
                defaultFont.getStrikeout()));
    }

    /**
     * The font of the style, as CSS of what differs from the font of the
     * default style. Catches and warn-logs an {@link IndexOutOfBoundsException}
     * from POI when the workbook's font table is missing an index the cell
     * points to.
     */
    private void fontStyle(StringBuilder sb, CellStyle cellStyle) {
        try {
            Font font = spreadsheet.getWorkbook()
                    .getFontAt(cellStyle.getFontIndexAsInt());
            if (font.getIndexAsInt() == defaultFont.getIndexAsInt()) {
                // uses default font, no need to add styles
                return;
            }
            String fontFamily = fontFamily(font);
            if (!fontFamily.equals(defaultFontFamily)) {
                sb.append(fontFamily);
            }
            if (font.getBold()) {
                sb.append("font-weight:bold;");
            }
            if (font.getItalic()) {
                sb.append("font-style:italic;");
            }
            final int fontheight = font.getFontHeightInPoints();
            if (fontheight != defaultFontHeightInPoints) {
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
     * CSS rule for a font's family.
     * <p>
     * A font with no name, which POI reports for a {@code <name/>} without a
     * value, sets no family at all: its cells keep the family they would have
     * without it, the workbook default's or the theme's.
     */
    private static String fontFamily(Font font) {
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
    private static String cssFontName(String name) {
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

    private <K> void styleOut(StringBuilder sb, String attr, K key,
            Map<K, String> mapping) {
        String value = mapping.get(key);
        if (value != null) {
            sb.append(attr);
            sb.append(":");
            sb.append(value);
            sb.append(";");
        }
    }

    private void setupColorMap() {
        final Workbook workbook = spreadsheet.getWorkbook();
        if (workbook instanceof HSSFWorkbook hssf) {
            colorConverter = new HSSFColorConverter(hssf);
        } else {
            colorConverter = new XSSFColorConverter((XSSFWorkbook) workbook);
        }
    }
}
