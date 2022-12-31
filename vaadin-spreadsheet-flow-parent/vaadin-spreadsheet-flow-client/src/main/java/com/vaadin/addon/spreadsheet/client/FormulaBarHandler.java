/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

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
