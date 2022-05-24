package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.charts.model.style.Style;

/**
 * A HTML label that can be positioned anywhere in the chart area.
 */
@SuppressWarnings("serial")
public class HTMLLabelItem extends AbstractConfigurationObject {
    private String html;
    private Style style;

    /**
     * Constructs a HTMLLabelItem with the given HTML content
     *
     * @param html
     */
    public HTMLLabelItem(String html) {
        setHtml(html);
    }

    /**
     * Constructs a HTMLLabelItem with the given HTML content and style
     *
     * @param html
     * @param style
     */
    public HTMLLabelItem(String html, Style style) {
        this(html);
        setStyle(style);
    }

    /**
     * @see #setHtml(String)
     */
    public String getHtml() {
        return html;
    }

    /**
     * Sets the inner HTML or text for the label. Defaults to "".
     *
     * @param html
     */
    public void setHtml(String html) {
        this.html = html;
    }

    /**
     * @see #setStyle(Style)
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Sets the CSS style for the label. To position the label, use
     * {@link Style#setLeft(String)} and {@link Style#setTop(String)}.
     *
     * @param style
     */
    public void setStyle(Style style) {
        this.style = style;
    }
}
