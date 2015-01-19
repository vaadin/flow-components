package com.vaadin.addon.spreadsheet;

import java.util.Set;

/**
 * Interface for filter components that can be added to a
 * {@link SpreadsheetFilterTable}. Filtering is done by simply hiding the
 * table's rows that are filtered.
 * <p>
 * Add / remove filters from table with
 * {@link SpreadsheetFilterTable#registerFilter(PopupButton, SpreadsheetFilter)}
 * and
 * {@link SpreadsheetFilterTable#unRegisterFilter(PopupButton, SpreadsheetFilter)}
 * <p>
 * When a filter been has updated (by server side or user actions),
 * {@link SpreadsheetFilterTable#onFiltersUpdated()} should be called.
 * 
 * @author Vaadin Ltd.
 */
public interface SpreadsheetFilter {

    /**
     * Clear the filtering options. After this method the
     * {@link #getFilteredRows()} for this filter should return an empty set.
     */
    public void clearFilter();

    /**
     * Returns the rows that should be filtered by this filter. In other words
     * the returned set of rows will be hidden from the table.
     * 
     * @return Row indexes of the filtered rows, 0-based
     */
    public Set<Integer> getFilteredRows();
}
