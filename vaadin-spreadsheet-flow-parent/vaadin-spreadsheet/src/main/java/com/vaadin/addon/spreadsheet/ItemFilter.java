package com.vaadin.addon.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.addon.spreadsheet.PopupButton.PopupCloseEvent;
import com.vaadin.addon.spreadsheet.PopupButton.PopupCloseListener;
import com.vaadin.addon.spreadsheet.PopupButton.PopupOpenEvent;
import com.vaadin.addon.spreadsheet.PopupButton.PopupOpenListener;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CheckBoxGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * A simple filter for spreadsheet filtering table, filtering cell values with
 * checkboxes.
 * <p>
 * Has a check box for selecting all items (cell values), and one check box per
 * unique cell value that can be found within the cells of the table column.
 */
@SuppressWarnings("serial")
public class ItemFilter extends Panel implements SpreadsheetFilter {

    private static final String ITEM_FILTER_LAYOUT_CLASSNAME = "spreadsheet-item-filter-layout";
    private Spreadsheet spreadsheet;
    private CellRangeAddress filterRange;
    private CheckBox allItems;
    private CheckBoxGroup<String> filterCheckbox;
    private DataProvider<String, ?> filterOptionsProvider;
    private List<String> filterOptions = new ArrayList<>();
    private ArrayList<String> allCellValues;
    private Collection<String> latestFilteredValues;
    private PopupButton popupButton;
    private VerticalLayout layout;
    private boolean firstUpdate = true;
    private boolean cancelValueChangeUpdate;
    private SpreadsheetFilterTable filterTable;
    private Set<Integer> filteredRows;

    /**
     * Constructs a new item filter for the given spreadsheet, filtering range,
     * pop-up button and filtering table.
     * 
     * @param filterRange
     *            Range of cells to filter
     * @param spreadsheet
     *            Target Spreadsheet
     * @param popupButton
     *            Pop-up button to insert the filter components in
     * @param filterTable
     *            Target SpreadsheetFilterTable
     */
    public ItemFilter(CellRangeAddress filterRange, Spreadsheet spreadsheet,
            PopupButton popupButton, SpreadsheetFilterTable filterTable) {
        this.filterRange = filterRange;
        this.spreadsheet = spreadsheet;
        this.popupButton = popupButton;
        this.filterTable = filterTable;

        allCellValues = new ArrayList<>();
        filteredRows = new HashSet<>();
        latestFilteredValues = new LinkedHashSet<>();
        initComponents();
        updateOptions();
    }

    /**
     * Create all components of the ItemFilter.
     */
    protected void initComponents() {
        initAllItemsCheckbox();
        initOptions();
        initLayouts();
        initPopupButtonListeners();
    }

    /**
     * Creates the base layout for the filter components.
     */
    protected void initLayouts() {
        layout = new VerticalLayout();
        layout.setMargin(false);
        layout.addComponent(allItems);
        layout.addComponent(filterCheckbox);

        setContent(layout);
        setStyleName(ITEM_FILTER_LAYOUT_CLASSNAME);
    }

    /**
     * Initializes pop-up close listener for verifying that filter selections
     * match with what is currently shown.
     */
    protected void initPopupButtonListeners() {

        popupButton.addPopupCloseListener(new PopupCloseListener() {

            @Override
            public void onPopupClose(PopupCloseEvent event) {
                // need to check that the filter wasn't left a different state
                // than what is displayed, like in case when allItems and all
                // options were left unchecked.
                if (!allItems.getValue()) {
                    @SuppressWarnings("unchecked")
                    Collection<String> currentValue = filterCheckbox
                            .getValue();
                    cancelValueChangeUpdate = true;
                    if (currentValue.isEmpty()) {
                        if (latestFilteredValues.isEmpty()
                                || latestFilteredValues
                                        .containsAll(allCellValues)) {
                            allItems.setValue(true);
                            filterCheckbox.setValue(new HashSet<>(allCellValues));
                        } else {
                            filterCheckbox.setValue(new HashSet<>(latestFilteredValues));
                        }
                    } else {
                        if (currentValue.containsAll(allCellValues)) {
                            allItems.setValue(true);
                        }
                    }
                    cancelValueChangeUpdate = false;
                }
            }
        });
        popupButton.addPopupOpenListener(new PopupOpenListener() {

            @Override
            public void onPopupOpen(PopupOpenEvent event) {
                updateOptions();
            }
        });
    }

    /**
     * Creates the "Select All" filter component.
     */
    protected void initAllItemsCheckbox() {
        allItems = new CheckBox("(Select All)", true);
        allItems.addValueChangeListener(event -> {
                if (!cancelValueChangeUpdate) {
                    Boolean value = allItems.getValue();
                    cancelValueChangeUpdate = true;
                    if (value) {
                        filterCheckbox.setValue(new HashSet<>(allCellValues));
                        updateFilteredItems(allCellValues);
                    } else {
                        filterCheckbox.setValue(Collections.emptySet());
                    }
                    cancelValueChangeUpdate = false;
                }
        });
    }

    /**
     * Creates the filter selection component.
     */
    protected void initOptions() {
        filterOptionsProvider = new ListDataProvider<>(filterOptions);
        filterCheckbox = new CheckBoxGroup<>();
        filterCheckbox.setDataProvider(filterOptionsProvider);
        filterCheckbox.addValueChangeListener(event->{
            if (firstUpdate) {
                firstUpdate = false;
            } else {
                if (!cancelValueChangeUpdate) {
                    Collection<String> value = filterCheckbox
                            .getValue();
                    // value should not be updated when options are empty and all
                    // items is unchecked - just as in Excel
                    if (!value.isEmpty()) {
                        updateFilteredItems(value);
                        cancelValueChangeUpdate = true;
                        if (value.containsAll(allCellValues)) {
                            if (!allItems.getValue()) {
                                allItems.setValue(true);
                            }
                        } else {
                            if (allItems.getValue()) {
                                allItems.setValue(false);
                            }
                        }
                        cancelValueChangeUpdate = false;
                    }
                }
            }
        });
    }

    /**
     * Updates the filtering options based on the values within the column.
     */
    public void updateOptions() {
        Set<String> newValues = getAllValues();
        boolean needsSort = false;

        // remove changed, or update value
        Iterator<String> iter = allCellValues.iterator();
        while (iter.hasNext()) {
            String old = iter.next();
            if (!newValues.contains(old)) {
                filterOptions.remove(old);
                iter.remove();
            }
        }

        // add new
        for (String item : newValues) {
            if (!allCellValues.contains(item)) {
                filterOptions.add(item);
                allCellValues.add(item);
                needsSort = true;
            }
        }

        if (needsSort) {
            Comparator<String> byString = (String s1,String s2) -> s1.compareToIgnoreCase(s2);
            Collections.sort(filterOptions, byString);
        }

        Set<String> visibleValues = getVisibleValues();
        cancelValueChangeUpdate = true;
        filterCheckbox.setValue(visibleValues);
        allItems.setValue(visibleValues.containsAll(allCellValues));
        cancelValueChangeUpdate = false;
    }

    /**
     * Gets the currently NOT filtered cell values.
     * 
     * @return All unique values currently visible (= not filtered) within this
     *         column
     */
    protected Set<String> getVisibleValues() {
        Set<String> values = new HashSet<>();
        for (int r = filterRange.getFirstRow(); r <= filterRange.getLastRow(); r++) {
            if (!filteredRows.contains(r) && !spreadsheet.isRowHidden(r)) {
                values.add(spreadsheet.getCellValue(spreadsheet.getCell(r,
                        filterRange.getFirstColumn())));
            }
        }
        return values;
    }

    /**
     * Gets all of the unique values for this filter column.
     * 
     * @return All unique values within this column
     */
    protected Set<String> getAllValues() {
        Set<String> values = new HashSet<>();
        for (int r = filterRange.getFirstRow(); r <= filterRange.getLastRow(); r++) {
            values.add(spreadsheet.getCellValue(spreadsheet.getCell(r,
                    filterRange.getFirstColumn())));
        }
        return values;
    }

    /**
     * Updates the filtered rows to reflect the new filtered values.
     * 
     * @param visibleValues
     *            the values that are NOT filtered
     */
    protected void updateFilteredItems(Collection<String> visibleValues) {
        filteredRows.clear();
        for (int r = filterRange.getFirstRow(); r <= filterRange.getLastRow(); r++) {
            String cellValue = spreadsheet.getCellValue(spreadsheet.getCell(r,
                    filterRange.getFirstColumn()));
            if (!visibleValues.contains(cellValue)) {
                filteredRows.add(new Integer(r));
            }
        }
        latestFilteredValues = new ArrayList<>(visibleValues);

        filterTable.onFiltersUpdated();
    }

    @Override
    public Set<Integer> getFilteredRows() {
        return filteredRows;
    }

    @Override
    public void clearFilter() {
        cancelValueChangeUpdate = true;
        allItems.setValue(true);
        filterCheckbox.setValue(new HashSet<>(allCellValues));
        filterOptionsProvider.refreshAll();
        filteredRows.clear();
        cancelValueChangeUpdate = false;
    }
}
