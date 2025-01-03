/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * Progress indicator, how much of the task completed
 */
@SuppressWarnings("unused")
public class Completed extends AbstractConfigurationObject {

    private Number amount;
    private Color fill;

    public Completed() {
    }

    public Completed(Number amount) {
        this.amount = amount;
    }

    public Completed(Number amount, Color fill) {
        this.amount = amount;
        this.fill = fill;
    }

    /**
     * @see #setAmount(Number)
     */
    public Number getAmount() {
        return amount;
    }

    /**
     * The amount of the progress indicator, ranging from 0 (not started) to 1
     * (finished). Defaults to 0.
     * 
     * @param amount
     */
    public void setAmount(Number amount) {
        this.amount = amount;
    }

    /**
     * @see #setFill(Color)
     */
    public Color getFill() {
        return fill;
    }

    /**
     * The fill of the progress indicator. Defaults to a darkened variety of the
     * main color. The value will be ignored if the chart is in Styled mode.
     * 
     * @param fill
     */
    public void setFill(Color fill) {
        this.fill = fill;
    }
}
