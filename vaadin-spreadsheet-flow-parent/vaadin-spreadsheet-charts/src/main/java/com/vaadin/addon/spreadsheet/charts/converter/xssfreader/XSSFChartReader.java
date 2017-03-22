package com.vaadin.addon.spreadsheet.charts.converter.xssfreader;

/*
 * #%L
 * Vaadin Spreadsheet Charts Integration
 * %%
 * Copyright (C) 2016 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAreaChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBar3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBubbleChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDoughnutChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPie3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTUnsignedInt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTView3D;
import org.openxmlformats.schemas.drawingml.x2006.chart.STDispBlanksAs;
import org.openxmlformats.schemas.drawingml.x2006.chart.STLegendPos;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.charts.converter.Utils;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.AbstractSeriesData;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.AxisProperties;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.LegendPosition;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.LegendProperties;

@SuppressWarnings("serial")
public class XSSFChartReader {
    private final XSSFChart xssfChart;
    private final Spreadsheet spreadsheet;
    private final ChartStylesReader stylesReader;
    private LinkedHashMap<Long, AxisProperties> yAxes;

    public XSSFChartReader(Spreadsheet spreadsheet, XSSFChart xssfChart) {
        this.spreadsheet = spreadsheet;
        this.xssfChart = xssfChart;
        stylesReader = new ChartStylesReader(spreadsheet, xssfChart);
    }

    public ChartData readXSSFChart() {
        ChartData chartData = new ChartData();

        final CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();

        yAxes = stylesReader.getYAxisProperties();

        chartData.plotData = readPlotData(plotArea);

        chartData.title = getTitle(xssfChart, chartData.plotData);

        // read these parameters to
        final CTView3D view3d = xssfChart.getCTChart().getView3D();

        if (view3d != null) {
            chartData.view3dData = new ChartData.View3dData();
            if (view3d.getRotX() != null) {
                chartData.view3dData.rotation3dAngleA = view3d.getRotX()
                        .getVal();
            }
            if (view3d.getRotY() != null) {
                chartData.view3dData.rotation3dAngleB = view3d.getRotY()
                        .getVal();
            }
        }

        chartData.legendProperties = getLegendProperties();

        chartData.borderStyle = stylesReader.getBorderStyle();

        chartData.background = stylesReader.getBackgroundProperties();

        chartData.titleStyle = stylesReader.getTitleProperties();

        chartData.xAxisProperties = stylesReader.getXAxisProperties();

        // values() of LinkedHashMap guarantees the order of items
        chartData.yAxesProperties = new ArrayList<AxisProperties>(
                yAxes.values());

        chartData.blanksAsZeros = getDisplayBlanksAs();

        return chartData;
    }

    private boolean getDisplayBlanksAs() {
        try {
            STDispBlanksAs.Enum blanksAs = xssfChart.getCTChart()
                    .getDispBlanksAs().getVal();

            return blanksAs == STDispBlanksAs.ZERO;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private LegendProperties getLegendProperties() {
        LegendProperties result = new LegendProperties();

        result.position = getLegendPosition();

        result.textProperties = stylesReader.getLegendTextProperties();

        return result;
    }

    private LegendPosition getLegendPosition() {
        Map<STLegendPos.Enum, LegendPosition> translationMap = new HashMap<STLegendPos.Enum, LegendPosition>() {
            {
                put(STLegendPos.B, LegendPosition.BOTTOM);
                put(STLegendPos.L, LegendPosition.LEFT);
                put(STLegendPos.T, LegendPosition.TOP);
                put(STLegendPos.TR, LegendPosition.TOP_RIGHT);
                put(STLegendPos.R, LegendPosition.RIGHT);
            }
        };
        try {
            STLegendPos.Enum legendPos = xssfChart.getCTChart().getLegend()
                    .getLegendPos().getVal();

            return translationMap.get(legendPos);
        } catch (NullPointerException e) {
            return LegendPosition.NONE;
        }
    }

    private List<AbstractSeriesData> readPlotData(CTPlotArea plotArea) {
        boolean showDataInHiddenCells = getShowDataInHiddenCells();

        ArrayList<AbstractSeriesData> list = new ArrayList<AbstractSeriesData>();

        for (CTBarChart ctChart : plotArea.getBarChartList()) {
            list.addAll(addYAxis(new BarSeriesReader(ctChart, spreadsheet,
                    showDataInHiddenCells).getSeries(), getAxIdList(ctChart)));
        }

        for (CTBar3DChart ctChart : plotArea.getBar3DChartList()) {
            list.addAll(addYAxis(new BarSeriesReader(ctChart, spreadsheet,
                    showDataInHiddenCells).getSeries(), getAxIdList(ctChart)));
        }

        for (CTPieChart ctChart : plotArea.getPieChartList()) {
            list.addAll(new PieSeriesReader(ctChart, spreadsheet,
                    showDataInHiddenCells).getSeries());
        }

        for (CTPie3DChart ctChart : plotArea.getPie3DChartList()) {
            list.addAll(new PieSeriesReader(ctChart, spreadsheet,
                    showDataInHiddenCells).getSeries());
        }

        for (CTAreaChart ctChart : plotArea.getAreaChartList()) {
            list.addAll(addYAxis(new AreaSeriesReader(ctChart, spreadsheet,
                    showDataInHiddenCells).getSeries(), getAxIdList(ctChart)));
        }

        for (CTBubbleChart ctChart : plotArea.getBubbleChartList()) {
            list.addAll(addYAxis(new BubbleSeriesReader(ctChart, spreadsheet,
                    showDataInHiddenCells).getSeries(), getAxIdList(ctChart)));
        }

        for (CTLineChart ctChart : plotArea.getLineChartList()) {
            list.addAll(addYAxis(new LineSeriesReader(ctChart, spreadsheet,
                    showDataInHiddenCells).getSeries(), getAxIdList(ctChart)));
        }

        for (CTRadarChart ctChart : plotArea.getRadarChartList()) {
            list.addAll(addYAxis(new RadarSeriesReader(ctChart, spreadsheet,
                    showDataInHiddenCells).getSeries(), getAxIdList(ctChart)));
        }

        for (CTScatterChart ctChart : plotArea.getScatterChartList()) {
            list.addAll(addYAxis(new ScatterSeriesReader(ctChart, spreadsheet,
                    showDataInHiddenCells).getSeries(), getAxIdList(ctChart)));
        }

        for (CTDoughnutChart ctChart : plotArea.getDoughnutChartList()) {
            list.addAll(new PieSeriesReader(ctChart, spreadsheet,
                    showDataInHiddenCells).getSeries());
        }

        return list;
    }

    private boolean getShowDataInHiddenCells() {
        try {
            return !xssfChart.getCTChart().getPlotVisOnly().getVal();
        } catch (NullPointerException e) {
            return false;
        }
    }

    private Collection<? extends AbstractSeriesData> addYAxis(
            Collection<? extends AbstractSeriesData> seriesList,
            List<CTUnsignedInt> axIdList) {
        // keySet() of LinkedHashMap guarantees the order of items
        final List<Long> axisIds = new ArrayList<Long>(yAxes.keySet());

        for (Long axisId : axisIds) {
            for (CTUnsignedInt id : axIdList) {
                if (id.getVal() == axisId) {
                    for (AbstractSeriesData series : seriesList) {
                        series.yAxis = axisIds.indexOf(axisId);
                    }
                    break;
                }
            }
        }

        return seriesList;
    }

    @SuppressWarnings("unchecked")
    private List<CTUnsignedInt> getAxIdList(XmlObject ctChart) {
        return (List<CTUnsignedInt>) Utils.callMethodUsingReflection(ctChart,
                "getAxIdList");
    }

    private String getTitle(XSSFChart chart, List<AbstractSeriesData> plotData) {
        String title = "";

        final CTChart ctChart = chart.getCTChart();

        if (ctChart.isSetTitle()) {
            title = "" + chart.getTitle();

            // default title
            if (title.isEmpty() && plotData.size() > 0
                    && plotData.get(0).name != null) {
                title = plotData.get(0).name;
            }
        }

        return title;
    }
}
