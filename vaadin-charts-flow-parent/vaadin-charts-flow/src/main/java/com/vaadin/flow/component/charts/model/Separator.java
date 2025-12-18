/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Style;

public class Separator extends AbstractConfigurationObject {
    private String text;
    private Style style;

    /**
     * @return The text used as a separator between breadcrumb items.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text used as a separator between breadcrumb items.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return The style options for the separator text.
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Sets the style options for the separator text.
     */
    public void setStyle(Style style) {
        this.style = style;
    }
}
