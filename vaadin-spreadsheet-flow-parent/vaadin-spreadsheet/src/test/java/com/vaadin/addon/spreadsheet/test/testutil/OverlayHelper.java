package com.vaadin.addon.spreadsheet.test.testutil;


import org.apache.poi.ss.util.CellReference;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OverlayHelper extends SeleniumHelper {

    public OverlayHelper(WebDriver driver) {
        super(driver);
    }
    public WebElement getOverlayElement(String cell) {
        int[] coordinates = numericCoordinates(cell);

        WebElement element = driver.findElement(By
                .cssSelector(".sheet-image.col" + coordinates[0] + ".row"
                        + coordinates[1]));
        return element;
    }

    public int[] numericCoordinates(String cell) {
        CellReference cellReference = new CellReference(cell);
        return new int[] { cellReference.getCol() + 1,
                cellReference.getRow() + 1 };
    }
}
