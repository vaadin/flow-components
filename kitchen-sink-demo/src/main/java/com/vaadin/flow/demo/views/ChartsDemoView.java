/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.demo.views;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for Charts component.
 */
@Route(value = "charts", layout = MainLayout.class)
@PageTitle("Charts | Vaadin Kitchen Sink")
public class ChartsDemoView extends VerticalLayout {

    public ChartsDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Charts Component"));
        add(new Paragraph("Charts provides powerful data visualization capabilities."));

        // Line chart
        Chart lineChart = new Chart(ChartType.LINE);
        Configuration lineConfig = lineChart.getConfiguration();
        lineConfig.setTitle("Monthly Sales");
        lineConfig.getxAxis().setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun");
        lineConfig.addSeries(new ListSeries("2024", 150, 180, 210, 195, 230, 260));
        lineConfig.addSeries(new ListSeries("2023", 120, 140, 170, 160, 190, 210));
        lineChart.setHeight("300px");
        lineChart.setWidthFull();
        addSection("Line Chart", lineChart);

        // Bar chart
        Chart barChart = new Chart(ChartType.BAR);
        Configuration barConfig = barChart.getConfiguration();
        barConfig.setTitle("Quarterly Revenue");
        barConfig.getxAxis().setCategories("Q1", "Q2", "Q3", "Q4");
        barConfig.addSeries(new ListSeries("Revenue", 42000, 58000, 49000, 67000));
        barChart.setHeight("300px");
        barChart.setWidthFull();
        addSection("Bar Chart", barChart);

        // Column chart
        Chart columnChart = new Chart(ChartType.COLUMN);
        Configuration columnConfig = columnChart.getConfiguration();
        columnConfig.setTitle("Product Comparison");
        columnConfig.getxAxis().setCategories("Product A", "Product B", "Product C");
        columnConfig.addSeries(new ListSeries("Sales", 340, 280, 420));
        columnConfig.addSeries(new ListSeries("Revenue", 1200, 980, 1450));
        columnChart.setHeight("300px");
        columnChart.setWidthFull();
        addSection("Column Chart", columnChart);

        // Pie chart
        Chart pieChart = new Chart(ChartType.PIE);
        Configuration pieConfig = pieChart.getConfiguration();
        pieConfig.setTitle("Market Share");
        DataSeries pieSeries = new DataSeries();
        pieSeries.add(new DataSeriesItem("Chrome", 65));
        pieSeries.add(new DataSeriesItem("Firefox", 12));
        pieSeries.add(new DataSeriesItem("Safari", 10));
        pieSeries.add(new DataSeriesItem("Edge", 8));
        pieSeries.add(new DataSeriesItem("Other", 5));
        pieConfig.addSeries(pieSeries);
        pieChart.setHeight("350px");
        pieChart.setWidthFull();
        addSection("Pie Chart", pieChart);

        // Area chart
        Chart areaChart = new Chart(ChartType.AREA);
        Configuration areaConfig = areaChart.getConfiguration();
        areaConfig.setTitle("Website Traffic");
        areaConfig.getxAxis().setCategories("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
        areaConfig.addSeries(new ListSeries("Visitors", 1200, 1450, 1380, 1520, 1680, 890, 720));
        areaChart.setHeight("300px");
        areaChart.setWidthFull();
        addSection("Area Chart", areaChart);

        // Stacked column chart
        Chart stackedChart = new Chart(ChartType.COLUMN);
        Configuration stackedConfig = stackedChart.getConfiguration();
        stackedConfig.setTitle("Sales by Region");
        stackedConfig.getxAxis().setCategories("Q1", "Q2", "Q3", "Q4");
        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        plotOptions.setStacking(Stacking.NORMAL);
        stackedConfig.setPlotOptions(plotOptions);
        stackedConfig.addSeries(new ListSeries("North", 15, 18, 21, 24));
        stackedConfig.addSeries(new ListSeries("South", 12, 14, 16, 19));
        stackedConfig.addSeries(new ListSeries("East", 8, 11, 13, 15));
        stackedConfig.addSeries(new ListSeries("West", 10, 12, 14, 17));
        stackedChart.setHeight("300px");
        stackedChart.setWidthFull();
        addSection("Stacked Column Chart", stackedChart);

        // Donut chart
        Chart donutChart = new Chart(ChartType.PIE);
        Configuration donutConfig = donutChart.getConfiguration();
        donutConfig.setTitle("Budget Allocation");
        PlotOptionsPie donutOptions = new PlotOptionsPie();
        donutOptions.setInnerSize("50%");
        donutConfig.setPlotOptions(donutOptions);
        DataSeries donutSeries = new DataSeries();
        donutSeries.add(new DataSeriesItem("Development", 35));
        donutSeries.add(new DataSeriesItem("Marketing", 25));
        donutSeries.add(new DataSeriesItem("Operations", 20));
        donutSeries.add(new DataSeriesItem("HR", 12));
        donutSeries.add(new DataSeriesItem("Other", 8));
        donutConfig.addSeries(donutSeries);
        donutChart.setHeight("350px");
        donutChart.setWidthFull();
        addSection("Donut Chart", donutChart);
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }
}
