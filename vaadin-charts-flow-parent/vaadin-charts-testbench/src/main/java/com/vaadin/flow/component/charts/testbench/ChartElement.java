/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.testbench;

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-chart")
public class ChartElement extends TestBenchElement {

    public List<TestBenchElement> getPoints() {
        return $(".highcharts-point").all();
    }

    public List<TestBenchElement> getVisiblePoints() {
        return $(":not([visibility=hidden]).highcharts-point").all();
    }

}
