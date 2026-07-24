/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.flow.testutil.TestPath;

/**
 * Covers both pattern-fill paths on one view holding two charts: chart 0 in
 * styled mode (the Vaadin CSS-rule bridge) and chart 1 in the default
 * non-styled mode (Highcharts' native pattern-fill).
 */
@TestPath("vaadin-charts/column/column-pattern-fill")
public class ColumnPatternFillIT extends AbstractChartIT {

    private static final String VAADIN_PREFIX = "vaadin-pattern-";
    private static final String NATIVE_PREFIX = "highcharts-pattern-";

    /**
     * Styled mode: series-wide patterns are applied through an injected
     * shadow-scoped CSS rule (no per-point fill attributes), which also styles
     * the legend symbol. A per-point override falls back to a fill attribute.
     */
    @Test
    public void styledMode_appliesPatternFillViaCssRuleAndLegend() {
        ChartElement chart = waitForChartWithPatterns(0, VAADIN_PREFIX);

        // A series-wide pattern plus one per-point override => two distinct
        // defs.
        List<String> patternIds = getPatternIds(chart, VAADIN_PREFIX);
        Assert.assertTrue(
                "Expected two distinct vaadin-pattern- defs, got: "
                        + patternIds,
                patternIds.stream().distinct().count() >= 2);

        // Series-level points get their fill from the CSS rule, not an
        // attribute.
        List<String> fillAttrs = getPointFillAttributes(chart, 0);
        Assert.assertTrue(
                "Series-level points must not carry a url() fill attribute, "
                        + "got: " + fillAttrs,
                isNoUrl(fillAttrs.get(0)) && isNoUrl(fillAttrs.get(2)));

        String seriesFill = getComputedPointFill(chart, 0, 0);
        Assert.assertTrue(
                "Series point fill should be a pattern url, got: " + seriesFill,
                isPatternUrl(seriesFill, VAADIN_PREFIX));
        Assert.assertEquals("Non-override points should share the series url",
                seriesFill, getComputedPointFill(chart, 0, 2));

        // The legend symbol receives the same pattern fill via the class rule.
        String legendFill = getLegendFillByColorIndex(chart, 0);
        Assert.assertEquals(
                "Legend symbol should use the same pattern url as the series",
                seriesFill, legendFill);

        // The per-point override (index 1) uses a fill attribute and a distinct
        // pattern url.
        Assert.assertTrue(
                "Override point should carry a url() fill attribute, " + "got: "
                        + fillAttrs.get(1),
                isPatternUrl(fillAttrs.get(1), VAADIN_PREFIX));
        String overrideFill = getComputedPointFill(chart, 0, 1);
        Assert.assertNotEquals("Override url should differ from the series url",
                seriesFill, overrideFill);

        assertControlSeriesIsSolid(chart);
    }

    /**
     * Non-styled mode (Flow default): Highcharts renders patterns natively via
     * {@code highcharts-pattern-*} defs and {@code fill} attributes. The Vaadin
     * bridge must stay out of the way (no {@code vaadin-pattern-*} defs, no
     * injected stylesheet).
     */
    @Test
    public void nonStyledMode_appliesNativePatternFillViaAttributes() {
        ChartElement chart = waitForChartWithPatterns(1, NATIVE_PREFIX);

        // Not styled mode: no styledMode option, no host attribute.
        Assert.assertFalse("Chart 1 should default to non-styled mode",
                getStyledModeOption(chart));
        Assert.assertFalse("Host must not carry a styled-mode attribute",
                hasStyledModeAttribute(chart));

        // Native defs for the series pattern and the per-point override.
        List<String> nativeIds = getPatternIds(chart, NATIVE_PREFIX);
        Assert.assertTrue(
                "Expected two distinct highcharts-pattern- defs, got: "
                        + nativeIds,
                nativeIds.stream().distinct().count() >= 2);

        // The bridge stayed out of the way: no vaadin-pattern- defs exist.
        Assert.assertTrue(
                "No vaadin-pattern- defs should exist in non-styled mode",
                getPatternIds(chart, VAADIN_PREFIX).isEmpty());

        // Points carry a native url() fill attribute and render (not black).
        List<String> fillAttrs = getPointFillAttributes(chart, 0);
        Assert.assertTrue(
                "Every patterned point should carry a native url() fill "
                        + "attribute, got: " + fillAttrs,
                fillAttrs.stream()
                        .allMatch(f -> isPatternUrl(f, NATIVE_PREFIX)));

        String seriesFill = getComputedPointFill(chart, 0, 0);
        Assert.assertTrue("Series point fill should be a native pattern url, "
                + "got: " + seriesFill,
                isPatternUrl(seriesFill, NATIVE_PREFIX));
        Assert.assertNotEquals(
                "Patterned point must render, not fall back to " + "black",
                "rgb(0, 0, 0)", seriesFill);
        Assert.assertEquals("Non-override points should share the series url",
                seriesFill, getComputedPointFill(chart, 0, 2));

        // The per-point override renders its own distinct native pattern.
        String overrideFill = getComputedPointFill(chart, 0, 1);
        Assert.assertTrue(
                "Override point fill should be a native pattern url, " + "got: "
                        + overrideFill,
                isPatternUrl(overrideFill, NATIVE_PREFIX));
        Assert.assertNotEquals("Override url should differ from the series url",
                seriesFill, overrideFill);

        // Highcharts renders the legend symbol natively too.
        String legendFill = getLegendFillBySeriesIndex(chart, 0);
        Assert.assertTrue(
                "Patterned legend symbol should be a native pattern "
                        + "url, got: " + legendFill,
                isPatternUrl(legendFill, NATIVE_PREFIX));
        Assert.assertEquals("Legend symbol should use the series pattern url",
                seriesFill, legendFill);

        assertControlSeriesIsSolid(chart);
    }

    // Series 1 is a plain SolidColor: solid computed fill, no url() attribute.
    private void assertControlSeriesIsSolid(ChartElement chart) {
        List<String> controlAttrs = getPointFillAttributes(chart, 1);
        Assert.assertFalse("Control series should have points",
                controlAttrs.isEmpty());
        Assert.assertTrue(
                "Control points must not carry a url() fill attribute",
                controlAttrs.stream().allMatch(ColumnPatternFillIT::isNoUrl));

        String controlFill = getComputedPointFill(chart, 1, 0);
        Assert.assertTrue(
                "Control fill should be a solid color, got: " + controlFill,
                controlFill != null && !controlFill.contains("url(")
                        && controlFill.startsWith("rgb"));
    }

    // Resolve the chart at the given index with $(...).get(index), which waits
    // and syncs (unlike .all()), then poll until its pattern defs for the given
    // prefix have been rendered.
    private ChartElement waitForChartWithPatterns(int index, String prefix) {
        ChartElement chart = $(ChartElement.class).get(index);
        waitUntil(driver -> !getPatternIds(chart, prefix).isEmpty());
        return chart;
    }

    private static boolean isPatternUrl(String fill, String prefix) {
        return fill != null && fill.contains("url(") && fill.contains(prefix);
    }

    private static boolean isNoUrl(String fill) {
        return fill == null || !fill.contains("url(");
    }

    // Plot-area point selector, scoped to the series group so legend item
    // symbols (which also carry a highcharts-series-<idx> class) are not
    // matched.
    private static String pointsSelector(int seriesIndex) {
        return ".highcharts-series-group .highcharts-series-" + seriesIndex
                + " .highcharts-point";
    }

    @SuppressWarnings("unchecked")
    private List<String> getPatternIds(ChartElement chart, String prefix) {
        return (List<String>) executeScript(
                "return Array.from(arguments[0].shadowRoot"
                        + ".querySelectorAll('pattern')).map(function(p){"
                        + "return p.id;}).filter(function(id){"
                        + "return id.indexOf('" + prefix + "')===0;});",
                chart);
    }

    @SuppressWarnings("unchecked")
    private List<String> getPointFillAttributes(ChartElement chart,
            int seriesIndex) {
        return (List<String>) executeScript(
                "return Array.from(arguments[0].shadowRoot"
                        + ".querySelectorAll('" + pointsSelector(seriesIndex)
                        + "')).map(function(p){return p.getAttribute('fill');});",
                chart);
    }

    private String getComputedPointFill(ChartElement chart, int seriesIndex,
            int pointIndex) {
        return getElementFromShadowRoot(chart, pointsSelector(seriesIndex),
                pointIndex).getCssValue("fill");
    }

    // Styled mode assigns highcharts-color-<index> to the legend item <g>, so
    // the injected pattern CSS rule fills it directly.
    private String getLegendFillByColorIndex(ChartElement chart,
            int colorIndex) {
        return chart.$(".highcharts-legend-item.highcharts-color-" + colorIndex)
                .first().getCssValue("fill");
    }

    // Non-styled mode leaves the legend <g> as highcharts-color-undefined, so
    // select by series index and read the rendered symbol (a rect for columns).
    private String getLegendFillBySeriesIndex(ChartElement chart,
            int seriesIndex) {
        return chart.$(".highcharts-legend-item.highcharts-series-"
                + seriesIndex + " rect").first().getCssValue("fill");
    }

    private Boolean getStyledModeOption(ChartElement chart) {
        return Boolean.TRUE.equals(executeScript("var el = arguments[0];"
                + "return !!(el.configuration && el.configuration.options"
                + " && el.configuration.options.chart"
                + " && el.configuration.options.chart.styledMode);", chart));
    }

    private boolean hasStyledModeAttribute(ChartElement chart) {
        return Boolean.TRUE.equals(executeScript(
                "return arguments[0].hasAttribute('styled-mode');", chart));
    }
}
