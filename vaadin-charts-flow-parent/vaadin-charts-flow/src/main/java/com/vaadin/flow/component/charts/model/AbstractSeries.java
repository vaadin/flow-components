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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.vaadin.flow.component.charts.Chart;

/**
 * Abstract base class for series
 */
public abstract class AbstractSeries extends AbstractConfigurationObject
        implements Series {

    private String name;
    private String stack;
    private String id;

    @JsonUnwrapped
    private AbstractPlotOptions plotOptions;

    private Boolean visible;

    @JsonIgnore
    private Configuration configuration;

    private Integer xAxis;
    private Integer yAxis;
    private Integer colorAxis;

    public AbstractSeries() {
    }

    /**
     * Constructs a named series
     */
    public AbstractSeries(String name) {
        setName(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @see #setStack(String)
     */
    public String getStack() {
        return stack;
    }

    /**
     * This option allows grouping series in a stacked chart. The stack option
     * can be a string or a number or anything else, as long as the grouped
     * series' stack options match each other. Defaults to null.
     *
     * @param stack
     */
    public void setStack(String stack) {
        this.stack = stack;
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * @return the {@link Configuration} that this series is linked to.
     */
    @JsonIgnoreProperties
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPlotOptions getPlotOptions() {
        return plotOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPlotOptions(AbstractPlotOptions plotOptions) {
        this.plotOptions = plotOptions;
    }

    /**
     * Control the visibility of the series. Although the series is invisible in
     * the client it is still "cached" there and thus setting it visible happens
     * quickly.
     *
     * @see #setVisible(boolean, boolean)
     *
     * @param visible
     *            true if the series should be displayed, false if not
     */
    public void setVisible(Boolean visible) {
        setVisible(visible, true);
    }

    /**
     * Control the visibility of the series.
     * <p>
     * With this version of the method developer can disable immediate chart
     * update for already rendered chart, if e.g. multiple changes to the chart
     * configuration are wished to be applied at once.
     *
     * @see #setVisible(Boolean)
     * @see Chart#drawChart()
     *
     * @param visible
     *            true if the series should be displayed, false if not
     * @param updateChartImmediately
     *            Updates the chart immediately if true.
     */
    public void setVisible(Boolean visible, boolean updateChartImmediately) {
        boolean doDynamicChange = updateChartImmediately
                && getConfiguration() != null && this.visible != visible;
        this.visible = visible;
        if (doDynamicChange) {
            getConfiguration().fireSeriesEnabled(this, visible);
        }

    }

    /**
     * @return true if the series is displayed on the client
     */
    public Boolean isVisible() {
        if (visible == null) {
            return Boolean.TRUE;
        }

        return visible;
    }

    /**
     * @see #setxAxis(Number)
     * @return The index of the X-axis that this data series is bound to.
     *         Returns null if undefined.
     */
    public Integer getxAxis() {
        return xAxis;
    }

    /**
     * When using dual or multiple X-axes, this number defines which X-axis the
     * particular series is connected to. It refers to the index of the axis in
     * the X-axis array, with 0 being the first. Defaults to 0.
     *
     * @param xAxis
     *            The index of the X-axis to bind this data series to.
     */
    public void setxAxis(Integer xAxis) {
        this.xAxis = xAxis;
    }

    /**
     * @see #setyAxis(Number)
     * @return The index of the Y-axis that this data series is bound to.
     *         Returns null if undefined.
     */
    public Integer getyAxis() {
        return yAxis;
    }

    /**
     * When using dual or multiple Y-axes, this number defines which Y-axis the
     * particular series is connected to. It refers to the index of the axis in
     * the Y-axis array, with 0 being the first. Defaults to 0.
     *
     * @param yAxis
     *            The index of the Y-axis to bind this data series to.
     */
    public void setyAxis(Integer yAxis) {
        this.yAxis = yAxis;
    }

    /**
     * @see #setColorAxis(Number)
     * @return The index of the color-axis that this data series is bound to.
     *         Returns null if undefined.
     */
    public Integer getColorAxis() {
        return colorAxis;
    }

    /**
     * When using dual or multiple color-axes, this number defines which
     * color-axis the particular series is connected to. It refers to the index
     * of the axis in the color-axis array, with 0 being the first. Defaults to
     * 0.
     *
     * @param colorAxis
     *            The index of the color-axis to bind this data series to.
     */
    public void setColorAxis(Integer colorAxis) {
        this.colorAxis = colorAxis;
    }

    /**
     * * When using dual or multiple Y-axes, this number defines which Y-axis
     * the particular series is connected to.
     *
     * <p>
     * Note, that this method cannot be used until series and axis are both
     * attached to same configuration object.
     *
     * @see #setyAxis(Number)
     *
     * @param secondaryAxis
     */
    public void setyAxis(YAxis secondaryAxis) {
        if (configuration == null) {
            throw new IllegalStateException(
                    "This method must be called only when series is attached to configuration options");
        }
        int indexOf = configuration.getyAxes().indexOf(secondaryAxis);
        if (indexOf == -1) {
            throw new IllegalStateException(
                    "This method can only be used if axis is already attached to the same configuration object");
        }
        setyAxis(indexOf);
    }

    public void updateSeries() {
        if (getConfiguration() != null) {
            getConfiguration().fireSeriesChanged(this);
        }
    }
}
