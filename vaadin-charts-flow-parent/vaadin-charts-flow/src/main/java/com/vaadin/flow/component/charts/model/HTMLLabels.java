package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.charts.model.style.LabelStyle;

/**
 * HTML labels that can be positioned anywhere in the chart area.
 */
@SuppressWarnings("serial")
public class HTMLLabels extends AbstractConfigurationObject {
    private HTMLLabelItem[] items;
    private LabelStyle style;

    public HTMLLabels() {
    }

    /**
     * Constructs a HTMLLabels instance with the given label items
     *
     * @param items
     */
    public HTMLLabels(HTMLLabelItem... items) {
        setItems(items);
    }

    /**
     * Constructs a HTMLLabels instance with a style shared for all the labels
     * and the given label items
     *
     * @param items
     */
    public HTMLLabels(LabelStyle style, HTMLLabelItem... items) {
        setItems(items);
        setStyle(style);
    }

    /**
     * @return An array of the label items
     */
    public HTMLLabelItem[] getItems() {
        return items;
    }

    /**
     * Sets the label items
     *
     * @param items
     */
    public void setItems(HTMLLabelItem... items) {
        this.items = items;
    }

    /**
     * @see #setStyle(LabelStyle)
     */
    public LabelStyle getStyle() {
        return style;
    }

    /**
     * Sets the CSS style shared for all the labels.
     *
     * @param style
     */
    public void setStyle(LabelStyle style) {
        this.style = style;
    }
}
