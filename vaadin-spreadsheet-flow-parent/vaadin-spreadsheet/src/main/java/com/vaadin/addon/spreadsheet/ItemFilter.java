package com.vaadin.addon.spreadsheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.addon.spreadsheet.PopupButton.PopupCloseEvent;
import com.vaadin.addon.spreadsheet.PopupButton.PopupCloseListener;
import com.vaadin.addon.spreadsheet.PopupButton.PopupOpenEvent;
import com.vaadin.addon.spreadsheet.PopupButton.PopupOpenListener;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.OptionGroup;
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
        allItems.setImmediate(true);
        allItems.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
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
            }
        });
    }

    /**
     * Creates the filter selection component.
     */
    protected void initOptions() {
        options = new OptionGroup();
        options.setImmediate(true);
        options.setMultiSelect(true);
        options.addValueChangeListener(this);
    }

    /**
     * Updates the filtering options based on the values within the column.
     */
    public void updateOptions() {
        Set<String> newValues = getAllValues();
        // remove changed, or update value
        for (String old : allCellValues) {
            if (!newValues.contains(old)) {
                options.removeItem(old);
                allCellValues.remove(old);
            }
        }

        // add new
        for (String item : newValues) {
            if (!allCellValues.contains(item)) {
                options.addItem(item);
                allCellValues.add(item);
            }
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
     * @returns All unique values currently visible (= not filtered) within this
     *          column
     */
    protected Set<String> getVisibleValues() {
        Set<String> values = new HashSet<String>();
        for (int r = filterRange.getFirstRow(); r <= filterRange.getLastRow(); r++) {
            if (!filteredRows.contains(r)) {
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
