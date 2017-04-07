package com.vaadin.addon.spreadsheet.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.ColorConverterUtil;
import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

public class XSSFColorConverterTest extends AbstractSpreadsheetTestCase {

    private static final String BACKGROUND_COLOR = "background-color";
    public static final String BORDER_RIGHT_COLOR = "border-right-color";
    private XSSFWorkbook workbook;
    private SpreadsheetPage spreadsheetPage;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        InputStream is = getClass()
            .getResourceAsStream("/test_sheets/wrong_color.xlsx");

        workbook = new XSSFWorkbook(is);
        spreadsheetPage = headerPage.loadFile("wrong_color.xlsx", this);
    }

    @Test
    public void customIndexedColor_compareForegroundColor_consistentColors() throws IOException {
        XSSFCell cell = workbook.getSheetAt(1).getRow(0).getCell(0);
        XSSFColor color = cell.getCellStyle().getFillForegroundColorColor();
        String indexedARGB = ColorConverterUtil.getIndexedARGB(workbook,color);

        assertNotNull(indexedARGB);

        String cssValue = spreadsheetPage.getCellAt(1, 1)
            .getCssValue(BACKGROUND_COLOR);

        assertNotNull(cssValue);

        // ignore some parts of the css to avoid failures such as
        // "Expected :rgba(232, 232, 232, 1.0);  Actual   :rgba(232, 232, 232, 1)"
        assertEquals(indexedARGB.substring(0,21), cssValue.substring(0,21));

    }

    @Test
    public void customIndexedColor_compareBorderColor_consistentColors() throws IOException {

        XSSFCell cell = workbook.getSheetAt(1).getRow(2).getCell(1);
        XSSFColor color = cell.getCellStyle().getBorderColor(
            XSSFCellBorder.BorderSide.RIGHT);
        String indexedARGB = ColorConverterUtil.getIndexedARGB(workbook,color);

        assertNotNull(indexedARGB);

        String cssValue = spreadsheetPage.getCellAt(2, 3)
            .getCssValue(BORDER_RIGHT_COLOR);

        assertNotNull(cssValue);

        // ignore some parts of the css to avoid failures such as
        // "Expected :rgba(232, 232, 232, 1.0);  Actual   :rgba(232, 232, 232, 1)"
        assertEquals(indexedARGB.substring(0,21), cssValue.substring(0,21));
    }


}