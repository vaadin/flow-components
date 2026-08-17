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

/**
 * Defines whether an item whose label partially matches the typed filter is
 * automatically focused in the dropdown of a combo box.
 *
 * @author Vaadin Ltd.
 * @see ComboBoxBase#setAutoFocusPartialMatch(AutoFocusPartialMatch)
 * @since 25.3
 */
public enum AutoFocusPartialMatch {

    /**
     * Partial matches are not focused.
     */
    NONE("none"),

    /**
     * The first item in the filtered results is focused.
     */
    FIRST_MATCH("first-match"),

    /**
     * The item is focused when filtering narrows the results to a single item.
     */
    ONLY_MATCH("only-match");

    private final String clientName;

    AutoFocusPartialMatch(String clientName) {
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
}
