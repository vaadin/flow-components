/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.benchmark;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ai.chart.ChartAIController;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AbstractPlotOptions;
import com.vaadin.flow.component.charts.model.AbstractSeries;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.HeatSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsArea;
import com.vaadin.flow.component.charts.model.PlotOptionsBar;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.Stacking;

/**
 * Benchmarks {@link ChartAIController}: chart type, series structure and the
 * aggregated data are compared against the expected values.
 */
@EnabledIfEnvironmentVariable(named = AIBenchmark.MODEL_VARIABLE, matches = ".+")
class ChartAIControllerBenchmark {

    private static final Map<String, Double> SALES_BY_REGION = Map.of("Europe",
            3200.0, "North America", 2100.0, "Asia", 900.0);
    private static final List<String> MONTHS = List.of("Jan", "Feb", "Mar",
            "Apr", "May", "Jun");

    @RegisterExtension
    static AIBenchmark bench = new AIBenchmark();

    @Test
    void stackedColumnByRegion() {
        var db = BenchmarkDatabase.regionalSales();
        var chart = new Chart();
        var controller = new ChartAIController(chart, db);
        bench.conversation(chart, controller).say("""
                Stacked column chart of revenue per month, months \
                in calendar order, with the two regions stacked \
                on top of each other.""");
        Assertions.assertEquals(ChartType.COLUMN, chartType(chart),
                () -> "chart type, with " + queries(controller));
        var byName = seriesByName(chart);
        Assertions.assertEquals(Set.of("North", "South"), byName.keySet());
        Assertions.assertEquals(MONTHS, categories(byName.get("North")));
        Assertions.assertEquals(
                List.of(10000.0, 12000.0, 14000.0, 16000.0, 18000.0, 20000.0),
                values(byName.get("North")));
        Assertions.assertTrue(isStacked(chart),
                "column stacking is not enabled");
    }

    @Test
    void scatterSalaryAgainstAgePerDepartment() {
        var db = BenchmarkDatabase.employees();
        var chart = new Chart();
        var controller = new ChartAIController(chart, db);
        bench.conversation(chart, controller).say("""
                Scatter plot of salary against age, age on the \
                x axis, one series per department.""");
        Assertions.assertEquals(ChartType.SCATTER, chartType(chart),
                () -> "chart type, with " + queries(controller));
        var byName = seriesByName(chart);
        Assertions.assertEquals(Set.of("Engineering", "Sales", "Marketing"),
                byName.keySet());
        var points = new HashSet<List<Double>>();
        byName.values()
                .forEach(series -> series.getData().forEach(
                        item -> points.add(List.of(item.getX().doubleValue(),
                                item.getY().doubleValue()))));
        Assertions.assertEquals(
                Set.of(List.of(34.0, 72000.0), List.of(42.0, 88000.0),
                        List.of(28.0, 54000.0), List.of(39.0, 67000.0),
                        List.of(26.0, 49000.0), List.of(45.0, 61000.0)),
                points);
    }

    @Test
    void heatmapOfTrafficByDayAndHour() {
        var db = BenchmarkDatabase.traffic();
        var chart = new Chart();
        var controller = new ChartAIController(chart, db);
        bench.conversation(chart, controller).say("""
                Heatmap of visitors with the weekday on one axis \
                and the hour of day on the other.""");
        Assertions.assertEquals(ChartType.HEATMAP, chartType(chart),
                () -> "chart type, with " + queries(controller));
        var series = chart.getConfiguration().getSeries();
        Assertions.assertEquals(1, series.size(),
                () -> "expected one heat series, got " + series.size());
        Assertions.assertInstanceOf(HeatSeries.class, series.getFirst());
        var points = ((HeatSeries) series.getFirst()).getData();
        Assertions.assertNotNull(points,
                () -> "heat series has no points; the converter needs numeric x and y, "
                        + queries(controller));
        Assertions.assertEquals(20, points.length,
                "one heat point per day and hour");
    }

    @Test
    void changesOneSeriesToSplineKeepingTheOther() {
        var db = BenchmarkDatabase.regionalSales();
        var chart = new Chart();
        var controller = new ChartAIController(chart, db);
        var conversation = bench.conversation(chart, controller);
        conversation.say("""
                Column chart of monthly revenue with one series \
                per region, months in calendar order.""");
        conversation.say("""
                Draw the South series as a smooth spline line \
                instead, but keep North as columns.""");
        Assertions.assertEquals(ChartType.COLUMN, chartType(chart),
                () -> "chart type, with " + queries(controller));
        var byName = seriesByName(chart);
        Assertions.assertEquals(Set.of("North", "South"), byName.keySet());
        Assertions.assertEquals(ChartType.SPLINE,
                seriesType(byName.get("South")),
                "South should be a spline series");
        Assertions.assertNotEquals(ChartType.SPLINE,
                seriesType(byName.get("North")),
                "North should still be drawn as columns");
        Assertions.assertEquals(MONTHS, categories(byName.get("South")));
    }

    @Test
    void swapsToBarKeepingTitles() {
        var db = BenchmarkDatabase.regionalSales();
        var chart = new Chart();
        var controller = new ChartAIController(chart, db);
        var conversation = bench.conversation(chart, controller);
        conversation.say("""
                Stacked area chart of monthly revenue per region. \
                Title it "Regional Revenue" and label the value \
                axis "Revenue (EUR)".""");
        conversation.say("Turn it into a horizontal bar chart");
        var config = chart.getConfiguration();
        Assertions.assertEquals(ChartType.BAR, chartType(chart),
                () -> "chart type, with " + queries(controller));
        Assertions.assertEquals("Regional Revenue", config.getTitle().getText(),
                "title was lost");
        Assertions.assertEquals("Revenue (EUR)",
                config.getyAxis().getTitle().getText(),
                "value axis title was lost");
        Assertions.assertEquals(Set.of("North", "South"),
                seriesByName(chart).keySet());
    }

    @Test
    void keepsSeriesTypeThroughLaterChanges() {
        var db = BenchmarkDatabase.regionalSales();
        var chart = new Chart();
        var controller = new ChartAIController(chart, db);
        var conversation = bench.conversation(chart, controller);
        conversation.say("""
                Column chart of monthly revenue with one series \
                per region, months in calendar order.""");
        conversation.say("""
                Draw the South series as a smooth spline line \
                instead, but keep North as columns.""");
        conversation.say("Title the chart \"Revenue by region\".");
        Assertions.assertEquals("Revenue by region",
                chart.getConfiguration().getTitle().getText(),
                "title was not set");
        var byName = seriesByName(chart);
        Assertions.assertEquals(Set.of("North", "South"), byName.keySet());
        Assertions.assertEquals(ChartType.SPLINE,
                seriesType(byName.get("South")), "South lost its spline type");
        Assertions.assertNotEquals(ChartType.SPLINE,
                seriesType(byName.get("North")),
                "North should still be drawn as columns");
        Assertions.assertEquals(MONTHS, categories(byName.get("South")));
    }

    @Test
    void sortsBarsAndAddsLabelsInFollowUp() {
        var db = BenchmarkDatabase.employees();
        var chart = new Chart();
        var controller = new ChartAIController(chart, db);
        var conversation = bench.conversation(chart, controller);
        conversation
                .say("Horizontal bar chart of the salary of each employee.");
        conversation.say("""
                Sort the bars from the highest salary to the \
                lowest and show each salary as a data label.""");
        Assertions.assertEquals(ChartType.BAR, chartType(chart),
                () -> "chart type, with " + queries(controller));
        var series = chart.getConfiguration().getSeries();
        Assertions.assertEquals(1, series.size(),
                () -> "expected one series, got " + series.size());
        var bars = (DataSeries) series.getFirst();
        Assertions.assertEquals(
                List.of("Bram", "Aino", "Dana", "Fatima", "Chen", "Emil"),
                categories(bars),
                () -> "bar order, with " + queries(controller));
        Assertions.assertTrue(showsDataLabels(chart), "data labels are off");
    }

    @Test
    void changesOneRepsSeriesTypeByName() {
        var db = BenchmarkDatabase.salesReps();
        var chart = new Chart();
        var controller = new ChartAIController(chart, db);
        var conversation = bench.conversation(chart, controller);
        conversation.say("""
                Line chart of the deal amount per quarter, one \
                line per sales rep, quarters in order.""");
        conversation.say("""
                Draw Hanna Berg's series as columns and keep the \
                other reps as lines.""");
        Assertions.assertEquals(ChartType.LINE, chartType(chart),
                () -> "chart type, with " + queries(controller));
        var byName = seriesByName(chart);
        Assertions.assertEquals(
                Set.of("Hanna Berg", "Mateo Ruiz", "Olivia Park"),
                byName.keySet());
        Assertions.assertEquals(List.of("Q1", "Q2", "Q3", "Q4"),
                categories(byName.get("Hanna Berg")));
        Assertions.assertEquals(ChartType.COLUMN,
                seriesType(byName.get("Hanna Berg")),
                "Hanna Berg should be a column series");
        Assertions.assertNotEquals(ChartType.COLUMN,
                seriesType(byName.get("Mateo Ruiz")),
                "Mateo Ruiz should still be drawn as a line");
        Assertions.assertNotEquals(ChartType.COLUMN,
                seriesType(byName.get("Olivia Park")),
                "Olivia Park should still be drawn as a line");
    }

    /** The state is {@code null} until the first successful render. */
    private static String queries(ChartAIController controller) {
        var state = controller.getState();
        return state == null ? "no rendered state"
                : "queries " + state.queries();
    }

    /**
     * The type is {@code null} when the model only updated the data source and
     * never called {@code update_chart_configuration}.
     */
    private static ChartType chartType(Chart chart) {
        return chart.getConfiguration().getChart().getType();
    }

    private static Map<String, DataSeries> seriesByName(Chart chart) {
        var byName = new LinkedHashMap<String, DataSeries>();
        for (var series : chart.getConfiguration().getSeries()) {
            Assertions.assertInstanceOf(DataSeries.class, series);
            byName.put(series.getName(), (DataSeries) series);
        }
        return byName;
    }

    private static List<String> categories(DataSeries series) {
        return series.getData().stream().map(item -> item.getName()).toList();
    }

    private static List<Double> values(DataSeries series) {
        return series.getData().stream().map(item -> item.getY().doubleValue())
                .toList();
    }

    private static ChartType seriesType(AbstractSeries series) {
        var options = series.getPlotOptions();
        return options == null ? null : options.getChartType();
    }

    /**
     * Stacking may be set chart-wide under {@code plotOptions} or on the
     * individual series; both render as stacked columns.
     */
    private static boolean isStacked(Chart chart) {
        var config = chart.getConfiguration();
        if (config.getPlotOptions().stream()
                .anyMatch(ChartAIControllerBenchmark::stacked)) {
            return true;
        }
        return config.getSeries().stream()
                .anyMatch(series -> series instanceof AbstractSeries s
                        && stacked(s.getPlotOptions()));
    }

    /**
     * Data labels may be switched on chart-wide under {@code plotOptions} or on
     * the individual series.
     */
    private static boolean showsDataLabels(Chart chart) {
        var config = chart.getConfiguration();
        if (config.getPlotOptions().stream()
                .anyMatch(ChartAIControllerBenchmark::labelled)) {
            return true;
        }
        return config.getSeries().stream()
                .anyMatch(series -> series instanceof AbstractSeries s
                        && labelled(s.getPlotOptions()));
    }

    private static boolean labelled(AbstractPlotOptions options) {
        var labels = switch (options) {
        case PlotOptionsBar bar -> bar.getDataLabels();
        case PlotOptionsColumn column -> column.getDataLabels();
        case PlotOptionsSeries any -> any.getDataLabels();
        case null, default -> null;
        };
        return labels != null && Boolean.TRUE.equals(labels.getEnabled());
    }

    private static boolean stacked(AbstractPlotOptions options) {
        var stacking = switch (options) {
        case PlotOptionsColumn column -> column.getStacking();
        case PlotOptionsArea area -> area.getStacking();
        case PlotOptionsSeries any -> any.getStacking();
        case null, default -> null;
        };
        return stacking != null && stacking != Stacking.NONE;
    }
}
