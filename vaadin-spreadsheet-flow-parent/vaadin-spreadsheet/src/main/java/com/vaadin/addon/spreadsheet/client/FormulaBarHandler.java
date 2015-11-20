package com.vaadin.addon.spreadsheet.client;

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

public interface FormulaBarHandler {

    void onAddressEntered(String value);

    void onAddressFieldEsc();

    /** Swap the cell data to the value if it is a formula. */
    void onFormulaFieldFocus(String value);

    void onFormulaFieldBlur(String value);

    void onFormulaEnter(String value);

    void onFormulaTab(String value, boolean focusSheet);

    void onFormulaEsc();

    void onFormulaValueChange(String value);

    boolean isTouchMode();

    void setSheetFocused(boolean focused);

    String createCellAddress(int column, int row);

    String[] getSheetNames();

    String getActiveSheetName();

}
