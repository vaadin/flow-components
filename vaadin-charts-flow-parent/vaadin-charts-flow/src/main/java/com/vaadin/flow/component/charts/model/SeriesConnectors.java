/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * A configuration object to override Pathfinder connector options for a series.
 * Requires Highcharts Gantt.
 */
@SuppressWarnings("unused")
public class SeriesConnectors extends ConnectorStyle {

    private SeriesConnectorAnimation animation;

    public SeriesConnectorAnimation getAnimation() {
        return animation;
    }

    public void setAnimation(SeriesConnectorAnimation animation) {
        this.animation = animation;
    }
}
