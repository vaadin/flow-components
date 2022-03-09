package com.vaadin.flow.component.charts.examples.pie;

import com.vaadin.flow.component.charts.examples.SkipFromDemo;

@SkipFromDemo
public class PieWithSize extends PieWithLegend {

    @Override
    public void initDemo() {
        super.initDemo();
        chart.setHeight("500px");
        chart.setWidth("400px");
    }

}
