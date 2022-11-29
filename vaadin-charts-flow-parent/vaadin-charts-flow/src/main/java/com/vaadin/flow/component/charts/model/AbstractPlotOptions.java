package com.vaadin.flow.component.charts.model;

/**
 * Copyright (C) 2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

public abstract class AbstractPlotOptions extends AbstractConfigurationObject {

    public ChartType getChartType() {
        return ChartType.LINE;
    }
}
