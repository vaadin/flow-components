package com.vaadin.flow.component.spreadsheet.test;

import java.util.List;

import org.apache.poi.ss.util.CellReference;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OverlayHelper {
    private WebDriver driver;

    public OverlayHelper(WebDriver driver) {
        this.driver = driver;
    }

    public WebElement getOverlayElement(String cell) {
        int[] coordinates = numericCoordinates(cell);

        WebElement element = driver.findElement(By
                .cssSelector(".sheet-image.col" + coordinates[0] + ".row"
                        + coordinates[1]));
        return element;
    }

    public boolean isOverlayPresent(String cell) {
        int[] coordinates = numericCoordinates(cell);

        List<WebElement> elements = driver.findElements(By
                .cssSelector(".sheet-image.col" + coordinates[0] + ".row"
                        + coordinates[1]));
        return elements.size() > 0;
    }

    public int[] numericCoordinates(String cell) {
        CellReference cellReference = new CellReference(cell);
        return new int[] { cellReference.getCol() + 1,
                cellReference.getRow() + 1 };
    }
}
