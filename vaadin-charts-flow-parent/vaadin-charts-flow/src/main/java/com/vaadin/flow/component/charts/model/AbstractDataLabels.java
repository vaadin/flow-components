package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.model.style.Style;

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

public abstract class AbstractDataLabels extends AbstractConfigurationObject {

    public static final String OVERFLOW_JUSTIFY = "justify";
    public static final String OVERFLOW_NONE = "none";

    /**
     * @see #setClassName(String)
     */
    public abstract String getClassName();

    /**
     * A class name for the data label.
     */
    public abstract void setClassName(String className);

    /**
     * @see #setBackgroundColor(Color)
     */
    public abstract Color getBackgroundColor();

    /**
     * The background color or gradient for the data label.
     */
    public abstract void setBackgroundColor(Color backgroundColor);

    /**
     * @see #setBorderColor(Color)
     */
    public abstract Color getBorderColor();

    /**
     * The border color for the data label.
     */
    public abstract void setBorderColor(Color borderColor);

    /**
     * @see #setBorderRadius(Number)
     */
    public abstract Number getBorderRadius();

    /**
     * The border radius in pixels for the data label.
     */
    public abstract void setBorderRadius(Number borderRadius);

    /**
     * @see #setColor(Color)
     */
    public abstract Color getColor();

    /**
     * The text color for the data labels.
     */
    public abstract void setColor(Color color);

    /**
     * @see #setBorderWidth(Number)
     */
    public abstract Number getBorderWidth();

    /**
     * The border width in pixels for the data label.
     */
    public abstract void setBorderWidth(Number borderWidth);

    /**
     * @see #setCrop(Boolean)
     */
    public abstract Boolean getCrop();

    /**
     * Whether to hide data labels that are outside the plot area. By default,
     * the data label is moved inside the plot area according to the overflow
     * option.
     */
    public abstract void setCrop(Boolean crop);

    /**
     * @see #setDefer(Boolean)
     */
    public abstract Boolean getDefer();

    /**
     * Whether to defer displaying the data labels until the initial series
     * animation has finished.
     */
    public abstract void setDefer(Boolean defer);

    /**
     * @see #setEnabled(Boolean)
     */
    public abstract Boolean getEnabled();

    /**
     * Enable or disable the data labels.
     */
    public abstract void setEnabled(Boolean enabled);

    /**
     * @see #setFormat(String)
     */
    public abstract String getFormat();

    /**
     * A format string for the data label. Available variables are the same as
     * for <code>formatter</code>.
     */
    public abstract void setFormat(String format);

    /**
     * @see #setFormatter(String)
     */
    public abstract String getFormatter();

    /**
     * Callback JavaScript function to format the data label. Note that if a
     * <code>format</code> is defined, the format takes precedence and the
     * formatter is ignored.
     */
    public abstract void setFormatter(String _fn_formatter);

    /**
     * @see #setInside(Boolean)
     */
    public abstract Boolean getInside();

    /**
     * For points with an extent, like columns, whether to align the data label
     * inside the box or to the actual value point.
     */
    public abstract void setInside(Boolean inside);

    /**
     * @see #setOverflow(String)
     */
    public abstract String getOverflow();

    /**
     * How to handle data labels that flow outside the plot area. The default is
     * <code>justify</code>, which aligns them inside the plot area. For columns
     * and bars, this means it will be moved inside the bar. To display data
     * labels outside the plot area, set <code>crop</code> to <code>false</code>
     * and <code>overflow</code> to <code>"none"</code>.
     */
    public abstract void setOverflow(String overflow);

    /**
     * @see #setPadding(Number)
     */
    public abstract Number getPadding();

    /**
     * When either the <code>borderWidth</code> or the
     * <code>backgroundColor</code> is set, this is the padding within the box.
     */
    public abstract void setPadding(Number padding);

    /**
     * @see #setRotation(Number)
     */
    public abstract Number getRotation();

    /**
     * Text rotation in degrees. Note that due to a more complex structure,
     * backgrounds, borders and padding will be lost on a rotated data label.
     */
    public abstract void setRotation(Number rotation);

    /**
     * @see #setShadow(Boolean)
     */
    public abstract Boolean getShadow();

    /**
     * The shadow of the box.
     */
    public abstract void setShadow(Boolean shadow);

    /**
     * @see #setShape(Shape)
     */
    public abstract Shape getShape();

    /**
     * The name of a symbol to use for the border around the label. Symbols are
     * predefined functions on the Renderer object.
     */
    public abstract void setShape(Shape shape);

    /**
     * @see #setStyle(Style)
     */
    public abstract Style getStyle();

    /**
     * Styles for the label.
     */
    public abstract void setStyle(Style style);

    /**
     * @see #setUseHTML(Boolean)
     */
    public abstract Boolean getUseHTML();

    /**
     * Whether to use HTML to render the labels.
     */
    public abstract void setUseHTML(Boolean useHTML);

    /**
     * @see #setVerticalAlign(VerticalAlign)
     */
    public abstract VerticalAlign getVerticalAlign();

    /**
     * The vertical alignment of a data label.
     */
    public abstract void setVerticalAlign(VerticalAlign verticalAlign);

    /**
     * @see #setZIndex(Number)
     */
    public abstract Number getZIndex();

    /**
     * The Z index of the data labels. The default Z index puts it above the
     * series. Use a Z index of 2 to display it behind the series.
     */
    public abstract void setZIndex(Number zIndex);
}
