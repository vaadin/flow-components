package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetHeaderElement;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-spreadsheet")
public class FreezePaneIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void addFreezePane_verticalAndHorizontal_firstHeaderIsPlacedCorrectly()
            throws Exception {
        createNewSpreadsheet();

        addFreezePane();

        SheetHeaderElement firstColumnHeader = getSpreadsheet()
                .getColumnHeader(1);
        SheetHeaderElement firstRowHeader = getSpreadsheet().getRowHeader(1);
        Assert.assertEquals("A", firstColumnHeader.getText());
        Assert.assertEquals("0px",
                firstColumnHeader.getWrappedElement().getCssValue("left"));
        Assert.assertEquals("1", firstRowHeader.getText());
        Assert.assertEquals("0px",
                firstRowHeader.getWrappedElement().getCssValue("top"));
    }

    @Test
    public void addFreezePane_onlyVertical_firstHeaderIsPlacedCorrectly()
            throws Exception {
        createNewSpreadsheet();

        addFreezePane(0, 1);

        SheetHeaderElement firstColumnHeader = getSpreadsheet()
                .getColumnHeader(1);
        SheetHeaderElement firstRowHeader = getSpreadsheet().getRowHeader(1);
        Assert.assertEquals("A", firstColumnHeader.getText());
        Assert.assertEquals("0px",
                firstColumnHeader.getWrappedElement().getCssValue("left"));
        Assert.assertEquals("1", firstRowHeader.getText());
        Assert.assertEquals("0px",
                firstRowHeader.getWrappedElement().getCssValue("top"));
    }

    @Test
    public void addFreezePane_onlyHorizontal_firstHeaderIsPlacedCorrectly()
            throws Exception {
        createNewSpreadsheet();

        addFreezePane(1, 0);

        SheetHeaderElement firstColumnHeader = getSpreadsheet()
                .getColumnHeader(1);
        SheetHeaderElement firstRowHeader = getSpreadsheet().getRowHeader(1);
        Assert.assertEquals("A", firstColumnHeader.getText());
        Assert.assertEquals("0px",
                firstColumnHeader.getWrappedElement().getCssValue("left"));
        Assert.assertEquals("1", firstRowHeader.getText());
        Assert.assertEquals("0px",
                firstRowHeader.getWrappedElement().getCssValue("top"));
    }

    @Test
    public void largeSheet_addFreezePane_verticalAndHorizontal_firstHeaderIsPlacedCorrectly()
            throws Exception {
        loadFile("100_000_rows.xlsx");

        addFreezePane();

        SheetHeaderElement firstColumnHeader = getSpreadsheet()
                .getColumnHeader(1);
        SheetHeaderElement firstRowHeader = getSpreadsheet().getRowHeader(1);
        Assert.assertEquals("A", firstColumnHeader.getText());
        Assert.assertEquals("0px",
                firstColumnHeader.getWrappedElement().getCssValue("left"));
        Assert.assertEquals("1", firstRowHeader.getText());
        Assert.assertEquals("0px",
                firstRowHeader.getWrappedElement().getCssValue("top"));
    }

}
