/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.column;

import java.util.stream.IntStream;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.XAxis;

/**
 * A column chart with more points than fit in the x-axis range and a scrollbar
 * to pan over them.
 *
 * The Highcharts accessibility module only navigates the points inside the
 * visible range with the arrow keys: a point outside the range is skipped, the
 * scrollbar from {@link Configuration#getScrollbar()} is never scrolled, and at
 * the end of a series the focus jumps to the first visible point of the next
 * series instead of its first point. {@link #addFullKeyboardNavigation}
 * replaces the Left and Right handling on the client so that every point of
 * every series is reachable.
 */
public class ColumnWithScrollbarKeyboardNavigation
        extends AbstractChartExample {

    private static final int POINTS = 60;
    private static final int VISIBLE_POINTS = 10;

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.COLUMN);

        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("Keyboard navigation with a scrollbar");
        configuration.addSeries(createSeries("Alpha", 1));
        configuration.addSeries(createSeries("Bravo", 100));

        XAxis xAxis = configuration.getxAxis();
        xAxis.setMin(0);
        xAxis.setMax(VISIBLE_POINTS - 1);
        configuration.getNavigator().setEnabled(false);
        configuration.getScrollbar().setEnabled(true);

        addFullKeyboardNavigation(chart);
        add(chart);
    }

    private static ListSeries createSeries(String name, int offset) {
        return new ListSeries(name, IntStream.range(0, POINTS)
                .mapToObj(i -> (Number) (i + offset)).toArray(Number[]::new));
    }

    /**
     * Installs a Left and Right arrow handler that walks the full data of the
     * chart, scrolls the x-axis when the target point is outside the visible
     * range, and crosses to the adjacent series at the ends of a series.
     *
     * The listener is added in the capture phase on purpose. Highcharts stops
     * the propagation of the keys it handles, so a bubble-phase listener on the
     * host element never runs.
     */
    private static void addFullKeyboardNavigation(Chart chart) {
        chart.getElement().executeJs(
                """
                        this.addEventListener('keydown', (e) => {
                          const chart = this.configuration;
                          if ((e.key !== 'ArrowRight' && e.key !== 'ArrowLeft')
                              || !chart || !chart.highlightedPoint) {
                            return;
                          }
                          const xsOf = (s) => (s.getColumn ? s.getColumn('x') : s.xData);
                          const next = e.key === 'ArrowRight';
                          let series = chart.highlightedPoint.series;
                          let i = chart.highlightedPoint.index + (next ? 1 : -1);
                          if (i < 0 || i >= xsOf(series).length) {
                            const all = chart.series.filter((s) => s.visible);
                            const index = all.indexOf(series) + (next ? 1 : -1);
                            series = all[(index + all.length) % all.length];
                            i = next ? 0 : xsOf(series).length - 1;
                          }
                          const x = xsOf(series)[i];
                          const axis = series.xAxis;
                          if (x < axis.min || x > axis.max) {
                            const range = axis.max - axis.min;
                            const min = Math.max(axis.dataMin,
                                Math.min(x, axis.dataMax - range));
                            axis.setExtremes(min, min + range, true, false);
                          }
                          const point = series.points.find((p) => p.x === x);
                          if (!point) {
                            return;
                          }
                          e.preventDefault();
                          e.stopPropagation();
                          point.highlight();
                        }, true);
                        """);
    }
}
