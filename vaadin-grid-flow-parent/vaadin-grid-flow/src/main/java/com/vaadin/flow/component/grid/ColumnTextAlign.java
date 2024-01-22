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
package com.vaadin.flow.component.grid;

/**
 * Constants for the text alignment of columns.
 *
 * @author Vaadin Ltd.
 *
 * @see ColumnBase#setTextAlign(ColumnTextAlign)
 *
 */
public enum ColumnTextAlign {

    START("start"), CENTER("center"), END("end");

    private final String propertyValue;

    private ColumnTextAlign(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    /**
     * Converts the property name in String form to the corresponding enum
     * value. Values that cannot be mapped to a direct constant (including
     * <code>null</code>) are mapped to {@link ColumnTextAlign#START}.
     *
     * @param propertyValue
     *            the value for the textAlign property
     * @return the enum value corresponding to the property value, not
     *         <code>null</code>
     */
    public static ColumnTextAlign fromPropertyValue(String propertyValue) {
        if (propertyValue == null) {
            return START;
        }
        switch (propertyValue) {
        case "center":
            return CENTER;
        case "end":
            return END;
        default:
            return START;
        }
    }

    /**
     * Gets the client-side property for the textAlign property.
     *
     * @return the property value
     */
    public String getPropertyValue() {
        return propertyValue;
    }
}
