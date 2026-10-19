/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.charts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.events.PointSelectEvent;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;

class ChartPointSelectTest extends ChartTestBase {

    @Test
    void selectingAPoint_selectsTheCellsOfItsSeries() throws Exception {
        Spreadsheet spreadsheet = TestHelper
                .createSpreadsheet("ColorByPoint.xlsx");
        Chart chart = getChart(spreadsheet, "D2");

        ComponentUtil.fireEvent(chart,
                new PointSelectEvent(chart, true, 0, "", 1, ""));

        Assertions.assertEquals("B2:B4", spreadsheet.getCellSelectionManager()
                .getSelectedCellRange().formatAsString());
    }
}
