package com.vaadin.flow.component.crud;

/*
 * #%L
 * Vaadin Crud for Vaadin 10
 * %%
 * Copyright (C) 2018 Vaadin Ltd
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

import com.vaadin.flow.data.provider.SortDirection;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleCrudFilter {

    private final Map<String, String> constraints = new LinkedHashMap<>();
    private final Map<String, SortDirection> sortOrders = new LinkedHashMap<>();

    public Map<String, String> getConstraints() {
        return constraints;
    }

    public Map<String, SortDirection> getSortOrders() {
        return sortOrders;
    }
}
