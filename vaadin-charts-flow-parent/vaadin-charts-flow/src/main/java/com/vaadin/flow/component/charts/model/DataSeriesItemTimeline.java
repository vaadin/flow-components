package com.vaadin.flow.component.charts.model;

import java.time.Instant;

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

/**
 * DataSeriesItem that can hold also Label and Description values. Used in e.g. timeline series.
 */
public class DataSeriesItemTimeline extends DataSeriesItem {

    private String label;
    private String description;

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
    public DataSeriesItemTimeline(String name, String label, String description) {
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
    public DataSeriesItemTimeline(Number x, String name, String label, String description) {
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
    public DataSeriesItemTimeline(Instant x, String name, String label, String description) {
        super();
        setX(x);
        setName(name);
        setLabel(label);
        setDescription(description);
    }

    /**
     * @see #setDescription(String)
     */
    public String getDescription() {
        return description;
    }

    /**
     * <p>
     * <i>Requires Accessibility module</i>
     * </p>
     * <p>
     * A description of the series to add to the screen reader information about the
     * series.
     * </p>
     * <p>
     * Defaults to: undefined
     */
    public void setDescription(String description) {
        this.description = description;
        makeCustomized();
    }

    /**
     * @see #setLabel(String)
     */
    public String getLabel() {
        return label;
    }

    /**
     * <p>
     * <i>Requires Accessibility module</i>
     * </p>
     * <p>
     * A description of the series to add to the screen reader information about the
     * series.
     * </p>
     * <p>
     * Defaults to: undefined
     */
    public void setLabel(String label) {
        this.label = label;
        makeCustomized();
    }
}
