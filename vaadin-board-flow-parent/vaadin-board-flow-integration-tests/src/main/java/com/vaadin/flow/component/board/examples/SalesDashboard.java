package com.vaadin.flow.component.board.examples;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.Labels;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Marker;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsFunnel;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.PointPlacement;
import com.vaadin.flow.component.charts.model.Stacking;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("SalesDashboard")
public class SalesDashboard extends Div {

    private final String YELLOW = "#F9DD51";
    private final String GREEN = "#98DF58";
    private final String BLUE = "#3090F0";
    private final String GRAY = "#808080";
    private final String LIGHT_GRAY = "#CECECE";
    private final String MAGENTA = "#EC64A5";
    private final String CYAN = "#24DCD4";
    private final String PURPLE = "#685CB0";

    public SalesDashboard() {
        Board board = new Board();
        board.addClassName("sales-dashboard-demo-area");

        // First row
        board.addRow(
                createColumnChart("Total Revenue / 1 k$", BLUE, 63, 51, 70, 83,
                        87, 37),
                createColumnChart("Billed / 1k$", GREEN, 63, 68, 67, 65, 83,
                        42),
                createColumnChart("Outstanding / 1k$", GREEN, 5, 78, 7, 1, 54,
                        37),
                createColumnChart("Refunded / 1k$", GREEN, 13, 9, 51, 62, 8,
                        8));

        // Second row
        Row lineChartsInnerRow = new Row();
        lineChartsInnerRow.add(
                createLineChart("Customers", "↑501", BLUE, 29.9, 71.5, 106.4,
                        80.2, 83.0, 95.0, 92.6, 112.5, 146.4, 183.1, 201.6,
                        220.4),
                createLineChart("ROI", "↑75%", BLUE, 29.9, 25.5, 20.4, 24.2,
                        29.0, 31.0, 28.6, 27.5, 32.4, 33.1, 35.6, 35.4),
                createLineChart("Churn", "↓32", MAGENTA, 29.9, 27.5, 32.4, 28.2,
                        26.0, 27.0, 27.6, 25.5, 24.4, 23.1, 22.6, 21.4));

        Div midColumnChart = createMidColumnChart();
        midColumnChart.addClassName("mid-line-main-chart");

        Row secondLine = board.addRow(midColumnChart, lineChartsInnerRow);
        secondLine.addClassName("mid-line-charts-row");

        secondLine.setComponentSpan(midColumnChart, 3);

        // Third row
        board.addRow(createFunnelChart(), createPieChart());

        add(board);
    }

    private Component createColumnChart(String title, String color,
            Number... values) {
        Div container = new Div();

        Chart chart = new Chart();
        Configuration configuration = chart.getConfiguration();

        configuration.getChart().setType(ChartType.COLUMN);

        DataSeries dataSeries = new DataSeries("Short");
        dataSeries.setData(values);
        configuration.setSeries(dataSeries);

        PlotOptionsColumn plotOptionsColumn = new PlotOptionsColumn();
        // plotOptionsColumn.setColor(new SolidColor(color));
        plotOptionsColumn.setShowInLegend(true);
        plotOptionsColumn.setGroupPadding(0);
        plotOptionsColumn.getDataLabels().setEnabled(true);
        plotOptionsColumn.setAnimation(false);
        configuration.setPlotOptions(plotOptionsColumn);

        configuration.setTitle(title);

        // Style titleStyle = configuration.getTitle().getStyle();
        // titleStyle.setColor(SolidColor.GRAY);
        // titleStyle.setFontSize("14");

        configuration.getLegend().setEnabled(false);

        YAxis yAxis = configuration.getyAxis();
        yAxis.getLabels().setEnabled(false);
        yAxis.setGridZIndex(4);
        yAxis.setTitle("");

        Labels xLabels = configuration.getxAxis().getLabels();
        xLabels.setEnabled(false);

        Labels yLabels = configuration.getyAxis().getLabels();
        yLabels.setEnabled(false);

        chart.drawChart();

        chart.getElement().getStyle().set("height", "300px");

        container.add(chart);

        return container;
    }

    private Div createMidColumnChart() {
        Div container = new Div();

        Chart chart = new Chart();

        Configuration configuration = chart.getConfiguration();
        configuration.getChart().setType(ChartType.COLUMN);

        PlotOptionsColumn chartOptions = new PlotOptionsColumn();
        chartOptions.setStacking(Stacking.NORMAL);
        configuration.setPlotOptions(chartOptions);

        configuration.getxAxis().setCategories("Apples", "Oranges", "Pears",
                "Grapes", "Bananas");

        configuration.setTitle("Q1 Product Sales");

        ListSeries johnSeries = new ListSeries("John", 510000, 300000, 520000,
                840000, 610000);
        PlotOptionsColumn johnSeriesOptions = new PlotOptionsColumn();
        // johnSeriesOptions.setColor(new SolidColor(YELLOW));
        johnSeriesOptions.setAnimation(false);
        johnSeries.setPlotOptions(johnSeriesOptions);

        ListSeries janeSeries = new ListSeries("Jane", 510000, 300000, 520000,
                840000, 610000);
        PlotOptionsColumn janeSeriesOptions = new PlotOptionsColumn();
        // janeSeriesOptions.setColor(new SolidColor(GREEN));
        janeSeriesOptions.setAnimation(false);
        janeSeries.setPlotOptions(janeSeriesOptions);

        ListSeries joeSeries = new ListSeries("Joe", 980000, 540000, 430000,
                650000, 610000);
        PlotOptionsColumn joeSeriesOptions = new PlotOptionsColumn();
        // joeSeriesOptions.setColor(new SolidColor(BLUE));
        joeSeriesOptions.setAnimation(false);
        joeSeries.setPlotOptions(joeSeriesOptions);

        ListSeries budgetSeries = new ListSeries("Budget", 2200000, 1300000,
                1700000, 1200000, 1700000);
        PlotOptionsColumn budgetSeriesOptions = new PlotOptionsColumn();
        // budgetSeriesOptions.setColor(new SolidColor(LIGHT_GRAY));
        budgetSeriesOptions.setPointPadding(0.3f);
        budgetSeriesOptions.setPointPlacement(PointPlacement.ON);
        budgetSeriesOptions.setPointStart(0.45f);
        budgetSeriesOptions.setAnimation(false);

        budgetSeries.setStack("budget");
        budgetSeries.setPlotOptions(budgetSeriesOptions);

        configuration.setSeries(budgetSeries, johnSeries, janeSeries,
                joeSeries);

        YAxis yAxis = configuration.getyAxis();
        yAxis.getLabels().setEnabled(true);
        yAxis.setGridZIndex(4);
        yAxis.setTitle("");

        chart.drawChart();

        chart.getElement().getStyle().set("height", "500px");

        container.add(chart);

        return container;
    }

    private Component createLineChart(String title, String overallValue,
            String color, Number... values) {
        Div container = new Div();
        container.addClassName("mid-line-chart-container");

        Chart chart = new Chart();
        chart.getElement().getClassList().add("mid-line-chart");

        Configuration configuration = chart.getConfiguration();

        configuration.getChart().setType(ChartType.LINE);

        configuration.setTitle(title);
        configuration.setSubTitle(overallValue);

        // Style titleStyle = configuration.getTitle().getStyle();
        // titleStyle.setFontSize("14");
        // titleStyle.setColor(new SolidColor(GRAY));
        //
        // Style subtitleStyle = configuration.getSubTitle().getStyle();
        // subtitleStyle.setFontSize("24");
        // subtitleStyle.setColor(new SolidColor(BLUE));

        PlotOptionsLine plotOptionsLine = new PlotOptionsLine();
        // plotOptionsLine.setColor(new SolidColor(color));
        plotOptionsLine.setAnimation(false);

        Marker marker = new Marker();
        marker.setEnabled(false);
        plotOptionsLine.setMarker(marker);

        configuration.setPlotOptions(plotOptionsLine);

        DataSeries dataSeries = new DataSeries("Short");
        dataSeries.setData(values);

        configuration.setSeries(dataSeries);
        configuration.getxAxis().getLabels().setEnabled(false);

        configuration.getyAxis().getLabels().setEnabled(false);
        configuration.getyAxis().setTitle("");
        // configuration.getyAxis().setGridLineWidth(0);
        // configuration.getyAxis().setMinorTickWidth(0);

        configuration.getLegend().setEnabled(false);

        container.add(chart);

        chart.drawChart();

        chart.getElement().getStyle().set("height", "166.66px");

        return container;
    }

    private Component createFunnelChart() {
        Div container = new Div();

        Chart chart = new Chart();

        Configuration configuration = chart.getConfiguration();
        configuration.getChart().setType(ChartType.FUNNEL);

        configuration.getLegend().setEnabled(false);

        PlotOptionsFunnel plotOptionsFunnel = new PlotOptionsFunnel();
        plotOptionsFunnel.setColors(new SolidColor(BLUE), new SolidColor(GREEN),
                new SolidColor(YELLOW), new SolidColor(CYAN),
                new SolidColor(MAGENTA), new SolidColor(PURPLE));
        plotOptionsFunnel.setNeckWidth("5%");
        plotOptionsFunnel.setNeckHeight("0%");
        plotOptionsFunnel.setWidth("40%");
        plotOptionsFunnel.getDataLabels().setFormat("{point.name}: {point.x}");
        plotOptionsFunnel.setAnimationLimit(0);
        configuration.setPlotOptions(plotOptionsFunnel);

        DataSeries dataSeries = new DataSeries("Unique users");

        DataSeriesItem dataLead = new DataSeriesItem("Leads", 1);
        dataLead.setX(2543);
        dataSeries.add(dataLead);

        DataSeriesItem dataMarketing = new DataSeriesItem(
                "Marketing qualified lead", 1);
        dataMarketing.setX(1264);
        dataSeries.add(dataMarketing);

        DataSeriesItem dataSales = new DataSeriesItem("Sales qualified lead",
                1);
        dataSales.setX(305);
        dataSeries.add(dataSales);

        DataSeriesItem dataCustomer = new DataSeriesItem("Customer engagement",
                1);
        dataCustomer.setX(141);
        dataSeries.add(dataCustomer);

        DataSeriesItem dataOpportunity = new DataSeriesItem("Opportunity", 1);
        dataOpportunity.setX(65);
        dataSeries.add(dataOpportunity);

        DataSeriesItem dataClosed = new DataSeriesItem("Closed / Won", 1);
        dataClosed.setX(34);
        dataSeries.add(dataClosed);

        configuration.setSeries(dataSeries);

        configuration.setTitle("Sales & Marketing pipeline");
        // Style titleStyle = configuration.getTitle().getStyle();
        // titleStyle.setFontSize("14");
        // titleStyle.setColor(new SolidColor(GRAY));

        chart.drawChart();

        container.add(chart);

        return container;
    }

    private Component createPieChart() {
        Div container = new Div();

        Chart chart = new Chart();

        Configuration configuration = chart.getConfiguration();
        configuration.getChart().setType(ChartType.PIE);

        PlotOptionsPie plotOptionsPie = new PlotOptionsPie();
        plotOptionsPie.getDataLabels().setFormat("{point.name}: {point.y}");
        plotOptionsPie.setAnimation(false);
        configuration.setPlotOptions(plotOptionsPie);

        configuration.setTitle("Working Today");

        DataSeries dataSeries = new DataSeries();
        dataSeries.add(new DataSeriesItem("Sales", 3));
        dataSeries.add(new DataSeriesItem("Marketing", 5));
        dataSeries.add(new DataSeriesItem("R&D", 9));
        dataSeries.add(new DataSeriesItem("Consulting", 8));
        configuration.setSeries(dataSeries);

        chart.drawChart();

        container.add(chart);

        return container;
    }
}
