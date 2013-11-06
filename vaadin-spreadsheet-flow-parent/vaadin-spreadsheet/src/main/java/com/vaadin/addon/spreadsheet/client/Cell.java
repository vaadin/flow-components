package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Element;

public class Cell {

    public static final String CELL_COMMENT_TRIANGLE_CLASSNAME = "cell-comment-triangle";
    private final DivElement element;
    private DivElement cellCommentTriangle;
    private int col;
    private int row;
    private Element popupButtonElement;

    public Cell(int col, int row) {
        this.col = col;
        this.row = row;

        element = Document.get().createDivElement();
        updateCellValues();
    }

    public Cell(int col, int row, String html) {
        this(col, row);
        element.setInnerText(html);
    }

    public DivElement getElement() {
        return element;
    }

    public void update(int col, int row, String html) {
        this.col = col;
        this.row = row;
        if (html != null) {
            element.setInnerText(html);
        } else {
            element.setInnerText("");
        }
        updateCellValues();
    }

    private void updateCellValues() {
        removeCellCommentMark();
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
        return element.getInnerText();
    }

    public void setValue(String innerText) {
        if (innerText != null) {
            element.setInnerText(innerText);
        } else {
            element.setInnerText("");
        }
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
