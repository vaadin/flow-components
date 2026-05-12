/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.util.GregorianCalendar;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SpreadsheetTheme;
import com.vaadin.flow.component.spreadsheet.SpreadsheetFilterTable;
import com.vaadin.flow.component.spreadsheet.framework.Action;
import com.vaadin.flow.component.spreadsheet.framework.Action.Handler;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("spreadsheet-dialog")
@PageTitle("Demo")
public class SpreadsheetDialogPage extends VerticalLayout {

    public SpreadsheetDialogPage() {
        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setTheme(SpreadsheetTheme.LUMO);
        spreadsheet.setHeight("400px");
        spreadsheet.createFreezePane(2, 1);
        spreadsheet.setSheetName(0, "First");

        Drawing<?> drawing = spreadsheet.getActiveSheet()
                .createDrawingPatriarch();
        CreationHelper factory = spreadsheet.getActiveSheet().getWorkbook()
                .getCreationHelper();

        ClientAnchor anchor = factory.createClientAnchor();
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(new XSSFRichTextString("First cell comment"));

        spreadsheet.createCell(0, 0, "cell").setCellComment(comment);

        // Define a cell style for dates
        CellStyle dateStyle = spreadsheet.getWorkbook().createCellStyle();
        DataFormat format = spreadsheet.getWorkbook().createDataFormat();
        dateStyle.setDataFormat(format.getFormat("yyyy-mm-dd"));

        // Add some data rows
        spreadsheet.createCell(1, 0, "Nicolaus");
        spreadsheet.createCell(1, 1, "Copernicus");
        spreadsheet.createCell(1, 2,
                new GregorianCalendar(1999, 2, 19).getTime());

        // Style the date cell
        spreadsheet.getCell(1, 2).setCellStyle(dateStyle);

        spreadsheet.createNewSheet("Second", 10, 10);

        int maxColumns = 5;
        int maxRows = 5;

        for (int column = 1; column < maxColumns + 1; column++) {
            spreadsheet.createCell(1, column, "Column " + column);
        }

        for (int row = 2; row < maxRows + 2; row++) {
            for (int col = 1; col < maxColumns + 1; col++) {
                spreadsheet.createCell(row, col, row + col);
            }
        }
        CellRangeAddress range = new CellRangeAddress(1, maxRows, 1,
                maxColumns);
        SpreadsheetFilterTable table = new SpreadsheetFilterTable(spreadsheet,
                range);
        spreadsheet.registerTable(table);
        spreadsheet.refreshAllCellValues();

        spreadsheet.setSelectionRange(2, 7, 2 + 5, 7 + 3);

        var action0 = new Action("Test", VaadinIcon.ABACUS.create());
        var action1 = new Action("Other", VaadinIcon.CARET_DOWN.create());
        spreadsheet.getContextMenuManager().addActionHandler(new Handler() {

            @Override
            public Action[] getActions(Object target, Object sender) {
                Action[] actions = new Action[2];
                actions[0] = action0;
                actions[1] = action1;
                return actions;
            }

            @Override
            public void handleAction(Action action, Object sender,
                    Object target) {
                if (action == action0) {
                    System.out.println("Test!");
                } else if (action == action1) {
                    System.out.println("Other!");
                }
            }

        });

        Dialog dialog = new Dialog();
        dialog.add(spreadsheet);
        dialog.setSizeFull();
        dialog.open();
    }

}
