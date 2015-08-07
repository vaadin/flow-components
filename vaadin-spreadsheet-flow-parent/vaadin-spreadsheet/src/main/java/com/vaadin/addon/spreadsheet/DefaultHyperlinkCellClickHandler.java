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

import static org.apache.poi.common.usermodel.Hyperlink.LINK_DOCUMENT;

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
@SuppressWarnings("serial")
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
            if (hyperlink.getType() == LINK_DOCUMENT) { // internal
                navigateTo(cell, spreadsheet, hyperlink.getAddress());
            } else {
                spreadsheet.getUI().getPage()
                        .open(cell.getHyperlink().getAddress(), "_new");
            }
        } else if (isHyperlinkFormulaCell(cell)) {
            String address = getHyperlinkFunctionCellAddress(cell);
            if (address.startsWith("#")) { // inter-sheet address
                navigateTo(cell, spreadsheet, address.substring(1));
            } else if (address.startsWith("[") && address.contains("]")) {
                // FIXME: for now we assume that the hyperlink points to the
                // current file. Should check file name against
                // address.substring(1, address.indexOf("]"));
                navigateTo(cell, spreadsheet,
                        address.substring(address.indexOf("]") + 1));
            } else {
                spreadsheet.getUI().getPage().open(address, "_new");
            }
        }
    }

    private void navigateTo(Cell cell, Spreadsheet spreadsheet, String address) {
        if (address.contains("!")) { // has sheet name -> change
            String currentSheetName = cell.getSheet().getSheetName();
            String sheetName = address.substring(0, address.indexOf("!"));
            if (!currentSheetName.equals(sheetName)) {
                int sheetPOIIndex = cell.getSheet().getWorkbook()
                        .getSheetIndex(sheetName);
                spreadsheet.setActiveSheetWithPOIIndex(sheetPOIIndex);
            }
            spreadsheet.initialSheetSelection = address;
            spreadsheet.getCellSelectionManager().onSheetAddressChanged(
                    address, true);
        } else {
            // change selection to cell within the same sheet
            spreadsheet.getCellSelectionManager().onSheetAddressChanged(
                    address, false);
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
