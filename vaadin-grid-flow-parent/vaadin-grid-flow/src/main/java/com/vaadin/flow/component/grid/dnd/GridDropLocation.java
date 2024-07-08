/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.dnd;

/**
 * Defines drop locations within a Grid row.
 *
 * @author Vaadin Ltd.
 */
public enum GridDropLocation {

    /**
     * Drop on top of the row.
     */
    ON_TOP("on-top"),

    /**
     * Drop above or before the row.
     */
    ABOVE("above"),

    /**
     * Drop below or after the row.
     */
    BELOW("below"),

    /**
     * Dropping into an empty grid, or to the empty area below the grid rows
     * when {@link GridDropMode#ON_TOP} is used.
     */
    EMPTY("empty");

    private final String clientName;

    GridDropLocation(String clientName) {
        this.clientName = clientName;
    }

    /**
     * Gets name that is used in the client side representation of the
     * component.
     *
     * @return the name used in the client side representation of the component.
     */
    public String getClientName() {
        return clientName;
    }

}
