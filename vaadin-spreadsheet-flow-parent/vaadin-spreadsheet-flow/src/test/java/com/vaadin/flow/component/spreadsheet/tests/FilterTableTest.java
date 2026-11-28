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
import java.util.Set;

import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.spreadsheet.ItemFilter;
import com.vaadin.flow.component.spreadsheet.PopupButton;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetFilterTable;
import com.vaadin.flow.component.textfield.TextField;

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
        Assertions.assertEquals(0, getItemFilter(1).getFilteredRows().size());
    }

    @Test
    void filter_filteredRows_hasFilteredRow() {
        // Filter out row 4
        getFilterCheckboxGroup(1).deselect("4");

        Assertions.assertEquals(1, getItemFilter(1).getFilteredRows().size());
        Assertions.assertEquals(3, getItemFilter(1).getFilteredRows().iterator()
                .next().intValue());
    }

    @Test
    void filter_filteredRows_rowHidden() {
        getFilterCheckboxGroup(1).deselect("4");

        Assertions.assertTrue(spreadsheet.isRowHidden(3));
    }

    @Test
    void filter_searchFiltersOptionsAndClearingRestoresOptions() {
        TextField searchField = getFilterSearchField(5);
        searchField.setValue("0");

        Assertions.assertEquals(List.of("10"), getFilterOptions(5));

        searchField.setValue("no match");
        Assertions.assertTrue(getFilterOptions(5).isEmpty());

        searchField.clear();
        Assertions.assertEquals(List.of("10", "7", "8", "9"),
                getFilterOptions(5));
    }

    @Test
    void filter_searchResetsWhenPopupOpens() {
        getFilterCheckboxGroup(1).deselect("4");
        TextField searchField = getFilterSearchField(1);
        searchField.setValue("5");

        getPopupButton(1).openPopup();

        Assertions.assertEquals("", searchField.getValue());
        Assertions.assertEquals(List.of("3", "4", "5", "6"),
                getFilterOptions(1));
        Assertions.assertEquals(Set.of("3", "5", "6"),
                getFilterCheckboxGroup(1).getValue());
        Assertions.assertTrue(spreadsheet.isRowHidden(3));
    }

    @ParameterizedTest
    @ValueSource(strings = { "app", "APP", "  pP  " })
    void search_caseInsensitiveSubstringWithTrimmedQuery_matchesOptions(
            String query) {
        List<String> values = List.of("Apple", "Pineapple", "Banana", "Cherry");
        for (int i = 0; i < values.size(); i++) {
            spreadsheet.createCell(i + 2, 1, values.get(i));
        }
        getItemFilter(1).updateOptions();

        getFilterSearchField(1).setValue(query);

        Assertions.assertEquals(List.of("Apple", "Pineapple"),
                getFilterOptions(1));
        Assertions.assertEquals(Set.copyOf(values),
                getFilterCheckboxGroup(1).getValue());
        Assertions.assertFalse(getPopupButton(1).isActive());
    }

    @Test
    void search_whitespaceQuery_restoresAllOptions() {
        TextField searchField = getFilterSearchField(1);
        searchField.setValue("5");

        searchField.setValue("   ");

        Assertions.assertEquals(List.of("3", "4", "5", "6"),
                getFilterOptions(1));
        Assertions.assertEquals(Set.of("3", "4", "5", "6"),
                getFilterCheckboxGroup(1).getValue());
    }

    @Test
    void search_selectMatchesAcrossQueries_addsToExistingSelection() {
        CheckboxGroup<String> group = getFilterCheckboxGroup(1);
        group.setValue(Set.of("3"));
        TextField searchField = getFilterSearchField(1);

        searchField.setValue("4");
        group.select("4");
        searchField.setValue("6");
        group.select("6");
        searchField.clear();

        Assertions.assertEquals(Set.of("3", "4", "6"), group.getValue());
        Assertions.assertEquals(Set.of(4), getItemFilter(1).getFilteredRows());
        Assertions.assertFalse(spreadsheet.isRowHidden(2));
        Assertions.assertFalse(spreadsheet.isRowHidden(3));
        Assertions.assertTrue(spreadsheet.isRowHidden(4));
        Assertions.assertFalse(spreadsheet.isRowHidden(5));
    }

    @Test
    void search_selectAllControlsAllOptionsNotOnlyMatches() {
        TextField searchField = getFilterSearchField(1);
        searchField.setValue("5");

        getSelectAllCheckbox(1).setValue(false);

        Assertions.assertTrue(getFilterCheckboxGroup(1).getValue().isEmpty());
        Assertions.assertEquals(Set.of(2, 3, 4, 5),
                getItemFilter(1).getFilteredRows());
        Assertions.assertEquals(List.of("5"), getFilterOptions(1));

        getSelectAllCheckbox(1).setValue(true);
        searchField.setValue("no match");

        Assertions.assertTrue(getFilterOptions(1).isEmpty());
        Assertions.assertEquals(Set.of("3", "4", "5", "6"),
                getFilterCheckboxGroup(1).getValue());
        Assertions.assertTrue(getSelectAllCheckbox(1).getValue());
        Assertions.assertFalse(getSelectAllCheckbox(1).isIndeterminate());
        Assertions.assertTrue(getItemFilter(1).getFilteredRows().isEmpty());
        Assertions.assertFalse(getPopupButton(1).isActive());
    }

    @Test
    void search_updateOptions_appliesQueryToChangedCellValues() {
        TextField searchField = getFilterSearchField(1);
        searchField.setValue("5");
        spreadsheet.createCell(2, 1, "15");
        spreadsheet.createCell(4, 1, "8");

        getItemFilter(1).updateOptions();

        Assertions.assertEquals("5", searchField.getValue());
        Assertions.assertEquals(List.of("15"), getFilterOptions(1));
        Assertions.assertEquals(Set.of("15", "4", "6", "8"),
                getFilterCheckboxGroup(1).getValue());
        Assertions.assertTrue(getItemFilter(1).getFilteredRows().isEmpty());
    }

    @Test
    void search_otherColumnHidesMatchingRow_doesNotOfferItsValue() {
        getFilterCheckboxGroup(2).deselect("4");
        getPopupButton(1).openPopup();

        getFilterSearchField(1).setValue("3");

        Assertions.assertTrue(getFilterOptions(1).isEmpty());
        Assertions.assertEquals(Set.of("4", "5", "6"),
                getFilterCheckboxGroup(1).getValue());
        Assertions.assertTrue(spreadsheet.isRowHidden(2));
        Assertions.assertTrue(getItemFilter(1).getFilteredRows().isEmpty());

        getFilterSearchField(1).clear();
        Assertions.assertEquals(List.of("4", "5", "6"), getFilterOptions(1));
    }

    @Test
    void filter_searchPreservesSelectionsAndFilteredRows() {
        ItemFilter itemFilter = getItemFilter(1);
        CheckboxGroup<String> group = getFilterCheckboxGroup(1);
        group.deselect("4");

        TextField searchField = getFilterSearchField(1);
        searchField.setValue("5");

        Assertions.assertEquals(List.of("5"), getFilterOptions(1));
        Assertions.assertEquals(Set.of("3", "5", "6"), group.getValue());
        Assertions.assertEquals(Set.of(3), itemFilter.getFilteredRows());

        group.deselect("5");

        Assertions.assertEquals(Set.of("3", "6"), group.getValue());
        Assertions.assertEquals(Set.of(3, 4), itemFilter.getFilteredRows());

        searchField.clear();
        Assertions.assertEquals(List.of("3", "4", "5", "6"),
                getFilterOptions(1));
    }

    @Test
    void init_clearButtonDisabled() {
        Assertions.assertFalse(getClearButton(1).isEnabled());
    }

    @Test
    void filter_filteredAllRows_clearButtonEnabled() {
        getFilterCheckboxGroup(1).deselectAll();

        Assertions.assertTrue(getClearButton(1).isEnabled());
    }

    @Test
    void init_popupButtonInactive() {
        Assertions.assertFalse(getPopupButton(1).isActive());
    }

    @Test
    void filter_popupButtonActive() {
        getFilterCheckboxGroup(1).deselect("4");

        Assertions.assertTrue(getPopupButton(1).isActive());
    }

    @Test
    void filter_clearAllButtonClick_filtersCleared() {
        getFilterCheckboxGroup(1).deselect("4");
        getClearButton(1).click();

        Assertions.assertEquals(0, getItemFilter(1).getFilteredRows().size());
        Assertions.assertFalse(spreadsheet.isRowHidden(3));
        Assertions.assertFalse(getClearButton(1).isEnabled());
        Assertions.assertFalse(getPopupButton(1).isActive());
    }

    @Test
    void filter_clearAndReload_filtersCleared() {
        getFilterCheckboxGroup(1).deselect("4");
        table.clear();
        table.reload();
        table.onFiltersUpdated();

        Assertions.assertEquals(0, getItemFilter(1).getFilteredRows().size());
        Assertions.assertFalse(spreadsheet.isRowHidden(3));
        Assertions.assertFalse(getClearButton(1).isEnabled());
        Assertions.assertFalse(getPopupButton(1).isActive());
    }

    @Test
    void filter_unregisterFilter_filtersCleared() {
        getFilterCheckboxGroup(1).deselect("4");
        table.unRegisterFilter(getPopupButton(1), getItemFilter(1));
        table.onFiltersUpdated();

        Assertions.assertFalse(spreadsheet.isRowHidden(3));
    }

    @Test
    void registerFilter_uknownPopupButton_throws() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> table
                .registerFilter(new PopupButton(), getItemFilter(1)));
    }

    @Test
    void filteredTable_unhideRowAndOpenPopup_rowIsUnhidden() {
        getFilterCheckboxGroup(1).deselect("4");

        spreadsheet.setRowHidden(3, false);
        getPopupButton(1).openPopup();

        Assertions.assertFalse(spreadsheet.isRowHidden(3));
    }

    @Test
    void deselectAll_allRowsHidden() {
        getSelectAllCheckbox(1).setValue(false);

        Assertions.assertEquals(4, getItemFilter(1).getFilteredRows().size());
        Assertions.assertTrue(spreadsheet.isRowHidden(2));
        Assertions.assertTrue(spreadsheet.isRowHidden(3));
        Assertions.assertTrue(spreadsheet.isRowHidden(4));
        Assertions.assertTrue(spreadsheet.isRowHidden(5));
    }

    @Test
    void deselectAll_reopenPopup_staysDeselectedAndHidden() {
        getSelectAllCheckbox(1).setValue(false);

        getPopupButton(1).openPopup();

        Assertions.assertFalse(getSelectAllCheckbox(1).getValue());
        Assertions.assertTrue(getFilterCheckboxGroup(1).getValue().isEmpty());
        Assertions.assertEquals(4, getItemFilter(1).getFilteredRows().size());
        Assertions.assertTrue(spreadsheet.isRowHidden(2));
    }

    @Test
    void deselectAll_reopenPopup_selectAllNotIndeterminate() {
        getSelectAllCheckbox(1).setValue(false);

        getPopupButton(1).openPopup();

        Assertions.assertFalse(getSelectAllCheckbox(1).isIndeterminate());
        Assertions.assertFalse(getSelectAllCheckbox(1).getValue());
    }

    @Test
    void allValuesHiddenByOtherFilter_selectAllHidden() {
        // Hide every row through another column's filter
        getFilterCheckboxGroup(2).deselectAll();

        // Refresh this column's options by reopening its popup
        getPopupButton(1).openPopup();

        Assertions.assertFalse(getSelectAllCheckbox(1).isVisible());
    }

    @Test
    void someValuesHiddenByOtherFilter_selectAllVisible() {
        // Hide a single row through another column's filter
        getFilterCheckboxGroup(2).deselect("4");

        getPopupButton(1).openPopup();

        Assertions.assertTrue(getSelectAllCheckbox(1).isVisible());
    }

    @Test
    void deselectLastRemainingValue_lastRowHidden() {
        // Keep only the value of row 5 selected, then deselect it too
        CheckboxGroup<String> group = getFilterCheckboxGroup(1);
        group.deselect("3");
        group.deselect("4");
        group.deselect("5");
        // Only "6" (row 5) remains selected
        group.deselect("6");

        Assertions.assertEquals(4, getItemFilter(1).getFilteredRows().size());
        Assertions.assertTrue(getFilterCheckboxGroup(1).getValue().isEmpty());
        Assertions.assertTrue(spreadsheet.isRowHidden(5));
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

    private TextField getFilterSearchField(int column) {
        return (TextField) getItemFilter(column).getChildren()
                .filter(component -> component instanceof TextField).findFirst()
                .get();
    }

    private List<String> getFilterOptions(int column) {
        return getFilterCheckboxGroup(column).getChildren()
                .filter(component -> component instanceof Checkbox)
                .map(Checkbox.class::cast).map(Checkbox::getLabel).toList();
    }

    private ItemFilter getItemFilter(int column) {
        return (ItemFilter) getPopupButtonChildren(column).get(0);
    }

    private Button getClearButton(int column) {
        return (Button) getPopupButtonChildren(column).get(1);
    }

    private List<Component> getPopupButtonChildren(int column) {
        return getPopupButton(column).getContent().getChildren().toList();
    }

    private PopupButton getPopupButton(int column) {
        return table.getPopupButton(column);
    }
}
