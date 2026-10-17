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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;

import com.vaadin.flow.component.spreadsheet.SpreadsheetStyleFactory.BorderStyle;
import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * The CSS of the borders of the cell styles, and the borders the cells draw on
 * other cells than their own.
 * <p>
 * Excel draws the top border of a cell at the bottom of the cell above it, and
 * the left border at the right of the cell before it, past the rows and columns
 * hidden, and on the merged cell when that cell is in a merged region. It draws
 * the right and the bottom borders of the cells inside a merged region on the
 * merged cell, the first of each side in the order of the cells.
 * <p>
 * What a cell draws on other cells is kept by the cell, so a cell changed or
 * removed takes its borders with it, and the rules the client is given are made
 * from what the cells draw now, the same however they came to draw it.
 */
final class CellBorderStyleBuilder implements Serializable {

    /** A cell, 0-based, in the order of the rows, then of the columns. */
    record CellKey(int row,
            int column) implements Comparable<CellKey>, Serializable {

        @Override
        public int compareTo(CellKey other) {
            return row != other.row ? Integer.compare(row, other.row)
                    : Integer.compare(column, other.column);
        }

        String selector() {
            return ".col" + (column + 1) + ".row" + (row + 1);
        }
    }

    /**
     * The cells a cell of the style draws its borders on: its top border, its
     * left border, and its right and bottom borders on the merged cell it is
     * in. {@code null} where it draws nothing.
     */
    private record Drawn(int style, CellKey top, CellKey left,
            CellKey merged) implements Serializable {
    }

    private final Spreadsheet spreadsheet;
    private final ColorConverter colorConverter;

    /** By style, its top border as the rule of the cell above. */
    private final Map<Integer, String> topBorders = new HashMap<>();
    /** By style, its left border as the rule of the cell before. */
    private final Map<Integer, String> leftBorders = new HashMap<>();
    /** By style, the declarations of its right border. */
    private final Map<Integer, String> rightBorders = new HashMap<>();
    /** By style, the declarations of its bottom border. */
    private final Map<Integer, String> bottomBorders = new HashMap<>();
    /** By cell, what it draws on other cells, for the cells that do. */
    private final TreeMap<CellKey, Drawn> drawn = new TreeMap<>();

    CellBorderStyleBuilder(Spreadsheet spreadsheet,
            ColorConverter colorConverter) {
        this.spreadsheet = spreadsheet;
        this.colorConverter = colorConverter;
    }

    /**
     * Forgets the borders of the styles, to have them all again. What the cells
     * draw stays: a style whose borders change has its cells loaded again.
     */
    void resetStyles() {
        topBorders.clear();
        leftBorders.clear();
        rightBorders.clear();
        bottomBorders.clear();
    }

    /**
     * Appends the CSS of the right and the bottom borders of the style, which
     * are drawn on its own cells, and keeps the CSS of the borders its cells
     * draw on others.
     */
    void appendBorderStyles(StringBuilder sb, CellStyle cellStyle) {
        int style = cellStyle.getIndex();
        BorderStyle borderLeft = SpreadsheetStyleFactory.BORDER
                .get(cellStyle.getBorderLeft());
        BorderStyle borderRight = SpreadsheetStyleFactory.BORDER
                .get(cellStyle.getBorderRight());
        BorderStyle borderTop = SpreadsheetStyleFactory.BORDER
                .get(cellStyle.getBorderTop());
        BorderStyle borderBottom = SpreadsheetStyleFactory.BORDER
                .get(cellStyle.getBorderBottom());
        if (cellStyle instanceof XSSFCellStyle xssfCellStyle
                && !xssfCellStyle.getCoreXf().getApplyBorder()) {
            // POI reads no borders for a style not applying its border entry,
            // as some files have them: the entry is read instead.
            CTBorder ct = ((XSSFWorkbook) spreadsheet.getWorkbook())
                    .getStylesSource()
                    .getBorderAt((int) xssfCellStyle.getCoreXf().getBorderId())
                    .getCTBorder();
            if (ct.isSetLeft()) {
                borderLeft = fromEntry(ct.getLeft().getStyle());
            }
            if (ct.isSetRight()) {
                borderRight = fromEntry(ct.getRight().getStyle());
            }
            if (ct.isSetBottom()) {
                borderBottom = fromEntry(ct.getBottom().getStyle());
            }
            if (ct.isSetTop()) {
                borderTop = fromEntry(ct.getTop().getStyle());
            }
        }

        keep(rightBorders, style, declarations(borderRight, "border-right",
                BorderSide.RIGHT, "border-right-color", cellStyle));
        keep(bottomBorders, style, declarations(borderBottom, "border-bottom",
                BorderSide.BOTTOM, "border-bottom-color", cellStyle));
        sb.append(rightBorders.getOrDefault(style, ""));
        sb.append(bottomBorders.getOrDefault(style, ""));
        // the top and left borders are drawn on the cells before
        keep(topBorders, style, rule(declarations(borderTop, "border-bottom",
                BorderSide.TOP, "border-bottom-color", cellStyle)));
        keep(leftBorders, style, rule(declarations(borderLeft, "border-right",
                BorderSide.LEFT, "border-right-color", cellStyle)));
    }

    /** A side of a border entry, which has no style when it has no line. */
    private static BorderStyle fromEntry(STBorderStyle.Enum pattern) {
        return SpreadsheetStyleFactory.BORDER
                .get(org.apache.poi.ss.usermodel.BorderStyle
                        .valueOf((short) (pattern.intValue() - 1)));
    }

    private String declarations(BorderStyle border, String property,
            BorderSide side, String colorProperty, CellStyle cellStyle) {
        if (border == BorderStyle.NONE) {
            return "";
        }
        return property + ":" + border.getBorderAttributeValue()
                + colorConverter.getBorderColorCSS(side, colorProperty,
                        cellStyle);
    }

    private static String rule(String declarations) {
        return declarations.isEmpty() ? "" : "{" + declarations + "}";
    }

    private static void keep(Map<Integer, String> borders, int style,
            String css) {
        if (css.isEmpty()) {
            borders.remove(style);
        } else {
            borders.put(style, css);
        }
    }

    /**
     * Which of its borders the style has, one bit for each. A change in them
     * changes what its cells draw on other cells.
     */
    int sides(int style) {
        return (topBorders.containsKey(style) ? 1 : 0)
                | (leftBorders.containsKey(style) ? 2 : 0)
                | (rightBorders.containsKey(style) ? 4 : 0)
                | (bottomBorders.containsKey(style) ? 8 : 0);
    }

    /** Which of its borders each style has, by style. */
    Map<Integer, Integer> sidesOfStyles() {
        Map<Integer, Integer> sides = new HashMap<>();
        for (Map<Integer, String> borders : List.of(topBorders, leftBorders,
                rightBorders, bottomBorders)) {
            borders.keySet().forEach(style -> sides.put(style, sides(style)));
        }
        return sides;
    }

    /** Finds what every cell of the sheet shown draws on other cells. */
    void load() {
        drawn.clear();
        for (Row row : spreadsheet.getActiveSheet()) {
            for (Cell cell : row) {
                cellChanged(cell);
            }
        }
    }

    /** Finds what the cell draws on other cells, as it is now. */
    void cellChanged(Cell cell) {
        CellKey key = new CellKey(cell.getRowIndex(), cell.getColumnIndex());
        Drawn cellDraws = drawnBy(cell);
        if (cellDraws == null) {
            drawn.remove(key);
        } else {
            drawn.put(key, cellDraws);
        }
    }

    /** Takes off what a cell no longer there drew on other cells. */
    void cellRemoved(int row, int column) {
        drawn.remove(new CellKey(row, column));
    }

    private Drawn drawnBy(Cell cell) {
        int style = cell.getCellStyle().getIndex();
        int row = cell.getRowIndex();
        int column = cell.getColumnIndex();
        if (style == 0 || spreadsheet.isColumnHidden(column)
                || spreadsheet.isRowHidden(row)) {
            return null;
        }
        // 1-based
        MergedRegion region = spreadsheet.mergedRegionContainer
                .getMergedRegion(column + 1, row + 1);
        CellKey merged = null;
        if (region != null && (rightBorders.containsKey(style)
                || bottomBorders.containsKey(style))) {
            merged = new CellKey(region.row1 - 1, region.col1 - 1);
        }
        CellKey top = null;
        CellKey left = null;
        // Inside a merged region, only the cells on its edges draw on others.
        if (region == null || region.col1 == column + 1
                || region.col2 == column + 1 || region.row1 == row + 1
                || region.row2 == row + 1) {
            if (leftBorders.containsKey(style)) {
                left = cellBefore(row, column);
            }
            if (topBorders.containsKey(style)) {
                top = cellAbove(row, column);
            }
        }
        return top == null && left == null && merged == null ? null
                : new Drawn(style, top, left, merged);
    }

    /**
     * The cell shown before the cell, or the merged cell it is in; {@code null}
     * if there is none.
     */
    private CellKey cellBefore(int row, int column) {
        for (int before = column - 1; before >= 0; before--) {
            if (!spreadsheet.isColumnHidden(before)) {
                return mergedCellOf(row, before);
            }
        }
        return null;
    }

    /**
     * The cell shown above the cell, or the merged cell it is in; {@code null}
     * if there is none.
     */
    private CellKey cellAbove(int row, int column) {
        for (int above = row - 1; above >= 0; above--) {
            if (!spreadsheet.isRowHidden(above)) {
                return mergedCellOf(above, column);
            }
        }
        return null;
    }

    private CellKey mergedCellOf(int row, int column) {
        // 1-based
        MergedRegion region = spreadsheet.mergedRegionContainer
                .getMergedRegion(column + 1, row + 1);
        return region == null ? new CellKey(row, column)
                : new CellKey(region.row1 - 1, region.col1 - 1);
    }

    /**
     * The rules of the borders the cells draw on other cells, as the client is
     * given them: the left borders by style, the top borders by style, then the
     * borders of the merged cells. A merged cell given the same side twice
     * draws the first.
     */
    ArrayList<String> rules() {
        Map<Integer, TreeSet<CellKey>> lefts = new TreeMap<>();
        Map<Integer, TreeSet<CellKey>> tops = new TreeMap<>();
        Map<CellKey, String[]> mergedCells = new TreeMap<>();
        for (Drawn cellDraws : drawn.values()) {
            if (cellDraws.left() != null) {
                lefts.computeIfAbsent(cellDraws.style(),
                        style -> new TreeSet<>()).add(cellDraws.left());
            }
            if (cellDraws.top() != null) {
                tops.computeIfAbsent(cellDraws.style(),
                        style -> new TreeSet<>()).add(cellDraws.top());
            }
            if (cellDraws.merged() != null) {
                String[] sides = mergedCells.computeIfAbsent(cellDraws.merged(),
                        cell -> new String[2]);
                if (sides[0] == null) {
                    sides[0] = rightBorders.get(cellDraws.style());
                }
                if (sides[1] == null) {
                    sides[1] = bottomBorders.get(cellDraws.style());
                }
            }
        }
        ArrayList<String> rules = new ArrayList<>();
        lefts.forEach((style, cells) -> rules
                .add(selectors(cells) + leftBorders.get(style)));
        tops.forEach((style, cells) -> rules
                .add(selectors(cells) + topBorders.get(style)));
        mergedCells.forEach((cell,
                sides) -> rules.add(cell.selector() + "{"
                        + (sides[0] == null ? "" : sides[0])
                        + (sides[1] == null ? "" : sides[1]) + "}"));
        return rules;
    }

    private static String selectors(Set<CellKey> cells) {
        return cells.stream().map(CellKey::selector)
                .collect(Collectors.joining(","));
    }
}
