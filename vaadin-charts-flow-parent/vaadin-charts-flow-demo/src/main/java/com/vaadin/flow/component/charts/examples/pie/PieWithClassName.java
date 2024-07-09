/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.pie;

import com.vaadin.flow.component.charts.SkipFromDemo;

@SkipFromDemo
public class PieWithClassName extends PieWithLegend {

    public static final String CLASS_NAME = "redchart";

    @Override
    public void initDemo() {
        super.initDemo();
        chart.addClassName(CLASS_NAME);
    }

}
