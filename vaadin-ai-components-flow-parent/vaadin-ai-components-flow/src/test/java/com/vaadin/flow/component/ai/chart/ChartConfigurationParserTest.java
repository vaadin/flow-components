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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.charts.model.AbstractSeries;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Dimension;
import com.vaadin.flow.component.charts.model.Frame;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.LayoutDirection;
import com.vaadin.flow.component.charts.model.PlotOptionsArea;
import com.vaadin.flow.component.charts.model.PlotOptionsBar;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.Stacking;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.YAxis;

class ChartConfigurationParserTest {

    private static Configuration parse(String json) {
        return ChartConfigurationParser.parse(json);
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
                var config = parse(
                        "{\"type\":\"" + type.toString().toLowerCase() + "\"}");
                Assertions.assertEquals(type, config.getChart().getType(),
                        "Chart type " + type + " should be mapped");
            }
        }

        @Test
        void topLevelType_setsChartType() {
            var config = parse("{\"type\":\"bar\"}");
            Assertions.assertEquals(ChartType.BAR, config.getChart().getType());
        }

        @Test
        void nestedChartType_setsChartType() {
            var config = parse("{\"chart\":{\"type\":\"pie\"}}");
            Assertions.assertEquals(ChartType.PIE, config.getChart().getType());
        }

        @Test
        void unknownType_defaultsToLine() {
            var config = parse("{\"type\":\"unknown_type\"}");
            Assertions.assertEquals(ChartType.LINE,
                    config.getChart().getType());
        }

        @Test
        void caseInsensitiveType() {
            var config = parse("{\"type\":\"COLUMN\"}");
            Assertions.assertEquals(ChartType.COLUMN,
                    config.getChart().getType());
        }
    }

    @Nested
    class TitleAndSubtitle {

        @Test
        void titleAsObject() {
            var config = parse("{\"title\":{\"text\":\"My Title\"}}");
            Assertions.assertEquals("My Title", config.getTitle().getText());
        }

        @Test
        void titleAsString() {
            var config = parse("{\"title\":\"My Title\"}");
            Assertions.assertEquals("My Title", config.getTitle().getText());
        }

        @Test
        void subtitleAsObject() {
            var config = parse("{\"subtitle\":{\"text\":\"Sub\"}}");
            Assertions.assertEquals("Sub", config.getSubTitle().getText());
        }

        @Test
        void subtitleAsString() {
            var config = parse("{\"subtitle\":\"Sub\"}");
            Assertions.assertEquals("Sub", config.getSubTitle().getText());
        }
    }

    @Nested
    class TooltipConfig {

        @Test
        void tooltipProperties() {
            var config = parse("{\"tooltip\":{\"pointFormat\":\"{point.y}\","
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
            var config = parse(
                    "{\"legend\":{\"enabled\":false,\"align\":\"right\","
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
            parse("{\"legend\":{\"align\":\"invalid\"}}");
            // Should not throw, default value remains
        }
    }

    @Nested
    class AxisConfig {

        @Test
        void xAxisType() {
            var config = parse("{\"xAxis\":{\"type\":\"datetime\"}}");
            Assertions.assertEquals(AxisType.DATETIME,
                    config.getxAxis().getType());
        }

        @Test
        void yAxisTitleAndRange() {
            var config = parse("{\"yAxis\":{\"title\":{\"text\":\"Revenue\"},"
                    + "\"min\":0,\"max\":100}}");
            var yAxis = config.getyAxis();
            Assertions.assertEquals("Revenue", yAxis.getTitle().getText());
            Assertions.assertEquals(0.0, yAxis.getMin().doubleValue());
            Assertions.assertEquals(100.0, yAxis.getMax().doubleValue());
        }

        @Test
        void xAxisCategories() {
            var config = parse(
                    "{\"xAxis\":{\"categories\":[\"Jan\",\"Feb\",\"Mar\"]}}");
            Assertions.assertArrayEquals(new String[] { "Jan", "Feb", "Mar" },
                    config.getxAxis().getCategories());
        }

        @Test
        void zAxis_isApplied() {
            var config = parse("{\"zAxis\":{\"type\":\"logarithmic\","
                    + "\"title\":{\"text\":\"Depth\"},\"min\":1,\"max\":1000}}");
            var zAxis = config.getzAxis();
            Assertions.assertEquals(AxisType.LOGARITHMIC, zAxis.getType());
            Assertions.assertEquals("Depth", zAxis.getTitle().getText());
            Assertions.assertEquals(1.0, zAxis.getMin().doubleValue());
            Assertions.assertEquals(1000.0, zAxis.getMax().doubleValue());
        }

        @Test
        void yAxisArray_createsSecondaryAxes() {
            var config = parse("{\"yAxis\":["
                    + "{\"title\":{\"text\":\"Price\"},\"min\":0},"
                    + "{\"title\":{\"text\":\"Volume\"},\"opposite\":true}"
                    + "]}");
            Assertions.assertEquals(2, config.getNumberOfyAxes());

            YAxis primary = config.getyAxis(0);
            Assertions.assertEquals("Price", primary.getTitle().getText());
            Assertions.assertEquals(0.0, primary.getMin().doubleValue());

            YAxis secondary = config.getyAxis(1);
            Assertions.assertEquals("Volume", secondary.getTitle().getText());
            Assertions.assertTrue(secondary.getOpposite());
        }

        @Test
        void yAxisArray_nonObjectElement_skipped() {
            var config = parse("{\"yAxis\":["
                    + "{\"title\":{\"text\":\"Price\"}}," + "42,"
                    + "{\"title\":{\"text\":\"Volume\"},\"opposite\":true}"
                    + "]}");
            // The non-object element (42) should be skipped and not create
            // a spurious empty secondary axis.
            Assertions.assertEquals(2, config.getNumberOfyAxes());
        }

        @Test
        void yAxisSingleObject_stillWorks() {
            var config = parse(
                    "{\"yAxis\":{\"title\":{\"text\":\"Revenue\"}}}");
            Assertions.assertEquals("Revenue",
                    config.getyAxis().getTitle().getText());
        }

        @Test
        void invalidAxisType_isIgnored() {
            parse("{\"xAxis\":{\"type\":\"invalid\"}}");
            // Should not throw
        }

        @Test
        void nonObjectAxis_isIgnored() {
            parse("{\"xAxis\":\"not_an_object\"}");
            // Should not throw
        }
    }

    @Nested
    class CreditsConfig {

        @Test
        void creditsProperties() {
            var config = parse(
                    "{\"credits\":{\"enabled\":false,\"text\":\"Source\","
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
            var config = parse("{\"chart\":{\"inverted\":true}}");
            Assertions.assertTrue(config.getChart().getInverted());
        }

        @Test
        void polar() {
            var config = parse("{\"chart\":{\"polar\":true}}");
            Assertions.assertTrue(config.getChart().getPolar());
        }

        @Test
        void dimensions() {
            var config = parse(
                    "{\"chart\":{\"width\":800,\"height\":\"600\"}}");
            Assertions.assertEquals(800, config.getChart().getWidth());
            Assertions.assertEquals("600", config.getChart().getHeight());
        }

        @Test
        void heightAsNumber() {
            var config = parse("{\"chart\":{\"height\":400}}");
            Assertions.assertEquals("400", config.getChart().getHeight());
        }

        @Test
        void backgroundColor() {
            var config = parse("{\"chart\":{\"backgroundColor\":\"#f0f0f0\"}}");
            Assertions.assertNotNull(config.getChart().getBackgroundColor());
        }

        @Test
        void borderProperties() {
            var config = parse("{\"chart\":{\"borderColor\":\"#333\","
                    + "\"borderWidth\":2,\"borderRadius\":5}}");
            var chartModel = config.getChart();
            Assertions.assertNotNull(chartModel.getBorderColor());
            Assertions.assertEquals(2, chartModel.getBorderWidth());
            Assertions.assertEquals(5, chartModel.getBorderRadius());
        }

        @Test
        void margins() {
            var config = parse(
                    "{\"chart\":{\"marginTop\":10,\"marginRight\":20,"
                            + "\"marginBottom\":30,\"marginLeft\":40}}");
            var chartModel = config.getChart();
            Assertions.assertEquals(10, chartModel.getMarginTop());
            Assertions.assertEquals(20, chartModel.getMarginRight());
            Assertions.assertEquals(30, chartModel.getMarginBottom());
            Assertions.assertEquals(40, chartModel.getMarginLeft());
        }

        @Test
        void spacing() {
            var config = parse(
                    "{\"chart\":{\"spacingTop\":5,\"spacingRight\":10,"
                            + "\"spacingBottom\":15,\"spacingLeft\":20}}");
            var chartModel = config.getChart();
            Assertions.assertEquals(5, chartModel.getSpacingTop());
            Assertions.assertEquals(10, chartModel.getSpacingRight());
            Assertions.assertEquals(15, chartModel.getSpacingBottom());
            Assertions.assertEquals(20, chartModel.getSpacingLeft());
        }

        @Test
        void plotBackground() {
            var config = parse("{\"chart\":{\"plotBackgroundColor\":\"#eee\","
                    + "\"plotBorderColor\":\"#aaa\",\"plotBorderWidth\":1}}");
            var chartModel = config.getChart();
            Assertions.assertNotNull(chartModel.getPlotBackgroundColor());
            Assertions.assertNotNull(chartModel.getPlotBorderColor());
            Assertions.assertEquals(1, chartModel.getPlotBorderWidth());
        }

        @Test
        void animation() {
            var config = parse("{\"chart\":{\"animation\":false}}");
            Assertions.assertFalse(config.getChart().getAnimation());
        }

        @Test
        void styledMode() {
            var config = parse("{\"chart\":{\"styledMode\":true}}");
            Assertions.assertTrue(config.getChart().getStyledMode());
        }

        @SuppressWarnings("deprecation")
        @Test
        void zoomType() {
            var config = parse("{\"chart\":{\"zoomType\":\"xy\"}}");
            Assertions.assertEquals(Dimension.XY,
                    config.getChart().getZoomType());
        }

        @Test
        void invalidZoomType_isIgnored() {
            parse("{\"chart\":{\"zoomType\":\"invalid\"}}");
            // Should not throw
        }
    }

    @Nested
    class ColorAxisConfig {

        @Test
        void colorAxisMinMax() {
            var config = parse("{\"colorAxis\":{\"min\":0,\"max\":100}}");
            var colorAxis = config.getColorAxis();
            Assertions.assertEquals(0.0, colorAxis.getMin().doubleValue());
            Assertions.assertEquals(100.0, colorAxis.getMax().doubleValue());
        }

        @Test
        void colorAxisColors() {
            var config = parse("{\"colorAxis\":{\"minColor\":\"#ffffff\","
                    + "\"maxColor\":\"#ff0000\"}}");
            var colorAxis = config.getColorAxis();
            Assertions.assertNotNull(colorAxis.getMinColor());
            Assertions.assertNotNull(colorAxis.getMaxColor());
        }

        @Test
        void nonObjectColorAxis_isIgnored() {
            parse("{\"colorAxis\":\"not_an_object\"}");
            // Should not throw
        }
    }

    @Nested
    class PaneConfig {

        @Test
        void paneAngles() {
            var config = parse(
                    "{\"pane\":{\"startAngle\":-150,\"endAngle\":150}}");
            var pane = config.getPane();
            Assertions.assertEquals(-150, pane.getStartAngle());
            Assertions.assertEquals(150, pane.getEndAngle());
        }

        @Test
        void paneCenterAndSize() {
            var config = parse("{\"pane\":{\"center\":[\"50%\",\"75%\"],"
                    + "\"size\":\"80%\"}}");
            var pane = config.getPane();
            Assertions.assertArrayEquals(new String[] { "50%", "75%" },
                    pane.getCenter());
            Assertions.assertEquals("80%", pane.getSize());
        }

        @Test
        void nonObjectPane_isIgnored() {
            parse("{\"pane\":\"not_an_object\"}");
            // Should not throw
        }
    }

    @Nested
    class PlotOptionsConfig {

        @Test
        void seriesStacking() {
            var config = parse(
                    "{\"plotOptions\":{\"series\":{\"stacking\":\"normal\"}}}");
            var series = getPlotOption(config, PlotOptionsSeries.class);
            Assertions.assertEquals(Stacking.NORMAL, series.getStacking());
        }

        @Test
        void seriesDataLabels() {
            var config = parse("{\"plotOptions\":{\"series\":{\"dataLabels\":"
                    + "{\"enabled\":true,\"format\":\"{point.y}\"}}}}");
            var series = getPlotOption(config, PlotOptionsSeries.class);
            Assertions.assertTrue(series.getDataLabels().getEnabled());
            Assertions.assertEquals("{point.y}",
                    series.getDataLabels().getFormat());
        }

        @Test
        void seriesMarker() {
            var config = parse("{\"plotOptions\":{\"series\":{\"marker\":"
                    + "{\"enabled\":false}}}}");
            var series = getPlotOption(config, PlotOptionsSeries.class);
            Assertions.assertFalse(series.getMarker().getEnabled());
        }

        @Test
        void pieInnerSize() {
            var config = parse(
                    "{\"plotOptions\":{\"pie\":{\"innerSize\":\"50%\"}}}");
            var pie = getPlotOption(config, PlotOptionsPie.class);
            Assertions.assertEquals("50%", pie.getInnerSize());
        }

        @Test
        void columnStacking_andBorderRadius() {
            var config = parse(
                    "{\"plotOptions\":{\"column\":{\"stacking\":\"percent\","
                            + "\"borderRadius\":5}}}");
            var column = getPlotOption(config, PlotOptionsColumn.class);
            Assertions.assertEquals(Stacking.PERCENT, column.getStacking());
            Assertions.assertEquals(5, column.getBorderRadius());
        }

        @Test
        void barStacking_andBorderRadius() {
            var config = parse(
                    "{\"plotOptions\":{\"bar\":{\"stacking\":\"normal\","
                            + "\"borderRadius\":3}}}");
            var bar = getPlotOption(config, PlotOptionsBar.class);
            Assertions.assertEquals(Stacking.NORMAL, bar.getStacking());
            Assertions.assertEquals(3, bar.getBorderRadius());
        }

        @Test
        void invalidStacking_isIgnored() {
            parse("{\"plotOptions\":{\"series\":"
                    + "{\"stacking\":\"invalid_value\"}}}");
            // Should not throw
        }

        @Test
        void lineLineWidth() {
            var config = parse(
                    "{\"plotOptions\":{\"line\":{\"lineWidth\":3}}}");
            var line = getPlotOption(config, PlotOptionsLine.class);
            Assertions.assertEquals(3, line.getLineWidth());
        }

        @Test
        void areaFillOpacity() {
            var config = parse(
                    "{\"plotOptions\":{\"area\":{\"fillOpacity\":0.5}}}");
            var area = getPlotOption(config, PlotOptionsArea.class);
            Assertions.assertEquals(0.5, area.getFillOpacity());
        }

        @Test
        void unknownChartType_isIgnored() {
            var config = parse(
                    "{\"plotOptions\":{\"nonexistent\":{\"foo\":true}}}");
            Assertions.assertTrue(config.getPlotOptions().isEmpty());
        }

        @Test
        void multipleTypes_inSamePlotOptions() {
            var config = parse(
                    "{\"plotOptions\":{" + "\"column\":{\"borderRadius\":5},"
                            + "\"line\":{\"lineWidth\":2}}}");
            var column = getPlotOption(config, PlotOptionsColumn.class);
            Assertions.assertEquals(5, column.getBorderRadius());
            var line = getPlotOption(config, PlotOptionsLine.class);
            Assertions.assertEquals(2, line.getLineWidth());
        }

        @Test
        void caseInsensitiveEnumValues() {
            var config = parse(
                    "{\"plotOptions\":{\"column\":{\"stacking\":\"Normal\"}}}");
            var column = getPlotOption(config, PlotOptionsColumn.class);
            Assertions.assertEquals(Stacking.NORMAL, column.getStacking());
        }

        @Test
        void nonObjectValue_isSkipped() {
            var config = parse(
                    "{\"plotOptions\":{\"column\":\"not an object\"}}");
            Assertions.assertTrue(config.getPlotOptions().isEmpty());
        }

        @Test
        void colorField_deserializedFromString() {
            var config = parse("{\"plotOptions\":{\"column\":"
                    + "{\"color\":\"#ff0000\"}}}");
            var column = getPlotOption(config, PlotOptionsColumn.class);
            Assertions.assertNotNull(column.getColor());
            Assertions.assertEquals("#ff0000", column.getColor().toString());
        }

        @SuppressWarnings("unchecked")
        private <T> T getPlotOption(Configuration config, Class<T> type) {
            return (T) config.getPlotOptions().stream().filter(type::isInstance)
                    .findFirst().orElseThrow(() -> new RuntimeException(
                            "No PlotOptions of type " + type.getSimpleName()));
        }
    }

    @Nested
    class Options3dConfig {

        @Test
        void allOptions3dProperties() {
            var config = parse("{\"chart\":{\"options3d\":{"
                    + "\"enabled\":true,\"alpha\":15,\"beta\":25,"
                    + "\"depth\":200,\"viewDistance\":150}}}");
            var options3d = config.getChart().getOptions3d();
            Assertions.assertTrue(options3d.getEnabled());
            Assertions.assertEquals(15, options3d.getAlpha());
            Assertions.assertEquals(25, options3d.getBeta());
            Assertions.assertEquals(200, options3d.getDepth());
            Assertions.assertEquals(150, options3d.getViewDistance());
        }

        @Test
        void partialOptions3d_onlyEnabled() {
            var config = parse(
                    "{\"chart\":{\"options3d\":{\"enabled\":true}}}");
            var options3d = config.getChart().getOptions3d();
            Assertions.assertTrue(options3d.getEnabled());
            Assertions.assertNull(options3d.getAlpha());
            Assertions.assertNull(options3d.getBeta());
        }

        @Test
        void nonObjectOptions3d_isIgnored() {
            var config = parse("{\"chart\":{\"options3d\":\"not_an_object\"}}");
            // Should not throw, options3d stays at defaults
            Assertions.assertNull(config.getChart().getOptions3d().getAlpha());
        }

        @Test
        void frameWithAllPanels() {
            var config = parse("{\"chart\":{\"options3d\":{"
                    + "\"enabled\":true," + "\"frame\":{"
                    + "\"back\":{\"color\":\"#f5f5dc\",\"size\":2},"
                    + "\"bottom\":{\"color\":\"#cccccc\",\"size\":3},"
                    + "\"side\":{\"color\":\"#dddddd\",\"size\":4},"
                    + "\"top\":{\"color\":\"#eeeeee\",\"size\":5}" + "}}}}");
            Frame frame = config.getChart().getOptions3d().getFrame();
            Assertions.assertNotNull(frame.getBack().getColor());
            Assertions.assertEquals(2, frame.getBack().getSize());
            Assertions.assertNotNull(frame.getBottom().getColor());
            Assertions.assertEquals(3, frame.getBottom().getSize());
            Assertions.assertNotNull(frame.getSide().getColor());
            Assertions.assertEquals(4, frame.getSide().getSize());
            Assertions.assertNotNull(frame.getTop().getColor());
            Assertions.assertEquals(5, frame.getTop().getSize());
        }

        @Test
        void frameWithOnlyBack() {
            var config = parse("{\"chart\":{\"options3d\":{"
                    + "\"frame\":{\"back\":{\"color\":\"#ff0000\",\"size\":1}}"
                    + "}}}");
            Frame frame = config.getChart().getOptions3d().getFrame();
            Assertions.assertNotNull(frame.getBack().getColor());
            Assertions.assertEquals(1, frame.getBack().getSize());
            // Other panels should have no color set
            Assertions.assertNull(frame.getBottom().getColor());
            Assertions.assertNull(frame.getSide().getColor());
            Assertions.assertNull(frame.getTop().getColor());
        }

        @Test
        void framePanelWithOnlyColor() {
            var config = parse("{\"chart\":{\"options3d\":{"
                    + "\"frame\":{\"bottom\":{\"color\":\"#aabbcc\"}}" + "}}}");
            var bottom = config.getChart().getOptions3d().getFrame()
                    .getBottom();
            Assertions.assertNotNull(bottom.getColor());
            Assertions.assertNull(bottom.getSize());
        }

        @Test
        void framePanelWithOnlySize() {
            var config = parse("{\"chart\":{\"options3d\":{"
                    + "\"frame\":{\"side\":{\"size\":10}}" + "}}}");
            var side = config.getChart().getOptions3d().getFrame().getSide();
            Assertions.assertNull(side.getColor());
            Assertions.assertEquals(10, side.getSize());
        }

        @Test
        void nonObjectFrame_isIgnored() {
            var config = parse("{\"chart\":{\"options3d\":{"
                    + "\"enabled\":true,\"frame\":\"not_an_object\"}}}");
            // Should not throw
            Assertions
                    .assertTrue(config.getChart().getOptions3d().getEnabled());
        }

        @Test
        void nonObjectPanelEntry_isIgnored() {
            var config = parse("{\"chart\":{\"options3d\":{"
                    + "\"frame\":{\"back\":42,\"bottom\":\"str\","
                    + "\"side\":true,\"top\":null}}}}");
            // Should not throw, panels remain at defaults
            Frame frame = config.getChart().getOptions3d().getFrame();
            Assertions.assertNull(frame.getBack().getColor());
            Assertions.assertNull(frame.getBottom().getColor());
            Assertions.assertNull(frame.getSide().getColor());
            Assertions.assertNull(frame.getTop().getColor());
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void doubleEncodedJson_isHandled() {
            var config = parse(
                    "\"{\\\"title\\\":{\\\"text\\\":\\\"Decoded\\\"}}\"");
            Assertions.assertEquals("Decoded", config.getTitle().getText());
        }

        @Test
        void invalidJson_throws() {
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> parse("not json"));
        }

        @Test
        void nonObjectJson_throws() {
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> parse("[1,2,3]"));
        }

        @Test
        void emptyObject_producesDefaultConfig() {
            var config = parse("{}");
            Assertions.assertNull(config.getTitle().getText());
        }
    }

    @Nested
    class SeriesConfig {

        private static AbstractSeries findSeries(Configuration config,
                String name) {
            return config.getSeries().stream()
                    .filter(s -> s instanceof AbstractSeries as
                            && name.equals(as.getName()))
                    .map(AbstractSeries.class::cast).findFirst().orElse(null);
        }

        @Test
        void seriesWithType_createsCorrectPlotOptions() {
            var config = parse("{\"series\":[{\"name\":\"South\","
                    + "\"type\":\"column\",\"plotOptions\":{}}]}");
            var series = findSeries(config, "South");
            Assertions.assertNotNull(series);
            Assertions.assertInstanceOf(PlotOptionsColumn.class,
                    series.getPlotOptions());
        }

        @Test
        void seriesWithYAxis_parsed() {
            var config = parse(
                    "{\"series\":[{\"name\":\"Volume\",\"yAxis\":1}]}");
            var series = findSeries(config, "Volume");
            Assertions.assertNotNull(series);
            Assertions.assertEquals(1, series.getyAxis());
        }

        @Test
        void seriesWithPlotOptions_deserialized() {
            var config = parse(
                    "{\"series\":[{\"name\":\"Revenue\"," + "\"type\":\"line\","
                            + "\"plotOptions\":{\"lineWidth\":3}}]}");
            var series = findSeries(config, "Revenue");
            Assertions.assertInstanceOf(PlotOptionsLine.class,
                    series.getPlotOptions());
            var lineOptions = (PlotOptionsLine) series.getPlotOptions();
            Assertions.assertEquals(3, lineOptions.getLineWidth());
        }

        @Test
        void seriesWithStacking_deserialized() {
            var config = parse(
                    "{\"series\":[{\"name\":\"Sales\"," + "\"type\":\"column\","
                            + "\"plotOptions\":{\"stacking\":\"normal\"}}]}");
            var series = findSeries(config, "Sales");
            var columnOptions = (PlotOptionsColumn) series.getPlotOptions();
            Assertions.assertEquals(Stacking.NORMAL,
                    columnOptions.getStacking());
        }

        @Test
        void multipleSeries_parsed() {
            var config = parse(
                    "{\"series\":[{\"name\":\"North\",\"type\":\"areaspline\","
                            + "\"plotOptions\":{}},"
                            + "{\"name\":\"South\",\"type\":\"column\","
                            + "\"yAxis\":1,\"plotOptions\":{}}]}");
            Assertions.assertEquals(2, config.getSeries().size());
            Assertions.assertNotNull(findSeries(config, "North"));
            Assertions.assertEquals(1, findSeries(config, "South").getyAxis());
        }

        @Test
        void seriesMissing_noSeriesAdded() {
            var config = parse("{\"title\":\"Test\"}");
            Assertions.assertTrue(config.getSeries().isEmpty());
        }

        @Test
        void seriesWithoutPlotOptions_noProblem() {
            var config = parse(
                    "{\"series\":[{\"name\":\"Data\",\"yAxis\":1}]}");
            var series = findSeries(config, "Data");
            Assertions.assertNotNull(series);
            Assertions.assertNull(series.getPlotOptions());
        }

        @Test
        void seriesWithTypeButNoPlotOptions_createsPlotOptionsFromType() {
            var config = parse("{\"series\":[{\"name\":\"South\","
                    + "\"type\":\"line\",\"yAxis\":1}]}");
            var series = findSeries(config, "South");
            Assertions.assertNotNull(series);
            Assertions.assertInstanceOf(PlotOptionsLine.class,
                    series.getPlotOptions(),
                    "type without plotOptions should create default "
                            + "plotOptions of the correct type");
        }

        @Test
        void seriesWithoutName_skipped() {
            var config = parse("{\"series\":[{\"type\":\"column\"}]}");
            Assertions.assertTrue(config.getSeries().isEmpty());
        }

        @Test
        void seriesWithEmptyName_skipped() {
            var config = parse(
                    "{\"series\":[{\"name\":\"\",\"type\":\"column\"}]}");
            Assertions.assertTrue(config.getSeries().isEmpty());
        }

        @Test
        void seriesWithUnknownType_fallsBackToPlotOptionsSeries() {
            var config = parse("{\"series\":[{\"name\":\"Data\","
                    + "\"type\":\"nonexistent\","
                    + "\"plotOptions\":{\"lineWidth\":3}}]}");
            var series = findSeries(config, "Data");
            Assertions.assertNotNull(series);
            Assertions.assertInstanceOf(PlotOptionsSeries.class,
                    series.getPlotOptions());
        }

    }
}
