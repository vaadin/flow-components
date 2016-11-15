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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.addon.spreadsheet.PopupButton.PopupCloseEvent;
import com.vaadin.addon.spreadsheet.PopupButton.PopupCloseListener;
import com.vaadin.addon.spreadsheet.PopupButton.PopupOpenEvent;
import com.vaadin.addon.spreadsheet.PopupButton.PopupOpenListener;
import com.vaadin.v7.data.Container.Sortable;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.data.util.ItemSorter;
import com.vaadin.ui.CheckBox;
import com.vaadin.v7.ui.OptionGroup;
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
public class ItemFilter extends Panel implements ValueChangeListener,
        SpreadsheetFilter {

    private static final String ITEM_FILTER_LAYOUT_CLASSNAME = "spreadsheet-item-filter-layout";
    private Spreadsheet spreadsheet;
    private CellRangeAddress filterRange;
    private CheckBox allItems;
    private OptionGroup options;
    private IndexedContainer optionsContainer;
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

        allCellValues = new ArrayList<String>();
        filteredRows = new HashSet<Integer>();
        latestFilteredValues = new LinkedHashSet<String>();
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
        layout.addComponent(allItems);
        layout.addComponent(options);

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
                    Collection<String> currentValue = (Collection<String>) options
                            .getValue();
                    cancelValueChangeUpdate = true;
                    if (currentValue.isEmpty()) {
                        if (latestFilteredValues.isEmpty()
                                || latestFilteredValues
                                        .containsAll(allCellValues)) {
                            allItems.setValue(true);
                            options.setValue(allCellValues);
                        } else {
                            options.setValue(latestFilteredValues);
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
                        options.setValue(allCellValues);
                        updateFilteredItems(allCellValues);
                    } else {
                        options.setValue(Collections.emptySet());
                    }
                    cancelValueChangeUpdate = false;
                }
        });
    }

    /**
     * Creates the filter selection component.
     */
    protected void initOptions() {
        optionsContainer = new IndexedContainer();
        optionsContainer.setItemSorter(new ItemSorter() {

            @Override
            public void setSortProperties(Sortable container,
                    Object[] propertyId, boolean[] ascending) {
            }

            @Override
            public int compare(Object itemId1, Object itemId2) {
                return ((String) itemId1).compareToIgnoreCase((String) itemId2);
            }
        });
        options = new OptionGroup();
        options.setContainerDataSource(optionsContainer);
        options.setMultiSelect(true);
        options.addValueChangeListener(this);
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
                optionsContainer.removeItem(old);
                iter.remove();
            }
        }

        // add new
        for (String item : newValues) {
            if (!allCellValues.contains(item)) {
                optionsContainer.addItem(item);
                allCellValues.add(item);
                needsSort = true;
            }
        }

        if (needsSort) {
            optionsContainer.sort(new Object[0], new boolean[0]);
        }

        Set<String> visibleValues = getVisibleValues();
        cancelValueChangeUpdate = true;
        options.setValue(visibleValues);
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
        Set<String> values = new HashSet<String>();
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
        Set<String> values = new HashSet<String>();
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
        latestFilteredValues = visibleValues;

        filterTable.onFiltersUpdated();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void valueChange(ValueChangeEvent event) {
        if (firstUpdate) {
            firstUpdate = false;
        } else {
            if (!cancelValueChangeUpdate) {
                Collection<String> value = (Collection<String>) options
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
    }

    @Override
    public Set<Integer> getFilteredRows() {
        return filteredRows;
    }

    @Override
    public void clearFilter() {
        cancelValueChangeUpdate = true;
        allItems.setValue(true);
        options.setValue(allCellValues);
        filteredRows.clear();
        cancelValueChangeUpdate = false;
    }
}
