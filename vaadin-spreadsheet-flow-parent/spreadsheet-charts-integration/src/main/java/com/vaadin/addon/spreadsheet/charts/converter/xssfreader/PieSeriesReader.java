package com.vaadin.addon.spreadsheet.charts.converter.xssfreader;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTDoughnutChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPie3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieSer;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.PieSeriesData;

public class PieSeriesReader extends
        AbstractSeriesReader<CTPieSer, PieSeriesData> {

    private boolean isDoughnut = false;
    
    /**
     * In Excel donuts have only one exploded ring, this flag marks that we handled it
     */
    private boolean isExplodedDoughnutHandled = false;

    public PieSeriesReader(CTPieChart ctChart, Spreadsheet spreadsheet) {
        super(ctChart, spreadsheet);
    }

    public PieSeriesReader(CTDoughnutChart ctChart, Spreadsheet spreadsheet) {
        super(ctChart, spreadsheet);
        isDoughnut = true;
    }

    public PieSeriesReader(CTPie3DChart ctChart, Spreadsheet spreadsheet) {
        super(ctChart, spreadsheet, true);
    }

    @Override
    protected PieSeriesData createSeriesDataObject(CTPieSer serie) {
        return new PieSeriesData();
    }

    @Override
    protected void fillSeriesData(PieSeriesData seriesData, CTPieSer serie) {
        super.fillSeriesData(seriesData, serie);

        if (!isDoughnut || !isExplodedDoughnutHandled) {
            seriesData.isExploded = serie.isSetExplosion();
        }

        if (isDoughnut) {
            isExplodedDoughnutHandled = true;
            seriesData.isDonut = true;
            seriesData.donutHoleSizePercent = ((CTDoughnutChart) getChart())
                    .getHoleSize().getVal();
        }
    }
}
