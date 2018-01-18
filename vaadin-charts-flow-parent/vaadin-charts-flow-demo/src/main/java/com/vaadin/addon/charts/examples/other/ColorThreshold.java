package com.vaadin.addon.charts.examples.other;

import com.vaadin.addon.charts.model.PlotOptionsArearange;

public class ColorThreshold extends AreaRange {

    @Override
    public void initDemo() {
        super.initDemo();
        PlotOptionsArearange plotOptions = new PlotOptionsArearange();
        // Make "value" below -5 displayed with another color. Default threshold value is 0
        plotOptions.setThreshold(-5);
        plotOptions.setNegativeFillColor(true);
        chart.getConfiguration().setPlotOptions(plotOptions);
    }

}
