/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;

/**
 * Represents a "table" inside a spreadsheet, that has filters (
 * {@link SpreadsheetFilter}) inside the table's headers' pop-up buttons.
 * <p>
 * Does simple filtering by hiding the filtered rows from the table (as in
 * Excel).
 * <p>
 * Automatically generates "item filters" ({@link ItemFilter}) for each column.
 * <p>
 * Has a "Clear Filters" button inside the {@link PopupButton}s pop-up, that
 * clears all the filters for that column (contained within the same pop-up).
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class SpreadsheetFilterTable extends SpreadsheetTable {
    public static final String CLEAR_FILTERS_BUTTON_CLASSNAME = "clear-filters-button";

    public static final String FILTER_TABLE_CONTENT_CLASSNAME = "spreadsheet-filter-table-content";

    protected final Map<PopupButton, HashSet<SpreadsheetFilter>> popupButtonToFiltersMap;
    protected final Map<PopupButton, Button> popupButtonToClearButtonMap;
    protected CellRangeAddress filteringRegion;

    /**
     * Creates a new filter table for the given spreadsheet component, its
     * active sheet ({@link Spreadsheet#getActiveSheet()} and region. Pop-up
     * buttons and content (filters) are created.
     *
     * @param spreadsheet
     *            Target Spreadsheet
     * @param fullTableRegion
     *            Cell range to include in the table
     */
    public SpreadsheetFilterTable(Spreadsheet spreadsheet,
            CellRangeAddress fullTableRegion) {
        this(spreadsheet, spreadsheet.getActiveSheet(), fullTableRegion);
    }

    /**
     * Creates a new filter table for the given spreadsheet component, sheet and
     * region. If the component is currently displaying the sheet that the table
     * belongs to, pop-up buttons and content (filters) are created.
     *
     * @param spreadsheet
     *            Target Spreadsheet
     * @param sheet
     *            Target sheet within the Spreadsheet
     * @param fullTableRegion
     *            Cell range to include in the table
     */
    public SpreadsheetFilterTable(Spreadsheet spreadsheet, Sheet sheet,
            CellRangeAddress fullTableRegion) {
        this(spreadsheet, spreadsheet.getActiveSheet(), fullTableRegion, null,
                null);
    }

    public SpreadsheetFilterTable(Spreadsheet spreadsheet, Sheet sheet,
            CellRangeAddress fullTableRegion,
            CTAutoFilter ctWorksheetAutoFilter, XSSFTable xssfTable) {
        super(spreadsheet, sheet, fullTableRegion, ctWorksheetAutoFilter,
                xssfTable);

        popupButtonToFiltersMap = new HashMap<>();
        popupButtonToClearButtonMap = new HashMap<>();
        filteringRegion = new CellRangeAddress(
                fullTableRegion.getFirstRow() + 1, fullTableRegion.getLastRow(),
                fullTableRegion.getFirstColumn(),
                fullTableRegion.getLastColumn());

        if (isTableSheetCurrentlyActive()) {
            initFilters();
            initClearAllButtons();
        }
    }

    /**
     * Reloads all the pop-up buttons and the pop-up contents (filters).
     * <p>
     * If the pop-up buttons and filters have been cleared ({@link #clear()} has
     * been called) they will be recreated with "empty" filters. Otherwise the
     * existing pop-up buttons are just added to the component again.
     */
    @Override
    public void reload() {
        super.reload();
        if (isTableSheetCurrentlyActive()) {
            if (popupButtonToFiltersMap.isEmpty()) {
                initFilters();
            }
            if (popupButtonToClearButtonMap.isEmpty()) {
                initClearAllButtons();
            }
        }
    }

    /**
     * Clears all the pop-up buttons and their contents (filters).
     * <p>
     * If this Table is reloaded after this method has been called, the filters
     * will have cleared their states.
     */
    @Override
    public void clear() {
        super.clear();
        popupButtonToClearButtonMap.clear();
        popupButtonToFiltersMap.clear();
    }

    /**
     * Clears all filters for this table. Does not remove the actual filter
     * components.
     */
    public void clearAllFilters() {
        for (Entry<PopupButton, HashSet<SpreadsheetFilter>> entry : popupButtonToFiltersMap
                .entrySet()) {
            PopupButton popupButton = entry.getKey();
            HashSet<SpreadsheetFilter> filters = entry.getValue();
            for (SpreadsheetFilter filter : filters) {
                filter.clearFilter();
            }
            popupButtonToClearButtonMap.get(popupButton).setEnabled(false);
            popupButton.markActive(false);
        }
        Spreadsheet spreadsheet = getSpreadsheet();
        for (int r = filteringRegion.getFirstRow(); r <= filteringRegion
                .getLastRow(); r++) {
            spreadsheet.setRowHidden(r, false);
        }
    }

    /**
     * Creates the "Clear filters" buttons for the pop-ups.
     */
    protected void initClearAllButtons() {
        for (PopupButton popupButton : getPopupButtons()) {
            Button clearButton = createClearButton();
            addComponentToPopup(popupButton, clearButton);
            popupButtonToClearButtonMap.put(popupButton, clearButton);
        }
    }

    /**
     * Creates all filters for this table. Override this in an extending class
     * for adding filters on class construction.
     */
    protected void initFilters() {
        initItemFilters();
    }

    /**
     * Creates item filters for this table.
     */
    protected void initItemFilters() {
        int firstRow = filteringRegion.getFirstRow();
        int lastRow = filteringRegion.getLastRow();
        for (PopupButton popupButton : getPopupButtons()) {
            int column = popupButton.getColumn();
            ItemFilter itemFilter = new ItemFilter(
                    new CellRangeAddress(firstRow, lastRow, column, column),
                    getSpreadsheet(), popupButton, this);
            addComponentToPopup(popupButton, itemFilter);
            registerFilter(popupButton, itemFilter);
        }
    }

    private void addComponentToPopup(PopupButton popupButton,
            Component component) {
        if (popupButton.getContent() == null) {
            Div content = new Div();
            content.addClassName(FILTER_TABLE_CONTENT_CLASSNAME);
            popupButton.setContent(content);
        }

        ((Div) popupButton.getContent()).add(component);
    }

    /**
     * Creates a "Clear filters" button. It has the
     * {@value #CLEAR_FILTERS_BUTTON_CLASSNAME} class name.
     *
     * @return Button for clearing the filters
     */
    protected Button createClearButton() {
        final Button button = new Button("Clear filters");
        button.setDisableOnClick(true);
        button.setEnabled(false);
        button.addClassName(CLEAR_FILTERS_BUTTON_CLASSNAME);
        button.addClickListener(event -> clearAllFilters());
        return button;
    }

    /**
     * Gets the filtering region, {@link CellRangeAddress} for this table.
     *
     * @return The filtering region
     */
    public CellRangeAddress getFilteringRegion() {
        return filteringRegion;
    }

    /**
     * Called when one of the filters ({@link SpreadsheetFilter}) has been
     * updated, and the sheet ({@link #getSheet()}) and component (
     * {@link #getSpreadsheet()}) need to be updated to reflect to the filters'
     * values.
     * <p>
     * NOTE: The default ItemFilters will call this method automatically on
     * change. You only need to call this method when you have implemented and
     * added your own SpreadsheetFilter.
     */
    public void onFiltersUpdated() {
        Set<Integer> filteredRows = new HashSet<>();
        for (Entry<PopupButton, HashSet<SpreadsheetFilter>> entry : popupButtonToFiltersMap
                .entrySet()) {
            PopupButton popupButton = entry.getKey();
            HashSet<SpreadsheetFilter> filters = entry.getValue();
            HashSet<Integer> temp = new HashSet<Integer>();
            for (SpreadsheetFilter filter : filters) {
                temp.addAll(filter.getFilteredRows());
            }
            popupButtonToClearButtonMap.get(popupButton)
                    .setEnabled(!temp.isEmpty());
            popupButton.markActive(!temp.isEmpty());
            filteredRows.addAll(temp);
        }
        Spreadsheet spreadsheet = getSpreadsheet();
        for (int r = filteringRegion.getFirstRow(); r <= filteringRegion
                .getLastRow(); r++) {
            spreadsheet.setRowHidden(r, filteredRows.contains(r));
        }
    }

    /**
     * Registers a new filter to this filter table and adds it inside the given
     * pop-up button.
     * <p>
     * NOTE: Does not apply updates, if the registered filter is filtering some
     * rows, {@link #onFiltersUpdated()} should be called.
     *
     * @param popupButton
     *            The pop-up button this filter is added to
     * @param filter
     *            The filter to apply
     */
    public void registerFilter(PopupButton popupButton,
            SpreadsheetFilter filter) {
        if (getPopupButtons().contains(popupButton)) {
            if (popupButtonToFiltersMap.containsKey(popupButton)) {
                popupButtonToFiltersMap.get(popupButton).add(filter);
            } else {
                HashSet<SpreadsheetFilter> filters = new HashSet<SpreadsheetFilter>();
                filters.add(filter);
                popupButtonToFiltersMap.put(popupButton, filters);
            }
        } else {
            throw new IllegalArgumentException(
                    "PopupButton is not inside this filterable Table");
        }
    }

    /**
     * Unregisters the filter from this filter table and removes it from the
     * given pop-up button.
     *
     * @param popupButton
     *            The pop-up button this filter is removed from
     * @param filter
     *            The filter to remove
     */
    public void unRegisterFilter(PopupButton popupButton,
            SpreadsheetFilter filter) {
        HashSet<SpreadsheetFilter> filters = popupButtonToFiltersMap
                .get(popupButton);
        filters.remove(filter);
        if (filters.isEmpty()) {
            popupButtonToFiltersMap.remove(popupButton);
        }
    }
}
