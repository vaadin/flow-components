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
 * Options for displaying a message like "No data to display". This feature
 * requires the file <code>no-data-to-display.js</code> to be loaded in the
 * page. The actual text to display is set in the
 * <a href="#lang.noData">lang.noData</a> option.
 */
public class NoData extends AbstractConfigurationObject {

    private Position position;
    private Style style;
    private Boolean useHTML;

    public NoData() {
    }

    /**
     * @see #setPosition(Position)
     */
    public Position getPosition() {
        if (position == null) {
            position = new Position();
        }
        return position;
    }

    /**
     * The position of the no-data label, relative to the plot area.
     * <p>
     * Defaults to: { "x": 0, "y": 0, "align": "center", "verticalAlign":
     * "middle" }
     */
    public void setPosition(Position position) {
        this.position = position;
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
     * CSS styles for the no-data label.
     * <p>
     * Defaults to: { "fontSize": "12px", "fontWeight": "bold", "color":
     * "#666666" }
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see #setUseHTML(Boolean)
     */
    public Boolean getUseHTML() {
        return useHTML;
    }

    /**
     * Whether to insert the label as HTML, or as pseudo-HTML rendered with SVG.
     * <p>
     * Defaults to: false
     */
    public void setUseHTML(Boolean useHTML) {
        this.useHTML = useHTML;
    }
}
