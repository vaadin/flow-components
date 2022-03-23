package com.vaadin.flow.component.crud;

/*
 * #%L
 * Vaadin Crud for Vaadin 10
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.data.provider.SortDirection;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

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
