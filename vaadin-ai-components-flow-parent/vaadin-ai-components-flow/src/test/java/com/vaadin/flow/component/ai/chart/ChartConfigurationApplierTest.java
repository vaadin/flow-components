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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.LayoutDirection;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.tests.MockUIExtension;

class ChartConfigurationApplierTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Chart chart;
    private ChartConfigurationApplier applier;

    @BeforeEach
    void setUp() {
        chart = new Chart();
        ui.add(chart);
        applier = new ChartConfigurationApplier();
    }

    @Nested
    class ChartTypeMapping {

        @Test
        void topLevelType_setsChartType() {
            applier.applyConfiguration(chart, "{\"type\":\"bar\"}");
            Assertions.assertEquals(ChartType.BAR,
                    chart.getConfiguration().getChart().getType());
        }

        @Test
        void nestedChartType_setsChartType() {
            applier.applyConfiguration(chart, "{\"chart\":{\"type\":\"pie\"}}");
            Assertions.assertEquals(ChartType.PIE,
                    chart.getConfiguration().getChart().getType());
        }

        @Test
        void unknownType_defaultsToLine() {
            applier.applyConfiguration(chart, "{\"type\":\"unknown_type\"}");
            Assertions.assertEquals(ChartType.LINE,
                    chart.getConfiguration().getChart().getType());
        }

        @Test
        void caseInsensitiveType() {
            applier.applyConfiguration(chart, "{\"type\":\"COLUMN\"}");
            Assertions.assertEquals(ChartType.COLUMN,
                    chart.getConfiguration().getChart().getType());
        }
    }

    @Nested
    class TitleAndSubtitle {

        @Test
        void titleAsObject() {
            applier.applyConfiguration(chart,
                    "{\"title\":{\"text\":\"My Title\"}}");
            Assertions.assertEquals("My Title",
                    chart.getConfiguration().getTitle().getText());
        }

        @Test
        void titleAsString() {
            applier.applyConfiguration(chart, "{\"title\":\"My Title\"}");
            Assertions.assertEquals("My Title",
                    chart.getConfiguration().getTitle().getText());
        }

        @Test
        void subtitleAsObject() {
            applier.applyConfiguration(chart,
                    "{\"subtitle\":{\"text\":\"Sub\"}}");
            Assertions.assertEquals("Sub",
                    chart.getConfiguration().getSubTitle().getText());
        }

        @Test
        void subtitleAsString() {
            applier.applyConfiguration(chart, "{\"subtitle\":\"Sub\"}");
            Assertions.assertEquals("Sub",
                    chart.getConfiguration().getSubTitle().getText());
        }
    }

    @Nested
    class TooltipConfig {

        @Test
        void tooltipProperties() {
            applier.applyConfiguration(chart,
                    "{\"tooltip\":{\"pointFormat\":\"{point.y}\",\"headerFormat\":\"<b>{series.name}</b>\",\"shared\":true,\"valueSuffix\":\" kg\",\"valuePrefix\":\"$\"}}");
            var tooltip = chart.getConfiguration().getTooltip();
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
            applier.applyConfiguration(chart,
                    "{\"legend\":{\"enabled\":false,\"align\":\"right\",\"verticalAlign\":\"top\",\"layout\":\"vertical\"}}");
            var legend = chart.getConfiguration().getLegend();
            Assertions.assertFalse(legend.getEnabled());
            Assertions.assertEquals(HorizontalAlign.RIGHT, legend.getAlign());
            Assertions.assertEquals(VerticalAlign.TOP,
                    legend.getVerticalAlign());
            Assertions.assertEquals(LayoutDirection.VERTICAL,
                    legend.getLayout());
        }

        @Test
        void invalidAlign_isIgnored() {
            applier.applyConfiguration(chart,
                    "{\"legend\":{\"align\":\"invalid\"}}");
            // Should not throw, default value remains
        }
    }

    @Nested
    class AxisConfig {

        @Test
        void xAxisType() {
            applier.applyConfiguration(chart,
                    "{\"xAxis\":{\"type\":\"datetime\"}}");
            Assertions.assertEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
        }

        @Test
        void yAxisTitleAndRange() {
            applier.applyConfiguration(chart,
                    "{\"yAxis\":{\"title\":{\"text\":\"Revenue\"},\"min\":0,\"max\":100}}");
            var yAxis = chart.getConfiguration().getyAxis();
            Assertions.assertEquals("Revenue", yAxis.getTitle().getText());
            Assertions.assertEquals(0.0, yAxis.getMin());
            Assertions.assertEquals(100.0, yAxis.getMax());
        }

        @Test
        void xAxisCategories() {
            applier.applyConfiguration(chart,
                    "{\"xAxis\":{\"categories\":[\"Jan\",\"Feb\",\"Mar\"]}}");
            Assertions.assertArrayEquals(new String[] { "Jan", "Feb", "Mar" },
                    chart.getConfiguration().getxAxis().getCategories());
        }

        @Test
        void invalidAxisType_isIgnored() {
            applier.applyConfiguration(chart,
                    "{\"xAxis\":{\"type\":\"invalid\"}}");
            // Should not throw
        }
    }

    @Nested
    class CreditsConfig {

        @Test
        void creditsProperties() {
            applier.applyConfiguration(chart,
                    "{\"credits\":{\"enabled\":false,\"text\":\"Source\",\"href\":\"https://example.com\"}}");
            var credits = chart.getConfiguration().getCredits();
            Assertions.assertFalse(credits.getEnabled());
            Assertions.assertEquals("Source", credits.getText());
            Assertions.assertEquals("https://example.com", credits.getHref());
        }
    }

    @Nested
    class PlotOptionsConfig {

        @Test
        void seriesStacking() {
            applier.applyConfiguration(chart,
                    "{\"plotOptions\":{\"series\":{\"stacking\":\"normal\"}}}");
            Configuration config = chart.getConfiguration();
            var plotOptions = config.getPlotOptions();
            Assertions.assertFalse(plotOptions.isEmpty());
        }

        @Test
        void columnStacking() {
            applier.applyConfiguration(chart,
                    "{\"plotOptions\":{\"column\":{\"stacking\":\"percent\",\"borderRadius\":5}}}");
            // Should not throw
        }

        @Test
        void barStacking() {
            applier.applyConfiguration(chart,
                    "{\"plotOptions\":{\"bar\":{\"stacking\":\"normal\",\"borderRadius\":3}}}");
            // Should not throw
        }

        @Test
        void pieInnerSize() {
            applier.applyConfiguration(chart,
                    "{\"plotOptions\":{\"pie\":{\"innerSize\":\"50%\"}}}");
            // Should not throw
        }

        @Test
        void seriesDataLabels() {
            applier.applyConfiguration(chart,
                    "{\"plotOptions\":{\"series\":{\"dataLabels\":{\"enabled\":true,\"format\":\"{point.y}\"}}}}");
            // Should not throw
        }

        @Test
        void seriesMarker() {
            applier.applyConfiguration(chart,
                    "{\"plotOptions\":{\"series\":{\"marker\":{\"enabled\":false}}}}");
            // Should not throw
        }

        @Test
        void invalidStacking_isIgnored() {
            applier.applyConfiguration(chart,
                    "{\"plotOptions\":{\"series\":{\"stacking\":\"invalid_value\"}}}");
            // Should not throw
        }
    }

    @Nested
    class ChartModelConfig {

        @Test
        void inverted() {
            applier.applyConfiguration(chart,
                    "{\"chart\":{\"inverted\":true}}");
            Assertions.assertTrue(
                    chart.getConfiguration().getChart().getInverted());
        }

        @Test
        void polar() {
            applier.applyConfiguration(chart, "{\"chart\":{\"polar\":true}}");
            Assertions
                    .assertTrue(chart.getConfiguration().getChart().getPolar());
        }

        @Test
        void dimensions() {
            applier.applyConfiguration(chart,
                    "{\"chart\":{\"width\":800,\"height\":\"600\"}}");
            Assertions.assertEquals(800,
                    chart.getConfiguration().getChart().getWidth());
            Assertions.assertEquals("600",
                    chart.getConfiguration().getChart().getHeight());
        }

        @Test
        void heightAsNumber() {
            applier.applyConfiguration(chart, "{\"chart\":{\"height\":400}}");
            Assertions.assertEquals("400",
                    chart.getConfiguration().getChart().getHeight());
        }
    }

    @Nested
    class ColorAxisConfig {

        @Test
        void colorAxisMinMax() {
            applier.applyConfiguration(chart,
                    "{\"colorAxis\":{\"min\":0,\"max\":100,\"minColor\":\"#ffffff\",\"maxColor\":\"#ff0000\"}}");
            var colorAxis = chart.getConfiguration().getColorAxis();
            Assertions.assertEquals(0.0, colorAxis.getMin());
            Assertions.assertEquals(100.0, colorAxis.getMax());
        }
    }

    @Nested
    class PaneConfig {

        @Test
        void paneProperties() {
            applier.applyConfiguration(chart,
                    "{\"pane\":{\"startAngle\":-150,\"endAngle\":150,\"center\":[\"50%\",\"75%\"],\"size\":\"80%\"}}");
            // Should not throw — pane is added to config
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void doubleEncodedJson_isHandled() {
            applier.applyConfiguration(chart,
                    "\"{\\\"title\\\":{\\\"text\\\":\\\"Decoded\\\"}}\"");
            Assertions.assertEquals("Decoded",
                    chart.getConfiguration().getTitle().getText());
        }

        @Test
        void invalidJson_doesNotThrow() {
            applier.applyConfiguration(chart, "not json");
            // Should log error but not throw
        }

        @Test
        void nonObjectJson_doesNotThrow() {
            applier.applyConfiguration(chart, "[1,2,3]");
            // Should log warning but not throw
        }

        @Test
        void emptyObject_doesNotChangeConfig() {
            chart.getConfiguration().setTitle("Original");
            applier.applyConfiguration(chart, "{}");
            Assertions.assertEquals("Original",
                    chart.getConfiguration().getTitle().getText());
        }
    }
}
