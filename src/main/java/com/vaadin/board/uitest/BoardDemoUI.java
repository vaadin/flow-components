package com.vaadin.board.uitest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataProviderSeries;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.board.Board;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

@Theme("mytheme")
public class BoardDemoUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        Board board = new Board();
        board.setWidth("100%");
        board.addRow(createLabel("Today", "3/7"), createLabel("N/A", "1"),
                createLabel("New", "2"), createLabel("Tomorrow", "4"));

        board.addRow(
                createChart("Monthly sales", SalesData.generateMonthData()),
                createChart("Yearly sales", SalesData.generateYearData()));
        board.addRow(createChart("Last years", SalesData.generateYearData(),
                SalesData.generateYearData(), SalesData.generateYearData()));
        board.addRow(createPieChart(), createGrid());
        setContent(board);
    }

    private Component createPieChart() {
        Chart chart = new Chart(ChartType.PIE);
        ListSeries series = new ListSeries(8, 2);
        chart.getConfiguration().addSeries(series);
        return chart;
    }

    private Component createGrid() {
        Grid<OrderInfo> grid = new Grid<>();
        List<OrderInfo> infos = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            infos.add(OrderInfo.create());
        }
        grid.addColumn(OrderInfo::getDue);
        grid.addColumn(OrderInfo::getDescription);
        grid.addColumn(OrderInfo::getStatus);
        grid.removeHeaderRow(0);

        grid.setItems(infos);
        return grid;
    }

    private Component createChart(String title, List<SalesData>... sales) {

        Chart chart = new Chart();
        chart.addStyleName("v-clip");

        Configuration conf = chart.getConfiguration();
        conf.setTitle(title);
        conf.getChart().setType(ChartType.LINE);
        conf.getLegend().setEnabled(false);

        for (List<SalesData> salesData : sales) {
            ListDataProvider<SalesData> salesDataProvider = new ListDataProvider<>(
                    salesData);
            DataProviderSeries<SalesData> series = new DataProviderSeries<>(
                    salesDataProvider, SalesData::getValue);
            series.setX(SalesData::getTime);
            conf.addSeries(series);
        }

        return chart;
    }

    private Label createLabel(String header, String content) {
        Label l = new Label();
        l.addStyleName("center border");
        l.setContentMode(ContentMode.HTML);
        l.setWidth("100%");
        l.setValue("<h1>" + header + "</h1><p><h2>" + content + "</h2>");
        return l;
    }

    @WebServlet(urlPatterns = { "/basic/*",
            "/VAADIN/*" }, name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = BoardDemoUI.class, productionMode = false, widgetset = "com.vaadin.board.BoardWidgetSet")
    public static class BoardUIServlet extends VaadinServlet {
    }
}
