/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellDeletionHandler;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellValueChangeEvent;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellValueHandler;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.FormulaValueChangeEvent;
import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.command.CellValueCommand;

/**
 * CellValueManager is an utility class of SpreadsheetClass, which handles
 * values and formatting for individual cells.
 *
 * @author Vaadin Ltd.
 * @since 23.1
 */
public class CellValueManager implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CellValueManager.class);

    private short hyperlinkStyleIndex = -1;

    /**
     * The Spreadsheet this class is tied to.
     */
    protected final Spreadsheet spreadsheet;

    private CellValueHandler customCellValueHandler;
    private CellDeletionHandler customCellDeletionHandler;

    final CellNumericFormatter numericFormatter = new CellNumericFormatter();
    final CellFitCalculator fitCalculator = new CellFitCalculator();
    private final CellDeletionGuard deletionGuard = new CellDeletionGuard(this);
    private final ClientCellCache clientCache = new ClientCellCache();
    final ClientFlushQueue flushQueue = new ClientFlushQueue();
    private final ClientCacheFlusher cacheFlusher;

    private boolean topLeftCellsLoaded;

    FormulaFormatter formulaFormatter = new FormulaFormatter();

    private final CellDataBuilder cellDataBuilder = new CellDataBuilder(this);

    /**
     * Creates a new CellValueManager and ties it to the given Spreadsheet.
     *
     * @param spreadsheet
     *            Target Spreadsheet
     */
    public CellValueManager(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
        this.cacheFlusher = new ClientCacheFlusher(spreadsheet, this,
                clientCache, flushQueue);

        UI current = UI.getCurrent();
        if (current != null) {
            updateLocale(current.getLocale());
        }
    }

    private CellSelectionManager getCellSelectionManager() {
        return spreadsheet.getCellSelectionManager();
    }

    /**
     * Clears all cached data.
     */
    public void clearCachedContent() {
        flushQueue.clear();
        clientCache.clear();
        hyperlinkStyleIndex = -1;
        topLeftCellsLoaded = false;
    }

    public DataFormatter getDataFormatter() {
        return numericFormatter.getDataFormatter();
    }

    public void setDataFormatter(DataFormatter dataFormatter) {
        numericFormatter.setDataFormatter(dataFormatter);
    }

    public DecimalFormat getOriginalValueDecimalFormat() {
        return numericFormatter.getOriginalValueDecimalFormat();
    }

    protected void updateLocale(Locale locale) {
        numericFormatter.updateLocale(locale);
    }

    /**
     * Get the common {@link FormulaEvaluator} instance from {@link Spreadsheet}
     */
    protected FormulaEvaluator getFormulaEvaluator() {
        return spreadsheet.getFormulaEvaluator();
    }

    /**
     * @return the common {@link ConditionalFormattingEvaluator} instance from
     *         {@link Spreadsheet}
     */
    protected ConditionalFormattingEvaluator getConditionalFormattingEvaluator() {
        return spreadsheet.getConditionalFormattingEvaluator();
    }

    private String getCachedFormulaCellValue(Cell formulaCell) {
        return numericFormatter.getCachedFormulaCellValue(formulaCell);
    }

    protected CellData createCellDataForCell(Cell cell) {
        return cellDataBuilder.createCellDataForCell(cell);
    }

    private void setLeadingQuoteStyle(Cell cell, boolean leadingQuote) {
        if (cell instanceof XSSFCell xssfCell) {
            xssfCell.getCellStyle().getCoreXf().setQuotePrefix(leadingQuote);
        }
    }

    /**
     * Gets the raw value of the given cell as text, rather than its formatted,
     * displayed value:
     * <ul>
     * <li>a formula without the leading {@code =};</li>
     * <li>a number without grouping and with up to 16 decimals, with the
     * decimal separator of the spreadsheet's locale;</li>
     * <li>a date as a short date and time in the spreadsheet's locale;</li>
     * <li>text, with a leading apostrophe when the cell's style has the quote
     * prefix set, which in an XLS file is every text cell;</li>
     * <li>{@code true} or {@code false} for a boolean;</li>
     * <li>the numeric error code for an error.</li>
     * </ul>
     *
     * @param cell
     *            the cell, may be {@code null}
     * @return the cell's value for editing, or an empty string for a
     *         {@code null} or blank cell
     */
    public String getOriginalCellValue(Cell cell) {
        return numericFormatter.getOriginalCellValue(cell);
    }

    boolean doesValueFit(Cell cell, String value) {
        return fitCalculator.doesValueFit(cell, value, getCellWidth(cell));
    }

    /**
     * Whether the cell's style has CSS on the client.
     * <p>
     * A style set on a cell through POI has none until the workbook styles are
     * loaded again. A number too wide for its column is shortened only in a
     * style that has CSS.
     */
    boolean hasStyleCss(Cell cell) {
        HashMap<Integer, String> css = spreadsheet.getCellStyleToCSSStyle();
        return css != null
                && css.containsKey((int) cell.getCellStyle().getIndex());
    }

    /**
     * Calculate cell width, accounting for merged cells (see #655)
     *
     * @param cell
     * @return cell width, including widths of any merged columns
     */
    protected int getCellWidth(Cell cell) {
        for (CellRangeAddress range : cell.getSheet().getMergedRegions()) {
            if (range.isInRange(cell)) {
                int w = 0;
                for (int c = range.getFirstColumn(); c <= range
                        .getLastColumn(); c++) {
                    w += spreadsheet.getColW()[c];
                }
                return w;
            }
        }
        // if we get here, cell is not in a merged region
        return spreadsheet.getColW()[cell.getColumnIndex()];
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
     * Gets the current CellDeletionHandler
     *
     * @return the customCellDeletionHandler
     */
    public CellDeletionHandler getCustomCellDeletionHandler() {
        return customCellDeletionHandler;
    }

    /**
     * Sets the current CellDeletionHandler
     *
     * @param customCellDeletionHandler
     *            the customCellDeletionHandler to set
     */
    public void setCustomCellDeletionHandler(
            CellDeletionHandler customCellDeletionHandler) {
        this.customCellDeletionHandler = customCellDeletionHandler;
    }

    /**
     * Notifies evaluator and marks cell for update on next call to
     * {@link #updateMarkedCellValues()}
     *
     * @param cell
     *            Cell to mark for updates
     */
    protected void cellUpdated(Cell cell) {
        getFormulaEvaluator().notifyUpdateCell(cell);
        markCellForUpdate(cell);
    }

    /**
     * Marks cell for update on next call to {@link #updateMarkedCellValues()}
     *
     * @param cell
     *            Cell to mark for updates
     */
    protected void markCellForUpdate(Cell cell) {
        flushQueue.markForUpdate(SpreadsheetUtil.toKey(cell));
    }

    /**
     * Marks the given cell as deleted and notifies the evaluator
     *
     * @param cell
     *            Deleted cell
     */
    protected void cellDeleted(Cell cell) {
        getFormulaEvaluator().notifyDeleteCell(cell);
        spreadsheet.removeInvalidFormulaMark(cell.getColumnIndex() + 1,
                cell.getRowIndex() + 1);
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
        flushQueue.enqueueRemoval(cd);
        clearCellCache(cellKey);
    }

    /**
     * Clears the cell with the given key from the cache
     *
     * @param cellKey
     *            Key of target cell
     */
    protected void clearCellCache(String cellKey) {
        clientCache.dropSent(cellKey);
    }

    /**
     * Updates the cell value and type, causes a recalculation of all the values
     * in the cell.
     *
     * If there is a {@link CellValueHandler} defined, then it is used.
     *
     * Cells starting with "=" or "+" will be created/changed into FORMULA type.
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
     *            with an extra "=" or "+"
     */
    public void onCellValueChange(int col, int row, String value) {
        Workbook workbook = spreadsheet.getWorkbook();
        // update cell value
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        Row r = activeSheet.getRow(row - 1);
        if (r == null) {
            r = activeSheet.createRow(row - 1);
        }
        Cell cell = r.getCell(col - 1);
        String formattedCellValue = null;
        CellType oldCellType = CellType._NONE;

        // capture cell value to history
        CellValueCommand command = new CellValueCommand(spreadsheet);
        command.captureCellValues(
                new CellReference(spreadsheet.getActiveSheet().getSheetName(),
                        row - 1, col - 1, false, false));
        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);
        boolean updateHyperlinks = false;
        boolean formulaChanged = false;

        boolean handlerTookOver = getCustomCellValueHandler() != null
                && !getCustomCellValueHandler().cellValueUpdated(cell,
                        activeSheet, col - 1, row - 1, value,
                        getFormulaEvaluator(),
                        numericFormatter.getDataFormatter(),
                        getConditionalFormattingEvaluator());

        if (!handlerTookOver) {
            Exception exception = null;
            try {
                // handle new cell creation
                SpreadsheetStyleFactory styler = spreadsheet
                        .getSpreadsheetStyleFactory();
                final Locale spreadsheetLocale = spreadsheet.getLocale();
                if (cell == null) {
                    cell = r.createCell(col - 1);
                } else {
                    // modify existing cell, possibly switch type
                    formattedCellValue = getFormattedCellValue(cell);
                    final String key = SpreadsheetUtil.toKey(col, row);
                    oldCellType = cell.getCellType();
                    clientCache.dropSent(key);

                    // Old value was hyperlink => needs refresh
                    if (cell.getCellType() == CellType.FORMULA
                            && cell.getCellFormula().startsWith("HYPERLINK")) {
                        updateHyperlinks = true;
                    }
                    setLeadingQuoteStyle(cell, false);
                }
                if (formulaFormatter.isFormulaFormat(value)) {
                    if (formulaFormatter.isValidFormulaFormat(value,
                            spreadsheetLocale)) {
                        spreadsheet.removeInvalidFormulaMark(col, row);
                        getFormulaEvaluator().notifyUpdateCell(cell);
                        String newFormula = formulaFormatter
                                .unFormatFormulaValue(value.substring(1),
                                        spreadsheetLocale);

                        formulaChanged = ((cell
                                .getCellType() == CellType.FORMULA)
                                && !newFormula.equals(cell.getCellFormula()));
                        cell.setCellFormula(newFormula);
                        getFormulaEvaluator().notifySetFormula(cell);
                        if (value.startsWith("=HYPERLINK(")
                                && cell.getCellStyle()
                                        .getIndex() != hyperlinkStyleIndex) {
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
                            updateHyperlinks = true;
                        }
                    } else {
                        // it's formula but invalid
                        cell.setCellValue(value);
                        spreadsheet.markInvalidFormula(col, row);
                    }
                } else {
                    if (oldCellType == CellType.FORMULA) {
                        // The old cell type was formula.
                        // Set the cell blank to clear the old formula first.
                        cell.setBlank();
                    }

                    spreadsheet.removeInvalidFormulaMark(col, row);
                    Double percentage = SpreadsheetUtil.parsePercentage(value,
                            spreadsheetLocale);
                    Double numVal = SpreadsheetUtil.parseNumber(cell, value,
                            spreadsheetLocale);
                    if (value.isEmpty()) {
                        cell.setBlank();
                    } else if (percentage != null) {
                        CellStyle cs = cell.getCellStyle();
                        // Do not use default cell style (index == 0) for
                        // percentage as it will affect all cells
                        if (cs == null || cs.getIndex() == 0) {
                            cs = workbook.createCellStyle();
                            cell.setCellStyle(cs);
                        }

                        if (cs.getDataFormatString() != null
                                && !cs.getDataFormatString().contains("%")) {
                            cs.setDataFormat(workbook.createDataFormat()
                                    .getFormat(spreadsheet
                                            .getDefaultPercentageFormat()));
                            styler.cellStyleUpdated(cell, true);
                        }
                        cell.setCellValue(percentage);
                    } else if (numVal != null) {
                        cell.setCellValue(numVal);
                    } else if (oldCellType == CellType.BOOLEAN) {
                        cell.setCellValue(Boolean.parseBoolean(value));
                    } else {
                        if (value.startsWith("'")) {
                            value = value.substring(1, value.length());
                            setLeadingQuoteStyle(cell, true);
                        }
                        cell.setCellValue(value);
                    }
                }

            } catch (FormulaParseException fpe) {
                try {
                    exception = fpe;

                    // parses formula
                    cell.setCellFormula(value.substring(1).replace(" ", ""));
                } catch (FormulaParseException fpe2) {
                    exception = fpe2;
                    /*
                     * We could force storing the formula even if it is invalid.
                     * Instead, just store it as the value. Clearing the formula
                     * makes sure the value is displayed as-is.
                     */
                    cell.setCellValue(value);
                    spreadsheet.markInvalidFormula(col, row);
                }
            } catch (NumberFormatException nfe) {
                exception = nfe;
                cell.setCellValue(value);
            } catch (Exception e) {
                exception = e;
                cell.setCellValue(value);
            } finally {
                getFormulaEvaluator().notifyUpdateCell(cell);
            }
            if (cell != null) {
                markCellForUpdate(cell);
                if (formattedCellValue == null
                        || !formattedCellValue
                                .equals(getFormattedCellValue(cell))
                        || oldCellType != cell.getCellType()
                        || formulaChanged) {
                    fireCellValueChangeEvent(cell);
                }
            }
            if (exception != null) {
                LOGGER.trace("Failed to parse cell value for cell at col " + col
                        + " row " + row + " (" + exception.getMessage() + ")",
                        exception);
            }
        }

        if (handlerTookOver) {
            refreshCellHandledByCustomHandler(activeSheet, col, row);
        }

        spreadsheet.updateMarkedCells();

        if (updateHyperlinks) {
            spreadsheet.loadHyperLinks();
        }
    }

    /**
     * Sends the edited cell back to the client after a {@link CellValueHandler}
     * has taken over the edit.
     * <p>
     * The client displays the text the user typed as soon as the editor closes.
     * A handler is free to store something else, or nothing at all, so that
     * text is never the authoritative value and the cell has to be sent again.
     * Without this the client keeps showing the raw input until something else
     * happens to refresh the cell.
     *
     * @param sheet
     *            the sheet the edited cell belongs to
     * @param col
     *            column index of the edited cell, 1-based
     * @param row
     *            row index of the edited cell, 1-based
     */
    private void refreshCellHandledByCustomHandler(Sheet sheet, int col,
            int row) {
        Row r = sheet.getRow(row - 1);
        // The handler may have created the cell, so it is read again instead
        // of reusing the reference the handler was given.
        Cell cell = r == null ? null : r.getCell(col - 1);
        if (cell == null) {
            CellData cd = new CellData();
            cd.col = col;
            cd.row = row;
            flushQueue.enqueueRemoval(cd);
            clearCellCache(SpreadsheetUtil.toKey(col, row));
        } else {
            spreadsheet.markCellAsUpdated(cell, true);
        }
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
            return numericFormatter.getDataFormatter().formatCellValue(cell,
                    getFormulaEvaluator(), getConditionalFormattingEvaluator());
        } catch (RuntimeException rte) {
            return null;
        }
    }

    private void fireCellValueChangeEvent(Cell cell) {
        Set<CellReference> cells = new HashSet<>();
        CellReference ref = new CellReference(cell);
        cells.add(ref);
        spreadsheet.fireEvent(new CellValueChangeEvent(spreadsheet, cells));
    }

    void fireFormulaValueChangeEvent(Set<CellReference> changedCells) {
        spreadsheet.fireEvent(
                new FormulaValueChangeEvent(spreadsheet, changedCells));
    }

    private void fireCellValueChangeEvent(Set<CellReference> changedCells) {
        spreadsheet
                .fireEvent(new CellValueChangeEvent(spreadsheet, changedCells));
    }

    /**
     * Deletes the currently selected cells' values. Does not affect styles.
     */
    public void onDeleteSelectedCells() {
        final Sheet activeSheet = spreadsheet.getActiveSheet();
        CellReference selectedCellReference = getCellSelectionManager()
                .getSelectedCellReference();
        // TODO show error on locked cells instead
        if (selectedCellReference != null) {
            Row row = activeSheet.getRow(selectedCellReference.getRow());
            if (row != null && spreadsheet.isCellLocked(
                    new CellAddress(selectedCellReference.getRow(),
                            selectedCellReference.getCol()))) {
                return;
            }
        }
        List<CellReference> individualSelectedCells = getCellSelectionManager()
                .getIndividualSelectedCells();
        for (CellReference cr : individualSelectedCells) {
            final Row row = activeSheet.getRow(cr.getRow());
            if (row != null && spreadsheet
                    .isCellLocked(new CellAddress(cr.getRow(), cr.getCol()))) {
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

        boolean selectedIsInRange = deletionGuard
                .isInRange(selectedCellReference, cellRangeAddresses);
        boolean cellDeletionCheckPassed = !selectedIsInRange
                && individualSelectedCells.isEmpty()
                && deletionGuard.passesDeletionCheck(selectedCellReference);
        boolean individualCellsDeletionCheckPassed;
        if (selectedCellReference == null) {
            individualCellsDeletionCheckPassed = deletionGuard
                    .passesIndividualDeletionCheck(individualSelectedCells);
        } else if (!selectedIsInRange && !individualSelectedCells.isEmpty()) {
            List<CellReference> individualSelectedCellsIncludingCurrentSelection = new ArrayList<CellReference>(
                    individualSelectedCells);
            individualSelectedCellsIncludingCurrentSelection
                    .add(selectedCellReference);
            individualCellsDeletionCheckPassed = deletionGuard
                    .passesIndividualDeletionCheck(
                            individualSelectedCellsIncludingCurrentSelection);
            cellDeletionCheckPassed = individualCellsDeletionCheckPassed;
        } else {
            individualCellsDeletionCheckPassed = deletionGuard
                    .passesIndividualDeletionCheck(individualSelectedCells);
        }
        boolean cellRangeDeletionCheckPassed = deletionGuard
                .passesRangeDeletionCheck(cellRangeAddresses);
        // at least one of the selection types must pass the check and have
        // contents
        if ((selectedCellReference == null || !cellDeletionCheckPassed)
                && (individualSelectedCells.isEmpty()
                        || !individualCellsDeletionCheckPassed)
                && (cellRangeAddresses.isEmpty()
                        || !cellRangeDeletionCheckPassed)) {
            return;
        }
        if (!cellDeletionCheckPassed) {
            selectedCellReference = null;
        }
        if (!individualCellsDeletionCheckPassed) {
            individualSelectedCells.clear();
        }
        if (!cellRangeDeletionCheckPassed) {
            cellRangeAddresses.clear();
        }

        CellValueCommand command = new CellValueCommand(spreadsheet);
        if (selectedCellReference != null && !selectedIsInRange) {
            command.captureCellValues(selectedCellReference);
        }
        for (CellReference cr : individualSelectedCells) {
            command.captureCellValues(cr);
        }
        for (CellRangeAddress range : cellRangeAddresses) {
            command.captureCellRangeValues(range);
        }
        if (selectedCellReference != null && !selectedIsInRange) {
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
        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);
        fireCellValueChangeEvent(spreadsheet.getSelectedCellReferences());
        spreadsheet.updateMarkedCells();
        spreadsheet.loadHyperLinks();
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
    protected void parseValueIntoNumericCell(final Cell cell,
            final String value) {
        // try to parse the string with the existing cell format
        Format oldFormat = numericFormatter.getDataFormatter()
                .createFormat(cell);
        if (oldFormat != null) {
            try {
                final Object parsedObject = oldFormat.parseObject(value);
                if (parsedObject instanceof Date date) {
                    cell.setCellValue(date);
                } else if (parsedObject instanceof Calendar cal) {
                    cell.setCellValue(cal);
                } else if (parsedObject instanceof Number num) {
                    cell.setCellValue(num.doubleValue());
                } else {
                    cell.setCellValue(Double.parseDouble(value));
                }
            } catch (ParseException pe) {
                LOGGER.trace("Could not parse String to format, "
                        + oldFormat.getClass() + ", "
                        + cell.getCellStyle().getDataFormatString() + " : "
                        + pe.getMessage(), pe);
                try {
                    cell.setCellValue(Double.parseDouble(value));
                } catch (NumberFormatException nfe) {
                    LOGGER.trace("Could not parse String to Double: "
                            + nfe.getMessage(), nfe);
                    cell.setCellValue(value);
                }
            } catch (NumberFormatException nfe) {
                LOGGER.trace(
                        "Could not parse String to Double: " + nfe.getMessage(),
                        nfe);
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
                    spreadsheet.getRpcProxy()
                            .updateTopLeftCellValues(topLeftData);
                }
            }

            if (verticalSplitPosition > 0) { // top right pane
                ArrayList<CellData> topRightData = loadCellDataForRowAndColumnRange(
                        1, firstColumn, verticalSplitPosition, lastColumn);
                if (!topRightData.isEmpty()) {
                    spreadsheet.getRpcProxy()
                            .updateTopRightCellValues(topRightData);
                }
            }
            if (horizontalSplitPosition > 0) { // bottom left pane
                ArrayList<CellData> bottomLeftData = loadCellDataForRowAndColumnRange(
                        firstRow, 1, lastRow, horizontalSplitPosition);
                if (!bottomLeftData.isEmpty()) {
                    spreadsheet.getRpcProxy()
                            .updateBottomLeftCellValues(bottomLeftData);
                }
            }

            ArrayList<CellData> bottomRightData = loadCellDataForRowAndColumnRange(
                    firstRow, firstColumn, lastRow, lastColumn);
            if (!bottomRightData.isEmpty()) {
                spreadsheet.getRpcProxy()
                        .updateBottomRightCellValues(bottomRightData);
            }
        } catch (NullPointerException npe) {
            LOGGER.trace(npe.getMessage(), npe);
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
    protected ArrayList<CellData> loadCellDataForRowAndColumnRange(int firstRow,
            int firstColumn, int lastRow, int lastColumn) {
        ArrayList<CellData> cellData = new ArrayList<CellData>();
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        final Collection<String> customComponentCells = spreadsheet
                .getComponentIDtoCellKeysMap().values();
        for (int r = firstRow - 1; r < lastRow; r++) {
            Row row = activeSheet.getRow(r);
            if (row != null && row.getLastCellNum() != -1
                    && row.getLastCellNum() >= firstColumn) {
                for (int c = firstColumn - 1; c < lastColumn; c++) {
                    final String key = SpreadsheetUtil.toKey(c + 1, r + 1);
                    if (!customComponentCells.contains(key)
                            && !clientCache.containsNormal(key)
                            && !clientCache.containsFormula(key)) {
                        Cell cell = row.getCell(c);
                        if (cell != null) {
                            final CellData cd = createCellDataForCell(cell);
                            if (cd != null) {
                                CellType cellType = cell.getCellType();
                                if (cellType == CellType.FORMULA) {
                                    clientCache.markFormulaSent(key);
                                } else {
                                    clientCache.markNormalSent(key);
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
        cacheFlusher.flush();
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
        for (CellData cd : clientCache.pruneRowsRange(startRow, endRow)) {
            flushQueue.enqueueRemoval(cd);
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
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        for (int i = firstRow - 1; i < lastRow; i++) {
            Row row = activeSheet.getRow(i);
            if (row != null) {
                for (int j = firstColumn - 1; j < lastColumn; j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        final String key = SpreadsheetUtil.toKey(j + 1, i + 1);
                        clientCache.dropSent(key);
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
                            flushQueue.enqueueRemoval(cd);
                        } else {
                            flushQueue.markForUpdate(key);
                        }
                        cell.setCellValue((String) null);
                        getFormulaEvaluator().notifyUpdateCell(cell);

                        spreadsheet.removeInvalidFormulaMark(
                                cell.getColumnIndex() + 1,
                                cell.getRowIndex() + 1);
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
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
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
                    flushQueue.enqueueRemoval(cd);
                } else {
                    flushQueue.markForUpdate(key);
                }
                clientCache.dropSent(key);
                // POI (3.9) doesn't have a method for removing a hyperlink !!!
                if (cell.getHyperlink() != null) {
                    removeHyperlink(cell, activeSheet);
                }
                if (clearRemovedCellStyle) {
                    // update style to 0
                    cell.setCellStyle(null);
                    spreadsheet.getSpreadsheetStyleFactory()
                            .cellStyleUpdated(cell, true);
                }
                cell.setCellValue((String) null);
                getFormulaEvaluator().notifyUpdateCell(cell);

                spreadsheet.removeInvalidFormulaMark(cell.getColumnIndex() + 1,
                        cell.getRowIndex() + 1);
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
            LOGGER.trace(e.getMessage(), e);
        } catch (NoSuchFieldException e) {
            LOGGER.trace(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            LOGGER.trace(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.trace(e.getMessage(), e);
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
        boolean changed = fitCalculator
                .onWidthRatioUpdate(cellStyleWidthRatioMap);
        if (changed && spreadsheet.getFirstColumn() != -1) {
            // Whether a value fits its column - and so whether it is
            // shortened, spills into its neighbours or shows hashes - is
            // judged against these ratios, and the client can only measure
            // them once the styles have arrived. That is after the first
            // batch of cells was built, so those cells went out judged
            // against nothing: a freshly loaded workbook never spilled its
            // text. Rebuild them now that there is something to judge by -
            // the ones in view, which are the ones the client has. A cell
            // further away is built when it is first sent, and by then the
            // ratios are known; rebuilding the whole sheet here sent every
            // cell of a 500 x 200 workbook, 9 MB, as it loaded.
            clearCachedContent();
            updateVisibleCellValues();
        }
    }

    /**
     * Clears data cache for the column at the given index
     *
     * @param indexColumn
     *            Index of target column, 1-based
     */
    public void clearCacheForColumn(int indexColumn) {
        clientCache.pruneColumn(indexColumn);
    }

    /**
     * Sends the cells whose text runs into the given column again, after the
     * column's width changed: in each row, the nearest cell to the left of the
     * column that holds a value, if the client has it and its text is too wide
     * for its own column.
     *
     * @param columnIndex
     *            the column, 0-based
     */
    void resendTextRunningInto(int columnIndex) {
        ArrayList<CellData> resent = new ArrayList<>();
        for (Row row : spreadsheet.getActiveSheet()) {
            Cell source = CellDataBuilder.spillSourceOf(row, columnIndex);
            if (source == null) {
                continue;
            }
            String key = SpreadsheetUtil.toKey(source);
            if (clientCache.containsNormal(key)
                    || clientCache.containsFormula(key)) {
                CellData cellData = createCellDataForCell(source);
                if (cellData != null && cellData.overflowWidth > 0) {
                    resent.add(cellData);
                }
            }
        }
        if (!resent.isEmpty()) {
            spreadsheet.getRpcProxy().cellsUpdated(resent);
        }
    }

}
