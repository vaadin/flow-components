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

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * PartialFill configuration object to be used in {@link DataSeriesItemXrange}.
 * Typically used to visualize how much of a task is performed.
 */
public class ItemPartialFill extends AbstractConfigurationObject {

    private Color fill;
    private Number amount;

    /**
     * Creates an empty PartialFill configuration object
     */
    public ItemPartialFill() {

    }

    /**
     * Creates a new PartialFill with the defined fill amount
     *
     * @param amount
     *            The amount of the xrange point to be filled.
     */
    public ItemPartialFill(Number amount) {
        this.amount = amount;
    }

    /**
     * Creates a new PartialFill with the defined fill color and amount
     *
     * @param amount
     *            The amount of the xrange point to be filled.
     * @param fill
     *            The color to be used for partial fills
     */
    public ItemPartialFill(Number amount, Color fill) {
        this(amount);
        this.fill = fill;
    }

    /**
     * @see #setFill(Color)
     *
     * @return the color used for partial fills
     */
    public Color getFill() {
        return fill;
    }

    /**
     * The fill color to be used for partial fills. When <code>null</code>, a
     * darker shade of the point's color is used.
     *
     * @param fill
     *            color to be used for partial fills
     */
    public void setFill(Color fill) {
        this.fill = fill;
    }

    /**
     * @see #setAmount(Number)
     *
     * @return the amount used for partial fill
     */
    public Number getAmount() {
        return amount;
    }

    /**
     * The amount of the xrange point to be filled. Values can be 0-1 and are
     * converted to percentages in the default data label formatter.
     *
     * @param amount
     */
    public void setAmount(Number amount) {
        this.amount = amount;
    }

}
