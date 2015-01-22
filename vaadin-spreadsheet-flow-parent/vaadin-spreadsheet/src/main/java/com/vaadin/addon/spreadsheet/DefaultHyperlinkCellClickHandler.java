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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;

import com.vaadin.addon.spreadsheet.Spreadsheet.HyperlinkCellClickHandler;

/**
 * Default implementation of the {@link HyperlinkCellClickHandler} interface.
 * Handles links to cells in either the same or some other sheet, as well as
 * external URLs.
 * 
 * @author Vaadin Ltd.
 * @since 1.0
 */
public class DefaultHyperlinkCellClickHandler implements
        HyperlinkCellClickHandler {

    private static final DefaultHyperlinkCellClickHandler instance = new DefaultHyperlinkCellClickHandler();

    protected DefaultHyperlinkCellClickHandler() {
    }

    /**
     * Returns the static singleton instance of
     * DefaultHyperlinkCellClickHandler.
     * 
     * @return singleton instance of the handler
     */
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
                    spreadsheet.getCellSelectionManager()
                            .onSheetAddressChanged(cellAddress);
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
     *            Target cell containing a hyperlink function
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
     *            Cell to investigate
     * @return True if hyperlink is found
     */
    public final static boolean isHyperlinkFormulaCell(Cell cell) {
        return cell != null && cell.getCellType() == Cell.CELL_TYPE_FORMULA
                && cell.getCellFormula().startsWith("HYPERLINK(");
    }

}
