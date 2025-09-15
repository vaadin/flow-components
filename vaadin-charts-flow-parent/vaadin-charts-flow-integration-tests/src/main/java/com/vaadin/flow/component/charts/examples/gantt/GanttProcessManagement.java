/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.gantt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.ChartConnectors;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Completed;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DashStyle;
import com.vaadin.flow.component.charts.model.GanttSeries;
import com.vaadin.flow.component.charts.model.GanttSeriesItem;
import com.vaadin.flow.component.charts.model.GanttSeriesItemDependency;
import com.vaadin.flow.component.charts.model.PlotLine;
import com.vaadin.flow.component.charts.model.PlotOptionsGantt;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.style.SolidColor;

@SuppressWarnings("unused")
public class GanttProcessManagement extends AbstractChartExample {
    private static final Instant TODAY = Instant.now()
            .truncatedTo(ChronoUnit.DAYS);

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.GANTT);

        final Configuration configuration = chart.getConfiguration();
        configuration.setTitle("Gantt Project Management");
        ChartConnectors connectors = new ChartConnectors();
        connectors.setDashStyle(DashStyle.SHORTDASH);
        configuration.setConnectors(connectors);

        Tooltip tooltip = new Tooltip();
        tooltip.setPointFormat(
                "<span><b>{point.name}</b></span><br/><span>Start: {point.start:%e. %b}</span><br/><span>End: {point.end:%e. %b}</span><br/><span>Owner: {point.custom.owner}</span>");
        configuration.setTooltip(tooltip);

        XAxis xAxis = configuration.getxAxis();
        PlotLine dateIndicator = new PlotLine();
        dateIndicator.setDashStyle(DashStyle.DASH);
        xAxis.setCurrentDateIndicator(dateIndicator);
        xAxis.setMin(todayPlus(-3));
        xAxis.setMax(todayPlus(18));

        PlotOptionsGantt plotOptionsGantt = new PlotOptionsGantt();
        configuration.setPlotOptions(plotOptionsGantt);

        configuration.addSeries(createOfficesSeries());
        configuration.addSeries(createNewProductLaunchSeries());

        chart.addPointClickListener(event -> {
            var name = ((GanttSeries) event.getSeries())
                    .get(event.getItemIndex()).getName();
            System.out.println("Clicked on " + name);
        });

        add(chart);
    }

    private GanttSeries createNewProductLaunchSeries() {
        GanttSeries series = new GanttSeries();
        series.setName("Product");

        GanttSeriesItem item;

        item = new GanttSeriesItem();
        item.setName("New product launch");
        item.setId("new_product");
        item.setCustom(new TaskCustomData("Peter"));
        series.add(item);

        item = new GanttSeriesItem("Development", todayPlus(-1), todayPlus(11));
        item.setId("development");
        item.setCompleted(new Completed(0.6, SolidColor.ORANGE));
        item.setCustom(new TaskCustomData("Susan"));
        item.setParent("new_product");
        series.add(item);

        item = new GanttSeriesItem();
        item.setName("Beta");
        item.setStart(todayPlus(12));
        item.setId("beta");
        item.addDependency(new GanttSeriesItemDependency("development"));
        item.setMilestone(true);
        item.setCustom(new TaskCustomData("Peter"));
        item.setParent("new_product");
        series.add(item);

        item = new GanttSeriesItem("Final development", todayPlus(13),
                todayPlus(17));
        item.setId("finalize");
        item.addDependency(new GanttSeriesItemDependency("beta"));
        item.setParent("new_product");
        series.add(item);

        item = new GanttSeriesItem();
        item.setName("Launch");
        item.setStart(todayPlus(17).plus(12, ChronoUnit.HOURS));
        item.addDependency(new GanttSeriesItemDependency("finalize"));
        item.setMilestone(true);
        item.setCustom(new TaskCustomData("Peter"));
        item.setParent("new_product");
        series.add(item);

        return series;
    }

    private GanttSeries createOfficesSeries() {
        GanttSeries series = new GanttSeries();
        series.setName("Offices");

        GanttSeriesItem item;

        item = new GanttSeriesItem();
        item.setName("New offices");
        item.setId("new_offices");
        item.setCustom(new TaskCustomData("Peter"));
        series.add(item);

        item = new GanttSeriesItem("Prepare office building", todayPlus(-2),
                todayPlus(6));
        item.setId("prepare_building");
        item.setCustom(new TaskCustomData("Linda"));
        item.setCompleted(0.2);
        item.setParent("new_offices");
        series.add(item);

        item = new GanttSeriesItem("Inspect building", todayPlus(4),
                todayPlus(8));
        item.setId("inspect_building");
        item.setParent("new_offices");
        item.setCustom(new TaskCustomData("Ivy"));
        series.add(item);

        item = new GanttSeriesItem("Passed inspection", todayPlus(9),
                todayPlus(9));
        item.setId("passed_inspection");
        item.addDependency("prepare_building");
        item.addDependency("inspect_building");
        item.setParent("new_offices");
        item.setMilestone(true);
        item.setCustom(new TaskCustomData("Peter"));
        series.add(item);

        item = new GanttSeriesItem();
        item.setName("Relocate");
        item.setId("relocate");
        item.addDependency(new GanttSeriesItemDependency("passed_inspection"));
        item.setParent("new_offices");
        item.setCustom(new TaskCustomData("Josh"));
        series.add(item);

        item = new GanttSeriesItem("Relocate staff", todayPlus(10),
                todayPlus(11));
        item.setId("relocate_staff");
        item.setParent("relocate");
        item.setCustom(new TaskCustomData("Mark"));
        series.add(item);

        item = new GanttSeriesItem("Relocate test facility", todayPlus(11),
                todayPlus(13));
        item.setId("relocate_test_facility");
        item.addDependency(new GanttSeriesItemDependency("relocate_staff"));
        item.setParent("relocate");
        item.setCustom(new TaskCustomData("Anne"));
        series.add(item);

        item = new GanttSeriesItem("Relocate cantina", todayPlus(11),
                todayPlus(14));
        item.setId("relocate_cantina");
        final GanttSeriesItemDependency relocateStaff = new GanttSeriesItemDependency(
                "relocate_staff");
        relocateStaff.setDashStyle(DashStyle.SOLID);
        item.addDependency(relocateStaff);
        item.setParent("relocate");
        series.add(item);

        return series;
    }

    private Instant todayPlus(int days) {
        return TODAY.plus(days, ChronoUnit.DAYS);
    }

    @SuppressWarnings("unused")
    static class TaskCustomData extends AbstractConfigurationObject {
        private String owner;

        public TaskCustomData(String owner) {
            this.owner = owner;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }
    }

}
