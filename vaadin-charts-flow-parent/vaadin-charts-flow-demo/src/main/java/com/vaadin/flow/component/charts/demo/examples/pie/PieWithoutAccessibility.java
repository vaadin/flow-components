package com.vaadin.flow.component.charts.demo.examples.pie;

import com.vaadin.flow.component.charts.demo.SkipFromDemo;
import com.vaadin.flow.component.charts.model.Accessibility;

@SkipFromDemo
public class PieWithoutAccessibility extends PieWithLegend {

    @Override
    public void initDemo() {
        super.initDemo();
        chart.getConfiguration().setAccessibility(new Accessibility(false));
    }

}
