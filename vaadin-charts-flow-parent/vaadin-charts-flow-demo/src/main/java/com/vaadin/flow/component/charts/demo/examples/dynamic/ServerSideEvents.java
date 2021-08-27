package com.vaadin.flow.component.charts.demo.examples.dynamic;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.demo.AbstractChartExample;
import com.vaadin.flow.component.charts.demo.SkipFromDemo;
import com.vaadin.flow.component.charts.model.AbstractSeries;
import com.vaadin.flow.component.charts.model.AxisTitle;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.Dimension;
import com.vaadin.flow.component.charts.model.PlotLine;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;

@SkipFromDemo
public class ServerSideEvents extends AbstractChartExample {

    private Chart chart;
    private Span lastEvent;
    private Span eventDetails;
    private int id;
    private VerticalLayout historyLayout;
    private int eventNumber;
    private DataSeriesItem firstDataPoint;
    private Checkbox visibilityToggling;
    private boolean setExtremes = true;
    private boolean hideSeries = true;

    @Override
    public void initDemo() {
        eventDetails = new Span();
        eventDetails.setId("eventDetails");

        lastEvent = new Span();
        lastEvent.setId("lastEvent");

        historyLayout = new VerticalLayout();
        historyLayout.setId("history");

        chart = new Chart();
        chart.setId("chart");

        final Configuration configuration = chart.getConfiguration();
        configuration.getChart().setWidth(500);
        configuration.getChart().setType(ChartType.SCATTER);
        configuration.getTitle().setText("Test server side events.");
        configuration.getSubTitle().setText(
                "When an event occurs, the details are shown below the chart");
        configuration.setExporting(true);
        configuration.getChart().setAnimation(false);
        configuration.getChart().setZoomType(Dimension.XY);

        configuration.getAccessibility().setEnabled(false);

        XAxis xAxis = configuration.getxAxis();
        xAxis.setMinPadding(0.2);
        xAxis.setMaxPadding(0.2);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle(new AxisTitle("Value"));
        PlotLine plotline = new PlotLine();
        plotline.setValue(0);
        plotline.setWidth(1);
        plotline.setColor(new SolidColor("#808080"));
        yAxis.setPlotLines(plotline);
        yAxis.setMinPadding(0.2);
        yAxis.setMaxPadding(0.2);

        YAxis yAxis1 = new YAxis();
        yAxis1.setTitle("Another axis");
        yAxis1.setOpposite(true);
        configuration.addyAxis(yAxis1);

        PlotOptionsSeries opt = new PlotOptionsSeries();
        opt.setShowCheckbox(true);
        opt.setAllowPointSelect(true);

        configuration.setPlotOptions(opt);
        configuration.setTooltip(new Tooltip(false));
        final DataSeries series1 = createDataSeries(0);
        final DataSeries series2 = createDataSeries(20);
        DataSeries series3 = createDataSeries(100);
        series3.get(0).setY(105);
        series3.get(3).setY(95);
        series3.setName("Another axis");
        series3.setyAxis(1);
        firstDataPoint = series1.get(0);
        firstDataPoint.setSelected(true);
        configuration.setSeries(series1, series2, series3);

        chart.addChartClickListener(event -> logEvent(event));
        chart.addPointClickListener(event -> logEvent(event));
        chart.addCheckBoxClickListener(event -> logEvent(event));
        chart.addSeriesLegendItemClickListener(event -> logEvent(event));
        chart.addSeriesHideListener(event -> logEvent(event));
        chart.addSeriesShowListener(event -> logEvent(event));
        chart.addPointSelectListener(event -> logEvent(event));
        chart.addPointUnselectListener(event -> logEvent(event));
        chart.addYAxesExtremesSetListener(event -> logEvent(event));
        chart.drawChart();

        chart.setVisibilityTogglingDisabled(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setId("master");
        layout.add(createControls());

        layout.add(lastEvent);
        layout.add(eventDetails);
        layout.add(historyLayout);
        add(chart, layout);
    }

    private Component createControls() {
        visibilityToggling = new Checkbox("Disable series visibility toggling");
        visibilityToggling.setId("visibilityToggler");
        visibilityToggling.addValueChangeListener(e -> chart
                .setVisibilityTogglingDisabled(visibilityToggling.getValue()));
        visibilityToggling.setValue(false);

        final Button firstSeriesVisible = new Button("Hide first series", e -> {
            Series firstSeries = chart.getConfiguration().getSeries().get(0);
            ((AbstractSeries) firstSeries).setVisible(!hideSeries);
            hideSeries = !hideSeries;
        });
        firstSeriesVisible.setId("hideFirstSeries");

        final RadioButtonGroup<Dimension> zoomLevels = new RadioButtonGroup<>();
        zoomLevels.setItems(Dimension.XY, Dimension.X, Dimension.Y);
        zoomLevels.setValue(Dimension.XY);
        zoomLevels.addValueChangeListener(event -> {
            chart.getConfiguration().getChart()
                    .setZoomType(zoomLevels.getValue());
            chart.drawChart();
        });

        Button resetHistory = new Button("Reset history");
        resetHistory.setId("resetHistory");
        resetHistory.addClickListener(event -> {
            lastEvent.setText(null);
            eventDetails.setText(null);
            historyLayout.removeAll();
            eventNumber = 0;
        });

        Button toggleExtremes = new Button("Toggle Extremes");
        toggleExtremes.setId("toggleExtremes");
        toggleExtremes.addClickListener(e -> {
            if (setExtremes) {
                chart.getConfiguration().getyAxes().getAxis(0).setExtremes(9,
                        15);
            } else {
                chart.getConfiguration().resetZoom();
            }
            setExtremes = !setExtremes;
        });

        HorizontalLayout controls = new HorizontalLayout();
        controls.setId("controls");
        controls.add(visibilityToggling);
        controls.add(firstSeriesVisible);
        controls.add(zoomLevels);
        controls.add(resetHistory);
        controls.add(toggleExtremes);
        return controls;
    }

    private DataSeries createDataSeries(Number yvalue) {
        final DataSeries series = new DataSeries();
        series.add(new DataSeriesItem(20, yvalue));
        series.add(new DataSeriesItem(40, yvalue.intValue() + 10));
        series.add(new DataSeriesItem(60, yvalue.intValue() - 15));
        series.add(new DataSeriesItem(80, yvalue));
        series.setId("" + id);
        series.setName("Test Series " + id++);
        return series;
    }

    private void logEvent(ComponentEvent<Chart> event) {
        String name = event.getClass().getSimpleName();
        String details = createEventString(event);
        lastEvent.setText(name);
        eventDetails.setText(details);
        Span history = new Span(name + ": " + details + "\n");
        history.setId("event" + eventNumber++);
        historyLayout.addComponentAtIndex(0, history);
    }
}
