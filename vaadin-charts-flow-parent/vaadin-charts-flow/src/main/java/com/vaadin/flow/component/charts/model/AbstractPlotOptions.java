/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

public abstract class AbstractPlotOptions extends AbstractConfigurationObject {

    public ChartType getChartType() {
        return ChartType.LINE;
    }
}
