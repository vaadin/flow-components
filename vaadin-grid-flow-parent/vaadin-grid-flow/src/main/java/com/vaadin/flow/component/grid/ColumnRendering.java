/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
