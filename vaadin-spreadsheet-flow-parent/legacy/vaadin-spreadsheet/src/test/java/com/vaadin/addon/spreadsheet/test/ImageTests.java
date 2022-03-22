package com.vaadin.addon.spreadsheet.test;

import org.junit.Test;
import org.openqa.selenium.By;

public class ImageTests extends AbstractSpreadsheetTestCase {

    @Test
    public void testFromUpload() {
        headerPage.loadFile("picture_sheet.xlsx", this);

        assertInRange(200, imageWidth("C2"), 260);

        assertInRange(240, imageWidth("G4"), 260);

        assertInRange(340, imageWidth("K2"), 360);

        assertInRange(15, imageWidth("R2"), 25);
    }

    public double imageWidth(String cell) {
        testBench(driver).waitForVaadin();
        return driver.findElement(
                By.xpath(sheetController.cellToXPath(cell) + "/img")).getSize().width;
    }
}
