/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.XSSFColorConverter;

public class XSSFColorConverterTest {

    private Spreadsheet spreadsheet;
    private XSSFColorConverter converter;

    @Before
    public void setUp() {
        spreadsheet = new Spreadsheet();
        converter = new XSSFColorConverter(
                (XSSFWorkbook) spreadsheet.getWorkbook());
    }

    @Test
    public void getFontColorCSS_withIndexedColor() {
        var rule = createRule();
        var font = rule.createFontFormatting();
        font.setFontColorIndex(IndexedColors.RED.index);

        var cssColor = converter.getFontColorCSS(rule);

        Assert.assertEquals("rgba(255, 0, 0, 1.0);", cssColor);
    }

    @Test
    public void getFontColorCSS_withRgbColor() {
        var rule = createRule();
        var font = rule.createFontFormatting();
        var color = new XSSFColor(
                new byte[] { (byte) 255, (byte) 128, (byte) 64 });
        font.setFontColor(color);

        var cssColor = converter.getFontColorCSS(rule);

        Assert.assertEquals("rgba(255, 128, 64, 1.0);", cssColor);
    }

    @Test
    public void getBackgroundColorCSS_withIndexedColor() {
        var rule = createRule();
        var pattern = rule.createPatternFormatting();
        pattern.setFillBackgroundColor(IndexedColors.RED.index);

        var cssColor = converter.getBackgroundColorCSS(rule);

        Assert.assertEquals("rgba(255, 0, 0, 1.0);", cssColor);
    }

    @Test
    public void getBackgroundColorCSS_withRgbColor() {
        var rule = createRule();
        var pattern = rule.createPatternFormatting();
        var color = new XSSFColor(
                new byte[] { (byte) 255, (byte) 128, (byte) 64 });
        pattern.setFillBackgroundColor(color);

        var cssColor = converter.getBackgroundColorCSS(rule);

        Assert.assertEquals("rgba(255, 128, 64, 1.0);", cssColor);
    }

    private ConditionalFormattingRule createRule() {
        var conditionalFormatting = spreadsheet.getActiveSheet()
                .getSheetConditionalFormatting();
        return conditionalFormatting
                .createConditionalFormattingRule(ComparisonOperator.LT, "0");
    }
}
