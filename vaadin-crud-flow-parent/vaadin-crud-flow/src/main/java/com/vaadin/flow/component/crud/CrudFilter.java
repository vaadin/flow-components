/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.flow.data.provider.SortDirection;

/**
 * The filter type for use with {@link CrudGrid}. This provides information
 * about the filter constraints and sort orders applied to the grid by the user.
 */
public class CrudFilter implements Serializable {

    private final Map<String, String> constraints = new LinkedHashMap<>();
    private final Map<String, SortDirection> sortOrders = new LinkedHashMap<>();

    /**
     * Returns the filter constraint applied to the grid as a map of column to
     * filter text.
     *
     * @return all constraints for the grid
     */
    public Map<String, String> getConstraints() {
        return constraints;
    }

    /**
     * Returns the sort orders applied to the grid as a map of column to sort
     * direction. Only columns with active sorting are present.
     *
     * @return the sort orders for the grid
     */
    public Map<String, SortDirection> getSortOrders() {
        return sortOrders;
    }
}
