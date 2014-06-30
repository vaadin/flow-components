package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.vaadin.client.ui.VLabel;
import com.vaadin.client.ui.VOverlay;

public class CellComment extends VOverlay {

    protected static final String COMMENT_OVERLAY_LINE_CLASSNAME = "comment-overlay-line";
    private static final String COMMENT_OVERLAY_CLASSNAME = "v-spreadsheet-comment-overlay";
    private static final String COMMENT_OVERLAY_LABEL_CLASSNAME = "comment-overlay-label";
    private static final String COMMENT_OVERLAY_SHADOW_CLASSNAME = COMMENT_OVERLAY_CLASSNAME
            + "-shadow";

    private final VLabel label;

    private final DivElement sheet;

    private final DivElement line;

    private Element cellElement;

    private String linePositionClassName;

    private int offsetWidth;
    private int offsetHeight;
    private int cellRow;
    private int cellCol;

    public CellComment(final SheetWidget owner, DivElement sheet) {
        this.sheet = sheet;
        line = Document.get().createDivElement();
        line.setClassName(COMMENT_OVERLAY_LINE_CLASSNAME);
        label = new VLabel();
        label.setStyleName(COMMENT_OVERLAY_LABEL_CLASSNAME);
        setStyleName(COMMENT_OVERLAY_CLASSNAME);
        setShadowStyle(COMMENT_OVERLAY_SHADOW_CLASSNAME);
        setOwner(owner);
        setAnimationEnabled(false);
        setShadowEnabled(true);
        setVisible(false);
        setZIndex(30);
        add(label);

        label.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                owner.onCellCommentFocus(CellComment.this);
            }
        });
    }

    public void bringForward() {
        setZIndex(35);
    }

    public void pushBack() {
        setZIndex(30);
    }

    @Override
    public void hide() {
        super.hide();
        line.removeFromParent();
    }

    public void setCommentText(String text) {
        label.setText(text);
    }

    public int getCol() {
        return cellCol;
    }

    public int getRow() {
        return cellRow;
    }

    public void show(Element cellElement, int row, int col) {
        this.cellElement = cellElement;
        cellRow = row;
        cellCol = col;
        setVisible(false);
        show();
        offsetHeight = getOffsetHeight();
        offsetWidth = getOffsetWidth();
        calculatePosition();
        setVisible(true);
    }

    public void showDependingToCellRightCorner(Element cellElement, int row,
            int col) {
        this.cellElement = cellElement;
        cellRow = row;
        cellCol = col;
        setVisible(false);
        show();
        offsetHeight = getOffsetHeight();
        offsetWidth = getOffsetWidth();
        refreshPositionAccordingToCellRightCorner();
    }

    public void refreshPositionAccordingToCellRightCorner() {
        // do not set overlay visible if the cell top-right corner is not
        // visible on sheet
        if (cellElement != null) {
            int cellRight = cellElement.getAbsoluteRight();
            int cellTop = cellElement.getAbsoluteTop();
            if (cellRight >= sheet.getAbsoluteLeft()
                    && cellRight < sheet.getAbsoluteRight()
                    && cellTop >= sheet.getAbsoluteTop()
                    && cellTop <= sheet.getAbsoluteBottom()) {
                calculatePosition();
                setVisible(true);
                if (!isShowing()) {
                    show();
                }
            } else {
                setVisible(false);
            }
        } else {
            setVisible(false);
            hide();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        line.getStyle().setVisibility(
                visible ? Visibility.VISIBLE : Visibility.HIDDEN);
    }

    private void calculatePosition() {
        // display the overlay on the right side of the
        // cell if possible, move it up/down if necessary. if there is not
        // enough space on the right side, move it to the left.
        int cellRight = cellElement.getAbsoluteRight();
        int cellTop = cellElement.getAbsoluteTop();
        int popupLeft = cellRight + 15;
        if (popupLeft + offsetWidth > sheet.getAbsoluteRight()) {
            // move to left side if it fits there
            int temp = cellElement.getAbsoluteLeft() - 15 - offsetWidth;
            if (sheet.getAbsoluteLeft() < temp) {
                popupLeft = temp;
            }
        }
        int popupTop = cellTop - 15;
        int sheetBottom = sheet.getAbsoluteBottom();
        if (popupTop + offsetHeight > sheetBottom) {
            // move upwards as much possible to make it fit
            popupTop -= (popupTop + offsetHeight - sheetBottom + 5);
            int sheetTop = sheet.getAbsoluteTop();
            if (popupTop < sheetTop) {
                popupTop = sheetTop;
            }
        } else if (popupTop < sheet.getAbsoluteTop()) {
            popupTop += (sheet.getAbsoluteTop() - popupTop);
        }
        setPopupPosition(popupLeft, popupTop);

        if (linePositionClassName != null) {
            line.removeClassName(linePositionClassName);
        }
        linePositionClassName = "col" + cellCol + " row" + cellRow;
        int a;
        int b = cellTop - popupTop;
        double deg;
        if (popupLeft > cellRight) {
            // draw the line to overlay top-left corner
            a = popupLeft - cellRight;
            if (b > 0) {
                deg = -1 * Math.toDegrees(Math.atan((double) b / (double) a));
            } else {
                b = Math.abs(b);
                deg = 0;
            }
        } else {
            // draw the line to overlay top-right corner
            a = cellRight - (popupLeft + offsetWidth);
            if (b > 0) {
                deg = -180 + Math.toDegrees(Math.atan((double) b / (double) a));
            } else {
                b = Math.abs(b);
                deg = -180;
            }
        }
        double c = Math.sqrt(a * a + b * b) + 1;
        line.getStyle().setWidth(c, Unit.PX);
        line.getStyle().setMarginLeft(cellElement.getOffsetWidth(), Unit.PX);
        line.getStyle().setProperty("transform", "rotate(" + deg + "deg)");
        line.getStyle().setProperty("msTransform", "rotate(" + deg + "deg)");
        line.getStyle()
                .setProperty("webkitTransform", "rotate(" + deg + "deg)");

        line.addClassName(linePositionClassName);
        sheet.appendChild(line);
    }
}