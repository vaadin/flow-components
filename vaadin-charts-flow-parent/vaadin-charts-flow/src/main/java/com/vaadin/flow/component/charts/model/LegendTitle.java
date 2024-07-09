/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * A title to be added on top of the legend.
 */
public class LegendTitle extends AbstractConfigurationObject {

    private String text;

    public LegendTitle() {
    }

    public LegendTitle(String text) {
        this.text = text;
    }

    /**
     * @see #setText(String)
     */
    public String getText() {
        return text;
    }

    /**
     * A text or HTML string for the title.
     * <p>
     * Defaults to: null
     */
    public void setText(String text) {
        this.text = text;
    }
}
