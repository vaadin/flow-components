/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

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
