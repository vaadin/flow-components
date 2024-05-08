/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import java.time.Instant;

/**
 * DataSeriesItem that can hold also "from", "to" and "weight". Used in sankey
 * series.
 */
public class DataSeriesItemSankey extends DataSeriesItem {

    private String from;
    private String to;
    private Number weight;

    public DataSeriesItemSankey() {
        super();
        makeCustomized();
    }

    /**
     * Constructs an item with from, to and weight values
     *
     * @param from
     * @param to
     * @param weight
     */
    public DataSeriesItemSankey(String from, String to, Number weight) {
        this();
        setFrom(from);
        setTo(to);
        setWeight(weight);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Number getWeight() {
        return weight;
    }

    public void setWeight(Number weight) {
        this.weight = weight;
    }
}
