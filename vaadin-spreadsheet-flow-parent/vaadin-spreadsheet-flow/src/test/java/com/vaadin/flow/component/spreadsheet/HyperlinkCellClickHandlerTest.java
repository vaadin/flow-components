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
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.Spreadsheet.HyperlinkCellClickHandler;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * A click on a link cell goes to the handler the application sets, with the
 * cell and its link, in place of the default handler.
 */
class HyperlinkCellClickHandlerTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void aClickOnALinkCell_reachesTheHandlerTheApplicationSets() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Cell cell = workbook.createSheet().createRow(1).createCell(2);
        cell.setCellValue("Vaadin");
        Hyperlink link = workbook.getCreationHelper()
                .createHyperlink(HyperlinkType.URL);
        link.setAddress("https://vaadin.com");
        cell.setHyperlink(link);
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
        List<Cell> clickedCells = new ArrayList<>();
        List<Hyperlink> clickedLinks = new ArrayList<>();
        spreadsheet
                .setHyperlinkCellClickHandler(new HyperlinkCellClickHandler() {
                    @Override
                    public void onHyperLinkCellClick(Cell clicked,
                            Hyperlink hyperlink) {
                        clickedCells.add(clicked);
                        clickedLinks.add(hyperlink);
                    }

                    @Override
                    public String getHyperlinkFunctionTarget(Cell clicked) {
                        return null;
                    }
                });

        // C2, which the client numbers from 1.
        TestHelper.fireClientEvent(spreadsheet, "linkCellClicked",
                "{\"row\": 2, \"column\": 3}");

        assertEquals(List.of(cell), clickedCells);
        assertEquals(1, clickedLinks.size());
        assertSame(cell.getHyperlink(), clickedLinks.get(0));
        assertEquals("https://vaadin.com", clickedLinks.get(0).getAddress());
    }
}
