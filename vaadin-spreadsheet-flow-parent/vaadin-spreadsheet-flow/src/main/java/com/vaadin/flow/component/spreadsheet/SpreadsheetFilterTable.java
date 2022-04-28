package com.vaadin.flow.component.spreadsheet;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

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
public class SpreadsheetFilterTable extends SpreadsheetTable
        implements ComponentEventListener {
    public static final String CLEAR_FILTERS_BUTTON_CLASSNAME = "clear-filters-button";

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
        super(spreadsheet, sheet, fullTableRegion);

        popupButtonToFiltersMap = new HashMap<PopupButton, HashSet<SpreadsheetFilter>>();
        popupButtonToClearButtonMap = new HashMap<PopupButton, Button>();
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
            VerticalLayout content = new VerticalLayout();
            content.setMargin(false);
            popupButton.setContent(content);
        }

        ((VerticalLayout) popupButton.getContent()).add(component);
    }

    /**
     * Creates a "Clear filters" button. It has the
     * {@value #CLEAR_FILTERS_BUTTON_CLASSNAME} class name.
     *
     * @return Button for clearing the filters
     */
    protected Button createClearButton() {
        final Button button = new Button("Clear filters", this);
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
        Set<Integer> filteredRows = new HashSet<Integer>();
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

    @Override
    public void onComponentEvent(ComponentEvent event) {
        if (event instanceof ClickEvent)
            event.getSource().getParent().ifPresent(parent -> {
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
            });
    }
}
