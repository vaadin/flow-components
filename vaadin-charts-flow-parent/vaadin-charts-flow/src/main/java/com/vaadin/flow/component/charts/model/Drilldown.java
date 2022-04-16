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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vaadin.flow.component.charts.model.style.Style;

/**
 * Options for drill down, the concept of inspecting increasingly high
 * resolution data through clicking on chart items like columns or pie slices.
 */
public class Drilldown extends AbstractConfigurationObject {

    private Style activeAxisLabelStyle;
    private Style activeDataLabelStyle;
    private Boolean allowPointDrilldown;
    private Boolean animation;
    private DrillUpButton drillUpButton;
    private List<Series> series = new ArrayList<>();

    @JsonIgnore
    private Configuration configuration;

    /**
     * Adds a series configurations for the drilldown. These drilldown series
     * are hidden by default. The drilldown series is linked to the parent
     * series' point by its {@link Series#getId()}
     *
     * @param series
     */
    void addSeries(Series series) {
        this.series.add(series);
    }

    /**
     * Sets the configuration linked to the drilldown series.
     *
     * @param configuration
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * @see #setConfiguration(Configuration)
     * @return the {@link Configuration} that this series is linked to.
     */
    @JsonIgnoreProperties
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * @see #setActiveAxisLabelStyle(Style)
     * @return
     */
    public Style getActiveAxisLabelStyle() {
        return activeAxisLabelStyle;
    }

    /**
     * Additional styles to apply to the X axis label for a point that has
     * drilldown data.
     *
     * @param activeAxisLabelStyle
     */
    public void setActiveAxisLabelStyle(Style activeAxisLabelStyle) {
        this.activeAxisLabelStyle = activeAxisLabelStyle;
    }

    /**
     * @see #setActiveDataLabelStyle(Style)
     * @return
     */
    public Style getActiveDataLabelStyle() {
        return activeDataLabelStyle;
    }

    /**
     * Additional styles to apply to the data label of a point that has
     * drilldown data.
     *
     * @param activeDataLabelStyle
     */
    public void setActiveDataLabelStyle(Style activeDataLabelStyle) {
        this.activeDataLabelStyle = activeDataLabelStyle;
    }

    /**
     * @return true if animation is enabled false otherwse.
     */
    public Boolean getAnimation() {
        return animation;
    }

    /**
     * Set the animation for all drilldown animations. Animation of a drilldown
     * occurs when drilling between a column point and a column series, or a pie
     * slice and a full pie series. Drilldown can still be used between series
     * and points of different types, but animation will not occur.
     *
     * @param animation
     */
    public void setAnimation(Boolean animation) {
        this.animation = animation;
    }

    /**
     * @see Drilldown#setDrillUpButton(DrillUpButton)
     * @return
     */
    public DrillUpButton getDrillUpButton() {
        if (drillUpButton == null) {
            drillUpButton = new DrillUpButton();
        }
        return drillUpButton;
    }

    /**
     * Options for the drill up button that appears when drilling down on a
     * series. The text for the button is defined in
     * {@link Lang#setDrillUpText(String)}.
     *
     * @param drillUpButton
     */
    public void setDrillUpButton(DrillUpButton drillUpButton) {
        this.drillUpButton = drillUpButton;
    }

    /**
     * @see #setAllowPointDrilldown(Boolean)
     */
    public Boolean getAllowPointDrilldown() {
        return allowPointDrilldown;
    }

    /**
     * When this option is false, clicking a single point will drill down all
     * points in the same category, equivalent to clicking the X axis label.
     * <p>
     * Defaults to: true
     */
    public void setAllowPointDrilldown(Boolean allowPointDrilldown) {
        this.allowPointDrilldown = allowPointDrilldown;
    }
}
