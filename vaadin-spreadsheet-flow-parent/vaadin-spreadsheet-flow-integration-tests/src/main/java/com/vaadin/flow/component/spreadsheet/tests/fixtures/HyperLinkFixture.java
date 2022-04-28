package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;

public class HyperLinkFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {

        spreadsheet.setHyperlinkCellClickHandler(
                new Spreadsheet.HyperlinkCellClickHandler() {

                    @Override
                    public void onHyperLinkCellClick(Cell cell,
                            Hyperlink hyperlink) {
                        if (hyperlink.getAddress() != null) {
                            Notification.show(
                                    "spreadsheet.getUI().getPage().open(hyperlink.getAddress(), \"_new\", true);");
                            return;
                        }
                        if (cell.getRow().getRowNum() == 2
                                && cell.getColumnIndex() == 2) {
                            cell.setCellValue("new value");
                            spreadsheet.refreshAllCellValues();
                        }
                    }

                    @Override
                    public String getHyperlinkFunctionTarget(Cell cell) {
                        return "new value function";
                    }
                });

        Cell cell;
        Hyperlink link;
        CreationHelper helper;
        Sheet sheet;

        cell = spreadsheet.createCell(2, 1, "file link");
        sheet = cell.getSheet();
        helper = sheet.getWorkbook().getCreationHelper();
        link = helper.createHyperlink(HyperlinkType.FILE);
        link.setAddress("/file-path");
        cell.setHyperlink(link);

        cell = spreadsheet.createCell(2, 2, "change me");
        link = helper.createHyperlink(HyperlinkType.DOCUMENT);
        ((XSSFHyperlink) link).setTooltip("handled hyperlink");
        cell.setHyperlink(link);

        spreadsheet.refreshAllCellValues();
    }

}
