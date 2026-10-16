/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;

/**
 * A workbook with something of every kind in every pane, for
 * {@link FreshLoadHarness}: values, formulas, styles, a comment, links, a
 * merge, a hidden row with a comment, a filter with its pop-up buttons,
 * conditional formatting, a named range, a picture, and cells with components
 * and editors.
 * <p>
 * Sheet "First" is frozen at two rows and two columns, and the client shows
 * rows 10-20 of columns E-H. Sheet "Second" holds what a formula on "First"
 * reads.
 */
final class FreshLoadFixture {

    /** Rows 10-20 of columns E-H. */
    static final FreshLoadHarness.Viewport VIEWPORT = new FreshLoadHarness.Viewport(
            10, 5, 20, 8);

    /** The cells with components, and with editors. */
    static final Set<String> COMPONENTS = Set.of("H13");
    static final Set<String> EDITORS = Set.of("H14");

    private FreshLoadFixture() {
    }

    /** Sets a spreadsheet up as all the spreadsheets compared are. */
    static void configure(Spreadsheet spreadsheet) {
        spreadsheet.setLocale(Locale.US);
        spreadsheet.setSpreadsheetComponentFactory(new Factory());
    }

    static XSSFWorkbook workbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet first = workbook.createSheet("First");
        Sheet second = workbook.createSheet("Second");
        first.createFreezePane(2, 2);

        set(first, "A1", "corner");
        set(first, "F1", "top");
        set(first, "A12", "left");
        set(first, "E10", 1);
        set(first, "F10", 2);
        cell(first, "G10").setCellFormula("E10+F10");
        cell(first, "H10").setCellFormula("Second!A1*2");

        CellStyle percent = workbook.createCellStyle();
        percent.setDataFormat(workbook.getCreationHelper().createDataFormat()
                .getFormat("0%"));
        set(first, "E11", 0.25).setCellStyle(percent);
        set(first, "F11", "text");
        CellStyle bold = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        bold.setFont(boldFont);
        set(first, "G11", "bold").setCellStyle(bold);
        CellStyle filled = workbook.createCellStyle();
        filled.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        filled.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        set(first, "H11", "filled").setCellStyle(filled);

        CreationHelper helper = workbook.getCreationHelper();
        Drawing<?> drawing = first.createDrawingPatriarch();
        ClientAnchor commentAnchor = helper.createClientAnchor();
        commentAnchor.setRow1(11);
        commentAnchor.setCol1(4);
        commentAnchor.setRow2(13);
        commentAnchor.setCol2(6);
        Comment comment = drawing.createCellComment(commentAnchor);
        comment.setString(helper.createRichTextString("note"));
        comment.setAuthor("author");
        set(first, "E12", "commented").setCellComment(comment);
        Hyperlink link = helper.createHyperlink(HyperlinkType.URL);
        link.setAddress("https://vaadin.com");
        set(first, "F12", "link").setHyperlink(link);
        cell(first, "G12").setCellFormula(
                "HYPERLINK(\"https://vaadin.com/docs\",\"docs\")");

        CellStyle bordered = workbook.createCellStyle();
        bordered.setBorderBottom(BorderStyle.THIN);
        set(first, "E13", "bordered").setCellStyle(bordered);

        set(first, "E15", "merged");
        first.addMergedRegion(CellRangeAddress.valueOf("E15:F16"));

        set(first, "E18", "hidden row");
        first.getRow(17).setZeroHeight(true);
        comment(first, drawing, "E18", "on a hidden row");
        set(first, "G19", "noted");
        comment(first, drawing, "G19", "in column G");

        set(first, "E19", "Name");
        set(first, "F19", "Value");
        set(first, "E20", "a");
        set(first, "F20", 3);
        first.setAutoFilter(CellRangeAddress.valueOf("E19:F20"));

        SheetConditionalFormatting formatting = first
                .getSheetConditionalFormatting();
        ConditionalFormattingRule rule = formatting
                .createConditionalFormattingRule(ComparisonOperator.GT, "5");
        rule.createFontFormatting()
                .setFontColorIndex(IndexedColors.RED.getIndex());
        formatting.addConditionalFormatting(
                new CellRangeAddress[] { CellRangeAddress.valueOf("E10:H20") },
                rule);

        Name total = workbook.createName();
        total.setNameName("Total");
        total.setRefersToFormula("First!$G$10");

        ClientAnchor pictureAnchor = helper.createClientAnchor();
        pictureAnchor.setRow1(14);
        pictureAnchor.setCol1(6);
        pictureAnchor.setRow2(16);
        pictureAnchor.setCol2(8);
        drawing.createPicture(pictureAnchor,
                workbook.addPicture(png(), Workbook.PICTURE_TYPE_PNG));

        set(second, "A1", 21);
        set(second, "B2", "second");
        return workbook;
    }

    private static void comment(Sheet sheet, Drawing<?> drawing, String name,
            String text) {
        CreationHelper helper = sheet.getWorkbook().getCreationHelper();
        CellReference reference = new CellReference(name);
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setRow1(reference.getRow());
        anchor.setCol1(reference.getCol());
        anchor.setRow2(reference.getRow() + 2);
        anchor.setCol2(reference.getCol() + 2);
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(helper.createRichTextString(text));
        comment.setAuthor("author");
        cell(sheet, name).setCellComment(comment);
    }

    static Cell cell(Sheet sheet, String name) {
        CellReference reference = new CellReference(name);
        Row row = sheet.getRow(reference.getRow());
        if (row == null) {
            row = sheet.createRow(reference.getRow());
        }
        Cell cell = row.getCell(reference.getCol());
        return cell == null ? row.createCell(reference.getCol()) : cell;
    }

    private static Cell set(Sheet sheet, String name, Object value) {
        Cell cell = cell(sheet, name);
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue((String) value);
        }
        return cell;
    }

    static byte[] png() {
        BufferedImage image = new BufferedImage(4, 4,
                BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    /** Gives the cells of {@link #COMPONENTS} and {@link #EDITORS} theirs. */
    static final class Factory implements SpreadsheetComponentFactory {
        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            return "First".equals(sheet.getSheetName()) && COMPONENTS.contains(
                    new CellReference(rowIndex, columnIndex).formatAsString())
                            ? new Span("component")
                            : null;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            return "First".equals(sheet.getSheetName()) && EDITORS.contains(
                    new CellReference(rowIndex, columnIndex).formatAsString())
                            ? new Span("editor")
                            : null;
        }

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
        }
    }
}
