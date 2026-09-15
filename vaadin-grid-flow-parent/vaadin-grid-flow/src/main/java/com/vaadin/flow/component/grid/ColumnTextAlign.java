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
package com.vaadin.flow.component.grid;

/**
 * Constants for the text alignment of columns.
 *
 * @author Vaadin Ltd.
 *
 * @see Grid.Column#setTextAlign(ColumnTextAlign)
 *
 * @since 2.1
 */
public enum ColumnTextAlign {

    /**
     * Aligns the content to the start of the cell, which is the left side in
     * left-to-right and the right side in right-to-left layout direction.
     */
    START("start"),

    /**
     * Aligns the content to the center of the cell.
     */
    CENTER("center"),

    /**
     * Aligns the content to the end of the cell, which is the right side in
     * left-to-right and the left side in right-to-left layout direction.
     */
    END("end"),

    /**
     * Aligns the content to the left side of the cell in both left-to-right and
     * right-to-left layout direction.
     *
     * @since 25.3
     */
    LEFT("left"),

    /**
     * Aligns the content to the right side of the cell in both left-to-right
     * and right-to-left layout direction.
     *
     * @since 25.3
     */
    RIGHT("right");

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
        for (ColumnTextAlign textAlign : values()) {
            if (textAlign.getPropertyValue().equals(propertyValue)) {
                return textAlign;
            }
        }
        return START;
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
