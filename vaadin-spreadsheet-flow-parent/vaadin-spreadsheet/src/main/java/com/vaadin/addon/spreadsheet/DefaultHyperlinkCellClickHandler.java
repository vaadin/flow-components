package com.vaadin.addon.spreadsheet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;

import com.vaadin.addon.spreadsheet.Spreadsheet.HyperlinkCellClickHandler;

public class DefaultHyperlinkCellClickHandler implements
        HyperlinkCellClickHandler {

    private static final DefaultHyperlinkCellClickHandler instance = new DefaultHyperlinkCellClickHandler();

    protected DefaultHyperlinkCellClickHandler() {
    }

    public static DefaultHyperlinkCellClickHandler get() {
        return instance;
    }

    @Override
    public void onHyperLinkCellClick(Cell cell, Hyperlink hyperlink,
            Spreadsheet spreadsheet) {
        if (hyperlink != null) {
            spreadsheet.getUI().getPage()
                    .open(cell.getHyperlink().getAddress(), "_new");
        } else if (isHyperlinkFormulaCell(cell)) {
            String address = getHyperlinkFunctionCellAddress(cell);
            if (address.startsWith("#")) { // inter-sheet address
                if (address.contains("!")) { // has sheet name -> change
                    String currentSheetName = cell.getSheet().getSheetName();
                    String sheetName = address.substring(
                            address.indexOf("#") + 1, address.indexOf("!"));
                    if (!currentSheetName.equals(sheetName)) {
                        int sheetPOIIndex = cell.getSheet().getWorkbook()
                                .getSheetIndex(sheetName);
                        spreadsheet.setActiveSheetWithPOIIndex(sheetPOIIndex);
                    }
                    String cellAddress = address
                            .substring(address.indexOf("#") + 1);
                    spreadsheet.initialSheetSelection = cellAddress;
                } else {
                    // change selection to cell within the same sheet
                    String cellAddress = address
                            .substring(address.indexOf("#") + 1);
                    spreadsheet.handleCellAddressChange(cellAddress);
                }
            } else {
                spreadsheet.getUI().getPage().open(address, "_new");
            }
        }
    }

    /**
     * Should only be called for cells {@link #isHyperlinkFormulaCell(Cell)}
     * returns true.
     * <p>
     * The address is inside the first quotation marks:
     * <code>HYPERLINK("address","friendly name")</code>
     * 
     * @param cell
     * @return the address that the hyperlink function points to
     */
    public final static String getHyperlinkFunctionCellAddress(Cell cell) {
        String cellFormula = cell.getCellFormula();
        int startindex = cellFormula.indexOf("\"");
        int endindex = cellFormula.indexOf('"', startindex + 1);
        String address = cellFormula.substring(startindex + 1, endindex);
        return address;
    }

    /**
     * Returns true if the cell contains a hyperlink function.
     * 
     * @param cell
     * @return
     */
    public final static boolean isHyperlinkFormulaCell(Cell cell) {
        return cell != null && cell.getCellType() == Cell.CELL_TYPE_FORMULA
                && cell.getCellFormula().startsWith("HYPERLINK(");
    }

}
