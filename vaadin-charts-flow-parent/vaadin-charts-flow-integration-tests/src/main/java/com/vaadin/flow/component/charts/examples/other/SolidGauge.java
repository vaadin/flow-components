package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.Background;
import com.vaadin.flow.component.charts.model.BackgroundShape;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.Pane;
import com.vaadin.flow.component.charts.model.PlotOptionsSolidgauge;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.dependency.CssImport;

@CssImport(value = "./styles/SolidGauge.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
public class SolidGauge extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.SOLIDGAUGE);

        Configuration configuration = chart.getConfiguration();

        Pane pane = configuration.getPane();
        pane.setCenter(new String[] { "50%", "50%" });
        pane.setStartAngle(-90);
        pane.setEndAngle(90);

        Background paneBackground = new Background();
        paneBackground.setInnerRadius("60%");
        paneBackground.setOuterRadius("100%");
        paneBackground.setShape(BackgroundShape.ARC);
        pane.setBackground(paneBackground);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTickAmount(2);
        yAxis.setTitle("Speed");
        yAxis.setMinorTickInterval("null");
        yAxis.getTitle().setY(-50);
        yAxis.getLabels().setY(16);
        yAxis.setMin(0);
        yAxis.setMax(200);

        PlotOptionsSolidgauge plotOptionsSolidgauge = new PlotOptionsSolidgauge();

        DataLabels dataLabels = plotOptionsSolidgauge.getDataLabels();
        dataLabels.setY(5);
        dataLabels.setUseHTML(true);

        configuration.setPlotOptions(plotOptionsSolidgauge);

        DataSeries series = new DataSeries("Speed");

        DataSeriesItem item = new DataSeriesItem();
        item.setY(80);
        // item.setColorIndex(2);
        item.setClassName("myClassName");
        DataLabels dataLabelsSeries = new DataLabels();
        dataLabelsSeries.setFormat(
                "<div style=\"text-align:center\"><span style=\"font-size:25px;"
                        + "color:black' + '\">{y}</span><br/>"
                        + "<span style=\"font-size:12px;color:silver\">km/h</span></div>");

        item.setDataLabels(dataLabelsSeries);

        series.add(item);

        configuration.addSeries(series);

        add(chart);
    }

}
