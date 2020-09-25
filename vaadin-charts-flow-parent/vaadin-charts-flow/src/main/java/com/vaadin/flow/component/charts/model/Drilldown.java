package com.vaadin.flow.component.charts.model;

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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Options for drill down, the concept of inspecting increasingly high
 * resolution data through clicking on chart items like columns or pie slices.
 */
public class Drilldown extends AbstractConfigurationObject {

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
