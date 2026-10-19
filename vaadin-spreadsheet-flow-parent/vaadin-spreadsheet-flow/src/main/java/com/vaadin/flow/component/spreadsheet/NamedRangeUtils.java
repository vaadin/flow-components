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
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellReference.NameType;

class NamedRangeUtils implements Serializable {

    private Spreadsheet spreadsheet;

    public NamedRangeUtils(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    private CellSelectionManager getSelectionManager() {
        return spreadsheet.getCellSelectionManager();
    }

    public String getNameForFormulaIfExists(CellRangeAddress cra) {
        final String sheetName = spreadsheet.getActiveSheet().getSheetName();
        final String formula = cra.formatAsString(sheetName, true);

        for (Name name : spreadsheet.getWorkbook().getAllNames()) {
            final boolean globalName = name.getSheetIndex() == -1;
            final boolean nameRefersToThisSheet = name
                    .getSheetIndex() == spreadsheet.getActiveSheetIndex();

            if (globalName || nameRefersToThisSheet) {
                if (formula.equals(name.getRefersToFormula())) {
                    return name.getNameName();
                }
            }
        }

        return null;
    }

    /**
     * Check if entered range is cell reference
     *
     * @param value
     *            New value of the address field
     */
    public boolean isCellReference(String value) {
        CellReference.NameType nameType = getCellReferenceType(value);
        List<CellReference.NameType> cellColRowTypes = Arrays
                .asList(NameType.CELL, NameType.COLUMN, NameType.ROW);
        if (cellColRowTypes.contains(nameType)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * What typing a name into the address field does, read before anything is
     * done: a name not there yet is made for the selection, and selects nothing
     * more; one there selects what it refers to, on its sheet. What POI cannot
     * read of the name throws here, as an {@link IllegalArgumentException}; the
     * selection returned only selects, and what it throws, a listener's
     * included, is not about the name.
     *
     * @param value
     *            Address field value
     * @return what selects the range the name refers to
     */
    Runnable selectionFor(String value) {
        Workbook workbook = spreadsheet.getWorkbook();
        Name name = workbook.getName(value);
        if (name == null) {
            createNewNamedRange(value);
            return () -> {
            };
        }
        String rangeFormula = name.getRefersToFormula();
        String formulaSheet = name.getSheetName();
        if (!formulaSheet.equals(spreadsheet.getActiveSheet().getSheetName())) {
            return () -> switchSheet(formulaSheet, rangeFormula);
        }
        String nameName = name.getNameName();
        if (rangeFormula.indexOf(":") == -1) {
            final CellReference cell = new CellReference(rangeFormula);
            return () -> getSelectionManager().handleCellAddressChange(
                    cell.getRow() + 1, cell.getCol() + 1, false, nameName);
        }
        CellRangeAddress cra = spreadsheet
                .createCorrectCellRangeAddress(rangeFormula);
        return () -> getSelectionManager().handleCellRangeSelection(nameName,
                cra);
    }

    /**
     * Check if entered range is cell reference
     *
     * @param value
     *            New value of the address field
     */
    public boolean isNamedRange(String value) {
        CellReference.NameType nameType = getCellReferenceType(value);
        if (NameType.NAMED_RANGE.equals(nameType)) {
            return true;
        } else {
            return false;
        }
    }

    private CellReference.NameType getCellReferenceType(String value) {
        SpreadsheetVersion spreadsheetVersion = getSpreadsheetVersion();
        return CellReference.classifyCellReference(value, spreadsheetVersion);
    }

    private SpreadsheetVersion getSpreadsheetVersion() {
        return spreadsheet.getWorkbook().getSpreadsheetVersion();
    }

    private void createNewNamedRange(String newName) {
        Workbook workbook = spreadsheet.getWorkbook();

        Name name = workbook.createName();
        name.setNameName(newName);
        name.setRefersToFormula(getSelectedRangeFormula());

        SpreadsheetFactory.loadNamedRanges(spreadsheet);
    }

    private String getSelectedRangeFormula() {
        String sheetName = spreadsheet.getActiveSheet().getSheetName();

        return getSelectionManager().getSelectedCellRange()
                .formatAsString(sheetName, true);
    }

    private void switchSheet(String formulaSheet, String range) {
        if (!spreadsheet.getActiveSheet().getSheetName().equals(formulaSheet)) {
            int sheetIndex = spreadsheet.getWorkbook()
                    .getSheetIndex(formulaSheet);
            spreadsheet.setActiveSheetIndex(sheetIndex);
            // The address field reads an address of the sheet shown, without
            // the sheet's name: a single cell with it is no address at all.
            spreadsheet.pendingSelection()
                    .select(range.substring(range.lastIndexOf('!') + 1));
        }
    }
}
