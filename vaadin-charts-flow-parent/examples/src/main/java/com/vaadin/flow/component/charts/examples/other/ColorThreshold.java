package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.model.PlotOptionsArearange;

public class ColorThreshold extends AreaRange {

    @Override
    public void initDemo() {
        super.initDemo();
        PlotOptionsArearange plotOptions = new PlotOptionsArearange();
        // Make "value" below -5 displayed with another color. Default threshold value is 0
        plotOptions.setThreshold(-5);
        plotOptions.setNegativeColor(true);
        chart.getConfiguration().setPlotOptions(plotOptions);
    }

}
