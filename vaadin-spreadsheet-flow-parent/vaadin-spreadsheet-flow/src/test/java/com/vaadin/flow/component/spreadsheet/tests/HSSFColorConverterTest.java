/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.HSSFColorConverter;

class HSSFColorConverterTest {

    private HSSFColorConverter converter;
    private HSSFCellStyle style;

    @BeforeEach
    void setUp() {
        var workbook = new HSSFWorkbook();
        converter = new HSSFColorConverter(workbook);
        style = workbook.createCellStyle();
        style.setLeftBorderColor(IndexedColors.RED.index);
        style.setTopBorderColor(IndexedColors.BRIGHT_GREEN.index);
        style.setRightBorderColor(IndexedColors.BLUE.index);
        style.setBottomBorderColor(IndexedColors.YELLOW.index);
    }

    @Test
    void getBorderColorCSS_givesEachSideItsOwnColor() {
        Assertions.assertEquals("left:#ff0000;",
                converter.getBorderColorCSS(BorderSide.LEFT, "left", style));
        Assertions.assertEquals("top:#00ff00;",
                converter.getBorderColorCSS(BorderSide.TOP, "top", style));
        Assertions.assertEquals("right:#0000ff;",
                converter.getBorderColorCSS(BorderSide.RIGHT, "right", style));
        Assertions.assertEquals("bottom:#ffff00;", converter
                .getBorderColorCSS(BorderSide.BOTTOM, "bottom", style));
    }

    @Test
    void getBorderColorCSS_givesNothingForASideACellHasNot() {
        Assertions.assertEquals("", converter
                .getBorderColorCSS(BorderSide.DIAGONAL, "diagonal", style));
    }
}
