package com.vaadin.flow.component.charts.examples.other;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItemXrange;
import com.vaadin.flow.component.charts.model.PlotOptionsXrange;
import com.vaadin.flow.component.charts.model.style.SolidColor;

public class Xrange extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.XRANGE);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("X-range");
        conf.getxAxis().setType(AxisType.DATETIME);
        conf.getyAxis().setTitle("");
        conf.getyAxis().setCategories("Prototyping", "Development", "Testing");
        conf.getyAxis().setReversed(true);

        DataSeries series = new DataSeries();
        series.setName("Project 1");

        series.add(new DataSeriesItemXrange(getInstant(2014, 11, 21),
                getInstant(2014, 12, 2), 0, 0.25));
        series.add(new DataSeriesItemXrange(getInstant(2014, 12, 2),
                getInstant(2014, 12, 5), 1));
        series.add(new DataSeriesItemXrange(getInstant(2014, 12, 8),
                getInstant(2014, 12, 9), 2));
        series.add(new DataSeriesItemXrange(getInstant(2014, 12, 9),
                getInstant(2014, 12, 19), 1));
        series.add(new DataSeriesItemXrange(getInstant(2014, 12, 10),
                getInstant(2014, 12, 23), 2));

        PlotOptionsXrange options = new PlotOptionsXrange();
        options.setBorderColor(SolidColor.GRAY);
        options.setPointWidth(20);
        options.getDataLabels().setEnabled(true);
        series.setPlotOptions(options);
        conf.addSeries(series);
        add(chart);
    }

    private Instant getInstant(int year, int month, int dayOfMonth) {
        return LocalDate.of(year, month, dayOfMonth).atStartOfDay()
                .toInstant(ZoneOffset.UTC);
    }

}
