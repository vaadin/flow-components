package com.vaadin.flow.component.charts.model;

import java.time.Instant;

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
 * DataSeriesItem that can hold also Label and Description values. Used in e.g.
 * timeline series.
 */
public class DataSeriesItemTimeline extends DataSeriesItem {

    private String label;

    public DataSeriesItemTimeline() {
        super();
    }

    /**
     * Constructs an item with Name, Label and Description values
     *
     * @param name
     * @param label
     * @param description
     */
    public DataSeriesItemTimeline(String name, String label,
            String description) {
        super();
        setName(name);
        setLabel(label);
        setDescription(description);
    }

    /**
     * Constructs an item with X, Name, Label and Description values
     *
     * @param x
     * @param name
     * @param label
     * @param description
     */
    public DataSeriesItemTimeline(Number x, String name, String label,
            String description) {
        this(name, label, description);
        setX(x);
    }

    /**
     * Constructs an item with X, Name, Label and Description values
     *
     * @param x
     * @param name
     * @param label
     * @param description
     */
    public DataSeriesItemTimeline(Instant x, String name, String label,
            String description) {
        super();
        setX(x);
        setName(name);
        setLabel(label);
        setDescription(description);
    }

    /**
     * @see #setLabel(String)
     */
    public String getLabel() {
        return label;
    }

    /**
     * The label of event.
     */
    public void setLabel(String label) {
        this.label = label;
        makeCustomized();
    }

    /**
     * The description of event. This description will be shown in tooltip.
     * <p>
     * Defaults to: undefined
     */
    @Override
    public void setDescription(String description) {
        super.setDescription(description);
    }
}
