package com.example.application.views.demo.views;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class FormattingExample extends Div implements Spreadsheet.SelectionChangeListener {

    private final Spreadsheet spreadsheet;

    public FormattingExample() {
        setSizeFull();
        add(spreadsheet = createSpreadsheet());
    }

    private Spreadsheet createSpreadsheet() {
        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.addSelectionChangeListener(this);

        Font fontBoldExample = spreadsheet.getWorkbook().createFont();
        fontBoldExample.setBold(true);
        CellStyle fontBoldExampleStyle = spreadsheet.getWorkbook()
                .createCellStyle();
        fontBoldExampleStyle.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
        fontBoldExampleStyle.setFont(fontBoldExample);
        Cell fontExampleCell = spreadsheet.createCell(0, 0,
                "Click the 'B' button in the top left corner to toggle bold font on and off.");
        fontExampleCell.setCellStyle(fontBoldExampleStyle);

        CellStyle backgroundColorStyle = spreadsheet.getWorkbook()
                .createCellStyle();
        backgroundColorStyle.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
        Cell backgroundExampleCell = spreadsheet.createCell(2, 0,
                "Click the 'Background Color' button to select and change the background color of a cell.");
        backgroundExampleCell.setCellStyle(backgroundColorStyle);

        Font fontColorExample = spreadsheet.getWorkbook().createFont();
        fontColorExample.setColor(HSSFColor.HSSFColorPredefined.LIGHT_BLUE.getIndex());
        CellStyle fontColorExampleStyle = spreadsheet.getWorkbook()
                .createCellStyle();
        fontColorExampleStyle.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
        fontColorExampleStyle.setFont(fontColorExample);
        Cell fontColorExampleCell = spreadsheet.createCell(4, 0,
                "Click the 'Font Color' button to select and change the font color of a cell.");
        fontColorExampleCell.setCellStyle(fontColorExampleStyle);

        Cell cell;
        for (int i = 0; i <= 4; i = i + 2) {
            for (int j = 1; j <= 9; j++) {
                cell = spreadsheet.createCell(i, j, "");
                cell.setCellStyle(backgroundColorStyle);
            }
        }

        spreadsheet.refreshCells(fontExampleCell, backgroundExampleCell,
                fontColorExampleCell);
        return spreadsheet;
    }

    @Override
    public void onSelectionChange(Spreadsheet.SelectionChangeEvent event) {
        CellReference selectedCell = event.getSelectedCellReference();
        Cell cell = spreadsheet.getCell(selectedCell.getRow(),
                selectedCell.getCol());
        //backgroundColor.setValue(Color.WHITE);
        //fontColor.setValue(Color.BLACK);
        if (cell != null) {
            // This cast an only be done when using .xlsx files
            XSSFCellStyle style = (XSSFCellStyle) cell.getCellStyle();
            if (style != null) {
                XSSFFont font = style.getFont();
                if (font != null) {
                    XSSFColor xssfFontColor = font.getXSSFColor();
                    if (xssfFontColor != null) {
                        //fontColor.setValue(convertColor(xssfFontColor));
                    }
                }
                XSSFColor foregroundColor = style.getFillForegroundColorColor();
                if (foregroundColor != null) {
                    //backgroundColor.setValue(convertColor(foregroundColor));
                }
            }
        }
    }
}
