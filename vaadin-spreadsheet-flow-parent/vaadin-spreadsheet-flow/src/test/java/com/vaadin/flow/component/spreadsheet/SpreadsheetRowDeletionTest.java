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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * Deleting rows: their cells go, with their links and comments, and what reads
 * the sheet afterwards, conditional formatting among it, does not trip over
 * them.
 */
class SpreadsheetRowDeletionTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    static Object[] formats() {
        return new Object[] { (Supplier<Workbook>) XSSFWorkbook::new,
                (Supplier<Workbook>) HSSFWorkbook::new };
    }

    private Workbook workbook;
    private Sheet sheet;
    private Spreadsheet spreadsheet;

    /** Row 2 holds a value, a link and a comment; row 3 a value. */
    private void show(Supplier<Workbook> format) {
        workbook = format.get();
        sheet = workbook.createSheet();
        CreationHelper helper = workbook.getCreationHelper();
        Cell linked = sheet.createRow(1).createCell(0);
        linked.setCellValue(7);
        Hyperlink link = helper.createHyperlink(HyperlinkType.URL);
        link.setAddress("https://vaadin.com");
        linked.setHyperlink(link);
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setRow1(1);
        anchor.setCol1(1);
        anchor.setRow2(3);
        anchor.setCol2(3);
        Comment comment = sheet.createDrawingPatriarch()
                .createCellComment(anchor);
        comment.setString(helper.createRichTextString("note"));
        Cell commented = sheet.getRow(1).createCell(1);
        commented.setCellValue("commented");
        commented.setCellComment(comment);
        sheet.createRow(2).createCell(0).setCellValue(9);

        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 20, "
                        + "\"lastColumn\": 10}");
    }

    private void formatValuesOver(int threshold) {
        SheetConditionalFormatting formatting = sheet
                .getSheetConditionalFormatting();
        ConditionalFormattingRule rule = formatting
                .createConditionalFormattingRule(ComparisonOperator.GT,
                        String.valueOf(threshold));
        rule.createFontFormatting()
                .setFontColorIndex(IndexedColors.RED.getIndex());
        formatting.addConditionalFormatting(
                new CellRangeAddress[] { CellRangeAddress.valueOf("A1:A5") },
                rule);
    }

    @ParameterizedTest
    @MethodSource("formats")
    void rowsDeleted_onASheetWithConditionalFormatting_leaveTheOtherCellsShown(
            Supplier<Workbook> format) {
        show(format);
        formatValuesOver(5);

        spreadsheet.deleteRows(1, 1);
        ui.fakeClientCommunication();

        // The formatting gives every cell it covers a blank one to hang on.
        Cell deleted = sheet.getRow(1) == null ? null
                : sheet.getRow(1).getCell(0);
        assertTrue(deleted == null || deleted.getCellType() == CellType.BLANK);
        assertEquals(9, sheet.getRow(2).getCell(0).getNumericCellValue());
    }

    @ParameterizedTest
    @MethodSource("formats")
    void aCellMadeWhereADeletedCellHadALink_hasNoLink(
            Supplier<Workbook> format) {
        show(format);

        spreadsheet.deleteRows(1, 1);

        assertNull(sheet.createRow(1).createCell(0).getHyperlink());
    }

    @ParameterizedTest
    @MethodSource("formats")
    void theCommentsOfDeletedRows_go(Supplier<Workbook> format) {
        show(format);

        spreadsheet.deleteRows(1, 1);

        assertNull(sheet.getCellComment(new CellAddress("B2")));
    }
}
