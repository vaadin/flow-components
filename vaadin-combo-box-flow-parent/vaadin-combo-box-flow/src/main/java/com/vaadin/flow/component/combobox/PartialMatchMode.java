/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox;

import java.util.Arrays;

/**
 * Defines which item in a combo box is automatically set to be selected on
 * Enter or outside click when the typed filter only partially matches its
 * label. The item that will be selected is highlighted in the dropdown while
 * typing.
 *
 * @author Vaadin Ltd.
 * @see ComboBoxBase#setPartialMatchMode(PartialMatchMode)
 * @since 25.3
 */
public enum PartialMatchMode {

    /**
     * An item is automatically set to be selected only when the filter matches
     * its label exactly.
     */
    NONE("none"),

    /**
     * The first item in the filtered results is automatically set to be
     * selected.
     */
    FIRST_MATCH("first-match"),

    /**
     * The item is automatically set to be selected when filtering narrows the
     * results to a single item.
     */
    ONLY_MATCH("only-match");

    private final String clientName;

    PartialMatchMode(String clientName) {
        this.clientName = clientName;
    }

    /**
     * Gets the name that is used in the client-side representation of the
     * component.
     *
     * @return the name used in the client-side representation of the component
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Gets the mode matching the given client-side name, or {@link #NONE} if no
     * mode matches.
     *
     * @param clientName
     *            the client-side name of the mode
     * @return the matching mode, or {@link #NONE}
     */
    static PartialMatchMode fromClientName(String clientName) {
        return Arrays.stream(values())
                .filter(mode -> mode.getClientName().equals(clientName))
                .findFirst().orElse(NONE);
    }
}
