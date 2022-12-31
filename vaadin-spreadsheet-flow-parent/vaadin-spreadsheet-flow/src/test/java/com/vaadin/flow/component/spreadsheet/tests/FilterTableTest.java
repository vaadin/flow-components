package com.vaadin.flow.component.spreadsheet.tests;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
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
        Assert.assertEquals(0, getItemFilter().getFilteredRows().size());
    }

    @Test
    public void filter_filteredRows_hasFilteredRow() {
        // Filter out row 4
        getFilterCheckboxGroup().deselect("4");

        Assert.assertEquals(1, getItemFilter().getFilteredRows().size());
        Assert.assertEquals(3,
                getItemFilter().getFilteredRows().iterator().next().intValue());
    }

    @Test
    public void filter_filteredRows_rowHidden() {
        getFilterCheckboxGroup().deselect("4");

        Assert.assertTrue(spreadsheet.isRowHidden(3));
    }

    @Test
    public void init_clearButtonDisabled() {
        Assert.assertFalse(getClearButton().isEnabled());
    }

    @Test
    public void filter_filteredAllRows_clearButtonEnabled() {
        getFilterCheckboxGroup().deselectAll();

        Assert.assertTrue(getClearButton().isEnabled());
    }

    @Test
    public void init_popupButtonInactive() {
        Assert.assertFalse(getPopupButton().isActive());
    }

    @Test
    public void filter_popupButtonActive() {
        getFilterCheckboxGroup().deselect("4");

        Assert.assertTrue(getPopupButton().isActive());
    }

    @Test
    public void filter_clearAllButtonClick_filtersCleared() {
        getFilterCheckboxGroup().deselect("4");
        getClearButton().click();

        Assert.assertEquals(0, getItemFilter().getFilteredRows().size());
        Assert.assertFalse(spreadsheet.isRowHidden(3));
        Assert.assertFalse(getClearButton().isEnabled());
        Assert.assertFalse(getPopupButton().isActive());
    }

    @Test
    public void filter_clearAndReload_filtersCleared() {
        getFilterCheckboxGroup().deselect("4");
        table.clear();
        table.reload();
        table.onFiltersUpdated();

        Assert.assertEquals(0, getItemFilter().getFilteredRows().size());
        Assert.assertFalse(spreadsheet.isRowHidden(3));
        Assert.assertFalse(getClearButton().isEnabled());
        Assert.assertFalse(getPopupButton().isActive());
    }

    @Test
    public void filter_unregisterFilter_filtersCleared() {
        getFilterCheckboxGroup().deselect("4");
        table.unRegisterFilter(getPopupButton(), getItemFilter());
        table.onFiltersUpdated();

        Assert.assertFalse(spreadsheet.isRowHidden(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerFilter_uknownPopupButton_throws() {
        table.registerFilter(new PopupButton(), getItemFilter());
    }

    @Test
    public void filteredTable_unhideRowAndOpenPopup_rowIsUnhidden() {
        getFilterCheckboxGroup().deselect("4");

        spreadsheet.setRowHidden(3, false);
        getPopupButton().openPopup();

        Assert.assertFalse(spreadsheet.isRowHidden(3));
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
