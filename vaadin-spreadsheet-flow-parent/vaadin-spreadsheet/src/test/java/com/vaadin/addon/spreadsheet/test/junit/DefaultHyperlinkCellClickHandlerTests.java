package com.vaadin.addon.spreadsheet.test.junit;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.DefaultHyperlinkCellClickHandler;
import com.vaadin.addon.spreadsheet.Spreadsheet;

/**
 * Created by mtzukanov on 24.4.2017.
 */
public class DefaultHyperlinkCellClickHandlerTests {

    @Test
    public void hyperlinkParser_validStrings_correctParsed()throws Exception {
        URL testSheetResource = this.getClass().getClassLoader()
                .getResource("test_sheets/hyper_links.xlsx");
        File testSheetFile = new File(testSheetResource.toURI());

        Workbook workbook = WorkbookFactory.create(testSheetFile);

        final Spreadsheet ss = new Spreadsheet(workbook);
        ss.setActiveSheetIndex(0);

        TestHyperlinkCellClickHandler handler = new TestHyperlinkCellClickHandler(ss);

        // this tests the condition from #537, formula first argument is a cell
        // ref whose value is the link target
        assertEquals("#A3", handler.getFirstArgumentFromFormula(ss.getCell(0, 1)));
        assertEquals("https://www.google.com", handler.getFirstArgumentFromFormula(ss.getCell(1, 1)));
    }

    public static class TestHyperlinkCellClickHandler extends DefaultHyperlinkCellClickHandler {
        public TestHyperlinkCellClickHandler(Spreadsheet spreadsheet) {
            super(spreadsheet);
        }

        /**
         * @see com.vaadin.addon.spreadsheet.DefaultHyperlinkCellClickHandler#getFirstArgumentFromFormula(org.apache.poi.ss.usermodel.Cell)
         */
        @Override
        public String getFirstArgumentFromFormula(Cell cell) {
            return super.getFirstArgumentFromFormula(cell);
        }
    }
}
