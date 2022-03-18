package com.example.application.views.demo.views;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class FormattingExample extends Div implements Spreadsheet.SelectionChangeListener {

    private final Spreadsheet spreadsheet;

    public FormattingExample() {
        setSizeFull();
        addClassName("formattingexample");
        add(createStyleToolbar());
        Div spreadsheetContainer = new Div();
        spreadsheetContainer.setSizeFull();
        spreadsheetContainer.add(spreadsheet = createSpreadsheet());
        add(spreadsheetContainer);
    }

    private HorizontalLayout createStyleToolbar() {
        HorizontalLayout stylingToolbar = new HorizontalLayout();
        //stylingToolbar.setSpacing(false);
        Button boldButton = new Button(new Icon(VaadinIcon.BOLD));
        boldButton.addClickListener(event -> updateSelectedCellsBold());

        stylingToolbar.add(boldButton);
        stylingToolbar.setVerticalComponentAlignment(FlexComponent.Alignment.END, boldButton);
        return stylingToolbar;
    }

    private void updateSelectedCellsBold() {
        if (spreadsheet != null) {
            List<Cell> cellsToRefresh = new ArrayList<>();
            for (CellReference cellRef : spreadsheet
                    .getSelectedCellReferences()) {
                // Obtain Cell using CellReference
                Cell cell = getOrCreateCell(cellRef);
                // Clone Cell CellStyle
                CellStyle style = cloneStyle(cell);
                // Clone CellStyle Font
                Font font = cloneFont(style);
                // Toggle current bold state
                font.setBold(!font.getBold());
                style.setFont(font);
                cell.setCellStyle(style);

                cellsToRefresh.add(cell);
            }
            // Update all edited cells
            spreadsheet.refreshCells(cellsToRefresh);
        }
    }

    private void updateSelectedCellsBackgroundColor(Color newColor) {
        if (spreadsheet != null && newColor != null) {
            List<Cell> cellsToRefresh = new ArrayList<>();
            for (CellReference cellRef : spreadsheet
                    .getSelectedCellReferences()) {
                // Obtain Cell using CellReference
                Cell cell = getOrCreateCell(cellRef);
                // Clone Cell CellStyle
                // This cast an only be done when using .xlsx files
                XSSFCellStyle style = (XSSFCellStyle) cloneStyle(cell);
                XSSFColor color = new XSSFColor(newColor);
                // Set new color value
                style.setFillForegroundColor(color);
                cell.setCellStyle(style);

                cellsToRefresh.add(cell);
            }
            // Update all edited cells
            spreadsheet.refreshCells(cellsToRefresh);
        }
    }

    private void updateSelectedCellsFontColor(Color newColor) {
        if (spreadsheet != null && newColor != null) {
            List<Cell> cellsToRefresh = new ArrayList<>();
            for (CellReference cellRef : spreadsheet
                    .getSelectedCellReferences()) {
                Cell cell = getOrCreateCell(cellRef);
                // Workbook workbook = spreadsheet.getWorkbook();
                XSSFCellStyle style = (XSSFCellStyle) cloneStyle(cell);
                XSSFColor color = new XSSFColor(newColor);
                XSSFFont font = (XSSFFont) cloneFont(style);
                font.setColor(color);
                style.setFont(font);
                cell.setCellStyle(style);
                cellsToRefresh.add(cell);
            }
            // Update all edited cells
            spreadsheet.refreshCells(cellsToRefresh);
        }
    }

    private Color convertColor(XSSFColor foregroundColor) {
        byte[] argb = foregroundColor.getARGB();
        return new Color(byteToInt(argb[1]), byteToInt(argb[2]),
                byteToInt(argb[3]), byteToInt(argb[0]));
    }

    private int byteToInt(byte byteValue) {
        return byteValue & 0xFF;
    }

    private Font cloneFont(CellStyle cellstyle) {
        Font newFont = spreadsheet.getWorkbook().createFont();
        Font originalFont = spreadsheet.getWorkbook()
                .getFontAt(cellstyle.getFontIndex());
        if (originalFont != null) {
            newFont.setBold(originalFont.getBold());
            newFont.setItalic(originalFont.getItalic());
            newFont.setFontHeight(originalFont.getFontHeight());
            newFont.setUnderline(originalFont.getUnderline());
            newFont.setStrikeout(originalFont.getStrikeout());
            // This cast an only be done when using .xlsx files
            XSSFFont originalXFont = (XSSFFont) originalFont;
            XSSFFont newXFont = (XSSFFont) newFont;
            newXFont.setColor(originalXFont.getXSSFColor());
        }
        return newFont;
    }


    private Cell getOrCreateCell(CellReference cellRef) {
        Cell cell = spreadsheet.getCell(cellRef.getRow(), cellRef.getCol());
        if (cell == null) {
            cell = spreadsheet.createCell(cellRef.getRow(), cellRef.getCol(),
                    "");
        }
        return cell;
    }

    private CellStyle cloneStyle(Cell cell) {
        CellStyle newStyle = spreadsheet.getWorkbook().createCellStyle();
        newStyle.cloneStyleFrom(cell.getCellStyle());
        return newStyle;
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
