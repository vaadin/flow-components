/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.pie;

import com.vaadin.flow.component.charts.SkipFromDemo;

@SkipFromDemo
public class PieWithSize extends PieWithLegend {

    @Override
    public void initDemo() {
        super.initDemo();
        chart.setHeight("500px");
        chart.setWidth("400px");
    }

}
