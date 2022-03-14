package com.vaadin.flow.component.charts.examples.column;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Credits;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.Title;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;

public class ColumnWithNegativeValues extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.COLUMN);

        Configuration conf = chart.getConfiguration();
        conf.setTitle(new Title("Column chart with negative values"));

        PlotOptionsColumn column = new PlotOptionsColumn();
        column.setMinPointLength(3);
        conf.setPlotOptions(column);

        XAxis xAxis = new XAxis();
        xAxis.setCategories("Apples", "Oranges", "Pears", "Grapes", "Bananas");
        conf.addxAxis(xAxis);

        Tooltip tooltip = new Tooltip();
        tooltip.setFormatter(
                "function() { return ''+ this.series.name +': '+ this.y +'';}");
        conf.setTooltip(tooltip);

        conf.setCredits(new Credits(false));

        conf.addSeries(new ListSeries("John", 5, 0.1, 4, 7, 2));
        conf.addSeries(new ListSeries("Jane", 2, -2, -0.1, 2, 1));
        conf.addSeries(new ListSeries("Joe", 3, 4, 4, -2, 5));

        add(chart);
    }

}
