package com.vaadin.addon.spreadsheet;

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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.vaadin.addon.spreadsheet.Spreadsheet.CellValueChangeEvent;
import com.vaadin.addon.spreadsheet.Spreadsheet.CellValueHandler;
import com.vaadin.addon.spreadsheet.client.CellData;
import com.vaadin.addon.spreadsheet.command.CellValueCommand;
import com.vaadin.ui.UI;

/**
 * CellValueManager is an utility class of SpreadsheetClass, which handles
 * values and formatting for individual cells.
 * 
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class CellValueManager implements Serializable {

    private static final Logger LOGGER = Logger
            .getLogger(CellValueManager.class.getName());

    private short hyperlinkStyleIndex = -1;

    /**
     * The Spreadsheet this class is tied to.
     */
    protected final Spreadsheet spreadsheet;

    private CellValueHandler customCellValueHandler;

    private DataFormatter formatter;

    private FormulaEvaluator evaluator;

    /** Cell keys that have values sent to client side and are cached there. */
    private final HashSet<String> sentCells = new HashSet<String>();
    /**
     * Formula cell keys that have values sent to client side and are cached
     * there.
     */
    private final HashSet<String> sentFormulaCells = new HashSet<String>();
    /** */
    private final HashSet<CellData> removedCells = new HashSet<CellData>();
    /** */
    private final HashSet<String> markedCells = new HashSet<String>();

    private boolean topLeftCellsLoaded;
    private HashMap<Integer, Float> cellStyleWidthRatioMap;

    /**
     * Creates a new CellValueManager and ties it to the given Spreadsheet.
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     */
    public CellValueManager(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
        UI current = UI.getCurrent();
        if (current != null) {
            formatter = new DataFormatter(current.getLocale());
        } else {
            formatter = new DataFormatter();
        }
    }

    private CellSelectionManager getCellSelectionManager() {
        return spreadsheet.getCellSelectionManager();
    }

    /**
     * Clears all evaluated cell values from cache.
     */
    public void clearEvaluatorCache() {
        evaluator.clearAllCachedResultValues();
    }

    /**
     * Clears all cached data.
     */
    public void clearCachedContent() {
        markedCells.clear();
        sentCells.clear();
        removedCells.clear();
        sentFormulaCells.clear();
        hyperlinkStyleIndex = -1;
        topLeftCellsLoaded = false;
    }

    public DataFormatter getDataFormatter() {
        return formatter;
    }

    public void setDataFormatter(DataFormatter dataFormatter) {
        formatter = dataFormatter;
    }

    public void updateFormatter(Locale locale) {
        formatter = new DataFormatter(locale);
    }

    public FormulaEvaluator getFormulaEvaluator() {
        return evaluator;
    }

    public void setFormulaEvaluator(FormulaEvaluator formulaEvaluator) {
        evaluator = formulaEvaluator;
    }

    public void updateEvaluator() {
        evaluator = spreadsheet.getWorkbook().getCreationHelper()
                .createFormulaEvaluator();
    }

    protected CellData createCellDataForCell(Cell cell) {
        CellData cellData = new CellData();
        cellData.row = cell.getRowIndex() + 1;
        cellData.col = cell.getColumnIndex() + 1;
        CellStyle cellStyle = cell.getCellStyle();
        cellData.cellStyle = "cs" + cellStyle.getIndex();
        cellData.locked = spreadsheet.isCellLocked(cell);
        try {
            String formattedCellValue = formatter.formatCellValue(cell,
                    evaluator);
            if (!spreadsheet.isCellHidden(cell)) {
                if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                    cellData.formulaValue = cell.getCellFormula();
                }
            }
            if (formattedCellValue != null && !formattedCellValue.isEmpty()
                    || cellStyle.getIndex() != 0) {
                // if the cell is not wrapping text, and is of type numeric or
                // formula (but not date), calculate if formatted cell value
                // fits the column width and possibly use scientific notation.
                if (!cellStyle.getWrapText()
                        && (!SpreadsheetUtil.cellContainsDate(cell)
                                && cell.getCellType() == Cell.CELL_TYPE_NUMERIC || (cell
                                .getCellType() == Cell.CELL_TYPE_FORMULA && !cell
                                .getCellFormula().startsWith("HYPERLINK")))) {
                    cellData.value = getScientificNotationStringForNumericCell(
                            cell, formattedCellValue);
                } else {
                    cellData.value = formattedCellValue;
                }
                if (cellStyle.getAlignment() == CellStyle.ALIGN_GENERAL) {
                    if (SpreadsheetUtil.cellContainsDate(cell)
                            || cell.getCellType() == Cell.CELL_TYPE_NUMERIC
                            || (cell.getCellType() == Cell.CELL_TYPE_FORMULA
                                    && !cell.getCellFormula().startsWith(
                                            "HYPERLINK") && !(cell
                                    .getCachedFormulaResultType() == Cell.CELL_TYPE_STRING))) {
                        cellData.cellStyle = cellData.cellStyle + " r";
                    }
                }

            }

            // conditional formatting might be applied even if there isn't a
            // value (such as borders for the cell to the right)
            Set<Integer> cellFormattingIndexes = spreadsheet
                    .getConditionalFormatter().getCellFormattingIndex(cell);
            if (cellFormattingIndexes != null) {

                for (Integer i : cellFormattingIndexes) {
                    cellData.cellStyle = cellData.cellStyle + " cf" + i;
                }

                markedCells.add(SpreadsheetUtil.toKey(cell));
            }
        } catch (RuntimeException rte) {
            LOGGER.log(Level.FINEST, rte.getMessage(), rte);
            cellData.value = "ERROR:" + rte.getMessage();
        }

        return cellData;
    }

    protected String getScientificNotationStringForNumericCell(Cell cell,
            String formattedValue) {

        Float r = cellStyleWidthRatioMap.get((int) cell.getCellStyle()
                .getIndex());

        if (r == null) {
            // no col data available, return same val
            return formattedValue;
        }

        BigDecimal ratio = new BigDecimal(r);
        BigDecimal stringPixels = ratio.multiply(new BigDecimal(formattedValue
                .length()));
        BigDecimal columnWidth = new BigDecimal(
                spreadsheet.getState(false).colW[cell.getColumnIndex()] - 10);
        if (stringPixels.compareTo(columnWidth) == 1) {
            int numberOfDigits = columnWidth.divide(ratio, RoundingMode.DOWN)
                    .intValue() - 4; // 0.#E10
            if (numberOfDigits < 1) {
                return "###";
            } else {
                StringBuilder format = new StringBuilder("0.");
                for (int i = 0; i < numberOfDigits; i++) {
                    format.append('#');
                }
                format.append("E0");
                try {
                    return new DecimalFormat(format.toString()).format(cell
                            .getNumericCellValue());
                } catch (IllegalStateException ise) {
                    LOGGER.log(
                            Level.FINE,
                            "CellType mismatch: " + ise.getMessage()
                                    + ", on cell "
                                    + SpreadsheetUtil.toKey(cell) + " of type "
                                    + cell.getCellType(), ise);
                }
            }
        }
        return formattedValue;
    }

    /**
     * Gets the current CellValueHandler
     * 
     * @return the customCellValueHandler
     */
    public CellValueHandler getCustomCellValueHandler() {
        return customCellValueHandler;
    }

    /**
     * Sets the current CellValueHandler
     * 
     * @param customCellValueHandler
     *            the customCellValueHandler to set
     */
    public void setCustomCellValueHandler(
            CellValueHandler customCellValueHandler) {
        this.customCellValueHandler = customCellValueHandler;
    }

    /**
     * Notifies evaluator and marks cell for update on next call to
     * {@link #updateMarkedCellValues()}
     * 
     * @param cell
     *            Cell to mark for updates
     */
    protected void cellUpdated(Cell cell) {
        evaluator.notifyUpdateCell(cell);
        markCellForUpdate(cell);
    }

    /**
     * Marks cell for update on next call to {@link #updateMarkedCellValues()}
     * 
     * @param cell
     *            Cell to mark for updates
     */
    protected void markCellForUpdate(Cell cell) {
        markedCells.add(SpreadsheetUtil.toKey(cell));
    }

    /**
     * Marks the given cell as deleted and notifies the evaluator
     * 
     * @param cell
     *            Deleted cell
     */
    protected void cellDeleted(Cell cell) {
        evaluator.notifyDeleteCell(cell);
        markCellForRemove(cell);
    }

    /**
     * Marks the given cell for removal.
     * 
     * @param cell
     *            Cell to mark for removal
     */
    protected void markCellForRemove(Cell cell) {
        String cellKey = SpreadsheetUtil.toKey(cell);
        CellData cd = new CellData();
        cd.col = cell.getColumnIndex() + 1;
        cd.row = cell.getRowIndex() + 1;
        removedCells.add(cd);
        clearCellCache(cellKey);
    }

    /**
     * Clears the cell with the given key from the cache
     * 
     * @param cellKey
     *            Key of target cell
     */
    protected void clearCellCache(String cellKey) {
        if (!sentCells.remove(cellKey)) {
            sentFormulaCells.remove(cellKey);
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
     *            Column index of target cell, 1-based
     * @param row
     *            Row index of target cell, 1-based
     * @param value
     *            The new value to set to the target cell, formulas will start
     *            with an extra "="
     */
    public void onCellValueChange(int col, int row, String value) {
        Workbook workbook = spreadsheet.getWorkbook();
        // update cell value
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        Row r = activeSheet.getRow(row - 1);
        if (r == null) {
            r = activeSheet.createRow(row - 1);
        }
        Cell cell = r.getCell(col - 1);
        String formattedCellValue = null;
        int cellType = -1;
        // capture cell value to history
        CellValueCommand command = new CellValueCommand(spreadsheet);
        command.captureCellValues(new CellReference(row - 1, col - 1));
        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);

        if (getCustomCellValueHandler() == null
                || getCustomCellValueHandler().cellValueUpdated(cell,
                        activeSheet, col - 1, row - 1, value, evaluator,
                        formatter)) {
            Exception exception = null;
            try {
                // handle new cell creation
                SpreadsheetStyleFactory styler = spreadsheet
                        .getSpreadsheetStyleFactory();
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

                        Double percentage = SpreadsheetUtil.parsePercentage(
                                value, spreadsheet.getLocale());
                        Double numVal = SpreadsheetUtil.parseNumber(value,
                                spreadsheet.getLocale());

                        if (value.isEmpty()) {
                            cell = r.createCell(col - 1); // BLANK

                        } else if (percentage != null) {
                            cell = r.createCell(col - 1, Cell.CELL_TYPE_NUMERIC);
                            CellStyle style = workbook.createCellStyle();
                            style.setDataFormat(workbook
                                    .createDataFormat()
                                    .getFormat(
                                            spreadsheet
                                                    .getDefaultPercentageFormat()));
                            cell.setCellStyle(style);
                            styler.cellStyleUpdated(cell, true);
                            cell.setCellValue(percentage);

                        } else if (numVal != null) {
                            cell = r.createCell(col - 1, Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(numVal);
                        } else {
                            cell = r.createCell(col - 1, Cell.CELL_TYPE_STRING);
                            cell.setCellValue(value);
                        }
                        evaluator.notifyUpdateCell(cell);
                    }
                } else { // modify existing cell, possibly switch type
                    formattedCellValue = getFormattedCellValue(cell);
                    final String key = SpreadsheetUtil.toKey(col, row);
                    cellType = cell.getCellType();
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

                        Double percentage = SpreadsheetUtil.parsePercentage(
                                value, spreadsheet.getLocale());
                        Double numVal = SpreadsheetUtil.parseNumber(value,
                                spreadsheet.getLocale());

                        if (value.isEmpty()) {
                            cell.setCellType(Cell.CELL_TYPE_BLANK);
                        } else if (percentage != null) {
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            CellStyle cs = cell.getCellStyle();
                            if (cs == null) {
                                cs = workbook.createCellStyle();
                                cell.setCellStyle(cs);
                            }
                            cs.setDataFormat(workbook
                                    .createDataFormat()
                                    .getFormat(
                                            spreadsheet
                                                    .getDefaultPercentageFormat()));
                            styler.cellStyleUpdated(cell, true);
                            cell.setCellValue(percentage);
                        } else if (numVal != null) {
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(numVal);
                        } else if (cellType == Cell.CELL_TYPE_BOOLEAN) {
                            cell.setCellValue(Boolean.parseBoolean(value));
                        } else if (cellType == Cell.CELL_TYPE_NUMERIC) {
                            parseValueIntoNumericCell(cell, value);
                        } else {
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            cell.setCellValue(value);
                        }
                        evaluator.notifyUpdateCell(cell);
                    }
                }
            } catch (FormulaParseException fpe) {
                try {
                    exception = fpe;
                    cell.setCellFormula(value.substring(1).replace(" ", ""));
                } catch (FormulaParseException fpe2) {
                    exception = fpe2;
                    cell.setCellValue(value);
                }
            } catch (NumberFormatException nfe) {
                exception = nfe;
                cell.setCellValue(value);
            } catch (Exception e) {
                exception = e;
                cell.setCellValue(value);
            }
            if (cell != null) {
                markCellForUpdate(cell);
                if (formattedCellValue == null
                        || !formattedCellValue
                                .equals(getFormattedCellValue(cell))
                        || cellType != cell.getCellType()) {
                    fireCellValueChangeEvent(cell);
                }
            }
            if (exception != null) {
                LOGGER.log(Level.FINE,
                        "Failed to parse cell value for cell at col " + col
                                + " row " + row + " (" + exception.getMessage()
                                + ")", exception);
            }
        }

        spreadsheet.updateMarkedCells();
    }

    /**
     * Returns the formatted cell value or null if value could not be determined
     * 
     * @param cell
     *            to get value from
     * @return formattedCellValue or null if could not format
     */
    private String getFormattedCellValue(Cell cell) {
        try {
            return formatter.formatCellValue(cell, evaluator);
        } catch (RuntimeException rte) {
            return null;
        }
    }

    private void fireCellValueChangeEvent(Cell cell) {
        Set<CellReference> cells = new HashSet<CellReference>();
        cells.add(new CellReference(cell));
        spreadsheet.fireEvent(new CellValueChangeEvent(spreadsheet, cells));
    }

    private void fireCellValueChangeEvent(Set<CellReference> changedCells) {
        spreadsheet.fireEvent(new CellValueChangeEvent(spreadsheet,
                changedCells));
    }

    /**
     * Deletes the currently selected cells' values. Does not affect styles.
     */
    public void onDeleteSelectedCells() {
        final Sheet activeSheet = spreadsheet.getActiveSheet();
        final CellReference selectedCellReference = getCellSelectionManager()
                .getSelectedCellReference();
        // TODO show error on locked cells instead
        if (selectedCellReference != null) {
            Row row = activeSheet.getRow(selectedCellReference.getRow());
            if (row != null
                    && spreadsheet.isCellLocked(row
                            .getCell(selectedCellReference.getCol()))) {
                return;
            }
        }
        List<CellReference> individualSelectedCells = getCellSelectionManager()
                .getIndividualSelectedCells();
        for (CellReference cr : individualSelectedCells) {
            final Row row = activeSheet.getRow(cr.getRow());
            if (row != null
                    && spreadsheet.isCellLocked(row.getCell(cr.getCol()))) {
                return;
            }
        }
        List<CellRangeAddress> cellRangeAddresses = getCellSelectionManager()
                .getCellRangeAddresses();
        for (CellRangeAddress range : cellRangeAddresses) {
            if (!spreadsheet.isRangeEditable(range)) {
                return;
            }
        }
        fireCellValueChangeEvent(spreadsheet.getSelectedCellReferences());

        CellValueCommand command = new CellValueCommand(spreadsheet);
        if (selectedCellReference != null) {
            command.captureCellValues(selectedCellReference);
        }
        for (CellReference cr : individualSelectedCells) {
            command.captureCellValues(cr);
        }
        for (CellRangeAddress range : cellRangeAddresses) {
            command.captureCellRangeValues(range);
        }
        if (selectedCellReference != null) {
            removeCell(selectedCellReference.getRow() + 1,
                    selectedCellReference.getCol() + 1, false);
        }
        for (CellReference cr : individualSelectedCells) {
            removeCell(cr.getRow() + 1, cr.getCol() + 1, false);
        }
        for (CellRangeAddress range : cellRangeAddresses) {
            removeCells(range.getFirstRow() + 1, range.getFirstColumn() + 1,
                    range.getLastRow() + 1, range.getLastColumn() + 1, false);
        }
        // removeCell and removeCells makes sure that cells are removed and
        // cleared from client side cache.
        updateMarkedCellValues();
        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);
    }

    /**
     * Attempts to parse a numeric value from the given String and set it to the
     * given Cell.
     * 
     * @param cell
     *            Target Cell
     * @param value
     *            Source for parsing the value
     */
    protected void parseValueIntoNumericCell(final Cell cell, final String value) {
        // try to parse the string with the existing cell format
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
                LOGGER.log(Level.FINEST, "Could not parse String to format, "
                        + oldFormat.getClass() + ", "
                        + cell.getCellStyle().getDataFormatString() + " : "
                        + pe.getMessage(), pe);
                try {
                    cell.setCellValue(Double.parseDouble(value));
                } catch (NumberFormatException nfe) {
                    LOGGER.log(
                            Level.FINEST,
                            "Could not parse String to Double: "
                                    + nfe.getMessage(), nfe);
                    cell.setCellValue(value);
                }
            } catch (NumberFormatException nfe) {
                LOGGER.log(Level.FINEST, "Could not parse String to Double: "
                        + nfe.getMessage(), nfe);
                cell.setCellValue(value);
            }
        }
    }

    /**
     * Sends cell data to the client. Only the data within the given bounds will
     * be sent.
     * 
     * @param firstRow
     *            Starting row index, 1-based
     * @param firstColumn
     *            Starting column index, 1-based
     * @param lastRow
     *            Ending row index, 1-based
     * @param lastColumn
     *            Ending column index, 1-based
     */
    protected void loadCellData(int firstRow, int firstColumn, int lastRow,
            int lastColumn) {
        try {
            int verticalSplitPosition = spreadsheet.getLastFrozenRow();
            int horizontalSplitPosition = spreadsheet.getLastFrozenColumn();
            if (verticalSplitPosition > 0 && horizontalSplitPosition > 0
                    && !topLeftCellsLoaded) { // top left pane
                ArrayList<CellData> topLeftData = loadCellDataForRowAndColumnRange(
                        1, 1, verticalSplitPosition, horizontalSplitPosition);
                topLeftCellsLoaded = true;
                if (!topLeftData.isEmpty()) {
                    spreadsheet.getRpcProxy().updateTopLeftCellValues(
                            topLeftData);
                }
            }

            if (verticalSplitPosition > 0) { // top right pane
                ArrayList<CellData> topRightData = loadCellDataForRowAndColumnRange(
                        1, firstColumn, verticalSplitPosition, lastColumn);
                if (!topRightData.isEmpty()) {
                    spreadsheet.getRpcProxy().updateTopRightCellValues(
                            topRightData);
                }
            }
            if (horizontalSplitPosition > 0) { // bottom left pane
                ArrayList<CellData> bottomLeftData = loadCellDataForRowAndColumnRange(
                        firstRow, 1, lastRow, horizontalSplitPosition);
                if (!bottomLeftData.isEmpty()) {
                    spreadsheet.getRpcProxy().updateBottomLeftCellValues(
                            bottomLeftData);
                }
            }

            ArrayList<CellData> bottomRightData = loadCellDataForRowAndColumnRange(
                    firstRow, firstColumn, lastRow, lastColumn);
            if (!bottomRightData.isEmpty()) {
                spreadsheet.getRpcProxy().updateBottomRightCellValues(
                        bottomRightData);
            }
        } catch (NullPointerException npe) {
            LOGGER.log(Level.FINEST, npe.getMessage(), npe);
        }
    }

    /**
     * Gets cell data for cells within the given bounds.
     * 
     * @param firstRow
     *            Starting row index, 1-based
     * @param firstColumn
     *            Starting column index, 1-based
     * @param lastRow
     *            Ending row index, 1-based
     * @param lastColumn
     *            Ending column index, 1-based
     * @return A list of CellData for the cells in the given area.
     */
    protected ArrayList<CellData> loadCellDataForRowAndColumnRange(
            int firstRow, int firstColumn, int lastRow, int lastColumn) {
        ArrayList<CellData> cellData = new ArrayList<CellData>();
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        Map<String, String> componentIDtoCellKeysMap = spreadsheet
                .getState(false).componentIDtoCellKeysMap;
        @SuppressWarnings("unchecked")
        final Collection<String> customComponentCells = (Collection<String>) (componentIDtoCellKeysMap == null ? Collections
                .emptyList() : componentIDtoCellKeysMap.values());
        for (int r = firstRow - 1; r < lastRow; r++) {
            Row row = activeSheet.getRow(r);
            if (row != null && row.getLastCellNum() != -1
                    && row.getLastCellNum() >= firstColumn) {
                for (int c = firstColumn - 1; c < lastColumn; c++) {
                    final String key = SpreadsheetUtil.toKey(c + 1, r + 1);
                    if (!customComponentCells.contains(key)
                            && !sentCells.contains(key)
                            && !sentFormulaCells.contains(key)) {
                        Cell cell = row.getCell(c);
                        if (cell != null) {
                            final CellData cd = createCellDataForCell(cell);
                            if (cd != null) {
                                int cellType = cell.getCellType();
                                if (cellType == Cell.CELL_TYPE_FORMULA) {
                                    sentFormulaCells.add(key);
                                } else {
                                    sentCells.add(key);
                                }
                                cellData.add(cd);
                            }
                        }
                    }
                }
            }
        }
        return cellData;
    }

    /**
     * Method for updating the spreadsheet client side visible cells and cached
     * data correctly.
     */
    protected void updateVisibleCellValues() {
        loadCellData(spreadsheet.getFirstRow(), spreadsheet.getFirstColumn(),
                spreadsheet.getLastRow(), spreadsheet.getLastColumn());
    }

    /**
     * Method for updating cells that are marked for update and formula cells.
     * 
     * Iterates over the whole sheet (existing rows and columns) and updates
     * client side cache for all sent formula cells, and cells that have been
     * marked for updating.
     * 
     */
    protected void updateMarkedCellValues() {
        final ArrayList<CellData> updatedCellData = new ArrayList<CellData>();
        Sheet sheet = spreadsheet.getActiveSheet();
        // it is unnecessary to worry about having custom components in the cell
        // because the client side handles it -> it will not replace a custom
        // component with a cell value

        // update all cached formula cell values on client side, because they
        // might have changed. also make sure all marked cells are updated
        Iterator<Row> rows = sheet.rowIterator();
        while (rows.hasNext()) {
            final Row r = rows.next();
            final Iterator<Cell> cells = r.cellIterator();
            while (cells.hasNext()) {
                final Cell cell = cells.next();
                int rowIndex = cell.getRowIndex();
                int columnIndex = cell.getColumnIndex();
                final String key = SpreadsheetUtil.toKey(columnIndex + 1,
                        rowIndex + 1);
                CellData cd = createCellDataForCell(cell);
                // update formula cells
                if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                    if (cd != null) {
                        if (sentFormulaCells.contains(key)
                                || markedCells.contains(key)) {
                            sentFormulaCells.add(key);
                            updatedCellData.add(cd);
                        }
                    } else if (sentFormulaCells.contains(key)) {
                        // in case the formula cell value has changed to null or
                        // empty; this case is probably quite rare, formula cell
                        // pointing to a cell that was removed or had its value
                        // cleared ???
                        sentFormulaCells.add(key);
                        cd = new CellData();
                        cd.col = columnIndex + 1;
                        cd.row = rowIndex + 1;
                        cd.cellStyle = "" + cell.getCellStyle().getIndex();
                        updatedCellData.add(cd);
                    }
                } else if (markedCells.contains(key)) {
                    sentCells.add(key);
                    updatedCellData.add(cd);
                }
            }
        }
        // empty cells have cell data with just col and row
        updatedCellData.addAll(removedCells);
        if (!updatedCellData.isEmpty()) {
            spreadsheet.getRpcProxy().cellsUpdated(updatedCellData);
            spreadsheet.getRpcProxy().refreshCellStyles();
        }
        markedCells.clear();
        removedCells.clear();
    }

    /**
     * Makes sure the next {@link Spreadsheet#updateMarkedCells()} call will
     * clear all removed rows from client cache.
     * 
     * @param startRow
     *            Index of the starting row, 1-based
     * @param endRow
     *            Index of the ending row, 1-based
     */
    protected void updateDeletedRowsInClientCache(int startRow, int endRow) {
        for (int i = startRow; i <= endRow; i++) {
            String rowKey = "row" + i;
            for (Iterator<String> iterator = sentCells.iterator(); iterator
                    .hasNext();) {
                String key = iterator.next();
                if (key.endsWith(rowKey)) {
                    iterator.remove();
                    CellData cd = new CellData();
                    cd.col = SpreadsheetUtil.getColumnIndexFromKey(key);
                    cd.row = i;
                    removedCells.add(cd);
                }
            }
            for (Iterator<String> iterator = sentFormulaCells.iterator(); iterator
                    .hasNext();) {
                String key = iterator.next();
                if (key.endsWith(rowKey)) {
                    iterator.remove();
                    CellData cd = new CellData();
                    cd.col = SpreadsheetUtil.getColumnIndexFromKey(key);
                    cd.row = i;
                    removedCells.add(cd);
                }
            }
        }
    }

    /**
     * Removes all the cells within the given bounds from the Spreadsheet and
     * the underlying POI model.
     * 
     * @param firstRow
     *            Starting row index, 1-based
     * @param firstColumn
     *            Starting column index, 1-based
     * @param lastRow
     *            Ending row index, 1-based
     * @param lastColumn
     *            Ending column index, 1-based
     * @param clearRemovedCellStyle
     *            true to also clear styles from the removed cells
     */
    protected void removeCells(int firstRow, int firstColumn, int lastRow,
            int lastColumn, boolean clearRemovedCellStyle) {
        final Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        for (int i = firstRow - 1; i < lastRow; i++) {
            Row row = activeSheet.getRow(i);
            if (row != null) {
                for (int j = firstColumn - 1; j < lastColumn; j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        final String key = SpreadsheetUtil.toKey(j + 1, i + 1);
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
                            spreadsheet.getSpreadsheetStyleFactory()
                                    .cellStyleUpdated(cell, true);
                        }
                        // need to make protection etc. settings for the cell
                        // won't get effected. deleting the cell would make it
                        // locked
                        if (clearRemovedCellStyle
                                || cell.getCellStyle().getIndex() == 0) {
                            CellData cd = new CellData();
                            cd.col = j + 1;
                            cd.row = i + 1;
                            removedCells.add(cd);
                        } else {
                            markedCells.add(key);
                        }
                        cell.setCellValue((String) null);
                        evaluator.notifyUpdateCell(cell);
                    }
                }
            }
        }
    }

    /**
     * Removes an individual cell from the Spreadsheet and the underlying POI
     * model.
     * 
     * @param rowIndex
     *            Row index of target cell, 1-based
     * @param colIndex
     *            Column index of target cell, 1-based
     * @param clearRemovedCellStyle
     *            true to also clear styles from the removed cell
     */
    protected void removeCell(int rowIndex, int colIndex,
            boolean clearRemovedCellStyle) {
        final Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        final Row row = activeSheet.getRow(rowIndex - 1);
        if (row != null) {
            final Cell cell = row.getCell(colIndex - 1);
            if (cell != null) {
                CellData cd = new CellData();
                cd.col = colIndex;
                cd.row = rowIndex;
                final String key = SpreadsheetUtil.toKey(colIndex, rowIndex);
                if (clearRemovedCellStyle
                        || cell.getCellStyle().getIndex() == 0) {
                    removedCells.add(cd);
                } else {
                    markedCells.add(key);
                }
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
                    spreadsheet.getSpreadsheetStyleFactory().cellStyleUpdated(
                            cell, true);
                }
                cell.setCellValue((String) null);
                evaluator.notifyUpdateCell(cell);
            }
        }
    }

    /**
     * Removes hyperlink from the given cell
     * 
     * @param cell
     *            Target cell
     * @param sheet
     *            Sheet the target cell belongs to
     */
    protected void removeHyperlink(Cell cell, Sheet sheet) {
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
            LOGGER.log(Level.FINEST, e.getMessage(), e);
        } catch (NoSuchFieldException e) {
            LOGGER.log(Level.FINEST, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.FINEST, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.FINEST, e.getMessage(), e);
        }
    }

    /**
     * Sets the cell style width ratio map
     * 
     * @param cellStyleWidthRatioMap
     *            New map
     */
    public void onCellStyleWidthRatioUpdate(
            HashMap<Integer, Float> cellStyleWidthRatioMap) {
        this.cellStyleWidthRatioMap = cellStyleWidthRatioMap;
    }

    /**
     * Clears data cache for the column at the given index
     * 
     * @param indexColumn
     *            Index of target column, 1-based
     */
    public void clearCacheForColumn(int indexColumn) {
        final String columnKey = "col" + indexColumn + " r";
        for (Iterator<String> iterator = sentCells.iterator(); iterator
                .hasNext();) {
            String key = iterator.next();
            if (key.startsWith(columnKey)) {
                iterator.remove();
            }
        }
        for (Iterator<String> iterator = sentFormulaCells.iterator(); iterator
                .hasNext();) {
            String key = iterator.next();
            if (key.startsWith(columnKey)) {
                iterator.remove();
            }
        }
    }

}
