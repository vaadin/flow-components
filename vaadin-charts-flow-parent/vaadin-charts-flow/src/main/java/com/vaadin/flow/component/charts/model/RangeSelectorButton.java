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
 * Options for range selector buttons.
 */
public class RangeSelectorButton extends AbstractConfigurationObject {

    private RangeSelectorTimespan type;
    private Number count;
    private String text;
    private DataGrouping dataGrouping;

    public RangeSelectorButton() {
    }

    public RangeSelectorButton(RangeSelectorTimespan type, Number count,
            String text) {
        this.type = type;
        this.count = count;
        this.text = text;
    }

    public RangeSelectorButton(RangeSelectorTimespan type, String text) {
        this.type = type;
        this.text = text;
    }

    /**
     * @see #setType(RangeSelectorTimespan)
     */
    public RangeSelectorTimespan getType() {
        return type;
    }

    /**
     * Defines the timespan for the button
     *
     * @param type
     */
    public void setType(RangeSelectorTimespan type) {
        this.type = type;
    }

    /**
     * @see #setCount(Number)
     */
    public Number getCount() {
        return count;
    }

    /**
     * Defines how many units of the defined type to use.
     *
     * @param count
     */
    public void setCount(Number count) {
        this.count = count;
    }

    /**
     * @see #setText(String)
     */
    public String getText() {
        return text;
    }

    /**
     * Defines the text for the button
     *
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @see #setDataGrouping(DataGrouping)
     */
    public DataGrouping getDataGrouping() {
        return dataGrouping;
    }

    /**
     * Defines a custom data grouping definition for the button
     *
     * @param dataGrouping
     */
    public void setDataGrouping(DataGrouping dataGrouping) {
        this.dataGrouping = dataGrouping;
    }

}
