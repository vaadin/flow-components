package com.vaadin.flow.component.charts.model;

/*
 * #%L
 * Vaadin Charts
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
public class Items extends AbstractConfigurationObject {

    private static final long serialVersionUID = 1L;
    private String html;
    private Style style;

    public Items() {
    }

    /**
     * @see #setHtml(String)
     */
    public String getHtml() {
        return html;
    }

    /**
     * Inner HTML or text for the label.
     */
    public void setHtml(String html) {
        this.html = html;
    }

    /**
     * @see #setStyle(Style)
     */
    public Style getStyle() {
        if (style == null) {
            style = new Style();
        }
        return style;
    }

    /**
     * CSS styles for each label. To position the label, use left and top like
     * this:
     *
     * <pre>
     * style: {
     * 		left: '100px',
     * 		top: '100px'
     * 	}
     * </pre>
     */
    public void setStyle(Style style) {
        this.style = style;
    }
}
