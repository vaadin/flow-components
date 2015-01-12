package com.vaadin.addon.spreadsheet.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.dom.client.StyleElement;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.addon.spreadsheet.client.CopyPasteTextBox.CopyPasteHandler;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.Util;
import com.vaadin.client.ui.VLabel;
import com.vaadin.client.ui.VLazyExecutor;
import com.vaadin.client.ui.VOverlay;

public class SheetWidget extends Panel {

    private static final String FREEZE_PANE_INACTIVE_STYLENAME = "inactive";
    private static final int CELL_COMMENT_OVERLAY_DELAY = 200;
    private static final int CELL_DATA_REQUESTER_DELAY = 200;
    private static final int SCROLL_HANDLER_TRIGGER_DELAY = 0;

    static final String MERGED_CELL_CLASSNAME = "merged-cell";
    private static final String RESIZE_LINE_CLASSNAME = "resize-line";
    private static final String ROW_RESIZING_CLASSNAME = "row-resizing";
    private static final String COLUMN_RESIZING_CLASSNAME = "col-resizing";
    private static final String RESIZE_TOOLTIP_LABEL_CLASSNAME = "v-spreadsheet-resize-tooltip-label";
    private static final String HEADER_RESIZE_DND_FIRST_CLASSNAME = "header-resize-dnd-first";
    private static final String HEADER_RESIZE_DND_SECOND_CLASSNAME = "header-resize-dnd-second";
    private static final String HEADER_RESIZE_DND_HTML = "<div class=\""
            + HEADER_RESIZE_DND_FIRST_CLASSNAME + "\" ></div><div class=\""
            + HEADER_RESIZE_DND_SECOND_CLASSNAME + "\" ></div>";
    private static final String HYPERLINK_TOOLTIP_LABEL_CLASSNAME = "v-spreadsheet-hyperlink-tooltip-label";
    private static final String CELL_COMMENT_TRIANGLE_CLASSNAME = Cell.CELL_COMMENT_TRIANGLE_CLASSNAME;
    private static final String NO_GRIDLINES_CLASSNAME = "nogrid";
    private static final String NO_ROWCOLHEADINGS_CLASSNAME = "noheaders";
    private static final String CUSTOM_EDITOR_CELL = "custom-editor-cell";
    private static final String SELECTED_CELL_STYLE2 = " { outline: solid #06f 1px;"
            + " -moz-outline-offset: -2px; outline-offset: -2px; z-index: 3; }";
    private static final String SELECTED_HEADER_STYLES = " { background: #e0F0ff; }";
    private static final String SELECTED_CELL_RANGE_STYLES = " { background: rgba(224,"
            + " 245, 255, 0.8) !important; }";
    private static final String EDITING_CELL_SELECTOR = ".v-spreadsheet .sheet div";
    private static final String EDITING_CELL_STYLE = "{ display: inline !important;"
            + " outline: none !important; width: auto !important; z-index: -10; }";
    private static final String HYPERLINK_CELL_STYLE = "{ cursor: pointer !important; }";
    private static final String MERGED_REGION_CELL_STYLE = "{ display: none; }";
    private static final String FREEZE_PANEL_OVERFLOW_STYLE = "{ overflow: hidden; }";

    private final Logger debugConsole = Logger.getLogger("spreadsheet-logger");

    private final SheetHandler actionHandler;

    private final SelectionWidget selectionWidget;

    private final VOverlay hyperlinkTooltip;

    private final VOverlay resizeTooltip;

    private final CellComment cellCommentOverlay;

    private CellComment focusedCellCommentOverlay;

    private final VLabel hyperlinkTooltipLabel;

    private final VLabel resizeTooltipLabel;

    /** Spreadsheet main (outmost) element */
    private DivElement spreadsheet = Document.get().createDivElement();

    /** Sheet that will contain all the cells */
    private DivElement sheet = Document.get().createDivElement();

    private SheetInputEventListener sheetInputEventListener;

    /** Header corner element that covers crossing headers */
    private DivElement corner = Document.get().createDivElement();

    private TextBox input;

    /** Invisible element for adjusting the scrollbars */
    private final DivElement floater = Document.get().createDivElement();

    /** A line to show the right/bottom dnd-resize position */
    private final DivElement resizeLine = Document.get().createDivElement();

    /** A line to show the left/top dnd-resize position */
    private final DivElement resizeLineStable = Document.get()
            .createDivElement();

    /**
     * Div elements for row header divs. Note that index 0 in array points to
     * div on row 1
     */
    private ArrayList<DivElement> rowHeaders = new ArrayList<DivElement>();

    private ArrayList<DivElement> frozenRowHeaders = new ArrayList<DivElement>();

    /**
     * Div elements for column header divs. Note that index 0 in array points to
     * div on column 1
     */
    private ArrayList<DivElement> colHeaders = new ArrayList<DivElement>();

    private ArrayList<DivElement> frozenColumnHeaders = new ArrayList<DivElement>();

    /**
     * List of rows. Each row is a list of divs on that row. Note that index 0
     * in the outer list points to row 1 and index 0 in the inner list points to
     * div in column 1. When frozen columns are used, this has the bottom right
     * pane's cells.
     */
    private ArrayList<ArrayList<Cell>> rows = new ArrayList<ArrayList<Cell>>();

    /** Cells in the frozen top left pane, starting from top left cell */
    private ArrayList<Cell> topLeftCells = new ArrayList<Cell>();

    /** Rows in the frozen top right pane */
    private ArrayList<ArrayList<Cell>> topRightRows = new ArrayList<ArrayList<Cell>>();

    /** Rows in the frozen bottom left pane */
    private ArrayList<ArrayList<Cell>> bottomLeftRows = new ArrayList<ArrayList<Cell>>();

    /**
     * Stylesheet element created for holding the dynamic row and column styles
     * (size position)
     */
    private StyleElement cellSizeAndPositionStyle = Document.get()
            .createStyleElement();

    /**
     * Stylesheet for holding the margins for headers (to fake scrolling)
     */
    private StyleElement headerMarginStyle = Document.get()
            .createStyleElement();

    /** Stylesheet element for holding the workbook defined styles */
    private StyleElement sheetStyle = Document.get().createStyleElement();

    /** Stylesheet element for holding custom cell sizes (because of borders) */
    private StyleElement shiftedBorderCellStyle = Document.get()
            .createStyleElement();

    /**
     * Stylesheet element for holding the selected cell + row & column header's
     * styles
     */
    private StyleElement selectionStyle = Document.get().createStyleElement();

    /**
     * Stylesheet element for holding the range cells + rows & columns headers'
     * styles
     */
    private StyleElement cellRangeStyle = Document.get().createStyleElement();

    /**
     * Stylesheet element for holding the edited cell style (for convenience
     * reasons, not actually visible). The selector is updated to the edited
     * cell. Also holds the style for the last freeze panel column, if any. This
     * is for preventing text oveflow over freeze panel.
     */
    private StyleElement editedCellFreezeColumnStyle = Document.get()
            .createStyleElement();

    /** Stylesheet for cursor: pointer for hyperlink cells. Created on-demand. */
    private StyleElement hyperlinkStyle;

    /**
     * Stylesheet for overriding column / row header sizes & positions when
     * dnd-resizing.
     */
    private StyleElement resizeStyle = Document.get().createStyleElement();

    /**
     * Stylesheet for hiding cells inside merged regions.
     */
    private StyleElement mergedRegionStyle = Document.get()
            .createStyleElement();

    /**
     * An element used for counting the ppi.
     */
    private DivElement ppiCounter = Document.get().createDivElement();

    private DivElement topLeftPane = Document.get().createDivElement();

    private DivElement topRightPane = Document.get().createDivElement();

    private DivElement bottomLeftPane = Document.get().createDivElement();

    /**
     * Hidden textfield that handles all copy and paste functions.
     */
    private final CopyPasteTextBox copyPasteBox;

    /**
     * A dummy element for calculating the width each cell style need so need of
     * scientific notation can be calculated
     */
    private SpanElement fontWidthDummyElement = Document.get()
            .createSpanElement();

    private final VLazyExecutor scrollHandler;

    private VLazyExecutor requester;

    private SheetJsniUtil jsniUtil = GWT.create(SheetJsniUtil.class);

    /**
     * Random id used as additional style for the widget element to connect
     * dynamic CSS rules to correct spreadsheet.
     */
    private String sheetId;

    private final HashMap<String, CellData> cachedCellData;

    private Widget customEditorWidget;

    private HashMap<String, Widget> customWidgetMap;

    private HashMap<String, String> cellLinksMap;

    private HashMap<String, String> cellCommentsMap;

    private HashMap<String, CellComment> alwaysVisibleCellComments;

    private HashMap<String, SheetImage> sheetImages;

    private HashMap<String, PopupButtonWidget> sheetPopupButtons;

    /** region ID to cell map */
    private HashMap<Integer, MergedCell> mergedCells;

    private String cellCommentCellClassName;

    private int selectedCellCol;
    private int selectedCellRow;

    private boolean cellRangeStylesCleared = true;
    private boolean coherentSelection = true;
    private boolean customCellEditorDisplayed;
    private boolean editingCell;
    private boolean editingMergedCell;
    private boolean loaded;
    private boolean selectingCells;

    private int firstColumnIndex;
    private int firstColumnPosition;
    private int firstRowIndex;
    private int firstRowPosition;
    /** index of the last rendered column */
    private int lastColumnIndex;
    /** right edge position of the last rendered column */
    private int lastColumnPosition;
    /** index of the last rendered row */
    private int lastRowIndex;
    /** *bottom edge position of the last rendered row */
    private int lastRowPosition;
    private int previousScrollLeft;
    private int previousScrollTop;
    private int scrollViewHeight;
    private int scrollViewWidth;
    private int ppi;
    private int defRowH = -1;
    private int[] definedRowHeights;
    private int topFrozenPanelHeight;
    private int leftFrozenPanelWidth;

    /** 1-based. marks the last frozen row index */
    private int verticalSplitPosition;
    /** 1-based. marks the last frozen column index */
    private int horizontalSplitPosition;

    private int resizedRowIndex = -1;
    private int resizedColumnIndex = -1;
    private int resizeFirstEdgePos;
    private int resizeLastEdgePos;
    private boolean resizing;
    private boolean resized;
    private boolean columnResizeCancelled;
    private final List<DivElement> resizeExtraHeaders;

    private int cellCommentCellColumn = -1;
    private int cellCommentCellRow = -1;

    private int tempCol;
    private int tempRow;

    private final boolean isIE = BrowserInfo.get().isIE();

    private boolean displayRowColHeadings;

    private Event mouseOverOrOutEvent;

    private HashMap<MergedRegion, Cell> overflownMergedCells;

    private VLazyExecutor cellCommentHandler = new VLazyExecutor(
            CELL_COMMENT_OVERLAY_DELAY, new ScheduledCommand() {

                @Override
                public void execute() {
                    if (cellCommentCellColumn != -1 && cellCommentCellRow != -1) {
                        showCellComment(cellCommentCellColumn,
                                cellCommentCellRow);
                    }

                }
            });

    private VLazyExecutor onMouseOverOrOutHandler = new VLazyExecutor(100,
            new ScheduledCommand() {

                @Override
                public void execute() {
                    if (isEditingCell()) {
                        return;
                    }
                    Element target = mouseOverOrOutEvent.getEventTarget()
                            .cast();
                    boolean targetParentIsPaneElement = target
                            .getParentElement().getClassName()
                            .contains("sheet");
                    String className = target.getClassName();
                    // cell comment lines are shown inside the sheet - skip
                    // those
                    if (className
                            .startsWith(CellComment.COMMENT_OVERLAY_LINE_CLASSNAME)) {
                        return;
                    }
                    if (className.contains("cell")) {
                        className = className.substring(0,
                                className.indexOf(" cell"));
                    }
                    if (className.equals(SheetImage.SHEET_IMAGE_CLASSNAME)) {
                        target = mouseOverOrOutEvent.getCurrentEventTarget()
                                .cast();
                        className = target.getClassName();
                    } else if (mouseOverOrOutEvent.getTypeInt() == Event.ONMOUSEOVER
                            && targetParentIsPaneElement) {
                        // because of cell overflow, the mouseover target might
                        // be a
                        // wrong cell
                        jsniUtil.parseColRow(className);
                        try {
                            int parsedCol = jsniUtil.getParsedCol();
                            int parsedRow = jsniUtil.getParsedRow();
                            if (parsedCol == 0 || parsedRow == 0) {
                                return;
                            }
                            target = getRealEventTargetCell(
                                    mouseOverOrOutEvent.getClientX(),
                                    mouseOverOrOutEvent.getClientY(),
                                    getCell(parsedCol, parsedRow)).getElement();
                            className = target.getClassName();
                            if (className.contains("cell")) {
                                className = className.substring(0,
                                        className.indexOf(" cell"));
                            }
                        } catch (JavaScriptException jse) {
                            debugConsole
                                    .severe("SheetWidget:onSheetMouseOverOrOut: JSE while trying to find real event target, className:"
                                            + className);
                        } catch (IndexOutOfBoundsException ioobe) {
                            debugConsole
                                    .warning("SheetWidget:onSheetMouseOverOrOut: IOOBE while trying to find correct event target, className:"
                                            + className);
                        }
                    }
                    jsniUtil.parseColRow(className);

                    // if mouse moved to/from a comment mark triangle, or the
                    // latest cell comment's cell, show/hide cell comment
                    if (className.equals(CELL_COMMENT_TRIANGLE_CLASSNAME)
                            || className.equals(cellCommentCellClassName)
                            || (cellCommentsMap != null && cellCommentsMap
                                    .containsKey(className))) {
                        updateCellCommentDisplay(mouseOverOrOutEvent, target);
                    } else {
                        if (cellCommentOverlay.isShowing()) {
                            cellCommentOverlay.hide();
                            cellCommentCellClassName = null;
                            cellCommentCellColumn = -1;
                            cellCommentCellRow = -1;
                        }
                    }
                    if (targetParentIsPaneElement && cellLinksMap != null
                            && cellLinksMap.containsKey(className)) {
                        updateCellLinkTooltip(mouseOverOrOutEvent.getTypeInt(),
                                jsniUtil.getParsedCol(),
                                jsniUtil.getParsedRow(),
                                cellLinksMap.get(className));
                        return;
                    } else if (hyperlinkTooltip.isVisible()) {
                        hyperlinkTooltip.hide();
                    }
                }
            });

    public SheetWidget(SheetHandler view) {
        actionHandler = view;
        cachedCellData = new HashMap<String, CellData>();
        alwaysVisibleCellComments = new HashMap<String, CellComment>();
        sheetImages = new HashMap<String, SheetImage>();
        mergedCells = new HashMap<Integer, MergedCell>();
        resizeExtraHeaders = new ArrayList<DivElement>();
        overflownMergedCells = new HashMap<MergedRegion, Cell>();
        hyperlinkTooltipLabel = new VLabel();
        hyperlinkTooltipLabel.setStyleName(HYPERLINK_TOOLTIP_LABEL_CLASSNAME);
        hyperlinkTooltip = new VOverlay();
        hyperlinkTooltip.setStyleName("v-tooltip");
        hyperlinkTooltip.setOwner(this);
        hyperlinkTooltip.add(hyperlinkTooltipLabel);
        resizeTooltipLabel = new VLabel();
        resizeTooltipLabel.setStyleName(RESIZE_TOOLTIP_LABEL_CLASSNAME);
        resizeTooltip = new VOverlay();
        resizeTooltip.setStyleName("v-tooltip");
        resizeTooltip.setOwner(this);
        resizeTooltip.add(resizeTooltipLabel);
        cellCommentOverlay = new CellComment(this, sheet);
        cellCommentOverlay.bringForward();
        initDOM();

        selectionWidget = new SelectionWidget(view, this);
        copyPasteBox = new CopyPasteTextBox(this, getCopyPasteHandler());
        getElement().appendChild(copyPasteBox.getElement());

        initListeners();

        scrollHandler = new VLazyExecutor(SCROLL_HANDLER_TRIGGER_DELAY,
                new ScheduledCommand() {

                    @Override
                    public void execute() {
                        if (loaded) {
                            onSheetScroll();
                        }
                    }
                });

        requester = new VLazyExecutor(CELL_DATA_REQUESTER_DELAY,
                new ScheduledCommand() {

                    @Override
                    public void execute() {
                        requestCells();
                    }
                });
    }

    protected CopyPasteHandler getCopyPasteHandler() {
        return new CopyPasteHandlerImpl(this);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        // we need to use the selectAll() method that needs GWT attachment
        adopt(copyPasteBox);
    }

    public SheetHandler getSheetHandler() {
        return actionHandler;
    }

    protected SheetJsniUtil getSheetJsniUtil() {
        return jsniUtil;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        hyperlinkTooltip.hide();
        resizeTooltip.hide();
    }

    protected void requestCells() {
        actionHandler.onScrollViewChanged(firstRowIndex, lastRowIndex,
                firstColumnIndex, lastColumnIndex);
    }

    /**
     * Set the model that stores the contents of the spreadsheet. Setting model
     * redraws the sheet.
     */
    public void resetFromModel(final int scrollLeft, final int scrollTop) {
        loaded = false;
        cachedCellData.clear();
        if (ppiCounter.hasParentElement()) {
            ppi = ppiCounter.getOffsetWidth();
        }
        removeCustomCellEditor();
        selectionWidget.setPosition(0, 0, 0, 0);
        defRowH = -1;
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                if (ppi == 0 && ppiCounter.hasParentElement()) {
                    ppi = ppiCounter.getOffsetWidth();
                }
                updateSheetStyles();
                updateCellStyles();
                updateConditionalFormattingStyles();
                resetScrollView(scrollLeft, scrollTop);
                actionHandler.onScrollViewChanged(firstRowIndex, lastRowIndex,
                        firstColumnIndex, lastColumnIndex);
                resetColHeaders();
                resetRowHeaders();
                resetCellContents();
                loaded = true;
            }
        });
    }

    public void relayoutSheet(boolean triggerRequest) {
        updateSheetStyles();
        int scrollTop = sheet.getScrollTop();
        int scrollLeft = sheet.getScrollLeft();
        int vScrollDiff = scrollTop - previousScrollTop;
        int hScrollDiff = scrollLeft - previousScrollLeft;
        try {
            // in case the number of cols/rows displayed has decreased
            if (lastRowIndex > actionHandler.getMaximumRows()) {
                lastRowIndex = actionHandler.getMaximumRows();
                while ((lastRowIndex - firstRowIndex + 1) < rows.size()) {
                    ArrayList<Cell> row = rows.remove(rows.size() - 1);
                    for (Cell cell : row) {
                        cell.getElement().removeFromParent();
                    }
                    rowHeaders.remove(rowHeaders.size() - 1).removeFromParent();
                }
            }
            if (lastColumnIndex > actionHandler.getMaximumCols()) {
                lastColumnIndex = actionHandler.getMaximumCols();
                for (ArrayList<Cell> row : rows) {
                    while ((lastColumnIndex - firstColumnIndex + 1) < row
                            .size()) {
                        row.remove(row.size() - 1).getElement()
                                .removeFromParent();
                    }
                }
                while ((lastColumnIndex - firstColumnIndex + 1) < colHeaders
                        .size()) {
                    colHeaders.remove(colHeaders.size() - 1).removeFromParent();
                }
            }
            // the sizes of the currently displayed columns / rows may have
            // changed -> update styles and display more columns and/or rows if
            // necessary (scroll positions may have not changed)

            int newFirstRowPosition = 0;
            for (int i = 1; i < firstRowIndex; i++) {
                newFirstRowPosition += getRowHeight(i);
                if (i == verticalSplitPosition) {
                    topFrozenPanelHeight = newFirstRowPosition;
                }
            }

            int newLastRowPosition = newFirstRowPosition;
            for (int i = firstRowIndex; i <= lastRowIndex; i++) {
                newLastRowPosition += getRowHeight(i);
            }
            final int bottomBound = topFrozenPanelHeight + scrollTop
                    + scrollViewHeight + actionHandler.getRowBufferSize();

            int topEdgeChange = newFirstRowPosition - firstRowPosition;
            int bottomEdgeChange = newLastRowPosition - lastRowPosition;
            firstRowPosition = newFirstRowPosition;
            lastRowPosition = newLastRowPosition;

            int newFirstColumnPosition = 0;
            for (int i = 1; i < firstColumnIndex; i++) {
                newFirstColumnPosition += actionHandler.getColWidthActual(i);
                if (horizontalSplitPosition == i) {
                    leftFrozenPanelWidth = newFirstColumnPosition;
                }
            }

            int newLastColumnPosition = newFirstColumnPosition;
            for (int i = firstColumnIndex; i <= lastColumnIndex; i++) {
                newLastColumnPosition += actionHandler.getColWidthActual(i);
            }
            final int rightBound = leftFrozenPanelWidth + scrollLeft
                    + scrollViewWidth + actionHandler.getColumnBufferSize();

            int leftEdgeChange = newFirstColumnPosition - firstColumnPosition;
            int rightEdgeChange = newLastColumnPosition - lastColumnPosition;
            firstColumnPosition = newFirstColumnPosition;
            lastColumnPosition = newLastColumnPosition;

            if (leftEdgeChange > 0 || hScrollDiff < 0) {
                handleHorizontalScrollLeft(scrollLeft);
                updateCells(0, -1);
            }
            if (rightEdgeChange < 0
                    || hScrollDiff > 0
                    || (lastColumnIndex < actionHandler.getMaximumCols() && lastColumnPosition < rightBound)) {
                handleHorizontalScrollRight(scrollLeft);
                updateCells(0, 1);
            }

            if (topEdgeChange > 0 || vScrollDiff < 0) {
                handleVerticalScrollUp(scrollTop);
                updateCells(-1, 0);
            }
            if (bottomEdgeChange != 0
                    || vScrollDiff > 0
                    || (lastRowIndex < actionHandler.getMaximumRows() && lastRowPosition < bottomBound)) {
                handleVerticalScrollDown(scrollTop);
                updateCells(1, 0);
            }

            previousScrollLeft = scrollLeft;
            previousScrollTop = scrollTop;

            if (triggerRequest) {
                requester.trigger();
            }

            // update the visible cell comment overlay positions
            for (CellComment cellComment : alwaysVisibleCellComments.values()) {
                if (actionHandler.isColumnHidden(cellComment.getCol())
                        || actionHandler.isRowHidden(cellComment.getRow())) {
                    cellComment.hide();
                } else {
                    cellComment.refreshPositionAccordingToCellRightCorner();
                }
            }

            moveHeadersToMatchScroll();

            updateSelectionOutline(selectionWidget.getCol1(),
                    selectionWidget.getCol2(), selectionWidget.getRow1(),
                    selectionWidget.getRow2());
        } catch (Exception e) {
            debugConsole.severe("SheetWidget:relayoutSheet: " + e.toString()
                    + " while relayouting spreadsheet");
            resetScrollView(scrollLeft, scrollTop);
            actionHandler.onScrollViewChanged(firstRowIndex, lastRowIndex,
                    firstColumnIndex, lastColumnIndex);
            resetColHeaders();
            resetRowHeaders();
            resetCellContents();
            refreshAlwaysVisibleCellCommentOverlays();
            updateOverflownMergedCellSizes();
        }
    }

    public void onWidgetResize() {
        if (loaded) {
            int newScrollViewHeight = sheet.getOffsetHeight();
            int newScrollViewWidth = sheet.getOffsetWidth();
            if (newScrollViewHeight > scrollViewHeight
                    || newScrollViewWidth > scrollViewWidth) {
                scrollViewHeight = newScrollViewHeight;
                scrollViewWidth = newScrollViewWidth;
                // FIXME optimize. haxor to force sheet load more cells
                // vertically and horiz.
                previousScrollLeft = actionHandler.getColumnBufferSize() * -1;
                previousScrollTop = actionHandler.getRowBufferSize() * -1;
                scrollHandler.trigger();
            } else {
                scrollViewHeight = newScrollViewHeight;
                scrollViewWidth = newScrollViewWidth;
                // no need to trigger scroll handler if the same size or smaller
            }
            // vaadin does bunch of layout phases so this needs to be done in
            // case the comment overlay position should be updated
            refreshAlwaysVisibleCellCommentOverlays();
        }
    }

    /** Build DOM elements for this spreadsheet */
    private void initDOM() {

        // Spreadsheet main element that acts as a viewport containing all the
        // other parts
        setElement(spreadsheet);
        spreadsheet.appendChild(sheet);
        sheetId = "spreadsheet-" + ((int) (Math.random() * 100000));
        spreadsheet.addClassName("v-spreadsheet");
        spreadsheet.addClassName(sheetId);

        // bottom-right-pane, always used
        sheet.setClassName("bottom-right-pane");
        sheet.addClassName("sheet");
        sheet.setTabIndex(3);

        // top right pane for cells. only used when needed.
        topRightPane.setClassName("top-right-pane");
        topRightPane.addClassName("sheet");
        spreadsheet.appendChild(topRightPane);

        // bottom left pane for cells. only used when needed.
        bottomLeftPane.setClassName("bottom-left-pane");
        bottomLeftPane.addClassName("sheet");
        spreadsheet.appendChild(bottomLeftPane);

        // top left pane for cells. only used when needed.
        topLeftPane.setClassName("top-left-pane");
        topLeftPane.addClassName("sheet");
        spreadsheet.appendChild(topLeftPane);

        resizeLine.setClassName(RESIZE_LINE_CLASSNAME);
        spreadsheet.appendChild(resizeLine);

        resizeLineStable.setClassName(RESIZE_LINE_CLASSNAME);
        sheet.appendChild(resizeLineStable);

        // Dynamic position & size styles for this spreadsheet
        cellSizeAndPositionStyle.setType("text/css");
        cellSizeAndPositionStyle.setId(sheetId + "-dynamicStyle");
        Document.get().getBody().getParentElement().getFirstChild()
                .appendChild(cellSizeAndPositionStyle);

        // Header margin style. Dynamically updated on scroll
        headerMarginStyle.setType("text/css");
        headerMarginStyle.setId(sheetId + "-headerDynamicStyle");
        cellSizeAndPositionStyle.getParentElement().appendChild(
                headerMarginStyle);

        // Workbook styles
        sheetStyle.setType("text/css");
        sheetStyle.setId(sheetId + "-sheetStyle");
        cellSizeAndPositionStyle.getParentElement().appendChild(sheetStyle);

        // Custom cell size styles (because of borders)
        shiftedBorderCellStyle.setType("text/css");
        shiftedBorderCellStyle.setId(sheetId + "-customCellSizeStyle");
        cellSizeAndPositionStyle.getParentElement().appendChild(
                shiftedBorderCellStyle);

        // styles for cell selection (cell outline + col&row header background)
        selectionStyle.setType("text/css");
        selectionStyle.setId(sheetId + "-selectionStyle");
        cellSizeAndPositionStyle.getParentElement().appendChild(selectionStyle);

        // style for the selected cell when the cell range outline is hidden
        jsniUtil.insertRule(selectionStyle, ".notusedselector"
                + SELECTED_CELL_STYLE2);

        // styles for cell range selection (cells&headers background)
        cellRangeStyle.setType("text/css");
        cellRangeStyle.setId(sheetId + "-cellRangeStyle");
        cellSizeAndPositionStyle.getParentElement().appendChild(cellRangeStyle);

        // initial cell range selection style (not visible)
        jsniUtil.insertRule(cellRangeStyle, ".notusedselector"
                + SELECTED_CELL_RANGE_STYLES);
        jsniUtil.insertRule(cellRangeStyle, ".notusedselector"
                + SELECTED_HEADER_STYLES);

        // style for "hiding" the edited cell
        editedCellFreezeColumnStyle.setType("text/css");
        editedCellFreezeColumnStyle.setId(sheetId + "-editedCellStyle");
        cellSizeAndPositionStyle.getParentElement().appendChild(
                editedCellFreezeColumnStyle);
        jsniUtil.insertRule(editedCellFreezeColumnStyle, ".notusedselector"
                + EDITING_CELL_STYLE);
        jsniUtil.insertRule(editedCellFreezeColumnStyle, ".notusedselector"
                + FREEZE_PANEL_OVERFLOW_STYLE);

        // style for hiding the cell inside merged regions
        mergedRegionStyle.setType("text/css");
        mergedRegionStyle.setId(sheetId + "-mergedRegionStyle");
        cellSizeAndPositionStyle.getParentElement().appendChild(
                mergedRegionStyle);

        resizeStyle.setType("text/css");
        resizeStyle.setId(sheetId + "-resizeStyle");
        cellSizeAndPositionStyle.getParentElement().appendChild(resizeStyle);

        // Corner div
        corner.setClassName("corner");
        spreadsheet.appendChild(corner);

        // floater, extra element for adjusting scroll bars correctly
        floater.setClassName("floater");

        // input
        input = new TextBox();
        input.setWidth("0");
        input.setValue("x");
        DOM.appendChild(sheet, input.getElement());
        adopt(input);

        // extra element for counting the pixels per inch so points can be
        // converted to pixels
        ppiCounter.getStyle().setWidth(1, Unit.IN);
        ppiCounter.getStyle().setPosition(Position.ABSOLUTE);
        ppiCounter.getStyle().setVisibility(Visibility.HIDDEN);
        ppiCounter.getStyle().setPadding(0, Unit.PX);
        spreadsheet.appendChild(ppiCounter);

        // extra element for counting the width in pixels each cell style needs
        // for showing numbers and applying scientific notation.
        fontWidthDummyElement.getStyle().setVisibility(Visibility.HIDDEN);
        fontWidthDummyElement.setInnerText("5555555555");
    }

    /**
     * Remove sheet DOM elements created. Currently does not clear Frozen panes'
     * contents - those are being handled when reloading sheet. FIXME unify
     * clearing of all DOM elements when reloading.
     */
    private void cleanDOM() {
        floater.removeFromParent();
        for (DivElement header : colHeaders) {
            header.removeFromParent();
        }
        colHeaders.clear();
        for (DivElement header : rowHeaders) {
            header.removeFromParent();
        }
        rowHeaders.clear();
        for (ArrayList<Cell> row : rows) {
            for (Cell cell : row) {
                cell.getElement().removeFromParent();
            }
            row.clear();
        }
        rows.clear();
    }

    /** For internal use. May be removed at a later time. */
    public void removeStyles() {
        // Remove style tags
        cellSizeAndPositionStyle.removeFromParent();
        sheetStyle.removeFromParent();
        shiftedBorderCellStyle.removeFromParent();
        selectionStyle.removeFromParent();
        cellRangeStyle.removeFromParent();
        editedCellFreezeColumnStyle.removeFromParent();
        resizeStyle.removeFromParent();
        mergedRegionStyle.removeFromParent();
        if (hyperlinkStyle != null) {
            hyperlinkStyle.removeFromParent();
        }
    }

    protected void onSheetScroll(Event event) {
        scrollHandler.trigger();
        moveHeadersToMatchScroll();
        updateOverflownMergedCellSizes();
        refreshAlwaysVisibleCellCommentOverlays();
        refreshPopupButtonOverlays();
    }

    /**
     * This is using a delayed execution because we don't want to try to do
     * stuff when it is unnecessary.
     * 
     * @param event
     * @param sheetPaneElement
     */
    protected void onSheetMouseOverOrOut(Event event) {
        mouseOverOrOutEvent = event;
        onMouseOverOrOutHandler.trigger();
    }

    protected void onSheetMouseMove(Event event) {
        if (cellCommentCellColumn != -1 && cellCommentCellRow != -1) {
            // the comment should only be displayed after the
            // mouse has "stopped" on top of a cell with a comment
            cellCommentHandler.trigger();
        }
    }

    protected boolean isEventInCustomEditorCell(Event event) {
        if (customEditorWidget != null) {
            final Element target = event.getEventTarget().cast();
            final Element customWidgetElement = customEditorWidget.getElement();
            return (customWidgetElement.isOrHasChild(target) || customWidgetElement
                    .getParentElement() != null
                    && customWidgetElement.getParentElement().isOrHasChild(
                            target));
        }
        return false;
    }

    protected Cell getRealEventTargetCell(final int clientX, final int clientY,
            final Cell cell) {
        Cell mergedCell = getMergedCell(toKey(cell.getCol(), cell.getRow()));
        if (mergedCell == null) {
            Element target = cell.getElement();

            int newX = cell.getCol();
            int newY = cell.getRow();
            boolean changed = false;

            if (clientX < target.getAbsoluteLeft()
                    && cell.getCol() > firstColumnIndex) {
                newX = cell.getCol() - 1;
                changed = true;
            } else if (clientX > target.getAbsoluteRight()
                    && cell.getCol() < lastColumnIndex) {
                newX = cell.getCol() + 1;
                changed = true;
            }

            if (clientY < target.getAbsoluteTop()
                    && cell.getRow() > firstRowIndex) {
                newY = cell.getRow() - 1;
                changed = true;
            } else if (clientY > target.getAbsoluteBottom()
                    && cell.getRow() < lastRowIndex) {
                newY = cell.getRow() + 1;
                changed = true;
            }

            if (changed) {
                return getRealEventTargetCell(clientX, clientY,
                        getCell(newX, newY));
            }

            return cell;
        } else {
            return mergedCell;
        }
    }

    protected void onSheetMouseDown(Event event) {
        Element target = event.getEventTarget().cast();
        onClick(target, event);
    }

    /**
     * 
     * @param target
     *            The clicked element
     * @param event
     *            The original event (that can be onClick or onTouchStart)
     */
    protected void onClick(Element target, Event event) {
        String className = target.getClassName();
        if (className.contains("sheet") || target.getTagName().equals("input")
                || className.equals("floater")) {
            return; // event target is one of the panes or input
        }

        if (isEventInCustomEditorCell(event)) {
            // allow sheet context menu on top of custom editors
            if (event.getButton() == NativeEvent.BUTTON_RIGHT) {
                actionHandler.onCellRightClick(event, selectedCellCol,
                        selectedCellRow);
            } else if (selectingCells) { // this is probably unnecessary
                stoppedSelectingCellsWithDrag(event);
            }
        } else if (className.contains("cell")) {
            jsniUtil.parseColRow(className);
            int targetCol = jsniUtil.getParsedCol();
            int targetRow = jsniUtil.getParsedRow();
            // because of text overflow, the click might have happened on
            // top of a another cell than what event has.
            // merged cells are a special case, text won't overflow -> skip
            try {
                if (!className.endsWith(MERGED_CELL_CLASSNAME)) {
                    int clientX = Util.getTouchOrMouseClientX(event);
                    int clientY = Util.getTouchOrMouseClientY(event);

                    Cell targetCell = getRealEventTargetCell(clientX, clientY,
                            getCell(targetCol, targetRow));
                    target = targetCell.getElement();
                    targetCol = targetCell.getCol();
                    targetRow = targetCell.getRow();
                }
            } catch (JavaScriptException jse) {
                debugConsole
                        .severe("SheetWidget:onSheetMouseDown - JSE while trying to find real event target, className:"
                                + className);
            } catch (IndexOutOfBoundsException ioobe) {
                debugConsole
                        .severe("SheetWidget:onSheetMouseDown - IOOBE while trying to find real event target, className:"
                                + className);
            }

            event.stopPropagation();
            event.preventDefault();
            if (event.getButton() == NativeEvent.BUTTON_RIGHT) {
                actionHandler.onCellRightClick(event, targetCol, targetRow);
            } else {
                sheet.focus();
                // quit input if active
                if (editingCell && !input.getElement().isOrHasChild(target)) {
                    actionHandler.onCellInputBlur(input.getValue());
                }
                if (event.getCtrlKey() || event.getMetaKey()
                        || event.getShiftKey()) {
                    actionHandler.onCellClick(targetCol, targetRow,
                            target.getInnerText(), event.getShiftKey(),
                            event.getMetaKey() || event.getCtrlKey(), true);
                    tempCol = -1;
                    tempRow = -1;
                } else { // no special keys used
                    // link cells are special keys
                    // TODO should investigate what is the correct action when
                    // clicking on hyperlink cells that overflow to next cells
                    if (cellLinksMap != null
                            && cellLinksMap.containsKey(toKey(
                                    jsniUtil.getParsedCol(),
                                    jsniUtil.getParsedRow()))) {
                        actionHandler.onLinkCellClick(targetCol, targetRow);
                    } else { // otherwise selecting starts
                        actionHandler
                                .onCellClick(targetCol, targetRow,
                                        target.getInnerText(),
                                        event.getShiftKey(), event.getMetaKey()
                                                || event.getCtrlKey(), false);
                        selectingCells = true;
                        tempCol = targetCol;
                        tempRow = targetRow;
                        Event.setCapture(sheet);
                    }
                }
            }
        }
    }

    protected void onSheetTouchStart(Event event) {

        if (event.getTouches().length() == 0) {
            return;
        }

        Touch touch = event.getTouches().get(0);
        Element e = touch.getTarget().cast();
        onClick(e, event);
    }

    protected void onMouseMoveWhenSelectingCells(Event event) {
        final Element target;

        /*
         * Touch events handle target element differently. According to specs,
         * Touch.getTarget() is the equivalent of event.getTarget(). Of course,
         * Safari doesn't follow the specifications; all target references are
         * to the element where we started the drag.
         * 
         * We need to manually parse x/y coords in #getRealEventTargetCell() to
         * find the correct cell.
         */
        if (event.getChangedTouches() != null
                && event.getChangedTouches().length() > 0) {
            JsArray<Touch> touches = event.getChangedTouches();
            target = touches.get(touches.length() - 1).getTarget().cast();
        } else if (event.getTouches() != null
                && event.getTouches().length() > 0) {
            JsArray<Touch> touches = event.getTouches();
            target = touches.get(touches.length() - 1).getTarget().cast();
        } else {
            target = event.getEventTarget().cast();
        }

        final boolean sheetOrChild = target.getParentElement().getClassName()
                .contains("sheet");
        final String className = target.getClassName();
        if (!sheetOrChild) { // sheet has capture, gets all events
            if (spreadsheet.isOrHasChild(target)) {
                // dragging on top of row/column headers
                final int header = jsniUtil.isHeader(className);
                if (header > 0) {
                    int parsedHeaderIndex = jsniUtil
                            .parseHeaderIndex(className);
                    if (header == 1) { // row
                        final int colIndex = getLeftVisibleColumnIndex() - 1;
                        tempCol = colIndex < 1 ? 1 : colIndex;
                        tempRow = parsedHeaderIndex;
                        actionHandler.onSelectingCellsWithDrag(tempCol,
                                parsedHeaderIndex);
                    } else { // column
                        final int rowIndex = getTopVisibleRowIndex() - 1;
                        tempRow = rowIndex < 1 ? 1 : rowIndex;
                        tempCol = parsedHeaderIndex;
                        actionHandler.onSelectingCellsWithDrag(
                                parsedHeaderIndex, tempRow);
                    }
                }
            }
        } else {

            /*
             * Parse according to classname of target element. As said above,
             * Safari gives us the wrong target and hence we have the wrong
             * style name here.
             * 
             * This also means that if we move outside the sheet, we continue
             * execution past this check.
             */
            jsniUtil.parseColRow(className);
            int col = jsniUtil.getParsedCol();
            int row = jsniUtil.getParsedRow();
            if (col == 0 || row == 0) {
                return;
            }

            // skip search of actual cell if this is a merged cell
            if (!className.endsWith(MERGED_CELL_CLASSNAME)) {
                Cell targetCell = getRealEventTargetCell(
                        Util.getTouchOrMouseClientX(event),
                        Util.getTouchOrMouseClientY(event), getCell(col, row));
                col = targetCell.getCol();
                row = targetCell.getRow();
            }

            if (col != tempCol || row != tempRow) {
                if (col == 0) { // on top of scroll bar
                    if (Util.getTouchOrMouseClientX(event) > target
                            .getParentElement().getAbsoluteRight()) {
                        col = getRightVisibleColumnIndex() + 1;
                    } else {
                        col = tempCol;
                    }
                }
                if (row == 0) {
                    if (Util.getTouchOrMouseClientY(event) > sheet
                            .getAbsoluteBottom()) {
                        row = getBottomVisibleRowIndex() + 1;
                    } else {
                        row = tempRow;
                    }
                }
                actionHandler.onSelectingCellsWithDrag(col, row);
                tempCol = col;
                tempRow = row;
            }
        }
    }

    protected void stoppedSelectingCellsWithDrag(Event event) {
        Event.releaseCapture((Element) sheet.cast());
        if ((selectedCellCol != tempCol || selectedCellRow != tempRow)
                && tempCol != -1 && tempRow != -1) {
            actionHandler.onFinishedSelectingCellsWithDrag(selectedCellCol,
                    tempCol, selectedCellRow, tempRow);
        } else {
            actionHandler.onCellClick(tempCol, tempRow, ((Element) event
                    .getEventTarget().cast()).getInnerText(), event
                    .getShiftKey(), event.getMetaKey() || event.getCtrlKey(),
                    true);
        }
        selectingCells = false;
        tempCol = -1;
        tempRow = -1;
    }

    private void initListeners() {
        sheetInputEventListener = GWT.create(SheetInputEventListener.class);
        sheetInputEventListener.setSheetWidget(this, input);

        SheetEventListener listener = GWT.create(SheetEventListener.class);
        listener.setSheetWidget(this);
        listener.setSheetPaneElement(topLeftPane, topRightPane, bottomLeftPane,
                sheet);
        // for some reason the click event is not fired normally for headers
        Event.addNativePreviewHandler(new NativePreviewHandler() {

            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                int eventTypeInt = event.getTypeInt();
                final NativeEvent nativeEvent = event.getNativeEvent();
                if (eventTypeInt == Event.ONCLICK) {
                    Element target = nativeEvent.getEventTarget().cast();
                    String className = target.getClassName();
                    int i = jsniUtil.isHeader(className);
                    if (i == 1 || i == 2) {
                        int index = jsniUtil.parseHeaderIndex(className);
                        if (i == 1) {
                            actionHandler.onRowHeaderClick(
                                    index,
                                    nativeEvent.getShiftKey(),
                                    nativeEvent.getMetaKey()
                                            || nativeEvent.getCtrlKey());
                        } else {
                            actionHandler.onColumnHeaderClick(
                                    index,
                                    nativeEvent.getShiftKey(),
                                    nativeEvent.getMetaKey()
                                            || nativeEvent.getCtrlKey());
                        }
                        event.cancel();
                        sheet.focus();
                    }
                } else if (eventTypeInt == Event.ONMOUSEDOWN
                        && actionHandler.canResize()) {
                    Element target = nativeEvent.getEventTarget().cast();
                    String className = target.getClassName();
                    if (className.equals(HEADER_RESIZE_DND_FIRST_CLASSNAME)) {
                        className = target.getParentElement().getClassName();
                        int i = jsniUtil.isHeader(className);
                        if (i == 1) { // row
                            i = jsniUtil.parseHeaderIndex(className);
                            startRowResizeDrag(i - 1, nativeEvent.getClientX(),
                                    nativeEvent.getClientY());
                        } else if (i == 2) { // col
                            i = jsniUtil.parseHeaderIndex(className);
                            columnResizeCancelled = false;
                            startColumnResizeDrag(i - 1,
                                    nativeEvent.getClientX(),
                                    nativeEvent.getClientY());
                        }
                        event.cancel();
                    } else if (className
                            .equals(HEADER_RESIZE_DND_SECOND_CLASSNAME)) {
                        className = target.getParentElement().getClassName();
                        int i = jsniUtil.isHeader(className);
                        if (i == 1) { // row
                            i = jsniUtil.parseHeaderIndex(className);
                            startRowResizeDrag(i, nativeEvent.getClientX(),
                                    nativeEvent.getClientY());
                        } else if (i == 2) { // col
                            i = jsniUtil.parseHeaderIndex(className);
                            columnResizeCancelled = false;
                            startColumnResizeDrag(i, nativeEvent.getClientX(),
                                    nativeEvent.getClientY());
                        }
                        event.cancel();
                    }
                } else if (resizing && eventTypeInt == Event.ONMOUSEMOVE) {
                    if (resizedColumnIndex != -1) {
                        handleColumnResizeDrag(
                                Util.getTouchOrMouseClientX(nativeEvent),
                                Util.getTouchOrMouseClientY(nativeEvent));
                    } else if (resizedRowIndex != -1) {
                        handleRowResizeDrag(
                                Util.getTouchOrMouseClientX(nativeEvent),
                                Util.getTouchOrMouseClientY(nativeEvent));
                    } else {
                        resizing = false;
                    }
                    event.cancel();
                } else if (resizing && eventTypeInt == Event.ONMOUSEUP) {
                    columnResizeCancelled = true;
                    resizing = false;
                    jsniUtil.clearCSSRules(resizeStyle);
                    resizeTooltip.hide();
                    event.cancel();
                    for (DivElement extraHeader : resizeExtraHeaders) {
                        extraHeader.removeFromParent();
                    }
                    resizeExtraHeaders.clear();
                    if (resizedColumnIndex != -1) {
                        spreadsheet.removeClassName(COLUMN_RESIZING_CLASSNAME);
                        stopColumnResizeDrag(event.getNativeEvent()
                                .getClientX());
                    } else {
                        spreadsheet.removeClassName(ROW_RESIZING_CLASSNAME);
                        stopRowResizeDrag(Util.getTouchOrMouseClientY(event
                                .getNativeEvent()));
                    }
                } else if (eventTypeInt == Event.ONDBLCLICK
                        && actionHandler.canResize()) {
                    Element target = nativeEvent.getEventTarget().cast();
                    String className = target.getClassName();
                    if (className.equals(HEADER_RESIZE_DND_FIRST_CLASSNAME)) {
                        className = target.getParentElement().getClassName();
                        int i = jsniUtil.isHeader(className);
                        if (i == 1) { // row
                            // autofit row ???
                        } else if (i == 2) { // col
                            i = jsniUtil.parseHeaderIndex(className) - 1;
                            while (actionHandler.isColumnHidden(i) && i > 0) {
                                i--;
                            }
                            if (i > 0) {
                                actionHandler
                                        .onColumnHeaderResizeDoubleClick(i);
                            }
                        }
                        event.cancel();
                    } else if (className
                            .equals(HEADER_RESIZE_DND_SECOND_CLASSNAME)) {
                        className = target.getParentElement().getClassName();
                        int i = jsniUtil.isHeader(className);
                        if (i == 1) { // row
                            // autofit row ???
                        } else if (i == 2) { // col
                            i = jsniUtil.parseHeaderIndex(className);
                            while (actionHandler.isColumnHidden(i) && i > 0) {
                                i--;
                            }
                            if (i > 0) {
                                actionHandler
                                        .onColumnHeaderResizeDoubleClick(i);
                            }
                        }
                        event.cancel();
                    }
                }
            }
        });
        addDomHandler(new ContextMenuHandler() {

            @Override
            public void onContextMenu(ContextMenuEvent event) {
                if (actionHandler.hasCustomContextMenu()) {
                    Element target = event.getNativeEvent().getEventTarget()
                            .cast();
                    if (sheet.isOrHasChild(target)
                            || sheet.isOrHasChild(target.getParentNode())) {
                        event.preventDefault();
                        event.stopPropagation();
                    } else {
                        String className = target.getClassName();
                        int i = jsniUtil.isHeader(className);
                        if (i == 1 || i == 2) {
                            int index = jsniUtil.parseHeaderIndex(className);
                            if (i == 1) {
                                actionHandler.onRowHeaderRightClick(
                                        event.getNativeEvent(), index);
                            } else {
                                actionHandler.onColumnHeaderRightClick(
                                        event.getNativeEvent(), index);
                            }
                            event.preventDefault();
                            event.stopPropagation();

                        }
                    }
                }
            }
        }, ContextMenuEvent.getType());
    }

    protected boolean isEditingCell() {
        return editingCell;
    }

    protected boolean isSelectingCells() {
        return selectingCells;
    }

    private void startRowResizeDrag(int rowIndex, int clientX, int clientY) {
        // for some reason FF doesn't hide headers instantly,
        // the event might be from hidden div
        while (actionHandler.isRowHidden(rowIndex)) {
            rowIndex--;
        }
        if (rowIndex == 0) { // ERROR ...
            return;
        }
        resizing = true;
        resized = false;
        resizedRowIndex = rowIndex;
        resizedColumnIndex = -1;
        DivElement header;
        if (resizedRowIndex <= verticalSplitPosition) {
            header = frozenRowHeaders.get(resizedRowIndex - 1);
        } else {
            header = rowHeaders.get(rowIndex - firstRowIndex);
        }
        resizeFirstEdgePos = header.getAbsoluteTop();
        resizeLastEdgePos = header.getAbsoluteBottom();
        resizeTooltipLabel.setText("Height: "
                + actionHandler.getRowHeight(rowIndex) + "pt");
        showResizeTooltipRelativeTo(clientX, clientY);
        resizeTooltip.show();
        spreadsheet.addClassName(ROW_RESIZING_CLASSNAME);
        resizeLineStable.addClassName("row" + rowIndex);
        rowIndex++;
        while (rowIndex < actionHandler.getMaximumRows()
                && actionHandler.isRowHidden(rowIndex)) {
            rowIndex++;
        }
        resizeLine.addClassName("rh row" + (rowIndex));

        // need to make sure the selection widget stays unaffected if the header
        // margins are changed
        selectionWidget.getElement().getStyle()
                .setMarginTop((49 - sheet.getScrollTop()), Unit.PX);
    }

    private void startColumnResizeDrag(final int columnIndex,
            final int clientX, final int clientY) {
        resized = false;
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                if (columnResizeCancelled) {
                    return;
                }
                // for some reason FF doesn't hide headers instantly,
                // the event might be from hidden div
                int tempColumnIndex = columnIndex;
                while (actionHandler.isColumnHidden(tempColumnIndex)) {
                    tempColumnIndex--;
                }
                if (tempColumnIndex < 1) { // ERROR ...
                    return;
                }
                resizing = true;
                resizedColumnIndex = tempColumnIndex;
                resizedRowIndex = -1;
                DivElement header;
                if (resizedColumnIndex <= horizontalSplitPosition) {
                    header = frozenColumnHeaders.get(resizedColumnIndex - 1);
                } else {
                    header = colHeaders.get(tempColumnIndex - firstColumnIndex);
                }
                resizeFirstEdgePos = header.getAbsoluteLeft();
                resizeLastEdgePos = header.getAbsoluteRight();
                resizeTooltipLabel.setText("Width: "
                        + actionHandler.getColWidth(tempColumnIndex) + "px");
                showResizeTooltipRelativeTo(clientX, clientY);
                resizeTooltip.show();
                spreadsheet.addClassName(COLUMN_RESIZING_CLASSNAME);
                resizeLineStable.addClassName("col" + tempColumnIndex);
                tempColumnIndex++;
                while (columnIndex <= actionHandler.getMaximumCols()
                        && actionHandler.isColumnHidden(tempColumnIndex)) {
                    tempColumnIndex++;
                }
                resizeLine.addClassName("ch col" + (tempColumnIndex));

                // need to make sure the selection widget stays unaffected if
                // the header
                // margins are changed
                selectionWidget.getElement().getStyle()
                        .setMarginLeft((50 - sheet.getScrollLeft()), Unit.PX);
            }
        });
    }

    private void stopRowResizeDrag(int clientY) {
        resizeLine.setClassName(RESIZE_LINE_CLASSNAME);
        selectionWidget.getElement().getStyle().clearMarginTop();
        resizeLineStable.removeClassName("row" + resizedRowIndex);
        if (resized) {
            int delta = clientY - resizeFirstEdgePos;
            final Map<Integer, Float> newSizes = new HashMap<Integer, Float>();
            if (delta <= 0) {
                newSizes.put(resizedRowIndex, 0.0F);
                int index = resizedRowIndex - 1; // 1-based
                while (delta < 0 && (index > 0)) {
                    if (actionHandler.isRowHidden(index)) {
                        index--;
                    } else {
                        int nextHeaderHeight = getRowHeight(index);
                        if (delta + nextHeaderHeight < 0) { // has 0-height
                            newSizes.put(index, 0.0F);
                            index--;
                            delta += nextHeaderHeight;
                        } else {
                            break;
                        }
                    }
                }
                if (delta != 0) {
                    // resize the "last" header
                    int changedSizePX = getRowHeight(index) + delta;
                    newSizes.put(index, convertPixelsToPoint(changedSizePX));
                }

            } else {
                // only the dragged header size has changed.
                int px = clientY - resizeFirstEdgePos;
                float pt = convertPixelsToPoint(px);
                if (pt != actionHandler.getRowHeight(resizedRowIndex)) {
                    newSizes.put(resizedRowIndex, pt);
                }
            }
            if (!newSizes.isEmpty()) {
                actionHandler.onRowsResized(newSizes);
            }
        }
        resizedRowIndex = -1;
    }

    private void stopColumnResizeDrag(int clientX) {
        resizeLine.setClassName(RESIZE_LINE_CLASSNAME);
        resizeLineStable.removeClassName("col" + resizedColumnIndex);
        selectionWidget.getElement().getStyle().clearMarginLeft();
        if (resized) {
            int delta = clientX - resizeFirstEdgePos;
            final Map<Integer, Integer> newSizes = new HashMap<Integer, Integer>();
            if (delta <= 0) {
                newSizes.put(resizedColumnIndex, 0);
                int index = resizedColumnIndex - 1; // 1-based
                while (delta < 0 && (index > 0)) {
                    if (actionHandler.isColumnHidden(index)) {
                        index--;
                    } else {
                        int nextHeaderWidth = actionHandler
                                .getColWidthActual(index);
                        if (delta + nextHeaderWidth < 0) { // has 0-width
                            newSizes.put(index, 0);
                            index--;
                            delta += nextHeaderWidth;
                        } else {
                            break;
                        }
                    }
                }
                if (delta != 0) {
                    // resize the "last" header
                    int changedSizePX = actionHandler.getColWidthActual(index)
                            + delta;
                    newSizes.put(index, changedSizePX);
                }

            } else {
                // only the dragged header size has changed.
                int px = clientX - resizeFirstEdgePos;
                if (px != actionHandler.getColWidthActual(resizedColumnIndex)) {
                    newSizes.put(resizedColumnIndex, px);
                }
            }

            if (!newSizes.isEmpty()) {
                actionHandler.onColumnsResized(newSizes);
            }
        }

        resizedColumnIndex = -1;
    }

    private void handleRowResizeDrag(int clientX, int clientY) {
        if (clientX < (spreadsheet.getAbsoluteLeft())
                || clientX > sheet.getAbsoluteRight()
                || clientY < (sheet.getAbsoluteTop() - topFrozenPanelHeight)
                || clientY > sheet.getAbsoluteBottom()) {
            return;
        }
        resized = true;
        int delta = clientY - resizeFirstEdgePos;
        jsniUtil.clearCSSRules(resizeStyle);
        String rule;
        if (delta <= 0) {
            // at least the dragged header is hidden
            resizeTooltipLabel.setText("Height: 0pt");
            // enter rule for 0-sized row headers
            rule = ".v-spreadsheet > div.rh.row" + resizedRowIndex;
            int index = resizedRowIndex - 1; // 1-based
            while (delta < 0 && (index > 0)) {
                int nextHeaderHeight = getRowHeight(index);
                if (delta + nextHeaderHeight < 0) { // has 0-height
                    rule += ",.v-spreadsheet > div.rh.row" + index;
                    index--;
                    delta += nextHeaderHeight;
                } else {
                    break;
                }
            }
            rule += "{height:0pt;}";
            jsniUtil.insertRule(resizeStyle, rule);
            if (delta != 0) {
                // resize the "last" header
                rule = ".v-spreadsheet > div.rh.row" + index + "{height:"
                        + (getRowHeight(index) + delta) + "px}";
                jsniUtil.insertRule(resizeStyle, rule);
            }

        } else {
            // only the dragged header size has changed.
            int px = clientY - resizeFirstEdgePos;
            resizeTooltipLabel.setText("Height: " + px + "px ≈ "
                    + convertPixelsToPoint(px) + "pt");
            // enter custom size for the resized row header
            rule = ".v-spreadsheet > div.rh.row" + resizedRowIndex + "{height:"
                    + px + "px;}";
            jsniUtil.insertRule(resizeStyle, rule);
        }
        int headersAfter = 0;
        int spaceAfter = sheet.getAbsoluteBottom() - clientY;
        // might need to add more headers
        // count how many headers after resize one
        for (int i = resizedRowIndex + 1; i <= lastRowIndex
                && headersAfter < spaceAfter; i++) {
            headersAfter += getRowHeight(i);
        }
        int index = lastRowIndex + 1; // 1-based
        // add extra headers that were added previously
        if (!resizeExtraHeaders.isEmpty()) {
            for (int i = 0; i < resizeExtraHeaders.size(); i++) {
                headersAfter += getRowHeight(index);
                index++;
            }
        }
        // add extra headers if necessary
        while (spaceAfter > headersAfter
                && index <= actionHandler.getMaximumRows()) {
            DivElement extraHeader = Document.get().createDivElement();
            extraHeader.setClassName("rh resize-extra row" + (index));
            extraHeader.setInnerText(actionHandler.getRowHeader(index));
            headersAfter += getRowHeight(index);
            spreadsheet.appendChild(extraHeader);
            resizeExtraHeaders.add(extraHeader);
            index++;
        }
        // adjust headers after resized one with margin
        int margin = clientY - resizeLastEdgePos;
        if (margin != 0) {
            rule = "";
            for (int i = resizedRowIndex + 1; i <= (lastRowIndex + resizeExtraHeaders
                    .size()); i++) {
                rule += ".v-spreadsheet > div.rh.row" + i;
                if ((lastRowIndex + resizeExtraHeaders.size()) != i) {
                    rule += ",";
                }
            }
            margin = 49 - sheet.getScrollTop() + margin;
            if (!rule.isEmpty()) {
                rule += "{margin-top:" + margin + "px;}";
                jsniUtil.insertRule(resizeStyle, rule);
            }
            rule = ".v-spreadsheet.row-resizing > div.resize-line.rh {margin-top:"
                    + (margin - 1) + "px;}";
            jsniUtil.insertRule(resizeStyle, rule);
        }
        showResizeTooltipRelativeTo(clientX, clientY);
    }

    private void handleColumnResizeDrag(int clientX, int clientY) {
        if (clientX < sheet.getAbsoluteLeft()
                || clientX > sheet.getAbsoluteRight()
                || clientY < (sheet.getAbsoluteTop() - 20 - topFrozenPanelHeight)
                || clientY > sheet.getAbsoluteBottom()) {
            return;
        }
        resized = true;
        int delta = clientX - resizeFirstEdgePos;
        jsniUtil.clearCSSRules(resizeStyle);
        String rule;
        if (delta <= 0) {
            // at least the dragged header is hidden
            resizeTooltipLabel.setText("Width: 0px");
            // enter rule for 0-sized row headers
            rule = ".v-spreadsheet > div.ch.col" + resizedColumnIndex;
            int index = resizedColumnIndex - 1; // 1-based
            while (delta < 0 && (index > 0)) {
                int nextHeaderWidth = actionHandler.getColWidthActual(index);
                if (delta + nextHeaderWidth < 0) { // has 0-width
                    rule += ",.v-spreadsheet > div.ch.col" + index;
                    index--;
                    delta += nextHeaderWidth;
                } else {
                    break;
                }
            }
            rule += "{width:0;}";
            jsniUtil.insertRule(resizeStyle, rule);
            if (delta != 0) {
                // resize the "last" header
                rule = ".v-spreadsheet > div.ch.col" + index + "{width:"
                        + (actionHandler.getColWidthActual(index) + delta)
                        + "px}";
                jsniUtil.insertRule(resizeStyle, rule);
            }
        } else {
            // only the dragged header size has changed.
            int px = clientX - resizeFirstEdgePos;
            resizeTooltipLabel.setText("Width: " + px + "px");
            // enter custom size for the resized column header
            rule = ".v-spreadsheet > div.ch.col" + resizedColumnIndex
                    + "{width:" + px + "px;}";
            jsniUtil.insertRule(resizeStyle, rule);
        }
        int headersAfter = 0;
        int spaceAfter = sheet.getAbsoluteRight() - clientX;
        // might need to add more headers
        // count how many headers after resize one
        for (int i = resizedColumnIndex + 1; i <= lastColumnIndex
                && headersAfter < spaceAfter; i++) {
            headersAfter += actionHandler.getColWidthActual(i);
        }
        int index = lastColumnIndex + 1; // 1-based
        // add extra headers that were added previously
        if (!resizeExtraHeaders.isEmpty()) {
            for (int i = 0; i < resizeExtraHeaders.size(); i++) {
                headersAfter += actionHandler.getColWidthActual(index);
                index++;
            }
        }
        // add extra headers if necessary
        while (spaceAfter > headersAfter
                && index <= actionHandler.getMaximumCols()) {
            DivElement extraHeader = Document.get().createDivElement();
            extraHeader.setClassName("ch resize-extra col" + (index));
            extraHeader.setInnerText(actionHandler.getColHeader(index));
            headersAfter += actionHandler.getColWidthActual(index);
            spreadsheet.appendChild(extraHeader);
            resizeExtraHeaders.add(extraHeader);
            index++;
        }
        // adjust headers after resized one with margin
        int margin = clientX - resizeLastEdgePos;
        if (margin != 0) {
            rule = "";
            for (int i = resizedColumnIndex + 1; i <= (lastColumnIndex + resizeExtraHeaders
                    .size()); i++) {
                rule += ".v-spreadsheet > div.ch.col" + i;
                if ((lastColumnIndex + resizeExtraHeaders.size()) != i) {
                    rule += ",";
                }
            }
            margin = 50 - sheet.getScrollLeft() + margin;
            if (!rule.isEmpty()) {
                rule += "{margin-left:" + margin + "px;}";
                jsniUtil.insertRule(resizeStyle, rule);
            }
            rule = ".v-spreadsheet.col-resizing > div.resize-line.ch {margin-left:"
                    + (margin - 1) + "px;}";
            jsniUtil.insertRule(resizeStyle, rule);
        }
        showResizeTooltipRelativeTo(clientX, clientY);
    }

    private void showResizeTooltipRelativeTo(int clientX, int clientY) {
        int left = clientX + 10;
        int top = clientY - 25;
        resizeTooltip.setPopupPosition(left, top);
    }

    /** Replace stylesheet with the array of rules given */
    private void resetStyleSheetRules(StyleElement stylesheet, String[] rules) {
        jsniUtil.clearCSSRules(stylesheet);
        for (int i = 0; i < rules.length; i++) {
            jsniUtil.insertRule(stylesheet, rules[i]);
        }
    }

    public CellData getCellData(int column, int row) {
        return cachedCellData.get(toKey(column, row));
    }

    public String getCellValue(int column, int row) {
        CellData cd = cachedCellData.get(toKey(column, row));
        return cd == null ? "" : cd.value;
    }

    private String createHeaderDNDHTML() {
        return HEADER_RESIZE_DND_HTML;
    }

    /**
     * Called after scrolling to move headers in order to keep them in sync with
     * the spreadsheet contents. Also effects the selection widget.
     */
    private void moveHeadersToMatchScroll() {
        jsniUtil.updateCSSRule(headerMarginStyle,
                ".v-spreadsheet .top-right-pane", "marginLeft",
                (0 - sheet.getScrollLeft()) + "px");
        jsniUtil.updateCSSRule(headerMarginStyle,
                ".v-spreadsheet .bottom-left-pane", "marginTop",
                (0 - sheet.getScrollTop()) + "px");
    }

    private float createRowStyles(String[] rules, int startIndex, int endIndex) {
        float top = 0;
        String stylePrimaryName = getStylePrimaryName();
        for (int i = startIndex; i <= endIndex; i++) {
            StringBuilder sb = new StringBuilder();
            float rowHeight = actionHandler.getRowHeight(i);
            sb.append(".").append(stylePrimaryName).append(" .sheet .row")
                    .append(i).append(" { ").append(getRowDisplayString(i))
                    .append("height: ").append(rowHeight).append("pt; top:")
                    .append(top).append("pt; }\n");
            top += rowHeight;
            definedRowHeights[i - 1] = convertPointsToPixel(rowHeight);
            rules[i - 1] = sb.toString();
        }
        return top;
    }

    private int createColumnStyles(String[] rules, int ruleIndex,
            int startIndex, int endIndex) {
        int left = 0;
        String stylePrimaryName = getStylePrimaryName();
        for (int i = startIndex; i <= endIndex; i++) {
            StringBuilder sb = new StringBuilder();
            int colWidth = actionHandler.getColWidth(i);
            sb.append(".").append(stylePrimaryName).append(" .sheet .col")
                    .append(i).append(" { ").append(getColumnDisplayString(i))
                    .append("width: ").append(colWidth).append("px; left:")
                    .append(left).append("px; }\n");
            left += colWidth;
            rules[ruleIndex++] = sb.toString();
        }
        return left;
    }

    private String getRowDisplayString(int rowIndex) {
        return actionHandler.isRowHidden(rowIndex) ? "display:none;" : "";
    }

    private String getColumnDisplayString(int columnIndex) {
        return actionHandler.isColumnHidden(columnIndex) ? "display:none;" : "";
    }

    private void updateSheetStyles() {
        // styles for sizes and position
        final String[] sizeStyleRules = new String[actionHandler
                .getMaximumRows() + actionHandler.getMaximumCols()];
        // + verticalSplitPosition > 0 ? 1
        // : 0 + horizontalSplitPosition > 0 ? 1 : 0]; // add extra rule if
        // vertical split
        // panel exists

        // create row rules (height + top offset)
        definedRowHeights = new int[actionHandler.getMaximumRows()];
        topFrozenPanelHeight = 0;
        float topFrozenPanelHeightPt = 0;
        if (verticalSplitPosition > 0) {
            topFrozenPanelHeightPt = createRowStyles(sizeStyleRules, 1,
                    verticalSplitPosition);
            topFrozenPanelHeight = convertPointsToPixel(topFrozenPanelHeightPt) + 1;
        }
        float bottomPanelHeightPt = createRowStyles(sizeStyleRules,
                verticalSplitPosition + 1, actionHandler.getMaximumRows());

        // create column rules (width + left offset)
        leftFrozenPanelWidth = 0;
        int ruleIndex = actionHandler.getMaximumRows();
        if (horizontalSplitPosition > 0) {
            leftFrozenPanelWidth = createColumnStyles(sizeStyleRules,
                    ruleIndex, 1, horizontalSplitPosition);
            ruleIndex += horizontalSplitPosition;
        }
        int bottomPanelWidth = createColumnStyles(sizeStyleRules, ruleIndex,
                horizontalSplitPosition + 1, actionHandler.getMaximumCols());

        resetStyleSheetRules(cellSizeAndPositionStyle, sizeStyleRules);

        updateSheetPanePositions();

        if (topFrozenPanelHeightPt > 0 && leftFrozenPanelWidth > 0) {
            topLeftPane.removeClassName(FREEZE_PANE_INACTIVE_STYLENAME);
        } else {
            topLeftPane.addClassName(FREEZE_PANE_INACTIVE_STYLENAME);
        }
        if (topFrozenPanelHeightPt > 0) {
            topRightPane.removeClassName(FREEZE_PANE_INACTIVE_STYLENAME);
        } else {
            topRightPane.addClassName(FREEZE_PANE_INACTIVE_STYLENAME);
        }
        if (leftFrozenPanelWidth > 0) {
            bottomLeftPane.removeClassName(FREEZE_PANE_INACTIVE_STYLENAME);
        } else {
            bottomLeftPane.addClassName(FREEZE_PANE_INACTIVE_STYLENAME);
        }

        String stylePrimaryName = getStylePrimaryName();
        // Styles for the header and selection widget location, scroll is faked
        // with margins. moveHeadersToMatchScroll handles updating.
        String[] headerMarginRules = new String[2];
        headerMarginRules[0] = "." + stylePrimaryName
                + " .bottom-left-pane { margin-top: 0px; }";
        headerMarginRules[1] = "." + stylePrimaryName
                + " .top-right-pane { margin-left: 0px; }";
        // headerMarginRules[2] = "." + stylePrimaryName
        // + " .sheet-selection.top-right, ." + stylePrimaryName
        // + " .sheet-selection.bottom-right { margin-left: 0px; }";
        // headerMarginRules[3] = "." + stylePrimaryName
        // + " .sheet-selection.bottom-right, ." + stylePrimaryName
        // + " .sheet-selection.bottom-left { margin-top: 0px; }";
        resetStyleSheetRules(headerMarginStyle, headerMarginRules);

        moveHeadersToMatchScroll();

        // update floater size the adjust scroll bars correctly
        floater.getStyle().setHeight(bottomPanelHeightPt, Unit.PT);
        floater.getStyle().setWidth(bottomPanelWidth, Unit.PX);
    }

    /**
     * Updates the left & top style property for sheet panes depending if
     * headers are shown or not.
     */
    private void updateSheetPanePositions() {
        int widthIncrease = 50;
        int heightIncrease = 19;
        int topOffset = 49;
        if (!displayRowColHeadings) {
            widthIncrease = 0;
            heightIncrease = 0;
            topOffset = 30;
        }
        Style style = topLeftPane.getStyle();
        style.setWidth(leftFrozenPanelWidth + widthIncrease, Unit.PX);
        style.setHeight(topFrozenPanelHeight + heightIncrease, Unit.PX);

        style = topRightPane.getStyle();
        // left offset is the same as the width increase
        style.setLeft(leftFrozenPanelWidth + widthIncrease, Unit.PX);
        style.setHeight(topFrozenPanelHeight + heightIncrease, Unit.PX);

        style = bottomLeftPane.getStyle();
        style.setWidth(leftFrozenPanelWidth + widthIncrease, Unit.PX);
        style.setTop(topFrozenPanelHeight + topOffset, Unit.PX);

        style = sheet.getStyle();
        style.setLeft(leftFrozenPanelWidth + widthIncrease, Unit.PX);
        style.setTop(topFrozenPanelHeight + topOffset, Unit.PX);
    }

    private void updateConditionalFormattingStyles() {
        Map<Integer, String> styles = actionHandler
                .getConditionalFormattingStyles();
        if (styles != null) {
            try {

                List<Integer> list = new ArrayList<Integer>(styles.keySet());
                Collections.sort(list);

                for (int i = 0; i < list.size(); i++) {

                    Integer key = list.get(i);
                    String val = styles.get(key);

                    jsniUtil.insertRule(sheetStyle,
                            ".v-spreadsheet .sheet .cell.cf" + key + " {" + val
                                    + "}");
                }
            } catch (Exception e) {
                debugConsole
                        .severe("SheetWidget:updateConditionalFormattingStyles: "
                                + e.toString()
                                + " while creating the cell styles");
            }
        }
    }

    private void updateCellStyles() {
        // styles for individual cells
        Map<Integer, String> styles = actionHandler.getCellStyleToCSSStyle();
        if (styles != null) {
            try {
                for (Entry<Integer, String> entry : styles.entrySet()) {
                    if (entry.getKey() == 0) {
                        jsniUtil.insertRule(
                                sheetStyle,
                                ".v-spreadsheet .sheet .cell {"
                                        + entry.getValue() + "}");
                    } else {
                        jsniUtil.insertRule(
                                sheetStyle,
                                ".v-spreadsheet .sheet .cell.cs"
                                        + entry.getKey() + " {"
                                        + entry.getValue() + "}");
                    }
                }
            } catch (Exception e) {
                debugConsole.severe("SheetWidget:updateStyles: " + e.toString()
                        + " while creating the cell styles");
            }
        }
        recalculateCellStyleWidthValues();
    }

    /**
     * Recalculates the width needed for each cell style for showing numbers.
     */
    private void recalculateCellStyleWidthValues() {
        Set<Integer> keys = actionHandler.getCellStyleToCSSStyle().keySet();
        HashMap<Integer, Float> cellStyleWidthRatioMap = new HashMap<Integer, Float>();
        sheet.appendChild(fontWidthDummyElement);
        for (Integer key : keys) {
            fontWidthDummyElement.setClassName("cell cs" + key);
            int clientWidth = fontWidthDummyElement.getClientWidth();
            cellStyleWidthRatioMap.put(key,
                    new BigDecimal(clientWidth).divide(new BigDecimal(10))
                            .floatValue());
        }
        fontWidthDummyElement.removeFromParent();
        actionHandler.setCellStyleWidthRatios(cellStyleWidthRatioMap);
    }

    private void removeFrozenHeaders(ArrayList<DivElement> headers) {
        for (DivElement e : headers) {
            e.removeFromParent();
        }
        headers.clear();
    }

    private void resetFrozenColumnHeaders() {
        if (horizontalSplitPosition < frozenColumnHeaders.size()) {
            // remove extra
            while (frozenColumnHeaders.size() > horizontalSplitPosition) {
                frozenColumnHeaders.remove(frozenColumnHeaders.size() - 1)
                        .removeFromParent();
            }
        } else { // add as many as needed
            for (int i = frozenColumnHeaders.size() + 1; i <= horizontalSplitPosition; i++) {
                DivElement colHeader = Document.get().createDivElement();
                colHeader.setInnerHTML(actionHandler.getColHeader(i)
                        + createHeaderDNDHTML());
                colHeader.setClassName("ch col" + (i));
                frozenColumnHeaders.add(colHeader);
                topLeftPane.appendChild(colHeader);
            }
        }
    }

    private void resetFrozenRowHeaders() {
        if (verticalSplitPosition < frozenRowHeaders.size()) {
            // remove extra
            while (frozenRowHeaders.size() > verticalSplitPosition) {
                frozenRowHeaders.remove(frozenRowHeaders.size() - 1)
                        .removeFromParent();
            }
        } else { // add as many as needed
            for (int i = frozenRowHeaders.size() > 0 ? frozenRowHeaders.size()
                    : 1; i <= verticalSplitPosition; i++) {
                DivElement rowHeader = Document.get().createDivElement();
                rowHeader.setInnerHTML(actionHandler.getRowHeader(i)
                        + createHeaderDNDHTML());
                rowHeader.setClassName("rh row" + (i));
                frozenRowHeaders.add(rowHeader);
                topLeftPane.appendChild(rowHeader);
            }
        }
    }

    /**
     * Update the column headers to match the state. Create and recycle header
     * divs as needed.
     */
    private void resetColHeaders() {
        if (frozenColumnHeaders != null) {
            if (horizontalSplitPosition > 0) {
                resetFrozenColumnHeaders();
            } else {
                removeFrozenHeaders(frozenColumnHeaders);
                frozenColumnHeaders = null;
            }
        } else if (horizontalSplitPosition > 0) {
            frozenColumnHeaders = new ArrayList<DivElement>();
            resetFrozenColumnHeaders();
        }

        for (int i = firstColumnIndex; i <= lastColumnIndex; i++) {
            if (i > horizontalSplitPosition) {
                DivElement colHeader;
                if (i - firstColumnIndex < colHeaders.size()) {
                    colHeader = colHeaders.get(i - firstColumnIndex);
                } else {
                    colHeader = Document.get().createDivElement();
                    topRightPane.appendChild(colHeader);
                    colHeaders.add(i - firstColumnIndex, colHeader);
                }
                colHeader.setClassName("ch col" + (i));
                colHeader.setInnerHTML(actionHandler.getColHeader(i)
                        + createHeaderDNDHTML());
            } else {
                debugConsole.severe("Trying to add plain column header (index:"
                        + i + ") into frozen pane, horizontalSplitPosition: "
                        + horizontalSplitPosition);
            }
        }
        while (colHeaders.size() > (lastColumnIndex - firstColumnIndex + 1)) {
            colHeaders.remove(colHeaders.size() - 1).removeFromParent();
        }
    }

    /**
     * Update the row headers to match the state. Create and recycle header divs
     * as needed.
     */
    private void resetRowHeaders() {
        if (frozenRowHeaders != null) {
            if (verticalSplitPosition > 0) {
                resetFrozenRowHeaders();
            } else {
                removeFrozenHeaders(frozenRowHeaders);
                frozenRowHeaders = null;
            }
        } else if (verticalSplitPosition > 0) {
            frozenRowHeaders = new ArrayList<DivElement>();
            resetFrozenRowHeaders();
        }

        for (int i = firstRowIndex; i <= lastRowIndex; i++) {
            if (verticalSplitPosition < i) {
                DivElement rowHeader;
                if (i - firstRowIndex < rowHeaders.size()) {
                    rowHeader = rowHeaders.get(i - firstRowIndex);
                } else {
                    rowHeader = Document.get().createDivElement();
                    bottomLeftPane.appendChild(rowHeader);
                    rowHeaders.add(i - firstRowIndex, rowHeader);
                }
                rowHeader.setClassName("rh row" + (i));
                rowHeader.setInnerHTML(actionHandler.getRowHeader(i)
                        + createHeaderDNDHTML());
            } else {
                debugConsole.severe("Trying to add plain row header (index:"
                        + i + ") into frozen pane, verticalSplitPosition: "
                        + verticalSplitPosition);
            }
        }
        // Remove unused headers
        while (rowHeaders.size() > (lastRowIndex - firstRowIndex + 1)) {
            rowHeaders.remove(rowHeaders.size() - 1).removeFromParent();
        }
    }

    private void resetScrollView(int scrollLeft, int scrollTop) {
        sheet.setScrollLeft(scrollLeft);
        sheet.setScrollTop(scrollTop);
        scrollViewHeight = sheet.getOffsetHeight();
        scrollViewWidth = sheet.getOffsetWidth();
        previousScrollLeft = scrollLeft;
        previousScrollTop = scrollTop;
        firstRowIndex = 1;
        firstRowPosition = 0;
        if (verticalSplitPosition > 0) {
            firstRowIndex = verticalSplitPosition + 1;
        }
        firstColumnIndex = 1;
        firstColumnPosition = 0;
        if (horizontalSplitPosition > 0) {
            firstColumnIndex = horizontalSplitPosition + 1;
        }
        lastColumnIndex = 0;
        clearSelectedCellStyle();
        clearCellRangeStyles();
        // move the indexes to the correct scroll position
        int columnBufferSize = actionHandler.getColumnBufferSize();
        if (firstColumnPosition < (scrollLeft - columnBufferSize)) {
            do {
                firstColumnPosition += actionHandler
                        .getColWidthActual(firstColumnIndex);
                firstColumnIndex++;
            } while (firstColumnPosition < (scrollLeft - columnBufferSize));
        }
        lastColumnIndex = firstColumnIndex;
        lastColumnPosition = firstColumnPosition
                + actionHandler.getColWidthActual(firstColumnIndex);
        int rowBufferSize = actionHandler.getRowBufferSize();
        if (firstRowPosition < (scrollTop - rowBufferSize)) {
            do {
                if (firstRowIndex >= actionHandler.getDefinedRows()) {
                    firstRowPosition += getDefaultRowHeight();
                } else {
                    // firstRowPosition += convertPointsToPixel(actionHandler
                    // .getRowHeight(firstRowIndex));
                    firstRowPosition += getRowHeight(firstRowIndex);
                }
                firstRowIndex++;
            } while (firstRowPosition < (scrollTop - rowBufferSize));
        }
        lastRowIndex = firstRowIndex;
        lastRowPosition = firstRowPosition + getRowHeight(lastRowIndex);

        // count how many columns fit to view on first view
        while (lastColumnPosition < (scrollLeft + scrollViewWidth + columnBufferSize)
                && lastColumnIndex < actionHandler.getMaximumCols()) {
            lastColumnIndex++;
            lastColumnPosition += actionHandler
                    .getColWidthActual(lastColumnIndex);
        }

        // count how many rows should be displayed
        while (lastRowPosition < (scrollTop + scrollViewHeight + rowBufferSize)
                && lastRowIndex < actionHandler.getMaximumRows()) {
            lastRowIndex++;
            if (lastRowIndex >= actionHandler.getDefinedRows()) {
                lastRowPosition += getDefaultRowHeight();
            } else {
                lastRowPosition += getRowHeight(lastRowIndex);
            }
        }
    }

    private void resetCellContents() {
        clearListOfCells(topLeftCells);
        for (ArrayList<Cell> row : topRightRows) {
            clearListOfCells(row);
        }
        topRightRows.clear();
        for (ArrayList<Cell> row : bottomLeftRows) {
            clearListOfCells(row);
        }
        bottomLeftRows.clear();
        // Remove old cells
        for (ArrayList<Cell> row : rows) {
            clearListOfCells(row);
        }
        rows.clear();
        sheet.appendChild(floater);

        // create freeze panes' cells
        if (verticalSplitPosition > 0 && horizontalSplitPosition > 0) {
            createTopLeftPaneCells();
            createTopRightPaneCells();
            createBottomLeftPaneCells();
        } else if (verticalSplitPosition > 0) {
            createTopRightPaneCells();
        } else if (horizontalSplitPosition > 0) {
            createBottomLeftPaneCells();
        }

        for (int i = firstRowIndex; i <= lastRowIndex; i++) {
            ArrayList<Cell> row = new ArrayList<Cell>(lastColumnIndex);
            for (int j = firstColumnIndex; j <= lastColumnIndex; j++) {
                Cell cell = new Cell(j, i);
                sheet.appendChild(cell.getElement());
                row.add(cell);
            }
            rows.add(row);
        }
    }

    private void createBottomLeftPaneCells() {
        for (int v = verticalSplitPosition > 0 ? verticalSplitPosition + 1 : 1; v <= lastRowIndex; v++) {
            ArrayList<Cell> row = new ArrayList<Cell>();
            for (int h = 1; h <= horizontalSplitPosition; h++) {
                Cell cell = new Cell(h, v);
                bottomLeftPane.appendChild(cell.getElement());
                row.add(cell);
            }
            bottomLeftRows.add(row);
        }
    }

    private void createTopRightPaneCells() {
        for (int v = 1; v <= verticalSplitPosition; v++) {
            ArrayList<Cell> row = new ArrayList<Cell>();
            for (int h = horizontalSplitPosition > 0 ? horizontalSplitPosition + 1
                    : 1; h <= lastColumnIndex; h++) {
                Cell cell = new Cell(h, v);
                topRightPane.appendChild(cell.getElement());
                row.add(cell);
            }
            topRightRows.add(row);
        }
    }

    private void createTopLeftPaneCells() {
        for (int v = 1; v <= verticalSplitPosition; v++) {
            for (int h = 1; h <= horizontalSplitPosition; h++) {
                Cell cell = new Cell(h, v);
                topLeftPane.appendChild(cell.getElement());
                topLeftCells.add(cell);
            }
        }
    }

    private void clearListOfCells(ArrayList<Cell> row) {
        for (Cell cell : row) {
            cell.getElement().removeFromParent();
        }
        row.clear();
    }

    /**
     * Update the headers and cells in the spreadsheet to reflect the current
     * view area. Runs the escalator (if needed) and requests cell data from
     * handler (if needed).
     */
    private void onSheetScroll() {

        int scrollTop = sheet.getScrollTop();
        int scrollLeft = sheet.getScrollLeft();
        int vScrollDiff = scrollTop - previousScrollTop;
        int hScrollDiff = scrollLeft - previousScrollLeft;

        if (Math.abs(vScrollDiff) < (actionHandler.getRowBufferSize() / 2)
                && Math.abs(hScrollDiff) < (actionHandler.getColumnBufferSize() / 2)) {
            return;
        }

        try {
            if (Math.abs(hScrollDiff) > (actionHandler.getColumnBufferSize() / 2)) {
                previousScrollLeft = scrollLeft;
                if (hScrollDiff > 0) {
                    handleHorizontalScrollRight(scrollLeft);
                } else if (hScrollDiff < 0) {
                    handleHorizontalScrollLeft(scrollLeft);
                }
            }

            if (Math.abs(vScrollDiff) > (actionHandler.getRowBufferSize() / 2)) {
                previousScrollTop = scrollTop;
                if (vScrollDiff > 0) {
                    handleVerticalScrollDown(scrollTop);
                } else if (vScrollDiff < 0) {
                    handleVerticalScrollUp(scrollTop);
                }
            }
            requester.trigger();
        } catch (Throwable t) {
            debugConsole.severe("SheetWidget:updateSheetDisplay: "
                    + t.toString());
        }
        // update cells
        updateCells(vScrollDiff, hScrollDiff);
    }

    private void runEscalatorOnAllCells(int r1, int r2, int c1, int c2,
            ArrayList<ArrayList<Cell>> rows, Element paneElement) {
        // run escalator on all rows&columns, remove if necessary
        for (int r = r1; r <= r2; r++) {
            final ArrayList<Cell> row;
            // run escalator vertically
            if (rows.size() > (r - r1)) {
                row = rows.get(r - r1);
            } else {
                row = new ArrayList<Cell>();
                row.ensureCapacity(c2 - c1 + 1);
                rows.add(r - r1, row);
            }
            // run escalator horizontally:
            for (int c = c1; c <= c2; c++) {
                final Cell cell;
                if (row.size() > (c - c1)) {
                    cell = row.get(c - c1);
                    cell.update(c, r, getCellData(c, r));
                } else {
                    cell = new Cell(c, r, getCellData(c, r));
                    paneElement.appendChild(cell.getElement());
                    row.add(c - c1, cell);
                }
            }
            while (row.size() > (c2 - c1 + 1)) {
                row.remove(row.size() - 1).getElement().removeFromParent();
            }
        }
        while (rows.size() > r2 - r1 + 1) {
            for (Cell cell : rows.remove(rows.size() - 1)) {
                cell.getElement().removeFromParent();
            }
        }
    }

    private void runEscalatorPartially(int vScrollDiff, int hScrollDiff,
            int r1, int r2, int c1, int c2, ArrayList<ArrayList<Cell>> rows,
            Element paneElement) {
        int firstR = rows.get(0).get(0).getRow();
        int lastR = rows.get(rows.size() - 1).get(0).getRow();
        int firstC = rows.get(0).get(0).getCol();
        int lastC = rows.get(0).get(rows.get(0).size() - 1).getCol();
        // run escalator on some rows/cells
        ArrayList<ArrayList<Cell>> tempRows = new ArrayList<ArrayList<Cell>>();
        for (Iterator<ArrayList<Cell>> iterator = rows.iterator(); iterator
                .hasNext();) {
            final ArrayList<Cell> row = iterator.next();
            int rIndex = row.get(0).getRow();
            // FIND OUT IF ROW INDEX HAS CHANGED FOR THE ROW (swap/remove)
            // scroll down
            if (vScrollDiff > 0) {
                if (rIndex < r1) {
                    // swap or remove
                    if (lastR < r2) {
                        // swap row to bottom
                        rIndex = ++lastR;
                        iterator.remove();
                        tempRows.add(row);
                    } else {
                        // remove row
                        for (Cell cell : row) {
                            cell.getElement().removeFromParent();
                        }
                        iterator.remove();
                        continue;
                    }
                }
            } // scroll up
            else if (vScrollDiff < 0) {
                // swap or remove
                if (rIndex > r2) {
                    if (firstR > r1) {
                        // swap from bottom to top
                        rIndex = --firstR;
                        iterator.remove();
                        tempRows.add(row);
                    } else {
                        // remove row
                        for (Cell cell : row) {
                            cell.getElement().removeFromParent();
                        }
                        iterator.remove();
                        continue;
                    }
                }
            }
            firstC = row.get(0).getCol();
            lastC = row.get(row.size() - 1).getCol();
            final ArrayList<Cell> tempCols = new ArrayList<Cell>();
            for (Iterator<Cell> cells = row.iterator(); cells.hasNext();) {
                Cell cell = cells.next();
                int cIndex = cell.getCol();
                // scroll right
                if (hScrollDiff > 0) {
                    // move cells from left to right
                    if (cIndex < c1) {
                        // swap or remove
                        if (lastC < c2) {
                            // swap cell to right
                            cIndex = ++lastC;
                            cells.remove();
                            tempCols.add(cell);
                        } else {
                            // remove cell
                            cell.getElement().removeFromParent();
                            cells.remove();
                            continue;
                        }
                    }
                } else if (hScrollDiff < 0) { // scroll left
                    // move cells from right to left
                    if (cIndex > c2) {
                        // swap or remove
                        if (firstC > c1) {
                            // swap cell to right
                            cIndex = --firstC;
                            cells.remove();
                            tempCols.add(cell);
                        } else {
                            // remove cell
                            cell.getElement().removeFromParent();
                            cells.remove();
                            continue;
                        }
                    }
                }

                if (cIndex != cell.getCol() || rIndex != cell.getRow()) {
                    cell.update(cIndex, rIndex, getCellData(cIndex, rIndex));
                }
            }
            if (hScrollDiff > 0) {
                // add moved cells to collection
                for (Cell cell : tempCols) {
                    row.add(cell);
                }
                // add new cells if required
                while (lastC < c2) {
                    lastC++;
                    Cell cell = new Cell(lastC, rIndex, getCellData(lastC,
                            rIndex));
                    paneElement.appendChild(cell.getElement());
                    row.add(cell);
                }
            } else if (hScrollDiff < 0) {
                // add moved cells to collection
                for (Cell cell : tempCols) {
                    row.add(0, cell);
                }
                // add new cells if required
                while (firstC > c1) {
                    firstC--;
                    Cell cell = new Cell(firstC, rIndex, getCellData(firstC,
                            rIndex));
                    paneElement.appendChild(cell.getElement());
                    row.add(0, cell);
                }
            }
        }
        // add moved rows to collection
        if (vScrollDiff > 0) {
            for (ArrayList<Cell> row : tempRows) {
                rows.add(row);
            }
        } else {
            for (ArrayList<Cell> row : tempRows) {
                rows.add(0, row);
            }
        }

        // add new rows if necessary
        if (vScrollDiff > 0) {
            while (lastR < r2) {
                ArrayList<Cell> row = new ArrayList<Cell>(c2 - c1 + 1);
                lastR++;
                for (int i = c1; i <= c2; i++) {
                    Cell cell = new Cell(i, lastR, getCellData(i, lastR));
                    row.add(cell);
                    paneElement.appendChild(cell.getElement());
                }
                rows.add(row);
            }
        } else if (vScrollDiff < 0) {
            while (firstR > r1) {
                ArrayList<Cell> row = new ArrayList<Cell>();
                row.ensureCapacity(c2 - c1 + 1);
                firstR--;
                for (int i = c1; i <= c2; i++) {
                    Cell cell = new Cell(i, firstR, getCellData(i, firstR));
                    row.add(cell);
                    paneElement.appendChild(cell.getElement());
                }
                rows.add(0, row);
            }
        }
    }

    /** push the cells to the escalator */
    private void updateCells(int vScrollDiff, int hScrollDiff) {
        Cell firstCell = rows.get(0).get(0);
        ArrayList<Cell> lastRow = rows.get(rows.size() - 1);
        Cell lastCell = lastRow.get(lastRow.size() - 1);
        int firstR = firstCell.getRow();
        int lastR = lastCell.getRow();
        int firstC = firstCell.getCol();
        int lastC = lastCell.getCol();
        if (firstR > lastRowIndex || lastR < firstRowIndex
                || firstC > lastColumnIndex || lastC < firstColumnIndex) {
            // big scroll
            runEscalatorOnAllCells(firstRowIndex, lastRowIndex,
                    firstColumnIndex, lastColumnIndex, rows, sheet);
            if (vScrollDiff != 0 && horizontalSplitPosition > 0) {
                runEscalatorOnAllCells(firstRowIndex, lastRowIndex, 1,
                        horizontalSplitPosition, bottomLeftRows, bottomLeftPane);
            }
            if (hScrollDiff != 0 && verticalSplitPosition > 0) {
                runEscalatorOnAllCells(1, verticalSplitPosition,
                        firstColumnIndex, lastColumnIndex, topRightRows,
                        topRightPane);
            }
        } else {
            runEscalatorPartially(vScrollDiff, hScrollDiff, firstRowIndex,
                    lastRowIndex, firstColumnIndex, lastColumnIndex, rows,
                    sheet);
            if (vScrollDiff != 0 && horizontalSplitPosition > 0) {
                runEscalatorPartially(vScrollDiff, 0, firstRowIndex,
                        lastRowIndex, 1, horizontalSplitPosition,
                        bottomLeftRows, bottomLeftPane);
            }
            if (hScrollDiff != 0 && verticalSplitPosition > 0) {
                runEscalatorPartially(0, hScrollDiff, 1, verticalSplitPosition,
                        firstColumnIndex, lastColumnIndex, topRightRows,
                        topRightPane);
            }
        }
    }

    private void handleHorizontalScrollLeft(int scrollLeft) {
        int columnBufferSize = actionHandler.getColumnBufferSize();
        int leftBound = scrollLeft - columnBufferSize;
        int rightBound = scrollLeft + scrollViewWidth + columnBufferSize;
        if (leftBound < 0) {
            leftBound = 0;
        }
        int move = 0;
        int add = 0;
        int maxFirstColumn = horizontalSplitPosition + 1; // hSP is 0 when no
                                                          // split
        while (firstColumnPosition > leftBound
                && firstColumnIndex > maxFirstColumn) {
            if (lastColumnPosition
                    - actionHandler.getColWidthActual(lastColumnIndex) > rightBound) {
                // move column from right to left
                move++;
                lastColumnPosition -= actionHandler
                        .getColWidthActual(lastColumnIndex);
                lastColumnIndex--;
            } else {
                // add column to left
                add++;
            }
            firstColumnIndex--;
            firstColumnPosition -= actionHandler
                    .getColWidthActual(firstColumnIndex);
        }
        if (firstColumnPosition <= 0 || firstColumnIndex <= 1) {
            firstColumnPosition = 0;
            firstColumnIndex = maxFirstColumn;
        }
        while (rightBound < (lastColumnPosition - actionHandler
                .getColWidthActual(lastColumnIndex)) && lastColumnIndex > 1) {
            lastColumnPosition -= actionHandler
                    .getColWidthActual(lastColumnIndex);
            lastColumnIndex--;
        }
        if (move > colHeaders.size()) {
            resetColHeaders();
        } else {
            while (move + add > 0) {
                DivElement header;
                if (move > 0) {
                    move--;
                    header = colHeaders.remove(colHeaders.size() - 1);
                    header.setInnerHTML(actionHandler
                            .getColHeader(firstColumnIndex + (move + add))
                            + createHeaderDNDHTML());
                    header.setClassName("ch col"
                            + Integer.toString(firstColumnIndex + (move + add)));
                } else {
                    add--;
                    header = Document.get().createDivElement();
                    header.setInnerHTML(actionHandler
                            .getColHeader(firstColumnIndex + add)
                            + createHeaderDNDHTML());
                    header.setClassName("ch col"
                            + Integer.toString(firstColumnIndex + add));
                    topRightPane.appendChild(header);
                }
                colHeaders.add(0, header);
            }
            while (colHeaders.size() > (lastColumnIndex - firstColumnIndex + 1)) {
                colHeaders.remove(colHeaders.size() - 1).removeFromParent();
            }
        }
    }

    /**
     * Calculates viewed cells after a scroll to right. Runs the escalator for
     * column headers.
     * 
     * @param scrollLeft
     */
    private void handleHorizontalScrollRight(int scrollLeft) {
        int columnBufferSize = actionHandler.getColumnBufferSize();
        int leftBound = scrollLeft - columnBufferSize;
        int rightBound = scrollLeft + scrollViewWidth + columnBufferSize;
        if (leftBound < 0) {
            leftBound = 0;
        }
        int move = 0;
        int add = 0;
        final int maximumCols = actionHandler.getMaximumCols();
        while (lastColumnPosition < rightBound && lastColumnIndex < maximumCols) {
            if ((firstColumnPosition + actionHandler
                    .getColWidthActual(firstColumnIndex)) < leftBound) {
                // move column from left to right
                move++;
                firstColumnPosition += actionHandler
                        .getColWidthActual(firstColumnIndex);
                firstColumnIndex++;
            } else {
                // add column to right
                add++;
            }
            lastColumnIndex++;
            lastColumnPosition += actionHandler
                    .getColWidthActual(lastColumnIndex);
        }
        while (leftBound > (firstColumnPosition + actionHandler
                .getColWidthActual(firstColumnIndex))
                && firstColumnIndex < maximumCols) {
            firstColumnPosition += actionHandler
                    .getColWidthActual(firstColumnIndex);
            firstColumnIndex++;
        }
        if (move > colHeaders.size()) {
            resetColHeaders();
        } else {
            while (move + add > 0) {
                DivElement header;
                if (move > 0) {
                    move--;
                    header = colHeaders.remove(0);
                    header.setInnerHTML(actionHandler
                            .getColHeader(lastColumnIndex - (move + add))
                            + createHeaderDNDHTML());
                    header.setClassName("ch col"
                            + Integer.toString(lastColumnIndex - (move + add)));
                } else {
                    add--;
                    header = Document.get().createDivElement();
                    header.setInnerHTML(actionHandler
                            .getColHeader(lastColumnIndex - add)
                            + createHeaderDNDHTML());
                    header.setClassName("ch col"
                            + Integer.toString(lastColumnIndex - add));
                    topRightPane.appendChild(header);
                }
                colHeaders.add(header);
            }
            while (colHeaders.size() > (lastColumnIndex - firstColumnIndex + 1)) {
                colHeaders.remove(0).removeFromParent();
            }
        }
    }

    private void handleVerticalScrollDown(int scrollTop) {
        int rowBufferSize = actionHandler.getRowBufferSize();
        int topBound = scrollTop - rowBufferSize;
        int bottomBound = scrollTop + scrollViewHeight + rowBufferSize;
        if (topBound < 0) {
            topBound = 0;
        }
        int move = 0;
        int add = 0;
        final int maximumRows = actionHandler.getMaximumRows();
        while (lastRowPosition < bottomBound && lastRowIndex < maximumRows) {
            if ((firstRowPosition + getRowHeight(firstRowIndex)) < topBound) {
                // move row from top to bottom
                move++;
                firstRowPosition += getRowHeight(firstRowIndex);
                firstRowIndex++;
            } else {
                // add row to bottom
                add++;
            }
            lastRowIndex++;
            lastRowPosition += getRowHeight(lastRowIndex);
        }
        while (topBound > (firstRowPosition + getRowHeight(firstRowIndex))
                && firstRowIndex < maximumRows) {
            firstRowPosition += getRowHeight(firstRowIndex);
            firstRowIndex++;
        }
        if (move > rowHeaders.size()) {
            resetRowHeaders();
        } else {
            while (move + add > 0) {
                DivElement header;
                if (move > 0) {
                    move--;
                    header = rowHeaders.remove(0);
                    header.setInnerHTML(actionHandler.getRowHeader(lastRowIndex
                            - (move + add))
                            + createHeaderDNDHTML());
                    header.setClassName("rh row"
                            + Integer.toString(lastRowIndex - (move + add)));
                } else {
                    add--;
                    header = Document.get().createDivElement();
                    header.setInnerHTML(actionHandler.getRowHeader(lastRowIndex
                            - add)
                            + createHeaderDNDHTML());
                    header.setClassName("rh row"
                            + Integer.toString(lastRowIndex - add));
                    bottomLeftPane.appendChild(header);
                }
                rowHeaders.add(header);
            }
            while (rowHeaders.size() > (lastRowIndex - firstRowIndex + 1)) {
                rowHeaders.remove(0).removeFromParent();
            }
        }
    }

    private void handleVerticalScrollUp(int scrollTop) {
        int rowBufferSize = actionHandler.getRowBufferSize();
        int topBound = scrollTop - rowBufferSize;
        int bottomBound = scrollTop + scrollViewHeight + rowBufferSize;
        if (topBound < 0) {
            topBound = 0;
        }
        int move = 0;
        int add = 0;
        int maxTopRow = verticalSplitPosition + 1; // vSP is 0 when no split
        while (firstRowPosition > topBound && firstRowIndex > maxTopRow) {
            if ((lastRowPosition - getRowHeight(lastRowIndex)) > bottomBound) {
                // move column from bottom to top
                move++;
                lastRowPosition -= getRowHeight(lastRowIndex);
                lastRowIndex--;
            } else {
                // add column to top
                add++;
            }
            firstRowIndex--;
            firstRowPosition -= getRowHeight(firstRowIndex);
        }
        if (firstRowPosition <= 0 || firstRowIndex <= 1) {
            firstRowPosition = 0;
            firstRowIndex = maxTopRow;
        }
        while (bottomBound < (lastRowPosition - getRowHeight(lastRowIndex))
                && lastRowIndex > 1) {
            lastRowPosition -= getRowHeight(lastRowIndex);
            lastRowIndex--;
        }
        if (move > rowHeaders.size()) {
            resetRowHeaders();
        } else {
            while (move + add > 0) {

                DivElement header;
                if (move > 0) {
                    move--;
                    header = rowHeaders.remove(rowHeaders.size() - 1);
                    header.setInnerHTML(actionHandler
                            .getRowHeader(firstRowIndex + (move + add))
                            + createHeaderDNDHTML());
                    header.setClassName("rh row"
                            + Integer.toString(firstRowIndex + (move + add)));
                } else {
                    add--;
                    header = Document.get().createDivElement();
                    header.setInnerHTML(actionHandler
                            .getRowHeader(firstRowIndex + add)
                            + createHeaderDNDHTML());
                    header.setClassName("rh row"
                            + Integer.toString(firstRowIndex + add));
                    bottomLeftPane.appendChild(header);
                }
                rowHeaders.add(0, header);
            }
            while (rowHeaders.size() > (lastRowIndex - firstRowIndex + 1)) {
                rowHeaders.remove(rowHeaders.size() - 1).removeFromParent();
            }
        }
    }

    public boolean isSelectedCellCustomized() {
        return customWidgetMap != null
                && customWidgetMap.containsKey(getSelectedCellKey());
    }

    public void showCustomWidgets(HashMap<String, Widget> customWidgetMap) {
        for (int r = firstRowIndex; r <= lastRowIndex; r++) {
            ArrayList<Cell> row = rows.get(r - firstRowIndex);
            for (int c = firstColumnIndex; c <= lastColumnIndex; c++) {
                final String key = toKey(c, r);
                if (customWidgetMap.containsKey(key)) {
                    Cell cell;
                    if (isMergedCell(key)) {
                        cell = getMergedCell(key);

                    } else {
                        cell = row.get(c - firstColumnIndex);
                    }
                    Widget customWidget = customWidgetMap.get(key);
                    addCustomWidgetToCell(cell, customWidget);
                }
            }
        }
        this.customWidgetMap = customWidgetMap;
    }

    private void addCustomWidgetToCell(Cell cell, Widget customWidget) {
        cell.setValue(null);
        Widget parent = customWidget.getParent();
        if (parent != null) {
            if (equals(parent)) {
                cell.getElement().appendChild(customWidget.getElement());
            } else {
                customWidget.removeFromParent();
                cell.getElement().appendChild(customWidget.getElement());
                adopt(customWidget);
            }
        } else {
            cell.getElement().appendChild(customWidget.getElement());
            adopt(customWidget);
        }
    }

    public void addSheetImage(String key, SheetImage image) {
        sheet.appendChild(image.getElement());
        adopt(image);
        sheetImages.put(key, image);
    }

    public SheetImage getSheetImage(String key) {
        return sheetImages.get(key);
    }

    public void removeSheetImage(String key) {
        SheetImage image = sheetImages.remove(key);
        remove(image);
    }

    public void addMergedRegion(MergedRegion region) {
        StringBuilder sb = new StringBuilder();
        for (int r = region.row1; r <= region.row2; r++) {
            for (int c = region.col1; c <= region.col2; c++) {
                sb.append(toCssKey(c, r));
                if (r != region.row2 || c != region.col2) {
                    sb.append(",");
                }
            }
        }
        if (sb.length() != 0) {
            sb.append(MERGED_REGION_CELL_STYLE);
            jsniUtil.insertRule(mergedRegionStyle, sb.toString());
        }
        String key = toKey(region.col1, region.row1);
        MergedCell mergedCell = new MergedCell(region.col1, region.row1);
        DivElement element = mergedCell.getElement();
        element.addClassName(MERGED_CELL_CLASSNAME);
        updateMergedRegionRegionSize(region, mergedCell);

        getPaneElementForCell(region.col1, region.row1).appendChild(element);
        mergedCells.put(region.id, mergedCell);

        // need to update the possible cell comment for the merged cell
        if (cellCommentsMap != null && cellCommentsMap.containsKey(key)) {
            mergedCell.showCellCommentMark();
        }
        if (alwaysVisibleCellComments.containsKey(key)) {
            CellComment cellComment = alwaysVisibleCellComments.get(key);
            cellComment.showDependingToCellRightCorner(
                    (Element) element.cast(), region.row1, region.col1);
        }
        // need to update the possible custom widget for the merged cell
        if (customWidgetMap != null && customWidgetMap.containsKey(key)) {
            Widget customWidget = customWidgetMap.get(key);
            addCustomWidgetToCell(mergedCell, customWidget);
        }
    }

    private void updateOverflownMergedCellSizes() {
        for (Entry<MergedRegion, Cell> entry : overflownMergedCells.entrySet()) {
            recalculateOverflownMergedCellHeight(entry.getKey(),
                    entry.getValue());
            recalculateOverflownMergedCellWidth(entry.getKey(),
                    entry.getValue());
        }
    }

    private void recalculateOverflownMergedCellWidth(MergedRegion region,
            Cell cell) {
        if (region.col1 <= horizontalSplitPosition
                && region.col2 > horizontalSplitPosition) {
            int[] colWidths = actionHandler.getColWidths();
            int width = selectionWidget.countSum(colWidths, region.col1,
                    horizontalSplitPosition + 1);
            int extraWidth = selectionWidget.countSum(colWidths,
                    horizontalSplitPosition + 1, region.col2 + 1)
                    - sheet.getScrollLeft() + 1;
            if (extraWidth > 0) {
                width += extraWidth;
                cell.getElement().getStyle().clearProperty("borderRight");
            } else {
                cell.getElement().getStyle().setProperty("borderRight", "0");
            }
            cell.getElement().getStyle().setWidth(width, Unit.PX);
        }
    }

    private void recalculateOverflownMergedCellHeight(MergedRegion region,
            Cell cell) {
        if (region.row1 <= verticalSplitPosition
                && region.row2 > verticalSplitPosition) {
            float[] rowHeights = actionHandler.getRowHeights();
            double height = selectionWidget.countSum(rowHeights, region.row1,
                    verticalSplitPosition + 1);
            double extraHeight = selectionWidget.countSum(rowHeights,
                    verticalSplitPosition + 1, region.row2 + 1)
                    + 1.0D
                    - convertPixelsToPoint(sheet.getScrollTop());
            if (extraHeight > 0) {
                height += extraHeight;
                cell.getElement().getStyle().clearProperty("borderBottom");
            } else {
                cell.getElement().getStyle().setProperty("borderBottom", "0");
            }
            cell.getElement().getStyle().setHeight(height, Unit.PT);
        }
    }

    private Element getPaneElementForCell(int col1, int row1) {
        if (col1 <= horizontalSplitPosition) {
            if (row1 <= verticalSplitPosition) {
                return topLeftPane;
            } else {
                return bottomLeftPane;
            }
        } else if (row1 <= verticalSplitPosition) {
            if (col1 <= horizontalSplitPosition) {
                return topLeftPane;
            } else {
                return topRightPane;
            }

        }
        return sheet;
    }

    private void updateMergedRegionRegionSize(MergedRegion region,
            Cell mergedCell) {
        int width = 0;
        float height = 0;
        DivElement element = mergedCell.getElement();
        if (horizontalSplitPosition >= region.col1
                && region.col2 > horizontalSplitPosition) {
            recalculateOverflownMergedCellWidth(region, mergedCell);
            overflownMergedCells.put(region, mergedCell);
            width = 1;
        } else {
            width = selectionWidget.countSum(actionHandler.getColWidths(),
                    region.col1, region.col2 + 1);
            element.getStyle().setWidth(width, Unit.PX);
        }
        if (verticalSplitPosition >= region.row1
                && region.row2 > verticalSplitPosition) {
            recalculateOverflownMergedCellHeight(region, mergedCell);
            overflownMergedCells.put(region, mergedCell);
            height = 1.0F;
        } else {
            height = selectionWidget.countSum(actionHandler.getRowHeights(),
                    region.row1, region.row2 + 1);
            element.getStyle().setHeight(height, Unit.PT);
        }

        if (width == 0 || height == 0) {
            mergedCell.getElement().getStyle().setDisplay(Display.NONE);
        } else {
            mergedCell.getElement().getStyle().setDisplay(Display.BLOCK);
        }
    }

    public void updateMergedRegionSize(MergedRegion region) {
        String key = toKey(region.col1, region.row1);
        Cell mergedCell = mergedCells.get(region.id);
        overflownMergedCells.remove(region);
        updateMergedRegionRegionSize(region, mergedCell);
        DivElement element = mergedCell.getElement();
        // need to update the position of possible visible comment for merged
        // cell
        if (alwaysVisibleCellComments.containsKey(key)) {
            CellComment cellComment = alwaysVisibleCellComments.get(key);
            if (element.getStyle().getDisplay()
                    .equals(Display.NONE.getCssName())) {
                cellComment.hide();
            } else {
                cellComment.refreshPositionAccordingToCellRightCorner();
            }
        }
    }

    public void removeMergedRegion(MergedRegion region, int ruleIndex) {
        String key = toKey(region.col1, region.row1);
        jsniUtil.deleteRule(mergedRegionStyle, ruleIndex);
        mergedCells.remove(region.id).getElement().removeFromParent();
        overflownMergedCells.remove(region);
        // paint new "released cells" as selected
        if (region.col1 >= selectionWidget.getCol1()
                && region.col2 <= selectionWidget.getCol2()
                && region.row1 >= selectionWidget.getRow1()
                && region.row2 <= selectionWidget.getRow2()) {
            updateSelectedCellStyles(region.col1, region.col2, region.row1,
                    region.row2, false);
        }
        DivElement cellCommentReplacementElement = null;
        // move the possible cell comment to the correct cell
        if (cellCommentsMap != null && cellCommentsMap.containsKey(key)) {
            try {
                Cell cell = rows.get(region.row1 - firstRowIndex).get(
                        region.col1 - firstColumnIndex);
                cell.showCellCommentMark();
                cellCommentReplacementElement = cell.getElement();
            } catch (Exception e) {
                // the cell just isn't visible, no problem.
            }
        }
        if (alwaysVisibleCellComments.containsKey(key)
                && cellCommentReplacementElement != null) {
            CellComment cellComment = alwaysVisibleCellComments.get(key);
            cellComment.showDependingToCellRightCorner(
                    (Element) cellCommentReplacementElement.cast(),
                    region.row1, region.col1);
        }
        if (customWidgetMap != null && customWidgetMap.containsKey(key)) {
            try {
                Cell cell = rows.get(region.row1 - firstRowIndex).get(
                        region.col1 - firstColumnIndex);
                Widget customWidget = customWidgetMap.get(key);
                addCustomWidgetToCell(cell, customWidget);
            } catch (Exception e) {
                // the cell just isn't visible, no problem.
            }
        }
    }

    public void setCellLinks(HashMap<String, String> cellLinksMap) {
        if (this.cellLinksMap == null) {
            this.cellLinksMap = cellLinksMap;
        } else {
            this.cellLinksMap.clear();
            if (cellLinksMap != null) {
                this.cellLinksMap.putAll(cellLinksMap);
            }
        }

        if (cellLinksMap != null && !cellLinksMap.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Iterator<String> i = cellLinksMap.keySet().iterator(); i
                    .hasNext();) {
                String cssKey = i.next().replace("col", ".col")
                        .replace(" r", ".r");
                sb.append(cssKey);
                if (i.hasNext()) {
                    sb.append(",");
                }
            }
            if (hyperlinkStyle == null) {
                hyperlinkStyle = Document.get().createStyleElement();
                hyperlinkStyle.setType("text/css");
                hyperlinkStyle.setId(sheetId + "-hyperlinkstyle");
                cellSizeAndPositionStyle.getParentElement().appendChild(
                        hyperlinkStyle);
                sb.append(HYPERLINK_CELL_STYLE);
                jsniUtil.insertRule(hyperlinkStyle, sb.toString());
            } else {
                jsniUtil.replaceSelector(hyperlinkStyle, sb.toString(), 0);
            }
        } else {
            if (hyperlinkStyle != null) {
                jsniUtil.replaceSelector(hyperlinkStyle, ".notusedselector", 0);
            }
        }
    }

    private boolean isMergedCell(String key) {
        for (Cell cell : mergedCells.values()) {
            if (key.equals(toKey(cell.getCol(), cell.getRow()))) {
                return true;
            }
        }
        return false;
    }

    private Cell getMergedCell(String key) {
        for (Cell cell : mergedCells.values()) {
            if (key.equals(toKey(cell.getCol(), cell.getRow()))) {
                return cell;
            }
        }
        return null;
    }

    public void setCellComments(HashMap<String, String> newCellCommentsMap) {
        updateRowCellComments(topLeftCells, newCellCommentsMap);
        updateRowsCellComments(topRightRows, newCellCommentsMap);
        updateRowsCellComments(bottomLeftRows, newCellCommentsMap);
        updateRowsCellComments(rows, newCellCommentsMap);
        updateMergedCellCommentsMap(newCellCommentsMap);
        if (cellCommentsMap != null) {
            cellCommentsMap.clear();
            if (newCellCommentsMap != null) {
                cellCommentsMap.putAll(newCellCommentsMap);
            }
        } else {
            cellCommentsMap = newCellCommentsMap;
        }
    }

    private void updateMergedCellCommentsMap(
            HashMap<String, String> newCellCommentsMap) {
        for (Cell mc : mergedCells.values()) {
            String key = toKey(mc.getCol(), mc.getRow());
            if (newCellCommentsMap != null
                    && newCellCommentsMap.containsKey(key)) {
                mc.showCellCommentMark();
            } else if (cellCommentsMap != null
                    && cellCommentsMap.containsKey(key)) {
                // remove
                mc.removeCellCommentMark();
            }
        }
    }

    private void updateRowCellComments(ArrayList<Cell> row,
            HashMap<String, String> newCellCommentsMap) {
        for (Cell cell : row) {
            String key = toKey(cell.getCol(), cell.getRow());
            if (newCellCommentsMap != null
                    && newCellCommentsMap.containsKey(key)) {
                cell.showCellCommentMark();
            } else if (cellCommentsMap != null
                    && cellCommentsMap.containsKey(key)) {
                // remove
                cell.removeCellCommentMark();
            }
        }
    }

    private void updateRowsCellComments(ArrayList<ArrayList<Cell>> rows,
            HashMap<String, String> newCellCommentsMap) {
        for (ArrayList<Cell> row : rows) {
            updateRowCellComments(row, newCellCommentsMap);
        }
    }

    public void setCellCommentVisible(boolean visible, String key) {
        if (visible) {
            jsniUtil.parseColRow(key);
            final Cell cell;
            int parsedRow = jsniUtil.getParsedRow();
            int parsedCol = jsniUtil.getParsedCol();
            if (isMergedCell(key)) {
                cell = getMergedCell(key);
            } else {
                cell = getCell(parsedCol, parsedRow);
            }
            final CellComment cellComment = new CellComment(this, cell
                    .getElement().getParentElement());
            cellComment.setCommentText(cellCommentsMap.get(key));
            cellComment.showDependingToCellRightCorner((Element) cell
                    .getElement().cast(), parsedRow, parsedCol);
            alwaysVisibleCellComments.put(key, cellComment);
        } else {
            CellComment comment = alwaysVisibleCellComments.remove(key);
            if (comment != null) { // possible if sheet has been cleared
                comment.hide();
            }
        }
    }

    public void refreshAlwaysVisibleCellCommentOverlays() {
        for (CellComment cellComment : alwaysVisibleCellComments.values()) {
            int row = cellComment.getRow();
            int col = cellComment.getCol();
            if (actionHandler.isColumnHidden(col)
                    || actionHandler.isRowHidden(row)
                    || !isCellRendered(col, row)) {
                cellComment.hide();
            } else {
                cellComment.refreshPositionAccordingToCellRightCorner();
            }
        }
    }

    public void refreshCurrentCellCommentOverlay() {
        if (cellCommentCellColumn != -1 && cellCommentCellRow != -1
                && cellCommentCellClassName != null) {
            cellCommentOverlay.refreshPositionAccordingToCellRightCorner();
        }
    }

    public void refreshPopupButtonOverlays() {
        if (sheetPopupButtons != null) {
            for (PopupButtonWidget pbw : sheetPopupButtons.values()) {
                if (pbw.isPopupOpen()) {
                    pbw.openPopup();
                }
            }
        }
    }

    protected void onCellCommentFocus(CellComment cellComment) {
        if (focusedCellCommentOverlay != null) {
            focusedCellCommentOverlay.pushBack();
        }
        cellComment.bringForward();
        focusedCellCommentOverlay = cellComment;
    }

    private void showCellComment(int column, int row) {
        String cellClassName = toKey(column, row);
        if (alwaysVisibleCellComments.containsKey(cellClassName)) {
            return;
        }
        final Element cellElement;
        if (isMergedCell(cellClassName)) {
            cellElement = getMergedCell(cellClassName).getElement().cast();
        } else {
            cellElement = getCell(column, row).getElement();
            cellCommentOverlay.setSheetElement(cellElement.getParentElement());
        }
        cellCommentOverlay.setCommentText(cellCommentsMap.get(cellClassName));
        cellCommentOverlay.show(cellElement, row, column);
        cellCommentCellClassName = cellClassName;
    }

    /**
     * Called when there is a MOUSEOVER or MOUSEOUT on a cell (cell element or
     * the triangle) that has a cell comment.
     * 
     * @param event
     */
    private void updateCellCommentDisplay(Event event, Element target) {
        int eventTypeInt = event.getTypeInt();
        String targetClassName = target.getClassName();
        if (targetClassName.equals(CELL_COMMENT_TRIANGLE_CLASSNAME)) {
            Element cellElement = target.getParentElement().cast();
            String cellElementClassName = cellElement.getClassName();
            if (cellElementClassName.endsWith(MERGED_CELL_CLASSNAME)) {
                cellElementClassName = cellElementClassName.replace(" "
                        + MERGED_CELL_CLASSNAME, "");
            }
            // if comment is always visible, skip it
            if (alwaysVisibleCellComments.containsKey(cellElementClassName)) {
                return;
            }
            if (eventTypeInt == Event.ONMOUSEOVER) {
                // MOUSEOVER triangle -> show comment unless already shown
                if (!(cellCommentOverlay.isVisible() && cellElementClassName
                        .equals(cellCommentCellClassName))) {
                    jsniUtil.parseColRow(cellElementClassName);
                    cellCommentCellColumn = jsniUtil.getParsedCol();
                    cellCommentCellRow = jsniUtil.getParsedRow();
                    cellCommentHandler.trigger();
                }
            } else {
                // MOUSEOUT triangle -> hide comment unless mouse moved on top
                // of the triangle's cell (parent)
                Element toElement = event.getRelatedEventTarget().cast();
                if (!toElement.equals(cellElement)) {
                    cellCommentOverlay.hide();
                    cellCommentCellClassName = null;
                    cellCommentCellColumn = -1;
                    cellCommentCellRow = -1;
                }
            }
        } else {
            if (targetClassName.endsWith(MERGED_CELL_CLASSNAME)) {
                targetClassName = targetClassName.replace(" "
                        + MERGED_CELL_CLASSNAME, "");
            }
            // if comment is always visible, skip it
            if (alwaysVisibleCellComments.containsKey(targetClassName)) {
                return;
            }

            if (eventTypeInt == Event.ONMOUSEOVER) {
                // show comment unless already shown
                if (!(cellCommentOverlay.isVisible() && targetClassName
                        .equals(cellCommentCellClassName))) {
                    jsniUtil.parseColRow(targetClassName);
                    cellCommentCellColumn = jsniUtil.getParsedCol();
                    cellCommentCellRow = jsniUtil.getParsedRow();
                    cellCommentHandler.trigger();
                }
            } else if (eventTypeInt == Event.ONMOUSEOUT) {
                // MOUSEOUT triangle's cell -> hide unless mouse moved back on
                // top of the same triangle
                Element toElement = event.getRelatedEventTarget().cast();
                if (toElement != null && toElement.getParentElement() != null) {
                    try {
                        if (!(toElement.getClassName().equals(
                                CELL_COMMENT_TRIANGLE_CLASSNAME) && toElement
                                .getParentElement().equals(target))) {
                            cellCommentOverlay.hide();
                            cellCommentCellClassName = null;
                            cellCommentCellRow = -1;
                            cellCommentCellColumn = -1;
                        }
                    } catch (NullPointerException npe) {
                        debugConsole
                                .warning("SheetWidget:updateCellCommentDisplay: NPE ONMOUSEOUT, "
                                        + npe.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 
     * @param col
     *            1-based
     * @param row
     *            1-based
     * @return
     */
    public boolean isLinkCell(int col, int row) {
        return cellLinksMap != null
                && cellLinksMap.containsKey(toKey(col, row));
    }

    private void updateCellLinkTooltip(int eventTypeInt, int col, int row,
            String tooltip) {
        if (eventTypeInt == Event.ONMOUSEOVER) {
            hyperlinkTooltipLabel.setText(tooltip);
            final DivElement element;
            final String key = toKey(col, row);
            if (isMergedCell(key)) {
                element = getMergedCell(key).getElement();
            } else {
                element = getCell(col, row).getElement();
            }
            hyperlinkTooltip.setPopupPositionAndShow(new PositionCallback() {

                @Override
                public void setPosition(int offsetWidth, int offsetHeight) {
                    setHyperlinkTooltipPosition(offsetWidth, offsetHeight,
                            element);
                }
            });
        } else { // mouseout
            hyperlinkTooltip.hide();
        }
    }

    public void updateBottomRightCellValues(List<CellData> cellData2) {
        updateCellData(firstRowIndex, lastRowIndex, firstColumnIndex,
                lastColumnIndex, rows, cellData2);
    }

    public void updateTopLeftCellValues(List<CellData> cellData2) {
        Iterator<CellData> i = cellData2.iterator();
        while (i.hasNext()) {
            CellData cd = i.next();
            topLeftCells.get(
                    (cd.row - 1) * horizontalSplitPosition + cd.col - 1)
                    .setValue(cd.value, cd.cellStyle);
            String key = toKey(cd.col, cd.row);
            if (isMergedCell(key)) {
                getMergedCell(key).setValue(cd.value, cd.cellStyle);
            }
            if (cd.value == null) {
                cachedCellData.remove(key);
            } else {
                cachedCellData.put(key, cd);
            }
        }
    }

    public void updateTopRightCellValues(List<CellData> cellData2) {
        updateCellData(1, verticalSplitPosition, firstColumnIndex,
                lastColumnIndex, topRightRows, cellData2);
    }

    public void updateBottomLeftCellValues(List<CellData> cellData2) {
        updateCellData(firstRowIndex, lastRowIndex, 1, horizontalSplitPosition,
                bottomLeftRows, cellData2);
    }

    private void updateCellData(int r1, int r2, int c1, int c2,
            ArrayList<ArrayList<Cell>> rows, List<CellData> cellData2) {
        Iterator<CellData> i = cellData2.iterator();
        ArrayList<Cell> row = null;
        int rowIndex = -1;
        while (i.hasNext()) {
            CellData cd = i.next();
            if (cd.row >= r1 && cd.row <= r2 && cd.col >= c1 && cd.col <= c2) {
                if (rowIndex != cd.row) {
                    row = rows.get(cd.row - r1);
                    rowIndex = cd.row;
                }
                row.get(cd.col - c1).setValue(cd.value, cd.cellStyle);
            }
            String key = toKey(cd.col, cd.row);
            if (isMergedCell(key)) {
                getMergedCell(key).setValue(cd.value, cd.cellStyle);
            }
            if (cd.value == null) {
                cachedCellData.remove(key);
            } else {
                cachedCellData.put(key, cd);
            }
        }
    }

    public void cellValuesUpdated(ArrayList<CellData> updatedCellData) {
        // can contain cells from any of the panes -> just iterate and access
        for (CellData cd : updatedCellData) {
            String key = toKey(cd.col, cd.row);
            // update cache
            if (cd.value == null) {
                cachedCellData.remove(key);
            } else {
                cachedCellData.put(key, cd);
            }
            if (isMergedCell(key)) {
                getMergedCell(key).setValue(cd.value, cd.cellStyle);
            } else {
                Cell cell = null;
                if (isCellRenderedInScrollPane(cd.col, cd.row)) {
                    cell = rows.get(cd.row - firstRowIndex).get(
                            cd.col - firstColumnIndex);
                } else if (isCellRenderedInFrozenPane(cd.col, cd.row)) {
                    cell = getFrozenCell(cd.col, cd.row);
                }

                if (cell != null) {
                    cell.setValue(cd.value, cd.cellStyle);
                }
            }
        }
    }

    /**
     * 
     * @param row
     *            1-based row index
     * @return height in pixels
     */
    private int getRowHeight(int row) {
        if (actionHandler.isRowHidden(row)) {
            return 0;
        } else if (row >= definedRowHeights.length) {
            return getDefaultRowHeight();
        } else {
            return definedRowHeights[row - 1];
        }
    }

    public int[] getRowHeights() {
        return definedRowHeights;
    }

    /**
     * 
     * @return sheet default row height in pixels, converted from points
     */
    private int getDefaultRowHeight() {
        if (defRowH == -1) {
            if (ppi == 0) {
                if (ppiCounter.hasParentElement()) {
                    ppi = ppiCounter.getOffsetWidth();
                }
                if (ppi == 0) {
                    ppi = 96;
                }
            }
            defRowH = (int) (actionHandler.getDefaultRowHeight() * ppi / 72);
        }
        return defRowH;
    }

    /**
     * Converts the point value to pixels using the ppi for this client
     * 
     * @param points
     *            to convert
     * @return pixels
     */
    private int convertPointsToPixel(float points) {
        return new BigDecimal(points * ppi / 72.0f).intValue();
    }

    private float convertPixelsToPoint(int pixels) {
        return new BigDecimal(((float) pixels) / ppi * 72).floatValue();
    }

    protected void handleInputElementValueChange(final boolean update) {
        if (!isSelectedCellCompletelyVisible()) {
            scrollSelectedCellIntoView();
        }
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                String value = input.getValue();
                recalculateInputElementWidth(value);
                if (update) {
                    actionHandler.onCellInputValueChange(value);
                }
            }
        });
    }

    private void recalculateInputElementWidth(final String value) {
        try {
            final Cell selectedCell = getSelectedCell();
            selectedCell.setValue(value);
            int textWidth = selectedCell.getElement().getOffsetWidth() + 5;
            int col = selectedCell.getCol();
            int width;
            if (editingMergedCell) {
                MergedRegion region = actionHandler
                        .getMergedRegionStartingFrom(selectedCellCol,
                                selectedCellRow);
                col = region.col2;
                width = selectionWidget.countSum(actionHandler.getColWidths(),
                        region.col1, region.col2 + 1);
            } else {
                width = actionHandler.getColWidthActual(col);
            }

            while (width < textWidth && col <= actionHandler.getMaximumCols()) {
                width += actionHandler.getColWidthActual(++col);
            }
            input.setWidth(width + "px");
        } catch (Exception e) {
            // cell is not visible yet, should not happen, but try again
            debugConsole.severe("SheetWidget:recalculateInputElementWidth: "
                    + e.toString() + " while calculating input element width");
            handleInputElementValueChange(false);
        }
    }

    /**
     * 
     * @param col
     *            1 based
     * @param row
     *            1 based
     * @return
     */
    public final static String toKey(int col, int row) {
        return "col" + col + " row" + row;
    }

    public final static String toCssKey(int col, int row) {
        return ".col" + col + ".row" + row;
    }

    /**
     * Clears the sheet. After this no headers or cells are visible. A
     * {@link #resetModel(SpreadsheetSettings)} call will make sheet visible
     * again.
     * 
     * @param removed
     *            if the widget is completely removed from DOM after this
     */
    public void clearAll(boolean removed) {
        loaded = false;
        for (Iterator<Widget> i = getCustomWidgetIterator(); i.hasNext();) {
            remove(i.next());
        }
        customEditorWidget = null;
        sheetImages.clear();
        if (customWidgetMap != null) {
            customWidgetMap.clear();
            customWidgetMap = null;
        }
        cleanDOM();
        cachedCellData.clear();

        clearPositionStyles();
        clearCellRangeStyles();
        clearSelectedCellStyle();
        clearBasicCellStyles();
        clearMergedCells();
        clearCellComments();
        if (removed) {
            clearShiftedBorderCellStyles();
            removeStyles();
        }
    }

    public String getSelectedCellKey() {
        return toKey(selectedCellCol, selectedCellRow);
    }

    public int getSelectedCellColumn() {
        return selectedCellCol;
    }

    public int getSelectedCellRow() {
        return selectedCellRow;
    }

    public String getSelectedCellLatestValue() {
        CellData cd = cachedCellData.get(getSelectedCellKey());
        return cd == null ? "" : cd.value;
    }

    public void setSelectedCell(int col, int row) {
        selectedCellRow = row;
        selectedCellCol = col;
    }

    public int getSelectionLeftCol() {
        return selectionWidget.getCol1();
    }

    public int getSelectionRightCol() {
        return selectionWidget.getCol2();
    }

    public int getSelectionTopRow() {
        return selectionWidget.getRow1();
    }

    public int getSelectionBottomRow() {
        return selectionWidget.getRow2();
    }

    public boolean isCoherentSelection() {
        return coherentSelection;
    }

    public void setCoherentSelection(boolean coherentSelection) {
        this.coherentSelection = coherentSelection;
    }

    public void setSelectionRangeOutlineVisible(boolean visible) {
        selectionWidget.setVisible(visible);
    }

    public boolean isSelectionRangeOutlineVisible() {
        return selectionWidget.isVisible();
    }

    public void updateSelectionOutline(int col1, int col2, int row1, int row2) {
        if (isMergedCell(toKey(col2, row2))) {
            MergedRegion region = actionHandler.getMergedRegionStartingFrom(
                    col2, row2);
            col2 = region.col2;
            row2 = region.row2;
        }
        selectionWidget.setPosition(col1, col2, row1, row2);
    }

    public void updateSelectedCellStyles(int col1, int col2, int row1,
            int row2, boolean replace) {
        cellRangeStylesCleared = false;
        StringBuffer sb = new StringBuffer();
        // cells
        if (replace && col1 == col2 && row1 == row2) {
            jsniUtil.replaceSelector(cellRangeStyle, ".notusedselector", 0);
        } else {
            for (int r = row1; r <= row2; r++) {
                for (int c = col1; c <= col2; c++) {
                    if (c != selectedCellCol || r != selectedCellRow) {
                        sb.append(toCssKey(c, r));
                        sb.append(",");
                    }
                }
            }
            if (sb.length() > 0) {
                if (replace) {
                    jsniUtil.replaceSelector(cellRangeStyle, sb.toString()
                            .substring(0, sb.length() - 1), 0);
                } else {
                    jsniUtil.addSelector(cellRangeStyle, sb.toString()
                            .substring(0, sb.length() - 1), 0);
                }
            }
        }
        sb = new StringBuffer();
        // row headers
        for (int r = row1; r <= row2; r++) {
            sb.append(".rh.row");
            sb.append(r);
            sb.append(",");
        }
        // column headers
        for (int c = col1; c <= col2; c++) {
            sb.append(".ch.col");
            sb.append(c);
            if (c != col2) {
                sb.append(",");
            }
        }
        if (replace) {
            jsniUtil.replaceSelector(cellRangeStyle, sb.toString(), 1);
        } else {
            jsniUtil.addSelector(cellRangeStyle, sb.toString(), 1);
        }
    }

    /**
     * Clears the light outline on the selected cell which is visible when the
     * selection is not coherent.
     */
    public void clearSelectedCellStyle() {
        try {
            jsniUtil.replaceSelector(selectionStyle, ".notusedselector", 0);
        } catch (Exception e) {
            jsniUtil.clearCSSRules(selectionStyle);
            jsniUtil.insertRule(selectionStyle, ".notusedselector"
                    + SELECTED_CELL_STYLE2);
        }
    }

    /**
     * Clears the highlight (background) on selected cells and their
     * corresponding headers.
     */
    public void clearCellRangeStyles() {
        try {
            jsniUtil.replaceSelector(cellRangeStyle, ".notusedselector", 0);
            jsniUtil.replaceSelector(cellRangeStyle, ".notusedselector", 1);
        } catch (Exception e) {
            jsniUtil.clearCSSRules(cellRangeStyle);
            jsniUtil.insertRule(cellRangeStyle, ".notusedselector "
                    + SELECTED_CELL_RANGE_STYLES);
            jsniUtil.insertRule(cellRangeStyle, ".notusedselector"
                    + SELECTED_HEADER_STYLES);
        }
        cellRangeStylesCleared = true;
    }

    protected void clearPositionStyles() {
        jsniUtil.clearCSSRules(cellSizeAndPositionStyle);
    }

    protected void clearBasicCellStyles() {
        jsniUtil.clearCSSRules(sheetStyle);
        // hyperlink style is created on-demand
        if (hyperlinkStyle != null) {
            jsniUtil.clearCSSRules(hyperlinkStyle);
            hyperlinkStyle.removeFromParent();
            hyperlinkStyle = null;
        }
    }

    protected void clearShiftedBorderCellStyles() {
        jsniUtil.clearCSSRules(shiftedBorderCellStyle);
    }

    protected void clearMergedCells() {
        jsniUtil.clearCSSRules(mergedRegionStyle);
        for (Cell mergedCell : mergedCells.values()) {
            mergedCell.getElement().removeFromParent();
        }
        mergedCells.clear();
    }

    protected void clearCellComments() {
        cellCommentOverlay.hide();
        for (CellComment cc : alwaysVisibleCellComments.values()) {
            cc.hide();
        }
        alwaysVisibleCellComments.clear();
        cellCommentsMap.clear();
    }

    /**
     * swaps the selected cell to the new one, which is the selected cell after
     * this call.
     * 
     * the old cell is "highlighted" and the new one gets the selected cell
     * outline (when selection range outline is hidden).
     * 
     * takes care of swapping into a merged cell (highlights correct all cell
     * headers).
     * 
     * @param column
     * @param row
     */
    public void swapCellSelection(int column, int row) {
        // highlight previously selected cell (background white->selected)
        // the headers for it are already highlighted
        // also remove the new selected cell from the highlighted cells (if it
        // is there).
        String newSelectionCssKey = toCssKey(column, row);
        String oldSelectionCssKey = toCssKey(selectedCellCol, selectedCellRow);
        if (cellRangeStylesCleared) {
            jsniUtil.replaceSelector(cellRangeStyle, oldSelectionCssKey, 0);
            cellRangeStylesCleared = false;
        } else {
            final String oldSelector = jsniUtil.getSelector(cellRangeStyle, 0);
            // IE likes to switch the order of the selectors
            if (oldSelector.startsWith(".row")) {
                newSelectionCssKey = ".row" + row + ".col" + column;
                oldSelectionCssKey = ".row" + selectedCellRow + ".col"
                        + selectedCellCol;
            }
            if (oldSelector.contains(newSelectionCssKey + ",")) {
                // replace
                jsniUtil.replaceSelector(cellRangeStyle, oldSelector.replace(
                        newSelectionCssKey + ",", oldSelectionCssKey + ","), 0);
                // the headers are already highlighted for the new cell
            } else if (oldSelector.endsWith(newSelectionCssKey)) {
                jsniUtil.replaceSelector(
                        cellRangeStyle,
                        oldSelector.substring(0, oldSelector.length()
                                - newSelectionCssKey.length())
                                + oldSelectionCssKey, 0);
            } else {
                // add
                jsniUtil.addSelector(cellRangeStyle, oldSelectionCssKey, 0);
                // highlight the new selected cell headers
                MergedRegion region = actionHandler
                        .getMergedRegionStartingFrom(column, row);
                final StringBuilder sb = new StringBuilder(".rh.row");
                sb.append(row);
                if (region != null) {
                    for (int i = region.row1 + 1; i <= region.row2; i++) {
                        sb.append(",.rh.row");
                        sb.append(i);
                    }
                }
                sb.append(",.ch.col");
                sb.append(column);
                if (region != null) {
                    for (int i = region.col1 + 1; i <= region.col2; i++) {
                        sb.append(",.ch.col");
                        sb.append(i);
                    }
                }
                jsniUtil.addSelector(cellRangeStyle, sb.toString(), 1);
            }

        }
        // mark the new selected cell with light outline
        jsniUtil.replaceSelector(selectionStyle, newSelectionCssKey, 0);
        setSelectedCell(column, row);
    }

    /**
     * Swaps the selected cell to the new one, which is the selected cell after
     * this call.
     * 
     * The old cell is "highlighted" and the new selected cell will be marked as
     * selected. This method is different to
     * {@link #swapCellSelection(int, int)} because the selected cell should be
     * inside the old selection, instead of adding a new cell into the
     * selection. No need to modify highlighted headers.
     * 
     * @param col
     * @param row
     */
    public void swapSelectedCellInsideSelection(int col, int row) {
        // highlight previously selected cell (background white->selected), and
        // remove the highlighted background from the new selected cell
        final String oldSelector = jsniUtil.getSelector(cellRangeStyle, 0);
        // for some nice reason, IE decides to switch the order of
        // selectors. i.e. ".col2.row3 -> .row3.col2"
        String cssKey;
        String oldSelectedKey;
        if (oldSelector.startsWith(".row")) {
            cssKey = ".row" + row + ".col" + col;
            oldSelectedKey = ".row" + getSelectedCellRow() + ".col"
                    + getSelectedCellColumn();
        } else {
            cssKey = toCssKey(col, row);
            oldSelectedKey = toCssKey(getSelectedCellColumn(),
                    getSelectedCellRow());
        }
        final String newSelector;
        if (oldSelector.endsWith(cssKey)) {
            newSelector = oldSelector.substring(0, oldSelector.length()
                    - cssKey.length())
                    + oldSelectedKey;
        } else {
            newSelector = oldSelector.replace(cssKey + ",", oldSelectedKey
                    + ",");
        }
        jsniUtil.replaceSelector(cellRangeStyle, newSelector, 0);
        setSelectedCell(col, row);
    }

    /**
     * Marks the given interval as selected (highlighted background), replaces
     * old selected cells. Ignores the currently selected cell.
     * 
     * @param col1
     * @param col2
     * @param row1
     * @param row2
     */
    public void replaceAsSelectedCells(int col1, int col2, int row1, int row2) {
        final StringBuffer sb = new StringBuffer();
        for (int r = row1; r <= row2; r++) {
            for (int c = col1; c <= col2; c++) {
                if (selectedCellCol != c || selectedCellRow != r) {
                    sb.append(toCssKey(c, r));
                    sb.append(",");
                }
            }
        }
        String newSelector = sb.toString();
        if (newSelector.length() > 1) {
            jsniUtil.replaceSelector(cellRangeStyle,
                    newSelector.substring(0, newSelector.length() - 1), 0);
        } else {
            jsniUtil.replaceSelector(cellRangeStyle, ".notusedselector", 0);
        }
    }

    /**
     * Replaces the currently marked selected headers (highlighted) with the
     * given intervals.
     * 
     * @param row1
     * @param row2
     * @param col1
     * @param col2
     */
    public void replaceHeadersAsSelected(int row1, int row2, int col1, int col2) {
        final StringBuilder sb = new StringBuilder();
        for (int i = row1; i <= row2; i++) {
            sb.append(".rh.row");
            sb.append(i);
            sb.append(",");
        }
        for (int i = col1; i <= col2; i++) {
            sb.append(".ch.col");
            sb.append(i);
            if (i != col2) {
                sb.append(",");
            }
        }
        if (sb.length() > 1) {
            jsniUtil.replaceSelector(cellRangeStyle, sb.toString(), 1);
        } else { // should be impossible though
            jsniUtil.replaceSelector(cellRangeStyle, ".notusedselector", 1);
        }
    }

    public int[] getSheetDisplayRange() {
        return new int[] { firstColumnIndex, lastColumnIndex, firstRowIndex,
                lastRowIndex };
    }

    /**
     * 
     * @return the first column index that is completely visible on the left
     */
    public int getLeftVisibleColumnIndex() {
        int index = firstColumnIndex;
        final int bound = sheet.getAbsoluteLeft();
        for (Cell cell : rows.get(0)) {
            if (cell.getElement().getAbsoluteLeft() >= bound) {
                return index;
            } else {
                index++;
            }
        }
        return firstColumnIndex;
    }

    public int getRightVisibleColumnIndex() {
        int index = lastColumnIndex;
        final int bound = sheet.getAbsoluteRight();
        final ArrayList<Cell> cells = rows.get(0);
        for (int i = cells.size() - 1; i > 0; i--) {
            if (cells.get(i).getElement().getAbsoluteRight() <= bound) {
                return index;
            } else {
                index--;
            }
        }
        return lastColumnIndex;
    }

    /**
     * 
     * @return the first row index that is completely visible on the top
     */
    public int getTopVisibleRowIndex() {
        int index = firstRowIndex;
        final int bound = sheet.getAbsoluteTop();
        for (ArrayList<Cell> row : rows) {
            if (row.get(0).getElement().getAbsoluteTop() >= bound) {
                return index;
            } else {
                index++;
            }
        }
        return firstRowIndex;
    }

    public int getBottomVisibleRowIndex() {
        int index = lastRowIndex;
        final int bound = sheet.getAbsoluteBottom();
        for (int i = rows.size() - 1; i > 0; i--) {
            if (rows.get(i).get(0).getElement().getAbsoluteBottom() <= bound) {
                return index;
            } else {
                index--;
            }
        }
        return lastRowIndex;
    }

    public void displayCustomCellEditor(Widget customEditorWidget) {
        customCellEditorDisplayed = true;
        jsniUtil.replaceSelector(editedCellFreezeColumnStyle,
                ".notusedselector", 0);
        this.customEditorWidget = customEditorWidget;
        Cell selectedCell = getSelectedCell();
        selectedCell.setValue(null);

        Widget parent = customEditorWidget.getParent();
        if (parent != null && !equals(parent)) {
            customEditorWidget.removeFromParent();
        }
        DivElement element = selectedCell.getElement();
        element.addClassName(CUSTOM_EDITOR_CELL);
        element.appendChild(customEditorWidget.getElement());
        adopt(customEditorWidget);

        focusSheet();
    }

    public void removeCustomCellEditor() {
        if (customCellEditorDisplayed) {
            customCellEditorDisplayed = false;
            customEditorWidget.getElement().removeClassName(CUSTOM_EDITOR_CELL);
            customEditorWidget.removeFromParent();
            // the cell value should have been updated
            if (loaded) {
                CellData cd = cachedCellData.get(getSelectedCellKey());
                if (cd == null) {
                    getSelectedCell().setValue(null);
                } else {
                    getSelectedCell().setValue(cd.value);
                }
            }
            customEditorWidget = null;
        }
    }

    private Cell getSelectedCell() {
        String selectedCellKey = getSelectedCellKey();
        if (isMergedCell(selectedCellKey)) {
            return getMergedCell(selectedCellKey);
        } else {
            return getCell(selectedCellCol, selectedCellRow);
        }
    }

    private boolean isCellRenderedInFrozenPane(int col, int row) {
        return (row <= verticalSplitPosition && (col >= firstColumnIndex
                && col <= lastColumnIndex || col <= horizontalSplitPosition))
                || (col <= horizontalSplitPosition && (row >= firstRowIndex
                        && row <= lastRowIndex || row <= verticalSplitPosition));
    }

    private Cell getFrozenCell(int col, int row) {
        if (verticalSplitPosition < row) { // bottom left
            return bottomLeftRows.get(row - firstRowIndex).get(col - 1);
        } else if (horizontalSplitPosition < col) { // top right
            return topRightRows.get(row - 1).get(col - firstColumnIndex);
        } else { // top left
            return topLeftCells.get((row - 1) * horizontalSplitPosition + col
                    - 1);
        }
    }

    /**
     * Returns the cell. Checks for it from a freeze pane.
     * 
     * @param col
     * @param row
     * @return
     */
    private Cell getCell(int col, int row) {
        if (isCellRenderedInFrozenPane(col, row)) {
            return getFrozenCell(col, row);
        } else {
            return rows.get(row - firstRowIndex).get(col - firstColumnIndex);
        }
    }

    private String getSelectedCellCellStyleString() {
        CellData cellData = getCellData(selectedCellCol, selectedCellRow);
        return cellData == null ? "cs0" : cellData.cellStyle;
    }

    public void startEditingCell(boolean focus, boolean inputFullFocus,
            boolean recalculate, final String value) {
        editingCell = true;
        sheetInputEventListener.setInputFullFocus(inputFullFocus);
        jsniUtil.replaceSelector(
                editedCellFreezeColumnStyle,
                EDITING_CELL_SELECTOR
                        + toCssKey(selectedCellCol, selectedCellRow), 0);

        input.setStyleName(toKey(selectedCellCol, selectedCellRow) + " cell"
                + " " + getSelectedCellCellStyleString());
        if (isMergedCell(toKey(selectedCellCol, selectedCellRow))) {
            editingMergedCell = true;
            input.setHeight(getMergedCell(
                    toKey(selectedCellCol, selectedCellRow)).getElement()
                    .getStyle().getHeight());
        }

        if (recalculate) {
            handleInputElementValueChange(false);
        }
        if (focus) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() { //
                        @Override
                        public void execute() {
                            input.setFocus(true);
                            if (value.endsWith("%")) {
                                input.setCursorPos(value.length() - 1);
                            } else if (isIE) { // would get wrong position on
                                               // first inline cell edit
                                input.setCursorPos(value.length());
                            }
                        }
                    });
        }
        input.setValue(value);
    }

    public void updateSelectedCellValue(String value) {
        if (isSelectedCellRendered()) {
            getSelectedCell().setValue(value);
        }
        CellData cd = new CellData();
        cd.value = value;
        cd.cellStyle = getSelectedCell().getCellStyle();
        cachedCellData.put(
                toKey(getSelectedCellColumn(), getSelectedCellRow()), cd);
    }

    public void updateInputValue(String value) {
        if (customCellEditorDisplayed) {
            // Do nothing here because the value ought to come from server side
        } else {
            input.setValue(value);
            if (editingCell) {
                handleInputElementValueChange(false);
            }
        }
    }

    public void stopEditingCell() {
        editingCell = false;
        editingMergedCell = false;
        sheetInputEventListener.cellEditingStopped();

        jsniUtil.replaceSelector(editedCellFreezeColumnStyle,
                ".notusedselector", 0);
        input.setValue("");
        input.setWidth("0");
        input.setHeight("");
        input.setStyleName("");
        focusSheet();
    }

    public void focusSheet() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                sheet.focus();
            }
        });
    }

    public boolean isSelectedCellRendered() {
        return isCellRenderedInScrollPane(selectedCellCol, selectedCellRow)
                || isCellRenderedInFrozenPane(selectedCellCol, selectedCellRow);
    }

    public boolean isSelectedCellCompletelyVisible() {
        return isCellCompletelyVisible(selectedCellCol, selectedCellRow);
    }

    public boolean isSelectionAreaCompletelyVisible() {
        return isAreaCompletelyVisible(selectionWidget.getCol1(),
                selectionWidget.getCol2(), selectionWidget.getRow1(),
                selectionWidget.getRow2());
    }

    public boolean isCellRenderedInScrollPane(int col, int row) {
        return col >= firstColumnIndex && col <= lastColumnIndex
                && row >= firstRowIndex && row <= lastRowIndex;
    }

    /**
     * Is the cell currently rendered in any of the frozen panes.
     * 
     * @param col
     * @param row
     * @return
     */
    public boolean isFrozenCellRendered(int col, int row) {
        return isCellRenderedInTopLeftPane(col, row)
                || isCellRenderedInTopRightPane(col, row)
                || isCellRenderedInBottomLeftPane(col, row);
    }

    /**
     * Is the cell currently rendered in top left pane.
     * 
     * @param col
     * @param row
     * @return
     */
    public boolean isCellRenderedInTopLeftPane(int col, int row) {
        return col <= horizontalSplitPosition && row <= verticalSplitPosition;
    }

    /**
     * Is the cell currently rendered in top right pane.
     * 
     * @param col
     * @param row
     * @return
     */
    public boolean isCellRenderedInTopRightPane(int col, int row) {
        return col > horizontalSplitPosition && col <= lastColumnIndex
                && row <= verticalSplitPosition;
    }

    /**
     * Is the cell currently rendered in bottom left pane.
     * 
     * @param col
     * @param row
     * @return
     */
    public boolean isCellRenderedInBottomLeftPane(int col, int row) {
        return row > verticalSplitPosition && row <= lastRowIndex
                && col <= horizontalSplitPosition;
    }

    /**
     * Is the given cell currently rendered. Checks freeze panes too.
     * 
     * @param col
     * @param row
     * @return
     */
    public boolean isCellRendered(int col, int row) {
        return isCellRenderedInScrollPane(col, row)
                || isFrozenCellRendered(col, row);
    }

    /**
     * Is the given cell currently visible completely. Checks freeze panes too.
     * 
     * @param col
     * @param row
     * @return
     */
    public boolean isCellCompletelyVisible(int col, int row) {
        return (col <= horizontalSplitPosition || col >= getLeftVisibleColumnIndex()
                && col <= getRightVisibleColumnIndex())
                && (row <= verticalSplitPosition || row <= getTopVisibleRowIndex()
                        && row >= getBottomVisibleRowIndex());
    }

    public boolean isAreaCompletelyVisible(int col1, int col2, int row1,
            int row2) {
        return isCellCompletelyVisible(col1, row1)
                && isCellRendered(col1, row2) && isCellRendered(col2, row1)
                && isCellRendered(col2, row2);
    }

    public void scrollSelectedCellIntoView() {
        scrollCellIntoView(selectedCellCol, selectedCellRow);
    }

    /**
     * Scrolls the sheet to show the given cell, then triggers escalator for
     * updating cells if necessary.
     * 
     * This method does the {@link #isCellRenderedInScrollPane(int, int)} in
     * itself, so no need to do the check before calling this. Nothing is done
     * if the cell is already visible.
     * 
     * Scrolls one cell extra to all directions to cut the scrolls to half when
     * using keyboard navigation.
     * 
     * @param col
     *            1-based
     * @param row
     *            1-based
     */
    public void scrollCellIntoView(int col, int row) {
        boolean scrolled = false;
        // vertical:
        final int leftColumnIndex = getLeftVisibleColumnIndex();
        if (col < leftColumnIndex && col > horizontalSplitPosition) {
            // scroll to left until column is visible (+ 1 cell extra)
            int scroll = 0;
            for (int i = leftColumnIndex - 1; i >= col - 1 && i > 0; i--) {
                scroll += actionHandler.getColWidthActual(i);
            }
            sheet.setScrollLeft(sheet.getScrollLeft() - scroll);
            if (col <= firstColumnIndex
                    || scroll > (actionHandler.getColumnBufferSize() / 2)) {
                scrolled = true;
            }

        } else {
            final int rightColumnIndex = getRightVisibleColumnIndex();
            if (col > rightColumnIndex) {
                // scroll to right until column is visible (+ 1 cell extra)
                int scroll = 0;
                final int maximumCols = actionHandler.getMaximumCols();
                for (int i = rightColumnIndex + 1; i <= col + 1
                        && i <= maximumCols; i++) {
                    scroll += actionHandler.getColWidthActual(i);
                }
                sheet.setScrollLeft(sheet.getScrollLeft() + scroll);
                if (col >= lastColumnIndex
                        || scroll > (actionHandler.getColumnBufferSize() / 2)) {
                    scrolled = true;
                }
            }
        }

        // horizontal:
        final int topRowIndex = getTopVisibleRowIndex();
        if (row < topRowIndex && row > verticalSplitPosition) {
            // scroll up until row is visible (+ 1 cell extra)
            int scroll = 0;
            for (int i = topRowIndex - 1; i >= row - 1 && i > 0; i--) {
                scroll += getRowHeight(i);
            }
            // with horizontal need to add 1 pixel per cell because borders
            scroll += (topRowIndex - row);
            final int result = sheet.getScrollTop() - scroll;
            sheet.setScrollTop(result > 0 ? result : 0);
            if (row <= firstRowIndex
                    || scroll > (actionHandler.getRowBufferSize() / 2)) {
                scrolled = true;
            }
        } else {
            final int bottomRowIndex = getBottomVisibleRowIndex();
            if (row > bottomRowIndex) {
                // scroll down until row is visible (+1 cell extra)
                int scroll = 0;
                final int maximumRows = actionHandler.getMaximumRows();
                for (int i = bottomRowIndex + 1; i <= row + 1
                        && i <= maximumRows; i++) {
                    scroll += getRowHeight(i);
                }
                // with horizontal need to add 1 pixel per cell because borders
                scroll += (row - bottomRowIndex);
                sheet.setScrollTop(sheet.getScrollTop() + scroll);
                if (row >= lastRowIndex
                        || scroll > (actionHandler.getRowBufferSize() / 2)) {
                    scrolled = true;
                }
            }
        }
        if (scrolled) {
            onSheetScroll();
            moveHeadersToMatchScroll();
        }
    }

    public void scrollSelectionAreaIntoView() {
        scrollAreaIntoView(selectionWidget.getCol1(),
                selectionWidget.getCol2(), selectionWidget.getRow1(),
                selectionWidget.getRow2());
    }

    /**
     * Scrolls the sheet to show the given area, or as much of it as fits into
     * the view.
     * 
     * @param col1
     *            1-based
     * @param col2
     *            1-based
     * @param row1
     *            1-based
     * @param row2
     *            1-based
     */
    public void scrollAreaIntoView(int col1, int col2, int row1, int row2) {
        boolean scrolled = false;
        // horizontal:
        if (col1 <= horizontalSplitPosition) {
            col1 = horizontalSplitPosition + 1;
        }
        final int leftColumnIndex = getLeftVisibleColumnIndex();
        if (col1 < leftColumnIndex) {
            // scroll to left until col1 comes visible
            int scroll = 0;
            for (int i = leftColumnIndex - 1; i >= col1 - 1 && i > 0; i--) {
                scroll += actionHandler.getColWidthActual(i);
            }
            sheet.setScrollLeft(sheet.getScrollLeft() - scroll);
            if (col1 <= firstColumnIndex
                    || scroll > (actionHandler.getColumnBufferSize() / 2)) {
                scrolled = true;
            }
        } else {
            final int rightColumnIndex = getRightVisibleColumnIndex();
            if (col2 > rightColumnIndex) {
                // scroll right until col2 comes visible
                int scroll = 0;
                final int maximumCols = actionHandler.getMaximumCols();
                for (int i = rightColumnIndex + 1; i <= col2 + 1
                        && i <= maximumCols; i++) {
                    scroll += actionHandler.getColWidthActual(i);
                }
                sheet.setScrollLeft(sheet.getScrollLeft() + scroll);
                if (col2 >= lastColumnIndex
                        || scroll > (actionHandler.getColumnBufferSize() / 2)) {
                    scrolled = true;
                }
            }
        }
        // vertical:
        if (row1 <= verticalSplitPosition) {
            row1 = verticalSplitPosition + 1;
        }
        final int topRowIndex = getTopVisibleRowIndex();
        if (row1 < topRowIndex) {
            // scroll up until the row1 come visible
            int scroll = 0;
            for (int i = topRowIndex - 1; i >= row1 - 1 && i > 0; i--) {
                scroll += getRowHeight(i);
            }
            // with horizontal need to add 1 pixel per cell because borders
            scroll += (topRowIndex - row1);
            final int result = sheet.getScrollTop() - scroll;
            sheet.setScrollTop(result > 0 ? result : 0);
            if (row1 <= firstRowIndex
                    || scroll > (actionHandler.getRowBufferSize() / 2)) {
                scrolled = true;
            }
        } else {
            final int bottomRowIndex = getBottomVisibleRowIndex();
            if (row2 > bottomRowIndex) {
                // scroll down until row2 is visible
                int scroll = 0;
                final int maximumRows = actionHandler.getMaximumRows();
                for (int i = bottomRowIndex + 1; i <= row2 + 1
                        && i <= maximumRows; i++) {
                    scroll += getRowHeight(i);
                }
                // with horizontal need to add 1 pixel per cell because borders
                scroll += (row2 - bottomRowIndex);
                sheet.setScrollTop(sheet.getScrollTop() + scroll);
                if (row2 >= lastRowIndex
                        || scroll > (actionHandler.getRowBufferSize() / 2)) {
                    scrolled = true;
                }
            }
        }
        if (scrolled) {
            onSheetScroll();
            moveHeadersToMatchScroll();
        }
    }

    public void addShiftedCellBorderStyles(List<String> styles) {
        for (String style : styles) {
            try {
                jsniUtil.insertRule(shiftedBorderCellStyle,
                        style.replace(".col", ".v-spreadsheet .cell.col"));
            } catch (Exception e) {
                debugConsole.log(
                        Level.SEVERE,
                        "Invalid custom cell border style: " + style + ", "
                                + e.getMessage());
            }
        }
    }

    public void removeShiftedCellBorderStyles() {
        jsniUtil.clearCSSRules(shiftedBorderCellStyle);
    }

    public int getSheetScrollLeft() {
        return sheet.getScrollLeft();
    }

    public int getSheetScrollTop() {
        return sheet.getScrollTop();
    }

    public void setScrollPosition(int scrollLeft, int scrollTop) {
        sheet.setScrollLeft(scrollLeft);
        sheet.setScrollTop(scrollTop);
    }

    public void addPopupButton(PopupButtonWidget popupButton) {
        if (sheetPopupButtons == null) {
            sheetPopupButtons = new HashMap<String, PopupButtonWidget>();
        }
        int col = popupButton.getCol();
        int row = popupButton.getRow();
        String key = toKey(col, row);
        if (col != 0 && row != 0) { // on first load col and row might be 0
            sheetPopupButtons.put(key, popupButton);
            if (isCellRendered(col, row)) {
                Cell cell = getCell(col, row);
                Widget parent = popupButton.getParent();
                if (parent != null) {
                    if (equals(parent)) {
                        cell.showPopupButton(popupButton.getElement());
                    } else {
                        popupButton.removeFromParent();
                        cell.showPopupButton(popupButton.getElement());
                        adopt(popupButton);
                    }
                } else {
                    cell.showPopupButton(popupButton.getElement());
                    adopt(popupButton);
                }
            }
        } else { // if getting 0 col / row, still need to store the popupbutton
            while (sheetPopupButtons.containsKey(key)) {
                popupButton.setCol(--col);
                key = toKey(col, row);
            }
            sheetPopupButtons.put(key, popupButton);
        }
        popupButton.setSheetWidget(this, sheet);
    }

    public void removePopupButton(PopupButtonWidget popupButton) {
        int col = popupButton.getCol();
        int row = popupButton.getRow();
        sheetPopupButtons.remove(toKey(col, row));
        if (col >= firstColumnIndex && col <= lastColumnIndex
                && row >= firstRowIndex && row <= lastRowIndex) {
            remove(popupButton);
            // need to remove the possible reference from the cell too
            getCell(col, row).removePopupButton();
        }
    }

    /**
     * PopupButtons should not change position (cell), but the popupButton's
     * col&row values come after the popupButton has actually been added.
     */
    public void updatePopupButtonPosition(PopupButtonWidget popupButton,
            int oldRow, int oldCol, int newRow, int newCol) {
        sheetPopupButtons.remove(toKey(oldCol, oldRow));
        sheetPopupButtons.put(toKey(newCol, newRow), popupButton);
        Widget parent = popupButton.getParent();
        // convert to
        if (isCellRendered(newCol, newRow)) {
            Cell cell = getCell(newCol, newRow);
            if (parent != null) {
                if (equals(parent)) {
                    if (isCellRendered(oldCol, oldRow)) {
                        getCell(oldCol, oldRow).removePopupButton();
                    }
                    cell.showPopupButton(popupButton.getElement());
                } else {
                    popupButton.removeFromParent();
                    cell.showPopupButton(popupButton.getElement());
                    adopt(popupButton);
                }
            } else {
                cell.showPopupButton(popupButton.getElement());
                adopt(popupButton);
            }
        } else if (parent != null) {
            popupButton.removeFromParent();
        }
    }

    // This is for GWT
    @Override
    public Iterator<Widget> iterator() {
        final List<Widget> emptyList = new ArrayList<Widget>();
        emptyList.add(input);
        if (customEditorWidget != null) {
            emptyList.add(customEditorWidget);
        }
        emptyList.addAll(sheetImages.values());
        if (customWidgetMap != null) {
            emptyList.addAll(customWidgetMap.values());
        }
        if (sheetPopupButtons != null) {
            emptyList.addAll(sheetPopupButtons.values());
        }
        return emptyList.iterator();
    }

    // This is for clearing of sheet from custom widgets
    protected Iterator<Widget> getCustomWidgetIterator() {
        final List<Widget> emptyList = new ArrayList<Widget>();
        if (customEditorWidget != null) {
            emptyList.add(customEditorWidget);
        }
        emptyList.addAll(sheetImages.values());
        if (customWidgetMap != null) {
            emptyList.addAll(customWidgetMap.values());
        }
        if (sheetPopupButtons != null) {
            emptyList.addAll(sheetPopupButtons.values());
        }
        return emptyList.iterator();
    }

    @Override
    public boolean remove(Widget child) {
        try {
            Element element = child.getElement();
            com.google.gwt.dom.client.Element parentElement = element
                    .getParentElement();
            Widget widgetParent = child.getParent();
            if (sheet.equals(parentElement)
                    || child.equals(customEditorWidget)
                    || (parentElement != null
                            && parentElement.getParentNode() != null && sheet
                                .isOrHasChild(parentElement.getParentNode()))) {
                orphan(child);
                element.removeFromParent();
                return true;
            } else if (equals(widgetParent)) {
                orphan(child);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {

            debugConsole.log(Level.WARNING,
                    "Exception while removing child widget from SheetWidget");
        }
        return false;
    }

    private void setHyperlinkTooltipPosition(int offsetWidth, int offsetHeight,
            DivElement element) {
        // Calculate left position for the popup. The computation for
        // the left position is bidi-sensitive.

        int textBoxOffsetWidth = element.getOffsetWidth();

        // Compute the difference between the popup's width and the
        // textbox's width
        int offsetWidthDiff = offsetWidth - textBoxOffsetWidth;

        int left;

        if (LocaleInfo.getCurrentLocale().isRTL()) { // RTL case

            int textBoxAbsoluteLeft = element.getAbsoluteLeft();

            // Right-align the popup. Note that this computation is
            // valid in the case where offsetWidthDiff is negative.
            left = textBoxAbsoluteLeft - offsetWidthDiff;

            // If the suggestion popup is not as wide as the text box, always
            // align to the right edge of the text box. Otherwise, figure out
            // whether
            // to right-align or left-align the popup.
            if (offsetWidthDiff > 0) {

                // Make sure scrolling is taken into account, since
                // box.getAbsoluteLeft() takes scrolling into account.
                int windowRight = Window.getClientWidth()
                        + Window.getScrollLeft();
                int windowLeft = Window.getScrollLeft();

                // Compute the left value for the right edge of the textbox
                int textBoxLeftValForRightEdge = textBoxAbsoluteLeft
                        + textBoxOffsetWidth;

                // Distance from the right edge of the text box to the right
                // edge
                // of the window
                int distanceToWindowRight = windowRight
                        - textBoxLeftValForRightEdge;

                // Distance from the right edge of the text box to the left edge
                // of the
                // window
                int distanceFromWindowLeft = textBoxLeftValForRightEdge
                        - windowLeft;

                // If there is not enough space for the overflow of the popup's
                // width to the right of the text box and there IS enough space
                // for the
                // overflow to the right of the text box, then left-align the
                // popup.
                // However, if there is not enough space on either side, stick
                // with
                // right-alignment.
                if (distanceFromWindowLeft < offsetWidth
                        && distanceToWindowRight >= offsetWidthDiff) {
                    // Align with the left edge of the text box.
                    left = textBoxAbsoluteLeft;
                }
            }
        } else { // LTR case

            // Left-align the popup.
            left = element.getAbsoluteLeft();

            // If the suggestion popup is not as wide as the text box, always
            // align to
            // the left edge of the text box. Otherwise, figure out whether to
            // left-align or right-align the popup.
            if (offsetWidthDiff > 0) {
                // Make sure scrolling is taken into account, since
                // box.getAbsoluteLeft() takes scrolling into account.
                int windowRight = Window.getClientWidth()
                        + Window.getScrollLeft();
                int windowLeft = Window.getScrollLeft();

                // Distance from the left edge of the text box to the right edge
                // of the window
                int distanceToWindowRight = windowRight - left;

                // Distance from the left edge of the text box to the left edge
                // of the
                // window
                int distanceFromWindowLeft = left - windowLeft;

                // If there is not enough space for the overflow of the popup's
                // width to the right of hte text box, and there IS enough space
                // for the
                // overflow to the left of the text box, then right-align the
                // popup.
                // However, if there is not enough space on either side, then
                // stick with
                // left-alignment.
                if (distanceToWindowRight < offsetWidth
                        && distanceFromWindowLeft >= offsetWidthDiff) {
                    // Align with the right edge of the text box.
                    left -= offsetWidthDiff;
                }
            }
        }

        // Calculate top position for the popup

        int top = element.getAbsoluteTop();

        // Make sure scrolling is taken into account, since
        // box.getAbsoluteTop() takes scrolling into account.
        int windowTop = Window.getScrollTop();
        int windowBottom = Window.getScrollTop() + Window.getClientHeight();

        // Distance from the top edge of the window to the top edge of the
        // text box
        int distanceFromWindowTop = top - windowTop;

        // Distance from the bottom edge of the window to the bottom edge of
        // the text box
        int distanceToWindowBottom = windowBottom
                - (top + element.getOffsetHeight());

        // If there is not enough space for the popup's height below the text
        // box and there IS enough space for the popup's height above the text
        // box, then then position the popup above the text box. However, if
        // there
        // is not enough space on either side, then stick with displaying the
        // popup below the text box.
        if (distanceToWindowBottom < offsetHeight
                && distanceFromWindowTop >= offsetHeight) {
            top -= offsetHeight;
        } else {
            // Position above the text box
            top += element.getOffsetHeight();
        }
        hyperlinkTooltip.setPopupPosition(left, top);
    }

    public void setDisplayGridlines(boolean displayGridlines) {
        if (displayGridlines) {
            spreadsheet.removeClassName(NO_GRIDLINES_CLASSNAME);
        } else {
            spreadsheet.addClassName(NO_GRIDLINES_CLASSNAME);
        }
        updateSheetPanePositions();
    }

    public void setDisplayRowColHeadings(boolean displayRowColHeadings) {
        this.displayRowColHeadings = displayRowColHeadings;

        if (displayRowColHeadings) {
            spreadsheet.removeClassName(NO_ROWCOLHEADINGS_CLASSNAME);
        } else {
            spreadsheet.addClassName(NO_ROWCOLHEADINGS_CLASSNAME);
        }
        if (loaded) {
            moveHeadersToMatchScroll();
            updateSheetPanePositions();
        }
    }

    public void setVerticalSplitPosition(int verticalSplitPosition) {
        this.verticalSplitPosition = verticalSplitPosition;
        selectionWidget.setVerticalSplitPosition(verticalSplitPosition);
    }

    public void setHorizontalSplitPosition(int horizontalSplitPosition) {
        this.horizontalSplitPosition = horizontalSplitPosition;
        selectionWidget.setHorizontalSplitPosition(horizontalSplitPosition);
        if (horizontalSplitPosition > 0) {
            jsniUtil.replaceSelector(editedCellFreezeColumnStyle,
                    ".v-spreadsheet .top-left-pane .cell.col"
                            + horizontalSplitPosition
                            + ", .v-spreadsheet .bottom-left-pane .cell.col"
                            + horizontalSplitPosition, 1);
        } else {
            jsniUtil.replaceSelector(editedCellFreezeColumnStyle,
                    ".notusedselector", 1);
        }
    }

    protected Element getBottomRightPane() {
        return sheet;
    }

    protected Element getBottomLeftPane() {
        return bottomLeftPane;
    }

    protected Element getTopRightPane() {
        return topRightPane;
    }

    protected Element getTopLeftPane() {
        return topLeftPane;
    }

    public boolean isSheetElement(Element cast) {
        return cast == getBottomLeftPane() || cast == getTopLeftPane()
                || cast == getTopRightPane() || cast == getBottomRightPane();
    }

    public void clearSelectedCellsOnCut() {
        actionHandler.clearSelectedCellsOnCut();
    }
}
