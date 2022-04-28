package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;

import org.apache.poi.xssf.usermodel.XSSFChart;

import com.vaadin.flow.component.Component;

public interface ChartCreator extends Serializable {

    /**
     * Converts the XSSFChart model into a Chart Component
     *
     * @param chartXml
     *            metadata with the chart configuration
     * @param spreadsheet
     *            spreadsheet that chart uses as data source
     * @return
     */
    public Component createChart(XSSFChart chartXml, Spreadsheet spreadsheet);
}
