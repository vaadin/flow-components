/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
