package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.util.Objects;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;

public class Cell {

    public static final String CELL_COMMENT_TRIANGLE_CLASSNAME = "cell-comment-triangle";
    public static final String CELL_INVALID_FORMULA_CLASSNAME = "cell-invalidformula-triangle";
    private static final int ZINDEXVALUE = 2;
    private final DivElement element;
    private DivElement cellCommentTriangle;
    private DivElement invalidFormulaTriangle;
    /**
     * 1-based
     */
    private int col;
    /**
     * 1-based
     */
    private int row;
    private Element popupButtonElement;
    private String value;

    private String cellStyle = "cs0";
    private boolean needsMeasure;
    private SheetWidget sheetWidget;
    private boolean overflowDirty = true;
    private boolean overflowing;

    public Cell(SheetWidget sheetWidget, int col, int row) {
        this.sheetWidget = sheetWidget;
        this.col = col;
        this.row = row;

        element = Document.get().createDivElement();
        updateCellValues();
    }

    public Cell(SheetWidget sheetWidget, int col, int row, CellData cellData) {
        this.sheetWidget = sheetWidget;
        this.col = col;
        this.row = row;
        element = Document.get().createDivElement();
        if (cellData == null) {
            value = null;
        } else {
            needsMeasure = cellData.needsMeasure;
            value = cellData.value;
            cellStyle = cellData.cellStyle;
        }
        updateCellValues();
        updateInnerText();
    }

    public DivElement getElement() {
        return element;
    }

    public void update(int col, int row, CellData cellData) {
        this.col = col;
        this.row = row;
        cellStyle = cellData == null ? "cs0" : cellData.cellStyle;
        value = cellData == null ? null : cellData.value;

        updateInnerText();
        updateCellValues();

        markAsOverflowDirty();
    }

    private void updateInnerText() {
        element.getStyle().setOverflow(Overflow.HIDDEN);
        if (value == null || value.isEmpty()) {
            element.setInnerText("");
            element.getStyle().clearZIndex();
        } else {
            element.getStyle().setZIndex(ZINDEXVALUE);
            if (needsMeasure
                    && sheetWidget.measureValueWidth(cellStyle, value) > getElement()
                            .getClientWidth()) {
                element.setInnerText("###");
            } else {
                element.setInnerText(value);
            }
        }
    }

    void updateOverflow() {

        boolean rightAligned = element.getAttribute("class").contains(" r ")
                || element.getAttribute("class").endsWith(" r");

        int columnWidth = sheetWidget.actionHandler.getColWidth(col);

        Integer scrollW = sheetWidget.scrollWidthCache.get(getUniqueKey());
        if (scrollW == null) {
            scrollW = measureOverflow();
        }
        int overflowPx = scrollW - columnWidth;
        if (!rightAligned && overflowPx > 0) {
            // Increase overflow by cell left padding (2px)
            overflowPx += 2;
            int colIndex = col;
            int width = 0;
            int[] colW = sheetWidget.actionHandler.getColWidths();
            boolean inFreezePane = col <= sheetWidget.verticalSplitPosition;

            while (colIndex < colW.length && width < overflowPx) {
                if (inFreezePane
                        && colIndex >= sheetWidget.verticalSplitPosition) {
                    break;
                }
                Cell nextCell = sheetWidget.getCell(colIndex + 1, row);
                if (nextCell != null && nextCell.hasValue()) {
                    break;
                }
                width += colW[colIndex];
                colIndex++;
            }
            // columnWidth is added after calculating the overflowing width
            width += columnWidth;

            // create element to contain the text, so we can apply overflow
            // rules
            DivElement overflowDiv = Document.get().createDivElement();
            overflowDiv.getStyle().setProperty("pointerEvents", "none");
            overflowDiv.getStyle().setWidth(width, Style.Unit.PX);
            overflowDiv.getStyle().setOverflow(Overflow.HIDDEN);
            overflowDiv.getStyle().setTextOverflow(Style.TextOverflow.ELLIPSIS);
            overflowDiv.setInnerText(element.getInnerText());
            element.setInnerText(null);
            element.appendChild(overflowDiv);

            overflowing = true;
        } else {
            overflowing = false;
        }
        if (sheetWidget.isMergedCell(SheetWidget.toKey(col, row))
                && !(this instanceof MergedCell)) {
            element.getStyle().setOverflow(Overflow.HIDDEN);
        } else {
            element.getStyle().setOverflow(Overflow.VISIBLE);
        }
        overflowDirty = false;
    }

    int measureOverflow() {
        if (overflowing) {
            updateInnerText();
        }
        Integer scrollW = sheetWidget.scrollWidthCache.get(getUniqueKey());
        if (scrollW == null) {
            scrollW = element.getScrollWidth();
            sheetWidget.scrollWidthCache.put(getUniqueKey(), scrollW);
        }
        return scrollW;
    }

    protected void updateCellValues() {
        removeCellCommentMark();
        removePopupButton();
        updateClassName();
    }

    protected void updateClassName() {
        element.setClassName(SheetWidget.toKey(col, row) + " cell " + cellStyle);
    }

    public String getCellStyle() {
        return cellStyle;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value, String cellStyle, boolean needsMeasure) {
        if (!this.cellStyle.equals(cellStyle)) {
            this.cellStyle = cellStyle;
            updateClassName();
        }
        this.needsMeasure = needsMeasure;
        setValue(value);
    }

    public void setValue(String value) {
        this.value = value;
        updateInnerText();

        if (cellCommentTriangle != null) {
            element.appendChild(cellCommentTriangle);
        }
        if (invalidFormulaTriangle != null) {
            element.appendChild(invalidFormulaTriangle);
        }
        if (popupButtonElement != null) {
            element.appendChild(popupButtonElement);
        }

        markAsOverflowDirty();
    }

    public void showPopupButton(Element popupButtonElement) {
        this.popupButtonElement = popupButtonElement;
        element.appendChild(popupButtonElement);
    }

    public void removePopupButton() {
        if (popupButtonElement != null) {
            popupButtonElement.removeFromParent();
            popupButtonElement = null;
        }
    }

    public void showCellCommentMark() {
        if (cellCommentTriangle == null) {
            cellCommentTriangle = Document.get().createDivElement();
            cellCommentTriangle.setClassName(CELL_COMMENT_TRIANGLE_CLASSNAME);
            element.appendChild(cellCommentTriangle);
        }
    }

    public void removeCellCommentMark() {
        if (cellCommentTriangle != null) {
            cellCommentTriangle.removeFromParent();
            cellCommentTriangle = null;
        }
    }

    public void showInvalidFormulaIndicator() {
        if (invalidFormulaTriangle == null) {
            invalidFormulaTriangle = Document.get().createDivElement();
            invalidFormulaTriangle.setClassName(CELL_INVALID_FORMULA_CLASSNAME);
            element.appendChild(invalidFormulaTriangle);
        }
    }

    public void removeInvalidFormulaIndicator() {
        if (invalidFormulaTriangle != null) {
            invalidFormulaTriangle.removeFromParent();
            invalidFormulaTriangle = null;
        }
    }

    public boolean isNeedsMeasure() {
        return needsMeasure;
    }

    /**
     * @param sizes
     * @param beginIndex
     *            1-based inclusive
     * @param endIndex
     *            1-based exclusive
     * @return
     */
    public int countSum(int[] sizes, int beginIndex, int endIndex) {
        if (sizes == null || sizes.length < endIndex - 1) {
            return 0;
        }
        int pos = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            pos += sizes[i - 1];
        }
        return pos;
    }

    public boolean isOverflowDirty() {
        return value != null && !value.isEmpty() && overflowDirty;
    }

    public void markAsOverflowDirty() {
        overflowDirty = true;
    }

    public boolean hasValue() {
        return value != null && !value.isEmpty();
    }

    private CellValueStyleKey getUniqueKey() {
        return new CellValueStyleKey(value, cellStyle);
    }

    static class CellValueStyleKey {
        private String value;

        private String cellStyle;

        public CellValueStyleKey(String value, String cellStyle) {
            super();
            this.value = value;
            this.cellStyle = cellStyle;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, cellStyle);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof CellValueStyleKey)) {
                return false;
            }
            CellValueStyleKey other = (CellValueStyleKey) obj;
            return Objects.equals(value, other.value)
                    && Objects.equals(cellStyle, other.cellStyle);
        }

    }

}
