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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.vaadin.addon.spreadsheet.client.SheetWidget.CellCoord;

public class FormulaBarWidget extends Composite {

    private static final List<String> formulaColors;
    private static final String BACKGROUND_OPACITY = "0.25";
    private static final String BORDER_OPACITY = "0.75";
    private static final String BORDER_BASE = "2px solid ";

    static {

        formulaColors = new ArrayList<String>();

        formulaColors.add("rgba(48, 144, 240, %s)");
        formulaColors.add("rgba(236, 100, 100, %s)");
        formulaColors.add("rgba(152, 223, 88, %s)");
        formulaColors.add("rgba(249, 221, 81, %s)");
        formulaColors.add("rgba(36, 220, 212, %s)");
        formulaColors.add("rgba(236, 100, 165, %s)");
        formulaColors.add("rgba(104, 92, 176, %s)");
        formulaColors.add("rgba(255, 125, 66, %s)");
        formulaColors.add("rgba(51, 97, 144, %s)");
        formulaColors.add("rgba(170, 81, 77, %s)");
        formulaColors.add("rgba(127, 176, 83, %s)");
        formulaColors.add("rgba(187, 168, 91, %s)");
        formulaColors.add("rgba(36, 121, 129, %s)");
        formulaColors.add("rgba(150, 57, 112, %s)");
        formulaColors.add("rgba(75, 86, 168, %s)");
        formulaColors.add("rgba(154, 89, 61, %s)");

    }

    private final TextBox formulaField;
    private final ListBox namedRangeBox;
    private final HTML namedRangeBoxArrow;
    private final TextBox addressField;
    private final Element formulaOverlay = DOM.createDiv();

    private String cachedAddressFieldValue;
    private String cachedFunctionFieldValue;

    private final FormulaBarHandler handler;

    // editing control
    private boolean editingFormula;
    private boolean enableKeyboardNavigation;
    private TextBox currentEditor;
    private boolean inlineEdit;

    /**
     * Input is in full focus when it has been double-clicked or in 'edit' mode.
     */
    private boolean inFullFocus;

    private SheetInputEventListener sheetInputEventListener;

    /**
     * Caret position where the formula starts
     */
    private int formulaStartPos = -1;

    /**
     * Last known position of the caret
     */
    private int formulaLastKnownPos = -1;

    /**
     * the index that moves on keypress
     */
    private int formulaKeyboardSelectionEndCol = -1;
    /**
     * the index that moves on keypress
     */
    private int formulaKeyboardSelectionEndRow = -1;

    /**
     * the index that anchors shift-selected ranges
     */
    private int formulaKeyboardSelectionStartCol = -1;
    /**
     * the index that anchors shift-selected ranges
     */
    private int formulaKeyboardSelectionStartRow = -1;

    private List<MergedRegion> formulaCellReferences = new ArrayList<MergedRegion>();
    /**
     * Holder for cells that are being selected for a formula.
     */
    private HashSet<Cell> paintedFormulaCells = new HashSet<Cell>();

    private Map<CellCoord, String> paintedFormulaCellCoords = new HashMap<CellCoord, String>();
    private Map<String, String> refColors = new HashMap<String, String>();
    private String lastCaretReference = null;

    /**
     * SheetWidget owns this widget.
     */
    private TextBox inlineEditor;

    private SheetWidget widget;
    private RegExp cachedRegex;

    public FormulaBarWidget(FormulaBarHandler selectionManager,
            SheetWidget widget) {
        handler = selectionManager;
        this.widget = widget;

        inlineEditor = widget.getInlineEditor();

        sheetInputEventListener = GWT.create(SheetInputEventListener.class);
        sheetInputEventListener.setSheetWidget(widget, this);

        formulaField = new TextBox();
        formulaField.setTabIndex(2);
        addressField = new TextBox();
        addressField.setTabIndex(1);
        formulaField.setStyleName("functionfield");
        addressField.setStyleName("addressfield");

        namedRangeBox = new ListBox();
        namedRangeBox.setStyleName("namedrangebox");
        namedRangeBox.setMultipleSelect(false);
        namedRangeBox.addItem("");

        namedRangeBoxArrow = new HTML("▼");
        namedRangeBoxArrow.setStyleName("arrow");

        setNamedRangeBoxVisible(false);

        FlowPanel panel = new FlowPanel();
        FlowPanel left = new FlowPanel();
        FlowPanel right = new FlowPanel();
        left.setStyleName("fixed-left-panel");
        right.setStyleName("adjusting-right-panel");
        left.add(addressField);
        left.add(namedRangeBoxArrow);
        left.add(namedRangeBox);
        right.add(formulaField);
        panel.add(left);
        panel.add(right);

        initWidget(panel);

        setStyleName("functionbar");

        initListeners();

        formulaOverlay.setClassName("formulaoverlay");
        getElement().appendChild(formulaOverlay);
    }

    private void trySelectNamedRangeBoxValue(String value) {
        final int size = namedRangeBox.getItemCount();

        for (int i = 0; i < size; i++) {
            if (namedRangeBox.getItemText(i).equals(value)) {
                namedRangeBox.setSelectedIndex(i);
                return;
            }
        }

        namedRangeBox.setSelectedIndex(0);
    }

    /**
     * Removes all keyboard selection variables, clears paint
     */
    public void clearFormulaSelection() {
        formulaKeyboardSelectionEndCol = -1;
        formulaKeyboardSelectionEndRow = -1;
        formulaKeyboardSelectionStartCol = -1;
        formulaKeyboardSelectionStartRow = -1;
        clearFormulaSelectedCells();
    }

    public void moveFormulaCellSelection(boolean shiftPressed, boolean up,
            boolean right, boolean down) {

        if (!isEditingFormula()) {
            return;
        }

        // starting point, use old if available
        if (formulaKeyboardSelectionEndCol == -1) {
            formulaKeyboardSelectionEndCol = widget.getSelectedCellColumn();
            formulaKeyboardSelectionEndRow = widget.getSelectedCellRow();
        }

        if (up) {
            formulaKeyboardSelectionEndRow--;
        } else if (right) {
            formulaKeyboardSelectionEndCol++;
        } else if (down) {
            formulaKeyboardSelectionEndRow++;
        } else {
            formulaKeyboardSelectionEndCol--;
        }

        // sheet bounds
        if (formulaKeyboardSelectionEndRow == 0) {
            formulaKeyboardSelectionEndRow = 1;
        }
        if (formulaKeyboardSelectionEndCol == 0) {
            formulaKeyboardSelectionEndCol = 1;
        }
        int[] range = widget.getSheetDisplayRange();
        if (formulaKeyboardSelectionEndRow > range[2] - 1) {
            formulaKeyboardSelectionEndRow = range[2] - 1;
        }
        if (formulaKeyboardSelectionEndCol > range[3] - 1) {
            formulaKeyboardSelectionEndCol = range[3] - 1;
        }

        // check for single or range selection
        if (shiftPressed && formulaKeyboardSelectionStartCol != -1) {
            // keep start, unless its empty
        } else {
            formulaKeyboardSelectionStartCol = formulaKeyboardSelectionEndCol;
            formulaKeyboardSelectionStartRow = formulaKeyboardSelectionEndRow;
        }

        setFormulaCellRange(formulaKeyboardSelectionStartCol,
                formulaKeyboardSelectionStartRow,
                formulaKeyboardSelectionEndCol, formulaKeyboardSelectionEndRow);

        // make sure current selection is visible
        widget.scrollCellIntoView(formulaKeyboardSelectionEndCol,
                formulaKeyboardSelectionEndRow);
    }

    private void initListeners() {
        Event.sinkEvents(namedRangeBox.getElement(), Event.ONCHANGE);
        Event.setEventListener(namedRangeBox.getElement(), new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                addressField.setValue(namedRangeBox.getSelectedValue());
                submitAddressValue();
            }
        });

        Event.sinkEvents(addressField.getElement(),
                Event.ONKEYUP | Event.FOCUSEVENTS);
        Event.setEventListener(addressField.getElement(), new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                final int type = event.getTypeInt();
                if (type == Event.ONKEYUP) {
                    final int keyCode = event.getKeyCode();
                    if (keyCode == KeyCodes.KEY_ENTER) {
                        // submit address value
                        submitAddressValue();
                        trySelectNamedRangeBoxValue(addressField.getValue());
                        addressField.setFocus(false);
                    } else if (keyCode == KeyCodes.KEY_ESCAPE) {
                        revertCellAddressValue();
                        handler.onAddressFieldEsc();
                    }
                } else if (type == Event.ONFOCUS) {
                    handler.setSheetFocused(true);
                    addressField.getElement().getStyle()
                            .setTextAlign(TextAlign.LEFT);
                } else {
                    handler.setSheetFocused(false);
                    addressField.getElement().getStyle().clearTextAlign();
                }
            }
        });
        Event.sinkEvents(formulaField.getElement(),
                Event.KEYEVENTS | Event.FOCUSEVENTS | Event.ONMOUSEUP);
        Event.setEventListener(formulaField.getElement(), new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {
                switch (event.getTypeInt()) {
                case Event.ONFOCUS:

                    // if we move focus from inline editor to here, swap the
                    // editor
                    if (editingFormula && currentEditor == inlineEditor) {
                        editingFormula = false;
                        checkFormulaEdit(formulaField);

                    } else {
                        handler.setSheetFocused(true);
                        cachedFunctionFieldValue = formulaField.getValue();
                        handler.onFormulaFieldFocus(cachedFunctionFieldValue);
                        checkFormulaEdit(formulaField);
                    }
                    break;
                case Event.ONBLUR:

                    // temporary blur (cell selection)?
                    if (!editingFormula) {
                        handler.setSheetFocused(false);
                        handler.onFormulaFieldBlur(formulaField.getValue());
                    }
                    break;
                case Event.ONKEYDOWN:
                    handleFunctionFieldKeyDown(event);
                    break;
                case Event.ONPASTE:
                case Event.ONKEYPRESS:

                    checkKeyboardNavigation();
                    updateEditorCaretPos(true);

                    scheduleFormulaValueUpdate();

                    break;
                case Event.ONMOUSEUP:
                    if (editingFormula) {
                        updateEditorCaretPos(true);
                    }
                default:
                    break;
                }
            }

        });
    }

    private void submitAddressValue() {
        handler.onAddressEntered(addressField.getValue().replaceAll(" ", ""));
    }

    /**
     * Checks the char before the current caret pos, and enables or disables
     * keyboard selection of cells accordingly.
     */
    public void checkKeyboardNavigation() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {

                if (!isEditingFormula()) {
                    return;
                }

                String value = currentEditor.getValue();
                int cursorPos = currentEditor.getCursorPos();
                char c = cursorPos > 0 ? value.charAt(cursorPos - 1) : 0;

                enableKeyboardNavigation = false;

                if (c == '(' || c == '+' || c == '-' || c == '/' || c == '*') {
                    enableKeyboardNavigation = true;

                } else if (c == '=') {
                    // better UX: check if user forgot to add '=' and now adds
                    // it to the beginning of the val
                    if (value.length() == 1) {
                        enableKeyboardNavigation = true;
                    }
                }
            }
        });
    }

    /**
     * Adds a selection range to the current formula
     */
    public void addFormulaCellRange(int col1, int row1, int col2, int row2) {
        setFormulaCellRange(col1, row1, col2, row2, true);
    }

    /**
     * Set a cell range in the formula. If the user hasn't moved the caret after
     * last call, replace the value instead.
     */
    public void setFormulaCellRange(int col1, int row1, int col2, int row2) {
        setFormulaCellRange(col1, row1, col2, row2, false);
    }

    /**
     * Set a cell range in the formula. If the user hasn't moved the caret after
     * last call, replace the value instead.
     */
    private void setFormulaCellRange(int col1, int row1, int col2, int row2,
            boolean add) {

        String cellRange;
        if (col1 == col2 && row1 == row2) {
            cellRange = handler.createCellAddress(col1, row1);
        } else {

            // swap so that smaller indexes are first
            int temp;
            if (col1 > col2) {
                temp = col1;
                col1 = col2;
                col2 = temp;
            }
            if (row1 > row2) {
                temp = row1;
                row1 = row2;
                row2 = temp;
            }

            cellRange = handler.createCellAddress(col1, row1) + ":"
                    + handler.createCellAddress(col2, row2);
        }

        if (add && formulaStartPos >= 0) {

            // POI always uses comma, http://dev.vaadin.com/ticket/17223
            cellRange = "," + cellRange;
            formulaLastKnownPos++;
        }

        final int startPos;
        int endPos;

        int selectionLength = currentEditor.getSelectionLength();
        boolean rangeSelected = selectionLength > 0;

        if (rangeSelected) {
            // replace whatever was selected
            startPos = currentEditor.getCursorPos();
            endPos = startPos + selectionLength;

            formulaStartPos = startPos;
            formulaLastKnownPos = endPos;
        } else if (add || formulaStartPos < 0) {
            // not editing cell range, or ctrl key pressed. Insert.
            startPos = formulaLastKnownPos;
            endPos = formulaLastKnownPos;

            formulaStartPos = formulaLastKnownPos;
        } else {
            // currently editing cell range, replace old reference
            startPos = formulaStartPos;
            endPos = formulaLastKnownPos;
        }

        String val = currentEditor.getValue();
        String sub1 = val.substring(0, startPos);
        String sub2 = val.substring(endPos, val.length());
        val = sub1 + cellRange + sub2;

        formulaLastKnownPos = (sub1 + cellRange).length();

        currentEditor.setValue(val);

        // synchronize to other editor too
        if (currentEditor == inlineEditor) {
            formulaField.setValue(val);
        }

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                currentEditor.setFocus(true);

                // not GWT attached, use JSNI
                setSelectionRange(currentEditor.getElement(),
                        formulaLastKnownPos, 0);

                parseAndPaintCellRefs(currentEditor.getValue());
                checkForCoordsAtCaret();
            }
        });

    }

    private native void setSelectionRange(Element elem, int pos, int length)
    /*-{
        try {
          elem.setSelectionRange(pos, pos + length);
        } catch (e) {
          // Firefox throws exception if TextBox is not visible, even if attached
        }
    }-*/;

    private void scheduleFormulaValueUpdate() {

        if (handler.isTouchMode()) {

            /*
             * Can't be done deferred because that is apparently too fast in
             * iPads, resulting in textfield not having new value when this is
             * run. A timer makes sure hat the textfield actually has the
             * entered character.
             */
            new Timer() {

                @Override
                public void run() {
                    handler.onFormulaValueChange(formulaField.getValue());
                }
            }.schedule(100);

        } else {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    handler.onFormulaValueChange(formulaField.getValue());
                }
            });
        }
    }

    public void checkEmptyValue() {

        // if value is empty, stop editing formula
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                if (currentEditor != null
                        && currentEditor.getValue().isEmpty()) {

                    if (currentEditor == inlineEditor) {
                        stopInlineEdit();
                    } else {
                        stopEditing();
                    }
                }
            }
        });
    }

    private void handleFunctionFieldKeyDown(Event event) {
        switch (event.getKeyCode()) {
        case KeyCodes.KEY_BACKSPACE:
        case KeyCodes.KEY_DELETE:
            scheduleFormulaValueUpdate();
            checkEmptyValue();

            break;
        case KeyCodes.KEY_ESCAPE:
            formulaField.setValue(cachedFunctionFieldValue);
            handler.onFormulaEsc();
            stopEditing();
            event.stopPropagation();
            event.preventDefault();
            break;
        case KeyCodes.KEY_ENTER:
            handler.onFormulaEnter(formulaField.getValue());
            stopEditing();
            event.stopPropagation();
            event.preventDefault();
            break;
        case KeyCodes.KEY_TAB:
            handler.onFormulaTab(formulaField.getValue(), !event.getShiftKey());
            stopEditing();
            event.stopPropagation();
            break;

        case KeyCodes.KEY_UP:
            if (enableKeyboardNavigation) {
                moveFormulaCellSelection(event.getShiftKey(), true, false,
                        false);
                event.preventDefault();
            }
            break;
        case KeyCodes.KEY_RIGHT:
            if (enableKeyboardNavigation) {
                moveFormulaCellSelection(event.getShiftKey(), false, true,
                        false);
                event.preventDefault();
            }
            break;
        case KeyCodes.KEY_DOWN:
            if (enableKeyboardNavigation) {
                moveFormulaCellSelection(event.getShiftKey(), false, false,
                        true);
                event.preventDefault();
            }
            break;
        case KeyCodes.KEY_LEFT:
            if (enableKeyboardNavigation) {
                moveFormulaCellSelection(event.getShiftKey(), false, false,
                        false);
                event.preventDefault();
            }
            break;

        default:
            checkFormulaEdit(formulaField);
            break;
        }

        if (currentEditor != null) {
            updateEditorCaretPos(false);
            updateFormulaSelectionStyles();
        }
    }

    private void stopEditing() {
        editingFormula = false;
        currentEditor = null;
        formulaLastKnownPos = -1;
        formulaStartPos = -1;
        clearFormulaSelection();
    }

    private void checkFormulaEdit(final TextBox editor) {

        // give text box time to fill value
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {

                if (!editingFormula) {

                    String val = editor.getValue();
                    if (val.startsWith("=") || val.startsWith("+")) {
                        editingFormula = true;
                        currentEditor = editor;

                        parseAndPaintCellRefs(val);
                        checkForCoordsAtCaret();
                    }
                }
            }
        });
    }

    /**
     * Parses formula to see if the caret has landed on a cell reference. Sets
     * {@link #formulaLastKnownPos} and {@link #formulaStartPos} if it has.
     */
    private void checkForCoordsAtCaret() {

        String val = currentEditor.getValue();

        // count unescaped quote chars to see if caret is inside string literal
        int caretPos = currentEditor.getCursorPos();
        // scan backward
        int numQuotes = 0;
        while (--caretPos > 0) {
            if (val.charAt(caretPos) == '"') {
                if (caretPos == 0 || val.charAt(caretPos - 1) != '\\') {
                    numQuotes++;
                }
            }
        }

        if (numQuotes % 2 == 1) {
            // caret is inside a quoted string, no cell refs here.
            return;
        }

        // Find next not-matching char before and after caret, and try to match
        // section inbetween.

        int start = -1, end = -1;

        caretPos = currentEditor.getCursorPos();
        // scan back
        while (caretPos > 0) {
            char c = val.charAt(caretPos - 1);
            if (String.valueOf(c).matches("[^A-z0-9:!]")) {
                start = caretPos;
                break;
            }
            caretPos--;
        }

        caretPos = currentEditor.getCursorPos();
        // scan forward
        while (caretPos < val.length()) {
            char c = val.charAt(caretPos);
            if (String.valueOf(c).matches("[^A-z0-9:!]")) {
                end = caretPos;
                break;
            }
            caretPos++;
        }

        String sub = val.substring(start, end);
        clearPreviousCaretRefBorder();
        if (isCellRef(sub)) {
            formulaStartPos = start;
            formulaLastKnownPos = end;
            updateCaretRefBorder(sub);
        }

    }

    private void clearPreviousCaretRefBorder() {
        if (lastCaretReference != null) {
            MergedRegion region = parseSingleCellRef(lastCaretReference);
            if (region == null) {
                return;
            }
            int colStart = Math.min(region.col1, region.col2);
            int colEnd = Math.max(region.col1, region.col2);
            int rowStart = Math.min(region.row1, region.row2);
            int rowEnd = Math.max(region.row1, region.row2);
            for (int c = colStart; c <= colEnd; c++) {
                for (int r = rowStart; r <= rowEnd; r++) {
                    Cell cell = widget.getCell(c, r);
                    if (cell != null) {
                        cell.getElement().getStyle().clearProperty("border");
                    }
                }
            }
        }
        lastCaretReference = null;
    }

    private void updateCaretRefBorder(String ref) {
        if (refColors.containsKey(ref)) {
            MergedRegion region = parseSingleCellRef(ref);
            if (region == null) {
                return;
            }
            int colStart = Math.min(region.col1, region.col2);
            int colEnd = Math.max(region.col1, region.col2);
            int rowStart = Math.min(region.row1, region.row2);
            int rowEnd = Math.max(region.row1, region.row2);

            if (colEnd > 20000) {
                Logger.getLogger(getClass().getSimpleName())
                        .fine("invalid column index, halting parse");
                return;
            }

            for (int c = colStart; c <= colEnd; c++) {
                for (int r = rowStart; r <= rowEnd; r++) {
                    Cell cell = widget.getCell(c, r);
                    if (cell != null) {
                        DivElement elem = cell.getElement();
                        String color = refColors.get(ref).replace("%s",
                                BORDER_OPACITY);

                        if (c == colStart) {
                            elem.getStyle().setProperty("borderLeft",
                                    BORDER_BASE + color);
                        }
                        if (c == colEnd) {
                            elem.getStyle().setProperty("borderRight",
                                    BORDER_BASE + color);
                        }
                        if (r == rowStart) {
                            elem.getStyle().setProperty("borderTop",
                                    BORDER_BASE + color);
                        }
                        if (r == rowEnd) {
                            elem.getStyle().setProperty("borderBottom",
                                    BORDER_BASE + color);
                        }

                    }
                }
            }
            lastCaretReference = ref;
        }
    }

    private MergedRegion parseSingleCellRef(String ref) {
        MergedRegion range = new MergedRegion();
        if (ref.contains("!")) {
            // Sheet ref; only parse if on this sheet
            String sheetname = ref.split("!")[0];

            if (handler.getActiveSheetName().equals(sheetname)) {
                return parseSingleCellRef(ref.split("!")[1]);
            } else {
                return null;
            }

        } else if (ref.contains(":")) {
            String[] refs = ref.split(":");
            CellCoord c1 = parseSingleCell(refs[0]);
            range.col1 = c1.getCol();
            range.row1 = c1.getRow();
            CellCoord c2 = parseSingleCell(refs[1]);
            range.col2 = c2.getCol();
            range.row2 = c2.getRow();
        } else {
            CellCoord cc = parseSingleCell(ref);
            range.col1 = cc.getCol();
            range.row1 = cc.getRow();
            range.col2 = range.col1;
            range.row2 = range.row1;
        }

        return range;
    }

    /**
     * Parse formula for cell references, and paint each with a unique color.
     */
    private void parseAndPaintCellRefs(String val) {

        // clear old paint, but leave keyboard indexes as is
        clearFormulaSelectedCells();

        List<String> references = parseCellReferences(val);
        refColors.clear();
        int currentIndex = 0;
        int currentColor = 0;
        for (String ref : references) {

            MergedRegion range = parseSingleCellRef(ref);

            if (range == null) {
                // couldn't parse, probably sheet ref
                continue;
            }

            String color;
            // color should be reused if reference is for same cell or region
            if (refColors.containsKey(ref)) {
                color = refColors.get(ref);
            } else {
                currentColor = currentColor % formulaColors.size();
                color = formulaColors.get(currentColor);
                refColors.put(ref, color);
                currentColor++;
            }

            // Set the opacity
            color = color.replace("%s", BACKGROUND_OPACITY);
            // paint sheet cells
            paintFormulaSelectedCells(range, color);

            // Add paint on top of formula field with overlay spans.
            int i = val.indexOf(ref, currentIndex);
            Element e = DOM.createSpan();
            String text = val.substring(currentIndex, i);
            text = text.replaceAll(" ", "&nbsp;");
            e.setInnerHTML(text);
            formulaOverlay.appendChild(e);

            currentIndex = i + ref.length();

            e = DOM.createSpan();
            e.setInnerText(ref);
            e.getStyle().setBackgroundColor(color);
            formulaOverlay.appendChild(e);

        }

    }

    /**
     * Parses single
     *
     * @param cellRef
     * @return
     */
    private static CellCoord parseSingleCell(String cellRef) {
        String c = "", r = "";
        if (cellRef != null) {
            String[] split1 = cellRef.toUpperCase().split("[0-9]");
            String[] split = cellRef.split("[A-z]");
            if (split1.length > 0) {
                c = split1[0];
            }
            if (split.length > 0) {
                r = split[split.length - 1];
            }
        }

        int row = r.length() > 0 ? Integer.valueOf(r) : 0;
        int col = 0;
        for (int i = 0; i < c.length(); i++) {
            char current = c.charAt(i);

            int charNum = 0;
            if (current >= 'A' && current <= 'Z') {
                charNum = (current - 64);
            } else if (current >= 'a' && current <= 'z') {
                charNum = (current - 96);
            }
            col = col * 26 + charNum;

        }

        return new CellCoord(col, row);

    }

    /**
     * Clears all selection paint from the sheet
     */
    private void clearFormulaSelectedCells() {
        for (Cell c : paintedFormulaCells) {
            c.getElement().getStyle().clearBackgroundColor();
            c.getElement().getStyle().clearProperty("border");
        }
        paintedFormulaCells.clear();
        formulaCellReferences.clear();
        paintedFormulaCellCoords.clear();

        formulaOverlay.removeAllChildren();
    }

    /**
     * Paints the given cell region with the given color.
     */
    private void paintFormulaSelectedCells(MergedRegion region, String color) {

        // we might drag upward too, so sort indexes

        int colStart, colEnd;
        colStart = Math.min(region.col1, region.col2);
        colEnd = Math.max(region.col1, region.col2);
        int rowStart, rowEnd;
        rowStart = Math.min(region.row1, region.row2);
        rowEnd = Math.max(region.row1, region.row2);

        if (colEnd > 20000) {
            Logger.getLogger(getClass().getSimpleName())
                    .fine("invalid column index, halting parse");
            return;
        }

        for (int c = colStart; c <= colEnd; c++) {
            for (int r = rowStart; r <= rowEnd; r++) {

                Cell cell = widget.getCell(c, r);
                if (cell != null) {
                    DivElement elem = cell.getElement();

                    elem.getStyle().setBackgroundColor(color);

                    paintedFormulaCells.add(cell);
                    paintedFormulaCellCoords.put(new CellCoord(c, r), color);
                }
            }

        }

        formulaCellReferences.add(region);
    }

    public void revertCellAddressValue() {
        addressField.setValue(cachedAddressFieldValue);
        addressField.setFocus(false);
    }

    public void revertCellValue() {
        formulaField.setValue(cachedFunctionFieldValue);
    }

    public void setSelectedCellAddress(String selection) {
        cachedAddressFieldValue = selection;
        addressField.setValue(selection);
        trySelectNamedRangeBoxValue(selection);
    }

    public void setCellPlainValue(String plainValue) {
        formulaField.setValue(plainValue);
    }

    public void setCellFormulaValue(String formula) {
        if (!formula.isEmpty()) {
            formulaField.setValue("=" + formula);
        } else {
            formulaField.setValue(formula);
        }
    }

    public void clear() {
        setCellPlainValue("");
        setSelectedCellAddress("");

        clearFormulaSelection();
    }

    public String getFormulaFieldValue() {
        return formulaField.getValue();
    }

    public void setFormulaFieldEnabled(boolean enabled) {
        formulaField.setEnabled(enabled);
    }

    public void cacheFormulaFieldValue() {
        cachedFunctionFieldValue = formulaField.getValue();
    }

    /**
     * If the user has focused an editor field, and the editor field contains a
     * formula.
     */
    public boolean isEditingFormula() {
        return editingFormula;
    }

    /**
     * If arrow key events should be processed normally, or as cell selection
     * for the formula.
     */
    public boolean isKeyboardNavigationEnabled() {
        return enableKeyboardNavigation && editingFormula;
    }

    public void startInlineEdit(boolean inputFullFocus) {

        setInlineEdit(inputFullFocus);
        checkFormulaEdit(inlineEditor);
        updateEditorCaretPos(true);
        checkKeyboardNavigation();
    }

    public void stopInlineEdit() {
        setInlineEdit(false);
        setInFullFocus(false);
        stopEditing();
    }

    /**
     * Stores the current position if the editor caret (e.g. when focus is
     * temporarily moved somewhere else)
     */
    public void updateEditorCaretPos(boolean doAsDeferred) {

        if (doAsDeferred) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {

                    if (isEditingFormula()) {

                        formulaStartPos = -1;
                        formulaLastKnownPos = currentEditor.getCursorPos();

                        checkForCoordsAtCaret();
                    }
                }
            });
        } else if (isEditingFormula()) {
            formulaLastKnownPos = currentEditor.getCursorPos();
            checkForCoordsAtCaret();
        }
    }

    /**
     * Parses all cell references from the given formula for painting. Ranges
     * (A1:A3) are returned as single string instead of two separate ones.
     */
    private List<String> parseCellReferences(String formula) {
        List<String> cells = new ArrayList<String>();

        for (String c : formula.split("[^A-z0-9:!]+")) {
            if (isCellRef(c)) {
                cells.add(c);
            }
        }

        return cells;
    }

    /**
     * Checks if given string is a cell reference.
     */
    private boolean isCellRef(String current) {

        RegExp regex = cachedRegex;

        if (regex == null) {
            String singleCellRef = "([A-Za-z]{1,3}[0-9]{1,7})";

            String sheetNames = "";
            for (String s : handler.getSheetNames()) {
                sheetNames += s + "|";
            }
            sheetNames = sheetNames.substring(0, sheetNames.length() - 1);

            // beginning of string + optional sheetname
            String regexp = "^((" + sheetNames + ")!){0,1}";
            // cell ref
            regexp += singleCellRef;
            // optional range
            regexp += "(:" + singleCellRef + "){0,1}";

            cachedRegex = regex = RegExp.compile(regexp);

            // forget after a while
            new Timer() {

                @Override
                public void run() {
                    cachedRegex = null;
                }

            }.schedule(2000);
        }

        return regex.test(current);
    }

    /**
     * Sheet has scrolled, and cell elements are not in the same position they
     * were. Re-paint background colors.
     */
    public void ensureSelectionStylesAfterScroll() {

        // clear cells that moved
        for (Cell c : paintedFormulaCells) {
            CellCoord cc = new CellCoord(c.getCol(), c.getRow());
            if (!paintedFormulaCellCoords.containsKey(cc)) {
                c.getElement().getStyle().clearBackgroundColor();
                c.getElement().getStyle().clearProperty("border");
            }
        }
        paintedFormulaCells.clear();
        if (editingFormula) {
            checkForCoordsAtCaret();
        }
        // re-paint all cells
        for (Entry<CellCoord, String> e : paintedFormulaCellCoords.entrySet()) {
            Cell c = widget.getCell(e.getKey().getCol(), e.getKey().getRow());
            if (c != null) {
                c.getElement().getStyle().setBackgroundColor(e.getValue());
                paintedFormulaCells.add(c);
            }
        }

    }

    /**
     * Re-parses and paints the cell references in the current formula.
     */
    public void updateFormulaSelectionStyles() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {

                if (!isEditingFormula()) {
                    return;
                }

                parseAndPaintCellRefs(currentEditor.getValue());
                checkForCoordsAtCaret();
            }
        });
    }

    public void setNamedRanges(List<String> namedRanges) {
        namedRangeBox.clear();
        namedRangeBox.addItem("");

        if (namedRanges != null && !namedRanges.isEmpty()) {
            setNamedRangeBoxVisible(true);

            for (String name : namedRanges) {
                namedRangeBox.addItem(name);
            }

            trySelectNamedRangeBoxValue(addressField.getValue());
        } else {
            setNamedRangeBoxVisible(false);
        }
    }

    private void setNamedRangeBoxVisible(boolean visible) {
        namedRangeBox.setVisible(visible);
        namedRangeBoxArrow.setVisible(visible);
    }

    public boolean isInFullFocus() {
        return inFullFocus;
    }

    public void setInFullFocus(boolean inFullFocus) {
        this.inFullFocus = inFullFocus;
    }

    public boolean isInlineEdit() {
        return inlineEdit;
    }

    public void setInlineEdit(boolean inlineEdit) {
        this.inlineEdit = inlineEdit;
    }

    public boolean hasLightFocus() {
        return !inlineEdit || (inlineEdit && !inFullFocus && !editingFormula);
    }
}
