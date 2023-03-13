package com.vaadin.flow.component.spreadsheet.test;

import java.io.IOException;
import java.io.InputStream;

import com.vaadin.flow.component.spreadsheet.ColorConverterUtil;
import com.vaadin.flow.testutil.TestPath;

import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColors;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRgbColor;

@TestPath("vaadin-spreadsheet")
public class XSSFColorConverterIT extends AbstractSpreadsheetIT {

    private static final String BACKGROUND_COLOR = "background-color";
    public static final String BORDER_RIGHT_COLOR = "border-right-color";
    private XSSFWorkbook workbook;

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    public void loadWorkbook() throws IOException {
        InputStream is = getClass()
                .getResourceAsStream("/test_sheets/wrong_color.xlsx");

        workbook = new XSSFWorkbook(is);
        loadFile("wrong_color.xlsx");
    }

    @Test
    public void nullColor_openFile_noException() throws IOException {
        loadFile("null_color.xlsx");
        assertNoErrorIndicatorDetected();
    }

    @Test
    public void customIndexedColor_compareForegroundColor_consistentColors()
            throws IOException {
        loadWorkbook();
        XSSFCell cell = workbook.getSheetAt(1).getRow(0).getCell(0);
        XSSFColor color = cell.getCellStyle().getFillForegroundColorColor();
        String indexedARGB = getIndexedARGB(workbook, color);

        Assert.assertNotNull(indexedARGB);

        String cssValue = getCellAt(1, 1).getCssValue(BACKGROUND_COLOR);

        Assert.assertNotNull(cssValue);

        // ignore some parts of the css to avoid failures such as
        // "Expected :rgba(232, 232, 232, 1.0); Actual :rgba(232, 232, 232, 1)"
        Assert.assertEquals(indexedARGB.substring(0, 21),
                cssValue.substring(0, 21));

    }

    @Test
    public void customIndexedColor_compareBorderColor_consistentColors()
            throws IOException {
        loadWorkbook();
        XSSFCell cell = workbook.getSheetAt(1).getRow(2).getCell(1);
        XSSFColor color = cell.getCellStyle().getBorderColor(BorderSide.RIGHT);
        String indexedARGB = getIndexedARGB(workbook, color);

        Assert.assertNotNull(indexedARGB);

        String cssValue = getCellAt(2, 3).getCssValue(BORDER_RIGHT_COLOR);

        Assert.assertNotNull(cssValue);

        // ignore some parts of the css to avoid failures such as
        // "Expected :rgba(232, 232, 232, 1.0); Actual :rgba(232, 232, 232, 1)"
        Assert.assertEquals(indexedARGB.substring(0, 21),
                cssValue.substring(0, 21));
    }

    /**
     * for testing only - POI should do the proper color lookups now
     *
     * @param workbook
     * @param color
     * @return ARGB Hex
     */
    private static String getIndexedARGB(XSSFWorkbook workbook,
            XSSFColor color) {
        if (color.isIndexed() && hasCustomIndexedColors(workbook)) {
            try {
                StylesTable styleSource = workbook.getStylesSource();
                CTRgbColor ctRgbColor = styleSource.getCTStylesheet()
                        .getColors().getIndexedColors().getRgbColorList()
                        .get(color.getIndex());
                String rgb = ctRgbColor.getDomNode().getAttributes()
                        .getNamedItem("rgb").getNodeValue();
                return ColorConverterUtil.toRGBA(rgb);
            } catch (IndexOutOfBoundsException e) {
                return color.getARGBHex();
            }
        }
        return color.getARGBHex();
    }

    private static boolean hasCustomIndexedColors(XSSFWorkbook workbook) {
        StylesTable stylesSource = workbook.getStylesSource();
        CTColors ctColors = stylesSource.getCTStylesheet().getColors();
        if (ctColors == null) {
            return false;
        }
        if (ctColors.getIndexedColors() == null) {
            return false;
        }
        return true;
    }

}
