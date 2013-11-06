package com.vaadin.addon.spreadsheet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.Format;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.converter.ExcelToHtmlUtils;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.record.cf.CellRangeUtil;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;

import com.vaadin.addon.spreadsheet.client.ImageInfo;
import com.vaadin.addon.spreadsheet.client.MergedRegion;
import com.vaadin.addon.spreadsheet.client.MergedRegionUtil;
import com.vaadin.addon.spreadsheet.client.MergedRegionUtil.MergedRegionContainer;
import com.vaadin.addon.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.addon.spreadsheet.client.SpreadsheetClientRpc;
import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;
import com.vaadin.addon.spreadsheet.client.SpreadsheetState;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.server.KeyMapper;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.util.ReflectTools;

public class Spreadsheet extends AbstractComponent implements HasComponents,
        Action.Container {

    /**
     * An interface for handling the edited cell value from user input.
     */
    public interface CellValueHandler {

        /**
         * Called if a cell value has been edited by the user by using the
         * default cell editor. Use
         * {@link Spreadsheet#setCellValueHandler(CellValueHandler)} to enable
         * it for the spreadsheet.
         * 
         * @param cell
         *            the cell that has been edited, may be <code>null</code> if
         *            the cell doesn't yet exists
         * @param sheet
         *            the sheet the cell belongs to, the currently active sheet
         * @param colIndex
         *            0-based cell column index
         * @param rowIndex
         *            0-based cell row index
         * @param newValue
         *            the value user has entered
         * @param formulaEvaluator
         *            the {@link FormulaEvaluator} for this sheet
         * @param formatter
         *            the {@link DataFormatter} for this workbook
         * @return <code>true</code> if component default parsing should still
         *         be done, <code>false</code> if not
         */
        public boolean cellValueUpdated(Cell cell, Sheet sheet, int colIndex,
                int rowIndex, String newValue,
                FormulaEvaluator formulaEvaluator, DataFormatter formatter);
    }

    /**
     * An interface for handling clicks on cells that contain a hyperlink.
     * <p>
     * Implement this interface and use
     * {@link Spreadsheet#setHyperlinkCellClickHandler(HyperlinkCellClickHandler)}
     * to enable it for the spreadsheet.
     */
    public interface HyperlinkCellClickHandler {

        /**
         * Called when a hyperlink cell has been clicked.
         * 
         * @param cell
         *            the cell that contains the hyperlink
         * @param hyperlink
         *            the actual hyperlink
         * @param spreadsheet
         *            the component
         */
        public void onHyperLinkCellClick(Cell cell, Hyperlink hyperlink,
                Spreadsheet spreadsheet);
    }

    private static final String numericCellDetectionPattern = "[^A-Za-z]*[0-9]+[^A-Za-z]*";
    private static final String rowShiftRegex = "[$]?[a-zA-Z]+[$]?\\d+";
    private static final Pattern rowShiftPattern = Pattern
            .compile(rowShiftRegex);
    private CellReference selectedCellReference;
    private CellRangeAddress paintedCellRange;
    private final ArrayList<CellRangeAddress> cellRangeAddresses;
    private final ArrayList<CellReference> individualSelectedCells;
    private SelectionChangeEvent latestSelectionEvent;

    private FormulaEvaluator evaluator;
    private DataFormatter formatter;
    private SpreadsheetStyleFactory styler;
    private CellValueHandler customCellValueHandler;
    private HyperlinkCellClickHandler hyperlinkCellClickHandler;
    private SpreadsheetComponentFactory customComponentFactory;
    private LinkedList<Handler> actionHandlers;
    private KeyMapper<Action> actionMapper;

    /** Cell keys that have values sent to client side and are cached there. */
    private final HashSet<String> sentCells;
    /**
     * Formula cell keys that have values sent to client side and are cached
     * there.
     */
    private final HashSet<String> sentFormulaCells;
    /** */
    private final HashSet<String> removedCells;
    /** */
    private final HashSet<String> markedCells;

    private int firstRow;
    private int lastRow;
    private int firstColumn;
    private int lastColumn;
    private int contextMenuHeaderIndex = -1;
    private short hyperlinkStyleIndex = -1;

    private int defaultNewSheetRows = SpreadsheetFactory.DEFAULT_ROWS;
    private int defaultNewSheetColumns = SpreadsheetFactory.DEFAULT_COLUMNS;

    protected int mergedRegionCounter;

    private Workbook workbook;

    /** true if the component sheet should be reloaded on client side. */
    private boolean reload;

    /** are tables for currently active sheet loaded */
    private boolean tablesLoaded;

    /** image sizes need to be recalculated on column/row resizing s */
    private boolean reloadImageSizesFromPOI;

    protected String initialSheetSelection = null;

    private HashSet<Component> customComponents;

    private HashSet<PopupButton> sheetPopupButtons;

    protected HashSet<SheetImageWrapper> sheetImages;

    private HashSet<SpreadsheetTable> tables;

    protected final MergedRegionContainer mergedRegionContainer = new MergedRegionContainer() {

        @Override
        public MergedRegion getMergedRegionStartingFrom(int column, int row) {
            List<MergedRegion> mergedRegions = getState(false).mergedRegions;
            if (mergedRegions != null) {
                for (MergedRegion region : mergedRegions) {
                    if (region.col1 == column && region.row1 == row) {
                        return region;
                    }
                }
            }
            return null;
        }

        @Override
        public MergedRegion getMergedRegion(int column, int row) {
            List<MergedRegion> mergedRegions = getState(false).mergedRegions;
            if (mergedRegions != null) {
                for (MergedRegion region : mergedRegions) {
                    if (region.col1 <= column && region.row1 <= row
                            && region.col2 >= column && region.row2 >= row) {
                        return region;
                    }
                }
            }
            return null;
        }
    };

    private final SpreadsheetServerRpc serverRPC = new SpreadsheetServerRpc() {

        @Override
        public void loadCellsData(int firstRow, int lastRow, int firstColumn,
                int lastColumn) {
            if (Spreadsheet.this.firstRow != firstRow
                    || Spreadsheet.this.lastRow != lastRow
                    || Spreadsheet.this.firstColumn != firstColumn
                    || Spreadsheet.this.lastColumn != lastColumn) {
                Spreadsheet.this.firstRow = firstRow;
                Spreadsheet.this.lastRow = lastRow;
                Spreadsheet.this.firstColumn = firstColumn;
                Spreadsheet.this.lastColumn = lastColumn;
                loadCells(firstRow, lastRow, firstColumn, lastColumn);
            }
            if (initialSheetSelection != null) {
                handleCellAddressChange(initialSheetSelection);
                initialSheetSelection = null;
            }
        }

        /* respond to client with the selected cells formula if any */
        @Override
        public void cellSelected(int column, int row,
                boolean discardOldRangeSelection) {
            CellReference cellReference = new CellReference(row - 1, column - 1);
            CellReference previousCellReference = selectedCellReference;
            handleCellSelection(column, row);
            selectedCellReference = cellReference;
            selectedCellChanged();
            if (discardOldRangeSelection) {
                cellRangeAddresses.clear();
                individualSelectedCells.clear();
                paintedCellRange = createCorrectCellRangeAddress(column,
                        column, row, row);
            }
            if (!cellReference.equals(previousCellReference)
                    || discardOldRangeSelection) {
                fireNewSelectionChangeEvent();
            }
        }

        /*
         * Cell selected from address field -> need to update sheet selection &
         * & function field
         */
        @Override
        public void sheetAddressChanged(String value) {
            try {
                handleCellAddressChange(value);
            } catch (Exception e) {
                getRpcProxy(SpreadsheetClientRpc.class).invalidCellAddress();
            }
        }

        @Override
        public void cellRangeSelected(int col1, int col2, int row1, int row2) {
            cellRangeAddresses.clear();
            individualSelectedCells.clear();
            CellRangeAddress cra = createCorrectCellRangeAddress(col1, col2,
                    row1, row2);
            paintedCellRange = cra;
            if (col1 != col2 || row1 != row2) {
                cellRangeAddresses.add(cra);
            }
            fireNewSelectionChangeEvent();
        }

        /* */
        @Override
        public void cellRangePainted(int selectedCellColumn,
                int selectedCellRow, int col1, int col2, int row1, int row2) {
            cellRangeAddresses.clear();
            individualSelectedCells.clear();

            selectedCellReference = new CellReference(selectedCellRow - 1,
                    selectedCellColumn - 1);

            handleCellSelection(selectedCellColumn, selectedCellRow);

            CellRangeAddress cra = createCorrectCellRangeAddress(col1, col2,
                    row1, row2);
            paintedCellRange = cra;
            cellRangeAddresses.add(cra);

            fireNewSelectionChangeEvent();
        }

        /* sent to client new selected cells formula if any */
        @Override
        public void cellAddedToSelectionAndSelected(int column, int row) {
            boolean oldSelectedCellInRange = false;
            for (CellRangeAddress range : cellRangeAddresses) {
                if (range.isInRange(selectedCellReference.getRow(),
                        selectedCellReference.getCol())) {
                    oldSelectedCellInRange = true;
                    break;
                }
            }
            boolean oldSelectedCellInIndividual = false;
            for (CellReference cell : individualSelectedCells) {
                if (cell.equals(selectedCellReference)) {
                    // it shouldn't be there yet(!)
                    oldSelectedCellInIndividual = true;
                    break;
                }
            }
            if (!oldSelectedCellInRange && !oldSelectedCellInIndividual) {
                individualSelectedCells.add(selectedCellReference);
            }
            handleCellSelection(column, row);
            selectedCellReference = new CellReference(row - 1, column - 1);
            selectedCellChanged();
            if (individualSelectedCells.contains(selectedCellReference)) {
                individualSelectedCells.remove(individualSelectedCells
                        .indexOf(selectedCellReference));
            }
            paintedCellRange = null;
            fireNewSelectionChangeEvent();
        }

        @Override
        public void cellsAddedToRangeSelection(int col1, int col2, int row1,
                int row2) {
            CellRangeAddress newRange = createCorrectCellRangeAddress(col1,
                    col2, row1, row2);
            for (Iterator<CellReference> i = individualSelectedCells.iterator(); i
                    .hasNext();) {
                CellReference cell = i.next();
                if (newRange.isInRange(cell.getRow(), cell.getCol())) {
                    i.remove();
                }
            }

            cellRangeAddresses.add(newRange);
            paintedCellRange = null;
            fireNewSelectionChangeEvent();
        }

        /* sent to client new selected cells formula if any */
        @Override
        public void rowSelected(int row, int firstColumnIndex) {
            handleCellSelection(firstColumnIndex, row);
            selectedCellReference = new CellReference(row - 1,
                    firstColumnIndex - 1);
            selectedCellChanged();
            cellRangeAddresses.clear();
            individualSelectedCells.clear();
            CellRangeAddress cra = createCorrectCellRangeAddress(1, getCols(),
                    row, row);
            paintedCellRange = cra;
            cellRangeAddresses.add(cra);
            fireNewSelectionChangeEvent();
        }

        /* sent to client new selected cells formula if any */
        @Override
        public void rowAddedToRangeSelection(int row, int firstColumnIndex) {
            boolean oldSelectedCellInRange = false;
            for (CellRangeAddress range : cellRangeAddresses) {
                if (range.isInRange(selectedCellReference.getRow(),
                        selectedCellReference.getCol())) {
                    oldSelectedCellInRange = true;
                    break;
                }
            }
            if (!oldSelectedCellInRange) {
                individualSelectedCells.add(selectedCellReference);
            }
            handleCellSelection(firstColumnIndex, row);
            selectedCellReference = new CellReference(row - 1,
                    firstColumnIndex - 1);
            selectedCellChanged();
            cellRangeAddresses.add(createCorrectCellRangeAddress(1, getCols(),
                    row, row));
            paintedCellRange = null;
            fireNewSelectionChangeEvent();
        }

        /* sent to client new selected cells formula if any */
        @Override
        public void columnSelected(int col, int firstRowIndex) {
            handleCellSelection(col, firstRowIndex);
            selectedCellReference = new CellReference(firstRowIndex - 1,
                    col - 1);
            selectedCellChanged();
            cellRangeAddresses.clear();
            individualSelectedCells.clear();
            CellRangeAddress cra = createCorrectCellRangeAddress(col, col, 1,
                    getRows());
            paintedCellRange = cra;
            cellRangeAddresses.add(cra);
            fireNewSelectionChangeEvent();
        }

        /* sent to client new selected cells formula if any */
        @Override
        public void columnAddedToSelection(int column, int firstRowIndex) {
            boolean oldSelectedCellInRange = false;
            for (CellRangeAddress range : cellRangeAddresses) {
                if (range.isInRange(selectedCellReference.getRow(),
                        selectedCellReference.getCol())) {
                    oldSelectedCellInRange = true;
                    break;
                }
            }
            if (!oldSelectedCellInRange) {
                individualSelectedCells.add(selectedCellReference);
            }
            handleCellSelection(column, firstRowIndex);
            selectedCellReference = new CellReference(firstRowIndex - 1,
                    column - 1);
            selectedCellChanged();
            cellRangeAddresses.add(createCorrectCellRangeAddress(column,
                    column, 1, getRows()));
            paintedCellRange = null;
            fireNewSelectionChangeEvent();
        }

        /* the actual selected cell hasn't changed */
        @Override
        public void selectionIncreasePainted(int c1, int c2, int r1, int r2) {
            if (paintedCellRange != null) {
                if (isRangeEditable(paintedCellRange)
                        && isRangeEditable(c1 - 1, c2 - 1, r1 - 1, r2 - 1)) {
                    if (c1 != paintedCellRange.getFirstColumn() + 1) {
                        // shift left
                        shiftColumnsLeft(c1);
                        updateMarkedCellValues(c1,
                                paintedCellRange.getFirstColumn(), r1, r2);
                    } else if (c2 != paintedCellRange.getLastColumn() + 1) {
                        // shift right
                        shiftColumnsRight(c2);
                        updateMarkedCellValues(
                                paintedCellRange.getLastColumn() + 2, c2, r1,
                                r2);
                    } else if (r1 != paintedCellRange.getFirstRow() + 1) {
                        // shift top
                        shiftRowsUp(r1);
                        updateMarkedCellValues(c1, c2, r1,
                                paintedCellRange.getFirstRow());
                    } else if (r2 != paintedCellRange.getLastRow() + 1) {
                        // shift bottom
                        shiftRowsDown(r2);
                        updateMarkedCellValues(c1, c2,
                                paintedCellRange.getLastRow() + 2, r2);
                    }
                    paintedCellRange = createCorrectCellRangeAddress(c1, c2,
                            r1, r2);
                    cellRangeAddresses.clear();
                    cellRangeAddresses.add(createCorrectCellRangeAddress(c1,
                            c2, r1, r2));
                    Row row = getActiveSheet().getRow(
                            selectedCellReference.getRow());
                    if (row != null) {
                        Cell cell = row.getCell(selectedCellReference.getCol());
                        if (cell != null) {
                            String value = "";
                            boolean formula = cell.getCellType() == Cell.CELL_TYPE_FORMULA;
                            if (!isCellHidden(cell)) {
                                if (formula) {
                                    value = cell.getCellFormula();
                                } else {
                                    value = getCellValue(cell);
                                }
                            }
                            getRpcProxy(SpreadsheetClientRpc.class)
                                    .showSelectedCellRange(c1, c2, r1, r2,
                                            value, formula, isCellLocked(cell));
                        } else {
                            getRpcProxy(SpreadsheetClientRpc.class)
                                    .showSelectedCellRange(c1, c2, r1, r2, "",
                                            false, isCellLocked(cell));
                        }
                    } else {
                        getRpcProxy(SpreadsheetClientRpc.class)
                                .showSelectedCellRange(c1, c2, r1, r2, "",
                                        false, isSheetProtected());
                    }
                    // The selected cell hasn't changed, but the cell range
                    // addresses has
                    fireNewSelectionChangeEvent();
                } else {
                    // TODO should show some sort of error, saying that some
                    // cells are locked so cannot shift
                }
            }
        }

        /* the actual selected cell hasn't changed */
        @Override
        public void selectionDecreasePainted(int c, int r) {
            if (paintedCellRange != null) {
                if (isRangeEditable(paintedCellRange)) {
                    removeCells(c, paintedCellRange.getLastColumn() + 1, r,
                            paintedCellRange.getLastRow() + 1, false);
                    // removedCells makes sure that removed cells are marked.
                    updateMarkedCellValues(0, 0, 0, 0);
                    // range selection was updated if NOT all cells were painted
                    boolean rangeSelectionChanged = false;
                    if (c != paintedCellRange.getFirstColumn() + 1) {
                        rangeSelectionChanged = true;
                        paintedCellRange = createCorrectCellRangeAddress(
                                paintedCellRange.getFirstColumn() + 1, c - 1,
                                paintedCellRange.getFirstRow() + 1,
                                paintedCellRange.getLastRow() + 1);
                    } else if (r != paintedCellRange.getFirstRow() + 1) {
                        rangeSelectionChanged = true;
                        paintedCellRange = createCorrectCellRangeAddress(
                                paintedCellRange.getFirstColumn() + 1,
                                paintedCellRange.getLastColumn() + 1,
                                paintedCellRange.getFirstRow() + 1, r - 1);
                    }
                    int c1 = paintedCellRange.getFirstColumn() + 1;
                    int c2 = paintedCellRange.getLastColumn() + 1;
                    int r1 = paintedCellRange.getFirstRow() + 1;
                    int r2 = paintedCellRange.getLastRow() + 1;
                    Row row = getActiveSheet().getRow(
                            selectedCellReference.getRow());
                    if (row != null) {
                        Cell cell = row.getCell(selectedCellReference.getCol());
                        if (cell != null) {
                            String value = "";
                            boolean formula = cell.getCellType() == Cell.CELL_TYPE_FORMULA;
                            if (!isCellHidden(cell)) {
                                if (formula) {
                                    value = cell.getCellFormula();
                                } else {
                                    value = getCellValue(cell);
                                }
                            }
                            getRpcProxy(SpreadsheetClientRpc.class)
                                    .showSelectedCellRange(c1, c2, r1, r2,
                                            value, formula, isCellLocked(cell));
                        } else {
                            getRpcProxy(SpreadsheetClientRpc.class)
                                    .showSelectedCellRange(c1, c2, r1, r2, "",
                                            false, isCellLocked(cell));
                        }
                    } else {
                        getRpcProxy(SpreadsheetClientRpc.class)
                                .showSelectedCellRange(c1, c2, r1, r2, "",
                                        false, isSheetProtected());
                    }
                    // the cell hasn't changed, but the value in the cell might
                    // have changed if it was decreased. need to call this so
                    // user can update possible custom editor value
                    if ((c - 1) == selectedCellReference.getCol()
                            && (r - 1) == selectedCellReference.getRow()) {
                        selectedCellChanged();
                    }
                    // the cell range addresses has changed (decreased)
                    if (rangeSelectionChanged) {
                        cellRangeAddresses.clear();
                        if (paintedCellRange.getFirstColumn() != paintedCellRange
                                .getLastColumn()
                                || paintedCellRange.getFirstRow() != paintedCellRange
                                        .getLastRow()) {
                            cellRangeAddresses.add(paintedCellRange);
                        }
                        fireNewSelectionChangeEvent();
                    }
                } else {
                    // TODO should show some sort of error, saying that some
                    // cells are locked so cannot shift
                }
            }
        }

        @Override
        public void cellValueEdited(int col, int row, String value) {
            if (col > 0 && row > 0) {
                handleCellValueChange(col, row, value);
            }
        }

        @Override
        public void sheetSelected(int tabIndex, int scrollLeft, int scrollTop) {
            // this is for the very rare occasion when the sheet has been
            // selected and the selected sheet value is still negative
            int oldIndex = Math.abs(getState().sheetIndex) - 1;
            getState().verticalScrollPositions[oldIndex] = scrollTop;
            getState().horizontalScrollPositions[oldIndex] = scrollLeft;
            Sheet oldSheet = getActiveSheet();
            setActiveSheetIndex(tabIndex);
            Sheet newSheet = getActiveSheet();
            fireSelectedSheetChangeEvent(oldSheet, newSheet);
        }

        @Override
        public void sheetRenamed(int sheetIndex, String sheetName) {
            setSheetNameWithPOIIndex(getVisibleSheetPOIIndex(sheetIndex),
                    sheetName);
        }

        @Override
        public void sheetCreated(int scrollLeft, int scrollTop) {
            getState().verticalScrollPositions[getState().sheetIndex - 1] = scrollTop;
            getState().horizontalScrollPositions[getState().sheetIndex - 1] = scrollLeft;
            createNewSheet(null, defaultNewSheetRows, defaultNewSheetColumns);
        }

        @Override
        public void firstVisibleTabChanged(int firstVisibleTab) {
            // workbook.setFirstVisibleTab(firstVisibleTab);
            // getState().firstVisibleTab = firstVisibleTab;
        }

        @Override
        public void deleteSelectedCells() {
            final Sheet activeSheet = getActiveSheet();
            // TODO show error on locked cells instead
            if (selectedCellReference != null) {
                Row row = activeSheet.getRow(selectedCellReference.getRow());
                if (row != null
                        && isCellLocked(row.getCell(selectedCellReference
                                .getCol()))) {
                    return;
                }
            }
            for (CellReference cr : individualSelectedCells) {
                final Row row = activeSheet.getRow(cr.getRow());
                if (row != null && isCellLocked(row.getCell(cr.getCol()))) {
                    return;
                }
            }
            for (CellRangeAddress range : cellRangeAddresses) {
                if (!isRangeEditable(range)) {
                    return;
                }
            }

            if (selectedCellReference != null) {
                removeCell(selectedCellReference.getCol() + 1,
                        selectedCellReference.getRow() + 1, false);
            }
            for (CellReference cr : individualSelectedCells) {
                removeCell(cr.getCol() + 1, cr.getRow() + 1, false);
            }
            for (CellRangeAddress range : cellRangeAddresses) {
                removeCells(range.getFirstColumn() + 1,
                        range.getLastColumn() + 1, range.getFirstRow() + 1,
                        range.getLastRow() + 1, false);
            }
            // removeCell and removeCells makes sure that cells are removed and
            // cleared from client side cache.
            updateMarkedCellValues(0, 0, 0, 0);
        }

        @Override
        public void linkCellClicked(int column, int row) {
            Cell cell = getActiveSheet().getRow(row - 1).getCell(column - 1);
            if (hyperlinkCellClickHandler != null) {
                hyperlinkCellClickHandler.onHyperLinkCellClick(cell,
                        cell.getHyperlink(), Spreadsheet.this);
            } else {
                DefaultHyperlinkCellClickHandler.get().onHyperLinkCellClick(
                        cell, cell.getHyperlink(), Spreadsheet.this);
            }
        }

        @Override
        public void contextMenuOpenOnSelection(int column, int row) {
            try {
                // update the selection if the context menu wasn't triggered on
                // top of any of the cells inside the current selection.
                CellReference cellReference = new CellReference(row - 1,
                        column - 1);
                boolean keepSelection = cellReference
                        .equals(selectedCellReference)
                        || individualSelectedCells.contains(cellReference);
                if (!keepSelection) {
                    for (CellRangeAddress cra : cellRangeAddresses) {
                        if (cra.isInRange(row - 1, column - 1)) {
                            keepSelection = true;
                            break;
                        }
                    }
                }
                if (!keepSelection) {
                    // click was on top of a cell that is not the selected cell,
                    // not one of the individual cells nor part of any cell
                    // ranges -> set as the selected cell
                    handleCellAddressChange(cellReference.getCol() + 1,
                            cellReference.getRow() + 1);
                    paintedCellRange = createCorrectCellRangeAddress(
                            cellReference.getCol() + 1,
                            cellReference.getCol() + 1,
                            cellReference.getRow() + 1,
                            cellReference.getRow() + 1);
                    selectedCellReference = cellReference;
                    cellRangeAddresses.clear();
                    individualSelectedCells.clear();
                    selectedCellChanged();
                    fireNewSelectionChangeEvent();
                }

                List<SpreadsheetActionDetails> actions = createActionsListForSelection();
                if (!actions.isEmpty()) {
                    getRpcProxy(SpreadsheetClientRpc.class)
                            .showActions(actions);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // rather catch it than let the component crash and burn
            }
        }

        @Override
        public void rowHeaderContextMenuOpen(int rowIndex) {
            List<SpreadsheetActionDetails> actions = createActionsListForRow(rowIndex);
            if (!actions.isEmpty()) {
                getRpcProxy(SpreadsheetClientRpc.class).showActions(actions);
                contextMenuHeaderIndex = rowIndex;
            }
        }

        @Override
        public void columnHeaderContextMenuOpen(int columnIndex) {
            List<SpreadsheetActionDetails> actions = createActionsListForColumn(columnIndex);
            if (!actions.isEmpty()) {
                getRpcProxy(SpreadsheetClientRpc.class).showActions(actions);
                contextMenuHeaderIndex = columnIndex;
            }
        }

        @Override
        public void actionOnCurrentSelection(String actionKey) {
            Action action = actionMapper.get(actionKey);
            for (Action.Handler ah : actionHandlers) {
                ah.handleAction(action, Spreadsheet.this, latestSelectionEvent);
            }
        }

        @Override
        public void actionOnRowHeader(String actionKey) {
            Action action = actionMapper.get(actionKey);
            final CellRangeAddress row = new CellRangeAddress(
                    contextMenuHeaderIndex - 1, contextMenuHeaderIndex - 1, -1,
                    -1);
            for (Action.Handler ah : actionHandlers) {
                ah.handleAction(action, Spreadsheet.this, row);
            }
        }

        @Override
        public void actionOnColumnHeader(String actionKey) {
            Action action = actionMapper.get(actionKey);
            final CellRangeAddress column = new CellRangeAddress(-1, -1,
                    contextMenuHeaderIndex - 1, contextMenuHeaderIndex - 1);
            for (Action.Handler ah : actionHandlers) {
                ah.handleAction(action, Spreadsheet.this, column);
            }
        }

        @Override
        public void rowsResized(Map<Integer, Float> newRowSizes, int col1,
                int col2, int row1, int row2) {
            for (Entry<Integer, Float> entry : newRowSizes.entrySet()) {
                int index = entry.getKey();
                float height = entry.getValue();
                if (height == 0.0F) {
                    setRowHidden(index - 1, true);
                } else {
                    getState().rowH[index - 1] = height;
                    Row row = getActiveSheet().getRow(index - 1);
                    if (row == null) {
                        row = getActiveSheet().createRow(index - 1);
                    }
                    row.setHeightInPoints(height);
                }
            }
            if (sheetImages != null) {
                reloadImageSizesFromPOI = true;
            }
            loadCells(row1, row2, col1, col2);
        }

        @Override
        public void columnResized(Map<Integer, Integer> newColumnSizes,
                int col1, int col2, int row1, int row2) {
            for (Entry<Integer, Integer> entry : newColumnSizes.entrySet()) {
                int index = entry.getKey();
                int width = entry.getValue();
                if (width == 0) {
                    setColumnHidden(index - 1, true);
                } else {
                    getState().colW[index - 1] = width;
                    getActiveSheet().setColumnWidth(index - 1,
                            SpreadsheetFactory.pixel2WidthUnits(width));
                }
            }
            if (sheetImages != null) {
                reloadImageSizesFromPOI = true;
            }
            loadCells(row1, row2, col1, col2);
        }

        @Override
        public void onColumnAutofit(int columnIndex) {
            autofitColumn(columnIndex - 1);
        }

    };

    public Spreadsheet() {
        individualSelectedCells = new ArrayList<CellReference>();
        cellRangeAddresses = new ArrayList<CellRangeAddress>();

        sentCells = new HashSet<String>();
        sentFormulaCells = new HashSet<String>();
        removedCells = new HashSet<String>();
        markedCells = new HashSet<String>();
        sheetImages = new HashSet<SheetImageWrapper>();
        tables = new HashSet<SpreadsheetTable>();

        registerRpc(serverRPC);
        setSizeFull(); // Default to full size

        formatter = new DataFormatter();

        SpreadsheetFactory.loadSpreadsheetWith(this, null);
    }

    public Spreadsheet(Workbook workbook) {
        individualSelectedCells = new ArrayList<CellReference>();
        cellRangeAddresses = new ArrayList<CellRangeAddress>();

        sentCells = new HashSet<String>();
        sentFormulaCells = new HashSet<String>();
        removedCells = new HashSet<String>();
        markedCells = new HashSet<String>();
        sheetImages = new HashSet<SheetImageWrapper>();
        tables = new HashSet<SpreadsheetTable>();

        registerRpc(serverRPC);
        setSizeFull(); // Default to full size

        formatter = new DataFormatter();

        SpreadsheetFactory.loadSpreadsheetWith(this, workbook);
    }

    protected Spreadsheet(int x) {
        individualSelectedCells = new ArrayList<CellReference>();
        cellRangeAddresses = new ArrayList<CellRangeAddress>();

        sentCells = new HashSet<String>();
        sentFormulaCells = new HashSet<String>();
        removedCells = new HashSet<String>();
        markedCells = new HashSet<String>();
        sheetImages = new HashSet<SheetImageWrapper>();
        tables = new HashSet<SpreadsheetTable>();

        registerRpc(serverRPC);
        setSizeFull(); // Default to full size

        formatter = new DataFormatter();
    }

    /**
     * Adds an action handler to the spreadsheet that handles the event produced
     * by the context menu (right click) on cells and row and column headers.
     * The action handler is component, not workbook, specific.
     * <p>
     * The parameters on the
     * {@link Handler#handleAction(Action, Object, Object)} and
     * {@link Handler#getActions(Object, Object)} depend on the actual target of
     * the right click.
     * <p>
     * The second parameter (sender) on
     * {@link Handler#getActions(Object, Object)} is always the spreadsheet
     * component. In case of a cell, the first parameter (target) on contains
     * the latest {@link SelectionChangeEvent} for the spreadsheet. In case of a
     * row or a column header, the first parameter (target) is a
     * {@link CellRangeAddress}. To distinct between column / row header, you
     * can use {@link CellRangeAddress#isFullColumnRange()} and
     * {@link CellRangeAddress#isFullRowRange()}.
     * <p>
     * Similarly for {@link Handler#handleAction(Action, Object, Object)} the
     * second parameter (sender) is always the spreadsheet component. The third
     * parameter (target) is the latest {@link SelectionChangeEvent} for the
     * spreadsheet, or the {@link CellRangeAddress} defining the selected row /
     * column header.
     */
    @Override
    public void addActionHandler(Handler actionHandler) {
        if (actionHandler != null) {
            if (actionHandlers == null) {
                actionHandlers = new LinkedList<Action.Handler>();
                actionMapper = new KeyMapper<Action>();
            }
            if (!actionHandlers.contains(actionHandler)) {
                actionHandlers.add(actionHandler);
                getState().hasActions = true;
            }
        }
    }

    /**
     * Removes a previously registered action handler from this spreadsheet.
     */
    @Override
    public void removeActionHandler(Handler actionHandler) {
        if (actionHandlers != null && actionHandlers.contains(actionHandler)) {
            actionHandlers.remove(actionHandler);
            if (actionHandlers.isEmpty()) {
                actionHandlers = null;
                actionMapper = null;
                getState().hasActions = false;
            }
        }
    }

    /**
     * Sets the {@link CellValueHandler} for this component (not workbook/sheet
     * specific). It is called when a cell's value has been updated by the user
     * by using the spreadsheet component's default editor (text input).
     * 
     * @param cellValueHandler
     *            or <code>null</code> if none should be used
     */
    public void setCellValueHandler(CellValueHandler cellValueHandler) {
        customCellValueHandler = cellValueHandler;
    }

    /**
     * See {@link CellValueHandler}.
     * 
     * @return the current {@link CellValueHandler} for this component or
     *         <code>null</code> if none has been set
     */
    public CellValueHandler getCellValueHandler() {
        return customCellValueHandler;
    }

    /**
     * Sets the {@link HyperlinkCellClickHandler} for this component (not
     * workbook/sheet specific). It's called when the user click a cell that is
     * a hyperlink.
     * 
     * @param hyperLinkCellClickHandler
     *            or <code>null</code> if none should be used
     */
    public void setHyperlinkCellClickHandler(
            HyperlinkCellClickHandler hyperLinkCellClickHandler) {
        hyperlinkCellClickHandler = hyperLinkCellClickHandler;
    }

    /**
     * see {@link HyperlinkCellClickHandler}.
     * 
     * @return the current {@link HyperlinkCellClickHandler} for this component
     *         or <code>null</code> if none has been set
     */
    public HyperlinkCellClickHandler getHyperlinkCellClickHandler() {
        return hyperlinkCellClickHandler;
    }

    protected void shiftRowsDown(int newLastRow) {
        int r1 = paintedCellRange.getFirstRow() + 1;
        int r2 = paintedCellRange.getLastRow() + 1;
        int c1 = paintedCellRange.getFirstColumn() + 1;
        int c2 = paintedCellRange.getLastColumn() + 1;
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        for (int shiftedRowIndex = r1; shiftedRowIndex <= r2; shiftedRowIndex++) {
            final Row shiftedRow = activeSheet.getRow(shiftedRowIndex - 1);
            int newRowIndex = r2 + 1 + (shiftedRowIndex - r1);
            while (newRowIndex <= newLastRow) {
                if (shiftedRow != null) {
                    Row newRow = activeSheet.getRow(newRowIndex - 1);
                    if (newRow == null) {
                        newRow = activeSheet.createRow(newRowIndex - 1);
                    }
                    for (int c = c1; c <= c2; c++) {
                        Cell shiftedCell = shiftedRow.getCell(c - 1);
                        Cell newCell = newRow.getCell(c - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = newRow.createCell(c - 1);
                            }
                            shiftCellValue(shiftedCell, newCell, false);
                        } else if (newCell != null) {
                            final String key = toKey(
                                    newCell.getColumnIndex() + 1,
                                    newCell.getRowIndex() + 1);
                            removedCells.add(Integer.toString(newCell
                                    .getRowIndex() + 1));
                            removedCells.add(key);
                            if (!sentCells.remove(key)) {
                                sentFormulaCells.remove(key);
                            }
                            newCell.setCellValue((String) null);
                            evaluator.notifyUpdateCell(newCell);
                        }
                    }
                } else {
                    removeCells(c1, c2, newRowIndex, newRowIndex, true);
                    removedCells.add(Integer.toString(newRowIndex));
                    for (int i = c1; i <= c2; i++) {
                        final String key = toKey(i, newRowIndex);
                        if (sentCells.remove(key)) {
                            sentFormulaCells.remove(key);
                        }
                        removedCells.add(key);
                    }
                }
                newRowIndex += r2 - r1 + 1;
            }
        }
    }

    protected void shiftRowsUp(int newFirstRow) {
        int r1 = paintedCellRange.getFirstRow() + 1;
        int r2 = paintedCellRange.getLastRow() + 1;
        int c1 = paintedCellRange.getFirstColumn() + 1;
        int c2 = paintedCellRange.getLastColumn() + 1;
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        for (int shiftedRowIndex = r1; shiftedRowIndex <= r2; shiftedRowIndex++) {
            final Row shiftedRow = activeSheet.getRow(shiftedRowIndex - 1);
            int newRowIndex = r1 - 1 - (shiftedRowIndex - r1);
            while (newRowIndex >= newFirstRow) {
                if (shiftedRow != null) {
                    Row newRow = activeSheet.getRow(newRowIndex - 1);
                    if (newRow == null) {
                        newRow = activeSheet.createRow(newRowIndex - 1);
                    }
                    for (int c = c1; c <= c2; c++) {
                        Cell shiftedCell = shiftedRow.getCell(c - 1);
                        Cell newCell = newRow.getCell(c - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = newRow.createCell(c - 1);
                            }
                            shiftCellValue(shiftedCell, newCell, false);
                        } else if (newCell != null) {
                            final String key = toKey(
                                    newCell.getColumnIndex() + 1,
                                    newCell.getRowIndex() + 1);
                            removedCells.add(Integer.toString(newCell
                                    .getRowIndex() + 1));
                            removedCells.add(key);
                            if (!sentCells.remove(key)) {
                                sentFormulaCells.remove(key);
                            }
                            // update style to 0
                            newCell.setCellStyle(null);
                            styler.cellStyleUpdated(newCell, true);
                            newCell.setCellValue((String) null);
                            evaluator.notifyUpdateCell(newCell);
                        }
                    }
                } else {
                    removeCells(c1, c2, newRowIndex, newRowIndex, true);
                    removedCells.add(Integer.toString(newRowIndex));
                    for (int i = c1; i <= c2; i++) {
                        final String key = toKey(i, newRowIndex);
                        if (sentCells.remove(key)) {
                            sentFormulaCells.remove(key);
                        }
                        removedCells.add(key);
                    }
                }
                newRowIndex = newRowIndex - (r2 - r1) - 1;
            }
        }
    }

    protected void shiftColumnsRight(int newRightMostColumn) {
        int r1 = paintedCellRange.getFirstRow() + 1;
        int r2 = paintedCellRange.getLastRow() + 1;
        int c1 = paintedCellRange.getFirstColumn() + 1;
        int c2 = paintedCellRange.getLastColumn() + 1;
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        for (int rIndex = r1; rIndex <= r2; rIndex++) {
            final Row row = activeSheet.getRow(rIndex - 1);
            if (row != null) {
                for (int shiftedCellIndex = c1; shiftedCellIndex <= c2; shiftedCellIndex++) {
                    Cell shiftedCell = row.getCell(shiftedCellIndex - 1);
                    int newCellIndex = c2 + 1 + (shiftedCellIndex - c1);
                    while (newCellIndex <= newRightMostColumn) {
                        Cell newCell = row.getCell(newCellIndex - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = row.createCell(newCellIndex - 1);
                            }
                            shiftCellValue(shiftedCell, newCell, false);

                        } else if (newCell != null) {
                            final String key = toKey(
                                    newCell.getColumnIndex() + 1,
                                    newCell.getRowIndex() + 1);
                            removedCells.add(Integer.toString(newCell
                                    .getRowIndex() + 1));
                            removedCells.add(key);
                            if (!sentCells.remove(key)) {
                                sentFormulaCells.remove(key);
                            }
                            newCell.setCellValue((String) null);
                            evaluator.notifyUpdateCell(newCell);
                            // update style to 0
                            newCell.setCellStyle(null);
                            styler.cellStyleUpdated(newCell, true);
                        }
                        newCellIndex += (c2 - c1) + 1;
                    }
                }
            }
        }
    }

    protected void shiftColumnsLeft(int newLeftMostColumn) {
        int r1 = paintedCellRange.getFirstRow() + 1;
        int r2 = paintedCellRange.getLastRow() + 1;
        int c1 = paintedCellRange.getFirstColumn() + 1;
        int c2 = paintedCellRange.getLastColumn() + 1;
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        for (int rIndex = r1; rIndex <= r2; rIndex++) {
            final Row row = activeSheet.getRow(rIndex - 1);
            if (row != null) {
                for (int shiftedCellIndex = c1; shiftedCellIndex <= c2; shiftedCellIndex++) {
                    Cell shiftedCell = row.getCell(shiftedCellIndex - 1);
                    int newCellIndex = c1 - (shiftedCellIndex - c1) - 1;
                    while (newCellIndex >= newLeftMostColumn) {
                        Cell newCell = row.getCell(newCellIndex - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = row.createCell(newCellIndex - 1);
                            }
                            shiftCellValue(shiftedCell, newCell, false);
                        } else if (newCell != null) {
                            final String key = toKey(
                                    newCell.getColumnIndex() + 1,
                                    newCell.getRowIndex() + 1);
                            if (!sentCells.remove(key)) {
                                sentFormulaCells.remove(key);
                            }
                            removedCells.add(Integer.toString(newCell
                                    .getRowIndex() + 1));
                            removedCells.add(key);
                            newCell.setCellValue((String) null);
                            evaluator.notifyUpdateCell(newCell);
                            // update style to 0
                            newCell.setCellStyle(null);
                            styler.cellStyleUpdated(newCell, true);
                        }
                        newCellIndex = newCellIndex - (c2 - c1) - 1;
                    }
                }
            }
        }
    }

    private void shiftCellValue(Cell shiftedCell, Cell newCell,
            boolean removeShifted) {
        // clear the new cell first because it might have errors which prevent
        // it from being set to a new type
        if (newCell.getCellType() != Cell.CELL_TYPE_BLANK
                || shiftedCell.getCellType() == Cell.CELL_TYPE_BLANK) {
            newCell.setCellType(Cell.CELL_TYPE_BLANK);
        }
        newCell.setCellType(shiftedCell.getCellType());
        newCell.setCellStyle(shiftedCell.getCellStyle());
        styler.cellStyleUpdated(newCell, true);
        if (shiftedCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            try {
                if (shiftedCell.getColumnIndex() != newCell.getColumnIndex()) {
                    // shift column indexes
                    int collDiff = newCell.getColumnIndex()
                            - shiftedCell.getColumnIndex();
                    Matcher matcher = rowShiftPattern.matcher(shiftedCell
                            .getCellFormula());
                    String originalFormula = shiftedCell.getCellFormula();
                    String newFormula = originalFormula;
                    while (matcher.find()) {
                        String s = matcher.group();
                        if (!s.startsWith("$")) {
                            int replaceIndex = newFormula.indexOf(s);
                            while (replaceIndex > 0
                                    && newFormula.charAt(replaceIndex - 1) == '$') {
                                replaceIndex = newFormula.indexOf(s,
                                        replaceIndex + 1);
                            }
                            if (replaceIndex > -1) {
                                String oldIndexString = s.replaceAll(
                                        "[$]{0,1}\\d+", "");

                                int columnIndex = getColHeaderIndex(oldIndexString);
                                columnIndex += collDiff;
                                String replacement = s.replace(oldIndexString,
                                        getColHeader(columnIndex));
                                newFormula = newFormula.substring(0,
                                        replaceIndex)
                                        + replacement
                                        + newFormula.substring(replaceIndex
                                                + s.length());
                            }
                        }
                    }
                    newCell.setCellFormula(newFormula);
                } else { // shift row indexes
                    int rowDiff = newCell.getRowIndex()
                            - shiftedCell.getRowIndex();
                    Matcher matcher = rowShiftPattern.matcher(shiftedCell
                            .getCellFormula());
                    String originalFormula = shiftedCell.getCellFormula();
                    String newFormula = originalFormula;
                    while (matcher.find()) {
                        String s = matcher.group();
                        String rowString = s.replaceAll(
                                "([$][a-zA-Z]+)|([a-zA-Z]+)", "");
                        if (!rowString.startsWith("$")) {
                            int row = Integer.parseInt(rowString);
                            row += rowDiff;
                            String replacement = s.replace(rowString,
                                    Integer.toString(row));
                            // impossible to replace a row with $ before it
                            // because
                            // of the column address
                            newFormula = newFormula.replace(s, replacement);
                        }
                    }
                    newCell.setCellFormula(newFormula);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // TODO visialize shifting error
                newCell.setCellFormula(shiftedCell.getCellFormula());
            }
            evaluator.notifySetFormula(newCell);
        } else {
            switch (shiftedCell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                newCell.setCellValue(shiftedCell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                newCell.setCellValue(shiftedCell.getErrorCellValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                newCell.setCellValue(shiftedCell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                newCell.setCellValue(shiftedCell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_BLANK:
                // cell is cleared when type is set
            default:
                break;
            }
            evaluator.notifyUpdateCell(newCell);
        }
        if (removeShifted) {
            final String key = toKey(shiftedCell.getColumnIndex() + 1,
                    shiftedCell.getRowIndex() + 1);
            removedCells.add(Integer.toString(shiftedCell.getRowIndex() + 1));
            removedCells.add(key);
            if (!sentCells.remove(key)) {
                sentFormulaCells.remove(key);
            }
            shiftedCell.setCellValue((String) null);
            evaluator.notifyUpdateCell(shiftedCell);
        }
    }

    private boolean isRangeEditable(CellRangeAddress cellRangeAddress) {
        return isRangeEditable(cellRangeAddress.getFirstColumn(),
                cellRangeAddress.getLastColumn(),
                cellRangeAddress.getFirstRow(), cellRangeAddress.getLastRow());
    }

    /**
     * 
     * @param col1
     *            0-based
     * @param col2
     *            0-based
     * @param row1
     *            0-based
     * @param row2
     *            0-based
     * @return
     */
    private boolean isRangeEditable(int col1, int col2, int row1, int row2) {
        if (isSheetProtected()) {
            for (int r = row1; r <= row2; r++) {
                final Row row = getActiveSheet().getRow(r);
                if (row != null) {
                    for (int c = col1; c <= col2; c++) {
                        final Cell cell = row.getCell(c);
                        if (isCellLocked(cell)) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    protected CellRangeAddress createCorrectCellRangeAddress(
            String addressString) {
        final String[] split = addressString.split(":");
        final CellReference cr1 = new CellReference(split[0]);
        final CellReference cr2 = new CellReference(split[1]);
        int r1 = cr1.getRow() > cr2.getRow() ? cr2.getRow() : cr1.getRow();
        int r2 = cr1.getRow() > cr2.getRow() ? cr1.getRow() : cr2.getRow();
        int c1 = cr1.getCol() > cr2.getCol() ? cr2.getCol() : cr1.getCol();
        int c2 = cr1.getCol() > cr2.getCol() ? cr1.getCol() : cr2.getCol();
        if (r1 >= getState().rows) {
            r1 = getState().rows - 1;
        }
        if (r2 >= getState().rows) {
            r2 = getState().rows - 1;
        }
        if (c1 >= getState().cols) {
            c1 = getState().cols - 1;
        }
        if (c2 >= getState().cols) {
            c2 = getState().cols - 1;
        }
        return new CellRangeAddress(r1, r2, c1, c2);
    }

    /**
     * 
     * @param col1
     *            1-based
     * @param col2
     *            1-based
     * @param row1
     *            1-based
     * @param row2
     *            1-based
     * @return
     */
    protected CellRangeAddress createCorrectCellRangeAddress(int col1,
            int col2, int row1, int row2) {
        int r1 = row1 > row2 ? row2 : row1;
        int r2 = row1 > row2 ? row1 : row2;
        int c1 = col1 > col2 ? col2 : col1;
        int c2 = col1 > col2 ? col1 : col2;
        if (r1 >= getState().rows) {
            r1 = getState().rows;
        }
        if (r2 >= getState().rows) {
            r2 = getState().rows;
        }
        if (c1 >= getState().cols) {
            c1 = getState().cols;
        }
        if (c2 >= getState().cols) {
            c2 = getState().cols;
        }
        return new CellRangeAddress(r1 - 1, r2 - 1, c1 - 1, c2 - 1);
    }

    /**
     * handles the new cell range that was given in the address field, returns
     * the range and new selected cell formula/value (if any)
     */
    private void handleCellRangeSelection(CellRangeAddress cra) {
        int row1 = cra.getFirstRow();
        int row2 = cra.getLastRow();
        int col1 = cra.getFirstColumn();
        int col2 = cra.getLastColumn();
        final Row row = workbook.getSheetAt(workbook.getActiveSheetIndex())
                .getRow(row1);
        if (row != null) {
            final Cell cell = row.getCell(col1);
            if (cell != null) {
                String value = "";
                boolean formula = cell.getCellType() == Cell.CELL_TYPE_FORMULA;
                if (!isCellHidden(cell)) {
                    if (formula) {
                        value = cell.getCellFormula();
                    } else {
                        value = getCellValue(cell);
                    }
                }
                getRpcProxy(SpreadsheetClientRpc.class).showSelectedCellRange(
                        col1 + 1, col2 + 1, row1 + 1, row2 + 1, value, formula,
                        isCellLocked(cell));
            } else {
                getRpcProxy(SpreadsheetClientRpc.class).showSelectedCellRange(
                        col1 + 1, col2 + 1, row1 + 1, row2 + 1, "", false,
                        isCellLocked(cell));
            }
        } else {
            getRpcProxy(SpreadsheetClientRpc.class).showSelectedCellRange(
                    col1 + 1, col2 + 1, row1 + 1, row2 + 1, "", false,
                    isSheetProtected());
        }
    }

    protected void handleCellAddressChange(String value) {
        if (value.contains(":")) {
            CellRangeAddress cra = createCorrectCellRangeAddress(value);
            // need to check the range for merged regions
            MergedRegion region = MergedRegionUtil.findIncreasingSelection(
                    mergedRegionContainer, cra.getFirstRow() + 1,
                    cra.getLastRow() + 1, cra.getFirstColumn() + 1,
                    cra.getLastColumn() + 1);
            if (region != null) {
                cra = new CellRangeAddress(region.row1 - 1, region.row2 - 1,
                        region.col1 - 1, region.col2 - 1);
            }
            handleCellRangeSelection(cra);
            selectedCellReference = new CellReference(cra.getFirstRow(),
                    cra.getFirstColumn());
            paintedCellRange = cra;
            cellRangeAddresses.clear();
            cellRangeAddresses.add(cra);
        } else {
            final CellReference cellReference = new CellReference(value);
            MergedRegion region = MergedRegionUtil.findIncreasingSelection(
                    mergedRegionContainer, cellReference.getRow() + 1,
                    cellReference.getRow() + 1, cellReference.getCol() + 1,
                    cellReference.getCol() + 1);
            if (region != null
                    && (region.col1 != region.col2 || region.row1 != region.row2)) {
                CellRangeAddress cra = createCorrectCellRangeAddress(
                        region.col1, region.col2, region.row1, region.row2);
                handleCellRangeSelection(cra);
                selectedCellReference = new CellReference(cra.getFirstRow(),
                        cra.getFirstColumn());
                paintedCellRange = cra;
                cellRangeAddresses.clear();
                cellRangeAddresses.add(cra);
            } else {
                handleCellAddressChange(cellReference.getCol() + 1,
                        cellReference.getRow() + 1);
                paintedCellRange = createCorrectCellRangeAddress(
                        cellReference.getCol() + 1, cellReference.getCol() + 1,
                        cellReference.getRow() + 1, cellReference.getRow() + 1);
                selectedCellReference = cellReference;
                cellRangeAddresses.clear();
            }
        }
        individualSelectedCells.clear();
        selectedCellChanged();
        fireNewSelectionChangeEvent();
    }

    /**
     * Reports the correct cell selection value (formula/data) and selection.
     * This method is called when the cell selection has changed via the address
     * field.
     * 
     * @param columnIndex
     *            1-based
     * @param rowIndex
     *            1-based
     */
    private void handleCellAddressChange(int colIndex, int rowIndex) {
        if (rowIndex >= getState().rows) {
            rowIndex = getState().rows;
        }
        if (colIndex >= getState().cols) {
            colIndex = getState().cols;
        }
        MergedRegion region = MergedRegionUtil.findIncreasingSelection(
                mergedRegionContainer, rowIndex, rowIndex, colIndex, colIndex);
        if (region.col1 != region.col2 || region.row1 != region.row2) {
            handleCellRangeSelection(new CellRangeAddress(region.row1 - 1,
                    region.row2 - 1, region.col1 - 1, region.col2 - 1));
        } else {
            rowIndex = region.row1;
            colIndex = region.col1;
            final Row row = workbook.getSheetAt(workbook.getActiveSheetIndex())
                    .getRow(rowIndex - 1);
            if (row != null) {
                final Cell cell = row.getCell(colIndex - 1);
                if (cell != null) {
                    String value = "";
                    boolean formula = cell.getCellType() == Cell.CELL_TYPE_FORMULA;
                    if (!isCellHidden(cell)) {
                        if (formula) {
                            value = cell.getCellFormula();
                        } else {
                            value = getCellValue(cell);
                        }
                    }
                    getRpcProxy(SpreadsheetClientRpc.class).showSelectedCell(
                            colIndex, rowIndex, value, formula,
                            isCellLocked(cell));
                } else {
                    getRpcProxy(SpreadsheetClientRpc.class).showSelectedCell(
                            colIndex, rowIndex, "", false, isCellLocked(cell));
                }
            } else {
                getRpcProxy(SpreadsheetClientRpc.class).showSelectedCell(
                        colIndex, rowIndex, "", false, isSheetProtected());
            }
        }
    }

    /**
     * Reports the selected cell formula value, if any. This method is called
     * when the cell value has changed via sheet cell selection change.
     * 
     * This method can also be used when the selected cell has NOT changed but
     * the value it displays on the formula field might have changed and needs
     * to be updated.
     * 
     * @param columnIndex
     *            1-based
     * @param rowIndex
     *            1-based
     */
    private void handleCellSelection(int columnIndex, int rowIndex) {
        final Row row = workbook.getSheetAt(workbook.getActiveSheetIndex())
                .getRow(rowIndex - 1);
        if (row != null) {
            final Cell cell = row.getCell(columnIndex - 1);
            if (cell != null) {
                String value = "";
                boolean formula = cell.getCellType() == Cell.CELL_TYPE_FORMULA;
                if (!isCellHidden(cell)) {
                    if (formula) {
                        value = cell.getCellFormula();
                    } else {
                        value = getCellValue(cell);
                    }
                }
                getRpcProxy(SpreadsheetClientRpc.class).showCellValue(value,
                        columnIndex, rowIndex, formula, isCellLocked(cell));
            } else {
                getRpcProxy(SpreadsheetClientRpc.class).showCellValue("",
                        columnIndex, rowIndex, false, isCellLocked(cell));
            }
        } else {
            getRpcProxy(SpreadsheetClientRpc.class).showCellValue("",
                    columnIndex, rowIndex, false, isSheetProtected());
        }
    }

    /**
     * Updates the cell value and type, causes a recalculation of all the values
     * in the cell.
     * 
     * If there is a {@link CellValueHandler} defined, then it is used.
     * 
     * Cells starting with "=" will be created/changed into FORMULA type.
     * 
     * Cells that are existing and are NUMERIC type will be parsed according to
     * their existing format, or if that fails, as Double.
     * 
     * Cells not containing any letters and containing at least one number will
     * be created/changed into NUMERIC type (formatting is not changed).
     * 
     * Existing Boolean cells will be parsed as Boolean.
     * 
     * For everything else and if any of the above fail, the cell will get the
     * STRING type and the value will just be a string, except empty values will
     * cause the cell type to be BLANK.
     * 
     * @param col
     *            1-based
     * @param row
     *            1-based
     * @param value
     *            the String value, formulas will start with an extra "="
     */
    private void handleCellValueChange(int col, int row, String value) {
        // update cell value
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        Row r = activeSheet.getRow(row - 1);
        if (r == null) {
            r = activeSheet.createRow(row - 1);
        }
        Cell cell = r.getCell(col - 1);

        if (customCellValueHandler == null
                || customCellValueHandler.cellValueUpdated(cell, activeSheet,
                        col - 1, row - 1, value, evaluator, formatter)) {
            try {
                // handle new cell creation
                if (cell == null) {
                    if (value.startsWith("=")) {
                        cell = r.createCell(col - 1, Cell.CELL_TYPE_FORMULA);
                        cell.setCellFormula(value.substring(1));
                        evaluator.notifySetFormula(cell);
                        if (value.startsWith("=HYPERLINK(")) {
                            // set the cell style to link cell
                            CellStyle hyperlinkCellStyle;
                            if (hyperlinkStyleIndex == -1) {
                                hyperlinkCellStyle = styler
                                        .createHyperlinkCellStyle();
                                hyperlinkStyleIndex = -1;
                            } else {
                                hyperlinkCellStyle = workbook
                                        .getCellStyleAt(hyperlinkStyleIndex);
                            }
                            cell.setCellStyle(hyperlinkCellStyle);
                            styler.cellStyleUpdated(cell, true);
                        }
                    } else {
                        if (value.isEmpty()) {
                            cell = r.createCell(col - 1); // BLANK
                        } else if (value.matches(numericCellDetectionPattern)) {
                            cell = r.createCell(col - 1, Cell.CELL_TYPE_NUMERIC);
                            try {
                                cell.setCellValue(Double.parseDouble(value));
                            } catch (NumberFormatException nfe) {
                                cell.setCellValue(value);
                            }
                        } else {
                            cell = r.createCell(col - 1, Cell.CELL_TYPE_STRING);
                            cell.setCellValue(value);
                        }
                        evaluator.notifyUpdateCell(cell);
                    }
                } else { // modify existing cell, possibly switch type
                    final String key = toKey(col, row);
                    final int cellType = cell.getCellType();
                    if (!sentCells.remove(key)) {
                        sentFormulaCells.remove(key);
                    }
                    if (value.startsWith("=")) {
                        evaluator.notifyUpdateCell(cell);
                        cell.setCellType(Cell.CELL_TYPE_FORMULA);
                        cell.setCellFormula(value.substring(1));
                        evaluator.notifySetFormula(cell);
                        if (value.startsWith("=HYPERLINK(")
                                && cell.getCellStyle().getIndex() != hyperlinkStyleIndex) {
                            // set the cell style to link cell
                            CellStyle hyperlinkCellStyle;
                            if (hyperlinkStyleIndex == -1) {
                                hyperlinkCellStyle = styler
                                        .createHyperlinkCellStyle();
                                hyperlinkStyleIndex = -1;
                            } else {
                                hyperlinkCellStyle = workbook
                                        .getCellStyleAt(hyperlinkStyleIndex);
                            }
                            cell.setCellStyle(hyperlinkCellStyle);
                            styler.cellStyleUpdated(cell, true);
                        }
                    } else {
                        if (value.isEmpty()) {
                            cell.setCellType(Cell.CELL_TYPE_BLANK);
                        } else if (cellType == Cell.CELL_TYPE_NUMERIC) {
                            parseValueIntoNumericCell(cell, value);
                        } else if (value.matches(numericCellDetectionPattern)) {
                            if (cellType == Cell.CELL_TYPE_FORMULA) {
                                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            }
                            try {
                                cell.setCellValue(Double.parseDouble(value));
                            } catch (NumberFormatException nfe) {
                                cell.setCellValue(value);
                            }
                        } else if (cellType == Cell.CELL_TYPE_BOOLEAN) {
                            cell.setCellValue(Boolean.parseBoolean(value));
                        } else {
                            if (cellType == Cell.CELL_TYPE_FORMULA) {
                                cell.setCellType(Cell.CELL_TYPE_STRING);
                            }
                            cell.setCellValue(value);
                        }
                        evaluator.notifyUpdateCell(cell);
                    }
                }
            } catch (FormulaParseException fpe) {
                try {
                    System.out.println(fpe.getMessage());
                    cell.setCellFormula(value.substring(1).replace(" ", ""));
                } catch (FormulaParseException fpe2) {
                    System.out.println(fpe2.getMessage());
                    cell.setCellValue(value);
                }
            } catch (NumberFormatException nfe) {
                System.out.println(nfe.getMessage());
                cell.setCellValue(value);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                cell.setCellValue(value);
            }
        }

        // update cell values
        updateMarkedCellValues(col, col, row, row);
    }

    private void parseValueIntoNumericCell(final Cell cell, final String value) {
        // try to parse the string with the existing cell
        // format
        Format oldFormat = formatter.createFormat(cell);
        if (oldFormat != null) {
            try {
                final Object parsedObject = oldFormat.parseObject(value);
                if (parsedObject instanceof Date) {
                    cell.setCellValue((Date) parsedObject);
                } else if (parsedObject instanceof Calendar) {
                    cell.setCellValue((Calendar) parsedObject);
                } else if (parsedObject instanceof Number) {
                    cell.setCellValue(((Number) parsedObject).doubleValue());
                } else {
                    cell.setCellValue(Double.parseDouble(value));
                }
            } catch (ParseException pe) {
                System.out.println("Could not parse String to format, "
                        + oldFormat.getClass() + ", "
                        + cell.getCellStyle().getDataFormatString() + " : "
                        + pe.getMessage());
                try {
                    cell.setCellValue(Double.parseDouble(value));
                } catch (NumberFormatException nfe) {
                    System.out.println("Could not parse String to Double: "
                            + nfe.getMessage());
                    cell.setCellValue(value);
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse String to Double: "
                        + nfe.getMessage());
                cell.setCellValue(value);
            }
        }
    }

    @Override
    protected SpreadsheetState getState() {
        return (SpreadsheetState) super.getState();
    }

    @Override
    protected SpreadsheetState getState(boolean markAsDirty) {
        return (SpreadsheetState) super.getState(markAsDirty);
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        formatter = new DataFormatter(locale);
        updatedAndRecalculateAllCellValues();
    }

    /**
     * See {@link Workbook#setSheetHidden(int, int)}.
     * <p>
     * Get the Workbook with {@link #getWorkbook()} and use its API to access
     * status on currently visible/hidden/very hidden sheets.
     * 
     * If the currently active sheet is set hidden, another sheet is set as
     * active sheet automatically. At least one sheet should be always visible.
     * 
     * @param hidden
     *            0-visible, 1-hidden, 2-very hidden
     * @param sheetPOIIndex
     *            0-based
     * @throws IllegalArgumentException
     *             if the index or state is invalid, or if trying to hide the
     *             only visible sheet
     */
    public void setSheetHidden(int sheetPOIIndex, int hidden)
            throws IllegalArgumentException {
        // POI allows user to hide all sheets ...
        if (hidden != 0
                && SpreadsheetFactory.getNumberOfVisibleSheets(workbook) == 1) {
            throw new IllegalArgumentException(
                    "At least one sheet should be always visible.");
        }
        boolean isHidden = workbook.isSheetHidden(sheetPOIIndex);
        boolean isVeryHidden = workbook.isSheetVeryHidden(sheetPOIIndex);
        int activeSheetIndex = workbook.getActiveSheetIndex();
        workbook.setSheetHidden(sheetPOIIndex, hidden);

        // skip component reload if "nothing changed"
        if (hidden == 0 && (isHidden || isVeryHidden) || hidden != 0
                && !(isHidden && isVeryHidden)) {
            if (sheetPOIIndex != activeSheetIndex) {
                reloadSheets();
                getState().sheetIndex = getSpreadsheetSheetIndex(activeSheetIndex) + 1;
            } else { // the active sheet can be only set as hidden
                int oldVisibleSheetIndex = getState().sheetIndex - 1;
                if (hidden != 0
                        && activeSheetIndex == (workbook.getNumberOfSheets() - 1)) {
                    // hiding the active sheet, and it was the last sheet
                    oldVisibleSheetIndex--;
                }
                int newActiveSheetIndex = getVisibleSheetPOIIndex(oldVisibleSheetIndex);
                workbook.setActiveSheet(newActiveSheetIndex);
                reloadActiveSheetData();
                SpreadsheetFactory.reloadSpreadsheetData(this, workbook,
                        getActiveSheet());
            }
        }
    }

    /**
     * Returns an array containing the currently visible sheets' names. Does not
     * contain hidden or very hidden sheets.
     * <p>
     * To get all of the current {@link Workbook}'s sheet names, you access the
     * Apache POI API with {@link #getWorkbook()}.
     * 
     * @return the currently visible sheets' names
     */
    public String[] getVisibleSheetNames() {
        final String[] names = getState(false).sheetNames;
        return Arrays.copyOf(names, names.length);
    }

    /**
     * 
     * @param sheetIndex
     *            0-based, visible sheets index
     * @param sheetName
     *            not null, nor empty nor longer than 31 characters. must be
     *            unique
     * @throws IllegalArgumentException
     *             if the index is invalid, or if the sheet name invalid see
     *             {@link WorkbookUtil#validateSheetName(String)}
     */
    public void setSheetName(int sheetIndex, String sheetName)
            throws IllegalArgumentException {
        if (sheetIndex < 0 || sheetIndex >= getState().sheetNames.length) {
            throw new IllegalArgumentException("Invalid Sheet index given.");
        }
        int poiSheetIndex = getVisibleSheetPOIIndex(sheetIndex);
        setSheetNameWithPOIIndex(poiSheetIndex, sheetName);
    }

    /**
     * 
     * @param sheetIndex
     *            0-based, Apache POI based index (includes hidden & very hidden
     *            sheets)
     * @param sheetName
     *            not null, empty nor longer than 31 characters. must be unique
     * @throws IllegalArgumentException
     *             if the index is invalid, or if the sheet name invalid see
     *             {@link WorkbookUtil#validateSheetName(String)}
     * 
     */
    public void setSheetNameWithPOIIndex(int sheetIndex, String sheetName)
            throws IllegalArgumentException {
        if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException("Invalid POI Sheet index given.");
        }
        if (sheetName == null || sheetName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sheet Name cannot be null or an empty String, or contain backslash \\.");
        }
        if (isSheetNameExisting(sheetName)) {
            throw new IllegalArgumentException(
                    "Sheet name must be unique within the workbook.");
        }
        workbook.setSheetName(sheetIndex, sheetName);
        if (!workbook.isSheetVeryHidden(sheetIndex)) {
            int ourIndex = getSpreadsheetSheetIndex(sheetIndex);
            getState().sheetNames[ourIndex] = sheetName;
        }
    }

    /**
     * Sets the protection enabled as well as the password for the sheet at the
     * given index. <code>null</code> password removes the protection.
     * 
     * @param sheetPOIIndex
     *            0-based, the POI index (contains hidden and very hidden
     *            sheets) of the sheet to protect
     * @param password
     *            to set for protection. Pass <code>null</code> to remove
     *            protection
     */
    public void setSheetProtected(int sheetPOIIndex, String password) {
        if (sheetPOIIndex < 0 || sheetPOIIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException("Invalid POI Sheet index given.");
        }
        workbook.getSheetAt(sheetPOIIndex).protectSheet(password);
        getState().sheetProtected = getActiveSheet().getProtect();
        // if the currently active sheet was protected, the protection for the
        // currently selected cell might have changed
        if (sheetPOIIndex == workbook.getActiveSheetIndex()) {
            loadCustomComponents();
            handleCellSelection(selectedCellReference.getCol() + 1,
                    selectedCellReference.getRow() + 1);
        }
    }

    /**
     * Sets the protection enabled as well as the password for the currently
     * active sheet. <code>null</code> password removes the protection.
     * 
     * @param password
     *            to set for protection. Pass <code>null</code> to remove
     *            protection
     */
    public void setActiveSheetProtected(String password) {
        setSheetProtected(workbook.getActiveSheetIndex(), password);
    }

    /**
     * Creates a new sheet as the last sheet, sets it as the active sheet.
     * 
     * If the sheetName given is null, then the sheet name is automatically
     * generated by Apache POI in {@link Workbook#createSheet()}.
     * 
     * @param sheetName
     *            can be null, but not empty nor longer than 31 characters. must
     *            be unique
     * @param rows
     *            number of rows the sheet should have
     * @param columns
     *            number of columns the sheet should have
     * @throws IllegalArgumentException
     *             if the index is invalid, or if the sheet name is null or
     *             empty or the sheet already contains a sheet by that name
     */
    public void createNewSheet(String sheetName, int rows, int columns)
            throws IllegalArgumentException {
        if (sheetName != null && sheetName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sheet Name cannot be an empty String.");
        }
        if (sheetName != null && sheetName.length() > 31) {
            throw new IllegalArgumentException(
                    "Sheet Name cannot be longer than 31 characters");
        }
        if (sheetName != null && isSheetNameExisting(sheetName)) {
            throw new IllegalArgumentException(
                    "Sheet name must be unique within the workbook.");
        }
        final Sheet previousSheet = getActiveSheet();
        SpreadsheetFactory
                .addNewSheet(this, workbook, sheetName, rows, columns);
        fireSelectedSheetChangeEvent(previousSheet, getActiveSheet());
    }

    /**
     * Deletes the sheet. A workbook must contain at least one visible sheet.
     * 
     * @param sheetIndex
     *            0-based, max value {@link Workbook#getNumberOfSheets()} -1
     * @throws IllegalArgumentException
     *             in case there is only one visible sheet, or if index is
     *             invalid
     */
    public void deleteSheetWithPOIIndex(int pOISheetIndex)
            throws IllegalArgumentException {
        if (getNumberOfVisibleSheets() < 2) {
            throw new IllegalArgumentException(
                    "A workbook must contain at least one visible worksheet");
        }
        int removedVisibleIndex = getSpreadsheetSheetIndex(pOISheetIndex);
        workbook.removeSheetAt(pOISheetIndex);

        // POI doesn't seem to shift the active sheet index ...
        int oldIndex = getState().sheetIndex - 1;
        if (removedVisibleIndex <= oldIndex) { // removed before current
            if (oldIndex == (getNumberOfVisibleSheets())) {
                // need to shift index backwards if the current sheet is last
                workbook.setActiveSheet(getVisibleSheetPOIIndex(oldIndex - 1));
            } else {
                workbook.setActiveSheet(getVisibleSheetPOIIndex(oldIndex));
            }
        }
        // need to reload everything because there is a ALWAYS chance that the
        // removed sheet effects the currently visible sheet (via cell formulas
        // etc.)
        reloadActiveSheetData();
    }

    /**
     * Deletes the sheet. A workbook must contain at least one visible sheet.
     * 
     * @param sheetIndex
     *            0-based, max value {@link #getNumberOfVisibleSheets()} -1
     * @throws IllegalArgumentException
     *             in case there is only one visible sheet, or if index is
     *             invalid
     */
    public void deleteSheet(int sheetIndex) throws IllegalArgumentException {
        if (getNumberOfVisibleSheets() < 2) {
            throw new IllegalArgumentException(
                    "A workbook must contain at least one visible worksheet");
        }
        deleteSheetWithPOIIndex(getVisibleSheetPOIIndex(sheetIndex));
    }

    /**
     * Returns the number of currently visible sheets in the component. Doesn't
     * include the hidden or very hidden sheets in the POI model.
     * 
     * @return
     */
    public int getNumberOfVisibleSheets() {
        if (getState().sheetNames != null) {
            return getState().sheetNames.length;
        } else {
            return 0;
        }
    }

    /**
     * Returns the total number of sheets in the workbook (includes hidden and
     * very hidden sheets).
     * 
     * @return total number of sheets in the workbook
     */
    public int getNumberOfSheets() {
        return workbook.getNumberOfSheets();
    }

    private boolean isSheetNameExisting(String sheetName) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (workbook.getSheetName(i).equals(sheetName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the currently active sheet's index among the visible sheets (no
     * hidden or very hidden sheets).
     * 
     * @return 0-based index
     */
    public int getActiveSheetIndex() {
        return getState(false).sheetIndex - 1;
    }

    /**
     * Returns the currently active sheet's index among all sheets (including
     * hidden and very hidden sheets).
     * 
     * @return 0-based index
     */
    public int getActiveSheetPOIIndex() {
        return getVisibleSheetPOIIndex(getState(false).sheetIndex - 1);
    }

    /**
     * Sets the currently active sheet within the sheets that are visible.
     * 
     * @param sheetIndex
     *            0-based visible sheets index
     * @throws IllegalArgumentException
     *             if invalid index
     */
    public void setActiveSheetIndex(int sheetIndex)
            throws IllegalArgumentException {
        if (sheetIndex < 0 || sheetIndex >= getState().sheetNames.length) {
            throw new IllegalArgumentException("Invalid Sheet index given.");
        }
        int POISheetIndex = getVisibleSheetPOIIndex(sheetIndex);
        setActiveSheetWithPOIIndex(POISheetIndex);
    }

    /**
     * Sets the currently active sheet. The sheet at the given index should be
     * visible (not hidden or very hidden), or
     * 
     * @param sheetIndex
     *            0-based POI sheets index (all sheets)
     * @throws IllegalArgumentException
     *             if invalid index, or if the sheet is hidden or very hidden.
     */
    public void setActiveSheetWithPOIIndex(int sheetIndex)
            throws IllegalArgumentException {
        if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException("Invalid POI Sheet index given.");
        }
        if (workbook.isSheetHidden(sheetIndex)
                || workbook.isSheetVeryHidden(sheetIndex)) {
            throw new IllegalArgumentException(
                    "Cannot set a hidden or very hidden sheet as the active sheet. Given index: "
                            + sheetIndex);
        }
        workbook.setActiveSheet(sheetIndex);
        reloadActiveSheetData();
        SpreadsheetFactory.reloadSpreadsheetData(this, workbook,
                workbook.getSheetAt(sheetIndex));
    }

    /**
     * Get the number of columns in the spreadsheet, or if
     * {@link #setMaximumColumns(int)} has been used, the current number of
     * columns the component shows (not the amount of columns in the actual
     * sheet).
     */
    public int getCols() {
        return getState().cols;
    }

    /**
     * Get the number of rows in the spreadsheet, or if
     * {@link #setMaximumRows(int)} has been used, the current number of rows
     * the component shows (not the amout of rows in the actual sheet).
     */
    public int getRows() {
        return getState().rows;
    }

    /**
     * 
     * @return the formatter for this sheet
     */
    public DataFormatter getDataFormatter() {
        return formatter;
    }

    /**
     * Returns Cell. If the cell is updated, call
     * {@link #markCellAsUpdated(Cell)} AFTER ALL UPDATES (value, type,
     * formatting or style) to mark the cell as "dirty" and when all updates are
     * done, remember to call {@link #updateMarkedCells()} to make sure client
     * side is updated.
     * 
     * @param row
     *            0-based
     * @param col
     *            0-based
     * @return the cell or null if not defined
     */
    public Cell getCell(int row, int col) {
        Row r = workbook.getSheetAt(workbook.getActiveSheetIndex()).getRow(row);
        if (r != null) {
            return r.getCell(col);
        } else {
            return null;
        }
    }

    /**
     * Deletes the cell from the sheet.
     * 
     * No need to call mark cell as updated with
     * {@link #markCellAsUpdated(Cell)}.
     * 
     * After editing is done, call {@link #updateMarkedCells()} to update the
     * values to the client.
     * 
     * This really deletes the cell, instead of just making it's value blank.
     * 
     * @param row
     *            0-based
     * @param col
     *            0-based
     */
    public void deleteCell(int row, int col) {
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        final Cell cell = activeSheet.getRow(row).getCell(col);
        cell.setCellStyle(null);
        styler.cellStyleUpdated(cell, true);
        activeSheet.getRow(row).removeCell(cell);
        evaluator.notifyDeleteCell(cell);
        markedCells.add(toKey(cell));
    }

    /**
     * Marks the cell as updated. Should be called when the cell
     * value/formatting/style/etc. updating is done.
     * 
     * When all cell updating is done, remember to call
     * {@link #updateMarkedCells()} to publish the updates.
     * 
     * @param cellStyleUpdated
     *            has the cell style changed
     * 
     * @param cell
     */
    public void markCellAsUpdated(Cell cell, boolean cellStyleUpdated) {
        evaluator.notifyUpdateCell(cell);
        markedCells.add(toKey(cell));
        if (cellStyleUpdated) {
            styler.cellStyleUpdated(cell, true);
        }
    }

    /**
     * Updates the content of the cells that have been marked for update with
     * {@link #markCellAsUpdated(Cell, boolean)}.
     * <p>
     * Does NOT update custom components (editors / always visible) for the
     * cells. For that, use {@link #reloadVisibleCellContents()}
     */
    public void updateMarkedCells() {
        updateMarkedCellValues(firstColumn, lastColumn, firstRow, lastRow);
        // if the selected cell is of type formula, there is a change that the
        // formula has been changed.
        handleCellSelection(selectedCellReference.getCol() + 1,
                selectedCellReference.getRow() + 1);
    }

    /**
     * Create a Formula type cell with the given formula.
     * 
     * After all editing is done, call {@link #updateMarkedCells()} or
     * {@link #updatedAndRecalculateAllCellValues()} to make sure client side is
     * updated. No need to call {@link #markCellAsUpdated(Cell)}, UNLESS the
     * cell style is changed.
     * 
     * @param row
     *            0-based
     * @param col
     *            0-based
     * @param formula
     *            the formula (should NOT start with "=")
     * @return the created cell
     * @throws IllegalArgumentException
     *             if columnIndex < 0 or greater than the maximum number of
     *             supported columns (255 for *.xls, 1048576 for *.xlsx)
     */
    public Cell createFormulaCell(int row, int col, String formula)
            throws IllegalArgumentException {
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        Row r = activeSheet.getRow(row);
        if (r == null) {
            r = activeSheet.createRow(row);
        }
        Cell cell = r.getCell(col);
        if (cell == null) {
            cell = r.createCell(col, Cell.CELL_TYPE_FORMULA);
        } else {
            final String key = toKey(col + 1, row + 1);
            if (sentCells.remove(key)) {
                sentFormulaCells.remove(key);
            }
            cell.setCellType(Cell.CELL_TYPE_FORMULA);
        }
        cell.setCellFormula(formula);
        evaluator.notifySetFormula(cell);
        return cell;
    }

    /**
     * Create a new cell (or replace existing) with the given value, the type of
     * the value parameter will define the type of the cell. Thus value may be
     * of types: Boolean, Calendar, Date, Double or String. The default type
     * will be String, value of ({@link #toString()} will be given as the cell
     * value.
     * 
     * For formula cells, use {@link #createFormulaCell(int, int, String)}.
     * 
     * After all editing is done, call {@link #updateMarkedCells()} or
     * {@link #updatedAndRecalculateAllCellValues()} to make sure client side is
     * updated. No need to call {@link #markCellAsUpdated(Cell)}, UNLESS the
     * cell style is changed.
     * 
     * @param row
     *            0-based
     * @param col
     *            0-based
     * @param value
     *            object representing the type of the Cell
     * @return the created cell
     * @throws IllegalArgumentException
     *             if columnIndex < 0 or greater than the maximum number of
     *             supported columns (255 for *.xls, 1048576 for *.xlsx)
     */
    public Cell createCell(int row, int col, Object value)
            throws IllegalArgumentException {
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        Row r = activeSheet.getRow(row);
        if (r == null) {
            r = activeSheet.createRow(row);
        }
        Cell cell = r.getCell(col);
        if (cell == null) {
            cell = r.createCell(col);
        } else {
            final String key = toKey(col + 1, row + 1);
            if (sentCells.remove(key)) {
                sentFormulaCells.remove(key);
            }
        }
        if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if (value instanceof Calendar) {
            cell.setCellValue((Calendar) value);
        } else {
            cell.setCellValue(value.toString());
        }
        evaluator.notifyUpdateCell(cell);
        markedCells.add(toKey(cell));
        return cell;
    }

    /**
     * Forces recalculation and update to client side for all of the sheet's
     * cells. DOES NOT UPDATE STYLES
     */
    public void updatedAndRecalculateAllCellValues() {
        evaluator.clearAllCachedResultValues();
        updateMarkedCellValues(1, getCols(), 1, getRows());

        // if the selected cell is of type formula, there is a change that the
        // formula has been changed.
        final Row row = workbook.getSheetAt(workbook.getActiveSheetIndex())
                .getRow(selectedCellReference.getRow());
        if (row != null) {
            Cell cell = row.getCell(selectedCellReference.getCol());
            if (cell != null) {
                String value = "";
                boolean formula = cell.getCellType() == Cell.CELL_TYPE_FORMULA;
                if (!isCellHidden(cell)) {
                    if (formula) {
                        value = cell.getCellFormula();
                    } else {
                        value = getCellValue(cell);
                    }
                }
                getRpcProxy(SpreadsheetClientRpc.class).showCellValue(value,
                        cell.getColumnIndex() + 1, cell.getRowIndex() + 1,
                        formula, isCellLocked(cell));
            } else {
                getRpcProxy(SpreadsheetClientRpc.class).showCellValue("",
                        selectedCellReference.getCol() + 1,
                        row.getRowNum() + 1, false, isCellLocked(cell));
            }
        } else {
            getRpcProxy(SpreadsheetClientRpc.class).showCellValue("",
                    selectedCellReference.getCol() + 1,
                    selectedCellReference.getRow() + 1, false,
                    isSheetProtected());
        }
    }

    /**
     * Set the number of columns shown for the current sheet. Any unset cells
     * are left empty. Any cells outside the given columns are hidden. Does not
     * update the actual POI-based model!
     * 
     * The default value will be the actual size of the sheet (from POI).
     * 
     */
    public void setMaximumColumns(int cols) {
        if (getState().cols != cols) {
            getState().cols = cols;
        }
    }

    /**
     * Set the number of rows shown for the current sheet. Any unset cells are
     * left empty. Any cells outside the given rows are hidden. Does not update
     * the actual POI-based model!
     * 
     * The default value will be the actual size of the sheet (from POI).
     */
    public void setMaximumRows(int rows) {
        if (getState().rows != rows) {
            getState().rows = rows;
        }
    }

    /**
     * Does {@link #setMaximumColumns(int)} & {@link #setMaximumRows(int)} in
     * one method.
     * 
     * @param cols
     * @param rows
     */
    public void setSheetMaximumSize(int cols, int rows) {
        getState().cols = cols;
        getState().rows = rows;
    }

    /**
     * This is the value that the component uses, it is derived from the active
     * sheets ({@link #getActiveSheet()}) default column width (Sheet
     * {@link #getDefaultColumnWidth()}).
     * 
     * @return the default column width in PX
     */
    public int getDefaultColumnWidth() {
        return getState().defColW;
    }

    /**
     * Set the default column width in pixels that the component uses, this
     * doesn't change the default column width of the underlying sheet, returned
     * by {@link #getActiveSheet()} and {@link Sheet#getDefaultColumnWidth()}.
     * 
     * @param widthPX
     */
    public void setDefaultColumnWidth(int widthPX) {
        if (widthPX <= 0) {
            throw new IllegalArgumentException(
                    "Default column width must be over 0, given value: "
                            + widthPX);
        }
        getState().defColW = widthPX;
    }

    /**
     * This is the default row height in points, by default it should be the
     * same as {@link Sheet#getDefaultRowHeightInPoints()} for current sheet
     * {@link #getActiveSheet()}.
     * 
     * @return
     */
    public float getDefaultRowHeightInPoints() {
        return getState().defRowH;
    }

    /**
     * Set the default row height in points for the component and the currently
     * active sheet, returned by {@link #getActiveSheet()}.
     * 
     * @param heightPT
     */
    public void setDefaultRowHeightInPoints(float heightPT) {
        if (heightPT <= 0.0f) {
            throw new IllegalArgumentException(
                    "Default row height must be over 0, given value: "
                            + heightPT);
        }
        getActiveSheet().setDefaultRowHeightInPoints(heightPT);
        getState().defRowH = heightPT;
    }

    /**
     * Sets the column to automatically adjust the column width to fit to the
     * largest cell content. This is a POI feature, and is ment to be called
     * after all the data for that column has been written. See
     * {@link Sheet#autoSizeColumn(int)}.
     * <p>
     * This does not take into account cells that have custom Vaadin components
     * inside them.
     * 
     * @param columnIndex
     *            0-based
     */
    public void autofitColumn(int columnIndex) {
        final Sheet activeSheet = getActiveSheet();
        activeSheet.autoSizeColumn(columnIndex);
        getState().colW[columnIndex] = ExcelToHtmlUtils
                .getColumnWidthInPx(activeSheet.getColumnWidth(columnIndex));
        if (sheetImages != null) {
            reloadImageSizesFromPOI = true;
            loadImages();
        }
    }

    /**
     * See {@link Sheet#shiftRows(int, int, int)}.
     * <p>
     * If you are adding / deleting rows, you might want to change the number of
     * visible rows rendered {@link #getRows()} with
     * {@link #setMaximumRows(int)}. see
     * {@link InsertNewRowAction#executeActionOnHeader(Spreadsheet, CellRangeAddress)}
     * or
     * {@link DeleteRowAction#executeActionOnHeader(Spreadsheet, CellRangeAddress)}
     * for example.
     * 
     * @param startRow
     *            0-based
     * @param endRow
     *            0-based
     * @param n
     */
    public void shiftRows(int startRow, int endRow, int n) {
        shiftRows(startRow, endRow, n, false, false);
    }

    /**
     * See {@link Sheet#shiftRows(int, int, int, boolean, boolean)}.
     * <p>
     * If you are adding / deleting rows, you might want to change the number of
     * visible rows rendered {@link #getRows()} with
     * {@link #setMaximumRows(int)}.
     * 
     * @param startRow
     * @param endRow
     * @param n
     * @param copyRowHeight
     * @param resetOriginalRowHeight
     */
    public void shiftRows(int startRow, int endRow, int n,
            boolean copyRowHeight, boolean resetOriginalRowHeight) {
        Sheet sheet = getActiveSheet();
        sheet.shiftRows(startRow, endRow, n, copyRowHeight,
                resetOriginalRowHeight);
        // need to resend the cell values to client
        // remove all cached cell data that is now empty
        int start = n < 0 ? endRow + n + 1 : startRow;
        int end = n < 0 ? endRow : startRow + n - 1;
        updateDeletedRowsInClientCache(start + 1, end + 1);
        int firstEffectedRow = n < 0 ? startRow + n : startRow;
        int lastEffectedRow = n < 0 ? endRow : endRow + n;
        if (copyRowHeight || resetOriginalRowHeight) {
            // might need to increase the size of the row heights array
            int oldLength = getState(false).rowH.length;
            int neededLength = endRow + n + 1;
            if (n > 0 && oldLength < neededLength) {
                getState().rowH = Arrays.copyOf(getState().rowH, neededLength);
            }
            for (int i = firstEffectedRow; i <= lastEffectedRow; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    if (row.getZeroHeight()) {
                        getState().rowH[i] = 0f;
                    } else {
                        getState().rowH[i] = row.getHeightInPoints();
                    }
                } else {
                    getState().rowH[i] = sheet.getDefaultRowHeightInPoints();
                }
            }
        }
        updateMergedRegions();
        if (sheetImages != null) {
            reloadImageSizesFromPOI = true;
        }
        updateMarkedCellValues(1, getState(false).cols, startRow + n + 1,
                endRow + n + 1);
        // need to shift the cell styles, clear and update
        // need to go -1 and +1 because of shifted borders..
        final ArrayList<Cell> cellsToUpdate = new ArrayList<Cell>();
        for (int r = (firstEffectedRow - 1); r <= (lastEffectedRow + 1); r++) {
            if (r < 0) {
                r = 0;
            }
            Row row = sheet.getRow(r);
            final Integer rowIndex = new Integer(r + 1);
            if (row == null) {
                if (getState(false).hiddenRowIndexes.contains(rowIndex)) {
                    getState().hiddenRowIndexes.remove(rowIndex);
                }
                for (int c = 0; c < getState().cols; c++) {
                    styler.clearCellStyle(c, r);
                }
            } else {
                if (row.getZeroHeight()) {
                    getState().hiddenRowIndexes.add(rowIndex);
                } else if (getState(false).hiddenRowIndexes.contains(rowIndex)) {
                    getState().hiddenRowIndexes.remove(rowIndex);
                }
                for (int c = 0; c < getState().cols; c++) {
                    Cell cell = row.getCell(c);
                    if (cell == null) {
                        styler.clearCellStyle(c, r);
                    } else {
                        cellsToUpdate.add(cell);
                    }
                }
            }
        }
        for (Cell cell : cellsToUpdate) {
            styler.cellStyleUpdated(cell, false);
        }
        styler.loadCustomBorderStylesToState();
        if (selectedCellReference.getRow() >= firstEffectedRow
                && selectedCellReference.getRow() <= lastEffectedRow) {
            handleCellAddressChange(selectedCellReference.formatAsString());
            // handleCellSelection(selectedCellReference.getCol() + 1,
            // selectedCellReference.getRow() + 1);
        }
    }

    private void updateMergedRegions() {
        int regions = getActiveSheet().getNumMergedRegions();
        if (regions > 0) {
            getState().mergedRegions = new ArrayList<MergedRegion>();
            for (int i = 0; i < regions; i++) {
                final CellRangeAddress region = getActiveSheet()
                        .getMergedRegion(i);
                try {
                    final MergedRegion mergedRegion = new MergedRegion();
                    mergedRegion.col1 = region.getFirstColumn() + 1;
                    mergedRegion.col2 = region.getLastColumn() + 1;
                    mergedRegion.row1 = region.getFirstRow() + 1;
                    mergedRegion.row2 = region.getLastRow() + 1;
                    mergedRegion.id = mergedRegionCounter++;
                    getState().mergedRegions.add(i, mergedRegion);
                } catch (IndexOutOfBoundsException ioobe) {
                    createMergedRegionIntoSheet(region);
                }
            }
            while (regions < getState(false).mergedRegions.size()) {
                getState().mergedRegions.remove(getState(false).mergedRegions
                        .size() - 1);
            }
        } else {
            getState().mergedRegions = null;
        }
    }

    /**
     * Removes rows. See {@link Sheet#removeRow(Row)}. Removes all row content,
     * deletes cell and resets size. Does not shift rows up (!) - use
     * {@link #shiftRows(int, int, int, boolean, boolean)} for that.
     * 
     * @param startRow
     *            0-based
     * @param endRow
     *            0-based
     */
    public void removeRows(int startRow, int endRow) {
        Sheet sheet = getActiveSheet();
        for (int i = startRow; i <= endRow; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                getActiveSheet().removeRow(row);
            }
        }
        for (int i = startRow; i <= endRow; i++) {
            getState(false).rowH[i] = sheet.getDefaultRowHeightInPoints();
        }
        updateMergedRegions();
        updateDeletedRowsInClientCache(startRow + 1, endRow + 1);
        if (sheetImages != null) {
            reloadImageSizesFromPOI = true;
        }
        updateMarkedCellValues(0, 0, 0, 0);
        if (selectedCellReference.getRow() >= startRow
                && selectedCellReference.getRow() <= endRow) {
            handleCellSelection(selectedCellReference.getCol() + 1,
                    selectedCellReference.getRow() + 1);
        }

    }

    /**
     * Makes sure the next {@link #updateMarkedCellValues(int, int, int, int)}
     * call will clear all removed rows from client cache.
     * 
     * @param startRow
     *            1-based
     * @param endRow
     *            1-based
     */
    private void updateDeletedRowsInClientCache(int startRow, int endRow) {
        for (int i = startRow; i <= endRow; i++) {
            String rowKey = "row" + i;
            boolean rowIsRemoved = false;
            for (Iterator<String> iterator = sentCells.iterator(); iterator
                    .hasNext();) {
                String key = iterator.next();
                if (key.endsWith(rowKey)) {
                    iterator.remove();
                    removedCells.add(key);
                    rowIsRemoved = true;
                }
            }
            for (Iterator<String> iterator = sentFormulaCells.iterator(); iterator
                    .hasNext();) {
                String key = iterator.next();
                if (key.endsWith(rowKey)) {
                    iterator.remove();
                    removedCells.add(key);
                    rowIsRemoved = true;
                }
            }
            if (rowIsRemoved) {
                removedCells.add(Integer.toString(i));
            }
        }
    }

    /**
     * Merge cells. See {@link Sheet#addMergedRegion(CellRangeAddress)}.
     * <p>
     * Parameters 0-based
     * 
     * @param col1
     * @param col2
     * @param row1
     * @param row2
     */
    public void addMergedRegion(int col1, int col2, int row1, int row2) {
        addMergedRegion(new CellRangeAddress(row1, row2, col1, col2));
    }

    /**
     * Merge cells. See {@link Sheet#addMergedRegion(CellRangeAddress)}.
     * <p>
     * If another existing merged region is completely inside the given range,
     * it is removed. If another existing region either encloses or overlaps the
     * given range, an error is thrown. See
     * {@link CellRangeUtil#intersect(CellRangeAddress, CellRangeAddress)}.
     * <p>
     * Note: POI doesn't seem to update the cells that are "removed" - the
     * values for those cells are still existing and being used in possible
     * formulas. If you need to make sure those values are removed, just delete
     * the cells before creating the merged region.
     * <p>
     * If the added region effects the currently selected cell, a new
     * {@link SelectionChangeEvent} is fired.
     * 
     * @param region
     * @throws IllegalArgumentException
     *             if the given region overlaps or encloses another existing
     *             region within the sheet
     */
    public void addMergedRegion(CellRangeAddress region)
            throws IllegalArgumentException {
        final Sheet sheet = getActiveSheet();
        // need to check if there are merged regions already inside the given
        // range, otherwise very bad inconsistencies appear.
        int index = 0;
        while (index < sheet.getNumMergedRegions()) {
            CellRangeAddress existingRegion = sheet.getMergedRegion(index);
            int intersect = CellRangeUtil.intersect(region, existingRegion);
            if (intersect == CellRangeUtil.INSIDE) {
                deleteMergedRegion(index);
            } else if (intersect == CellRangeUtil.OVERLAP
                    || intersect == CellRangeUtil.ENCLOSES) {
                throw new IllegalArgumentException("An existing region "
                        + existingRegion
                        + " "
                        + (intersect == CellRangeUtil.OVERLAP ? "overlaps "
                                : "encloses ") + "the given region " + region);
            } else {
                index++;
            }
        }
        createMergedRegionIntoSheet(region);
        // update selection if the new merged region effects selected cell
        boolean fire = false;
        if (region.isInRange(selectedCellReference.getRow(),
                selectedCellReference.getCol())) {
            if (selectedCellReference.getCol() != region.getFirstColumn()
                    || selectedCellReference.getRow() != region.getFirstRow()) {
                handleCellAddressChange(region.getFirstColumn() + 1,
                        region.getFirstRow() + 1);
            }
            selectedCellReference = new CellReference(region.getFirstRow(),
                    region.getFirstColumn());
            fire = true;
        }
        for (Iterator<CellRangeAddress> i = cellRangeAddresses.iterator(); i
                .hasNext();) {
            CellRangeAddress cra = i.next();
            if (CellRangeUtil.contains(region, cra)) {
                i.remove();
                fire = true;
            }
        }
        for (Iterator<CellReference> i = individualSelectedCells.iterator(); i
                .hasNext();) {
            CellReference cr = i.next();
            if (region.isInRange(cr.getRow(), cr.getCol())) {
                i.remove();
                fire = true;
            }
        }
        if (fire) {
            fireNewSelectionChangeEvent();
        }
    }

    private void createMergedRegionIntoSheet(CellRangeAddress region) {
        Sheet sheet = getActiveSheet();
        int addMergedRegionIndex = sheet.addMergedRegion(region);
        MergedRegion mergedRegion = new MergedRegion();
        mergedRegion.col1 = region.getFirstColumn() + 1;
        mergedRegion.col2 = region.getLastColumn() + 1;
        mergedRegion.row1 = region.getFirstRow() + 1;
        mergedRegion.row2 = region.getLastRow() + 1;
        mergedRegion.id = mergedRegionCounter++;
        if (getState().mergedRegions == null) {
            getState().mergedRegions = new ArrayList<MergedRegion>();
        }
        getState().mergedRegions.add(addMergedRegionIndex - 1, mergedRegion);
        // update the style & data for the region cells, effects region + 1
        // FIXME POI doesn't seem to care that the other cells inside the merged
        // region should be removed; the values those cells have are still used
        // in formulas..
        for (int r = mergedRegion.row1; r <= (mergedRegion.row2 + 1); r++) {
            Row row = sheet.getRow(r - 1);
            for (int c = mergedRegion.col1; c <= (mergedRegion.col2 + 1); c++) {
                if (row != null) {
                    Cell cell = row.getCell(c - 1);
                    if (cell != null) {
                        styler.cellStyleUpdated(cell, false);
                    }
                }
                if ((c != mergedRegion.col1 || r != mergedRegion.row1)
                        && c <= mergedRegion.col2 && r <= mergedRegion.row2) {
                    String key = toKey(c, r);
                    removedCells.add(key);
                    if (!sentCells.remove(key)) {
                        sentFormulaCells.remove(key);
                    }
                }
            }
        }
        styler.loadCustomBorderStylesToState();
        updateMarkedCellValues(0, 0, 0, 0);
    }

    /**
     * Removes a merged region with the given index. Current merged regions can
     * be inspected within the currently active sheet with
     * {@link #getActiveSheet()} and {@link Sheet#getMergedRegion(int)} and
     * {@link Sheet#getNumMergedRegions()}.
     * <p>
     * Note that in POI after removing a merged region at index n, all regions
     * added after the removed region will get a new index (index-1).
     * <p>
     * If the removed region effects the currently selected cell, a new
     * {@link SelectionChangeEvent} is fired.
     * 
     * @param index
     *            0-based position in the POI merged region array
     */
    public void removeMergedRegion(int index) {
        final CellRangeAddress removedRegion = getActiveSheet()
                .getMergedRegion(index);
        deleteMergedRegion(index);
        updateMarkedCellValues(0, 0, 0, 0);
        // update selection if removed region overlaps
        if (removedRegion.isInRange(selectedCellReference.getRow(),
                selectedCellReference.getCol())) {
            cellRangeAddresses.add(removedRegion);
            fireNewSelectionChangeEvent();
        }
    }

    private void deleteMergedRegion(int index) {
        final Sheet sheet = getActiveSheet();
        sheet.removeMergedRegion(index);
        MergedRegion mergedRegion = getState().mergedRegions.remove(index);
        // update the style for the region cells, effects region + 1 row&col
        for (int r = mergedRegion.row1; r <= (mergedRegion.row2 + 1); r++) {
            Row row = sheet.getRow(r - 1);
            if (row != null) {
                for (int c = mergedRegion.col1; c <= (mergedRegion.col2 + 1); c++) {
                    Cell cell = row.getCell(c - 1);
                    if (cell != null) {
                        styler.cellStyleUpdated(cell, false);
                        markedCells.add(toKey(cell));
                    } else {
                        styler.clearCellStyle(c, r);
                    }
                }
            }
        }
        styler.loadCustomBorderStylesToState();
    }

    /**
     * Discards all current merged regions for the sheet and reloads them from
     * POI model.
     * <p>
     * This can be used if you want to add / remove multiple merged regions
     * directly from the POI model and need to update the component.
     * 
     * Note that you must also make sure that possible styles for the merged
     * regions are updated, if those were modified, by calling
     * {@link #reloadActiveSheetStyles()}.
     */
    public void reloadAllMergedRegions() {
        SpreadsheetFactory.loadMergedRegions(this, getActiveSheet());
    }

    /**
     * Reloads the active sheets styles.
     */
    public void reloadActiveSheetStyles() {
        styler.reloadActiveSheetCellStyles();
    }

    /**
     * Hides or unhides the column, see
     * {@link Sheet#setColumnHidden(int, boolean)}.
     * 
     * @param columnIndex
     *            0-based
     * @param hidden
     */
    public void setColumnHidden(int columnIndex, boolean hidden) {
        getActiveSheet().setColumnHidden(columnIndex, hidden);
        if (hidden && !getState().hiddenColumnIndexes.contains(columnIndex + 1)) {
            getState().hiddenColumnIndexes.add(columnIndex + 1);
            getState().colW[columnIndex] = 0;
        } else if (!hidden
                && getState().hiddenColumnIndexes.contains(columnIndex + 1)) {
            getState().hiddenColumnIndexes
                    .remove(getState().hiddenColumnIndexes
                            .indexOf(columnIndex + 1));
            getState().colW[columnIndex] = ExcelToHtmlUtils
                    .getColumnWidthInPx(getActiveSheet().getColumnWidth(
                            columnIndex));
        }
        if (sheetImages != null) {
            reloadImageSizesFromPOI = true;
            loadImages();
        }
    }

    /**
     * See {@link Sheet#isColumnHidden(int)}.
     * 
     * @param columnIndex
     *            0-based
     * @return
     */
    public boolean isColumnHidden(int columnIndex) {
        return getActiveSheet().isColumnHidden(columnIndex);
    }

    /**
     * Hides or unhides the row, see {@link Row#setZeroHeight(boolean)}.
     * 
     * @param rowIndex
     *            0-based
     * @param hidden
     */
    public void setRowHidden(int rowIndex, boolean hidden) {
        final Sheet activeSheet = getActiveSheet();
        Row row = activeSheet.getRow(rowIndex);
        if (row == null) {
            row = activeSheet.createRow(rowIndex);
        }
        row.setZeroHeight(hidden);
        if (hidden && !getState().hiddenRowIndexes.contains(rowIndex + 1)) {
            getState().hiddenRowIndexes.add(rowIndex + 1);
            getState().rowH[rowIndex] = 0.0F;
        } else if (!hidden
                && getState().hiddenRowIndexes.contains(rowIndex + 1)) {
            getState().hiddenRowIndexes.remove(getState().hiddenRowIndexes
                    .indexOf(rowIndex + 1));
            getState().rowH[rowIndex] = row.getHeightInPoints();
        }
        if (sheetImages != null) {
            reloadImageSizesFromPOI = true;
            loadImages();
        }
    }

    /**
     * Row is hidden when it has zero height, see {@link Row#getZeroHeight()}.
     * 
     * @param rowIndex
     *            0-based
     * @return
     */
    public boolean isRowHidden(int rowIndex) {
        Row row = getActiveSheet().getRow(rowIndex);
        return row == null ? false : row.getZeroHeight();
    }

    public void reloadDataFrom(File file) throws InvalidFormatException,
            IOException {
        SpreadsheetFactory.reloadSpreadsheetComponent(this, file);
    }

    public int getRowBufferSize() {
        return getState().rowBufferSize;
    }

    public void setRowBufferSize(int rowBufferInPixels) {
        getState().rowBufferSize = rowBufferInPixels;
    }

    public int getColBufferSize() {
        return getState().columnBufferSize;
    }

    public void setColBufferSize(int colBufferInPixels) {
        getState().columnBufferSize = colBufferInPixels;
    }

    /**
     * @return the defaultNewSheetRows
     */
    public int getDefaultNewSheetRows() {
        return defaultNewSheetRows;
    }

    /**
     * @param defaultNewSheetRows
     *            the defaultNewSheetRows to set
     */
    public void setDefaultNewSheetRows(int defaultNewSheetRows) {
        this.defaultNewSheetRows = defaultNewSheetRows;
    }

    /**
     * @return the defaultNewSheetColumns
     */
    public int getDefaultNewSheetColumns() {
        return defaultNewSheetColumns;
    }

    /**
     * @param defaultNewSheetColumns
     *            the defaultNewSheetColumns to set
     */
    public void setDefaultNewSheetColumns(int defaultNewSheetColumns) {
        this.defaultNewSheetColumns = defaultNewSheetColumns;
    }

    /**
     * Call this to force the spreadsheet to reload the currently viewed cell
     * contents. This forces reload of all: custom components (always visible &
     * editors) from {@link SpreadsheetComponentFactory}, hyperlinks, cells'
     * comments and cells' contents.
     */
    public void reloadVisibleCellContents() {
        loadCustomComponents();
        updateMarkedCellValues(firstColumn, lastColumn, firstRow, lastRow);
    }

    private List<SpreadsheetActionDetails> createActionsListForSelection() {
        List<SpreadsheetActionDetails> actions = new ArrayList<SpreadsheetActionDetails>();
        for (Handler handler : actionHandlers) {
            Action[] actions2 = handler.getActions(latestSelectionEvent, this);
            if (actions2 != null) {
                for (Action action : actions2) {
                    String key = actionMapper.key(action);
                    setResource(key, action.getIcon());
                    SpreadsheetActionDetails spreadsheetActionDetails = new SpreadsheetActionDetails();
                    spreadsheetActionDetails.caption = action.getCaption();
                    spreadsheetActionDetails.key = key;
                    spreadsheetActionDetails.type = 0;
                    actions.add(spreadsheetActionDetails);
                }
            }
        }
        return actions;
    }

    private List<SpreadsheetActionDetails> createActionsListForColumn(
            int columnIndex) {
        List<SpreadsheetActionDetails> actions = new ArrayList<SpreadsheetActionDetails>();
        final CellRangeAddress column = new CellRangeAddress(-1, -1,
                columnIndex - 1, columnIndex - 1);
        for (Handler handler : actionHandlers) {
            for (Action action : handler.getActions(column, this)) {
                String key = actionMapper.key(action);
                setResource(key, action.getIcon());
                SpreadsheetActionDetails spreadsheetActionDetails = new SpreadsheetActionDetails();
                spreadsheetActionDetails.caption = action.getCaption();
                spreadsheetActionDetails.key = key;
                spreadsheetActionDetails.type = 2;
                actions.add(spreadsheetActionDetails);
            }
        }
        return actions;
    }

    private List<SpreadsheetActionDetails> createActionsListForRow(int rowIndex) {
        List<SpreadsheetActionDetails> actions = new ArrayList<SpreadsheetActionDetails>();
        final CellRangeAddress row = new CellRangeAddress(rowIndex - 1,
                rowIndex - 1, -1, -1);
        for (Handler handler : actionHandlers) {
            for (Action action : handler.getActions(row, this)) {
                String key = actionMapper.key(action);
                setResource(key, action.getIcon());
                SpreadsheetActionDetails spreadsheetActionDetails = new SpreadsheetActionDetails();
                spreadsheetActionDetails.caption = action.getCaption();
                spreadsheetActionDetails.key = key;
                spreadsheetActionDetails.type = 1;
                actions.add(spreadsheetActionDetails);
            }
        }
        return actions;
    }

    /** clears server side spread sheet content */
    protected void clearSheetServerSide() {
        workbook = null;
        evaluator = null;
        styler = null;
        sentCells.clear();
        sentFormulaCells.clear();
        selectedCellReference = null;
        paintedCellRange = null;
        cellRangeAddresses.clear();
        individualSelectedCells.clear();
        markedCells.clear();
        hyperlinkStyleIndex = -1;
        for (SheetImageWrapper image : sheetImages) {
            setResource(image.resourceKey, null);
        }
        sheetImages.clear();
    }

    protected void setInternalWorkbook(Workbook workbook) {
        this.workbook = workbook;
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        styler = new SpreadsheetStyleFactory(this);

        reloadActiveSheetData();
        // not working in POI
        // getState().firstVisibleTab = workbook.getFirstVisibleTab()
        // - veryHiddenSheets;
        if (workbook instanceof HSSFWorkbook) {
            getState().workbookProtected = ((HSSFWorkbook) workbook)
                    .isWriteProtected();
        } else if (workbook instanceof XSSFWorkbook) {
            getState().workbookProtected = ((XSSFWorkbook) workbook)
                    .isStructureLocked();
        }
        // clear all tables from memory
        tables.clear();

        getState().verticalScrollPositions = new int[getState().sheetNames.length];
        getState().horizontalScrollPositions = new int[getState().sheetNames.length];
    }

    protected void reloadActiveSheetData() {
        latestSelectionEvent = null;
        selectedCellReference = null;
        paintedCellRange = null;
        cellRangeAddresses.clear();
        individualSelectedCells.clear();
        markedCells.clear();
        sentCells.clear();
        sentFormulaCells.clear();
        firstColumn = lastColumn = firstRow = lastRow = -1;
        for (SheetImageWrapper image : sheetImages) {
            setResource(image.resourceKey, null);
        }
        sheetImages.clear();

        reload = true;
        getState().sheetIndex = getSpreadsheetSheetIndex(workbook
                .getActiveSheetIndex()) + 1;
        getState().sheetProtected = getActiveSheet().getProtect();
        getState().cellKeysToEditorIdMap = null;
        getState().hyperlinksTooltips = null;
        getState().componentIDtoCellKeysMap = null;
        getState().resourceKeyToImage = null;
        getState().mergedRegions = null;
        if (customComponents != null && !customComponents.isEmpty()) {
            for (Component c : customComponents) {
                unRegisterCustomComponent(c);
            }
            customComponents.clear();
        }
        if (sheetPopupButtons != null && !sheetPopupButtons.isEmpty()) {
            for (PopupButton sf : sheetPopupButtons) {
                unRegisterCustomComponent(sf);
            }
            sheetPopupButtons.clear();
        }
        // clear all tables, possible tables for new/changed sheet are added
        // after first round trip.
        tablesLoaded = false;

        reloadSheets();

        markAsDirty();
    }

    private void reloadSheets() {
        final ArrayList<String> sheetNamesList = new ArrayList<String>();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!workbook.isSheetVeryHidden(i) && !workbook.isSheetHidden(i)) {
                sheetNamesList.add(workbook.getSheetName(i));
            }
        }
        getState().sheetNames = sheetNamesList
                .toArray(new String[sheetNamesList.size()]);
    }

    /**
     * Returns Apache POI model based index for the visible sheet at the given
     * index.
     * 
     * @param visibleSheetIndex
     *            0-based
     * @return 0-based, the sheet index inside POI, or -1 if something went
     *         wrong
     */
    public int getVisibleSheetPOIIndex(int visibleSheetIndex) {
        int realIndex = -1;
        int i = -1;
        do {
            realIndex++;
            if (!workbook.isSheetVeryHidden(realIndex)
                    && !workbook.isSheetHidden(realIndex)) {
                i++;
            }
        } while (i < visibleSheetIndex
                && realIndex < (workbook.getNumberOfSheets() - 1));
        return realIndex;
    }

    /**
     * 
     * @param pOISheetIndex
     *            0-based
     * @return 0-based visible sheet index
     */
    private int getSpreadsheetSheetIndex(int pOISheetIndex) {
        int ourIndex = -1;
        for (int i = 0; i <= pOISheetIndex; i++) {
            if (!workbook.isSheetVeryHidden(i) && !workbook.isSheetHidden(i)) {
                ourIndex++;
            }
        }
        return ourIndex;
    }

    public boolean isSheetProtected() {
        return getState().sheetProtected;
    }

    public boolean isCellHidden(Cell cell) {
        return isSheetProtected() && cell.getCellStyle().getHidden();
    }

    public boolean isCellLocked(Cell cell) {
        return isSheetProtected()
                && (cell == null || cell.getCellStyle().getLocked());
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);
        if (reload) {
            reload = false;
            getState().reload = true;
            if (initialSheetSelection == null) {
                initialSheetSelection = "A1";
            }
        } else {
            getState().reload = false;
        }
    }

    public SpreadsheetStyleFactory getSpreadsheetStyleFactory() {
        return styler;
    }

    /**
     * Note that modifications done directly with the {@link Workbook}'s API
     * will not get automatically updated into the Spreadsheet component.
     * 
     * @return the currently presented workbook
     */
    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        if (workbook == null) {
            throw new NullPointerException(
                    "Cannot open a null workbook with Spreadsheet component.");
        }
        SpreadsheetFactory.reloadSpreadsheetComponent(this, workbook);
    }

    /**
     * Note that modifications done directly with the {@link Sheet}'s API will
     * not get automatically updated into the Spreadsheet component.
     * 
     * @return the currently active sheet in the component.
     */
    public Sheet getActiveSheet() {
        return workbook.getSheetAt(workbook.getActiveSheetIndex());
    }

    public boolean hasSheetData() {
        return workbook != null;
    }

    /**
     * 
     * @param colIndex
     *            1-based
     * @param rowIndex
     *            1-based
     * @param clearRemovedCellStyle
     */
    protected void removeCell(int colIndex, int rowIndex,
            boolean clearRemovedCellStyle) {
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        final Row row = activeSheet.getRow(rowIndex - 1);
        if (row != null) {
            final Cell cell = row.getCell(colIndex - 1);
            if (cell != null) {
                removedCells.add(Integer.toString(rowIndex));
                final String key = toKey(colIndex, rowIndex);
                if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                    sentFormulaCells.remove(key);
                } else {
                    sentCells.remove(key);
                }
                // POI (3.9) doesn't have a method for removing a hyperlink !!!
                if (cell.getHyperlink() != null) {
                    removeHyperlink(cell, activeSheet);
                }
                if (clearRemovedCellStyle) {
                    // update style to 0
                    cell.setCellStyle(null);
                    styler.cellStyleUpdated(cell, true);
                }
                cell.setCellValue((String) null);
                evaluator.notifyUpdateCell(cell);

                removedCells.add(key);
            }
        }
    }

    private void removeHyperlink(Cell cell, Sheet sheet) {
        try {
            if (sheet instanceof XSSFSheet) {
                Field f;
                f = XSSFSheet.class.getDeclaredField("hyperlinks");
                f.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<XSSFHyperlink> hyperlinks = (List<XSSFHyperlink>) f
                        .get(sheet);
                hyperlinks.remove(cell.getHyperlink());
                f.setAccessible(false);
            } else if (sheet instanceof HSSFSheet && cell instanceof HSSFCell) {
                HSSFHyperlink link = (HSSFHyperlink) cell.getHyperlink();
                Field sheetField = HSSFSheet.class.getDeclaredField("_sheet");
                sheetField.setAccessible(true);
                InternalSheet internalsheet = (InternalSheet) sheetField
                        .get(sheet);
                List<RecordBase> records = internalsheet.getRecords();
                Field recordField = HSSFHyperlink.class
                        .getDeclaredField("record");
                recordField.setAccessible(true);
                records.remove(recordField.get(link));
                sheetField.setAccessible(false);
                recordField.setAccessible(false);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Indexes 1-based
     * 
     * @param col1
     * @param col2
     * @param row1
     * @param row2
     * @param clearRemovedCellStyle
     */
    protected void removeCells(int col1, int col2, int row1, int row2,
            boolean clearRemovedCellStyle) {
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        for (int i = row1 - 1; i < row2; i++) {
            Row row = activeSheet.getRow(i);
            if (row != null) {
                removedCells.add(Integer.toString(i + 1));
                for (int j = col1 - 1; j < col2; j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        final String key = toKey(j + 1, i + 1);
                        if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                            sentFormulaCells.remove(key);
                        } else {
                            sentCells.remove(key);
                        }
                        if (cell.getHyperlink() != null) {
                            removeHyperlink(cell, activeSheet);
                        }
                        if (clearRemovedCellStyle) {
                            // update style to 0
                            cell.setCellStyle(null);
                            styler.cellStyleUpdated(cell, true);
                        }
                        // need to make protection etc. settings for the cell
                        // won't get effected. deleting the cell would make it
                        // locked
                        removedCells.add(key);
                        cell.setCellValue((String) null);
                        evaluator.notifyUpdateCell(cell);
                    }
                }
            }
        }
    }

    /**
     * Method for updating the spreadsheet client side visible cells and cached
     * data correctly.
     * 
     * Iterates over the given range and makes sure the client side is updated
     * correctly for the range. Handles clearing of missing rows/ from the
     * cache. This iteration can be skipped by giving r1 as 0.
     * 
     * Iterates over the whole spreadsheet (existing rows&columns) and updates
     * client side cache for all sent formula cells, and cells that have been
     * marked for updating.
     * 
     * Parameters 1-based.
     */
    private void updateMarkedCellValues(int c1, int c2, int r1, int r2) {
        loadHyperLinks();
        loadCellComments();
        loadImages();
        loadPopupButtons();
        // custom components not updated here on purpose

        // on both iterators it is unnecessary to worry about having custom
        // components in the cell because the client side handles it -> it will
        // not replace a custom component with a cell value

        final HashMap<String, String> updatedCellData = new HashMap<String, String>();

        Sheet sheet = getActiveSheet();
        if (r1 != 0) {
            for (int r = r1; r <= r2; r++) {
                Row row = sheet.getRow(r - 1);
                if (row != null) {
                    boolean rowHasContent = false;
                    boolean hasRemovedContent = false;
                    for (int c = c1; c <= c2; c++) {
                        Cell cell = row.getCell(c - 1);
                        final String key = toKey(c, r);
                        if (cell != null && !removedCells.contains(key)) {
                            final String value = getCellValue(cell);
                            if (cell.getCellType() != Cell.CELL_TYPE_FORMULA
                                    && ((value != null && !value.isEmpty()) || sentCells
                                            .contains(key))) {
                                sentCells.add(key);
                                updatedCellData.put(key, value);
                                rowHasContent = true;
                            }
                        } else if (sentCells.contains(key)) {
                            // cell doesn't exist, if it was removed the cell
                            // key
                            // should be in removedCells
                            sentCells.remove(key);
                            removedCells.add(key);
                            hasRemovedContent = true;
                        } else if (sentFormulaCells.contains(key)) {
                            sentFormulaCells.remove(key);
                            removedCells.add(key);
                            hasRemovedContent = true;
                        }
                    }
                    if (rowHasContent) {
                        updatedCellData.put(Integer.toString(r), null);
                    }
                    if (hasRemovedContent) {
                        removedCells.add(Integer.toString(r));
                    }
                } else {
                    // row doesn't exist, need to check that cached values are
                    // removed for it properly
                    updateDeletedRowsInClientCache(r, r);
                }
            }
        }
        // update all cached formula cell values on client side, because they
        // might have changed. also make sure all marked cells are updated
        Iterator<Row> rows = workbook
                .getSheetAt(workbook.getActiveSheetIndex()).rowIterator();
        while (rows.hasNext()) {
            final Row r = rows.next();
            final Iterator<Cell> cells = r.cellIterator();
            boolean rowHasContent = false;
            while (cells.hasNext()) {
                final Cell cell = cells.next();
                int rowIndex = cell.getRowIndex();
                int columnIndex = cell.getColumnIndex();
                final String key = toKey(columnIndex + 1, rowIndex + 1);
                final String value = getCellValue(cell);
                // update formula cells
                if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                    if (value != null && !value.isEmpty()) {
                        if (sentFormulaCells.contains(key)
                                || markedCells.contains(key)
                                || (rowIndex + 1 >= r1 && rowIndex + 1 <= r2
                                        && columnIndex + 1 >= c1 && columnIndex <= c2)) {
                            sentFormulaCells.add(key);
                            updatedCellData.put(key, value);
                            rowHasContent = true;
                        }
                    } else if (sentFormulaCells.contains(key)) {
                        // in case the formula cell value has changed to null or
                        // empty; this case is probably quite rare, formula cell
                        // pointing to a cell that was removed or had its value
                        // cleared ???
                        sentFormulaCells.add(key);
                        updatedCellData.put(key, "");
                        rowHasContent = true;
                    }
                } else if (markedCells.contains(key)) {
                    sentCells.add(key);
                    updatedCellData.put(key, value);
                    rowHasContent = true;
                }
            }
            if (rowHasContent) {
                final String key = Integer.toString(r.getRowNum() + 1);
                updatedCellData.put(key, null);
            }

        }
        if (removedCells.isEmpty()) {
            getRpcProxy(SpreadsheetClientRpc.class).addCells(updatedCellData,
                    getCellIndexToStyleMap());
        } else {
            // FIXME investigate why HashSet<String> is not
            // serializing/deserializing
            getRpcProxy(SpreadsheetClientRpc.class).addUpdatedCells(
                    updatedCellData, new ArrayList<String>(removedCells),
                    getCellIndexToStyleMap());
        }
        markedCells.clear();
        removedCells.clear();
    }

    /**
     * Sends cells' data to client side. Data is only sent once, unless there
     * are changes. Cells with custom components are skipped.
     * 
     * @param firstRow
     *            1-based
     * @param lastRow
     *            1-based
     * @param firstColumn
     *            1-based
     * @param lastColumn
     *            1-based
     */
    private void loadCells(int firstRow, int lastRow, int firstColumn,
            int lastColumn) {
        loadCustomComponents();
        loadHyperLinks();
        loadCellComments();
        loadImages();
        loadTables();
        loadPopupButtons();
        // hssf (xls) document contain something on all rows & columns, as xssf
        // (xlsx) documents don't (empty rows and cells just don't exist)
        try {
            final HashMap<String, String> map = new HashMap<String, String>();
            final Sheet activeSheet = workbook.getSheetAt(workbook
                    .getActiveSheetIndex());
            Map<String, String> componentIDtoCellKeysMap = getState().componentIDtoCellKeysMap;
            @SuppressWarnings("unchecked")
            final Collection<String> customComponentCells = (Collection<String>) (componentIDtoCellKeysMap == null ? Collections
                    .emptyList() : componentIDtoCellKeysMap.values());
            for (int r = firstRow - 1; r < lastRow; r++) {
                Row row = activeSheet.getRow(r);
                if (row != null && row.getLastCellNum() != -1
                        && row.getLastCellNum() >= firstColumn) {
                    boolean rowHasContent = false;
                    for (int c = firstColumn - 1; c < lastColumn; c++) {
                        final String key = toKey(c + 1, r + 1);
                        if (!customComponentCells.contains(key)
                                && !sentCells.contains(key)
                                && !sentFormulaCells.contains(key)) {
                            Cell cell = row.getCell(c);
                            if (cell != null) {
                                final String contents = getCellValue(cell);
                                if (contents != null && !contents.isEmpty()) {
                                    map.put(key, contents);
                                    if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                                        sentFormulaCells.add(key);
                                    } else {
                                        sentCells.add(key);
                                    }
                                    rowHasContent = true;
                                }
                            }
                        }
                    }
                    if (rowHasContent) {
                        final String key = Integer.toString(r + 1);
                        map.put(key, null);
                    }
                }
            }
            getRpcProxy(SpreadsheetClientRpc.class).addCells(map,
                    getCellIndexToStyleMap());

            SpreadsheetFactory.logMemoryUsage();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    private void loadHyperLinks() {
        if (getState(false).hyperlinksTooltips == null) {
            getState(false).hyperlinksTooltips = new HashMap<String, String>();
        } else {
            getState().hyperlinksTooltips.clear();
        }
        for (int r = firstRow - 1; r < lastRow; r++) {
            final Row row = getActiveSheet().getRow(r);
            if (row != null) {
                for (int c = firstColumn - 1; c < lastColumn; c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null) {
                        try {
                            Hyperlink link = cell.getHyperlink();
                            if (link != null) {
                                if (link instanceof XSSFHyperlink) {
                                    String tooltip = ((XSSFHyperlink) link)
                                            .getTooltip();
                                    // Show address if no defined tooltip (like
                                    // in
                                    // excel)
                                    if (tooltip == null) {
                                        tooltip = link.getAddress();
                                    }
                                    getState().hyperlinksTooltips.put(
                                            toKey(c + 1, r + 1), tooltip);
                                } else {
                                    getState().hyperlinksTooltips.put(
                                            toKey(c + 1, r + 1),
                                            link.getAddress());
                                }
                            } else {
                                // Check if the cell has HYPERLINK function
                                if (DefaultHyperlinkCellClickHandler
                                        .isHyperlinkFormulaCell(cell)) {
                                    getState().hyperlinksTooltips
                                            .put(toKey(c + 1, r + 1),
                                                    DefaultHyperlinkCellClickHandler
                                                            .getHyperlinkFunctionCellAddress(cell));
                                }
                            }
                        } catch (XmlValueDisconnectedException exc) {

                        }
                    }
                }
            }
        }
    }

    private void loadImages() {
        if (sheetImages.isEmpty()) {
            getState().resourceKeyToImage = null;
        } else {
            if (getState(false).resourceKeyToImage == null) {
                getState(false).resourceKeyToImage = new HashMap<String, ImageInfo>();
            }
            // reload images from POI because row / column sizes have changed
            // currently doesn't effect anything because POI doesn't update the
            // image anchor data after resizing
            if (reloadImageSizesFromPOI) {
                for (SheetImageWrapper image : sheetImages) {
                    if (image.visible) {
                        getState().resourceKeyToImage.remove(image.resourceKey);
                        setResource(image.resourceKey, null);
                    }
                }
                sheetImages.clear();
                SpreadsheetFactory.loadSheetImages(this, getActiveSheet());
            }
            for (final SheetImageWrapper image : sheetImages) {
                if (image.isVisible(firstColumn, lastColumn, firstRow, lastRow)) {
                    if (!getState(false).resourceKeyToImage
                            .containsKey(image.resourceKey)) {
                        ImageInfo imageInfo = new ImageInfo();
                        generateImageInfo(image, imageInfo);
                        getState().resourceKeyToImage.put(image.resourceKey,
                                imageInfo);
                        if (image.resource == null) {
                            StreamSource streamSource = new StreamSource() {

                                @Override
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(image.data);
                                }
                            };
                            StreamResource resource = new StreamResource(
                                    streamSource, image.resourceKey);
                            resource.setMIMEType(image.MIMEType);
                            setResource(image.resourceKey, resource);
                            image.resource = resource;
                        }
                        image.visible = true;
                    } else {
                        generateImageInfo(image,
                                getState(false).resourceKeyToImage
                                        .get(image.resourceKey));
                    }
                } else if (image.visible) {
                    getState().resourceKeyToImage.remove(image.resourceKey);
                    image.visible = false;
                }
            }
        }
        reloadImageSizesFromPOI = false;
    }

    private void generateImageInfo(final SheetImageWrapper image,
            final ImageInfo info) {
        Sheet sheet = getActiveSheet();

        int col = image.anchor.getCol1();
        while (sheet.isColumnHidden(col) && col < (getState(false).cols - 1)) {
            col++;
        }
        int row = image.anchor.getRow1();
        Row r = sheet.getRow(row);
        while (r != null && r.getZeroHeight()) {
            row++;
            r = sheet.getRow(row);
        }

        info.col = col + 1; // 1-based
        info.row = row + 1; // 1-based
        info.height = image.getHeight(sheet, getState(false).rowH);
        info.width = image.getWidth(sheet, getState(false).colW,
                getState(false).defColW);
        info.dx = image.getDx1(sheet);
        info.dy = image.getDy1(sheet);
    }

    private void loadCellComments() {
        if (getState(false).cellComments == null) {
            getState(false).cellComments = new HashMap<String, String>();
        } else {
            getState().cellComments.clear();
        }
        if (getState(false).visibleCellComments == null) {
            getState(false).visibleCellComments = new ArrayList<String>();
        } else {
            getState().visibleCellComments.clear();
        }
        Sheet sheet = getActiveSheet();
        for (int r = firstRow - 1; r < lastRow; r++) {
            Row row = sheet.getRow(r);
            if (row != null && row.getZeroHeight()) {
                continue;
            }
            for (int c = firstColumn - 1; c < lastColumn; c++) {
                if (sheet.isColumnHidden(c)) {
                    continue;
                }
                MergedRegion region = mergedRegionContainer.getMergedRegion(
                        c + 1, r + 1);
                // do not add comments that are "below" merged regions.
                // client side handles cases where comment "moves" (because
                // shifting etc.) from merged cell into basic or vice versa.
                if (region == null || region.col1 == (c + 1)
                        && region.row1 == (r + 1)) {
                    Comment comment = sheet.getCellComment(r, c);
                    if (comment != null) {
                        // by default comments are shown when mouse is over the
                        // red
                        // triangle on the cell's top right corner. the comment
                        // position is calculated so that it is completely
                        // visible.
                        String key = toKey(c + 1, r + 1);
                        getState().cellComments.put(key, comment.getString()
                                .getString());
                        if (comment.isVisible()) {
                            getState().visibleCellComments.add(key);
                        }
                    }
                } else {
                    c = region.col2 - 1;
                }
            }
        }
    }

    /**
     * loads the custom components for the currently viewed cells and clears
     * previous components that are not currently visible.
     */
    private void loadCustomComponents() {
        if (customComponentFactory != null) {
            if (getState().cellKeysToEditorIdMap == null) {
                getState().cellKeysToEditorIdMap = new HashMap<String, String>();
            } else {
                getState().cellKeysToEditorIdMap.clear();
            }
            if (getState().componentIDtoCellKeysMap == null) {
                getState().componentIDtoCellKeysMap = new HashMap<String, String>();
            } else {
                getState().componentIDtoCellKeysMap.clear();
            }
            if (customComponents == null) {
                customComponents = new HashSet<Component>();
            }
            HashSet<Component> newCustomComponents = new HashSet<Component>();
            // iteration indexes 0-based
            for (int r = firstRow - 1; r < lastRow; r++) {
                final Row row = getActiveSheet().getRow(r);
                for (int c = firstColumn - 1; c < lastColumn; c++) {
                    // Cells that are inside a merged region are skipped:
                    MergedRegion region = mergedRegionContainer
                            .getMergedRegion(c + 1, r + 1);
                    if (region == null
                            || (region.col1 == (c + 1) && region.row1 == (r + 1))) {
                        Cell cell = null;
                        if (row != null) {
                            cell = row.getCell(c);
                        }
                        // check if the cell has a custom component
                        Component customComponent = customComponentFactory
                                .getCustomComponentForCell(cell, r, c, this,
                                        getActiveSheet());
                        if (customComponent != null) {
                            final String key = toKey(c + 1, r + 1);
                            if (!customComponents.contains(customComponent)) {
                                registerCustomComponent(customComponent);
                            }
                            getState().componentIDtoCellKeysMap.put(
                                    customComponent.getConnectorId(), key);
                            newCustomComponents.add(customComponent);
                        } else if (!isCellLocked(cell)) {
                            // no custom component and not locked, check if
                            // the cell has a custom editor
                            Component customEditor = customComponentFactory
                                    .getCustomEditorForCell(cell, r, c, this,
                                            getActiveSheet());
                            if (customEditor != null) {
                                final String key = toKey(c + 1, r + 1);
                                if (!newCustomComponents.contains(customEditor)
                                        && !customComponents
                                                .contains(customEditor)) {
                                    registerCustomComponent(customEditor);
                                }
                                getState().cellKeysToEditorIdMap.put(key,
                                        customEditor.getConnectorId());
                                newCustomComponents.add(customEditor);
                            }
                        }
                    }
                    if (region != null) {
                        c = region.col2;
                    }
                }
            }
            // unregister old
            for (Iterator<Component> i = customComponents.iterator(); i
                    .hasNext();) {
                Component c = i.next();
                if (!newCustomComponents.contains(c)) {
                    unRegisterCustomComponent(c);
                    i.remove();
                }
            }
            customComponents = newCustomComponents;
        } else {
            getState().cellKeysToEditorIdMap = null;
            getState().componentIDtoCellKeysMap = null;
            if (customComponents != null && !customComponents.isEmpty()) {
                for (Component c : customComponents) {
                    unRegisterCustomComponent(c);
                }
                customComponents.clear();
            }
        }
    }

    private void registerCustomComponent(Component component) {
        if (!equals(component.getParent())) {
            component.setParent(this);
        }
    }

    private void unRegisterCustomComponent(Component component) {
        component.setParent(null);
    }

    /**
     * This method should be always called when the selected cell has changed so
     * proper actions can be triggered for possible custom component inside the
     * cell.
     */
    private void selectedCellChanged() {
        if (selectedCellReference != null && customComponentFactory != null) {
            final short col = selectedCellReference.getCol();
            final int row = selectedCellReference.getRow();
            final String key = toKey(col + 1, row + 1);
            Map<String, String> cellKeysToEditorIdMap = getState(false).cellKeysToEditorIdMap;
            if (cellKeysToEditorIdMap != null
                    && cellKeysToEditorIdMap.containsKey(key)
                    && customComponents != null) {
                String componentId = getState(false).cellKeysToEditorIdMap
                        .get(key);
                for (Component c : customComponents) {
                    if (c.getConnectorId().equals(componentId)) {
                        customComponentFactory.onCustomEditorDisplayed(
                                getCell(row, col), row, col, this,
                                getActiveSheet(), c);
                        return;
                    }
                }
            }
        }
    }

    private final HashMap<Integer, String> getCellIndexToStyleMap() {
        // add the cell selector to correct style index
        HashMap<Integer, String> styleMap = new HashMap<Integer, String>();

        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        for (int r = firstRow - 1; r < lastRow; r++) {
            Row row = activeSheet.getRow(r);
            if (row != null && row.getLastCellNum() != -1
                    && row.getLastCellNum() >= firstColumn) {
                for (int c = firstColumn - 1; c < lastColumn; c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null) {
                        Integer cellStyleKey = (int) cell.getCellStyle()
                                .getIndex();
                        if (cellStyleKey != 0) { // default style
                            if (styleMap.containsKey(cellStyleKey)) {
                                styleMap.put(cellStyleKey,
                                        styleMap.get(cellStyleKey) + ",.col"
                                                + (cell.getColumnIndex() + 1)
                                                + ".row"
                                                + (cell.getRowIndex() + 1));
                            } else {
                                styleMap.put(cellStyleKey,
                                        ".col" + (cell.getColumnIndex() + 1)
                                                + ".row"
                                                + (cell.getRowIndex() + 1));
                            }
                        }
                    }
                }
            }
        }
        return styleMap;
    }

    public void setSpreadsheetComponentFactory(
            SpreadsheetComponentFactory customComponentFactory) {
        this.customComponentFactory = customComponentFactory;
        if (firstRow != -1) {
            loadCustomComponents();
            selectedCellChanged();
        } else {
            getState().cellKeysToEditorIdMap = null;
            if (customComponents != null && !customComponents.isEmpty()) {
                for (Component c : customComponents) {
                    unRegisterCustomComponent(c);
                }
                customComponents.clear();
            }
        }
    }

    /**
     * Adds a pop-up button to the spreadsheet. The button is added to the cell
     * that is defined in pop-up button settings (
     * {@link PopupButton#getCellReference()}).
     * <p>
     * Note that if the active sheet is changed, all pop-up buttons are removed
     * from the spreadsheet.
     * 
     * @param popupButton
     *            the pop-up button to add
     */
    public void addPopupButton(PopupButton popupButton) {
        if (sheetPopupButtons == null) {
            sheetPopupButtons = new HashSet<PopupButton>();
        }
        if (!sheetPopupButtons.contains(popupButton)) {
            sheetPopupButtons.add(popupButton);
        }
        int column = popupButton.getColumn() + 1;
        int row = popupButton.getRow() + 1;
        if (column >= firstColumn && column <= lastColumn && row >= firstRow
                && row <= lastRow) {
            registerCustomComponent(popupButton);
            markAsDirty();
        }
    }

    /**
     * Removes the pop-up button from the spreadsheet.
     * <p>
     * Note that if the active sheet is changed, all pop-up buttons are removed
     * from the spreadsheet.
     * 
     * @param popupButton
     *            the pop-up button to remove
     */
    public void removePopup(PopupButton popupButton) {
        if (sheetPopupButtons.contains(popupButton)) {
            sheetPopupButtons.remove(popupButton);
            int column = popupButton.getColumn() + 1;
            int row = popupButton.getRow() + 1;
            if (column >= firstColumn && column <= lastColumn
                    && row >= firstRow && row <= lastRow) {
                unRegisterCustomComponent(popupButton);
                markAsDirty();
            }
        }
    }

    /**
     * Registers and unregister pop-up button components for the currently
     * visible cells.
     */
    private void loadPopupButtons() {
        if (sheetPopupButtons != null) {
            for (PopupButton popupButton : sheetPopupButtons) {
                int column = popupButton.getColumn() + 1;
                int row = popupButton.getRow() + 1;
                if (column >= firstColumn && column <= lastColumn
                        && row >= firstRow && row <= lastRow) {
                    registerCustomComponent(popupButton);
                } else {
                    unRegisterCustomComponent(popupButton);
                }
            }
        }
    }

    /**
     * Adds a table to "memory", meaning that this table will be reloaded when
     * the active sheet changes to the sheet containing the table.
     * <p>
     * Populating the table "content" (pop-up button & content) is the
     * responsibility of the table, with {@link SpreadsheetTable#reload()}.
     * <p>
     * When the sheet is changed to a different sheet than the one that the
     * table belongs to, the table contents are cleared with
     * {@link SpreadsheetTable#clear()}. If the table is a filtering table, the
     * filters are NOT cleared (can be done with
     * {@link SpreadsheetFilterTable#clearAllFilters()}.
     * <p>
     * The pop-up buttons are always removed by the spreadsheet when the sheet
     * changes.
     * 
     * @param table
     *            the table to add to memory
     */
    public void addTableToMemory(SpreadsheetTable table) {
        tables.add(table);
    }

    /**
     * Removes a table from the "memory", it will no longer get reloaded when
     * the sheet is changed back to the sheet containing the table. Does not
     * delete any table content, use {@link #deleteTable(SpreadsheetTable)} for
     * "complete removal" table.
     * <p>
     * See {@link #addTableToMemory(SpreadsheetTable)}.
     * 
     * @param table
     *            the table to remove from memory
     */
    public void removeTableFromMemory(SpreadsheetTable table) {
        tables.remove(table);
    }

    /**
     * Deletes the table: removes it from "memory" (see
     * {@link #addTableToMemory(SpreadsheetTable)}), clears and removes all
     * possible filters (if table is {@link SpreadsheetFilterTable}), and clears
     * all table pop-up buttons and content.
     * 
     * @param table
     *            to delete
     */
    public void deleteTable(SpreadsheetTable table) {
        removeTableFromMemory(table);
        if (table.isTableSheetCurrentlyActive()) {
            for (PopupButton popupButton : table.getPopupButtons()) {
                removePopup(popupButton);
            }
            if (table instanceof SpreadsheetFilterTable) {
                ((SpreadsheetFilterTable) table).clearAllFilters();
            }
            table.clear();
        }
    }

    /**
     * Gets all the tables that for the spreadsheet. See
     * {@link #addTableToMemory(SpreadsheetTable)}.
     * 
     * @return all tables for the spreadsheet
     */
    public HashSet<SpreadsheetTable> getTables() {
        return tables;
    }

    /**
     * Gets the tables that belong to the currently active sheet (
     * {@link #getActiveSheet()}). See
     * {@link #addTableToMemory(SpreadsheetTable)}.
     * 
     * @return tables for current sheet
     */
    public List<SpreadsheetTable> getTablesForActiveSheet() {
        List<SpreadsheetTable> temp = new ArrayList<SpreadsheetTable>();
        for (SpreadsheetTable table : tables) {
            if (table.getSheet().equals(getActiveSheet())) {
                temp.add(table);
            }
        }
        return temp;
    }

    /** reload tables for current sheet */
    private void loadTables() {
        if (!tablesLoaded) {
            for (SpreadsheetTable table : tables) {
                if (table.getSheet().equals(getActiveSheet())) {
                    table.reload();
                }
            }
            tablesLoaded = true;
        }
    }

    public final String getColHeader(int col) {
        String h = "";
        while (col > 0) {
            h = (char) ('A' + (col - 1) % 26) + h;
            col = (col - 1) / 26;
        }
        return h;
    }

    public final String getCellValue(Cell cell) {
        try {
            return formatter.formatCellValue(cell, evaluator);
        } catch (RuntimeException rte) {
            return "ERROR:" + rte.getMessage();
        }
    }

    public final int getColHeaderIndex(String header) {
        int x = 0;
        for (int i = 0; i < header.length(); i++) {
            char h = header.charAt(i);
            x = (h - 'A' + 1) + (x * 26);
        }
        return x;
    }

    /**
     * 
     * @param col
     *            1 based
     * @param row
     *            1 based
     * @return
     */
    public final String toKey(int col, int row) {
        return "col" + col + " row" + row;
    }

    public final String toKey(Cell cell) {
        return toKey(cell.getColumnIndex() + 1, cell.getRowIndex() + 1);
    }

    public static class SelectionChangeEvent extends Component.Event {

        private final CellReference selectedCellReference;
        private final CellReference[] individualSelectedCells;
        private final CellRangeAddress selectedCellMergedRegion;
        private final CellRangeAddress[] cellRangeAddresses;

        public SelectionChangeEvent(Component source,
                CellReference selectedCellReference,
                CellReference[] individualSelectedCells,
                CellRangeAddress selectedCellMergedRegion,
                CellRangeAddress... cellRangeAddresses) {
            super(source);
            this.selectedCellReference = selectedCellReference;
            this.individualSelectedCells = individualSelectedCells;
            this.selectedCellMergedRegion = selectedCellMergedRegion;
            this.cellRangeAddresses = cellRangeAddresses;
        }

        public Spreadsheet getSpreadsheet() {
            return (Spreadsheet) getSource();
        }

        public CellReference getSelectedCellReference() {
            return selectedCellReference;
        }

        public CellReference[] getIndividualSelectedCells() {
            return individualSelectedCells;
        }

        /**
         * @return the selectedCellMergedRegion
         */
        public CellRangeAddress getSelectedCellMergedRegion() {
            return selectedCellMergedRegion;
        }

        public CellRangeAddress[] getCellRangeAddresses() {
            return cellRangeAddresses;
        }
    }

    public interface SelectionChangeListener extends Serializable {
        public static final Method SELECTION_CHANGE_METHOD = ReflectTools
                .findMethod(SelectionChangeListener.class, "onSelectionChange",
                        SelectionChangeEvent.class);

        public void onSelectionChange(SelectionChangeEvent event);
    }

    public void addSelectedCellChangeListener(SelectionChangeListener listener) {
        addListener(SelectionChangeEvent.class, listener,
                SelectionChangeListener.SELECTION_CHANGE_METHOD);
    }

    public void removeSelectedCellChangeListener(
            SelectionChangeListener listener) {
        removeListener(SelectionChangeEvent.class, listener,
                SelectionChangeListener.SELECTION_CHANGE_METHOD);
    }

    private void fireNewSelectionChangeEvent() {
        CellRangeAddress selectedCellMergedRegion = null;
        MergedRegion region = mergedRegionContainer
                .getMergedRegionStartingFrom(
                        selectedCellReference.getCol() + 1,
                        selectedCellReference.getRow() + 1);
        if (region != null) {
            selectedCellMergedRegion = new CellRangeAddress(region.row1 - 1,
                    region.row2 - 1, region.col1 - 1, region.col2 - 1);
            // if the only range is the merged region, clear ranges
            if (cellRangeAddresses.size() == 1
                    && cellRangeAddresses.get(0).formatAsString()
                            .equals(selectedCellMergedRegion.formatAsString())) {
                cellRangeAddresses.clear();
            }
        }
        if (latestSelectionEvent != null) {
            boolean changed = false;
            if (!latestSelectionEvent.getSelectedCellReference().equals(
                    selectedCellReference)) {
                changed = true;
            }
            if (!changed) {
                if (latestSelectionEvent.getIndividualSelectedCells().length != individualSelectedCells
                        .size()) {
                    changed = true;
                } else {
                    for (CellReference cr : latestSelectionEvent
                            .getIndividualSelectedCells()) {
                        if (!individualSelectedCells.contains(cr)) {
                            changed = true;
                            break;
                        }
                    }
                }
            }
            if (!changed) {
                if (latestSelectionEvent.getCellRangeAddresses().length != cellRangeAddresses
                        .size()) {
                    changed = true;
                } else {
                    for (CellRangeAddress cra : latestSelectionEvent
                            .getCellRangeAddresses()) {
                        if (!cellRangeAddresses.contains(cra)) {
                            changed = true;
                            break;
                        }
                    }
                }
            }
            if (!changed) {
                CellRangeAddress previouSelectedCellMergedRegion = latestSelectionEvent
                        .getSelectedCellMergedRegion();
                if ((previouSelectedCellMergedRegion == null && selectedCellMergedRegion != null)
                        || (previouSelectedCellMergedRegion != null && !previouSelectedCellMergedRegion
                                .equals(selectedCellMergedRegion))) {
                    changed = true;
                }
            }
            if (!changed) {
                return;
            }
        }
        final CellReference[] individualCellsArray = individualSelectedCells
                .toArray(new CellReference[individualSelectedCells.size()]);
        final CellRangeAddress[] cellRangesArray = cellRangeAddresses
                .toArray(new CellRangeAddress[cellRangeAddresses.size()]);
        latestSelectionEvent = new SelectionChangeEvent(this,
                selectedCellReference, individualCellsArray,
                selectedCellMergedRegion, cellRangesArray);

        fireEvent(latestSelectionEvent);
    }

    /**
     * 
     * @return the reference to the currently selected cell.
     */
    public CellReference getSelectedCellReference() {
        return selectedCellReference;
    }

    public static class SelectedSheetChangeEvent extends Component.Event {

        private final Sheet newSheet;
        private final Sheet previousSheet;
        private final int newSheetVisibleIndex;
        private final int newSheetPOIIndex;

        public SelectedSheetChangeEvent(Component source, Sheet newSheet,
                Sheet previousSheet, int newSheetVisibleIndex,
                int newSheetPOIIndex) {
            super(source);
            this.newSheet = newSheet;
            this.previousSheet = previousSheet;
            this.newSheetVisibleIndex = newSheetVisibleIndex;
            this.newSheetPOIIndex = newSheetPOIIndex;
        }

        /**
         * @return the newSheet
         */
        public Sheet getNewSheet() {
            return newSheet;
        }

        /**
         * @return the previousSheet
         */
        public Sheet getPreviousSheet() {
            return previousSheet;
        }

        /**
         * @return the newSheetVisibleIndex
         */
        public int getNewSheetVisibleIndex() {
            return newSheetVisibleIndex;
        }

        /**
         * @return the newSheetPOIIndex
         */
        public int getNewSheetPOIIndex() {
            return newSheetPOIIndex;
        }
    }

    public interface SelectedSheetChangeListener extends Serializable {
        public static final Method SELECTED_SHEET_CHANGE_METHOD = ReflectTools
                .findMethod(SelectedSheetChangeListener.class,
                        "onSelectedSheetChange", SelectedSheetChangeEvent.class);

        public void onSelectedSheetChange(SelectedSheetChangeEvent event);
    }

    public void addSelectedSheetChangeListener(
            SelectedSheetChangeListener listener) {
        addListener(SelectedSheetChangeEvent.class, listener,
                SelectedSheetChangeListener.SELECTED_SHEET_CHANGE_METHOD);
    }

    public void removeSelectedSheetChangeListener(
            SelectedSheetChangeListener listener) {
        removeListener(SelectedSheetChangeEvent.class, listener,
                SelectedSheetChangeListener.SELECTED_SHEET_CHANGE_METHOD);
    }

    private void fireSelectedSheetChangeEvent(Sheet previousSheet,
            Sheet newSheet) {
        int newSheetPOIIndex = workbook.getActiveSheetIndex();
        fireEvent(new SelectedSheetChangeEvent(this, newSheet, previousSheet,
                getSpreadsheetSheetIndex(newSheetPOIIndex), newSheetPOIIndex));
    }

    @Override
    public Iterator<Component> iterator() {
        if (customComponents == null && sheetPopupButtons == null) {
            List<Component> emptyList = Collections.emptyList();
            return emptyList.iterator();
        } else {
            return new SpreadsheetIterator<Component>(customComponents,
                    sheetPopupButtons);
        }
    }

    public static class SpreadsheetIterator<E extends Component> implements
            Iterator<Component> {
        private final Iterator<Component> customComponentIterator;
        private final Iterator<PopupButton> sheetPopupButtonIterator;
        /** true for customComponentIterator, false for sheetPopupButtonIterator */
        private boolean currentIteratorPointer;

        public SpreadsheetIterator(Set<Component> customComponents,
                Set<PopupButton> sheetPopupButtons) {
            customComponentIterator = customComponents == null ? null
                    : customComponents.iterator();
            sheetPopupButtonIterator = sheetPopupButtons == null ? null
                    : sheetPopupButtons.iterator();
            currentIteratorPointer = true;
        }

        @Override
        public boolean hasNext() {
            return (customComponentIterator != null && customComponentIterator
                    .hasNext())
                    || (sheetPopupButtonIterator != null && sheetPopupButtonIterator
                            .hasNext());
        }

        @Override
        public Component next() {
            if (customComponentIterator != null
                    && customComponentIterator.hasNext()) {
                return customComponentIterator.next();
            }
            if (sheetPopupButtonIterator != null
                    && sheetPopupButtonIterator.hasNext()) {
                currentIteratorPointer = false;
                return sheetPopupButtonIterator.next();
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            if (currentIteratorPointer && customComponentIterator != null) {
                customComponentIterator.remove();
            } else if (sheetPopupButtonIterator != null) {
                sheetPopupButtonIterator.remove();
            }
        }

    }
}
