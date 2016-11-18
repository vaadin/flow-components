package com.vaadin.addon.spreadsheet.test.demoapps;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

@SuppressWarnings("serial")
@Theme("demo")
@Widgetset("com.vaadin.addon.spreadsheet.Widgetset")
public class MultipleSpreadsheetUI extends UI {

    private List<XSSFCellStyle> wb1CellStyles = new ArrayList<XSSFCellStyle>();

    private List<XSSFCellStyle> wb2CellStyles = new ArrayList<XSSFCellStyle>();

    private Spreadsheet spreadsheet;

    @Override
    protected void init(VaadinRequest request) {
        setSizeFull();

        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();
        setContent(layout);

        final VerticalSplitPanel verticalSplitPanel = new VerticalSplitPanel();
        verticalSplitPanel.setSizeFull();
        layout.addComponent(verticalSplitPanel);
        layout.setExpandRatio(verticalSplitPanel, 1f);
        Button button = new Button("Add; second spreadsheet",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        Spreadsheet spreadsheet2 = new Spreadsheet(
                                createWorkbook());
                        verticalSplitPanel.setSecondComponent(spreadsheet2);

                        setupDemoSpreadsheet(spreadsheet2);

                        spreadsheet2.createFreezePane(2, 2);
                    }
                });

        layout.addComponent(button);
        layout.addComponent(new Button("change freeze panes",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        spreadsheet.createFreezePane(6, 6);
                    }
                }));
        layout.addComponent(new Button("remove freeze panes",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        spreadsheet.createFreezePane(0, 0);
                    }
                }));
        spreadsheet = new Spreadsheet(createWorkbook());
        spreadsheet.setSizeFull();
        verticalSplitPanel.addComponent(spreadsheet);

        setupDemoSpreadsheet(spreadsheet);

        spreadsheet.createFreezePane(1, 1);

    }

    protected Workbook createWorkbook() {

        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        xssfWorkbook.createSheet("sheet");

        List<Color> colors = null;

        List<XSSFCellStyle> cellStyles = null;
        if (wb1CellStyles.isEmpty()) {
            colors = Arrays.asList(Color.GREEN, Color.BLUE, Color.RED);
            cellStyles = wb1CellStyles;
        } else {
            wb2CellStyles.clear();
            colors = Arrays.asList(Color.YELLOW, Color.GRAY, Color.ORANGE);
            cellStyles = wb2CellStyles;
        }

        // Create some cell styles for this workbook
        for (Color color : colors) {
            XSSFCellStyle createCellStyle = xssfWorkbook.createCellStyle();
            createCellStyle.setFillBackgroundColor(new XSSFColor(color));
            XSSFFont createFont = xssfWorkbook.createFont();
            createCellStyle.setFont(createFont);
            createFont.setBold(true);
            cellStyles.add(createCellStyle);
        }

        return xssfWorkbook;

    }

    protected void setupDemoSpreadsheet(Spreadsheet spreadsheet) {

        spreadsheet.setSizeFull();

        List<XSSFCellStyle> cellStyles = null;
        long counter = 0;
        if (spreadsheet == this.spreadsheet) {
            cellStyles = wb1CellStyles;
        } else {
            // Start the counter from 10000 for the second spreadsheet to show
            // difference..
            counter = 10000;
            cellStyles = wb2CellStyles;
        }

        Random random = new Random();

        for (int y = 0; y < 100; y++) {
            XSSFCellStyle xssfCellStyle = cellStyles.get(random
                    .nextInt(cellStyles.size()));

            for (int x = 0; x < 100; x++) {
                Cell createCell = spreadsheet.createCell(y, x, counter++);
                createCell.setCellStyle(xssfCellStyle);
            }
        }

        spreadsheet.refreshAllCellValues();
    }
}