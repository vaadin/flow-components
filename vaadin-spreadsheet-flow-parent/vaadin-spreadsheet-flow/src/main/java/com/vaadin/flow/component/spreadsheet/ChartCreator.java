/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
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
