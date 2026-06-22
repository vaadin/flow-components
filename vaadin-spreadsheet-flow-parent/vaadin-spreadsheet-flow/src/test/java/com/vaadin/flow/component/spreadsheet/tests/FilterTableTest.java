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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.spreadsheet.ItemFilter;
import com.vaadin.flow.component.spreadsheet.PopupButton;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetFilterTable;

public class FilterTableTest {

    private Spreadsheet spreadsheet;
    private SpreadsheetFilterTable table;

    @Before
    public void init() {
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
    public void init_filterRegion() {
        Assert.assertEquals("B3:F6",
                table.getFilteringRegion().formatAsString());
    }

    @Test
    public void init_fullTableRegion() {
        Assert.assertEquals("B2:F6",
                table.getFullTableRegion().formatAsString());
    }

    @Test
    public void init_filteredRows_empty() {
        Assert.assertEquals(0, getItemFilter(1).getFilteredRows().size());
    }

    @Test
    public void filter_filteredRows_hasFilteredRow() {
        // Filter out row 4
        getFilterCheckboxGroup(1).deselect("4");

        Assert.assertEquals(1, getItemFilter(1).getFilteredRows().size());
        Assert.assertEquals(3, getItemFilter(1).getFilteredRows().iterator()
                .next().intValue());
    }

    @Test
    public void filter_filteredRows_rowHidden() {
        getFilterCheckboxGroup(1).deselect("4");

        Assert.assertTrue(spreadsheet.isRowHidden(3));
    }

    @Test
    public void init_clearButtonDisabled() {
        Assert.assertFalse(getClearButton(1).isEnabled());
    }

    @Test
    public void filter_filteredAllRows_clearButtonEnabled() {
        getFilterCheckboxGroup(1).deselectAll();

        Assert.assertTrue(getClearButton(1).isEnabled());
    }

    @Test
    public void init_popupButtonInactive() {
        Assert.assertFalse(getPopupButton(1).isActive());
    }

    @Test
    public void filter_popupButtonActive() {
        getFilterCheckboxGroup(1).deselect("4");

        Assert.assertTrue(getPopupButton(1).isActive());
    }

    @Test
    public void filter_clearAllButtonClick_filtersCleared() {
        getFilterCheckboxGroup(1).deselect("4");
        getClearButton(1).click();

        Assert.assertEquals(0, getItemFilter(1).getFilteredRows().size());
        Assert.assertFalse(spreadsheet.isRowHidden(3));
        Assert.assertFalse(getClearButton(1).isEnabled());
        Assert.assertFalse(getPopupButton(1).isActive());
    }

    @Test
    public void filter_clearAndReload_filtersCleared() {
        getFilterCheckboxGroup(1).deselect("4");
        table.clear();
        table.reload();
        table.onFiltersUpdated();

        Assert.assertEquals(0, getItemFilter(1).getFilteredRows().size());
        Assert.assertFalse(spreadsheet.isRowHidden(3));
        Assert.assertFalse(getClearButton(1).isEnabled());
        Assert.assertFalse(getPopupButton(1).isActive());
    }

    @Test
    public void filter_unregisterFilter_filtersCleared() {
        getFilterCheckboxGroup(1).deselect("4");
        table.unRegisterFilter(getPopupButton(1), getItemFilter(1));
        table.onFiltersUpdated();

        Assert.assertFalse(spreadsheet.isRowHidden(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerFilter_uknownPopupButton_throws() {
        table.registerFilter(new PopupButton(), getItemFilter(1));
    }

    @Test
    public void filteredTable_unhideRowAndOpenPopup_rowIsUnhidden() {
        getFilterCheckboxGroup(1).deselect("4");

        spreadsheet.setRowHidden(3, false);
        getPopupButton(1).openPopup();

        Assert.assertFalse(spreadsheet.isRowHidden(3));
    }

    @Test
    public void deselectAll_allRowsHidden() {
        getSelectAllCheckbox(1).setValue(false);

        Assert.assertEquals(4, getItemFilter(1).getFilteredRows().size());
        Assert.assertTrue(spreadsheet.isRowHidden(2));
        Assert.assertTrue(spreadsheet.isRowHidden(3));
        Assert.assertTrue(spreadsheet.isRowHidden(4));
        Assert.assertTrue(spreadsheet.isRowHidden(5));
    }

    @Test
    public void deselectAll_reopenPopup_staysDeselectedAndHidden() {
        getSelectAllCheckbox(1).setValue(false);

        getPopupButton(1).openPopup();

        Assert.assertFalse(getSelectAllCheckbox(1).getValue());
        Assert.assertTrue(getFilterCheckboxGroup(1).getValue().isEmpty());
        Assert.assertEquals(4, getItemFilter(1).getFilteredRows().size());
        Assert.assertTrue(spreadsheet.isRowHidden(2));
    }

    @Test
    public void deselectAll_reopenPopup_selectAllNotIndeterminate() {
        getSelectAllCheckbox(1).setValue(false);

        getPopupButton(1).openPopup();

        Assert.assertFalse(getSelectAllCheckbox(1).isIndeterminate());
        Assert.assertFalse(getSelectAllCheckbox(1).getValue());
    }

    @Test
    public void allValuesHiddenByOtherFilter_selectAllHidden() {
        // Hide every row through another column's filter
        getFilterCheckboxGroup(2).deselectAll();

        // Refresh this column's options by reopening its popup
        getPopupButton(1).openPopup();

        Assert.assertFalse(getSelectAllCheckbox(1).isVisible());
    }

    @Test
    public void someValuesHiddenByOtherFilter_selectAllVisible() {
        // Hide a single row through another column's filter
        getFilterCheckboxGroup(2).deselect("4");

        getPopupButton(1).openPopup();

        Assert.assertTrue(getSelectAllCheckbox(1).isVisible());
    }

    @Test
    public void deselectLastRemainingValue_lastRowHidden() {
        // Keep only the value of row 5 selected, then deselect it too
        CheckboxGroup<String> group = getFilterCheckboxGroup(1);
        group.deselect("3");
        group.deselect("4");
        group.deselect("5");
        // Only "6" (row 5) remains selected
        group.deselect("6");

        Assert.assertEquals(4, getItemFilter(1).getFilteredRows().size());
        Assert.assertTrue(getFilterCheckboxGroup(1).getValue().isEmpty());
        Assert.assertTrue(spreadsheet.isRowHidden(5));
    }

    private Checkbox getSelectAllCheckbox(int column) {
        return (Checkbox) getItemFilter(column).getChildren()
                .filter(component -> component instanceof Checkbox).findFirst()
                .get();
    }

    private CheckboxGroup<String> getFilterCheckboxGroup(int column) {
        return (CheckboxGroup<String>) getItemFilter(column).getChildren()
                .filter(component -> component instanceof CheckboxGroup)
                .findFirst().get();
    }

    private ItemFilter getItemFilter(int column) {
        return (ItemFilter) getPopupButtonChildren(column).get(0);
    }

    private Button getClearButton(int column) {
        return (Button) getPopupButtonChildren(column).get(1);
    }

    private List<Component> getPopupButtonChildren(int column) {
        return getPopupButton(column).getContent().getChildren()
                .collect(Collectors.toList());
    }

    private PopupButton getPopupButton(int column) {
        return table.getPopupButton(column);
    }
}
