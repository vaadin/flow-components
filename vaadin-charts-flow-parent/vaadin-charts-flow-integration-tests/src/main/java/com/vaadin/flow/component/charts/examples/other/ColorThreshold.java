package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.model.PlotOptionsArearange;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.dependency.CssImport;

@CssImport(value = "./styles/ColorThreshold.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
public class ColorThreshold extends AreaRange {

    @Override
    public void initDemo() {
        super.initDemo();
        PlotOptionsArearange plotOptions = new PlotOptionsArearange();
        // Make "value" below -5 displayed with another color. Default threshold
        // value is 0
        plotOptions.setThreshold(-5);
        plotOptions.setNegativeColor(new SolidColor("#434348"));
        chart.getConfiguration().setPlotOptions(plotOptions);
    }

}
