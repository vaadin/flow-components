package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
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
