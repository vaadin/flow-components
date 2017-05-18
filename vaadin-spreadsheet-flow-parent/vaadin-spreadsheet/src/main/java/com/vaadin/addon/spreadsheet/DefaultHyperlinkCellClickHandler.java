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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String address = getHyperlinkFunctionCellAddress(cell, spreadsheet);
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
                int sheetPOIIndex = getSheetIndex(cell, sheetName);
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

    private int getSheetIndex(Cell cell, String sheetName) {
        // if name contains only numbers or contains spaces it's enclosed in
        // single quotes
        if (sheetName.charAt(0) == '\''
                && sheetName.charAt(sheetName.length() - 1) == '\'') {
            sheetName = sheetName.substring(1, sheetName.length() - 1);
        }
        return cell.getSheet().getWorkbook().getSheetIndex(sheetName);

    }

    /**
     * Should only be called for cells {@link #isHyperlinkFormulaCell(Cell)}
     * returns true.
     * <p>
     * The address is inside the first argument:
     * <code>HYPERLINK("address","friendly name")</code>
     * or
     * <code>HYPERLINK(D5,"friendly name")</code>
     *
     * @param cell
     *            Target cell containing a hyperlink function
     * @param spreadsheet
     *            spreadsheet for evaluating the first argument (formula case) 
     * @return the address that the hyperlink function points to
     */
    public final static String getHyperlinkFunctionCellAddress(Cell cell,
        Spreadsheet spreadsheet) {
        String address = "";
        final String firstArg = getFirstArgumentFromFormula(cell.getCellFormula());

        final boolean isDirectLink = firstArg.startsWith("\"") && firstArg.endsWith("\"");

        if (isDirectLink) {
            address = firstArg.substring(1, firstArg.length() - 1);
        } else { // address is specified in a cell
            Cell firstArgCell = spreadsheet.getCell(firstArg);

            if (firstArgCell != null) {
                address = spreadsheet.getCellValue(firstArgCell);
            }
        }

        return address;
    }

    private static String getFirstArgumentFromFormula(String cellFormula) {
        // matches (arg1[;...]) with possible whitespace between any constituents.
        Pattern pattern = Pattern.compile("\\(\\s*(\\w*?|\".*?\")\\s*(,.*)?\\)");
        Matcher matcher = pattern.matcher(cellFormula);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
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
