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

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

public class Cell {

    public static final String CELL_COMMENT_TRIANGLE_CLASSNAME = "cell-comment-triangle";
    private static final int ZINDEXVALUE = 2;
    private final DivElement element;
    private DivElement cellCommentTriangle;
    private int col;
    private int row;
    private Element popupButtonElement;
    private String value;
    private String cellStyle = "cs0";

    public Cell(int col, int row) {
        this.col = col;
        this.row = row;

        element = Document.get().createDivElement();
        updateCellValues();
    }

    public Cell(int col, int row, CellData cellData) {
        this.col = col;
        this.row = row;
        element = Document.get().createDivElement();
        if (cellData == null) {
            value = null;
        } else {
            value = cellData.value;
            cellStyle = cellData.cellStyle;
        }
        updateCellValues();
        updateInnerText();
    }

    public DivElement getElement() {
        return element;
    }

    public void update(int col, int row, String html, String cellStyle) {
        this.col = col;
        this.row = row;
        this.cellStyle = cellStyle;
        value = html;

        updateInnerText();

        updateCellValues();
    }

    public void update(int col, int row, CellData cellData) {
        if (cellData == null) {
            update(col, row, null, "cs0");
        } else {
            update(col, row, cellData.value, cellData.cellStyle);
        }
    }

    private void updateInnerText() {
        if (value == null || value.isEmpty()) {
            element.setInnerText("");
            element.getStyle().clearZIndex();
        } else {
            element.setInnerText(value);
            element.getStyle().setZIndex(ZINDEXVALUE);
        }
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

    public void setValue(String value, String cellStyle) {
        if (!this.cellStyle.equals(cellStyle)) {
            this.cellStyle = cellStyle;
            updateClassName();
        }
        setValue(value);
    }

    public void setValue(String value) {
        this.value = value;
        updateInnerText();

        if (cellCommentTriangle != null) {
            element.appendChild(cellCommentTriangle);
        }
        if (popupButtonElement != null) {
            element.appendChild(popupButtonElement);
        }
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

}