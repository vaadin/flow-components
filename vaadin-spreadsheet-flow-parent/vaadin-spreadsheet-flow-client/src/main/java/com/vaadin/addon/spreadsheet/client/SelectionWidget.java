package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
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

    private static final int ZINDEXVALUE = 2;

    private class SelectionOutlineWidget extends Widget {

        private static final int eventBits = Event.ONMOUSEDOWN
                | Event.ONMOUSEMOVE | Event.ONMOUSEUP | Event.TOUCHEVENTS
                | Event.ONLOSECAPTURE;

        private final DivElement root = Document.get().createDivElement();

        private final DivElement top = Document.get().createDivElement();
        private final DivElement left = Document.get().createDivElement();
        private final DivElement right = Document.get().createDivElement();
        private final DivElement bottom = Document.get().createDivElement();

        private final DivElement corner = Document.get().createDivElement();
        private final DivElement cornerTouchArea = Document.get()
                .createDivElement();

        private final DivElement topSquare = Document.get().createDivElement();
        private final DivElement leftSquare = Document.get().createDivElement();
        private final DivElement rightSquare = Document.get()
                .createDivElement();
        private final DivElement bottomSquare = Document.get()
                .createDivElement();
        private final DivElement topSquareTouchArea = Document.get()
                .createDivElement();
        private final DivElement leftSquareTouchArea = Document.get()
                .createDivElement();
        private final DivElement rightSquareTouchArea = Document.get()
                .createDivElement();
        private final DivElement bottomSquareTouchArea = Document.get()
                .createDivElement();

        private int col1;
        private int row1;
        private int col2;
        private int row2;

        private int maxColumn;

        private int minRow;

        private int maxRow;

        private int minColumn;

        private boolean leftEdgeHidden;

        private boolean topEdgeHidden;

        private boolean rightEdgeHidden;

        private boolean bottomEdgeHidden;

        private int width;

        private int height;

        public SelectionOutlineWidget() {
            initDOM();
            initListeners();
        }

        void setSquaresVisible(boolean top, boolean right, boolean bottom,
                boolean left) {
            topSquareTouchArea.getStyle()
                    .setVisibility(top && !topEdgeHidden ? Visibility.VISIBLE
                            : Visibility.HIDDEN);
            leftSquareTouchArea.getStyle()
                    .setVisibility(left && !leftEdgeHidden ? Visibility.VISIBLE
                            : Visibility.HIDDEN);
            rightSquareTouchArea.getStyle().setVisibility(
                    right && !rightEdgeHidden ? Visibility.VISIBLE
                            : Visibility.HIDDEN);
            bottomSquareTouchArea.getStyle().setVisibility(
                    bottom && !bottomEdgeHidden ? Visibility.VISIBLE
                            : Visibility.HIDDEN);
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

            topSquareTouchArea.setClassName("fill-touch-square");
            leftSquareTouchArea.setClassName("fill-touch-square");
            rightSquareTouchArea.setClassName("fill-touch-square");
            bottomSquareTouchArea.setClassName("fill-touch-square");

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
                top.appendChild(topSquareTouchArea);
                left.appendChild(leftSquareTouchArea);
                right.appendChild(rightSquareTouchArea);
                bottom.appendChild(bottomSquareTouchArea);
                topSquareTouchArea.appendChild(topSquare);
                leftSquareTouchArea.appendChild(leftSquare);
                rightSquareTouchArea.appendChild(rightSquare);
                bottomSquareTouchArea.appendChild(bottomSquare);
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
                        // TODO Implement dragging the selection
                    } else if (touchEvent) {

                        if (type == Event.ONTOUCHEND
                                || type == Event.ONTOUCHCANCEL) {
                            Event.releaseCapture(root);
                            stopSelectingCells(event);
                        } else if (target.equals(corner)
                                || target.equals(cornerTouchArea)) {

                            if (type == Event.ONTOUCHSTART) {
                                Event.setCapture(root);
                                beginSelectingCells(event);
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
            width = col2 - col1;
            height = row2 - row1;
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
            top.getStyle().setWidth(width + 1d, Unit.PX);
            bottom.getStyle().setWidth(width + 1d, Unit.PX);
        }

        private void setHeight(float height) {
            left.getStyle().setHeight(height, Unit.PX);
            right.getStyle().setHeight(height, Unit.PX);
        }

        public void setSheetElement(Element element) {
            element.appendChild(root);
        }

        public void setZIndex(int zIndex) {
            getElement().getStyle().setZIndex(zIndex);
        }

        protected void setLeftEdgeHidden(boolean hidden) {
            leftEdgeHidden = hidden;
            left.getStyle().setVisibility(
                    hidden ? Visibility.HIDDEN : Visibility.VISIBLE);
            leftSquareTouchArea.getStyle().setVisibility(
                    hidden ? Visibility.HIDDEN : Visibility.VISIBLE);
        }

        protected void setTopEdgeHidden(boolean hidden) {
            topEdgeHidden = hidden;
            top.getStyle().setVisibility(
                    hidden ? Visibility.HIDDEN : Visibility.VISIBLE);
            topSquareTouchArea.getStyle().setVisibility(
                    hidden ? Visibility.HIDDEN : Visibility.VISIBLE);
        }

        protected void setRightEdgeHidden(boolean hidden) {
            rightEdgeHidden = hidden;
            right.getStyle().setVisibility(
                    hidden ? Visibility.HIDDEN : Visibility.VISIBLE);
            rightSquareTouchArea.getStyle().setVisibility(
                    hidden ? Visibility.HIDDEN : Visibility.VISIBLE);
        }

        protected void setBottomEdgeHidden(boolean hidden) {
            bottomEdgeHidden = hidden;
            bottom.getStyle().setVisibility(
                    hidden ? Visibility.HIDDEN : Visibility.VISIBLE);
            bottomSquareTouchArea.getStyle().setVisibility(
                    hidden ? Visibility.HIDDEN : Visibility.VISIBLE);
        }

        protected void setCornerHidden(boolean hidden) {
            cornerTouchArea.getStyle()
                    .setDisplay(hidden ? Display.NONE : Display.BLOCK);
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

    private class PaintOutlineWidget extends Widget {

        private final DivElement root = Document.get().createDivElement();

        private final DivElement top = Document.get().createDivElement();
        private final DivElement left = Document.get().createDivElement();
        private final DivElement right = Document.get().createDivElement();
        private final DivElement bottom = Document.get().createDivElement();

        private int col1;
        private int row1;
        private int col2;
        private int row2;

        private int maxColumn;

        private int minRow;

        private int maxRow;

        private int minColumn;

        public PaintOutlineWidget() {
            root.setClassName("sheet-selection");
            root.addClassName("paintmode");
            top.setClassName("s-top");
            left.setClassName("s-left");
            right.setClassName("s-right");
            bottom.setClassName("s-bottom");
            top.appendChild(left);
            top.appendChild(right);
            left.appendChild(bottom);
            root.appendChild(top);
            setElement(root);
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
            } else {
                setBottomEdgeHidden(false);
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
            root.getStyle().setWidth(width + 1d, Unit.PX);
            top.getStyle().setWidth(width + 1d, Unit.PX);
            bottom.getStyle().setWidth(width + 1d, Unit.PX);
        }

        private void setHeight(float height) {
            root.getStyle().setHeight(height, Unit.PX);
            left.getStyle().setHeight(height, Unit.PX);
            right.getStyle().setHeight(height, Unit.PX);
        }

        public void setSheetElement(Element element) {
            element.appendChild(root);
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

        public void remove() {
            root.removeFromParent();
        }

        public void setLimits(int minRow, int maxRow, int minColumn,
                int maxColumn) {
            this.minRow = minRow;
            this.maxRow = maxRow;
            this.minColumn = minColumn;
            this.maxColumn = maxColumn;
        }

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            if (visible) {
                getElement().getStyle().clearOverflow();
            } else {
                getElement().getStyle().setOverflow(Overflow.HIDDEN);
            }
        }
    }

    private final SelectionOutlineWidget bottomRight;
    private SelectionOutlineWidget bottomLeft;
    private SelectionOutlineWidget topRight;
    private SelectionOutlineWidget topLeft;

    private int col1;
    private int row1;
    private int col2;
    private int row2;

    private final PaintOutlineWidget paintBottomRight;
    private PaintOutlineWidget paintBottomLeft;
    private PaintOutlineWidget paintTopRight;
    private PaintOutlineWidget paintTopLeft;

    private boolean paintMode;
    private boolean touchMode;
    private boolean fillMode;

    private final SheetHandler handler;
    private int origX;
    private int origY;

    private SheetWidget sheetWidget;

    private int horizontalSplitPosition;
    private int verticalSplitPosition;

    private int totalHeight;
    private int totalWidth;

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

    private boolean scrollTimerRunning;

    private int paintcol1;
    private int paintrow1;
    private int paintcol2;
    private int paintrow2;

    private boolean crossedLeft;
    private boolean crossedDown;

    public SelectionWidget(SheetHandler actionHandler,
            SheetWidget sheetWidget) {
        handler = actionHandler;
        this.sheetWidget = sheetWidget;
        touchMode = sheetWidget.isTouchMode();
        bottomRight = new SelectionOutlineWidget();
        initWidget(bottomRight);

        bottomRight.setZIndex(ZINDEXVALUE);
        bottomRight.addStyleName("bottom-right");
        setVisible(false);

        paintBottomRight = new PaintOutlineWidget();
        paintBottomRight.addStyleName("bottom-right");
        paintBottomRight.setZIndex(ZINDEXVALUE);

        Element bottomRightPane = sheetWidget.getBottomRightPane();
        bottomRight.setSheetElement(bottomRightPane);
        paintBottomRight.setSheetElement(bottomRightPane);
    }

    public void setHorizontalSplitPosition(int horizontalSplitPosition) {
        this.horizontalSplitPosition = horizontalSplitPosition;
        if (horizontalSplitPosition > 0 && bottomLeft == null) {
            bottomLeft = new SelectionOutlineWidget();
            bottomLeft.setSheetElement(sheetWidget.getBottomLeftPane());
            bottomLeft.setVisible(false);
            bottomLeft.setZIndex(ZINDEXVALUE);
            bottomLeft.addStyleName("bottom-left");
            paintBottomLeft = new PaintOutlineWidget();
            paintBottomLeft.setSheetElement(sheetWidget.getBottomLeftPane());
            paintBottomLeft.setVisible(false);
            paintBottomLeft.setZIndex(ZINDEXVALUE);
            paintBottomLeft.addStyleName("bottom-left");
        } else if (horizontalSplitPosition == 0 && bottomLeft != null) {
            bottomLeft.remove();
            bottomLeft = null;
            paintBottomLeft.remove();
            paintBottomLeft = null;
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
            topRight.setZIndex(ZINDEXVALUE);
            topRight.addStyleName("top-right");
            paintTopRight = new PaintOutlineWidget();
            paintTopRight.setSheetElement(sheetWidget.getTopRightPane());
            paintTopRight.setVisible(false);
            paintTopRight.setZIndex(ZINDEXVALUE);
            paintTopRight.addStyleName("top-left");
        } else if (verticalSplitPosition == 0 && topRight != null) {
            topRight.remove();
            topRight = null;
            paintTopRight.remove();
            paintTopRight = null;
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
            topLeft.setZIndex(ZINDEXVALUE);
            topLeft.addStyleName("top-left");
            paintTopLeft = new PaintOutlineWidget();
            paintTopLeft.setSheetElement(sheetWidget.getTopLeftPane());
            paintTopLeft.setVisible(false);
            paintTopLeft.setZIndex(ZINDEXVALUE);
            paintTopLeft.addStyleName("top-left");
        } else if (topLeft != null && (verticalSplitPosition == 0
                || horizontalSplitPosition == 0)) {
            topLeft.remove();
            topLeft = null;
            paintTopLeft.remove();
            paintTopLeft = null;
        }
    }

    private void updateLimits() {
        bottomRight.setLimits(
                verticalSplitPosition == 0 ? 0 : verticalSplitPosition + 1, 0,
                horizontalSplitPosition == 0 ? 0 : horizontalSplitPosition + 1,
                0);
        if (bottomLeft != null) {
            bottomLeft.setLimits(
                    verticalSplitPosition == 0 ? 0 : verticalSplitPosition + 1,
                    0, 0, horizontalSplitPosition);
        }
        if (topRight != null) {
            topRight.setLimits(0, verticalSplitPosition,
                    horizontalSplitPosition == 0 ? 0
                            : horizontalSplitPosition + 1,
                    0);
        }
        if (topLeft != null) {
            topLeft.setLimits(0, verticalSplitPosition, 0,
                    horizontalSplitPosition);
        }
        paintBottomRight.setLimits(
                verticalSplitPosition == 0 ? 0 : verticalSplitPosition + 1, 0,
                horizontalSplitPosition == 0 ? 0 : horizontalSplitPosition + 1,
                0);
        if (paintBottomLeft != null) {
            paintBottomLeft.setLimits(
                    verticalSplitPosition == 0 ? 0 : verticalSplitPosition + 1,
                    0, 0, horizontalSplitPosition);
        }
        if (paintTopRight != null) {
            paintTopRight.setLimits(0, verticalSplitPosition,
                    horizontalSplitPosition == 0 ? 0
                            : horizontalSplitPosition + 1,
                    0);
        }
        if (paintTopLeft != null) {
            paintTopLeft.setLimits(0, verticalSplitPosition, 0,
                    horizontalSplitPosition);
        }
    }

    private void setSelectionWidgetSquaresVisible(boolean visible) {
        if (touchMode) {
            boolean top, right, bottom, left;
            bottom = bottomLeft != null && bottomLeft.width > bottomRight.width
                    ? false
                    : visible;
            right = topRight != null && topRight.height > bottomRight.height
                    ? false
                    : visible;
            bottomRight.setSquaresVisible(bottom, right, bottom, right);
            if (bottomLeft != null) {
                bottom = bottomRight != null
                        && bottomRight.width >= bottomLeft.width ? false
                                : visible;
                left = topLeft != null && topLeft.height > bottomLeft.height
                        ? false
                        : visible;
                bottomLeft.setSquaresVisible(bottom, left, bottom, left);
            }
            if (topRight != null) {
                top = topLeft != null && topLeft.width > topRight.width ? false
                        : visible;
                right = bottomRight != null
                        && bottomRight.height >= topRight.height ? false
                                : visible;
                topRight.setSquaresVisible(top, right, top, right);
            }
            if (topLeft != null) {
                top = topRight != null && topRight.width >= topLeft.width
                        ? false
                        : visible;
                left = bottomLeft != null && bottomLeft.height >= topLeft.height
                        ? false
                        : visible;
                topLeft.setSquaresVisible(top, left, top, left);
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

        boolean hiddenCellSelected = totalWidth == 0 || totalHeight == 0;

        bottomRight.setPosition(col1, col2, row1, row2);
        if (hiddenCellSelected) {
            bottomRight.setCornerHidden(true);
        }
        if (verticalSplitPosition > 0 & horizontalSplitPosition > 0) {
            topLeft.setPosition(col1, col2, row1, row2);
            if (hiddenCellSelected) {
                topLeft.setCornerHidden(true);
            }
        }
        if (verticalSplitPosition > 0) {
            topRight.setPosition(col1, col2, row1, row2);
            if (hiddenCellSelected) {
                topRight.setCornerHidden(true);
            }
        }
        if (horizontalSplitPosition > 0) {
            bottomLeft.setPosition(col1, col2, row1, row2);
            if (hiddenCellSelected) {
                bottomLeft.setCornerHidden(true);
            }
        }

        if (fillMode) {
            setFillMode(false);
        }

        if (!dragging) {
            showTouchActions();
        }
    }

    public void setPaintPosition(int col1, int col2, int row1, int row2) {
        paintcol1 = col1;
        paintrow1 = row1;
        paintcol2 = col2;
        paintrow2 = row2;

        paintBottomRight.setPosition(col1, col2, row1, row2);
        if (verticalSplitPosition > 0 & horizontalSplitPosition > 0) {
            paintTopLeft.setPosition(col1, col2, row1, row2);
        }
        if (verticalSplitPosition > 0) {
            paintTopRight.setPosition(col1, col2, row1, row2);
        }
        if (horizontalSplitPosition > 0) {
            paintBottomLeft.setPosition(col1, col2, row1, row2);
        }
    }

    private void showTouchActions() {
        if (touchMode) {
            // show touch actions in popup

            if (touchActions != null) {
                // remove old
                touchActions.hide();
            }

            touchActions = new SpreadsheetOverlay(true);
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

            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    touchActions
                            .setPopupPositionAndShow(new PositionCallback() {

                                @Override
                                public void setPosition(int offsetWidth,
                                        int offsetHeight) {
                                    // above top border
                                    int top = 0;
                                    int left = 0;
                                    int bottom = 0;
                                    int width = 0;
                                    int parentTop = 0;
                                    if (topRight != null
                                            && topRight.isVisible()) {
                                        top = topRight.top.getAbsoluteTop();
                                        left = topRight.top.getAbsoluteLeft();
                                        width = topRight.top.getClientWidth();
                                        bottom = topRight.bottom
                                                .getAbsoluteBottom() + 5;
                                        if (topLeft.isVisible()) {
                                            width += topLeft.top
                                                    .getClientWidth();
                                        }
                                        if (bottomRight.isVisible()) {
                                            bottom = bottomRight.bottom
                                                    .getAbsoluteBottom() + 5;
                                        }
                                    } else if (topLeft != null
                                            && topLeft.isVisible()) {
                                        top = topLeft.top.getAbsoluteTop();
                                        left = topLeft.top.getAbsoluteLeft();
                                        width = topLeft.top.getClientWidth();
                                        bottom = topLeft.bottom
                                                .getAbsoluteBottom() + 5;
                                        if (bottomLeft.isVisible()) {
                                            bottom = bottomLeft.bottom
                                                    .getAbsoluteBottom() + 5;
                                        }
                                    } else if (bottomLeft != null
                                            && bottomLeft.isVisible()) {
                                        top = bottomLeft.top.getAbsoluteTop();
                                        left = bottomLeft.top.getAbsoluteLeft();
                                        width = bottomLeft.top.getClientWidth();
                                        bottom = bottomLeft.bottom
                                                .getAbsoluteBottom() + 5;
                                        if (bottomRight.isVisible()) {
                                            width += bottomRight.top
                                                    .getClientWidth();
                                        }
                                    } else {
                                        top = bottomRight.top.getAbsoluteTop();
                                        left = bottomRight.top
                                                .getAbsoluteLeft();
                                        width = bottomRight.top
                                                .getClientWidth();
                                        bottom = bottomRight.bottom
                                                .getAbsoluteBottom() + 5;
                                    }
                                    if (width > sheetWidget.getElement()
                                            .getClientWidth()) {
                                        width = sheetWidget.getElement()
                                                .getClientWidth();
                                    }

                                    if (sheetWidget.hasFrozenRows()) {
                                        parentTop = sheetWidget
                                                .getTopRightPane()
                                                .getAbsoluteTop();
                                    } else {
                                        parentTop = sheetWidget
                                                .getBottomRightPane()
                                                .getAbsoluteTop();
                                    }

                                    top -= offsetHeight + 5;
                                    left += (width / 2) - (offsetWidth / 2);

                                    if (parentTop > top) {
                                        // put under instead
                                        top = bottom + 5;
                                    }
                                    touchActions.setPopupPosition(left, top);

                                    // TODO check for room
                                }
                            });
                    touchActions.show();
                }
            });
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
        if (visible == isVisible()) {
            return;
        }
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

    public void setPaintVisible(boolean visible) {
        if (visible == isPaintVisible()) {
            return;
        }
        paintBottomRight.setVisible(visible);
        if (paintTopLeft != null) {
            paintTopLeft.setVisible(visible);
        }
        if (paintTopRight != null) {
            paintTopRight.setVisible(visible);
        }
        if (paintBottomLeft != null) {
            paintBottomLeft.setVisible(visible);
        }
        setSelectionWidgetSquaresVisible(!visible);
    }

    private boolean isPaintVisible() {
        return paintBottomRight.isVisible()
                || paintBottomLeft != null && paintBottomLeft.isVisible()
                || paintTopRight != null && paintTopRight.isVisible()
                || paintTopLeft != null && paintTopLeft.isVisible();
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() || bottomLeft != null && bottomLeft.isVisible()
                || topRight != null && topRight.isVisible()
                || topLeft != null && topLeft.isVisible();
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
     * cursor position. Used for determining how many rows/columns should be
     * painted when the mouse cursor is dragged somewhere.
     *
     * @param cellSizes
     *            the sizes used to calculate
     * @param startIndex
     *            1-based index where the cursorPosition refers to
     * @param cursorPosition
     *            the position of the cursor relative to startIndex. Can be
     *            negative
     * @param forSelection
     *            true if the result is used for touch selection, false if it's
     *            used for painting cells
     * @return
     */
    public int closestCellEdgeIndexToCursor(int cellSizes[], int startIndex,
            int cursorPosition, boolean forSelection) {
        int result = 0;
        int pos = 0;
        if (cursorPosition < 0) {
            if (startIndex > 1) {
                while (startIndex > 1 && pos > cursorPosition) {
                    startIndex--;
                    pos -= cellSizes[startIndex - 1];
                }
                if (forSelection && pos < cursorPosition) {
                    startIndex++;
                }
                result = startIndex;
            } else {
                result = 1;
            }
        } else {
            if (startIndex < cellSizes.length) {
                while (startIndex <= cellSizes.length && pos < cursorPosition) {
                    pos += cellSizes[startIndex - 1];
                    startIndex++;
                }
                result = startIndex;
            } else {
                result = cellSizes.length;
            }
        }
        return forSelection ? result : result - 1;
    }

    private void beginPaintingCells(Event event) {
        startCellTopLeft = sheetWidget.isCellRenderedInTopLeftPane(col2, row2);
        startCellTopRight = sheetWidget.isCellRenderedInTopRightPane(col2,
                row2);
        startCellBottomLeft = sheetWidget.isCellRenderedInBottomLeftPane(col2,
                row2);
        crossedDown = !startCellTopLeft && !startCellTopRight;
        crossedLeft = !startCellTopLeft && !startCellBottomLeft;
        initialScrollLeft = sheetWidget.sheet.getScrollLeft();
        initialScrollTop = sheetWidget.sheet.getScrollTop();
        clientX = SpreadsheetWidget.getTouchOrMouseClientX(event);
        clientY = SpreadsheetWidget.getTouchOrMouseClientY(event);
        tempCol = col2;
        tempRow = row2;
        paintMode = true;
        decreaseSelection = false;
        increaseSelection = false;
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
        paintMode = false;
        setPaintVisible(false);

        if (scrollTimerRunning) {
            stopScrollTimer();
        }

        if (decreaseSelection) {
            handler.onSelectionDecreasePainted(paintcol1, paintrow1);
        } else if (increaseSelection) {
            int c1 = Math.min(col1, paintcol1);
            int c2 = Math.max(col2, paintcol2);
            int r1 = Math.min(row1, paintrow1);
            int r2 = Math.max(row2, paintrow2);
            if (c1 <= c2 && r1 <= r2) {
                handler.onSelectionIncreasePainted(c1, c2, r1, r2);
            }
        }

        sheetWidget.getElement().removeClassName("selecting");
        setSelectionWidgetSquaresVisible(false);
    }

    private void beginSelectingCells(Event event) {
        startCellTopLeft = sheetWidget.isCellRenderedInTopLeftPane(col2, row2);
        startCellTopRight = sheetWidget.isCellRenderedInTopRightPane(col2,
                row2);
        startCellBottomLeft = sheetWidget.isCellRenderedInBottomLeftPane(col2,
                row2);
        crossedDown = !startCellTopLeft && !startCellTopRight;
        crossedLeft = !startCellTopLeft && !startCellBottomLeft;
        initialScrollLeft = sheetWidget.sheet.getScrollLeft();
        initialScrollTop = sheetWidget.sheet.getScrollTop();
        clientX = SpreadsheetWidget.getTouchOrMouseClientX(event);
        clientY = SpreadsheetWidget.getTouchOrMouseClientY(event);
        tempCol = col2;
        tempRow = row2;
        storeEventPos(event);
    }

    private void selectCells(Event event) {
        dragging = true;

        final int clientX = SpreadsheetWidget.getTouchOrMouseClientX(event);
        final int clientY = SpreadsheetWidget.getTouchOrMouseClientY(event);

        // If we're scrolling, do not paint anything
        if (checkScrollWhilePainting(clientY, clientX)) {
            return;
        }

        // position in perspective to the top left
        int xMousePos = clientX - origX + sheetWidget.sheet.getScrollLeft()
                - initialScrollLeft;
        int yMousePos = clientY - origY + sheetWidget.sheet.getScrollTop()
                - initialScrollTop;

        // touch offset; coords are made for mouse movement and need adjustment
        // on touch
        xMousePos -= 70;
        yMousePos -= 20;

        final int[] colWidths = handler.getColWidths();
        final int[] rowHeightsPX = handler.getRowHeightsPX();
        tempCol = closestCellEdgeIndexToCursor(colWidths, selectionStartCol,
                xMousePos, true);
        tempRow = closestCellEdgeIndexToCursor(rowHeightsPX, selectionStartRow,
                yMousePos, true);
        sheetWidget.getSheetHandler().onSelectingCellsWithDrag(tempCol,
                tempRow);
    }

    private void stopSelectingCells(Event event) {
        if (scrollTimerRunning) {
            stopScrollTimer();
        }
        sheetWidget.getSheetHandler().onFinishedSelectingCellsWithDrag(
                sheetWidget.getSelectedCellColumn(), tempCol,
                sheetWidget.getSelectedCellRow(), tempRow);

        dragging = false;
        showTouchActions();
    }

    protected void setFillMode(boolean fillMode) {
        this.fillMode = fillMode;
        if (fillMode) {
            bottomRight.addStyleName("fill");
            bottomRight.setCornerHidden(fillMode);
            if (topLeft != null) {
                topLeft.setCornerHidden(fillMode);
                topLeft.addStyleName("fill");
            }
            if (topRight != null) {
                topRight.setCornerHidden(fillMode);
                topRight.addStyleName("fill");
            }
            if (bottomLeft != null) {
                bottomLeft.setCornerHidden(fillMode);
                bottomLeft.addStyleName("fill");
            }
            setSelectionWidgetSquaresVisible(true);
        } else {
            bottomRight.removeStyleName("fill");
            if (topLeft != null) {
                topLeft.removeStyleName("fill");
            }
            if (topRight != null) {
                topRight.removeStyleName("fill");
            }
            if (bottomLeft != null) {
                bottomLeft.removeStyleName("fill");
            }
            setSelectionWidgetSquaresVisible(false);
        }
    }

    private boolean checkScrollWhilePainting(int y, int x) {
        int scrollPaneTop = sheetWidget.sheet.getAbsoluteTop();
        int scrollPaneLeft = sheetWidget.sheet.getAbsoluteLeft();
        int scrollPaneBottom = sheetWidget.sheet.getAbsoluteBottom();
        int scrollPaneRight = sheetWidget.sheet.getAbsoluteRight();

        clientX = x;
        clientY = y;

        if (y < scrollPaneTop) {
            if (crossedDown || (!startCellTopRight && !startCellTopLeft)) {
                deltaY = y - scrollPaneTop;
            }
        } else if (y > scrollPaneBottom) {
            deltaY = y - scrollPaneBottom;
        } else {
            deltaY = 0;
        }

        if (x < scrollPaneLeft) {
            if (crossedLeft || (!startCellBottomLeft && !startCellTopLeft)) {
                deltaX = x - scrollPaneLeft;
            }
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
            if (!crossedDown && (startCellTopLeft || startCellTopRight)
                    && sheetWidget.isCellRenderedInFrozenPane(tempCol, tempRow)
                    && !mouseOnTopSide) {
                sheetWidget.sheet.setScrollTop(0);
                sheetWidget.onSheetScroll(null);
                initialScrollTop = 0;
                crossedDown = true;
                scrolled = true;
            }
        }

        // If we're crossing the left freeze pane border, the right-hand part
        // must be scrolled all the way to the left.
        if (sheetWidget.sheet.getScrollLeft() != 0) {
            boolean mouseOnLeftSide = x < scrollPaneLeft;
            if (!crossedLeft && (startCellTopLeft || startCellBottomLeft)
                    && sheetWidget.isCellRenderedInFrozenPane(tempCol, tempRow)
                    && !mouseOnLeftSide) {
                sheetWidget.sheet.setScrollLeft(0);
                sheetWidget.onSheetScroll(null);
                initialScrollLeft = 0;
                crossedLeft = true;
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
            final String className = target.getAttribute("class");
            sheetWidget.jsniUtil.parseColRow(className);
            int col = sheetWidget.jsniUtil.getParsedCol();
            int row = sheetWidget.jsniUtil.getParsedRow();
            if (col != 0 && row != 0) {
                updatePaintRectangle(clientX, clientY, col, row);
            }
        }
    }

    private Timer scrollTimer = new Timer() {
        @Override
        public void run() {
            // Handle scrolling
            sheetWidget.sheet.setScrollTop(
                    sheetWidget.sheet.getScrollTop() + deltaY / 2);
            sheetWidget.sheet.setScrollLeft(
                    sheetWidget.sheet.getScrollLeft() + deltaX / 2);
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

            // Handle painting or selection
            if (paintMode) {
                handleCellShiftOnScroll(selectionPointX, selectionPointY);
            } else {
                Element target = WidgetUtil.getElementFromPoint(selectionPointX,
                        selectionPointY);
                if (target != null) {
                    final String className = target.getAttribute("class");
                    sheetWidget.jsniUtil.parseColRow(className);
                    int col = sheetWidget.jsniUtil.getParsedCol();
                    int row = sheetWidget.jsniUtil.getParsedRow();
                    if (col != 0 && row != 0) {
                        sheetWidget.getSheetHandler()
                                .onSelectingCellsWithDrag(col, row);
                    }
                }
            }
        }

    };
    private int initialScrollLeft;
    private int initialScrollTop;

    private void paintCells(Event event) {
        decreaseSelection = false;
        increaseSelection = false;
        final int clientX = SpreadsheetWidget.getTouchOrMouseClientX(event);
        final int clientY = SpreadsheetWidget.getTouchOrMouseClientY(event);

        // If we're scrolling, do not paint anything
        if (checkScrollWhilePainting(clientY, clientX)) {
            return;
        }

        // position in perspective to the top left
        int xMousePos = clientX - origX + sheetWidget.sheet.getScrollLeft()
                - initialScrollLeft;
        int yMousePos = clientY - origY + sheetWidget.sheet.getScrollTop()
                - initialScrollTop;

        final int[] colWidths = handler.getColWidths();
        final int[] rowHeightsPX = handler.getRowHeightsPX();
        int col = closestCellEdgeIndexToCursor(colWidths, col1, xMousePos,
                false);
        int row = closestCellEdgeIndexToCursor(rowHeightsPX, row1, yMousePos,
                false);

        if (col >= 0 && row >= 0) {
            updatePaintRectangle(clientX, clientY, col, row);
        }
    }

    private void updatePaintRectangle(final int clientX, final int clientY,
            int colIndex, int rowIndex) {
        // TODO This might need to handle merged cells
        // See http://dev.vaadin.com/ticket/17134
        if ((colIndex >= col1 && colIndex <= col2)
                && (rowIndex >= row1 && rowIndex <= row2)) {
            // case 1: shifting inside the selection
            int vDiff = Math.abs(row2 - rowIndex);
            int hDiff = Math.abs(col2 - colIndex);
            // Shifting inside the selection is prohibited in touch mode!
            if (touchMode || (vDiff == 0 && hDiff == 0)) {
                setPaintPosition(0, 0, 0, 0);
                setPaintVisible(false);
                return;
            }
            setPaintVisible(true);
            decreaseSelection = true;
            if (vDiff > hDiff) {
                int pos = Math.max(row1 + 1, row2 - vDiff + 1);
                setPaintPosition(col1, col2, pos, row2);
            } else {
                int pos = Math.max(col1 + 1, col2 - hDiff + 1);
                setPaintPosition(pos, col2, row1, row2);
            }
        } else if ((rowIndex < row1 || rowIndex > row2)
                || (colIndex < col1 || colIndex > col2)) {
            // case 2: shifting outside the selection
            setPaintVisible(true);
            increaseSelection = true;
            int diffDown = rowIndex - row2;
            int diffUp = row1 - rowIndex;
            int diffLeft = col1 - colIndex;
            int diffRight = colIndex - col2;
            if (Math.max(diffDown, diffUp) > Math.max(diffLeft, diffRight)) {
                // Shift up or down
                if (diffDown > diffUp) {
                    setPaintPosition(col1, col2, row2 + 1, rowIndex);
                } else {
                    setPaintPosition(col1, col2, rowIndex + 1, row1 - 1);
                }
            } else {
                // Shift left or right
                if (diffRight > diffLeft) {
                    setPaintPosition(col2 + 1, colIndex, row1, row2);
                } else {
                    setPaintPosition(colIndex + 1, col1 - 1, row1, row2);
                }
            }
        }
    }
}
