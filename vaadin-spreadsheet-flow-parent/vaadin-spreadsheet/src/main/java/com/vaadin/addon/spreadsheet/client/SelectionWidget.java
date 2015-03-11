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

import java.util.logging.Logger;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.MeasuredSize;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.ui.VOverlay;

public class SelectionWidget extends Composite {

    private class SelectionOutlineWidget extends Widget {

        private static final int eventBits = Event.ONMOUSEDOWN
                | Event.ONMOUSEMOVE | Event.ONMOUSEUP | Event.TOUCHEVENTS
                | Event.ONLOSECAPTURE;

        private final DivElement top = Document.get().createDivElement();
        private final DivElement left = Document.get().createDivElement();
        private final DivElement right = Document.get().createDivElement();
        private final DivElement bottom = Document.get().createDivElement();

        private final DivElement topSquare = Document.get().createDivElement();
        private final DivElement leftSquare = Document.get().createDivElement();
        private final DivElement rightSquare = Document.get()
                .createDivElement();
        private final DivElement bottomSquare = Document.get()
                .createDivElement();

        private final DivElement corner = Document.get().createDivElement();
        private final DivElement cornerTouchArea = Document.get()
                .createDivElement();
        private final DivElement root = Document.get().createDivElement();

        private int col1;
        private int row1;
        private int col2;
        private int row2;

        private int maxColumn;

        private int minRow;

        private int maxRow;

        private int minColumn;

        public SelectionOutlineWidget() {
            initDOM();
            initListeners();
        }

        void setSquaresVisible(boolean visible) {
            topSquare.getStyle().setVisibility(
                    visible ? Visibility.VISIBLE : Visibility.HIDDEN);
            leftSquare.getStyle().setVisibility(
                    visible ? Visibility.VISIBLE : Visibility.HIDDEN);
            rightSquare.getStyle().setVisibility(
                    visible ? Visibility.VISIBLE : Visibility.HIDDEN);
            bottomSquare.getStyle().setVisibility(
                    visible ? Visibility.VISIBLE : Visibility.HIDDEN);
        }

        private void initDOM() {
            root.setClassName("sheet-selection");

            if (touchMode) {
                // makes borders bigger with drag-symbol
                root.addClassName("touch");
            }

            top.setClassName("s-top");
            left.setClassName("s-left");
            right.setClassName("s-right");
            bottom.setClassName("s-bottom");
            corner.setClassName("s-corner");
            cornerTouchArea.setClassName("s-corner-touch");

            topSquare.setClassName("square");
            leftSquare.setClassName("square");
            rightSquare.setClassName("square");
            bottomSquare.setClassName("square");

            if (touchMode) {
                // append a large touch area for the corner, since it's too
                // small otherwise
                right.appendChild(cornerTouchArea);
                cornerTouchArea.appendChild(corner);
            } else {
                right.appendChild(corner);
            }

            top.appendChild(left);
            top.appendChild(right);
            left.appendChild(bottom);
            root.appendChild(top);

            if (touchMode) {
                top.appendChild(topSquare);
                left.appendChild(leftSquare);
                right.appendChild(rightSquare);
                bottom.appendChild(bottomSquare);
            }

            setElement(root);
        }

        private void initListeners() {
            // Widget is not attached in GWT terms, so can't call sinkEvents
            // directly
            Event.sinkEvents(root, eventBits);
            Event.setEventListener(root, new EventListener() {

                @Override
                public void onBrowserEvent(Event event) {
                    final Element target = DOM.eventGetTarget(event);
                    final int type = event.getTypeInt();

                    boolean touchEvent = type == Event.ONTOUCHSTART
                            || type == Event.ONTOUCHEND
                            || type == Event.ONTOUCHMOVE
                            || type == Event.ONTOUCHCANCEL;

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
                    } else if (touchEvent) {

                        if (type == Event.ONTOUCHEND
                                || type == Event.ONTOUCHCANCEL) {
                            Event.releaseCapture(root);
                            selectCellsStop(event);
                        } else if (target.equals(corner)
                                || target.equals(cornerTouchArea)) {

                            if (type == Event.ONTOUCHSTART) {
                                storeEventPos(event);
                                Event.setCapture(root);
                            } else {

                                // corners, resize selection
                                selectCells(event);
                            }
                        } else {
                            // handles, fill
                            if (fillMode) {
                                // same as dragging the corner in normal mode
                                onPaintEvent(event);
                            }
                        }
                        event.preventDefault();
                        event.stopPropagation();
                    }
                }

            });
        }

        public void setPosition(int col1, int col2, int row1, int row2) {

            root.removeClassName(SheetWidget.toKey(this.col1, this.row1));
            if (minColumn > 0 && col1 < minColumn) {
                col1 = minColumn;
                setLeftEdgeHidden(true);
            } else {
                setLeftEdgeHidden(false);
            }
            if (minRow > 0 && row1 < minRow) {
                row1 = minRow;
                setTopEdgeHidden(true);
            } else {
                setTopEdgeHidden(false);
            }
            if (maxRow > 0 && row2 > maxRow) {
                row2 = maxRow;
                setBottomEdgeHidden(true);
                setCornerHidden(true); // paint is hidden if right edge is
                                       // hidden
            } else {
                setBottomEdgeHidden(false);
                setCornerHidden(false);
            }
            if (maxColumn > 0 && maxColumn < col2) {
                col2 = maxColumn;
                setRightEdgeHidden(true);
            } else {
                setRightEdgeHidden(false);
            }
            this.col1 = col1;
            this.row1 = row1;
            this.col2 = col2;
            this.row2 = row2;
            if (col1 <= col2 && row1 <= row2) {
                root.addClassName(SheetWidget.toKey(this.col1, this.row1));
                setVisible(true);
                updateWidth();
                updateHeight();
            } else {
                setVisible(false);
            }
        }

        private void updateWidth() {
            int w = handler.getColWidthActual(col1);
            for (int i = col1 + 1; i <= col2; i++) {
                w += handler.getColWidthActual(i);
            }
            setWidth(w);
        }

        private void updateHeight() {
            int[] rowHeightsPX = handler.getRowHeightsPX();
            if (rowHeightsPX != null && rowHeightsPX.length != 0) {
                setHeight(countSum(handler.getRowHeightsPX(), row1, row2 + 1));
            }
        }

        private void setWidth(int width) {
            top.getStyle().setWidth(width + 1, Unit.PX);
            bottom.getStyle().setWidth(width + 1, Unit.PX);
        }

        private void setHeight(float height) {
            left.getStyle().setHeight(height, Unit.PX);
            right.getStyle().setHeight(height, Unit.PX);
        }

        public void setSheetElement(Element element) {
            element.appendChild(root);
            element.appendChild(paint);
        }

        public void setZIndex(int zIndex) {
            getElement().getStyle().setZIndex(zIndex);
        }

        protected void setLeftEdgeHidden(boolean hidden) {
            left.getStyle().setVisibility(
                    hidden ? Visibility.HIDDEN : Visibility.VISIBLE);
        }

        protected void setTopEdgeHidden(boolean hidden) {
            top.getStyle().setVisibility(
                    hidden ? Visibility.HIDDEN : Visibility.VISIBLE);
        }

        protected void setRightEdgeHidden(boolean hidden) {
            right.getStyle().setVisibility(
                    hidden ? Visibility.HIDDEN : Visibility.VISIBLE);
        }

        protected void setBottomEdgeHidden(boolean hidden) {
            bottom.getStyle().setVisibility(
                    hidden ? Visibility.HIDDEN : Visibility.VISIBLE);
        }

        protected void setCornerHidden(boolean hidden) {
            cornerTouchArea.getStyle().setDisplay(
                    hidden ? Display.NONE : Display.BLOCK);
            corner.getStyle().setDisplay(hidden ? Display.NONE : Display.BLOCK);
        }

        private void onPaintEvent(Event event) {
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
                paintCells(event);
                break;
            case Event.ONTOUCHMOVE:
                paintCells(event);
                event.preventDefault();
                break;
            default:
                break;
            }
        }

        public void remove() {
            root.removeFromParent();
            Event.sinkEvents(root, (~eventBits));
        }

        public void setLimits(int minRow, int maxRow, int minColumn,
                int maxColumn) {
            this.minRow = minRow;
            this.maxRow = maxRow;
            this.minColumn = minColumn;
            this.maxColumn = maxColumn;
        }
    }

    private final Logger debugConsole = Logger.getLogger("spreadsheet-logger");

    private final SelectionOutlineWidget bottomRight;
    private SelectionOutlineWidget bottomLeft;
    private SelectionOutlineWidget topRight;
    private SelectionOutlineWidget topLeft;

    private int col1;
    private int row1;
    private int col2;
    private int row2;

    private int colEdgeIndex;
    private int rowEdgeIndex;
    private int paintedRowIndex, prevPaintedRowIndex;
    private int paintedColIndex, prevPaintedColIndex;

    private boolean paintMode;
    private boolean touchMode;
    private boolean fillMode;
    private boolean extraInsideSelection = false;

    private final SheetHandler handler;
    private int cornerX;
    private int cornerY;
    private int origX;
    private int origY;

    private SheetWidget sheetWidget;

    private int horizontalSplitPosition;

    private int verticalSplitPosition;

    private final DivElement paint = Document.get().createDivElement();
    private int totalHeight;
    private int totalWidth;
    private String paintPaneClassName = "bottom-right";

    private int tempCol;
    private int tempRow;

    private int selectionStartCol;
    private int selectionStartRow;

    private VOverlay touchActions;

    private boolean dragging;

    private boolean decreaseSelection;

    private boolean increaseSelection;

    private boolean startCellTopLeft;

    private boolean startCellTopRight;

    private boolean startCellBottomLeft;

    private int clientX;

    private int clientY;

    private int deltaY;

    private int deltaX;

    private int shiftTempCol;

    private int shiftTempRow;

    private boolean scrollTimerRunning;

    private int initialScrollTop;

    private int initialScrollLeft;

    public SelectionWidget(SheetHandler actionHandler, SheetWidget sheetWidget) {
        handler = actionHandler;
        this.sheetWidget = sheetWidget;
        touchMode = sheetWidget.isTouchMode();
        bottomRight = new SelectionOutlineWidget();
        initWidget(bottomRight);

        bottomRight.setZIndex(8);
        bottomRight.addStyleName("bottom-right");
        setVisible(false);

        paint.setClassName("s-paint");
        paint.addClassName(paintPaneClassName);

        paint.getStyle().setVisibility(Visibility.HIDDEN);
        paint.getStyle().setWidth(0, Unit.PX);
        paint.getStyle().setHeight(0, Unit.PX);

        Element bottomRightPane = sheetWidget.getBottomRightPane();
        bottomRight.setSheetElement(bottomRightPane);
        bottomRightPane.appendChild(paint);
    }

    public void setHorizontalSplitPosition(int horizontalSplitPosition) {
        this.horizontalSplitPosition = horizontalSplitPosition;
        if (horizontalSplitPosition > 0 && bottomLeft == null) {
            bottomLeft = new SelectionOutlineWidget();
            bottomLeft.setSheetElement(sheetWidget.getBottomLeftPane());
            bottomLeft.setVisible(false);
            bottomLeft.setZIndex(18);
            bottomLeft.addStyleName("bottom-left");
        } else if (horizontalSplitPosition == 0 && bottomLeft != null) {
            bottomLeft.remove();
            bottomLeft = null;
        }
        updateTopLeft();
        updateLimits();
    }

    public void setVerticalSplitPosition(int verticalSplitPosition) {
        this.verticalSplitPosition = verticalSplitPosition;
        if (verticalSplitPosition > 0 && topRight == null) {
            topRight = new SelectionOutlineWidget();
            topRight.setSheetElement(sheetWidget.getTopRightPane());
            topRight.setVisible(false);
            topRight.setZIndex(18);
            topRight.addStyleName("top-right");
        } else if (verticalSplitPosition == 0 && topRight != null) {
            topRight.remove();
            topRight = null;
        }
        updateTopLeft();
        updateLimits();
    }

    private void updateTopLeft() {
        if (verticalSplitPosition > 0 && horizontalSplitPosition > 0
                && topLeft == null) {
            topLeft = new SelectionOutlineWidget();
            topLeft.setSheetElement(sheetWidget.getTopLeftPane());
            topLeft.setVisible(false);
            topLeft.setZIndex(28);
            topLeft.addStyleName("top-left");
        } else if (topLeft != null
                && (verticalSplitPosition == 0 || horizontalSplitPosition == 0)) {
            topLeft.remove();
            topLeft = null;
        }
    }

    private void updateLimits() {
        bottomRight.setLimits(verticalSplitPosition == 0 ? 0
                : verticalSplitPosition + 1, 0,
                horizontalSplitPosition == 0 ? 0 : horizontalSplitPosition + 1,
                0);
        if (bottomLeft != null) {
            bottomLeft.setLimits(verticalSplitPosition == 0 ? 0
                    : verticalSplitPosition + 1, 0, 0, horizontalSplitPosition);
        }
        if (topRight != null) {
            topRight.setLimits(0, verticalSplitPosition,
                    horizontalSplitPosition == 0 ? 0
                            : horizontalSplitPosition + 1, 0);
        }
        if (topLeft != null) {
            topLeft.setLimits(0, verticalSplitPosition, 0,
                    horizontalSplitPosition);
        }
    }

    private void setSelectionWidgetSquaresVisible(boolean visible) {
        if (touchMode) {
            bottomRight.setSquaresVisible(visible);
            if (bottomLeft != null) {
                bottomLeft.setSquaresVisible(visible);
            }
            if (topRight != null) {
                topRight.setSquaresVisible(visible);
            }
            if (topLeft != null) {
                topLeft.setSquaresVisible(visible);
            }
        }
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
        this.col1 = col1;
        this.row1 = row1;
        this.col2 = col2;
        this.row2 = row2;

        totalHeight = countSum(handler.getRowHeightsPX(), row1, row2 + 1);
        totalWidth = countSum(handler.getColWidths(), col1, col2 + 1);

        boolean hideCorner = totalWidth == 0 || totalHeight == 0;

        bottomRight.setPosition(col1, col2, row1, row2);
        bottomRight.setCornerHidden(hideCorner);
        if (verticalSplitPosition > 0 & horizontalSplitPosition > 0) {
            topLeft.setPosition(col1, col2, row1, row2);
            topLeft.setCornerHidden(hideCorner);
        }
        if (verticalSplitPosition > 0) {
            topRight.setPosition(col1, col2, row1, row2);
            topRight.setCornerHidden(hideCorner);
        }
        if (horizontalSplitPosition > 0) {
            bottomLeft.setPosition(col1, col2, row1, row2);
            bottomLeft.setCornerHidden(hideCorner);
        }

        if (fillMode) {
            setFillMode(false);
        }

        if (!dragging) {
            showTouchActions();
        }

    }

    private void showTouchActions() {
        if (touchMode) {
            // show touch actions in popup

            if (touchActions != null) {
                // remove old
                touchActions.hide();
            }

            touchActions = new VOverlay(true);
            touchActions.setOwner((Widget) sheetWidget.actionHandler);
            touchActions.addStyleName("v-contextmenu");

            final MenuBar m = new MenuBar();
            m.addItem(new SafeHtmlBuilder().appendEscaped("Fill").toSafeHtml(),
                    new ScheduledCommand() {

                        @Override
                        public void execute() {
                            setFillMode(true);
                            touchActions.hide();
                        }
                    });

            touchActions.add(m);

            touchActions.setPopupPositionAndShow(new PositionCallback() {

                @Override
                public void setPosition(int offsetWidth, int offsetHeight) {
                    // above top border
                    int top = bottomRight.top.getAbsoluteTop();
                    int left = bottomRight.top.getAbsoluteLeft();
                    int width = bottomRight.top.getClientWidth();

                    top -= offsetHeight + 5;
                    left += (width / 2) - (offsetWidth / 2);

                    Element parent = sheetWidget.getBottomRightPane();
                    int parentTop = parent.getAbsoluteTop();
                    debugConsole.warning(parent.getClassName() + " "
                            + parentTop + " " + top);
                    if (parentTop > top) {
                        // put under instead
                        top = bottomRight.bottom.getAbsoluteBottom() + 5;
                    }

                    touchActions.setPopupPosition(left, top);

                    // TODO check for room
                }
            });
            touchActions.show();
        }
    }

    @Override
    public void setWidth(String width) {

    }

    @Override
    public void setHeight(String height) {

    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (topLeft != null) {
            topLeft.setVisible(visible);
        }
        if (topRight != null) {
            topRight.setVisible(visible);
        }
        if (bottomLeft != null) {
            bottomLeft.setVisible(visible);
        }
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() || bottomLeft != null
                && bottomLeft.isVisible() || topRight != null
                && topRight.isVisible() || topLeft != null
                && topLeft.isVisible();
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
        if (sizes == null || sizes.length < endIndex - 1) {
            return 0;
        }
        int pos = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            pos += sizes[i - 1];
        }
        return pos;
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

        // TODO completely broken when zoomed in
        int pos = 0;
        if (cursorPosition < 0) {
            if (startIndex > 1) {
                while (startIndex > 1 && pos > cursorPosition) {
                    startIndex--;
                    pos -= cellSizes[startIndex - 1];
                }
                return startIndex;
            } else {
                return 1;
            }
        } else {
            if (startIndex < cellSizes.length) {
                while (startIndex <= cellSizes.length && pos < cursorPosition) {
                    pos += cellSizes[startIndex - 1];
                    startIndex++;
                }
                return startIndex;
            } else {
                return cellSizes.length;
            }
        }
    }

    private void beginPaintingCells(Event event) {
        initialScrollTop = sheetWidget.sheet.getScrollTop();
        initialScrollLeft = sheetWidget.sheet.getScrollLeft();
        startCellTopLeft = sheetWidget.isCellRenderedInTopLeftPane(col2, row2);
        startCellTopRight = sheetWidget
                .isCellRenderedInTopRightPane(col2, row2);
        startCellBottomLeft = sheetWidget.isCellRenderedInBottomLeftPane(col2,
                row2);
        clientX = WidgetUtil.getTouchOrMouseClientX(event);
        clientY = WidgetUtil.getTouchOrMouseClientY(event);
        shiftTempCol = col2;
        shiftTempRow = row2;
        colEdgeIndex = 0;
        rowEdgeIndex = 0;
        paintMode = true;
        storeEventPos(event);
        DOM.setCapture(getElement());
        event.preventDefault();

        sheetWidget.getElement().addClassName("selecting");
        setSelectionWidgetSquaresVisible(true);
    }

    private void storeEventPos(Event event) {
        Element element = getTopLeftMostElement();
        origX = element.getAbsoluteLeft();
        origY = element.getAbsoluteTop();
        cornerX = origX + totalWidth;
        cornerY = origY + totalHeight;

        selectionStartCol = col1;
        selectionStartRow = row1;
    }

    private Element getTopLeftMostElement() {
        if (sheetWidget.isCellRenderedInTopRightPane(col1, row1)) {
            return topRight.getElement();
        }
        if (sheetWidget.isCellRenderedInBottomLeftPane(col1, row1)) {
            return bottomLeft.getElement();
        }
        if (sheetWidget.isCellRenderedInTopLeftPane(col1, row1)) {
            return topLeft.getElement();
        }
        return bottomRight.getElement();
    }

    private void stopPaintingCells(Event event) {
        if (scrollTimerRunning) {
            stopScrollTimer();
        }

        paint.getStyle().setVisibility(Visibility.HIDDEN);
        paintMode = false;
        paint.getStyle().setWidth(0, Unit.PX);
        paint.getStyle().setHeight(0, Unit.PX);
        paint.getStyle().setVisibility(Visibility.HIDDEN);
        int c1, c2, r1, r2;
        if (decreaseSelection && (colEdgeIndex >= col1 && colEdgeIndex <= col2)
                && (rowEdgeIndex >= row1 && rowEdgeIndex <= row2)) {
            handler.onSelectionDecreasePainted(col1, col2, colEdgeIndex, row1,
                    row2, rowEdgeIndex);
        } else if (increaseSelection
                && (col2 + 1 < colEdgeIndex || colEdgeIndex < col1
                        || row2 + 1 < rowEdgeIndex || rowEdgeIndex < row1)) {
            c1 = colEdgeIndex < col1 ? colEdgeIndex : col1;
            c2 = colEdgeIndex > col2 ? colEdgeIndex - 1 : col2;
            r1 = rowEdgeIndex < row1 ? rowEdgeIndex : row1;
            r2 = rowEdgeIndex > row2 ? rowEdgeIndex - 1 : row2;
            if (c1 > 0 && r1 > 0) {
                handler.onSelectionIncreasePainted(c1, c2, r1, r2);
            }
        }

        sheetWidget.getElement().removeClassName("selecting");
        setSelectionWidgetSquaresVisible(false);
    }

    private void selectCells(Event event) {

        dragging = true;

        final int clientX = WidgetUtil.getTouchOrMouseClientX(event);
        final int clientY = WidgetUtil.getTouchOrMouseClientY(event);
        // position in perspective to the top left
        int xMousePos = clientX - origX;
        int yMousePos = clientY - origY;

        // touch offset; coords are made for mouse movement and need adjustment
        // on touch
        xMousePos -= 70;
        yMousePos -= 20;

        final int[] colWidths = handler.getColWidths();
        final int colIndex = closestCellEdgeIndexToCursor(colWidths,
                selectionStartCol, xMousePos);
        final int[] rowHeightsPX = handler.getRowHeightsPX();
        final int rowIndex = closestCellEdgeIndexToCursor(rowHeightsPX,
                selectionStartRow, yMousePos);

        tempCol = colIndex;
        tempRow = rowIndex;
        sheetWidget.getSheetHandler().onSelectingCellsWithDrag(colIndex,
                rowIndex);
    }

    private void selectCellsStop(Event event) {
        sheetWidget.getSheetHandler().onFinishedSelectingCellsWithDrag(
                sheetWidget.getSelectedCellColumn(), tempCol,
                sheetWidget.getSelectedCellRow(), tempRow);

        dragging = false;
        showTouchActions();
    }

    protected void setFillMode(boolean b) {
        fillMode = b;
        bottomRight.setCornerHidden(b);
        if (b) {
            bottomRight.addStyleName("fill");
            setSelectionWidgetSquaresVisible(true);
        } else {
            bottomRight.removeStyleName("fill");
            setSelectionWidgetSquaresVisible(false);
        }
    }

    private boolean checkScrollWhileSelecting(int y, int x) {
        int scrollPaneTop = sheetWidget.sheet.getAbsoluteTop();
        int scrollPaneLeft = sheetWidget.sheet.getAbsoluteLeft();
        int scrollPaneBottom = sheetWidget.sheet.getAbsoluteBottom();
        int scrollPaneRight = sheetWidget.sheet.getAbsoluteRight();

        clientX = x;
        clientY = y;

        if (y < scrollPaneTop) {
            deltaY = y - scrollPaneTop;
        } else if (y > scrollPaneBottom) {
            deltaY = y - scrollPaneBottom;
        } else {
            deltaY = 0;
        }

        if (x < scrollPaneLeft) {
            deltaX = x - scrollPaneLeft;
        } else if (x > scrollPaneRight) {
            deltaX = x - scrollPaneRight;
        } else {
            deltaX = 0;
        }

        // If we're crossing the top freeze pane border to the scroll area, the
        // bottom part must be scrolled all the way up.
        boolean scrolled = false;
        if (sheetWidget.sheet.getScrollTop() != 0) {
            boolean mouseOnTopSide = y < scrollPaneTop;
            if ((startCellTopLeft || startCellTopRight)
                    && sheetWidget.isCellRenderedInFrozenPane(shiftTempCol,
                            shiftTempRow) && !mouseOnTopSide) {
                sheetWidget.sheet.setScrollTop(0);
                sheetWidget.onSheetScroll(null);
                scrolled = true;
            }
        }

        // If we're crossing the left freeze pane border, the right-hand part
        // must be scrolled all the way to the left.
        if (sheetWidget.sheet.getScrollLeft() != 0) {
            boolean mouseOnLeftSide = x < scrollPaneLeft;
            if ((startCellTopLeft || startCellBottomLeft)
                    && sheetWidget.isCellRenderedInFrozenPane(shiftTempCol,
                            shiftTempRow) && !mouseOnLeftSide) {
                sheetWidget.sheet.setScrollLeft(0);
                sheetWidget.onSheetScroll(null);
                scrolled = true;
            }
        }

        if ((deltaY < 0 && sheetWidget.sheet.getScrollTop() != 0) || deltaY > 0
                || (deltaX < 0 && sheetWidget.sheet.getScrollLeft() != 0)
                || deltaX > 0) {
            startScrollTimer();
            scrolled = true;
        }

        // If the sheet was scrolled due to crossing freeze pane borders during
        // drag selection, the actual selection event will be handled on the
        // next mouse move event.
        if (scrolled) {
            return true;
        } else {
            stopScrollTimer();
            return false;
        }
    }

    private void startScrollTimer() {
        if (!scrollTimerRunning) {
            scrollTimerRunning = true;
            scrollTimer.scheduleRepeating(50);
        }
    }

    private void stopScrollTimer() {
        deltaX = 0;
        deltaY = 0;
        scrollTimer.cancel();
        scrollTimerRunning = false;
    }

    private void handleCellShiftOnScroll(int selectionPointX,
            int selectionPointY) {
        Element target = WidgetUtil.getElementFromPoint(selectionPointX,
                selectionPointY);
        if (target != null) {
            final String className = target.getClassName();
            sheetWidget.jsniUtil.parseColRow(className);
            int col = sheetWidget.jsniUtil.getParsedCol();
            int row = sheetWidget.jsniUtil.getParsedRow();
            if (col != 0 && row != 0) {
                int xMousePos = clientX - origX
                        + sheetWidget.sheet.getScrollLeft() - initialScrollLeft;
                int yMousePos = clientY - origY
                        + sheetWidget.sheet.getScrollTop() - initialScrollTop;
                final int[] colWidths = handler.getColWidths();
                final int[] rowHeightsPX = handler.getRowHeightsPX();

                updatePaintRectangle(selectionPointX, selectionPointY,
                        xMousePos, yMousePos, colWidths, rowHeightsPX, col, row);
            }
        }
    }

    private Timer scrollTimer = new Timer() {
        @Override
        public void run() {
            // Handle scrolling
            sheetWidget.sheet.setScrollTop(sheetWidget.sheet.getScrollTop()
                    + deltaY / 2);
            sheetWidget.sheet.setScrollLeft(sheetWidget.sheet.getScrollLeft()
                    + deltaX / 2);
            sheetWidget.onSheetScroll(null);

            // Determine selection point
            int selectionPointX = clientX;
            int selectionPointY = clientY;
            if (deltaX < 0) {
                selectionPointX = sheetWidget.sheet.getAbsoluteLeft()
                        + sheetWidget.TOP_LEFT_SELECTION_OFFSET;
            } else if (deltaX > 0) {
                selectionPointX = sheetWidget.sheet.getAbsoluteRight()
                        - sheetWidget.BOTTOM_RIGHT_SELECTION_OFFSET;
            }
            if (deltaY < 0) {
                selectionPointY = sheetWidget.sheet.getAbsoluteTop()
                        + sheetWidget.TOP_LEFT_SELECTION_OFFSET;
            } else if (deltaY > 0) {
                selectionPointY = sheetWidget.sheet.getAbsoluteBottom()
                        - sheetWidget.BOTTOM_RIGHT_SELECTION_OFFSET;
            }

            // Adjust selection point if we have reached scroll top
            if (deltaY != 0 && sheetWidget.sheet.getScrollTop() == 0) {
                MeasuredSize ms = new MeasuredSize();
                ms.measure(sheetWidget.spreadsheet);
                int minimumTop = sheetWidget.spreadsheet.getAbsoluteTop()
                        + ms.getPaddingTop()
                        + sheetWidget.TOP_LEFT_SELECTION_OFFSET;
                if (clientY > minimumTop) {
                    selectionPointY = clientY;
                } else {
                    selectionPointY = minimumTop;
                }
            }

            // Adjust selection point if we have reached scroll left
            if (deltaX != 0 && sheetWidget.sheet.getScrollLeft() == 0) {
                MeasuredSize ms = new MeasuredSize();
                ms.measure(sheetWidget.spreadsheet);
                int minimumLeft = sheetWidget.spreadsheet.getAbsoluteLeft()
                        + ms.getPaddingLeft()
                        + sheetWidget.TOP_LEFT_SELECTION_OFFSET;
                if (clientX > minimumLeft) {
                    selectionPointX = clientX;
                } else {
                    selectionPointX = minimumLeft;
                }
            }

            // Handle selection
            handleCellShiftOnScroll(selectionPointX, selectionPointY);
        }

    };

    private void paintCells(Event event) {
        paintedColIndex = 0;
        paintedRowIndex = 0;
        decreaseSelection = false;
        increaseSelection = false;
        final int clientX = WidgetUtil.getTouchOrMouseClientX(event);
        final int clientY = WidgetUtil.getTouchOrMouseClientY(event);

        // position in perspective to the top left
        int xMousePos = clientX - origX + sheetWidget.sheet.getScrollLeft()
                - initialScrollLeft;
        int yMousePos = clientY - origY + sheetWidget.sheet.getScrollTop()
                - initialScrollTop;

        final int[] colWidths = handler.getColWidths();
        final int[] rowHeightsPX = handler.getRowHeightsPX();
        int colIndex = closestCellEdgeIndexToCursor(colWidths, col1, xMousePos);
        int rowIndex = closestCellEdgeIndexToCursor(rowHeightsPX, row1,
                yMousePos);

        // If we're scrolling, do not paint anything
        if (checkScrollWhileSelecting(clientY, clientX)) {
            return;
        }

        updatePaintRectangle(clientX, clientY, xMousePos, yMousePos, colWidths,
                rowHeightsPX, colIndex, rowIndex);
    }

    private void updatePaintRectangle(final int clientX, final int clientY,
            int xMousePos, int yMousePos, final int[] colWidths,
            final int[] rowHeightsPX, int colIndex, int rowIndex) {
        int w = 0;
        int h = 0;

        // case 1: "removing"
        if ((colIndex >= col1 && colIndex <= col2 + 1)
                && (rowIndex >= row1 && rowIndex <= row2 + 1)) {
            // the depending point is the right bottom corner
            xMousePos = Math.abs(cornerX - clientX
                    + sheetWidget.sheet.getScrollLeft() - initialScrollLeft);
            yMousePos = Math.abs(cornerY - clientY
                    + sheetWidget.sheet.getScrollTop() - initialScrollTop);
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
                h = totalHeight;
                decreaseSelection = true;
            } else if (yMousePos > xMousePos && (rowIndex <= row2)) {
                // remove rows
                MergedRegion paintedRegion = MergedRegionUtil
                        .findIncreasingSelection(
                                handler.getMergedRegionContainer(), rowIndex,
                                row2, col1, col2);
                colEdgeIndex = col1;
                rowEdgeIndex = paintedRegion.row1;
                w = totalWidth;
                h = countSum(rowHeightsPX, rowEdgeIndex, row2 + 1);
                decreaseSelection = true;
            } else {
                h = 0;
                w = 0;
                colEdgeIndex = col2;
                rowEdgeIndex = row2;
            }
            if (!extraInsideSelection) {
                paint.addClassName("s-paint-inside");
                extraInsideSelection = true;
            }
            paintedColIndex = colEdgeIndex;
            paintedRowIndex = rowEdgeIndex;
        } else if ((rowIndex < row1 || rowIndex > row2)
                || (colIndex < col1 || colIndex > col2)) {
            if (extraInsideSelection) {
                paint.removeClassName("s-paint-inside");
                extraInsideSelection = false;
            }
            if (rowIndex > row2) {
                // see diff from old selection bottom
                yMousePos = clientY - cornerY
                        + sheetWidget.sheet.getScrollTop() - initialScrollTop;
            } else if (rowIndex >= row1) {
                yMousePos = 0;
            }
            if (colIndex > col2) {
                // see diff from old selection right
                xMousePos = clientX - cornerX
                        + sheetWidget.sheet.getScrollLeft() - initialScrollLeft;
            } else if (colIndex >= col1) {
                xMousePos = 0;
            }
            if (Math.abs(colIndex - col2) > Math.abs(rowIndex - row2)) {
                colEdgeIndex = colIndex;
                rowEdgeIndex = row1;
                paintedRowIndex = rowEdgeIndex;
                h = totalHeight;
                // left or right
                if (xMousePos < 0) {
                    w = countSum(colWidths, colEdgeIndex, col1);
                    paintedColIndex = colEdgeIndex;
                } else {
                    w = countSum(colWidths, col2 + 1, colEdgeIndex);
                    paintedColIndex = (col2 + 1);
                }
                increaseSelection = true;
            } else if (Math.abs(colIndex - col2) < Math.abs(rowIndex - row2)) {
                colEdgeIndex = col1;
                rowEdgeIndex = rowIndex;
                paintedColIndex = colEdgeIndex;
                w = totalWidth;
                // up or down
                if (yMousePos < 0) {
                    h = countSum(rowHeightsPX, rowEdgeIndex, row1);
                    paintedRowIndex = rowEdgeIndex;
                } else {
                    h = countSum(rowHeightsPX, row2 + 1, rowEdgeIndex);
                    paintedRowIndex = (row2 + 1);
                }
                increaseSelection = true;
            }
        }
        // update position
        Style style = paint.getStyle();
        if (paintedColIndex != 0
                && paintedRowIndex != 0
                && (prevPaintedColIndex != paintedColIndex || prevPaintedRowIndex != paintedRowIndex)
                && sheetWidget.isCellRendered(paintedColIndex, paintedRowIndex)) {
            Cell cell = sheetWidget.getCell(paintedColIndex, paintedRowIndex);
            int left = cell.getElement().getOffsetLeft();
            int top = cell.getElement().getOffsetTop();
            style.setLeft(left, Unit.PX);
            style.setTop(top, Unit.PX);
            paint.removeClassName(paintPaneClassName);
            paint.removeFromParent();
            if (sheetWidget.isCellRenderedInScrollPane(paintedColIndex,
                    paintedRowIndex)) {
                sheetWidget.getBottomRightPane().appendChild(paint);
                paintPaneClassName = "bottom-right";
            } else if (sheetWidget.isCellRenderedInBottomLeftPane(
                    paintedColIndex, paintedRowIndex)) {
                sheetWidget.getBottomLeftPane().appendChild(paint);
                paintPaneClassName = "bottom-left";
            } else if (sheetWidget.isCellRenderedInTopRightPane(
                    paintedColIndex, paintedRowIndex)) {
                sheetWidget.getTopRightPane().appendChild(paint);
                paintPaneClassName = "top-right";
            } else if (sheetWidget.isCellRenderedInTopLeftPane(paintedColIndex,
                    paintedRowIndex)) {
                sheetWidget.getTopLeftPane().appendChild(paint);
                paintPaneClassName = "top-left";
            }
            paint.addClassName(paintPaneClassName);
            prevPaintedColIndex = paintedColIndex;
            prevPaintedRowIndex = paintedRowIndex;
        }
        // update size
        if (w > 0 && h > 0) {
            style.setVisibility(Visibility.VISIBLE);
            style.setWidth(w + 1, Unit.PX);
            style.setHeight(h + 1, Unit.PX);
            setSelectionWidgetSquaresVisible(false);
        } else {
            style.setVisibility(Visibility.HIDDEN);
            style.setWidth(0, Unit.PX);
            style.setHeight(0, Unit.PX);
            setSelectionWidgetSquaresVisible(true);
        }
    }
}
