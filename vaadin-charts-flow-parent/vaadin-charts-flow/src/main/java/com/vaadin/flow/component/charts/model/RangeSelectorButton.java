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
