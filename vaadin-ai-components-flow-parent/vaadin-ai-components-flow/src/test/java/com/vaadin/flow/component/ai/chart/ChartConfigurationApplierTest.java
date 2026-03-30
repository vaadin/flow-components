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
package com.vaadin.flow.component.ai.chart;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Dimension;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.LayoutDirection;
import com.vaadin.flow.component.charts.model.PlotOptionsBar;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.Stacking;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.tests.MockUIExtension;

class ChartConfigurationApplierTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Chart chart;
    private Configuration config;

    @BeforeEach
    void setUp() {
        chart = new Chart();
        ui.add(chart);
        config = chart.getConfiguration();
    }

    private void apply(String json) {
        ChartConfigurationApplier.applyConfiguration(chart, json);
        config = chart.getConfiguration();
    }

    @Nested
    class ChartTypeMapping {

        @Test
        void allChartTypes_areSupported() {
            // Every ChartType (except EMPTY default) must be reachable
            var expectedTypes = Arrays.asList(ChartType.AREA, ChartType.LINE,
                    ChartType.SPLINE, ChartType.AREASPLINE, ChartType.BULLET,
                    ChartType.COLUMN, ChartType.BAR, ChartType.PIE,
                    ChartType.SCATTER, ChartType.GAUGE, ChartType.AREARANGE,
                    ChartType.COLUMNRANGE, ChartType.AREASPLINERANGE,
                    ChartType.BOXPLOT, ChartType.ERRORBAR, ChartType.BUBBLE,
                    ChartType.FUNNEL, ChartType.WATERFALL, ChartType.PYRAMID,
                    ChartType.SOLIDGAUGE, ChartType.HEATMAP, ChartType.TREEMAP,
                    ChartType.POLYGON, ChartType.CANDLESTICK, ChartType.FLAGS,
                    ChartType.TIMELINE, ChartType.OHLC, ChartType.ORGANIZATION,
                    ChartType.SANKEY, ChartType.XRANGE, ChartType.GANTT);
            for (ChartType type : expectedTypes) {
                chart = new Chart();
                ui.add(chart);
                ChartConfigurationApplier.applyConfiguration(chart,
                        "{\"type\":\"" + type.toString().toLowerCase() + "\"}");
                Assertions.assertEquals(type,
                        chart.getConfiguration().getChart().getType(),
                        "Chart type " + type + " should be mapped");
            }
        }

        @Test
        void topLevelType_setsChartType() {
            apply("{\"type\":\"bar\"}");
            Assertions.assertEquals(ChartType.BAR, config.getChart().getType());
        }

        @Test
        void nestedChartType_setsChartType() {
            apply("{\"chart\":{\"type\":\"pie\"}}");
            Assertions.assertEquals(ChartType.PIE, config.getChart().getType());
        }

        @Test
        void unknownType_defaultsToLine() {
            apply("{\"type\":\"unknown_type\"}");
            Assertions.assertEquals(ChartType.LINE,
                    config.getChart().getType());
        }

        @Test
        void caseInsensitiveType() {
            apply("{\"type\":\"COLUMN\"}");
            Assertions.assertEquals(ChartType.COLUMN,
                    config.getChart().getType());
        }
    }

    @Nested
    class TitleAndSubtitle {

        @Test
        void titleAsObject() {
            apply("{\"title\":{\"text\":\"My Title\"}}");
            Assertions.assertEquals("My Title", config.getTitle().getText());
        }

        @Test
        void titleAsString() {
            apply("{\"title\":\"My Title\"}");
            Assertions.assertEquals("My Title", config.getTitle().getText());
        }

        @Test
        void subtitleAsObject() {
            apply("{\"subtitle\":{\"text\":\"Sub\"}}");
            Assertions.assertEquals("Sub", config.getSubTitle().getText());
        }

        @Test
        void subtitleAsString() {
            apply("{\"subtitle\":\"Sub\"}");
            Assertions.assertEquals("Sub", config.getSubTitle().getText());
        }
    }

    @Nested
    class TooltipConfig {

        @Test
        void tooltipProperties() {
            apply("{\"tooltip\":{\"pointFormat\":\"{point.y}\","
                    + "\"headerFormat\":\"<b>{series.name}</b>\","
                    + "\"shared\":true,\"valueSuffix\":\" kg\","
                    + "\"valuePrefix\":\"$\"}}");
            var tooltip = config.getTooltip();
            Assertions.assertEquals("{point.y}", tooltip.getPointFormat());
            Assertions.assertEquals("<b>{series.name}</b>",
                    tooltip.getHeaderFormat());
            Assertions.assertTrue(tooltip.getShared());
            Assertions.assertEquals(" kg", tooltip.getValueSuffix());
            Assertions.assertEquals("$", tooltip.getValuePrefix());
        }
    }

    @Nested
    class LegendConfig {

        @Test
        void legendProperties() {
            apply("{\"legend\":{\"enabled\":false,\"align\":\"right\","
                    + "\"verticalAlign\":\"top\",\"layout\":\"vertical\"}}");
            var legend = config.getLegend();
            Assertions.assertFalse(legend.getEnabled());
            Assertions.assertEquals(HorizontalAlign.RIGHT, legend.getAlign());
            Assertions.assertEquals(VerticalAlign.TOP,
                    legend.getVerticalAlign());
            Assertions.assertEquals(LayoutDirection.VERTICAL,
                    legend.getLayout());
        }

        @Test
        void invalidAlign_isIgnored() {
            apply("{\"legend\":{\"align\":\"invalid\"}}");
            // Should not throw, default value remains
        }
    }

    @Nested
    class AxisConfig {

        @Test
        void xAxisType() {
            apply("{\"xAxis\":{\"type\":\"datetime\"}}");
            Assertions.assertEquals(AxisType.DATETIME,
                    config.getxAxis().getType());
        }

        @Test
        void yAxisTitleAndRange() {
            apply("{\"yAxis\":{\"title\":{\"text\":\"Revenue\"},"
                    + "\"min\":0,\"max\":100}}");
            var yAxis = config.getyAxis();
            Assertions.assertEquals("Revenue", yAxis.getTitle().getText());
            Assertions.assertEquals(0.0, yAxis.getMin());
            Assertions.assertEquals(100.0, yAxis.getMax());
        }

        @Test
        void xAxisCategories() {
            apply("{\"xAxis\":{\"categories\":[\"Jan\",\"Feb\",\"Mar\"]}}");
            Assertions.assertArrayEquals(new String[] { "Jan", "Feb", "Mar" },
                    config.getxAxis().getCategories());
        }

        @Test
        void zAxis_isApplied() {
            apply("{\"zAxis\":{\"type\":\"logarithmic\","
                    + "\"title\":{\"text\":\"Depth\"},\"min\":1,\"max\":1000}}");
            var zAxis = config.getzAxis();
            Assertions.assertEquals(AxisType.LOGARITHMIC, zAxis.getType());
            Assertions.assertEquals("Depth", zAxis.getTitle().getText());
            Assertions.assertEquals(1.0, zAxis.getMin());
            Assertions.assertEquals(1000.0, zAxis.getMax());
        }

        @Test
        void invalidAxisType_isIgnored() {
            apply("{\"xAxis\":{\"type\":\"invalid\"}}");
            // Should not throw
        }

        @Test
        void nonObjectAxis_isIgnored() {
            apply("{\"xAxis\":\"not_an_object\"}");
            // Should not throw
        }
    }

    @Nested
    class CreditsConfig {

        @Test
        void creditsProperties() {
            apply("{\"credits\":{\"enabled\":false,\"text\":\"Source\","
                    + "\"href\":\"https://example.com\"}}");
            var credits = config.getCredits();
            Assertions.assertFalse(credits.getEnabled());
            Assertions.assertEquals("Source", credits.getText());
            Assertions.assertEquals("https://example.com", credits.getHref());
        }
    }

    @Nested
    class ChartModelConfig {

        @Test
        void inverted() {
            apply("{\"chart\":{\"inverted\":true}}");
            Assertions.assertTrue(config.getChart().getInverted());
        }

        @Test
        void polar() {
            apply("{\"chart\":{\"polar\":true}}");
            Assertions.assertTrue(config.getChart().getPolar());
        }

        @Test
        void dimensions() {
            apply("{\"chart\":{\"width\":800,\"height\":\"600\"}}");
            Assertions.assertEquals(800, config.getChart().getWidth());
            Assertions.assertEquals("600", config.getChart().getHeight());
        }

        @Test
        void heightAsNumber() {
            apply("{\"chart\":{\"height\":400}}");
            Assertions.assertEquals("400", config.getChart().getHeight());
        }

        @Test
        void backgroundColor() {
            apply("{\"chart\":{\"backgroundColor\":\"#f0f0f0\"}}");
            Assertions.assertNotNull(config.getChart().getBackgroundColor());
        }

        @Test
        void borderProperties() {
            apply("{\"chart\":{\"borderColor\":\"#333\","
                    + "\"borderWidth\":2,\"borderRadius\":5}}");
            var chartModel = config.getChart();
            Assertions.assertNotNull(chartModel.getBorderColor());
            Assertions.assertEquals(2, chartModel.getBorderWidth());
            Assertions.assertEquals(5, chartModel.getBorderRadius());
        }

        @Test
        void margins() {
            apply("{\"chart\":{\"marginTop\":10,\"marginRight\":20,"
                    + "\"marginBottom\":30,\"marginLeft\":40}}");
            var chartModel = config.getChart();
            Assertions.assertEquals(10, chartModel.getMarginTop());
            Assertions.assertEquals(20, chartModel.getMarginRight());
            Assertions.assertEquals(30, chartModel.getMarginBottom());
            Assertions.assertEquals(40, chartModel.getMarginLeft());
        }

        @Test
        void spacing() {
            apply("{\"chart\":{\"spacingTop\":5,\"spacingRight\":10,"
                    + "\"spacingBottom\":15,\"spacingLeft\":20}}");
            var chartModel = config.getChart();
            Assertions.assertEquals(5, chartModel.getSpacingTop());
            Assertions.assertEquals(10, chartModel.getSpacingRight());
            Assertions.assertEquals(15, chartModel.getSpacingBottom());
            Assertions.assertEquals(20, chartModel.getSpacingLeft());
        }

        @Test
        void plotBackground() {
            apply("{\"chart\":{\"plotBackgroundColor\":\"#eee\","
                    + "\"plotBorderColor\":\"#aaa\",\"plotBorderWidth\":1}}");
            var chartModel = config.getChart();
            Assertions.assertNotNull(chartModel.getPlotBackgroundColor());
            Assertions.assertNotNull(chartModel.getPlotBorderColor());
            Assertions.assertEquals(1, chartModel.getPlotBorderWidth());
        }

        @Test
        void animation() {
            apply("{\"chart\":{\"animation\":false}}");
            Assertions.assertFalse(config.getChart().getAnimation());
        }

        @Test
        void styledMode() {
            apply("{\"chart\":{\"styledMode\":true}}");
            Assertions.assertTrue(config.getChart().getStyledMode());
        }

        @SuppressWarnings("deprecation")
        @Test
        void zoomType() {
            apply("{\"chart\":{\"zoomType\":\"xy\"}}");
            Assertions.assertEquals(Dimension.XY,
                    config.getChart().getZoomType());
        }

        @Test
        void invalidZoomType_isIgnored() {
            apply("{\"chart\":{\"zoomType\":\"invalid\"}}");
            // Should not throw
        }
    }

    @Nested
    class ColorAxisConfig {

        @Test
        void colorAxisMinMax() {
            apply("{\"colorAxis\":{\"min\":0,\"max\":100}}");
            var colorAxis = config.getColorAxis();
            Assertions.assertEquals(0.0, colorAxis.getMin());
            Assertions.assertEquals(100.0, colorAxis.getMax());
        }

        @Test
        void colorAxisColors() {
            apply("{\"colorAxis\":{\"minColor\":\"#ffffff\","
                    + "\"maxColor\":\"#ff0000\"}}");
            var colorAxis = config.getColorAxis();
            Assertions.assertNotNull(colorAxis.getMinColor());
            Assertions.assertNotNull(colorAxis.getMaxColor());
        }

        @Test
        void nonObjectColorAxis_isIgnored() {
            apply("{\"colorAxis\":\"not_an_object\"}");
            // Should not throw
        }
    }

    @Nested
    class PaneConfig {

        @Test
        void paneAngles() {
            apply("{\"pane\":{\"startAngle\":-150,\"endAngle\":150}}");
            // addPane adds at index 0 when no panes exist yet
            var pane = config.getPane();
            Assertions.assertEquals(-150, pane.getStartAngle());
            Assertions.assertEquals(150, pane.getEndAngle());
        }

        @Test
        void paneCenterAndSize() {
            apply("{\"pane\":{\"center\":[\"50%\",\"75%\"],"
                    + "\"size\":\"80%\"}}");
            var pane = config.getPane();
            Assertions.assertArrayEquals(new String[] { "50%", "75%" },
                    pane.getCenter());
            Assertions.assertEquals("80%", pane.getSize());
        }

        @Test
        void nonObjectPane_isIgnored() {
            apply("{\"pane\":\"not_an_object\"}");
            // Should not throw
        }
    }

    @Nested
    class PlotOptionsConfig {

        @Test
        void seriesStacking() {
            apply("{\"plotOptions\":{\"series\":{\"stacking\":\"normal\"}}}");
            var series = getPlotOption(PlotOptionsSeries.class);
            Assertions.assertEquals(Stacking.NORMAL, series.getStacking());
        }

        @Test
        void seriesDataLabels() {
            apply("{\"plotOptions\":{\"series\":{\"dataLabels\":"
                    + "{\"enabled\":true,\"format\":\"{point.y}\"}}}}");
            var series = getPlotOption(PlotOptionsSeries.class);
            Assertions.assertTrue(series.getDataLabels().getEnabled());
            Assertions.assertEquals("{point.y}",
                    series.getDataLabels().getFormat());
        }

        @Test
        void seriesMarker() {
            apply("{\"plotOptions\":{\"series\":{\"marker\":"
                    + "{\"enabled\":false}}}}");
            var series = getPlotOption(PlotOptionsSeries.class);
            Assertions.assertFalse(series.getMarker().getEnabled());
        }

        @Test
        void pieInnerSize() {
            apply("{\"plotOptions\":{\"pie\":{\"innerSize\":\"50%\"}}}");
            var pie = getPlotOption(PlotOptionsPie.class);
            Assertions.assertEquals("50%", pie.getInnerSize());
        }

        @Test
        void columnStacking_andBorderRadius() {
            apply("{\"plotOptions\":{\"column\":{\"stacking\":\"percent\","
                    + "\"borderRadius\":5}}}");
            var column = getPlotOption(PlotOptionsColumn.class);
            Assertions.assertEquals(Stacking.PERCENT, column.getStacking());
            Assertions.assertEquals(5, column.getBorderRadius());
        }

        @Test
        void barStacking_andBorderRadius() {
            apply("{\"plotOptions\":{\"bar\":{\"stacking\":\"normal\","
                    + "\"borderRadius\":3}}}");
            var bar = getPlotOption(PlotOptionsBar.class);
            Assertions.assertEquals(Stacking.NORMAL, bar.getStacking());
            Assertions.assertEquals(3, bar.getBorderRadius());
        }

        @Test
        void invalidStacking_isIgnored() {
            apply("{\"plotOptions\":{\"series\":"
                    + "{\"stacking\":\"invalid_value\"}}}");
            // Should not throw
        }

        @SuppressWarnings("unchecked")
        private <T> T getPlotOption(Class<T> type) {
            return (T) config.getPlotOptions().stream().filter(type::isInstance)
                    .findFirst().orElseThrow(() -> new RuntimeException(
                            "No PlotOptions of type " + type.getSimpleName()));
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void doubleEncodedJson_isHandled() {
            apply("\"{\\\"title\\\":{\\\"text\\\":\\\"Decoded\\\"}}\"");
            Assertions.assertEquals("Decoded", config.getTitle().getText());
        }

        @Test
        void invalidJson_doesNotThrow() {
            apply("not json");
            // Should log error but not throw
        }

        @Test
        void nonObjectJson_doesNotThrow() {
            apply("[1,2,3]");
            // Should log warning but not throw
        }

        @Test
        void emptyObject_resetsConfig() {
            config.setTitle("Original");
            apply("{}");
            Assertions.assertNull(config.getTitle().getText());
        }
    }
}
