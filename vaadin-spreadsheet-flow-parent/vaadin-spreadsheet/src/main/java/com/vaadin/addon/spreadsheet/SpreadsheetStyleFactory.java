package com.vaadin.addon.spreadsheet;

import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER_SELECTION;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_FILL;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_JUSTIFY;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_LEFT;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_RIGHT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DASHED;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DASH_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DASH_DOT_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DOTTED;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DOUBLE;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_HAIR;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM_DASHED;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM_DASH_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM_DASH_DOT_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_NONE;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_SLANTED_DASH_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_THICK;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_THIN;
import static org.apache.poi.ss.usermodel.CellStyle.VERTICAL_BOTTOM;
import static org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER;
import static org.apache.poi.ss.usermodel.CellStyle.VERTICAL_TOP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;

import com.vaadin.addon.spreadsheet.client.MergedRegion;

public class SpreadsheetStyleFactory {

    private static final String BORDER_STYLE_ZINDEX_5 = "z-index:5;";

    public enum BorderStyle {
        SOLID_THIN("solid", 1, 1), DOTTED_THIN("dotted", 1, 1), DASHED_THIN(
                "dashed", 1, 1), SOLID_MEDIUM("solid", 2, 2), DASHED_MEDIUM(
                "dashed", 2, 2), SOLID_THICK("solid", 3, 4), DOUBLE("double",
                3, 4), NONE("none", 0, 0);

        private final int size;
        private final int adjustment;
        private final String borderStyle;

        BorderStyle(String borderStyle, int size, int adjustment) {
            this.borderStyle = borderStyle;
            this.size = size;
            this.adjustment = adjustment;
        }

        public String getValue() {
            return borderStyle;
        }

        public int getSize() {
            return size;
        }

        public int getVerticalAdjustment() {
            return size;
        }

        public int getHorizontalAdjustment() {
            return adjustment;
        }

        public String getBorderAttributeValue() {
            return borderStyle + " " + size + "pt;";
        }

    }

    private static final Map<Short, String> ALIGN = mapFor(ALIGN_LEFT, "left",
            ALIGN_CENTER, "center", ALIGN_RIGHT, "right", ALIGN_FILL, "left",
            ALIGN_JUSTIFY, "left", ALIGN_CENTER_SELECTION, "center");

    // FIXME vertical alignment isn't working currently
    private static final Map<Short, String> VERTICAL_ALIGN = mapFor(
            VERTICAL_BOTTOM, "bottom", VERTICAL_CENTER, "middle", VERTICAL_TOP,
            "top");

    private static final Map<Short, BorderStyle> BORDER = mapFor(
            BORDER_DASH_DOT, BorderStyle.DASHED_THIN, BORDER_DASH_DOT_DOT,
            BorderStyle.DASHED_THIN, BORDER_DASHED, BorderStyle.DASHED_THIN,
            BORDER_DOTTED, BorderStyle.DOTTED_THIN, BORDER_DOUBLE,
            BorderStyle.DOUBLE, BORDER_HAIR, BorderStyle.SOLID_THIN,
            BORDER_MEDIUM, BorderStyle.SOLID_MEDIUM, BORDER_MEDIUM_DASH_DOT,
            BorderStyle.DASHED_MEDIUM, BORDER_MEDIUM_DASH_DOT_DOT,
            BorderStyle.DASHED_MEDIUM, BORDER_MEDIUM_DASHED,
            BorderStyle.DASHED_MEDIUM, BORDER_NONE, BorderStyle.NONE,
            BORDER_SLANTED_DASH_DOT, BorderStyle.DASHED_MEDIUM, BORDER_THICK,
            BorderStyle.SOLID_THICK, BORDER_THIN, BorderStyle.SOLID_THIN);

    // custom cell style indices used to implement default alignment. all POI
    // indices are non-negative
    public static final int CELL_STYLE_INDEX_ALIGN_LEFT = -1;
    public static final int CELL_STYLE_INDEX_ALIGN_RIGHT = -2;

    /** CellStyle index to selector + style map */
    private final HashMap<Integer, String> shiftedBorderTopStyles = new HashMap<Integer, String>();
    /** CellStyle index to selector + style map */
    private final HashMap<Integer, String> shiftedBorderLeftStyles = new HashMap<Integer, String>();
    /** */
    private final HashMap<String, String> mergedCellBorders = new HashMap<String, String>();

    private ColorConverter colorConverter;

    private Spreadsheet spreadsheet;

    private Font defaultFont;

    private short defaultVerticalAlign;

    private short defaultTextAlign;

    private short defaultFontHeightInPoints;

    private String defaultFontFamily;

    @SuppressWarnings({ "unchecked" })
    private static <K, V> Map<K, V> mapFor(Object... mapping) {
        Map<K, V> map = new HashMap<K, V>();
        for (int i = 0; i < mapping.length; i += 2) {
            map.put((K) mapping[i], (V) mapping[i + 1]);
        }
        return map;
    }

    public SpreadsheetStyleFactory(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;

        setupColorMap();
    }

    public void reloadWorkbookStyles() {
        final Workbook workbook = spreadsheet.getWorkbook();
        if (spreadsheet.getState().cellStyleToCSSStyle == null) {
            spreadsheet.getState().cellStyleToCSSStyle = new HashMap<Integer, String>(
                    workbook.getNumCellStyles());
        } else {
            spreadsheet.getState().cellStyleToCSSStyle.clear();
        }
        shiftedBorderLeftStyles.clear();
        shiftedBorderTopStyles.clear();
        mergedCellBorders.clear();

        // get default text alignments
        CellStyle cellStyle = workbook.getCellStyleAt((short) 0);
        defaultTextAlign = cellStyle.getAlignment();
        defaultVerticalAlign = cellStyle.getVerticalAlignment();

        // create default style
        StringBuilder sb = new StringBuilder();
        borderStyles(sb, cellStyle);
        defaultFontStyle(cellStyle, sb);
        colorConverter.defaultColorStyles(cellStyle, sb);
        spreadsheet.getState().cellStyleToCSSStyle.put(
                (int) cellStyle.getIndex(), sb.toString());

        // create default style, left aligned
        sb = new StringBuilder();
        defaultFontStyle(cellStyle, sb);
        borderStyles(sb, cellStyle);
        colorConverter.defaultColorStyles(cellStyle, sb);
        styleOut(sb, "text-align", ALIGN_LEFT, ALIGN);
        spreadsheet.getState().cellStyleToCSSStyle.put(
                getLeftAlignedStyleIndex(cellStyle.getIndex()), sb.toString());

        // create default style, right aligned
        sb = new StringBuilder();
        defaultFontStyle(cellStyle, sb);
        borderStyles(sb, cellStyle);
        colorConverter.defaultColorStyles(cellStyle, sb);
        styleOut(sb, "text-align", ALIGN_RIGHT, ALIGN);
        spreadsheet.getState().cellStyleToCSSStyle.put(
                getRightAlignedStyleIndex(cellStyle.getIndex()), sb.toString());

        // 0 is default style, create all styles indexed from 1 and upwards
        for (short i = 1; i < workbook.getNumCellStyles(); i++) {
            cellStyle = workbook.getCellStyleAt(i);
            addNormalCellStyleCSS(cellStyle);
            addLeftAlignedCellStyleCSS(cellStyle);
            addRightAlignedCellStyleCSS(cellStyle);
        }

        // Notification.show(spreadsheet.getState().cellStyleToCSSStyle.toString());
    }

    private void addNormalCellStyleCSS(CellStyle cellStyle) {
        StringBuilder sb = new StringBuilder();

        fontStyle(sb, cellStyle);
        colorConverter.colorStyles(cellStyle, sb);
        borderStyles(sb, cellStyle);
        if (cellStyle.getAlignment() != defaultTextAlign) {
            styleOut(sb, "text-align", cellStyle.getAlignment(), ALIGN);
        }
        if (cellStyle.getVerticalAlignment() != defaultVerticalAlign) {
            styleOut(sb, "vertical-align", cellStyle.getAlignment(),
                    VERTICAL_ALIGN);
        }

        spreadsheet.getState().cellStyleToCSSStyle.put(
                (int) cellStyle.getIndex(), sb.toString());
    }

    private void addLeftAlignedCellStyleCSS(CellStyle cellStyle) {
        StringBuilder sb = new StringBuilder();

        fontStyle(sb, cellStyle);
        colorConverter.colorStyles(cellStyle, sb);
        borderStyles(sb, cellStyle);
        styleOut(sb, "text-align", ALIGN_LEFT, ALIGN);
        if (cellStyle.getVerticalAlignment() != defaultVerticalAlign) {
            styleOut(sb, "vertical-align", cellStyle.getAlignment(),
                    VERTICAL_ALIGN);
        }

        spreadsheet.getState().cellStyleToCSSStyle.put(
                getLeftAlignedStyleIndex(cellStyle.getIndex()), sb.toString());
    }

    private void addRightAlignedCellStyleCSS(CellStyle cellStyle) {
        StringBuilder sb = new StringBuilder();

        fontStyle(sb, cellStyle);
        colorConverter.colorStyles(cellStyle, sb);
        borderStyles(sb, cellStyle);
        styleOut(sb, "text-align", ALIGN_RIGHT, ALIGN);
        if (cellStyle.getVerticalAlignment() != defaultVerticalAlign) {
            styleOut(sb, "vertical-align", cellStyle.getAlignment(),
                    VERTICAL_ALIGN);
        }

        spreadsheet.getState().cellStyleToCSSStyle.put(
                getRightAlignedStyleIndex(cellStyle.getIndex()), sb.toString());
    }

    static public int getLeftAlignedStyleIndex(int styleIndex) {
        return -2 * styleIndex - 1;
    }

    static public int getRightAlignedStyleIndex(int styleIndex) {
        return -2 * styleIndex - 2;
    }

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
     * Clears all styles for the given cell. Should used when i.e. a cell has
     * been shifted (the old location is cleared of all styles).
     * 
     * @param oldColumnIndex
     *            0-based
     * @param oldRowIndex
     *            0-based
     */
    public void clearCellStyle(int oldColumnIndex, int oldRowIndex) {
        final String cssSelector = ".col" + (oldColumnIndex + 1) + ".row"
                + (oldRowIndex + 1);
        // remove/modify all possible old custom styles that the cell had (can
        // be found from state)
        ArrayList<String> add = new ArrayList<String>();
        Iterator<String> iterator = spreadsheet.getState().customCellBorderStyles
                .iterator();
        while (iterator.hasNext()) {
            String style = iterator.next();
            // only cell with this style -> remove
            if (style.startsWith(cssSelector + "{")) {
                iterator.remove();
            } else if (style.contains(cssSelector)) { // shifted borders
                iterator.remove();
                int index = style.indexOf(cssSelector);
                if (index > 0) { // doesn't start with the selector
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the last
                    style = style.replace("," + cssSelector + "{", "{");
                } else {
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the last
                    style = style.replace(cssSelector + "{", "{");
                }
                if (!style.startsWith("{")) {
                    add.add(style);
                }
            }
        }
        for (String s : add) {
            spreadsheet.getState().customCellBorderStyles.add(s);
        }

        HashMap<Integer, String> add2 = new HashMap<Integer, String>();
        Iterator<Entry<Integer, String>> iterator2 = shiftedBorderLeftStyles
                .entrySet().iterator();
        while (iterator2.hasNext()) {
            Entry<Integer, String> entry = iterator2.next();
            String style = entry.getValue();
            if (style.contains(cssSelector)) { // shifted borders
                iterator2.remove();
                int index = style.indexOf(cssSelector);
                if (index > 0) { // doesn't start with the selector
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the last
                    style = style.replace("," + cssSelector + "{", "{");
                } else {
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the only
                    style = style.replace(cssSelector + "{", "{");
                }
                add2.put(entry.getKey(), style);
            }
        }
        shiftedBorderLeftStyles.putAll(add2);
        add2.clear();

        iterator2 = shiftedBorderTopStyles.entrySet().iterator();
        while (iterator2.hasNext()) {
            Entry<Integer, String> entry = iterator2.next();
            String style = entry.getValue();
            if (style.contains(cssSelector)) { // shifted borders
                iterator2.remove();
                int index = style.indexOf(cssSelector);
                if (index > 0) { // doesn't start with the selector
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the last
                    style = style.replace("," + cssSelector + "{", "{");
                } else {
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the only
                    style = style.replace(cssSelector + "{", "{");
                }
                add2.put(entry.getKey(), style);
            }
        }
        shiftedBorderTopStyles.putAll(add2);
        add2.clear();

        if (mergedCellBorders.containsKey(cssSelector)) {
            String rules = mergedCellBorders.remove(cssSelector);
            spreadsheet.getState().customCellBorderStyles
                    .remove(buildMergedCellBorderCSS(cssSelector, rules));
        }
    }

    public void cellStyleUpdated(Cell cell, boolean updateCustomBorders) {
        final String cssSelector = ".col" + (cell.getColumnIndex() + 1)
                + ".row" + (cell.getRowIndex() + 1);
        final Integer key = (int) cell.getCellStyle().getIndex();
        // remove/modify all possible old custom styles that the cell had (can
        // be found from state)
        ArrayList<String> add = new ArrayList<String>();
        Iterator<String> iterator = spreadsheet.getState().customCellBorderStyles
                .iterator();
        while (iterator.hasNext()) {
            String style = iterator.next();
            // only cell with this style -> remove
            if (style.startsWith(cssSelector + "{")) {
                iterator.remove();
            } else if (style.contains(cssSelector)) { // shifted borders
                iterator.remove();
                int index = style.indexOf(cssSelector);
                if (index > 0) { // doesn't start with the selector
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the last
                    style = style.replace("," + cssSelector + "{", "{");
                } else {
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the only
                    style = style.replace(cssSelector + "{", "{");
                }
                if (!style.startsWith(",") && !style.startsWith("{")) {
                    add.add(style);
                }
            }
        }
        for (String s : add) {
            spreadsheet.getState().customCellBorderStyles.add(s);
        }
        // remove the cell's new custom styles from state (will be added again
        // as this cell is styled)
        if (shiftedBorderLeftStyles.containsKey(key)) {
            final String style = shiftedBorderLeftStyles.get(key);
            spreadsheet.getState().customCellBorderStyles.remove(style);
        }
        if (shiftedBorderTopStyles.containsKey(key)) {
            final String style = shiftedBorderTopStyles.get(key);
            spreadsheet.getState().customCellBorderStyles.remove(style);
        }
        if (mergedCellBorders.containsKey(cssSelector)) {
            final String style = buildMergedCellBorderCSS(cssSelector,
                    mergedCellBorders.remove(cssSelector));
            spreadsheet.getState().customCellBorderStyles.remove(style);
        }

        // if a new style was created
        if (!spreadsheet.getState().cellStyleToCSSStyle.containsKey(key)) {
            CellStyle cellStyle = cell.getCellStyle();
            addNormalCellStyleCSS(cellStyle);
            addLeftAlignedCellStyleCSS(cellStyle);
            addRightAlignedCellStyleCSS(cellStyle);
        }

        // custom styles
        doCellCustomStyling(cell);

        if (updateCustomBorders) {
            if (shiftedBorderLeftStyles.containsKey(key)) {
                final String style = shiftedBorderLeftStyles.get(key);
                spreadsheet.getState().customCellBorderStyles.add(style);
            }
            if (shiftedBorderTopStyles.containsKey(key)) {
                final String style = shiftedBorderTopStyles.get(key);
                spreadsheet.getState().customCellBorderStyles.add(style);
            }
            if (mergedCellBorders.containsKey(cssSelector)) {
                spreadsheet.getState().customCellBorderStyles
                        .add(buildMergedCellBorderCSS(cssSelector,
                                mergedCellBorders.get(cssSelector)));
            }
        }
    }

    public void loadCustomBorderStylesToState() {
        if (spreadsheet.getState().customCellBorderStyles != null) {
            spreadsheet.getState().customCellBorderStyles.clear();
        } else {
            spreadsheet.getState().customCellBorderStyles = new ArrayList<String>();
        }
        for (String value : shiftedBorderLeftStyles.values()) {
            if (value.startsWith(".col")) {
                spreadsheet.getState().customCellBorderStyles.add(value);
            }
        }
        for (String value : shiftedBorderTopStyles.values()) {
            if (value.startsWith(".col")) {
                spreadsheet.getState().customCellBorderStyles.add(value);
            }
        }
        for (Entry<String, String> entry : mergedCellBorders.entrySet()) {
            spreadsheet.getState().customCellBorderStyles
                    .add(buildMergedCellBorderCSS(entry.getKey(),
                            entry.getValue()));

        }
    }

    public void reloadActiveSheetCellStyles() {

        if (spreadsheet.getState().customCellBorderStyles == null) {
            spreadsheet.getState().customCellBorderStyles = new ArrayList<String>();
        } else {
            spreadsheet.getState().customCellBorderStyles.clear();
        }

        for (Row row : spreadsheet.getActiveSheet()) {
            for (Cell cell : row) {
                doCellCustomStyling(cell);
            }
        }
        for (String value : shiftedBorderLeftStyles.values()) {
            if (value.startsWith(".col")) {
                spreadsheet.getState().customCellBorderStyles.add(value);
            }
        }
        for (String value : shiftedBorderTopStyles.values()) {
            if (value.startsWith(".col")) {
                spreadsheet.getState().customCellBorderStyles.add(value);
            }
        }
        for (Entry<String, String> entry : mergedCellBorders.entrySet()) {
            spreadsheet.getState().customCellBorderStyles
                    .add(buildMergedCellBorderCSS(entry.getKey(),
                            entry.getValue()));

        }
    }

    private String buildMergedCellBorderCSS(String selector, String rules) {
        if (selector.endsWith(",")) {
            selector = selector.substring(0, selector.length() - 1);
        }
        if (selector.length() < 1) {
            selector = ".notusedselector";
        }
        StringBuilder sb = new StringBuilder(selector);
        sb.append("{");
        sb.append(rules);
        sb.append(BORDER_STYLE_ZINDEX_5);
        sb.append("}");
        return sb.toString();
    }

    private void doCellCustomStyling(final Cell cell) {
        CellStyle cellStyle = cell.getCellStyle();
        final Integer key = (int) cellStyle.getIndex();
        if (key == 0) { // default style
            return;
        }

        // merged regions have their borders in edge cells that are "invisible"
        // inside the region -> right and bottom cells need to be transfered to
        // the actual merged cell
        final int columnIndex = cell.getColumnIndex();
        final int rowIndex = cell.getRowIndex();
        MergedRegion region = spreadsheet.mergedRegionContainer
                .getMergedRegion((columnIndex + 1), (rowIndex + 1));
        if (region != null) {
            final String borderRight = getBorderRightStyle(cellStyle);
            final String borderBottom = getBorderBottomStyle(cellStyle);
            if ((borderRight != null && !borderRight.isEmpty())
                    || (borderBottom != null && !borderBottom.isEmpty())) {
                StringBuilder sb = new StringBuilder(".col");
                sb.append(region.col1);
                sb.append(".row");
                sb.append(region.row1);
                final String cssKey = sb.toString();
                final String currentBorders = mergedCellBorders.get(cssKey);
                StringBuilder style;
                if (currentBorders != null && !currentBorders.isEmpty()) {
                    style = new StringBuilder(currentBorders);
                } else {
                    style = new StringBuilder();
                }
                if (borderRight != null
                        && !borderRight.isEmpty()
                        && (currentBorders == null || !currentBorders
                                .contains("border-right"))) {
                    style.append(borderRight);
                }
                if (borderBottom != null
                        && !borderBottom.isEmpty()
                        && (currentBorders == null || !currentBorders
                                .contains("border-bottom"))) {
                    style.append(borderBottom);
                }
                final String newBorders = style.toString();
                if (!newBorders.isEmpty()) {
                    mergedCellBorders.put(cssKey, newBorders);
                }
            }

        }

        // only take transfered borders into account on the (possible) merged
        // regions edges
        if (region == null || region.col1 == (columnIndex + 1)
                || region.col2 == (columnIndex + 1)
                || region.row1 == (rowIndex + 1)
                || region.row2 == (rowIndex + 1)) {

            StringBuilder sb;
            if (shiftedBorderLeftStyles.containsKey(key)) {
                // need to add the border right style to previous cell on
                // left, which might be a merged cell
                if (columnIndex > 0) {
                    String value = shiftedBorderLeftStyles.get(key);
                    sb = new StringBuilder();
                    sb.append(".col");
                    MergedRegion previousRegion = spreadsheet.mergedRegionContainer
                            .getMergedRegion(columnIndex, rowIndex + 1);
                    if (previousRegion != null) {
                        sb.append(previousRegion.col1);
                        sb.append(".row");
                        sb.append(previousRegion.row1);
                    } else {
                        sb.append(columnIndex);
                        sb.append(".row");
                        sb.append(rowIndex + 1);
                    }
                    if (!value.contains(sb.toString() + ",")
                            && !value.contains(sb.toString() + "{")) {
                        if (!value.startsWith("{")) {
                            sb.append(",");
                        }
                        sb.append(value);
                        shiftedBorderLeftStyles.put(key, sb.toString());
                    }
                }
            }
            if (shiftedBorderTopStyles.containsKey(key)) {
                // need to add the border bottom style to cell on previous
                // row, which might be a merged cell
                if (rowIndex > 0) {
                    String value = shiftedBorderTopStyles.get(key);
                    sb = new StringBuilder();
                    sb.append(".col");
                    MergedRegion previousRegion = spreadsheet.mergedRegionContainer
                            .getMergedRegion(columnIndex + 1, rowIndex);
                    if (previousRegion != null) {
                        sb.append(previousRegion.col1);
                        sb.append(".row");
                        sb.append(previousRegion.row1);
                    } else {
                        sb.append(columnIndex + 1);
                        sb.append(".row");
                        sb.append(rowIndex);
                    }
                    if (!value.contains(sb.toString() + ",")
                            && !value.contains(sb.toString() + "{")) {
                        if (!value.startsWith("{")) {
                            sb.append(",");
                        }
                        sb.append(value);
                        shiftedBorderTopStyles.put(key, sb.toString());
                    }
                }
            }

        }
    }

    private void defaultFontStyle(CellStyle cellStyle, StringBuilder sb) {
        if (cellStyle.getIndex() == 0) {
            defaultFont = spreadsheet.getWorkbook().getFontAt(
                    cellStyle.getFontIndex());
            defaultFontFamily = styleFontFamily(defaultFont);
            sb.append(defaultFontFamily);
            if (defaultFont.getBoldweight() != Font.BOLDWEIGHT_NORMAL) {
                sb.append("font-weight:");
                sb.append(defaultFont.getBoldweight());
                sb.append(";");
            }
            if (defaultFont.getItalic()) {
                sb.append("font-style:italic;");
            }
            defaultFontHeightInPoints = defaultFont.getFontHeightInPoints();
            sb.append("font-size:");
            sb.append(defaultFontHeightInPoints);
            sb.append("pt;");
            if (defaultFont.getUnderline() != Font.U_NONE) {
                sb.append("text-decoration:underline;");
            } else if (defaultFont.getStrikeout()) {
                sb.append("text-decoration:overline;");
            }
        }
    }

    private void fontStyle(StringBuilder sb, CellStyle cellStyle) {
        try {
            Font font = spreadsheet.getWorkbook().getFontAt(
                    cellStyle.getFontIndex());
            if (font.getIndex() == defaultFont.getIndex()) {
                // uses default font, no need to add styles
                return;
            }
            String fontFamily = styleFontFamily(font);
            if (!fontFamily.equals(defaultFontFamily)) {
                sb.append(fontFamily);
            }
            if (font.getBoldweight() != Font.BOLDWEIGHT_NORMAL) {
                sb.append("font-weight:");
                sb.append(font.getBoldweight());
                sb.append(";");
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
            if (font.getUnderline() != Font.U_NONE) {
                sb.append("text-decoration:underline;");
            } else if (font.getStrikeout()) {
                sb.append("text-decoration:overline;");
            }
        } catch (IndexOutOfBoundsException ioobe) {
            // somehow workbook doesn't have all the fonts the cells have???
            System.out.println("Font missing, " + cellStyle.getFontIndex()
                    + " / " + cellStyle.getClass() + ", " + ioobe.getMessage());
        }
    }

    private String styleFontFamily(Font font) {
        StringBuilder sb = new StringBuilder();
        sb.append("font-family:");
        String fontName = font.getFontName();
        if (fontName.contains(" ")) {
            sb.append("\"");
            sb.append(fontName);
            sb.append("\",");
        } else {
            sb.append(fontName);
            sb.append(",");
        }
        if (font instanceof XSSFFont) {
            FontFamily family = FontFamily.valueOf(((XSSFFont) font)
                    .getFamily());
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

    private String getBorderRightStyle(CellStyle cellStyle) {
        StringBuilder sb = new StringBuilder();
        BorderStyle borderRight = BORDER.get(cellStyle.getBorderRight());
        if (cellStyle instanceof XSSFCellStyle
                && !((XSSFCellStyle) cellStyle).getCoreXf().getApplyBorder()) {
            // borders from theme are not working in POI 3.9
            final CTXf _cellXf = ((XSSFCellStyle) cellStyle).getCoreXf();
            int idx = (int) _cellXf.getBorderId();
            CTBorder ct = ((XSSFWorkbook) spreadsheet.getWorkbook())
                    .getStylesSource().getBorderAt(idx).getCTBorder();
            if (borderRight == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetRight() ? ct.getRight()
                        .getStyle() : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderRight = BORDER.get(key);
                }
            }
        }
        if (borderRight != BorderStyle.NONE) {
            sb.append("border-right:");
            sb.append(borderRight.getBorderAttributeValue());
            colorConverter.colorBorder(BorderSide.RIGHT, "border-right-color",
                    cellStyle, sb);
        }
        return sb.toString();
    }

    private String getBorderBottomStyle(CellStyle cellStyle) {
        StringBuilder sb = new StringBuilder();
        BorderStyle borderBottom = BORDER.get(cellStyle.getBorderBottom());
        if (cellStyle instanceof XSSFCellStyle
                && !((XSSFCellStyle) cellStyle).getCoreXf().getApplyBorder()) {
            // borders from theme are not working in POI 3.9
            final CTXf _cellXf = ((XSSFCellStyle) cellStyle).getCoreXf();
            int idx = (int) _cellXf.getBorderId();
            CTBorder ct = ((XSSFWorkbook) spreadsheet.getWorkbook())
                    .getStylesSource().getBorderAt(idx).getCTBorder();
            if (borderBottom == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetBottom() ? ct.getBottom()
                        .getStyle() : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderBottom = BORDER.get(key);
                }
            }
        }
        if (borderBottom != BorderStyle.NONE) {
            sb.append("border-bottom:");
            sb.append(borderBottom.getBorderAttributeValue());
            colorConverter.colorBorder(BorderSide.BOTTOM,
                    "border-bottom-color", cellStyle, sb);
        }
        return sb.toString();
    }

    private void borderStyles(StringBuilder sb, CellStyle cellStyle) {

        BorderStyle borderLeft = BORDER.get(cellStyle.getBorderLeft());
        BorderStyle borderRight = BORDER.get(cellStyle.getBorderRight());
        BorderStyle borderTop = BORDER.get(cellStyle.getBorderTop());
        BorderStyle borderBottom = BORDER.get(cellStyle.getBorderBottom());

        if (cellStyle instanceof XSSFCellStyle
                && !((XSSFCellStyle) cellStyle).getCoreXf().getApplyBorder()) {
            // borders from theme are not working in POI 3.9
            final CTXf _cellXf = ((XSSFCellStyle) cellStyle).getCoreXf();
            int idx = (int) _cellXf.getBorderId();
            CTBorder ct = ((XSSFWorkbook) spreadsheet.getWorkbook())
                    .getStylesSource().getBorderAt(idx).getCTBorder();

            if (borderLeft == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetLeft() ? ct.getLeft()
                        .getStyle() : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderLeft = BORDER.get(key);
                }
            }
            if (borderRight == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetRight() ? ct.getRight()
                        .getStyle() : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderRight = BORDER.get(key);
                }
            }
            if (borderBottom == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetBottom() ? ct.getBottom()
                        .getStyle() : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderBottom = BORDER.get(key);
                }
            }
            if (borderTop == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetTop() ? ct.getTop()
                        .getStyle() : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderTop = BORDER.get(key);
                }

            }
        }

        if (borderRight != BorderStyle.NONE || borderBottom != BorderStyle.NONE) {
            if (borderRight != BorderStyle.NONE) {
                sb.append("border-right:");
                sb.append(borderRight.getBorderAttributeValue());
                colorConverter.colorBorder(BorderSide.RIGHT,
                        "border-right-color", cellStyle, sb);
            }
            if (borderBottom != BorderStyle.NONE) {
                sb.append("border-bottom:");
                sb.append(borderBottom.getBorderAttributeValue());
                colorConverter.colorBorder(BorderSide.BOTTOM,
                        "border-bottom-color", cellStyle, sb);
            }
            sb.append(BORDER_STYLE_ZINDEX_5);
        }

        // the top and right borders are transferred to previous cells
        if (borderTop != BorderStyle.NONE || borderLeft != BorderStyle.NONE) {
            if (borderTop != BorderStyle.NONE) {
                final StringBuilder sb2 = new StringBuilder("{border-bottom:");
                sb2.append(borderTop.getBorderAttributeValue());
                colorConverter.colorBorder(BorderSide.TOP,
                        "border-bottom-color", cellStyle, sb2);
                sb2.append(BORDER_STYLE_ZINDEX_5);
                sb2.append("}");
                shiftedBorderTopStyles.put((int) cellStyle.getIndex(),
                        sb2.toString());
            }
            if (borderLeft != BorderStyle.NONE) {
                final StringBuilder sb2 = new StringBuilder("{border-right:");
                sb2.append(borderLeft.getBorderAttributeValue());
                colorConverter.colorBorder(BorderSide.LEFT,
                        "border-right-color", cellStyle, sb2);
                sb2.append(BORDER_STYLE_ZINDEX_5);
                sb2.append("}");
                shiftedBorderLeftStyles.put((int) cellStyle.getIndex(),
                        sb2.toString());
            }
        }
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
        if (workbook instanceof HSSFWorkbook) {
            colorConverter = new HSSFColorConverter((HSSFWorkbook) workbook);
        } else {
            colorConverter = new XSSFColorConverter((XSSFWorkbook) workbook);
        }
    }
}
