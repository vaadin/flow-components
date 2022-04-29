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
     * Run when address field contains named range This creates new range or
     * selects already existing one.
     *
     * @param value
     *            Address field value
     */
    public void onNamedRange(String value) {
        Workbook workbook = spreadsheet.getWorkbook();
        Name name = workbook.getName(value);
        if (name == null) {
            createNewNamedRange(value);
        } else {
            selectExistingNameRange(name);
        }
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

    private void selectExistingNameRange(Name name) {
        String rangeFormula = name.getRefersToFormula();
        String formulaSheet = name.getSheetName();

        final boolean rangeIsOnDifferentSheet = !name.getSheetName()
                .equals(spreadsheet.getActiveSheet().getSheetName());

        if (rangeIsOnDifferentSheet) {
            switchSheet(formulaSheet, rangeFormula);
        } else {
            selectFormula(rangeFormula, name.getNameName());
        }
    }

    private void switchSheet(String formulaSheet, String range) {
        if (!spreadsheet.getActiveSheet().getSheetName().equals(formulaSheet)) {
            int sheetIndex = spreadsheet.getWorkbook()
                    .getSheetIndex(formulaSheet);
            spreadsheet.setActiveSheetIndex(sheetIndex);
            spreadsheet.initialSheetSelection = range;
        }
    }

    private void selectFormula(String formula, String name) {
        if (formula.indexOf(":") == -1) {
            final CellReference cell = new CellReference(formula);

            getSelectionManager().handleCellAddressChange(cell.getRow() + 1,
                    cell.getCol() + 1, false, name);
        } else {
            CellRangeAddress cra = spreadsheet
                    .createCorrectCellRangeAddress(formula);

            getSelectionManager().handleCellRangeSelection(name, cra);
        }
    }
}
