package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.TickmarkPlacement;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.Legend;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.LayoutDirection;
import com.vaadin.flow.component.charts.model.ListSeries;

public class Spiderweb extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart();

        Configuration configuration = chart.getConfiguration();
        configuration.getChart().setPolar(true);

        configuration.setTitle("Budget vs spending");

        XAxis xAxis = configuration.getxAxis();
        xAxis.setCategories("Sales", "Marketing", "Development",
                "Customer Support", "Information Technology", "Administration");
        xAxis.setTickmarkPlacement(TickmarkPlacement.ON);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setGridLineInterpolation("polygon");
        yAxis.setMin(0);

        Tooltip tooltip = configuration.getTooltip();
        tooltip.setShared(true);
        tooltip.setPointFormat(
                "<span style=\"color:{series.color}\">{series.name}: <b>${point.y:,.0f}</b><br/>");

        Legend legend = configuration.getLegend();
        legend.setAlign(HorizontalAlign.RIGHT);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setY(70);

        ListSeries allocatedBudget = new ListSeries("Allocated Budget", 43000,
                19000, 60000, 35000, 17000, 10000);
        configuration.addSeries(allocatedBudget);

        ListSeries actualSpending = new ListSeries("Actual Spending", 50000,
                39000, 42000, 31000, 26000, 14000);
        configuration.addSeries(actualSpending);

        add(chart);
    }
}
