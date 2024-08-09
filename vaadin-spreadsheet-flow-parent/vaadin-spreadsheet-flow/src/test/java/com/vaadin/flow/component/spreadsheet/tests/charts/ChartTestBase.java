/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.charts;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.util.CellReference;
import org.junit.Assert;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AbstractPlotOptions;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DashStyle;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsArea;
import com.vaadin.flow.component.charts.model.PlotOptionsBar;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.model.PlotOptionsScatter;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.charts.model.Stacking;
import com.vaadin.flow.component.spreadsheet.SheetChartWrapper;
import com.vaadin.flow.component.spreadsheet.SheetOverlayWrapper;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;

public class ChartTestBase {
    private static String sampleFileDiretory = "";

    protected Double[][] data = { { 100d, 200d, 2000d, 800d, 99d },
            { 230d, 300d, 600d, 1000d, 44d }, { 400d, 800d, 800d, 1200d, 123d },
            { 800d, 550d, 1000d, 1500d, 650d },
            { 1600d, 600d, 1200d, 400d, 990d } };

    protected File getSampleFile(String filename) {
        return new File(sampleFileDiretory + File.separator + filename);
    }

    protected Chart getChartFromSampleFile(String filename, String cell)
            throws Exception {

        Spreadsheet spreadsheet = TestHelper.createSpreadsheet(filename);

        CellReference cellRef = new CellReference(cell);

        Set<SheetOverlayWrapper> sheetOverlays = getSheetOverlays(spreadsheet);

        for (SheetOverlayWrapper wrapper : sheetOverlays) {
            if (wrapper instanceof SheetChartWrapper) {
                SheetChartWrapper chartWrapper = (SheetChartWrapper) wrapper;
                if (chartWrapper.getAnchor().getCol1() == cellRef.getCol()
                        && chartWrapper.getAnchor().getRow1() == cellRef
                                .getRow()) {
                    return getChart(chartWrapper);
                }
            }
        }

        throw new Exception("Chart not found in file");
    }

    private Chart getChart(SheetChartWrapper chartWrapper) throws Exception {
        Method getComponent = chartWrapper.getClass().getMethod("getComponent",
                boolean.class);

        Object minimizableWrapper = getComponent.invoke(chartWrapper, true);

        Method method = minimizableWrapper.getClass().getMethod("getContent");
        method.setAccessible(true);

        Chart chart = (Chart) method.invoke(minimizableWrapper);

        return chart;
    }

    @SuppressWarnings("unchecked")
    private Set<SheetOverlayWrapper> getSheetOverlays(Spreadsheet spreadsheet)
            throws NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        Field f = spreadsheet.getClass().getDeclaredField("sheetOverlays");
        f.setAccessible(true);
        return (Set<SheetOverlayWrapper>) f.get(spreadsheet);
    }

    protected void assertData(List<Series> seriesList, Number[] data) {
        Assert.assertEquals("The number of series is not correct", data.length,
                ((DataSeries) seriesList.get(0)).getData().size());
        for (int i = 0; i < data.length; i++) {
            final Number y = ((DataSeries) seriesList.get(0)).getData().get(i)
                    .getY();
            if (data[i] != null) {
                Assert.assertEquals((Double) y, data[i].doubleValue(), 0.1);
            } else {
                Assert.assertNull(y);
            }
        }
    }

    protected void assertData(List<Series> seriesList, Number[][] data) {
        Assert.assertEquals("The number of series is not correct", data.length,
                seriesList.size());
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                final Number y = ((DataSeries) seriesList.get(i)).getData()
                        .get(j).getY();

                if (data[i][j] != null) {
                    Assert.assertEquals((Double) y, data[i][j].doubleValue(),
                            0.1);
                } else {
                    Assert.assertNull(y);
                }
            }
        }
    }

    protected void assertDataXY(DataSeries series, Number[][] data) {
        for (int i = 0; i < data.length; i++) {
            final Number y = series.getData().get(i).getY();
            final Number x = series.getData().get(i).getX();

            if (data[i] != null) {
                Assert.assertEquals((Double) x, data[0][i].doubleValue(), 0.1);
                Assert.assertEquals((Double) y, data[1][i].doubleValue(), 0.1);
            } else {
                Assert.assertNull(y);
            }
        }
    }

    protected void assert3dEnabled(Configuration conf) {
        Assert.assertTrue("3D should be enabled for this chart",
                conf.getChart().getOptions3d().getEnabled());
    }

    protected void assertSeriesType(List<Series> seriesList, ChartType type) {
        for (Series series : seriesList) {
            assertSingleSeriesType(series, type);
        }
    }

    protected void assertSingleSeriesType(Series series, ChartType type) {
        Assert.assertEquals("Wrong series type", type,
                series.getPlotOptions().getChartType());
    }

    protected void assertStacking(List<Series> seriesList, Stacking stacking) {
        for (Series series : seriesList) {
            Stacking actualStacking = null;

            AbstractPlotOptions plotOptions = series.getPlotOptions();
            if (plotOptions instanceof PlotOptionsColumn) {
                actualStacking = ((PlotOptionsColumn) plotOptions)
                        .getStacking();
            } else if (plotOptions instanceof PlotOptionsArea) {
                actualStacking = ((PlotOptionsArea) plotOptions).getStacking();
            } else if (plotOptions instanceof PlotOptionsBar) {
                actualStacking = ((PlotOptionsBar) plotOptions).getStacking();
            }

            Assert.assertEquals(
                    "Stacking type is wrong for series "
                            + seriesList.indexOf(series),
                    stacking, actualStacking);
        }
    }

    protected void assertDashStyle(List<Series> seriesList,
            DashStyle... dashStyles) {
        int index = 0;
        for (Series series : seriesList) {
            var plotOptions = series.getPlotOptions();
            DashStyle actualDashStyle = null;
            if (plotOptions instanceof PlotOptionsArea) {
                actualDashStyle = ((PlotOptionsArea) plotOptions)
                        .getDashStyle();
            } else if (plotOptions instanceof PlotOptionsLine) {
                actualDashStyle = ((PlotOptionsLine) plotOptions)
                        .getDashStyle();
            } else if (plotOptions instanceof PlotOptionsScatter) {
                actualDashStyle = ((PlotOptionsScatter) plotOptions)
                        .getDashStyle();
            }

            System.out.println("Dash style is " + actualDashStyle);

            Assert.assertEquals(
                    "Dash style is wrong for series "
                            + seriesList.indexOf(series),
                    dashStyles[Math.min(dashStyles.length - 1, index++)],
                    actualDashStyle);
        }
    }

}
