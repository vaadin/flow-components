/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.spreadsheet.ItemFilter;
import com.vaadin.flow.component.spreadsheet.PopupButton;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetFilterTable;

class FilterTableTest {

    private Spreadsheet spreadsheet;
    private SpreadsheetFilterTable table;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();

        int maxColumns = 5;
        int maxRows = 5;

        for (int column = 1; column < maxColumns + 1; column++) {
            spreadsheet.createCell(1, column, "Column " + column);

            for (int row = 2; row < maxRows + 2; row++) {
                spreadsheet.createCell(row, column, row + column);
            }
        }

        var range = new CellRangeAddress(1, maxRows, 1, maxColumns);
        table = new SpreadsheetFilterTable(spreadsheet, range);
        spreadsheet.registerTable(table);
        spreadsheet.refreshAllCellValues();
    }

    @Test
    void init_filterRegion() {
        Assertions.assertEquals("B3:F6",
                table.getFilteringRegion().formatAsString());
    }

    @Test
    void init_fullTableRegion() {
        Assertions.assertEquals("B2:F6",
                table.getFullTableRegion().formatAsString());
    }

    @Test
    void init_filteredRows_empty() {
        Assertions.assertEquals(0, getItemFilter().getFilteredRows().size());
    }

    @Test
    void filter_filteredRows_hasFilteredRow() {
        // Filter out row 4
        getFilterCheckboxGroup().deselect("4");

        Assertions.assertEquals(1, getItemFilter().getFilteredRows().size());
        Assertions.assertEquals(3,
                getItemFilter().getFilteredRows().iterator().next().intValue());
    }

    @Test
    void filter_filteredRows_rowHidden() {
        getFilterCheckboxGroup().deselect("4");

        Assertions.assertTrue(spreadsheet.isRowHidden(3));
    }

    @Test
    void init_clearButtonDisabled() {
        Assertions.assertFalse(getClearButton().isEnabled());
    }

    @Test
    void filter_filteredAllRows_clearButtonEnabled() {
        getFilterCheckboxGroup().deselectAll();

        Assertions.assertTrue(getClearButton().isEnabled());
    }

    @Test
    void init_popupButtonInactive() {
        Assertions.assertFalse(getPopupButton().isActive());
    }

    @Test
    void filter_popupButtonActive() {
        getFilterCheckboxGroup().deselect("4");

        Assertions.assertTrue(getPopupButton().isActive());
    }

    @Test
    void filter_clearAllButtonClick_filtersCleared() {
        getFilterCheckboxGroup().deselect("4");
        getClearButton().click();

        Assertions.assertEquals(0, getItemFilter().getFilteredRows().size());
        Assertions.assertFalse(spreadsheet.isRowHidden(3));
        Assertions.assertFalse(getClearButton().isEnabled());
        Assertions.assertFalse(getPopupButton().isActive());
    }

    @Test
    void filter_clearAndReload_filtersCleared() {
        getFilterCheckboxGroup().deselect("4");
        table.clear();
        table.reload();
        table.onFiltersUpdated();

        Assertions.assertEquals(0, getItemFilter().getFilteredRows().size());
        Assertions.assertFalse(spreadsheet.isRowHidden(3));
        Assertions.assertFalse(getClearButton().isEnabled());
        Assertions.assertFalse(getPopupButton().isActive());
    }

    @Test
    void filter_unregisterFilter_filtersCleared() {
        getFilterCheckboxGroup().deselect("4");
        table.unRegisterFilter(getPopupButton(), getItemFilter());
        table.onFiltersUpdated();

        Assertions.assertFalse(spreadsheet.isRowHidden(3));
    }

    @Test
    void registerFilter_uknownPopupButton_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> table.registerFilter(new PopupButton(), getItemFilter()));
    }

    @Test
    void filteredTable_unhideRowAndOpenPopup_rowIsUnhidden() {
        getFilterCheckboxGroup().deselect("4");

        spreadsheet.setRowHidden(3, false);
        getPopupButton().openPopup();

        Assertions.assertFalse(spreadsheet.isRowHidden(3));
    }

    private CheckboxGroup<String> getFilterCheckboxGroup() {
        return (CheckboxGroup<String>) getItemFilter().getChildren()
                .filter(component -> component instanceof CheckboxGroup)
                .findFirst().get();
    }

    private ItemFilter getItemFilter() {
        return (ItemFilter) getPopupButtonChildren().get(0);
    }

    private Button getClearButton() {
        return (Button) getPopupButtonChildren().get(1);
    }

    private List<Component> getPopupButtonChildren() {
        return getPopupButton().getContent().getChildren()
                .collect(Collectors.toList());
    }

    private PopupButton getPopupButton() {
        return table.getPopupButton(1);
    }
}
