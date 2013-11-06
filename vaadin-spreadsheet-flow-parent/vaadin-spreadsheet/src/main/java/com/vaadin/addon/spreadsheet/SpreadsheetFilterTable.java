package com.vaadin.addon.spreadsheet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HasComponents;

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
 */
public class SpreadsheetFilterTable extends SpreadsheetTable implements
        Button.ClickListener {
    private static final String CLEAR_FILTERS_BUTTON_CLASSNAME = "clear-filters-button";

    protected final Map<PopupButton, HashSet<SpreadsheetFilter>> popupButtonToFiltersMap;
    protected final Map<PopupButton, Button> popupButtonToClearButtonMap;
    protected CellRangeAddress filteringRegion;

    /**
     * Creates a new filter table for the given spreadsheet component, its
     * active sheet ({@link Spreadsheet#getActiveSheet()} and region. Pop-up
     * buttons and content (filters) are created.
     * 
     * @param spreadsheet
     * @param sheet
     * @param fullTableRegion
     * @param spreadsheet
     * @param fullTableRegion
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
     * @param sheet
     * @param fullTableRegion
     */
    public SpreadsheetFilterTable(Spreadsheet spreadsheet, Sheet sheet,
            CellRangeAddress fullTableRegion) {
        super(spreadsheet, sheet, fullTableRegion);

        popupButtonToFiltersMap = new HashMap<PopupButton, HashSet<SpreadsheetFilter>>();
        popupButtonToClearButtonMap = new HashMap<PopupButton, Button>();
        filteringRegion = new CellRangeAddress(
                fullTableRegion.getFirstRow() + 1,
                fullTableRegion.getLastRow(), fullTableRegion.getFirstColumn(),
                fullTableRegion.getLastColumn());

        if (isTableSheetCurrentlyActive()) {
            initFilters();
            initClearAllButtons();
        }
    }

    /**
     * Reloads all the pop-up buttons and the pop-up contents (filters).
     * <p>
     * If the pop-up buttons and filters have been cleared, when
     * {@link #clear()} has been called, they are recreated with "empty"
     * filters. Otherwise the pop-up buttons are just added to the component
     * again.
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
     * If this Table is reloaded, the filters will have cleared their states.
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
            popupButton.addComponent(clearButton);
            popupButtonToClearButtonMap.put(popupButton, clearButton);
        }
    }

    /**
     * Creates all filters for this table. override this in extending class for
     * adding filters on class construction.
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
            ItemFilter itemFilter = new ItemFilter(new CellRangeAddress(
                    firstRow, lastRow, column, column), getSpreadsheet(),
                    popupButton, this);
            popupButton.addComponent(itemFilter);
            registerFilter(popupButton, itemFilter);
        }
    }

    /**
     * Creates a "Clear filters" button. It is has the
     * {@value #CLEAR_FILTERS_BUTTON_CLASSNAME} class name.
     * 
     * @return
     */
    protected Button createClearButton() {
        final Button button = new Button("Clear filters", this);
        button.setDisableOnClick(true);
        button.setEnabled(false);
        button.addStyleName(CLEAR_FILTERS_BUTTON_CLASSNAME);
        return button;
    }

    /**
     * Gets the filtering region for this table (the columns and rows that the
     * filters applies to).
     * 
     * @return
     */
    public CellRangeAddress getFilteringRegion() {
        return filteringRegion;
    }

    /**
     * Called when one of the filters ({@link SpreadsheetFilter}) has been
     * updated, and the sheet ({@link #getSheet()}) and component (
     * {@link #getSpreadsheet()}) need to be updated to reflect to the filters'
     * values.
     */
    public void onFiltersUpdated() {
        Set<Integer> filteredRows = new HashSet<Integer>();
        for (Entry<PopupButton, HashSet<SpreadsheetFilter>> entry : popupButtonToFiltersMap
                .entrySet()) {
            PopupButton popupButton = entry.getKey();
            HashSet<SpreadsheetFilter> filters = entry.getValue();
            HashSet<Integer> temp = new HashSet<Integer>();
            for (SpreadsheetFilter filter : filters) {
                temp.addAll(filter.getFilteredRows());
            }
            popupButtonToClearButtonMap.get(popupButton).setEnabled(
                    !temp.isEmpty());
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
     *            the pop-up button this filter is added to
     * @param filter
     */
    public void registerFilter(PopupButton popupButton, SpreadsheetFilter filter) {
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
     *            the pop-up button this filter is removed from
     * @param filter
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

    @Override
    public void buttonClick(ClickEvent event) {
        HasComponents parent = event.getButton().getParent();
        if (parent instanceof PopupButton
                && popupButtonToFiltersMap.containsKey(parent)) {
            HashSet<SpreadsheetFilter> filters = popupButtonToFiltersMap
                    .get(parent);
            for (SpreadsheetFilter filter : filters) {
                filter.clearFilter();
            }
            ((PopupButton) parent).markActive(false);
            onFiltersUpdated();
        }
    }
}