/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.model.PlotOptionsArearange;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.dependency.CssImport;

@CssImport(value = "./styles/ColorThreshold.css", themeFor = "vaadin-chart")
public class ColorThreshold extends AreaRange {

    @Override
    public void initDemo() {
        super.initDemo();
        chart.setClassName("ColorThreshold");

        PlotOptionsArearange plotOptions = new PlotOptionsArearange();
        // Make "value" below -5 displayed with another color. Default threshold
        // value is 0
        plotOptions.setThreshold(-5);
        plotOptions.setNegativeColor(new SolidColor("#434348"));
        chart.getConfiguration().setPlotOptions(plotOptions);
    }

}
