/*
 * Copyright 2000-2024 Vaadin Ltd.
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
 * Constants for the rendering mode of columns.
 *
 * @author Vaadin Ltd.
 *
 * @see Grid#setColumnRendering(ColumnRendering)
 */
public enum ColumnRendering {

    /**
     * In this mode, all columns are rendered upfront, regardless of their
     * visibility within the viewport.
     */
    EAGER("eager"),

    /**
     * In this mode, body cells are rendered only when their corresponding
     * columns are inside the visible viewport.
     */
    LAZY("lazy");

    private final String propertyValue;

    private ColumnRendering(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    /**
     * Converts the property name in String form to the corresponding enum
     * value. Values that cannot be mapped to a direct constant (including
     * <code>null</code>) are mapped to {@link ColumnRendering#EAGER}.
     *
     * @param propertyValue
     *            the value for the rendering property
     * @return the enum value corresponding to the property value, not
     *         <code>null</code>
     */
    public static ColumnRendering fromPropertyValue(String propertyValue) {
        if (propertyValue == null) {
            return EAGER;
        }
        switch (propertyValue) {
        case "lazy":
            return LAZY;
        default:
            return EAGER;
        }
    }

    /**
     * Gets the property value for the rendering mode.
     *
     * @return the property value
     */
    public String getPropertyValue() {
        return propertyValue;
    }

}
