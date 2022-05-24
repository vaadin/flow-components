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
 * Options for keyboard navigation.
 */
public class KeyboardNavigation extends AbstractConfigurationObject {

    private Boolean enabled;
    private Boolean skipNullPoints;
    private Boolean tabThroughChartElements;

    public KeyboardNavigation() {
    }

    public KeyboardNavigation(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable keyboard navigation for the chart.
     * <p>
     * Defaults to: true
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setSkipNullPoints(Boolean)
     */
    public Boolean getSkipNullPoints() {
        return skipNullPoints;
    }

    /**
     * Skip null points when navigating through points with the keyboard.
     * <p>
     * Defaults to: false
     */
    public void setSkipNullPoints(Boolean skipNullPoints) {
        this.skipNullPoints = skipNullPoints;
    }

    /**
     * @see #setTabThroughChartElements(Boolean)
     */
    public Boolean getTabThroughChartElements() {
        return tabThroughChartElements;
    }

    /**
     * Enable tab navigation for points. Without this, only arrow keys can be
     * used to navigate between points.
     * <p>
     * Defaults to: true
     */
    public void setTabThroughChartElements(Boolean tabThroughChartElements) {
        this.tabThroughChartElements = tabThroughChartElements;
    }
}
