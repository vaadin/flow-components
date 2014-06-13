package com.vaadin.addon.spreadsheet.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.dom.client.StyleElement;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.ui.VLabel;
import com.vaadin.client.ui.VLazyExecutor;
import com.vaadin.client.ui.VOverlay;

public class SheetWidget extends Panel {

    private static final String MERGED_CELL_CLASSNAME = "merged-cell";
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

    /** Header corner element that covers crossing headers */
    private DivElement corner = Document.get().createDivElement();

    private InputElement input = Document.get().createTextInputElement();

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

    /**
     * Div elements for column header divs. Note that index 0 in array points to
     * div on column 1
     */
    private ArrayList<DivElement> colHeaders = new ArrayList<DivElement>();

    /**
     * List of rows. Each row is a list of divs on that row. Note that index 0
     * in the outer list points to row 1 and index 0 in the inner list points to
     * div in column 1
     */
    private ArrayList<ArrayList<Cell>> rows = new ArrayList<ArrayList<Cell>>();

    /** Stylesheet element created for holding the dynamic row and column styles */
    private StyleElement style = Document.get().createStyleElement();

    /** Stylesheet element for holding the workbook defined styles */
    private StyleElement sheetStyle = Document.get().createStyleElement();

    /** Stylesheet element for holding custom cell sizes (because of borders) */
    private StyleElement customCellSizeStyle = Document.get()
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
     * cell.
     */
    private StyleElement editedCellStyle = Document.get().createStyleElement();

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

    /** An extra element used from scrolling a cell to the view. */
    private DivElement extraElement = Document.get().createDivElement();

    private final VLazyExecutor scrollHandler;

    private VLazyExecutor requester;

    /**
     * Random id used as additional style for the widget element to connect
     * dynamic CSS rules to correct spreadsheet.
     */
    private String sheetId;

    private final HashMap<String, String> cellData;

    private final HashMap<String, Double> numericCellData;

    private final HashMap<Integer, Integer> cellStyleToCSSRuleIndex;

    private Widget customEditorWidget;

    private Map<String, Widget> customWidgetMap;

    private Map<String, String> cellLinksMap;

    private Map<String, String> cellCommentsMap;

    private Map<String, CellComment> alwaysVisibleCellComments;

    private Map<String, SheetImage> sheetImages;

    private Map<String, PopupButtonWidget> sheetPopupButtons;

    /** region ID to cell map */
    private Map<Integer, Cell> mergedCells;

    private String cellCommentCellClassName;

    private int selectedCellCol;
    private int selectedCellRow;

    private boolean cellRangeStylesCleared = true;
    private boolean coherentSelection = true;
    private boolean customCellEditorDisplayed;
    private boolean editingCell;
    private boolean editingMergedCell;
    private boolean inputClicked;
    private boolean loaded;
    private boolean selectingCells;

    private final boolean isWebkit = BrowserInfo.get().isWebkit();
    private final boolean isIE = BrowserInfo.get().isIE();

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
    private int emptyRowPointer;
    private int emptyCellPointer;
    private int ppi;
    private int defRowH = -1;
    private int[] definedRowHeights;

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

    private boolean displayRowColHeadings;

    private VLazyExecutor cellCommentHandler = new VLazyExecutor(350,
            new ScheduledCommand() {

                @Override
                public void execute() {
                    if (cellCommentCellColumn != -1 && cellCommentCellRow != -1) {
                        showCellComment(cellCommentCellColumn,
                                cellCommentCellRow);
                    }

                }
            });

    public SheetWidget(SheetHandler view) {
        actionHandler = view;
        cellData = new HashMap<String, String>();
        numericCellData = new HashMap<String, Double>();
        cellStyleToCSSRuleIndex = new HashMap<Integer, Integer>();
        alwaysVisibleCellComments = new HashMap<String, CellComment>();
        sheetImages = new HashMap<String, SheetImage>();
        mergedCells = new HashMap<Integer, Cell>();
        resizeExtraHeaders = new ArrayList<DivElement>();
        selectionWidget = new SelectionWidget(view);
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

        initListeners();

        scrollHandler = new VLazyExecutor(0, new ScheduledCommand() {

            @Override
            public void execute() {
                if (loaded) {
                    updateSheetDisplay();
                }
            }
        });

        requester = new VLazyExecutor(200, new ScheduledCommand() {

            @Override
            public void execute() {
                requestCells();
            }
        });
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
        cellData.clear();
        numericCellData.clear();
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
                updateStyles(true);
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
        updateStyles(false);
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
            }

            int newLastRowPosition = newFirstRowPosition;
            for (int i = firstRowIndex; i <= lastRowIndex; i++) {
                newLastRowPosition += getRowHeight(i);
            }
            final int bottomBound = scrollTop + scrollViewHeight
                    + actionHandler.getRowBufferSize();

            int topEdgeChange = newFirstRowPosition - firstRowPosition;
            int bottomEdgeChange = newLastRowPosition - lastRowPosition;
            firstRowPosition = newFirstRowPosition;
            lastRowPosition = newLastRowPosition;

            int newFirstColumnPosition = 0;
            for (int i = 1; i < firstColumnIndex; i++) {
                newFirstColumnPosition += actionHandler.getColWidthActual(i);
            }

            int newLastColumnPosition = newFirstColumnPosition;
            for (int i = firstColumnIndex; i <= lastColumnIndex; i++) {
                newLastColumnPosition += actionHandler.getColWidthActual(i);
            }
            final int rightBound = scrollLeft + scrollViewWidth
                    + actionHandler.getColumnBufferSize();

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
            debugConsole.severe("Exception while relayouting spreadsheet, "
                    + e.toString());
            resetScrollView(scrollLeft, scrollTop);
            actionHandler.onScrollViewChanged(firstRowIndex, lastRowIndex,
                    firstColumnIndex, lastColumnIndex);
            resetColHeaders();
            resetRowHeaders();
            resetCellContents();
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

        // Sheet where cells are stored
        sheet.setClassName("sheet");
        sheet.setTabIndex(3);

        resizeLine.setClassName(RESIZE_LINE_CLASSNAME);
        spreadsheet.appendChild(resizeLine);

        resizeLineStable.setClassName(RESIZE_LINE_CLASSNAME);
        sheet.appendChild(resizeLineStable);

        // Dynamic position & size styles for this spreadsheet
        style.setType("text/css");
        style.setId(sheetId + "-dynamicStyle");
        Document.get().getBody().getParentElement().getFirstChild()
                .appendChild(style);

        // Workbook styles
        sheetStyle.setType("text/css");
        sheetStyle.setId(sheetId + "-sheetStyle");
        Document.get().getBody().getParentElement().getFirstChild()
                .appendChild(sheetStyle);

        // Custom cell size styles (because of borders)
        customCellSizeStyle.setType("text/css");
        customCellSizeStyle.setId(sheetId + "-customCellSizeStyle");
        Document.get().getBody().getParentElement().getFirstChild()
                .appendChild(customCellSizeStyle);

        // styles for cell selection (cell outline + col&row header background)
        selectionStyle.setType("text/css");
        selectionStyle.setId(sheetId + "-selectionStyle");
        style.getParentElement().appendChild(selectionStyle);

        // style for the selected cell when the cell range outline is hidden
        insertRule(selectionStyle, ".notusedselector" + SELECTED_CELL_STYLE2);

        // styles for cell range selection (cells&headers background)
        cellRangeStyle.setType("text/css");
        cellRangeStyle.setId(sheetId + "-cellRangeStyle");
        style.getParentElement().appendChild(cellRangeStyle);

        // initial cell range selection style (not visible)
        insertRule(cellRangeStyle, ".notusedselector"
                + SELECTED_CELL_RANGE_STYLES);
        insertRule(cellRangeStyle, ".notusedselector" + SELECTED_HEADER_STYLES);

        // style for "hiding" the edited cell
        editedCellStyle.setType("text/css");
        editedCellStyle.setId(sheetId + "-editedCellStyle");
        style.getParentElement().appendChild(editedCellStyle);
        insertRule(editedCellStyle, ".notusedselector" + EDITING_CELL_STYLE);

        // style for hiding the cell inside merged regions
        mergedRegionStyle.setType("text/css");
        mergedRegionStyle.setId(sheetId + "-mergedRegionStyle");
        style.getParentElement().appendChild(mergedRegionStyle);

        resizeStyle.setType("text/css");
        resizeStyle.setId(sheetId + "-resizeStyle");
        style.getParentElement().appendChild(resizeStyle);

        // Corner div
        corner.setClassName("corner");
        spreadsheet.appendChild(corner);

        // floater, extra element for adjusting scroll bars correctly
        floater.setClassName("floater");

        // input
        input.getStyle().setWidth(0.0d, Unit.PX);
        sheet.appendChild(input);

        // extra element for counting the pixels per inch so points can be
        // converted to pixels
        ppiCounter.getStyle().setWidth(1, Unit.IN);
        ppiCounter.getStyle().setPosition(Position.ABSOLUTE);
        ppiCounter.getStyle().setVisibility(Visibility.HIDDEN);
        ppiCounter.getStyle().setPadding(0, Unit.PX);
        spreadsheet.appendChild(ppiCounter);

        selectionWidget.setSpreadsheetElement(spreadsheet);
    }

    /** Remove sheet DOM elements created */
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
        style.removeFromParent();
        sheetStyle.removeFromParent();
        customCellSizeStyle.removeFromParent();
        selectionStyle.removeFromParent();
        cellRangeStyle.removeFromParent();
        editedCellStyle.removeFromParent();
        resizeStyle.removeFromParent();
        mergedRegionStyle.removeFromParent();
        if (hyperlinkStyle != null) {
            hyperlinkStyle.removeFromParent();
        }
    }

    /** Initialize scroll and mouse listeners to make spreadsheet interactive */
    private void initListeners() {
        Event.sinkEvents(input, Event.ONKEYPRESS | Event.ONKEYDOWN
                | Event.FOCUSEVENTS | Event.ONCLICK);
        Event.setEventListener(input, new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {
                final int type = event.getTypeInt();
                final int keyCode = event.getKeyCode();
                if (type == Event.ONFOCUS) {
                    actionHandler.onCellInputFocus();
                    event.stopPropagation();
                } else if (type == Event.ONBLUR) {
                    if (editingCell) {
                        actionHandler.onCellInputBlur(input.getValue());
                        event.stopPropagation();
                    }
                } else if (type == Event.ONKEYDOWN) {
                    if (editingCell) {
                        switch (keyCode) {
                        case KeyCodes.KEY_BACKSPACE:
                            handleInputElementValueChange(true);
                            break;
                        case KeyCodes.KEY_ESCAPE:
                            actionHandler.onCellInputCancel();
                            break;
                        case KeyCodes.KEY_TAB:
                            actionHandler.onCellInputTab(input.getValue(),
                                    event.getShiftKey());
                            // prevent the default tab from happening (switches
                            // focus)
                            event.preventDefault();
                            break;
                        case KeyCodes.KEY_UP:
                            if (!inputClicked) {
                                actionHandler.onCellInputEnter(
                                        input.getValue(), true);
                            }
                            break;
                        case KeyCodes.KEY_DOWN:
                            if (!inputClicked) {
                                actionHandler.onCellInputEnter(
                                        input.getValue(), false);
                            }
                            break;
                        case KeyCodes.KEY_LEFT:
                            if (!inputClicked) {
                                actionHandler.onCellInputTab(input.getValue(),
                                        true);
                            }
                            break;
                        case KeyCodes.KEY_RIGHT:
                            if (!inputClicked) {
                                actionHandler.onCellInputTab(input.getValue(),
                                        false);
                            }
                            break;
                        default:
                            break;
                        }
                    } else {
                        actionHandler
                                .onSheetKeyPress(event,
                                        convertUnicodeIntoCharacter(event
                                                .getCharCode()));
                    }
                    event.stopPropagation();
                } else if (type == Event.ONCLICK && editingCell) {
                    inputClicked = true;
                } else { // ONKEYPRESS
                    if (editingCell) {
                        if (keyCode == KeyCodes.KEY_ENTER) {
                            actionHandler.onCellInputEnter(input.getValue(),
                                    event.getShiftKey());
                        } else {
                            handleInputElementValueChange(true);
                        }
                        event.stopPropagation();
                    }
                }
            }
        });
        Event.sinkEvents(sheet, Event.ONSCROLL | Event.ONMOUSEDOWN
                | Event.ONMOUSEMOVE | Event.ONMOUSEOVER | Event.ONMOUSEOUT
                | Event.ONMOUSEUP | Event.TOUCHEVENTS | Event.ONLOSECAPTURE
                | Event.ONCLICK | Event.ONDBLCLICK | Event.ONKEYPRESS
                | Event.ONKEYDOWN | Event.FOCUSEVENTS);
        Event.setEventListener(sheet, new EventListener() {
            private int tempCol;
            private int tempRow;
            private boolean sheetFocused;

            @Override
            public void onBrowserEvent(Event event) {
                final int typeInt = event.getTypeInt();
                if (typeInt == Event.ONSCROLL) {
                    scrollHandler.trigger();
                    moveHeadersToMatchScroll();
                } else if (typeInt == Event.ONFOCUS) {
                    sheetFocused = true;
                    System.out.println("FOCUS GAINED !");
                } else if (typeInt == Event.ONBLUR) {
                    sheetFocused = false;
                    System.out.println("FOCUS LOST to: "
                            + event.getRelatedEventTarget());
                } else if (typeInt == Event.ONKEYPRESS && !editingCell) {
                    final int keyCode = event.getKeyCode();
                    if (!sheetFocused) {
                        return; // focus in input or custom editor
                    }
                    // these have been handled with onKeyDown (FF causes both
                    // for some reason!)
                    switch (keyCode) {
                    case KeyCodes.KEY_UP:
                    case KeyCodes.KEY_DOWN:
                    case KeyCodes.KEY_LEFT:
                    case KeyCodes.KEY_RIGHT:
                    case KeyCodes.KEY_TAB:
                    case KeyCodes.KEY_BACKSPACE:
                    case KeyCodes.KEY_DELETE:
                        event.preventDefault();
                        event.stopPropagation();
                        break;
                    default:
                        actionHandler
                                .onSheetKeyPress(event,
                                        convertUnicodeIntoCharacter(event
                                                .getCharCode()));
                    }
                } else if (typeInt == Event.ONKEYDOWN && !editingCell) {
                    if (!sheetFocused) {
                        return; // focus in input or custom editor
                    }
                    final int keyCode = event.getKeyCode();
                    if (keyCode == KeyCodes.KEY_BACKSPACE) {
                        actionHandler.onSheetKeyPress(event, "");
                        // prevent the default browser action, i.e. Chrome would
                        // try to navigate to previous page...
                        event.preventDefault();
                        event.stopPropagation();
                    } else if (keyCode == KeyCodes.KEY_UP
                            || keyCode == KeyCodes.KEY_DOWN
                            || keyCode == KeyCodes.KEY_LEFT
                            || keyCode == KeyCodes.KEY_RIGHT
                            || keyCode == KeyCodes.KEY_TAB
                            || keyCode == KeyCodes.KEY_DELETE) {
                        actionHandler.onSheetKeyPress(event, "");
                        // prevent the default browser action (scroll to key
                        // direction) or switch focus (tab)
                        event.preventDefault();
                        event.stopPropagation();
                    }
                } else if (typeInt == Event.ONMOUSEOUT
                        || typeInt == Event.ONMOUSEOVER) {
                    if (selectingCells) {
                        // cell comments && link tooltips not visible when
                        // selecting cells with drag
                        return;
                    }
                    Element target = event.getEventTarget().cast();
                    String className = target.getClassName();
                    if (className.endsWith(MERGED_CELL_CLASSNAME)) {
                        className = className.replace(" "
                                + MERGED_CELL_CLASSNAME, "");
                    }
                    if (className.equals(SheetImage.SHEET_IMAGE_CLASSNAME)) {
                        target = event.getCurrentEventTarget().cast();
                        className = target.getClassName();
                    }
                    // if mouse moved to/from a comment mark triangle, or the
                    // latest cell comment's cell, show/hide cell comment
                    if (className.equals(CELL_COMMENT_TRIANGLE_CLASSNAME)
                            || className.equals(cellCommentCellClassName)
                            || (cellCommentsMap != null && cellCommentsMap
                                    .containsKey(className))) {
                        updateCellCommentDisplay(event, target);
                    } else {
                        if (cellCommentOverlay.isShowing()) {
                            cellCommentOverlay.hide();
                            cellCommentCellClassName = null;
                            cellCommentCellColumn = -1;
                            cellCommentCellRow = -1;
                        }
                    }

                    if (target.getParentElement().equals(sheet)
                            && cellLinksMap != null
                            && cellLinksMap.containsKey(className)) {
                        parseColRow(className);
                        updateCellLinkTooltip(typeInt, parsedCol, parsedRow,
                                cellLinksMap.get(className));
                        return;
                    } else if (hyperlinkTooltip.isVisible()) {
                        hyperlinkTooltip.hide();
                    }
                } else {
                    if (typeInt == Event.ONMOUSEMOVE && !selectingCells) {
                        if (cellCommentCellColumn != -1
                                && cellCommentCellRow != -1) {
                            // the comment should only be displayed after the
                            // mouse has "stopped" on top of a cell with a
                            // comment
                            cellCommentHandler.trigger();
                        }
                        return;
                    }
                    final Element target = event.getEventTarget().cast();

                    if (customEditorWidget != null) {
                        Element customWidgetElement = customEditorWidget
                                .getElement();
                        if (customWidgetElement.isOrHasChild(target)
                                || customWidgetElement.getParentElement() != null
                                && customWidgetElement.getParentElement()
                                        .isOrHasChild(target)) {
                            // allow sheet context menu on top of custom editors
                            // (if the widget event handling allows it)
                            if (event.getButton() == NativeEvent.BUTTON_RIGHT) {
                                if (typeInt == Event.ONMOUSEDOWN) {
                                    parseColRow(customWidgetElement
                                            .getParentElement().getClassName());
                                    actionHandler.onCellRightClick(event,
                                            parsedCol, parsedRow);
                                }
                            } else {
                                if (selectingCells) {
                                    DOM.releaseCapture((Element) sheet.cast());
                                    if ((selectedCellCol != tempCol || selectedCellRow != tempRow)
                                            && tempCol != -1 && tempRow != -1) {
                                        actionHandler
                                                .onFinishedSelectingCellsWithDrag(
                                                        selectedCellCol,
                                                        tempCol,
                                                        selectedCellRow,
                                                        tempRow);
                                    } else {
                                        actionHandler.onCellClick(
                                                parsedCol,
                                                parsedRow,
                                                target.getInnerText(),
                                                event.getShiftKey(),
                                                event.getMetaKey()
                                                        || event.getCtrlKey(),
                                                true);
                                    }
                                    selectingCells = false;
                                }
                            }
                            tempCol = -1;
                            tempRow = -1;
                            return;
                        }
                    }

                    if (input.isOrHasChild(target)) {
                        if (typeInt == Event.ONMOUSEDOWN
                                && event.getButton() == NativeEvent.BUTTON_RIGHT) {
                            parseColRow(getSelectedCell().getElement()
                                    .getClassName());
                            actionHandler.onCellRightClick(event, parsedCol,
                                    parsedRow);
                        }
                        return;
                    }
                    final boolean sheetOrChild = DOM.isOrHasChild(
                            (Element) sheet.cast(), target);

                    final String className = target.getClassName();
                    if (sheetOrChild) {
                        parseColRow(className);
                    }
                    // Uncommenting this will prevent selecting (or painting) a
                    // cell that has a component inside
                    // if (customWidgetMap != null
                    // && customWidgetMap.containsKey(toKey(parsedCol,
                    // parsedRow))) {
                    // return;
                    // }
                    if ((typeInt == Event.ONMOUSEDOWN || typeInt == Event.ONTOUCHSTART)
                            && !target.getParentElement().equals(sheet)) {
                        return;
                    }

                    if (event.getButton() == NativeEvent.BUTTON_RIGHT) {
                        if (typeInt == Event.ONMOUSEDOWN && parsedCol != 0
                                && parsedRow != 0) {
                            actionHandler.onCellRightClick(event, parsedCol,
                                    parsedRow);
                        }
                        event.stopPropagation();
                        event.preventDefault();
                    } else {
                        switch (typeInt) {
                        case Event.ONTOUCHSTART:
                            if (event.getTouches().length() > 1) {
                                return;
                            }
                        case Event.ONMOUSEDOWN:
                            if (!sheetOrChild) {
                                if (selectingCells) {
                                    DOM.releaseCapture((Element) sheet.cast());
                                    actionHandler
                                            .onFinishedSelectingCellsWithDrag(
                                                    selectedCellCol, tempCol,
                                                    selectedCellRow, tempRow);
                                    tempCol = -1;
                                    tempRow = -1;
                                }
                                break;
                            } else {
                                sheet.focus();
                            }
                            if (editingCell && !input.isOrHasChild(target)) {
                                actionHandler.onCellInputBlur(input.getValue());
                            }
                            if (target.equals(sheet) || target.equals(floater)) {
                                // FIXME the click is so close to the edge that
                                // somehow it is not registered to the proper
                                // cell DIV.
                                return;
                            }
                            event.stopPropagation();
                            event.preventDefault();
                            if (!event.getCtrlKey() && !event.getMetaKey()
                                    && !event.getShiftKey()) {
                                // link cells are special case (only when no
                                // keys
                                // used)
                                if (cellLinksMap != null
                                        && cellLinksMap.containsKey(toKey(
                                                parsedCol, parsedRow))) {
                                    actionHandler.onLinkCellClick(parsedCol,
                                            parsedRow);
                                    tempCol = -1;
                                    tempRow = -1;
                                    selectingCells = false;
                                } else {
                                    actionHandler.onCellClick(
                                            parsedCol,
                                            parsedRow,
                                            target.getInnerText(),
                                            event.getShiftKey(),
                                            event.getMetaKey()
                                                    || event.getCtrlKey(),
                                            false);
                                    selectingCells = true;
                                    tempCol = parsedCol;
                                    tempRow = parsedRow;
                                    DOM.setCapture((Element) sheet.cast());
                                }
                            } else {
                                actionHandler.onCellClick(parsedCol, parsedRow,
                                        target.getInnerText(),
                                        event.getShiftKey(), event.getMetaKey()
                                                || event.getCtrlKey(), true);
                                tempCol = -1;
                                tempRow = -1;
                                selectingCells = false;
                            }
                            // DOM.eventPreventDefault(event);
                            break;
                        case Event.ONMOUSEUP:
                        case Event.ONTOUCHEND:
                        case Event.ONTOUCHCANCEL:
                        case Event.ONLOSECAPTURE:
                            if (selectingCells) {
                                DOM.releaseCapture((Element) sheet.cast());
                                if ((selectedCellCol != tempCol || selectedCellRow != tempRow)
                                        && tempCol != -1 && tempRow != -1) {
                                    actionHandler
                                            .onFinishedSelectingCellsWithDrag(
                                                    selectedCellCol, tempCol,
                                                    selectedCellRow, tempRow);
                                } else {
                                    actionHandler.onCellClick(
                                            parsedCol,
                                            parsedRow,
                                            target.getInnerText(),
                                            event.getShiftKey(),
                                            event.getMetaKey()
                                                    || event.getCtrlKey(), true);
                                }
                                selectingCells = false;
                            }
                            tempCol = -1;
                            tempRow = -1;
                            break;
                        case Event.ONMOUSEMOVE:
                        case Event.ONTOUCHMOVE:
                            if (!sheetOrChild) {
                                if (DOM.isOrHasChild(
                                        (Element) spreadsheet.cast(), target)) {
                                    final int header = isHeader(className);
                                    if (header > 0) {
                                        int parsedHeaderIndex = parseHeaderIndex(className);
                                        if (header == 1) { // row
                                            final int colIndex = getLeftColumnIndex() - 1;
                                            tempCol = colIndex < 1 ? 1
                                                    : colIndex;
                                            tempRow = parsedHeaderIndex;
                                            actionHandler
                                                    .onSelectingCellsWithDrag(
                                                            tempCol,
                                                            parsedHeaderIndex);
                                        } else { // column
                                            final int rowIndex = getTopRowIndex() - 1;
                                            tempRow = rowIndex < 1 ? 1
                                                    : rowIndex;
                                            tempCol = parsedHeaderIndex;
                                            actionHandler
                                                    .onSelectingCellsWithDrag(
                                                            parsedHeaderIndex,
                                                            tempRow);
                                        }
                                    }
                                }
                            } else {
                                if (selectingCells
                                        && (parsedCol != tempCol || parsedRow != tempRow)) {
                                    if (parsedCol == 0) { // on top of scroll
                                                          // bar
                                        if (event.getClientX() > sheet
                                                .getAbsoluteRight()) {
                                            parsedCol = getRightColumnIndex() + 1;
                                        } else {
                                            parsedCol = tempCol;
                                        }
                                    }
                                    if (parsedRow == 0) {
                                        if (event.getClientY() > sheet
                                                .getAbsoluteBottom()) {
                                            parsedRow = getBottomRowIndex() + 1;
                                        } else {
                                            parsedRow = tempRow;
                                        }
                                    }
                                    actionHandler.onSelectingCellsWithDrag(
                                            parsedCol, parsedRow);
                                    tempCol = parsedCol;
                                    tempRow = parsedRow;
                                }
                            }
                            break;
                        case Event.ONDBLCLICK:
                            actionHandler.onCellDoubleClick(parsedCol,
                                    parsedRow, target.getInnerText());
                            event.stopPropagation();
                        default:
                            break;
                        }
                    }
                }
            }
        });
        // for some reason the click event is not fired normally for headers
        Event.addNativePreviewHandler(new NativePreviewHandler() {

            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                int eventTypeInt = event.getTypeInt();
                final NativeEvent nativeEvent = event.getNativeEvent();
                if (eventTypeInt == Event.ONCLICK) {
                    Element target = nativeEvent.getEventTarget().cast();
                    String className = target.getClassName();
                    int i = isHeader(className);
                    if (i == 1 || i == 2) {
                        int index = parseHeaderIndex(className);
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
                        int i = isHeader(className);
                        if (i == 1) { // row
                            i = parseHeaderIndex(className);
                            startRowResizeDrag(i - 1, nativeEvent.getClientX(),
                                    nativeEvent.getClientY());
                        } else if (i == 2) { // col
                            i = parseHeaderIndex(className);
                            columnResizeCancelled = false;
                            startColumnResizeDrag(i - 1,
                                    nativeEvent.getClientX(),
                                    nativeEvent.getClientY());
                        }
                        event.cancel();
                    } else if (className
                            .equals(HEADER_RESIZE_DND_SECOND_CLASSNAME)) {
                        className = target.getParentElement().getClassName();
                        int i = isHeader(className);
                        if (i == 1) { // row
                            i = parseHeaderIndex(className);
                            startRowResizeDrag(i, nativeEvent.getClientX(),
                                    nativeEvent.getClientY());
                        } else if (i == 2) { // col
                            i = parseHeaderIndex(className);
                            columnResizeCancelled = false;
                            startColumnResizeDrag(i, nativeEvent.getClientX(),
                                    nativeEvent.getClientY());
                        }
                        event.cancel();
                    }
                } else if (resizing && eventTypeInt == Event.ONMOUSEMOVE) {
                    if (resizedColumnIndex != -1) {
                        handleColumnResizeDrag(nativeEvent.getClientX(),
                                nativeEvent.getClientY());
                    } else if (resizedRowIndex != -1) {
                        handleRowResizeDrag(nativeEvent.getClientX(),
                                nativeEvent.getClientY());
                    } else {
                        resizing = false;
                    }
                    event.cancel();
                } else if (resizing && eventTypeInt == Event.ONMOUSEUP) {
                    columnResizeCancelled = true;
                    resizing = false;
                    clearCSSRules(resizeStyle);
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
                        stopRowResizeDrag(event.getNativeEvent().getClientY());
                    }
                } else if (eventTypeInt == Event.ONDBLCLICK
                        && actionHandler.canResize()) {
                    Element target = nativeEvent.getEventTarget().cast();
                    String className = target.getClassName();
                    if (className.equals(HEADER_RESIZE_DND_FIRST_CLASSNAME)) {
                        className = target.getParentElement().getClassName();
                        int i = isHeader(className);
                        if (i == 1) { // row
                            // autofit row ???
                        } else if (i == 2) { // col
                            i = parseHeaderIndex(className);
                            actionHandler
                                    .onColumnHeaderResizeDoubleClick(i - 1);
                        }
                        event.cancel();
                    } else if (className
                            .equals(HEADER_RESIZE_DND_SECOND_CLASSNAME)) {
                        className = target.getParentElement().getClassName();
                        int i = isHeader(className);
                        if (i == 1) { // row
                            // autofit row ???
                        } else if (i == 2) { // col
                            i = parseHeaderIndex(className);
                            actionHandler.onColumnHeaderResizeDoubleClick(i);
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
                        int i = isHeader(className);
                        if (i == 1 || i == 2) {
                            int index = parseHeaderIndex(className);
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
        DivElement header = rowHeaders.get(rowIndex - firstRowIndex);
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
                DivElement header = colHeaders.get(tempColumnIndex
                        - firstColumnIndex);
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
                updateColWidths(newSizes);
            }
        }

        resizedColumnIndex = -1;
    }

    private void updateColWidths(Map<Integer, Integer> newSizes) {
        for (Entry<Integer, Integer> entry : newSizes.entrySet()) {
            int col = entry.getKey() - 1;

            for (int i = firstRowIndex; i <= lastRowIndex; i++) {
                if (i - firstRowIndex < rows.size()) {
                    ArrayList<Cell> cells = rows.get(i - firstRowIndex);
                    if (col < cells.size()) {
                        Cell cell = cells.get(col);
                        cell.refreshWidth();
                    }
                }
            }
        }
    }

    private void handleRowResizeDrag(int clientX, int clientY) {
        if (clientX < (sheet.getAbsoluteLeft() - 50)
                || clientX > sheet.getAbsoluteRight()
                || clientY < sheet.getAbsoluteTop()
                || clientY > sheet.getAbsoluteBottom()) {
            return;
        }
        resized = true;
        int delta = clientY - resizeFirstEdgePos;
        clearCSSRules(resizeStyle);
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
            insertRule(resizeStyle, rule);
            if (delta != 0) {
                // resize the "last" header
                rule = ".v-spreadsheet > div.rh.row" + index + "{height:"
                        + (getRowHeight(index) + delta) + "px}";
                insertRule(resizeStyle, rule);
            }

        } else {
            // only the dragged header size has changed.
            int px = clientY - resizeFirstEdgePos;
            resizeTooltipLabel.setText("Height: " + px + "px ≈ "
                    + convertPixelsToPoint(px) + "pt");
            // enter custom size for the resized row header
            rule = ".v-spreadsheet > div.rh.row" + resizedRowIndex + "{height:"
                    + px + "px;}";
            insertRule(resizeStyle, rule);
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
                insertRule(resizeStyle, rule);
            }
            rule = ".v-spreadsheet.row-resizing > div.resize-line.rh {margin-top:"
                    + (margin - 1) + "px;}";
            insertRule(resizeStyle, rule);
        }
        showResizeTooltipRelativeTo(clientX, clientY);
    }

    private void handleColumnResizeDrag(int clientX, int clientY) {
        if (clientX < sheet.getAbsoluteLeft()
                || clientX > sheet.getAbsoluteRight()
                || clientY < (sheet.getAbsoluteTop() - 20)
                || clientY > sheet.getAbsoluteBottom()) {
            return;
        }
        resized = true;
        int delta = clientX - resizeFirstEdgePos;
        clearCSSRules(resizeStyle);
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
            insertRule(resizeStyle, rule);
            if (delta != 0) {
                // resize the "last" header
                rule = ".v-spreadsheet > div.ch.col" + index + "{width:"
                        + (actionHandler.getColWidthActual(index) + delta)
                        + "px}";
                insertRule(resizeStyle, rule);
            }
        } else {
            // only the dragged header size has changed.
            int px = clientX - resizeFirstEdgePos;
            resizeTooltipLabel.setText("Width: " + px + "px");
            // enter custom size for the resized column header
            rule = ".v-spreadsheet > div.ch.col" + resizedColumnIndex
                    + "{width:" + px + "px;}";
            insertRule(resizeStyle, rule);
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
                insertRule(resizeStyle, rule);
            }
            rule = ".v-spreadsheet.col-resizing > div.resize-line.ch {margin-left:"
                    + (margin - 1) + "px;}";
            insertRule(resizeStyle, rule);
        }
        showResizeTooltipRelativeTo(clientX, clientY);
    }

    private void showResizeTooltipRelativeTo(int clientX, int clientY) {
        int left = clientX + 10;
        int top = clientY - 25;
        resizeTooltip.setPopupPosition(left, top);
    }

    /** returns 1 for row 2 for column 0 for not header */
    public static native int isHeader(String str)
    /*-{
        var c = str.charAt(0);
        if (c === 'r' ) {
            c = str.charAt(1);
            if (c === 'h') {
                return 1;
            }
        } else if (c === 'c') {
            c = str.charAt(1);
            if (c === 'h') {
            return 2;
            }
        }
        return 0;
     }-*/;

    /** returns the header index */
    public static native int parseHeaderIndex(String str)
    /*-{
        var strlen = str.length;
        var i = 0;
        var code = 0;
        var index = 0;
        while(i<strlen) {
            code = str.charCodeAt(i);
            if(code > 47 && code < 58) {
                index = index * 10 + code - 48;
            }
            i++;
        }
        return index;
     }-*/;

    int parsedRow;
    int parsedCol;

    public final native void parseColRow(String str)
    /*-{
        var strlen = str.length;
        var i=0;
        var code = 0;
        var flags = 0;
        var r = 0;
        var c = 0;
        while(i<strlen) {
            code = str.charCodeAt(i);
            if(code === 32) {
                flags = 1;
            } else if(code > 47 && code < 58) {
                if(flags === 0) {
                    c = c * 10 + code - 48;
                } else {
                    r = r * 10 + code - 48;
                }
            }
            i++;
        }
        this.@com.vaadin.addon.spreadsheet.client.SheetWidget::parsedRow = r;
        this.@com.vaadin.addon.spreadsheet.client.SheetWidget::parsedCol = c;

    }-*/;

    public final static native String convertUnicodeIntoCharacter(int charCode)
    /*-{
        return String.fromCharCode(charCode);
     }-*/;

    public String getCellValue(int column, int row) {
        final String value = cellData.get(toKey(column, row));
        return value == null ? "" : value;
    }

    private String createHeaderDNDHTML() {
        return HEADER_RESIZE_DND_HTML;
    }

    /**
     * Called after scrolling to move headers in order to keep them in sync with
     * the spreadsheet contents
     */
    private void moveHeadersToMatchScroll() {
        if (displayRowColHeadings) {
            updateCSSRule(style, ".v-spreadsheet .ch", "marginLeft",
                    (50 - sheet.getScrollLeft()) + "px");
            updateCSSRule(style, ".v-spreadsheet .rh", "marginTop",
                    (49 - sheet.getScrollTop()) + "px");
        } else {
            updateCSSRule(style, ".v-spreadsheet .ch", "marginLeft",
                    (0 - sheet.getScrollLeft()) + "px");
            updateCSSRule(style, ".v-spreadsheet .rh", "marginTop",
                    (31 - sheet.getScrollTop()) + "px");
        }
    }

    /** Update styles in for this spreadsheet */
    private void updateStyles(boolean reloadCellStyles) {
        // styles for sizes and position
        String[] rules = new String[actionHandler.getMaximumRows()
                + actionHandler.getMaximumCols() + 2];
        definedRowHeights = new int[actionHandler.getMaximumRows()];
        int ruleIndex = 0;
        float height = 0;
        for (int i = 1; i <= actionHandler.getMaximumRows(); i++) {
            String display = actionHandler.isRowHidden(i) ? "display:none;"
                    : "";
            rules[ruleIndex++] = "." + getStylePrimaryName() + " .row" + i
                    + " { " + display + "height: "
                    + (actionHandler.getRowHeight(i)) + "pt; top: " + height
                    + "pt;}\n";
            height += actionHandler.getRowHeight(i);
            definedRowHeights[i - 1] = convertPointsToPixel(actionHandler
                    .getRowHeight(i));
        }
        int width = 0;
        for (int i = 1; i <= actionHandler.getMaximumCols(); i++) {
            String display = actionHandler.isColumnHidden(i) ? "display: none;"
                    : "";
            rules[ruleIndex++] = "." + getStylePrimaryName() + " .col" + i
                    + " { " + display + "width: "
                    + (actionHandler.getColWidth(i)) + "px; left: " + width
                    + "px;}\n";
            width += actionHandler.getColWidthActual(i);
        }

        // these get properly assigned in moveHeadersToMatchScroll()
        rules[ruleIndex++] = "." + getStylePrimaryName()
                + " .rh { margin-top: 0px; }";
        rules[ruleIndex++] = "." + getStylePrimaryName()
                + " .ch { margin-left: 0px; }";

        resetStyleSheetRules(style, rules);

        moveHeadersToMatchScroll();

        // update floater size the adjust scroll bars correctly
        floater.getStyle().setHeight(height, Unit.PT);
        floater.getStyle().setWidth(width, Unit.PX);

        // styles for individual cells
        if (reloadCellStyles) {
            Map<Integer, String> styles = actionHandler
                    .getCellStyleToCSSStyle();
            if (styles != null) {
                try {
                    for (Entry<Integer, String> entry : styles.entrySet()) {
                        int styleIndex;
                        if (entry.getKey() == 0) {
                            styleIndex = insertRule(sheetStyle,
                                    ".v-spreadsheet .sheet > div, .v-spreadsheet .sheet > input {"
                                            + entry.getValue() + "}");
                        } else {
                            styleIndex = insertRule(sheetStyle,
                                    ".notusedselector {" + entry.getValue()
                                            + "}");
                        }
                        cellStyleToCSSRuleIndex.put(entry.getKey(), styleIndex);
                    }
                } catch (Exception e) {
                    debugConsole.log(
                            Level.SEVERE,
                            "Error while creating the cell styles, "
                                    + e.getMessage());
                }
            }
            List<String> customCellBorderStyles = actionHandler
                    .getCustomCellBorderStyles();
            if (customCellBorderStyles != null) {
                for (String cssText : customCellBorderStyles) {
                    insertRule(customCellSizeStyle, cssText.replace(".col",
                            ".v-spreadsheet .sheet div.col"));
                }
            }
        }
    }

    /** Replace stylesheet with the array of rules given */
    static private void resetStyleSheetRules(StyleElement stylesheet,
            String[] rules) {
        clearCSSRules(stylesheet);
        for (int i = 0; i < rules.length; i++) {
            insertRule(stylesheet, rules[i]);
        }
    }

    /** Insert one CSS rule to the end of given stylesheet */
    public final static native int insertRule(StyleElement stylesheet,
            String css)
    /*-{
        return stylesheet.sheet.insertRule(css, stylesheet.sheet.cssRules.length);
    }-*/;

    public final static native void deleteRule(StyleElement stylesheet,
            int ruleindex)
    /*-{
        stylesheet.sheet.deleteRule(ruleindex);
     }-*/;

    public final static native int replaceSelector(StyleElement stylesheet,
            String selector, int ruleindex)
    /*-{
        var oldSelector = stylesheet.sheet.cssRules[ruleindex].selectorText;        
        var cssText = stylesheet.sheet.cssRules[ruleindex].cssText.replace(oldSelector, selector);
        stylesheet.sheet.deleteRule(ruleindex);
        return stylesheet.sheet.insertRule(cssText, ruleindex);
    }-*/;

    /**
     * Adds the given selector into the specific rule in the stylesheet. The
     * selector should be a single selector or a list of selectors, but should
     * NOT end in a comma (",").
     * 
     * @param stylesheet
     * @param selector
     * @param ruleindex
     */
    public final static native int addSelector(StyleElement stylesheet,
            String selector, int ruleindex)
    /*-{
        var cssText = selector + ","+stylesheet.sheet.cssRules[ruleindex].cssText;
        stylesheet.sheet.deleteRule(ruleindex);
        return stylesheet.sheet.insertRule(cssText, ruleindex);
     }-*/;

    public final static native String getSelector(StyleElement stylesheet,
            int ruleindex)
    /*-{
        var x = stylesheet.sheet.cssRules[ruleindex].selectorText;
        return x;
    }-*/;

    /** Search and update a given CSS rule in a stylesheet */
    public final static native void updateCSSRule(StyleElement stylesheet,
            String selector, String property, String value)
    /*-{
            var classes = stylesheet.sheet.cssRules;
            for(var x=0;x<classes.length;x++) {
                    if(classes[x].selectorText.toLowerCase()==selector) {
                            classes[x].style[property]=value;
                    }
            }       
    }-*/;

    public final static native int replaceCssRule(StyleElement stylesheet,
            String css, int ruleindex)
    /*-{
        stylesheet.sheet.deleteRule(ruleindex);
        return stylesheet.sheet.insertRule(css, ruleindex);
    }-*/;

    /** Clears the rules starting from the given index */
    public final static native void clearCSSRules(StyleElement stylesheet)
    /*-{
        var rules = stylesheet.sheet.cssRules? stylesheet.sheet.cssRules : stylesheet.sheet.rules;        
        while ( rules.length > 0 ) {
            if (stylesheet.sheet.deleteRule) {
                stylesheet.sheet.deleteRule(0);
            } else {
                stylesheet.sheet.removeRule(0);
            } 
        }
    }-*/;

    /**
     * Update the column headers to match the state. Create and recycle header
     * divs as needed.
     */
    private void resetColHeaders() {
        for (int i = firstColumnIndex; i <= lastColumnIndex; i++) {
            DivElement colHeader;
            if (i - firstColumnIndex < colHeaders.size()) {
                colHeader = colHeaders.get(i - firstColumnIndex);
            } else {
                colHeader = Document.get().createDivElement();
                spreadsheet.insertBefore(colHeader, corner);
                colHeaders.add(i - firstColumnIndex, colHeader);
            }
            colHeader.setClassName("ch col" + (i));
            colHeader.setInnerHTML(actionHandler.getColHeader(i)
                    + createHeaderDNDHTML());
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
        for (int i = firstRowIndex; i <= lastRowIndex; i++) {
            DivElement rowHeader;
            if (i - firstRowIndex < rowHeaders.size()) {
                rowHeader = rowHeaders.get(i - firstRowIndex);
            } else {
                rowHeader = Document.get().createDivElement();
                spreadsheet.insertBefore(rowHeader, corner);
                rowHeaders.add(i - firstRowIndex, rowHeader);
            }
            rowHeader.setClassName("rh row" + (i));
            rowHeader.setInnerHTML(actionHandler.getRowHeader(i)
                    + createHeaderDNDHTML());
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
        firstColumnIndex = 1;
        firstColumnPosition = 0;
        lastColumnIndex = 0;
        emptyRowPointer = 1;
        emptyCellPointer = 1;
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
        // Remove old cells
        for (ArrayList<Cell> row : rows) {
            for (Cell cell : row) {
                sheet.removeChild(cell.getElement());
            }
            row.clear();
        }
        rows.clear();
        sheet.appendChild(floater);

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

    /**
     * Update the headers and data cells in the spreadsheet to reflect the
     * current scroll view and model
     */
    private void updateSheetDisplay() {

        int scrollTop = sheet.getScrollTop();
        int scrollLeft = sheet.getScrollLeft();
        int vScrollDiff = scrollTop - previousScrollTop;
        int hScrollDiff = scrollLeft - previousScrollLeft;

        // update the visible cell comment overlay positions
        for (CellComment cellComment : alwaysVisibleCellComments.values()) {
            if (actionHandler.isColumnHidden(cellComment.getCol())
                    || actionHandler.isRowHidden(cellComment.getRow())) {
                cellComment.hide();
            } else {
                cellComment.refreshPositionAccordingToCellRightCorner();
            }
        }

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
            debugConsole.log(Level.SEVERE, t.getMessage());
        }

        // update cells
        updateCells(vScrollDiff, hScrollDiff);
    }

    /** push the cells to the escalator */
    private void updateCells(int vScrollDiff, int hScrollDiff) {
        int firstR = rows.get(0).get(0).getRow();
        int lastR = rows.get(rows.size() - 1).get(0).getRow();
        int firstC = rows.get(0).get(0).getCol();
        int lastC = rows.get(0).get(rows.get(0).size() - 1).getCol();
        if (firstR > lastRowIndex || lastR < firstRowIndex
                || firstC > lastColumnIndex || lastC < firstColumnIndex) {
            // run escalator on all rows&columns, remove if necessary
            for (int i = firstRowIndex; i <= lastRowIndex; i++) {
                final ArrayList<Cell> row;
                if (rows.size() > (i - firstRowIndex)) {
                    row = rows.get(i - firstRowIndex);
                } else {
                    row = new ArrayList<Cell>();
                    row.ensureCapacity(lastColumnIndex - firstColumnIndex + 1);
                    rows.add(i - firstRowIndex, row);
                }
                for (int j = firstColumnIndex; j <= lastColumnIndex; j++) {
                    final Cell cell;
                    if (row.size() > (j - firstColumnIndex)) {
                        cell = row.get(j - firstColumnIndex);
                        cell.update(j, i, cellData.get(toKey(j, i)),
                                numericCellData.get(toKey(j, i)));
                    } else {
                        cell = new Cell(j, i, cellData.get(toKey(j, i)),
                                numericCellData.get(toKey(j, i)));
                        sheet.appendChild(cell.getElement());
                        row.add(j - firstColumnIndex, cell);
                    }
                }
                while (row.size() > (lastColumnIndex - firstColumnIndex + 1)) {
                    row.remove(row.size() - 1).getElement().removeFromParent();
                }
            }
            while (rows.size() > lastRowIndex - firstRowIndex + 1) {
                for (Cell cell : rows.remove(rows.size() - 1)) {
                    cell.getElement().removeFromParent();
                }
            }
        } else {
            // run escalator on some rows/cells
            ArrayList<ArrayList<Cell>> tempRows = new ArrayList<ArrayList<Cell>>();
            for (Iterator<ArrayList<Cell>> iterator = rows.iterator(); iterator
                    .hasNext();) {
                final ArrayList<Cell> row = iterator.next();
                int rIndex = row.get(0).getRow();
                // FIND OUT IF ROW INDEX HAS CHANGED FOR THE ROW (swap/remove)
                // scroll down
                if (vScrollDiff > 0) {
                    if (rIndex < firstRowIndex) {
                        // swap or remove
                        if (lastR < lastRowIndex) {
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
                    if (rIndex > lastRowIndex) {
                        if (firstR > firstRowIndex) {
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
                        if (cIndex < firstColumnIndex) {
                            // swap or remove
                            if (lastC < lastColumnIndex) {
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
                        if (cIndex > lastColumnIndex) {
                            // swap or remove
                            if (firstC > firstColumnIndex) {
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
                        cell.update(cIndex, rIndex,
                                cellData.get(toKey(cIndex, rIndex)),
                                numericCellData.get(toKey(cIndex, rIndex)));
                    }
                }
                if (hScrollDiff > 0) {
                    // add moved cells to collection
                    for (Cell cell : tempCols) {
                        row.add(cell);
                    }
                    // add new cells if required
                    while (lastC < lastColumnIndex) {
                        lastC++;
                        Cell cell = new Cell(lastC, rIndex, cellData.get(toKey(
                                lastC, rIndex)), numericCellData.get(toKey(
                                lastC, rIndex)));
                        sheet.appendChild(cell.getElement());
                        row.add(cell);
                    }
                } else if (hScrollDiff < 0) {
                    // add moved cells to collection
                    for (Cell cell : tempCols) {
                        row.add(0, cell);
                    }
                    // add new cells if required
                    while (firstC > firstColumnIndex) {
                        firstC--;
                        Cell cell = new Cell(firstC, rIndex,
                                cellData.get(toKey(firstC, rIndex)),
                                numericCellData.get(toKey(firstC, rIndex)));
                        sheet.appendChild(cell.getElement());
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
                while (lastR < lastRowIndex) {
                    ArrayList<Cell> row = new ArrayList<Cell>(lastColumnIndex
                            - firstColumnIndex + 1);
                    lastR++;
                    for (int i = firstColumnIndex; i <= lastColumnIndex; i++) {
                        Cell cell = new Cell(i, lastR, cellData.get(toKey(i,
                                lastR)), numericCellData.get(toKey(i, lastR)));
                        row.add(cell);
                        sheet.appendChild(cell.getElement());
                    }
                    rows.add(row);
                }
            } else if (vScrollDiff < 0) {
                while (firstR > firstRowIndex) {
                    ArrayList<Cell> row = new ArrayList<Cell>();
                    row.ensureCapacity(lastColumnIndex - firstColumnIndex + 1);
                    firstR--;
                    for (int i = firstColumnIndex; i <= lastColumnIndex; i++) {
                        Cell cell = new Cell(i, firstR, cellData.get(toKey(i,
                                firstR)), numericCellData.get(toKey(i, firstR)));
                        row.add(cell);
                        sheet.appendChild(cell.getElement());
                    }
                    rows.add(0, row);
                }
            }
        }
    }

    private void handleHorizontalScrollLeft(int scrollLeft) {

        int leftBound = scrollLeft - actionHandler.getColumnBufferSize();
        int rightBound = scrollLeft + scrollViewWidth
                + actionHandler.getColumnBufferSize();
        if (leftBound < 0) {
            leftBound = 0;
        }
        int move = 0;
        int add = 0;
        while (firstColumnPosition > leftBound && firstColumnIndex > 1) {
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
            firstColumnIndex = 1;
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
                    spreadsheet.insertBefore(header, corner);
                }
                colHeaders.add(0, header);
            }
            while (colHeaders.size() > (lastColumnIndex - firstColumnIndex + 1)) {
                colHeaders.remove(colHeaders.size() - 1).removeFromParent();
            }
        }
    }

    private void handleHorizontalScrollRight(int scrollLeft) {

        int leftBound = scrollLeft - actionHandler.getColumnBufferSize();
        int rightBound = scrollLeft + scrollViewWidth
                + actionHandler.getColumnBufferSize();
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
                    spreadsheet.insertBefore(header, corner);
                }
                colHeaders.add(header);
            }
            while (colHeaders.size() > (lastColumnIndex - firstColumnIndex + 1)) {
                colHeaders.remove(0).removeFromParent();
            }
        }
    }

    private void handleVerticalScrollDown(int scrollTop) {

        int topBound = scrollTop - actionHandler.getColumnBufferSize();
        int bottomBound = scrollTop + scrollViewHeight
                + actionHandler.getRowBufferSize();
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
                    spreadsheet.insertBefore(header, corner);
                }
                rowHeaders.add(header);
            }
            while (rowHeaders.size() > (lastRowIndex - firstRowIndex + 1)) {
                rowHeaders.remove(0).removeFromParent();
            }
        }
    }

    private void handleVerticalScrollUp(int scrollTop) {

        int topBound = scrollTop - actionHandler.getColumnBufferSize();
        int bottomBound = scrollTop + scrollViewHeight
                + actionHandler.getRowBufferSize();
        if (topBound < 0) {
            topBound = 0;
        }
        int move = 0;
        int add = 0;
        while (firstRowPosition > topBound && firstRowIndex > 1) {
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
            firstRowIndex = 1;
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
                    spreadsheet.insertBefore(header, corner);
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

    public void showCustomWidgets(Map<String, Widget> customWidgetMap) {
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
        cell.setValue(null, null);
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
            insertRule(mergedRegionStyle, sb.toString());
        }
        String key = toKey(region.col1, region.row1);
        Cell mergedCell = new Cell(region.col1, region.row1);
        DivElement element = mergedCell.getElement();
        element.addClassName(MERGED_CELL_CLASSNAME);
        int width = 0;
        for (int i = region.col1; i <= region.col2; i++) {
            width += actionHandler.getColWidthActual(i);
        }
        element.getStyle().setWidth(width, Unit.PX);
        element.getStyle().setHeight(
                selectionWidget.countSum(actionHandler.getRowHeights(),
                        region.row1, region.row2 + 1), Unit.PT);
        if (cellData.containsKey(key)) {
            mergedCell.setValue(cellData.get(key), numericCellData.get(key));
        }
        sheet.appendChild(element);
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

    public void updateMergedRegionSizeAndPosition(MergedRegion region,
            String oldKey, int index) {
        final String key = toKey(region.col1, region.row1);
        Cell mergedCell = mergedCells.remove(region.id);
        mergedCell
                .update(region.col1, region.row1, mergedCell.getValue(), null);
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
            replaceSelector(mergedRegionStyle, sb.toString(), index);
        } else { // should never happen
            replaceSelector(mergedRegionStyle, ".notusedselector", index);
        }
        int width = 0;
        for (int i = region.col1; i <= region.col2; i++) {
            width += actionHandler.getColWidthActual(i);
        }
        DivElement element = mergedCell.getElement();
        element.addClassName(MERGED_CELL_CLASSNAME);
        element.getStyle().setWidth(width, Unit.PX);
        float height = selectionWidget.countSum(actionHandler.getRowHeights(),
                region.row1, region.row2 + 1);
        if (width == 0 || height == 0) {
            mergedCell.getElement().getStyle().setDisplay(Display.NONE);
        } else {
            mergedCell.getElement().getStyle().setDisplay(Display.BLOCK);
        }
        element.getStyle().setHeight(height, Unit.PT);
        // POI doesn't shift cell comments together with rows, so comment might
        // be removed/added
        if (cellCommentsMap != null && cellCommentsMap.containsKey(key)) {
            mergedCell.showCellCommentMark();
        } else {
            mergedCell.removeCellCommentMark();
        }
        if (cellCommentsMap != null && cellCommentsMap.containsKey(oldKey)) {
            try {
                parseColRow(oldKey);
                Cell cell = rows.get(parsedRow - firstRowIndex).get(
                        parsedCol - firstColumnIndex);
                cell.showCellCommentMark();
            } catch (Exception e) {
                // the cell just isn't visible, no problem.
            }
        }
        // need to update the position of possible visible comment for merged
        // cell
        if (alwaysVisibleCellComments.containsKey(key)) {
            CellComment cellComment = alwaysVisibleCellComments.get(key);
            cellComment.showDependingToCellRightCorner((Element) mergedCell
                    .getElement().cast(), region.row1, region.col1);
            if (width == 0) {
                cellComment.hide();
            }
        } else if (alwaysVisibleCellComments.containsKey(oldKey)) {
            alwaysVisibleCellComments.remove(oldKey).hide();
            // add it to another cell
            try {
                setCellCommentVisible(true, oldKey);
                Cell cell;
                if (isMergedCell(oldKey)) {
                    cell = getMergedCell(oldKey);
                } else {
                    cell = rows.get(parsedRow - firstRowIndex).get(
                            parsedCol - firstColumnIndex);
                }
                cell.showCellCommentMark();
            } catch (Exception e) {
                debugConsole
                        .severe("Exception while trying to update a cell comment visibility in merged cell, "
                                + e.toString());
            }
        }
        mergedCells.put(region.id, mergedCell);
    }

    public void updateMergedRegionSize(MergedRegion region) {
        String key = toKey(region.col1, region.row1);
        Cell mergedCell = mergedCells.get(region.id);
        int width = 0;
        for (int i = region.col1; i <= region.col2; i++) {
            width += actionHandler.getColWidthActual(i);
        }
        DivElement element = mergedCell.getElement();
        element.getStyle().setWidth(width, Unit.PX);
        float height = selectionWidget.countSum(actionHandler.getRowHeights(),
                region.row1, region.row2 + 1);
        if (width == 0 || height == 0) {
            mergedCell.getElement().getStyle().setDisplay(Display.NONE);
        } else {
            mergedCell.getElement().getStyle().setDisplay(Display.BLOCK);
        }
        element.getStyle().setHeight(height, Unit.PT);
        // need to update the position of possible visible comment for merged
        // cell
        if (alwaysVisibleCellComments.containsKey(key)) {
            CellComment cellComment = alwaysVisibleCellComments.get(key);
            if (width == 0) {
                cellComment.hide();
            } else {
                cellComment.refreshPositionAccordingToCellRightCorner();
            }
        }
    }

    public void removeMergedRegion(MergedRegion region, int index) {
        String key = toKey(region.col1, region.row1);
        deleteRule(mergedRegionStyle, index);
        mergedCells.remove(region.id).getElement().removeFromParent();
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

    public void setCellLinks(Map<String, String> cellLinksMap) {
        this.cellLinksMap = cellLinksMap;
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
                style.getParentElement().appendChild(hyperlinkStyle);
                sb.append(HYPERLINK_CELL_STYLE);
                insertRule(hyperlinkStyle, sb.toString());
            } else {
                replaceSelector(hyperlinkStyle, sb.toString(), 0);
            }
        } else {
            if (hyperlinkStyle != null) {
                replaceSelector(hyperlinkStyle, ".notusedselector", 0);
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

    public void setCellComments(Map<String, String> newCellCommentsMap) {
        for (ArrayList<Cell> row : rows) {
            for (Cell cell : row) {
                String key = toKey(cell.getCol(), cell.getRow());
                if (isMergedCell(key)) {
                    cell = getMergedCell(key);
                }
                if (newCellCommentsMap.containsKey(key)) {
                    cell.showCellCommentMark();
                } else if (cellCommentsMap != null
                        && cellCommentsMap.containsKey(key)) {
                    // remove
                    cell.removeCellCommentMark();
                    cellCommentsMap.remove(key);
                }
            }
        }
        if (cellCommentsMap == null) {
            cellCommentsMap = newCellCommentsMap;
        } else {
            cellCommentsMap.putAll(newCellCommentsMap);
        }
    }

    public void setCellCommentVisible(boolean visible, String key) {
        if (visible) {
            parseColRow(key);
            final Cell cell;
            if (isMergedCell(key)) {
                cell = getMergedCell(key);
            } else {
                cell = rows.get(parsedRow - firstRowIndex).get(
                        parsedCol - firstColumnIndex);
            }
            final CellComment cellComment = new CellComment(this, sheet);
            cellComment.setCommentText(cellCommentsMap.get(key));
            cellComment.showDependingToCellRightCorner((Element) cell
                    .getElement().cast(), parsedRow, parsedCol);
            alwaysVisibleCellComments.put(key, cellComment);
        } else {
            CellComment comment = alwaysVisibleCellComments.remove(key);
            comment.hide();
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
        final Element cellElement;
        if (isMergedCell(cellClassName)) {
            cellElement = getMergedCell(cellClassName).getElement().cast();
        } else {
            cellElement = rows.get(row - firstRowIndex)
                    .get(column - firstColumnIndex).getElement().cast();
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
                    parseColRow(cellElementClassName);
                    cellCommentCellColumn = parsedCol;
                    cellCommentCellRow = parsedRow;
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
                    parseColRow(targetClassName);
                    cellCommentCellColumn = parsedCol;
                    cellCommentCellRow = parsedRow;
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
                        npe.printStackTrace();
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
            final DivElement element = rows.get(row - firstRowIndex)
                    .get(col - firstColumnIndex).getElement();
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

    /**
     * 
     * @param cellData2
     * @param removedCells
     */
    public void addCellsData(HashMap<String, String> cellData2,
            HashMap<String, Double> numericCellData2,
            ArrayList<String> removedCells) {
        // clear cached stuff

        if (removedCells != null) {
            for (String removed : removedCells) {
                cellData.remove(removed);
                numericCellData.remove(removed);
                if (isMergedCell(removed)) {
                    getMergedCell(removed).setValue(null, null);
                }
            }
        }
        for (int r = firstRowIndex; r <= lastRowIndex; r++) {
            // all rows that have some cells with data have the row index string
            // inserted
            if (cellData2.containsKey(Integer.toString(r))) {
                ArrayList<Cell> row = rows.get(r - firstRowIndex);
                for (int c = firstColumnIndex; c <= lastColumnIndex; c++) {
                    final String key = toKey(c, r);
                    if ((r == selectedCellRow && c == selectedCellCol && customCellEditorDisplayed)
                            || (customWidgetMap != null && customWidgetMap
                                    .containsKey(key))) {
                        continue;
                    }
                    final String value = cellData2.get(key);
                    Double numericValue = numericCellData2.get(key);
                    if (value != null) {
                        row.get(c - firstColumnIndex).setValue(value,
                                numericValue);
                    } else // the cell should be cleared if it is blank
                           // (cellData2 contains key but has null value)
                           // or if it has been "removed"
                    if ((cellData2.containsKey(key) || (removedCells != null && removedCells
                            .contains(key)))) {
                        row.get(c - firstColumnIndex).setValue(null, null);
                    }
                }
                // remove the row key so it is not unnecessarily cached
                cellData2.remove(Integer.toString(r));
            } else if (removedCells != null
                    && removedCells.contains(Integer.toString(r))) {
                ArrayList<Cell> row = rows.get(r - firstRowIndex);
                for (int j = firstColumnIndex; j <= lastColumnIndex; j++) {
                    final String key = toKey(j, r);
                    if ((r == selectedCellRow && j == selectedCellCol && customCellEditorDisplayed)
                            || (customWidgetMap != null && customWidgetMap
                                    .containsKey(key))) {
                        continue;
                    }
                    if (removedCells.contains(key)) {
                        row.get(j - firstColumnIndex).setValue(null, null);
                    }
                }
            }
        }
        for (String key : cellData2.keySet()) {
            if (isMergedCell(key)) {
                getMergedCell(key).setValue(cellData2.get(key),
                        numericCellData2.get(key));
            }
        }
        cellData.putAll(cellData2);
        numericCellData.putAll(numericCellData2);
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

    private void handleInputElementValueChange(final boolean update) {
        if (!isSelectedCellInView()) {
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
            final Cell selectedCell = rows.get(selectedCellRow - firstRowIndex)
                    .get(selectedCellCol - firstColumnIndex);
            selectedCell.setValue(value, null);
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
            input.getStyle().setWidth(width, Unit.PX);
        } catch (Exception e) {
            // cell is not visible yet, should not happen, but try again
            debugConsole
                    .severe("Exception while calculating input element width, "
                            + e.toString());
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
     */
    public void clearAll() {
        for (Iterator<Widget> i = iterator(); i.hasNext();) {
            remove(i.next());
        }
        customEditorWidget = null;
        sheetImages.clear();
        if (customWidgetMap != null) {
            customWidgetMap.clear();
            customWidgetMap = null;
        }
        cleanDOM();
        cellData.clear();
        numericCellData.clear();

        clearPositionStyles();
        clearCellRangeStyles();
        clearSelectedCellStyle();
        clearCellStyles();
        clearMergedCells();
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
        return cellData.get(toKey(selectedCellCol, selectedCellRow));
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

        int w = actionHandler.getColWidthActual(col1);
        for (int i = col1 + 1; i <= col2; i++) {
            w += actionHandler.getColWidthActual(i);
        }
        selectionWidget.setPosition(col1, col2, row1, row2);
        selectionWidget.setWidth(w);
        selectionWidget.setHeight(selectionWidget.countSum(
                actionHandler.getRowHeights(), row1, row2 + 1));
        if (!selectionWidget.isVisible()) {
            selectionWidget.setVisible(true);
        }
    }

    public void updateSelectedCellStyles(int col1, int col2, int row1,
            int row2, boolean replace) {
        cellRangeStylesCleared = false;
        StringBuffer sb = new StringBuffer();
        // cells
        if (replace && col1 == col2 && row1 == row2) {
            replaceSelector(cellRangeStyle, ".notusedselector", 0);
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
                    replaceSelector(cellRangeStyle,
                            sb.toString().substring(0, sb.length() - 1), 0);
                } else {
                    addSelector(cellRangeStyle,
                            sb.toString().substring(0, sb.length() - 1), 0);
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
            replaceSelector(cellRangeStyle, sb.toString(), 1);
        } else {
            addSelector(cellRangeStyle, sb.toString(), 1);
        }
    }

    /**
     * Clears the light outline on the selected cell which is visible when the
     * selection is not coherent.
     */
    public void clearSelectedCellStyle() {
        try {
            replaceSelector(selectionStyle, ".notusedselector", 0);
        } catch (Exception e) {
            clearCSSRules(selectionStyle);
            insertRule(selectionStyle, ".notusedselector"
                    + SELECTED_CELL_STYLE2);
        }
    }

    /**
     * Clears the highlight (background) on selected cells and their
     * corresponding headers.
     */
    public void clearCellRangeStyles() {
        try {
            replaceSelector(cellRangeStyle, ".notusedselector", 0);
            replaceSelector(cellRangeStyle, ".notusedselector", 1);
        } catch (Exception e) {
            clearCSSRules(cellRangeStyle);
            insertRule(cellRangeStyle, ".notusedselector "
                    + SELECTED_CELL_RANGE_STYLES);
            insertRule(cellRangeStyle, ".notusedselector"
                    + SELECTED_HEADER_STYLES);
        }
        cellRangeStylesCleared = true;
    }

    protected void clearPositionStyles() {
        clearCSSRules(style);
    }

    protected void clearCellStyles() {
        clearCSSRules(sheetStyle);
        clearCSSRules(customCellSizeStyle);
        // hyperlink style is created on-demand
        if (hyperlinkStyle != null) {
            clearCSSRules(hyperlinkStyle);
            hyperlinkStyle.removeFromParent();
            hyperlinkStyle = null;
        }
    }

    protected void clearMergedCells() {
        clearCSSRules(mergedRegionStyle);
        for (Cell mergedCell : mergedCells.values()) {
            mergedCell.getElement().removeFromParent();
        }
        mergedCells.clear();
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
            replaceSelector(cellRangeStyle, oldSelectionCssKey, 0);
            cellRangeStylesCleared = false;
        } else {
            final String oldSelector = getSelector(cellRangeStyle, 0);
            // IE likes to switch the order of the selectors
            if (oldSelector.startsWith(".row")) {
                newSelectionCssKey = ".row" + row + ".col" + column;
                oldSelectionCssKey = ".row" + selectedCellRow + ".col"
                        + selectedCellCol;
            }
            if (oldSelector.contains(newSelectionCssKey + ",")) {
                // replace
                replaceSelector(cellRangeStyle, oldSelector.replace(
                        newSelectionCssKey + ",", oldSelectionCssKey + ","), 0);
                // the headers are already highlighted for the new cell
            } else if (oldSelector.endsWith(newSelectionCssKey)) {
                replaceSelector(
                        cellRangeStyle,
                        oldSelector.substring(0, oldSelector.length()
                                - newSelectionCssKey.length())
                                + oldSelectionCssKey, 0);
            } else {
                // add
                addSelector(cellRangeStyle, oldSelectionCssKey, 0);
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
                addSelector(cellRangeStyle, sb.toString(), 1);
            }

        }
        // mark the new selected cell with light outline
        replaceSelector(selectionStyle, newSelectionCssKey, 0);
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
        final String oldSelector = getSelector(cellRangeStyle, 0);
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
        replaceSelector(cellRangeStyle, newSelector, 0);
        setSelectedCell(col, row);
    }

    /**
     * Marks the given interval as selected (highlighted background), doesn't
     * remove the old selected cells.
     * 
     * @param col1
     * @param col2
     * @param row1
     * @param row2
     */
    public void addAsSelectedCells(int col1, int col2, int row1, int row2) {
        final StringBuffer sb = new StringBuffer();
        for (int r = row1; r <= row2; r++) {
            for (int c = col1; c <= col2; c++) {
                sb.append(toCssKey(c, r));
                if (!(r == row2 && c == col2)) {
                    sb.append(",");
                }
            }
        }
        addSelector(cellRangeStyle, sb.toString(), 0);
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
            replaceSelector(cellRangeStyle,
                    newSelector.substring(0, newSelector.length() - 1), 0);
        } else {
            replaceSelector(cellRangeStyle, ".notusedselector", 0);
        }
    }

    public void addRowHeaderAsSelected(int row) {
        final String selector = ".rh.row" + row;
        addSelector(cellRangeStyle, selector, 1);
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
            replaceSelector(cellRangeStyle, sb.toString(), 1);
        } else { // should be impossible though
            replaceSelector(cellRangeStyle, ".notusedselector", 1);
        }
    }

    public void addColumnHeaderAsSelected(int col) {
        final String selector = ".ch.col" + col;
        addSelector(cellRangeStyle, selector, 1);
    }

    /**
     * Removes the given interval from selected (highlighted background).
     * 
     * @param col1
     * @param col2
     * @param row1
     * @param row2
     */
    public void removeFromSelectedCells(int col1, int col2, int row1, int row2) {
        String selector = getSelector(cellRangeStyle, 0);
        for (int r = row1; r <= row2; r++) {
            for (int c = col1; c <= col2; c++) {
                final String key = toCssKey(c, r);
                if (selector.endsWith(key)) {
                    selector = selector.substring(0,
                            selector.length() - key.length());
                } else {
                    selector = selector.replace(key + ",", "");
                }
            }
        }
        selector = selector.trim();
        if (selector.isEmpty() || selector.length() < 3) { // (",,")
            selector = ".notusedselector";
            replaceSelector(cellRangeStyle, selector, 0);
        } else if (selector.endsWith(",")) {
            replaceSelector(cellRangeStyle,
                    selector.substring(0, selector.lastIndexOf(",")), 0);
        } else {
            replaceSelector(cellRangeStyle, selector, 0);
        }
    }

    public void removeRowHeaderFromSelected(int row) {
        final String headerSelector = ".rh.row" + row;
        String oldSelector = getSelector(cellRangeStyle, 1);
        final String selector = oldSelector.endsWith(headerSelector) ? oldSelector
                .substring(0, oldSelector.length() - headerSelector.length())
                .trim() : oldSelector.replace(headerSelector + ",", "").trim();
        if (selector.endsWith(",")) {
            replaceSelector(cellRangeStyle,
                    selector.substring(0, selector.lastIndexOf(",")), 1);
        } else {
            replaceSelector(cellRangeStyle, selector, 1);
        }
    }

    public void removeColHeaderFromSelected(int col) {
        final String headerSelector = ".ch.col" + col;
        String oldSelector = getSelector(cellRangeStyle, 1);
        final String selector = oldSelector.endsWith(headerSelector) ? oldSelector
                .substring(0, oldSelector.length() - headerSelector.length())
                .trim() : oldSelector.replace(headerSelector + ",", "").trim();
        if (selector.endsWith(",")) {
            replaceSelector(cellRangeStyle,
                    selector.substring(0, selector.lastIndexOf(",")), 1);
        } else {
            replaceSelector(cellRangeStyle, selector, 1);
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
    public int getLeftColumnIndex() {
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

    public int getRightColumnIndex() {
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
    public int getTopRowIndex() {
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

    public int getBottomRowIndex() {
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
        replaceSelector(editedCellStyle, ".notusedselector", 0);
        this.customEditorWidget = customEditorWidget;
        Cell selectedCell = getSelectedCell();
        selectedCell.setValue(null, null);

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
                getSelectedCell().setValue(getSelectedCellLatestValue(), null);
            }
            customEditorWidget = null;
        }
    }

    private Cell getSelectedCell() {
        String selectedCellKey = getSelectedCellKey();
        if (isMergedCell(selectedCellKey)) {
            return getMergedCell(selectedCellKey);
        } else {
            return rows.get(selectedCellRow - firstRowIndex).get(
                    selectedCellCol - firstColumnIndex);
        }
    }

    public void startEditingCell(boolean focus, boolean recalculate,
            String value) {
        editingCell = true;
        replaceSelector(
                editedCellStyle,
                EDITING_CELL_SELECTOR
                        + toCssKey(selectedCellCol, selectedCellRow), 0);

        input.setClassName(toKey(selectedCellCol, selectedCellRow));
        if (isMergedCell(toKey(selectedCellCol, selectedCellRow))) {
            editingMergedCell = true;
            input.getStyle().setProperty(
                    "height",
                    getMergedCell(toKey(selectedCellCol, selectedCellRow))
                            .getElement().getStyle().getHeight());
        }

        if (recalculate) {
            handleInputElementValueChange(false);
        }
        if (focus) {
            if (isWebkit) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    @Override
                    public void execute() {
                        input.focus();
                    }
                });
            } else {
                input.focus();
            }
        }
        if (!isIE) { // ie would get the first letter twice, but WHY???
            input.setValue(value);
        }
    }

    public void updateSelectedCellValue(String value) {
        if (isSelectedCellInView()) {
            rows.get(selectedCellRow - firstRowIndex)
                    .get(selectedCellCol - firstColumnIndex)
                    .setValue(value, null);
        }
        cellData.put(toKey(getSelectedCellColumn(), getSelectedCellRow()),
                value);
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
        inputClicked = false;
        replaceSelector(editedCellStyle, ".notusedselector", 0);
        final String className = input.getClassName();
        input.setValue("");
        input.getStyle().setWidth(0.0d, Unit.PX);
        input.getStyle().clearHeight();
        if (className != null && !className.isEmpty()) {
            input.removeClassName(className);
        }
        focusSheet();
    }

    public void focusSheet() {
        // sheet.focus();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                sheet.focus();
            }
        });
    }

    public boolean isSelectedCellInView() {
        return selectedCellCol >= getLeftColumnIndex()
                && selectedCellCol <= getRightColumnIndex()
                && selectedCellRow >= getTopRowIndex()
                && selectedCellRow <= getBottomRowIndex();
    }

    public boolean isSelectionAreaInView() {
        return isAreaInView(selectionWidget.getCol1(),
                selectionWidget.getCol2(), selectionWidget.getRow1(),
                selectionWidget.getRow2());
    }

    public boolean isCellInView(int col, int row) {
        return (col >= getLeftColumnIndex() && col <= getRightColumnIndex()
                && row >= getTopRowIndex() && row <= getBottomRowIndex());
    }

    public boolean isAreaInView(int col1, int col2, int row1, int row2) {
        return isCellInView(col1, row1) && isCellInView(col1, row2)
                && isCellInView(col2, row1) && isCellInView(col2, row2);
    }

    public void scrollSelectedCellIntoView() {
        scrollCellIntoView(selectedCellCol, selectedCellRow);
    }

    /**
     * Scrolls the sheet to show the given cell, then triggers escalator for
     * updating cells if necessary.
     * 
     * This method does the {@link #isCellInView(int, int)} in itself, so no
     * need to do the check before calling this. Nothing is done if the cell is
     * already visible.
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
        final int leftColumnIndex = getLeftColumnIndex();
        if (col < leftColumnIndex) {
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
            final int rightColumnIndex = getRightColumnIndex();
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
        final int topRowIndex = getTopRowIndex();
        if (row < topRowIndex) {
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
            final int bottomRowIndex = getBottomRowIndex();
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
            updateSheetDisplay();
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
        // vertical:
        final int leftColumnIndex = getLeftColumnIndex();
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
            final int rightColumnIndex = getRightColumnIndex();
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

        // horizontal:
        final int topRowIndex = getTopRowIndex();
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
            final int bottomRowIndex = getBottomRowIndex();
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
            updateSheetDisplay();
            moveHeadersToMatchScroll();
        }
    }

    public void clearCellsData(int col1, int col2, int row1, int row2) {
        for (int i = row1; i <= row2; i++) {
            for (int j = col1; j <= col2; j++) {
                String key = toKey(j, i);
                cellData.remove(key);
                numericCellData.remove(key);
            }
            if (i >= firstRowIndex && i <= lastRowIndex) {
                ArrayList<Cell> row = rows.get(i - firstRowIndex);
                for (int j = col1; j <= col2; j++) {
                    Cell cell = row.get(j - firstColumnIndex);
                    cell.setValue("", null);
                }
            }
        }
    }

    /**
     * builds a CSS rule for each cell style from its associated CSS selector
     * and CSS style
     * 
     * @param cellStyleToCSSSelector
     */
    public void updateCellStyleCSSRules(
            HashMap<Integer, String> cellStyleToCSSSelector) {
        Map<Integer, String> cellStyles = actionHandler
                .getCellStyleToCSSStyle();
        for (Entry<Integer, String> entry : cellStyles.entrySet()) {
            Integer key = entry.getKey();
            if (key == 0) { // the default style
                continue;
            }
            String selector = cellStyleToCSSSelector.get(key);
            if (selector == null || selector.isEmpty()) {
                selector = ".notusedselector";
            } else {
                selector = createCellStyleSelector(selector);
            }
            if (cellStyleToCSSRuleIndex.containsKey(key)) {
                int ruleIndex = cellStyleToCSSRuleIndex.get(key);
                try {
                    replaceSelector(sheetStyle, selector, ruleIndex);
                } catch (Exception e) {
                    ruleIndex = insertRule(sheetStyle,
                            selector + "{" + entry.getValue() + "}");
                    cellStyleToCSSRuleIndex.put(key, ruleIndex);
                }
            } else {
                final String style = entry.getValue();
                if (style != null) {
                    int ruleIndex = insertRule(sheetStyle, selector + "{"
                            + style + "}");
                    cellStyleToCSSRuleIndex.put(key, ruleIndex);
                }
            }
        }
    }

    public void addCustomCellStyles(List<String> styles) {
        for (String style : styles) {
            try {
                insertRule(customCellSizeStyle,
                        style.replace(".col", ".v-spreadsheet .sheet div.col"));
            } catch (Exception e) {
                debugConsole.log(
                        Level.SEVERE,
                        "Invalid custom cell border style: " + style + ", "
                                + e.getMessage());
            }
        }
    }

    public void removeCustomCellStyles() {
        clearCSSRules(customCellSizeStyle);
    }

    public final String createCellStyleSelector(String selectors) {
        return selectors.replace(".col", ".v-spreadsheet .sheet div.col")
                + ", "
                + selectors
                        .replace(".col", ".v-spreadsheet .sheet > input.col");
    }

    public final String createCellCssText(String selectors, String style) {
        return (selectors.substring(0, selectors.length() - 1).replace(".col",
                ".v-spreadsheet .sheet div.col")) + "{" + style + "}";
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
        sheetPopupButtons.put(toKey(col, row), popupButton);
        if (col >= firstColumnIndex && col <= lastColumnIndex
                && row >= firstRowIndex && row <= lastRowIndex) {
            Cell cell = rows.get(row - firstRowIndex).get(
                    col - firstColumnIndex);
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
            rows.get(row - firstRowIndex).get(col - firstColumnIndex)
                    .removePopupButton();
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
        if (newCol >= firstColumnIndex && newCol <= lastColumnIndex
                && newRow >= firstRowIndex && newRow <= lastRowIndex) {
            Cell cell = rows.get(newRow - firstRowIndex).get(
                    newCol - firstColumnIndex);
            if (parent != null) {
                if (equals(parent)) {
                    if (oldCol >= firstColumnIndex && oldCol <= lastColumnIndex
                            && oldRow >= firstRowIndex
                            && oldRow <= lastRowIndex) {
                        rows.get(oldRow - firstRowIndex)
                                .get(oldCol - firstColumnIndex)
                                .removePopupButton();
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

    @Override
    public Iterator<Widget> iterator() {
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
            debugConsole.log(Level.SEVERE,
                    "Error while removing child widget from SheetWidget, child:"
                            + child.toString() + ", error: " + e.toString());
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
    }

    public void setDisplayRowColHeadings(boolean displayRowColHeadings) {
        this.displayRowColHeadings = displayRowColHeadings;

        if (displayRowColHeadings) {
            spreadsheet.removeClassName(NO_ROWCOLHEADINGS_CLASSNAME);
        } else {
            spreadsheet.addClassName(NO_ROWCOLHEADINGS_CLASSNAME);
        }

        moveHeadersToMatchScroll();
    }

}
