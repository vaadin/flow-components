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
