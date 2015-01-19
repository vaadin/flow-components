package com.vaadin.addon.spreadsheet.test.fixtures;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class HyperLinkFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {

        spreadsheet
                .setHyperlinkCellClickHandler(new Spreadsheet.HyperlinkCellClickHandler() {

                    @Override
                    public void onHyperLinkCellClick(Cell cell,
                            Hyperlink hyperlink, Spreadsheet spreadsheet) {
                        if (hyperlink.getAddress() != null) {
                            spreadsheet.getUI().getPage()
                                    .open(hyperlink.getAddress(), "_new", true);
                            return;
                        }
                        if (cell.getRow().getRowNum() == 2
                                && cell.getColumnIndex() == 2) {
                            cell.setCellValue("new value");
                            spreadsheet.refreshAllCellValues();
                        }
                    }
                });

        Cell cell;
        Hyperlink link;
        CreationHelper helper;
        Sheet sheet;

        cell = spreadsheet.createCell(2, 1, "file link");
        sheet = cell.getSheet();
        helper = sheet.getWorkbook().getCreationHelper();
        link = helper.createHyperlink(Hyperlink.LINK_FILE);
        link.setAddress("/file-path");
        cell.setHyperlink(link);

        cell = spreadsheet.createCell(2, 2, "change me");
        link = helper.createHyperlink(Hyperlink.LINK_DOCUMENT);
        ((XSSFHyperlink) link).setTooltip("handled hyperlink");
        cell.setHyperlink(link);

        spreadsheet.refreshAllCellValues();
    }

}
