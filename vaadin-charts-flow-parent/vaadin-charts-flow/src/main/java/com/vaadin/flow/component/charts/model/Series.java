/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import java.io.Serializable;

/**
 * Series interface for all kinds of Series
 */
public interface Series extends Serializable {

    /**
     * @see #setName(String)
     * @return The name of the series.
     */
    String getName();

    /**
     * Sets the name of the series as shown in the legend, tooltip etc. Defaults
     * to "".
     *
     * @param name
     */
    void setName(String name);

    /**
     * Sets the configuration to which this series is linked.
     *
     * @param configuration
     */
    void setConfiguration(Configuration configuration);

    /**
     * Gets the plot options related to this specific series. This is needed
     * e.g. in combined charts.
     *
     * @return
     */
    AbstractPlotOptions getPlotOptions();

    /**
     * Sets the plot options for this specific series. The type of the plot
     * options also explicitly sets the chart type used when rendering this
     * particular series. If plot options is null, the component wide chart type
     * is used.
     * <p>
     * Options that are not defined at this level will be inherited from the
     * chart and theme levels.
     *
     * @param plotOptions
     */
    void setPlotOptions(AbstractPlotOptions plotOptions);

    /**
     * @return the series ID
     */
    String getId();

    /**
     * Sets an id for the series
     *
     * @param id
     *            new ID to set
     */
    void setId(String id);
}
