package com.vaadin.addon.spreadsheet.client;


public interface FormulaBarHandler {

    void onAddressEntered(String value);

    void onAddressFieldEsc();

    /** Swap the cell data to the value if it is a formula. */
    void onFormulaFieldFocus(String value);

    void onFormulaFieldBlur(String value);

    void onFormulaEnter(String value);

    void onFormulaTab(String value);

    void onFormulaEsc();

    void onFormulaValueChange(String value);

}
