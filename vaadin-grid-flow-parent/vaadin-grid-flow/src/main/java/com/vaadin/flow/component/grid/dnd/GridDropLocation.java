/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
