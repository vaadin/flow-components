package com.vaadin.addon.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2012 - 2016 Vaadin Ltd
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