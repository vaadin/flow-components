/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.column;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.Legend;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.style.PatternColor;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.router.Route;

/**
 * Two identical column charts differing only in styled mode, to exercise both
 * pattern-fill paths:
 * <ul>
 * <li>styled mode on: the Vaadin CSS-rule bridge (series-wide patterns applied
 * through a shadow-scoped rule on the series' {@code highcharts-color-N}
 * class);</li>
 * <li>styled mode off (the Flow default): Highcharts' own pattern-fill module
 * renders {@code color:{pattern}} natively via {@code fill} attributes.</li>
 * </ul>
 */
@Route("vaadin-charts/column/column-pattern-fill")
public class ColumnPatternFill extends AbstractChartExample {

    @Override
    public void initDemo() {
        add(buildChart("Pattern fill (styled mode)", true));
        add(buildChart("Pattern fill (non-styled mode)", false));
    }

    private Chart buildChart(String title, boolean styledMode) {
        Chart chart = new Chart(ChartType.COLUMN);

        Configuration configuration = chart.getConfiguration();
        configuration.setTitle(title);
        configuration.getChart().setStyledMode(styledMode);

        XAxis x = new XAxis();
        x.setCategories("A", "B", "C");
        configuration.addxAxis(x);

        // Enable the legend so the test can assert the patterned series' legend
        // symbol also receives the pattern fill.
        Legend legend = new Legend();
        legend.setEnabled(true);
        configuration.setLegend(legend);

        // Series 0: patterned via series-level color, with one point overriding
        // the series pattern with a different PatternColor.
        DataSeries patterned = new DataSeries("Patterned");
        patterned.add(new DataSeriesItem("A", 5));
        DataSeriesItem overridePoint = new DataSeriesItem("B", 4);
        overridePoint.setColor(PatternColor.createPath("M 0 10 L 10 0",
                new SolidColor("#0000ff"), 10, 10));
        patterned.add(overridePoint);
        patterned.add(new DataSeriesItem("C", 6));

        PlotOptionsColumn patternedOptions = new PlotOptionsColumn();
        patternedOptions.setColor(PatternColor.createPath("M 0 0 L 10 10",
                new SolidColor("#ff0000"), 10, 10));
        patterned.setPlotOptions(patternedOptions);
        configuration.addSeries(patterned);

        // Series 1: plain solid color, used as the control series.
        ListSeries control = new ListSeries("Control", 3, 5, 4);
        PlotOptionsColumn controlOptions = new PlotOptionsColumn();
        controlOptions.setColor(new SolidColor("#00aa00"));
        control.setPlotOptions(controlOptions);
        configuration.addSeries(control);

        return chart;
    }
}
