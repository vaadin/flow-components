package com.vaadin.addon.spreadsheet.test.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

public class SpreadsheetCustomEditorsTest {

    private static class PublicSpreadsheet extends Spreadsheet {
        public PublicSpreadsheet(int i, int j) {
            super(i, j);
        }

        @Override
        public void onSheetScroll(int firstRow, int firstColumn, int lastRow,
                int lastColumn) {
            super.onSheetScroll(firstRow, firstColumn, lastRow, lastColumn);
        }
    }

    @Test
    public void cellAfterMergedRegion() throws Exception {
        // Test sheet setup:
        // X means non merged cell, m/M/mm are merged cell blocks
        // X M M X
        // m M M mm
        // m M M mm
        // X M M X
        PublicSpreadsheet ss = new PublicSpreadsheet(4, 4);
        ss.addMergedRegion(0, 1, 3, 2);
        ss.addMergedRegion(1, 0, 2, 0);
        ss.addMergedRegion(1, 3, 2, 3);
        ss.setSpreadsheetComponentFactory(new SpreadsheetComponentFactory() {

            @Override
            public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                    int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                    Component customEditor) {
            }

            @Override
            public Component getCustomEditorForCell(Cell cell, int rowIndex,
                    int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
                return null;
            }

            @Override
            public Component getCustomComponentForCell(Cell cell, int rowIndex,
                    int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
                return new Button(rowIndex + "," + columnIndex);
            }
        });

        // Must attach for Spreadsheet to work
        new TestableUI(ss);
        // Must fake a scroll event to update visible area and generate
        // components
        ss.onSheetScroll(1, 1, 4, 4);

        List<String> childComponentCaptions = new ArrayList<String>();
        Iterator<Component> iterator = ss.iterator();
        while (iterator.hasNext()) {
            childComponentCaptions.add(iterator.next().getCaption());
        }
        Collections.sort(childComponentCaptions);

        List<String> expected = new ArrayList<String>();
        expected.add("0,0");
        expected.add("3,0");
        expected.add("0,3");
        expected.add("3,3");
        // Merged cells
        expected.add("0,1");
        expected.add("1,0");
        expected.add("1,3");
        Collections.sort(expected);
        Assert.assertArrayEquals(expected.toArray(),
                childComponentCaptions.toArray());
    }
}
