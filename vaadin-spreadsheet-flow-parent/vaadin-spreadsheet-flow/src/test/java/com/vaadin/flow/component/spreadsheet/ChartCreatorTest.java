/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.charts.converter.DefaultChartCreator;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * The charts of a workbook are drawn by the chart creator an application sets,
 * in place of the default one.
 */
class ChartCreatorTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @AfterEach
    void restoreTheDefaultCreator() {
        // The creator is static: every other test draws with the default.
        SheetChartWrapper.setChartCreator(new DefaultChartCreator());
    }

    @Test
    void aChartOfTheWorkbook_isDrawnByTheCreatorTheApplicationSets() {
        List<Spreadsheet> askedBy = new ArrayList<>();
        List<Span> drawn = new ArrayList<>();
        SheetChartWrapper.setChartCreator((chartXml, spreadsheet) -> {
            askedBy.add(spreadsheet);
            Span chart = new Span(chartXml.getGraphicFrame().getName());
            drawn.add(chart);
            return chart;
        });
        Spreadsheet spreadsheet = TestHelper
                .createSpreadsheet("HiddenColumnChartTest.xlsx");
        ui.add(spreadsheet);

        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 100,"
                        + " \"lastColumn\": 30}");

        assertFalse(drawn.isEmpty(), "the creator drew the chart");
        assertSame(spreadsheet, askedBy.get(0));
        assertTrue(drawn.get(0).isAttached(), "what it drew is on the sheet");
        assertEquals(drawn.size(), askedBy.size());
    }
}
