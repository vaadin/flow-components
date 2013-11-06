package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.Util;

public class SelectionWidget extends Widget {

    private final DivElement top = Document.get().createDivElement();
    private final DivElement left = Document.get().createDivElement();
    private final DivElement right = Document.get().createDivElement();
    private final DivElement bottom = Document.get().createDivElement();
    private final DivElement corner = Document.get().createDivElement();
    private final DivElement paint = Document.get().createDivElement();

    private final DivElement root = Document.get().createDivElement();

    private int col1;
    private int row1;
    private int col2;
    private int row2;

    private int colEdgeIndex;
    private int rowEdgeIndex;
    private String paintColClassName = "s-paint-empty";
    private String paintRowClassName = "s-paint-empty";

    private boolean paintMode;
    private boolean extraInsideSelection = false;;

    private final SheetHandler handler;
    private int cornerX;
    private int cornerY;
    private int origX;
    private int origY;

    private int height;
    private int width;

    public SelectionWidget(SheetHandler actionHandler) {
        handler = actionHandler;

        initDOM();
        initListeners();

        setVisible(false);
    }

    public int getRow1() {
        return row1;
    }

    public int getRow2() {
        return row2;
    }

    public int getCol1() {
        return col1;
    }

    public int getCol2() {
        return col2;
    }

    public void setPosition(int col1, int col2, int row1, int row2) {
        root.removeClassName(SheetWidget.toKey(this.col1, this.row1));
        this.col1 = col1;
        this.row1 = row1;
        this.col2 = col2;
        this.row2 = row2;
        root.addClassName(SheetWidget.toKey(col1, row1));
    }

    public void setWidth(int width) {
        top.getStyle().setWidth(width + 2, Unit.PX);
        bottom.getStyle().setWidth(width + 2, Unit.PX);
        this.width = width;
    }

    public void setHeight(float f) {
        left.getStyle().setHeight(f + 2, Unit.PT);
        right.getStyle().setHeight(f + 2, Unit.PT);
        height = (int) f;
    }

    @Override
    public void setWidth(String width) {

    }

    @Override
    public void setHeight(String height) {

    }

    private void initDOM() {
        root.setClassName("sheet-selection");
        root.addClassName("rh");
        root.addClassName("ch");

        top.setClassName("s-top");
        left.setClassName("s-left");
        right.setClassName("s-right");
        bottom.setClassName("s-bottom");
        corner.setClassName("s-corner");
        paint.setClassName("s-paint");
        paint.addClassName("rh");
        paint.addClassName("ch");

        paint.getStyle().setVisibility(Visibility.HIDDEN);
        paint.getStyle().setWidth(0, Unit.PX);
        paint.getStyle().setHeight(0, Unit.PX);

        top.appendChild(left);
        top.appendChild(right);
        left.appendChild(bottom);
        right.appendChild(corner);
        root.appendChild(top);

        setElement(root);
    }

    public void setSpreadsheetElement(Element element) {
        element.appendChild(root);
        element.appendChild(paint);
    }

    private void initListeners() {
        Event.sinkEvents(root, Event.ONMOUSEDOWN | Event.ONMOUSEMOVE
                | Event.ONMOUSEUP | Event.TOUCHEVENTS | Event.ONLOSECAPTURE);
        Event.setEventListener(root, new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {
                final Element target = DOM.eventGetTarget(event);
                final int type = event.getTypeInt();
                if (paintMode) {
                    onPaintEvent(event);
                    event.stopPropagation();
                } else if (type == Event.ONMOUSEDOWN) {
                    if (target.equals(corner)) {
                        onPaintEvent(event);
                        event.stopPropagation();
                    } else if (target.equals(top)) {
                    } else if (target.equals(left)) {
                    } else if (target.equals(right)) {
                    } else if (target.equals(bottom)) {
                    }
                    // TODO dragging the selection
                }
            }

        });
    }

    private void onPaintEvent(Event event) {
        if (!Util.isTouchEventOrLeftMouseButton(event)) {
            return;
        }

        switch (DOM.eventGetType(event)) {
        case Event.ONTOUCHSTART:
            if (event.getTouches().length() > 1) {
                return;
            }
        case Event.ONMOUSEDOWN:
            beginPaintingCells(event);
            break;
        case Event.ONMOUSEUP:
        case Event.ONTOUCHEND:
        case Event.ONTOUCHCANCEL:
            DOM.releaseCapture(getElement());
        case Event.ONLOSECAPTURE:
            stopPaintingCells(event);
            break;
        case Event.ONMOUSEMOVE:
        case Event.ONTOUCHMOVE:
            paintCells(event);
            break;
        default:
            break;
        }
    }

    private void beginPaintingCells(Event event) {
        paint.getStyle().setVisibility(Visibility.VISIBLE);
        paintMode = true;
        origX = DOM.getAbsoluteLeft(getElement());
        origY = DOM.getAbsoluteTop(getElement());
        cornerX = origX + width;
        cornerY = origY + height;
        DOM.setCapture(getElement());
        DOM.eventPreventDefault(event);
    }

    private void stopPaintingCells(Event event) {
        paint.getStyle().setVisibility(Visibility.HIDDEN);
        paintMode = false;
        paint.getStyle().setWidth(0, Unit.PX);
        paint.getStyle().setHeight(0, Unit.PX);
        int c1, c2, r1, r2;
        if ((colEdgeIndex >= col1 && colEdgeIndex <= col2)
                && (rowEdgeIndex >= row1 && rowEdgeIndex <= row2)) {
            handler.onSelectionDecreasePainted(col1, col2, colEdgeIndex, row1,
                    row2, rowEdgeIndex);
        } else if (col2 + 1 < colEdgeIndex || colEdgeIndex < col1
                || row2 + 1 < rowEdgeIndex || rowEdgeIndex < row1) {
            c1 = colEdgeIndex < col1 ? colEdgeIndex : col1;
            c2 = colEdgeIndex > col2 ? colEdgeIndex - 1 : col2;
            r1 = rowEdgeIndex < row1 ? rowEdgeIndex : row1;
            r2 = rowEdgeIndex > row2 ? rowEdgeIndex - 1 : row2;
            handler.onSelectionIncreasePainted(c1, c2, r1, r2);
        }
    }

    private void paintCells(Event event) {

        // System.out.println("IS INSIDE SHEET: " + isCursorInsideSheet(event));
        final int clientX = Util.getTouchOrMouseClientX(event);
        final int clientY = Util.getTouchOrMouseClientY(event);
        // position in perspective to the top left
        int xMousePos = clientX - origX;
        int yMousePos = clientY - origY;

        final int[] colWidths = handler.getColWidths();
        final int colIndex = closestCellEdgeIndexToCursor(colWidths, col1,
                xMousePos);
        final float[] rowHeightsPT = handler.getRowHeights();
        final int[] rowHeightsPX = handler.getRowHeightsPX();
        final int rowIndex = closestCellEdgeIndexToCursor(rowHeightsPX, row1,
                yMousePos);

        int w;
        int h;

        paint.removeClassName(paintColClassName);
        paint.removeClassName(paintRowClassName);
        // case 1: "removing"
        if ((colIndex >= col1 && colIndex <= col2 + 1)
                && (rowIndex >= row1 && rowIndex <= row2 + 1)) {
            // the depending point is the right bottom corner
            xMousePos = Math.abs(cornerX - clientX);
            yMousePos = Math.abs(cornerY - clientY);
            // the axis with larger delta is used
            if (xMousePos >= yMousePos && (colIndex <= col2)) {
                // remove columns
                MergedRegion paintedRegion = MergedRegionUtil
                        .findIncreasingSelection(
                                handler.getMergedRegionContainer(), row1, row2,
                                colIndex, col2);
                colEdgeIndex = paintedRegion.col1;
                rowEdgeIndex = row1;
                w = countSum(colWidths, colEdgeIndex, col2 + 1);
                h = height;
            } else if (yMousePos > xMousePos && (rowIndex <= row2)) {
                // remove rows
                MergedRegion paintedRegion = MergedRegionUtil
                        .findIncreasingSelection(
                                handler.getMergedRegionContainer(), rowIndex,
                                row2, col1, col2);
                colEdgeIndex = col1;
                rowEdgeIndex = paintedRegion.row1;
                w = width;
                h = (int) countSum(rowHeightsPT, rowEdgeIndex, row2 + 1);
            } else {
                h = 0;
                w = 0;
            }
            if (!extraInsideSelection) {
                paint.addClassName("s-paint-inside");
                extraInsideSelection = true;
            }
            paintColClassName = "col" + colEdgeIndex;
            paintRowClassName = "row" + rowEdgeIndex;

        } else if ((rowIndex < row1 || rowIndex > (row2 + 1))
                || (colIndex < col1 || colIndex > (col2 + 1))) {
            if (extraInsideSelection) {
                paint.removeClassName("s-paint-inside");
                extraInsideSelection = false;
            }
            if (rowIndex > row2) {
                // see diff from old selection bottom
                yMousePos = clientY - cornerY;
            } else if (rowIndex >= row1 && yMousePos > 0) {
                yMousePos = 0;
            }
            if (colIndex > col2) {
                // see diff from old selection right
                xMousePos = clientX - cornerX;
            } else if (colIndex >= col1 && xMousePos > 0) {
                xMousePos = 0;
            }
            if (Math.abs(xMousePos) >= Math.abs(yMousePos)) {
                colEdgeIndex = colIndex;
                rowEdgeIndex = row1;
                paintRowClassName = "row" + rowEdgeIndex;
                h = height;
                // left or right
                if (xMousePos < 0) {
                    w = countSum(colWidths, colEdgeIndex, col1);
                    paintColClassName = "col" + colEdgeIndex;
                } else {
                    w = countSum(colWidths, col2 + 1, colEdgeIndex);
                    paintColClassName = "col" + (col2 + 1);
                }
            } else {
                colEdgeIndex = col1;
                rowEdgeIndex = rowIndex;
                paintColClassName = "col" + colEdgeIndex;
                w = width;
                // up or down
                if (yMousePos < 0) {
                    h = (int) countSum(rowHeightsPT, rowEdgeIndex, row1);
                    paintRowClassName = "row" + rowEdgeIndex;
                } else {
                    h = (int) countSum(rowHeightsPT, row2 + 1, rowEdgeIndex);
                    paintRowClassName = "row" + (row2 + 1);
                }
            }
        } else {
            paintColClassName = "s-paint-empty";
            paintRowClassName = "s-paint-empty";
            w = 0;
            h = 0;
        }
        // update position
        paint.addClassName(paintColClassName);
        paint.addClassName(paintRowClassName);
        // update size
        paint.getStyle().setWidth(w, Unit.PX);
        paint.getStyle().setHeight(h, Unit.PT);
    }

    /**
     * 
     * @param sizes
     * @param beginIndex
     *            1-based inclusive
     * @param endIndex
     *            1-based exclusive
     * @return
     */
    public int countSum(int[] sizes, int beginIndex, int endIndex) {
        int pos = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            pos += sizes[i - 1];
        }
        return pos;
    }

    /**
     * 
     * @param sizes
     * @param beginIndex
     *            1-based inclusive
     * @param endIndex
     *            1-based exclusive
     * @return
     */
    public float countSum(float[] sizes, int beginIndex, int endIndex) {
        int pos = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            pos += sizes[i - 1];
        }
        return pos;
    }

    @SuppressWarnings("unused")
    private boolean isCursorInsideSheet(Event event) {
        Element spreadsheetElement = root.getParentElement();
        if (event.getClientX() < spreadsheetElement.getAbsoluteLeft()
                || event.getClientY() < spreadsheetElement.getAbsoluteTop()) {
            return false;
        }

        if (event.getClientX() > spreadsheetElement.getAbsoluteRight()
                || event.getClientY() > spreadsheetElement.getAbsoluteBottom()) {
            return false;
        }

        return true;
    }

    /**
     * Returns index of the cell that has the left edge closest to the given
     * cursor position. Used for determinating how many rows/columns should be
     * painted when the mouse cursor is dragged somewhere.
     * 
     * @param cellSizes
     *            the sizes used to calculate
     * @param startIndex
     *            1-based index where the cursorPosition refers to
     * @param cursorPosition
     *            the position of the cursor relative to startIndex. Can be
     *            negative
     * @return
     */
    public int closestCellEdgeIndexToCursor(int cellSizes[], int startIndex,
            int cursorPosition) {
        int pos = 0;
        if (Math.abs(cursorPosition) > 200) {
            pos = 0;
        }
        if (cursorPosition < 0) {
            if (startIndex > 1) {
                while (startIndex > 1
                        && (pos - (cellSizes[startIndex - 2] / 2) > cursorPosition)) {
                    startIndex--;
                    pos -= cellSizes[startIndex - 1];
                }
                return startIndex;
            } else {
                return 1;
            }
        } else {
            if (startIndex < cellSizes.length) {
                while (startIndex <= cellSizes.length
                        && (pos + (cellSizes[startIndex - 1] / 2) < cursorPosition)) {
                    pos += cellSizes[startIndex - 1];
                    startIndex++;
                }
                return startIndex;
            } else {
                return cellSizes.length;
            }
        }
    }
}
