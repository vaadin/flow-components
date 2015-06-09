package com.vaadin.addon.spreadsheet.test.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SheetSelection extends Page {

    private final SpreadsheetPage spreadsheetPage;
    private WebElement sTop;
    private WebElement sBottom;
    private WebElement sLeft;
    private WebElement sRight;
    private Point sLocation;
    private Dimension sSize;

    public SheetSelection(WebDriver driver, SpreadsheetPage spreadsheetPage) {
        super(driver);
        this.spreadsheetPage = spreadsheetPage;
        findSelectionOutline();
    }

    public void findSelectionOutline() {
        // sometimes the spreadsheet takes so long to load that the selection
        // widget elements are not found
        new WebDriverWait(getDriver(), 15).until(ExpectedConditions
                .presenceOfElementLocated(By.className("s-top")));
        sTop = driver.findElement(By.className("s-top"));
        sBottom = driver.findElement(By.className("s-bottom"));
        sLeft = driver.findElement(By.className("s-left"));
        sRight = driver.findElement(By.className("s-right"));
    }

    public boolean isElementSelected(WebElement element) {
        updateSelectionLocationAndSize();
        Point location = element.getLocation();
        location.x += element.getSize().getWidth() / 2;
        location.y += element.getSize().getHeight() / 2;
        return isInSelection(location) || isNonCoherentlySelected(element);
    }

    private boolean isNonCoherentlySelected(WebElement element) {
        // an element is non-coherently selected if the background color is
        // rgba(224, 245, 255, 0.8) or
        // if it has a solid outline style
        return "rgba(224, 245, 255, 0.8)".equals(element
                .getCssValue("background-color"))
                || "solid".equals(element.getCssValue("outline-style"));
    }

    private void updateSelectionLocationAndSize() {
        if (sTop == null) {
            findSelectionOutline();
        }
        sLocation = sTop.getLocation();
        int bottomY = sBottom.getLocation().getY();
        int bottomH = sBottom.getSize().getHeight();
        int rightX = sRight.getLocation().getX();
        int rightW = sRight.getSize().getWidth();
        sSize = new Dimension(rightX + rightW - sLocation.getX(), bottomY
                + bottomH - sLocation.getY());
    }

    private boolean intersectsSelection(Point location, Dimension size) {
        // Test top left corner
        if (location.getX() < sLocation.getX()
                || location.getY() < sLocation.getY()) {
            return false;
        }
        // Test lower right corner
        if (location.getX() + size.getWidth() > sLocation.getX()
                + sSize.getWidth()
                || location.getY() + size.getHeight() > sLocation.getY()
                        + sSize.getHeight()) {
            return false;
        }
        // Everything is inside the selection
        return true;
    }

    private boolean isInSelection(Point location) {
        if (location.getX() < sLocation.getX()
                || location.getY() < sLocation.getY()) {
            return false;
        }
        if (location.getX() - sLocation.getX() > sSize.getWidth()
                || location.getY() - sLocation.getY() > sSize.getHeight()) {
            return false;
        }
        // Everything is inside the selection
        return true;
    }

}
