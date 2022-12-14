package com.vaadin.flow.component.spreadsheet.tests;

import static org.junit.Assert.*;

import org.apache.poi.ss.usermodel.Cell;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.DefaultHyperlinkCellClickHandler;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Created by mtzukanov on 24.4.2017.
 */
public class DefaultHyperlinkCellClickHandlerTests {

    @Test
    public void hyperlinkParser_validStrings_correctParsed() {
        final Spreadsheet ss = TestHelper.createSpreadsheet("hyper_links.xlsx");
        ss.setActiveSheetIndex(0);

        TestHyperlinkCellClickHandler handler = new TestHyperlinkCellClickHandler(
                ss);

        // this tests the condition from
        // https://github.com/vaadin/spreadsheet/pull/537,
        // formula first argument is a cell ref whose value is the link target
        assertEquals("#A3",
                handler.getFirstArgumentFromFormula(ss.getCell(0, 1)));
        assertEquals("https://www.google.com",
                handler.getFirstArgumentFromFormula(ss.getCell(1, 1)));
    }

    public static class TestHyperlinkCellClickHandler
            extends DefaultHyperlinkCellClickHandler {
        public TestHyperlinkCellClickHandler(Spreadsheet spreadsheet) {
            super(spreadsheet);
        }

        /**
         * @see com.vaadin.flow.component.spreadsheet.DefaultHyperlinkCellClickHandler#getFirstArgumentFromFormula(org.apache.poi.ss.usermodel.Cell)
         */
        @Override
        public String getFirstArgumentFromFormula(Cell cell) {
            return super.getFirstArgumentFromFormula(cell);
        }
    }
}
