package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.NumberFormat;

public class Cell {

    public static final String CELL_COMMENT_TRIANGLE_CLASSNAME = "cell-comment-triangle";
    private static final int ZINDEXVALUE = 2;
    private final DivElement element;
    private DivElement cellCommentTriangle;
    private int col;
    private int row;
    private Element popupButtonElement;
    private String value;
    private Double numericValue = null;

    private static int NUMERIC_VALUE_NRDIGITS = 15;

    public Cell(int col, int row) {
        this.col = col;
        this.row = row;

        element = Document.get().createDivElement();
        updateCellValues();
    }

    public Cell(int col, int row, String html, Double numericValue) {
        this(col, row);
        value = html;
        this.numericValue = numericValue;
        updateInnerText();
        refreshWidth();
    }

    public DivElement getElement() {
        return element;
    }

    public void update(int col, int row, String html, Double numericValue) {
        this.col = col;
        this.row = row;
        value = html;

        updateInnerText();
        this.numericValue = numericValue;

        updateCellValues();
        refreshWidth();
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

    private void updateCellValues() {
        removeCellCommentMark();
        removePopupButton();
        element.setClassName(SheetWidget.toKey(col, row));
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
        updateCellValues();
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
        updateCellValues();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value, Double numericValue) {
        this.value = value;
        this.numericValue = numericValue;
        updateInnerText();

        if (cellCommentTriangle != null) {
            element.appendChild(cellCommentTriangle);
        }
        if (popupButtonElement != null) {
            element.appendChild(popupButtonElement);
        }

        refreshWidth();
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

    public void refreshWidth() {
        // width calculations are only applied for numeric cell types
        if (numericValue == null) {
            return;
        }

        // get cell width
        int width = element.getOffsetWidth();

        // override cell style's width. occupy only as much space as needed
        // (getOffsetWidth gives us the content width)

        String oldWidth = element.getStyle().getProperty("width");
        element.getStyle().setProperty("width", "auto");

        // select longest representation that fits (or ###)
        for (int n = NUMERIC_VALUE_NRDIGITS + 1; n >= 0; n--) {
            element.setInnerText(getNumberRepresentation(n));
            int contentWidth = element.getOffsetWidth();
            if (contentWidth <= width) {
                break;
            }
        }

        // restore width property
        if (oldWidth != null) {
            element.getStyle().setProperty("width", oldWidth);
        } else {
            element.getStyle().setProperty("width", "");
        }
    }

    private String getNumberRepresentation(int nrDigits) {
        if (nrDigits == NUMERIC_VALUE_NRDIGITS + 1) {
            return value;
        } else if (nrDigits == 0) {
            return "###";
        }

        StringBuilder format = new StringBuilder("0.");
        for (int i = 0; i < nrDigits; i++) {
            format.append('#');
        }
        format.append("E0");

        return NumberFormat.getFormat(format.toString()).format(numericValue);
    }

}