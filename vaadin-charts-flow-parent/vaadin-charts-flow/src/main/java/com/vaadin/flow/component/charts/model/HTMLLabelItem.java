/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
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
 * A HTML label that can be positioned anywhere in the chart area.
 */
@SuppressWarnings("serial")
public class HTMLLabelItem extends AbstractConfigurationObject {
    private String html;
    private LabelStyle style;

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
    public HTMLLabelItem(String html, LabelStyle style) {
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
     * @see #setStyle(LabelStyle)
     */
    public LabelStyle getStyle() {
        return style;
    }

    /**
     * Sets the CSS style for the label. To position the label, use
     * {@link LabelStyle#setLeft(String)} and {@link LabelStyle#setTop(String)}.
     *
     * @param style
     */
    public void setStyle(LabelStyle style) {
        this.style = style;
    }
}
