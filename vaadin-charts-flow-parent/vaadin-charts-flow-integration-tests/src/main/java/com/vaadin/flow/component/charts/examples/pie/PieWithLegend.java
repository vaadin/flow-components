package com.vaadin.flow.component.charts.examples.pie;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.ChartVariant;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Cursor;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.shared.Registration;

public class PieWithLegend extends AbstractChartExample {

    protected Chart chart;
    protected Registration listenerRegistration;

    @Override
    public void initDemo() {
        chart = new Chart(ChartType.PIE);
        chart.addThemeVariants(ChartVariant.LUMO_GRADIENT);
        chart.getConfiguration().getChart().setStyledMode(true);

        Configuration conf = chart.getConfiguration();

        conf.setTitle("Browser market shares in January, 2018");

        Tooltip tooltip = new Tooltip();
        tooltip.setValueDecimals(1);
        conf.setTooltip(tooltip);

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);
        conf.setPlotOptions(plotOptions);

        DataSeries series = new DataSeries();
        DataSeriesItem chrome = new DataSeriesItem("Chrome", 61.41);
        chrome.setSliced(true);
        chrome.setSelected(true);
        series.add(chrome);
        series.add(new DataSeriesItem("Internet Explorer", 11.84));
        series.add(new DataSeriesItem("Firefox", 10.85));
        series.add(new DataSeriesItem("Edge", 4.67));
        series.add(new DataSeriesItem("Safari", 4.18));
        series.add(new DataSeriesItem("Sogou Explorer", 1.64));
        series.add(new DataSeriesItem("Opera", 6.2));
        series.add(new DataSeriesItem("QQ", 1.2));
        series.add(new DataSeriesItem("Others", 2.61));
        conf.setSeries(series);
        chart.setVisibilityTogglingDisabled(true);

        listenerRegistration = chart.addPointLegendItemClickListener(event -> {
            showNotification("Legend item click" + " : " + event.getItemIndex()
                    + " : " + event.getItem().getName());
        });

        add(chart);
    }

}
