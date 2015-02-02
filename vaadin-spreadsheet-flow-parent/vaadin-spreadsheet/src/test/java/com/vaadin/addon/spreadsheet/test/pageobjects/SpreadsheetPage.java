package com.vaadin.addon.spreadsheet.test.pageobjects;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.addon.spreadsheet.elements.AddressUtil;
import com.vaadin.testbench.By;

public class SpreadsheetPage extends Page {

    private final SheetSelection selection;

    public SpreadsheetPage(WebDriver driver) {
        super(driver);
        selection = new SheetSelection(driver, this);
    }

    public boolean isDisplayed() {
        try {
            return driver.findElement(By.className("v-spreadsheet")) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public String getAddressFieldValue() {
        return getAddressField().getAttribute("value");
    }

    private WebElement getAddressField() {
        return driver.findElement(By.cssSelector("input.addressfield"));
    }

    public boolean isCellSelected(int col, int row) {
        WebElement cell = getCellAt(col, row);
        return selection.isElementSelected(cell);
    }

    public boolean isCellSelected(String address) {
        Point point = AddressUtil.addressToPoint(address);
        return isCellSelected(point.getX(), point.getY());
    }

    public WebElement getCellAt(int col, int row) {
        return driver.findElement(By.cssSelector(String.format(".col%d.row%d",
                col, row)));
    }

    public void clickOnCell(int col, int row) {
        getCellAt(col, row).click();
    }

    public void clickOnCell(String address) {
        Point point = AddressUtil.addressToPoint(address);
        getCellAt(point.getX(), point.getY()).click();
    }

    public void clickOnCell(String address, Keys... modifiers) {
        Point point = AddressUtil.addressToPoint(address);
        WebElement cell = getCellAt(point.getX(), point.getY());
        Actions actions = new Actions(driver);
        actions.moveToElement(cell, 1, 1);
        for (Keys modifier : modifiers) {
            actions.keyDown(modifier);
        }
        actions.click();
        for (Keys modifier : modifiers) {
            actions.keyUp(modifier);
        }
        actions.build().perform();
    }

    public void clickOnCell(int column, int row, Keys... modifiers) {
        WebElement cell = getCellAt(column, row);
        Actions actions = new Actions(driver);
        actions.moveToElement(cell, 1, 1);
        for (Keys modifier : modifiers) {
            actions.keyDown(modifier);
        }
        actions.click();
        for (Keys modifier : modifiers) {
            actions.keyUp(modifier);
        }
        actions.build().perform();
    }

    public void setAddressFieldValue(String address) {
        WebElement addressField = getAddressField();
        addressField.clear();
        addressField.sendKeys(address + Keys.RETURN);
    }

    public void dragFromCellToCell(String from, String to) {
        Point fromPoint = AddressUtil.addressToPoint(from);
        Point toPoint = AddressUtil.addressToPoint(to);
        WebElement fromCell = getCellAt(fromPoint.getX(), fromPoint.getY());
        WebElement toCell = getCellAt(toPoint.getX(), toPoint.getY());

        new Actions(driver).dragAndDrop(fromCell, toCell).build().perform();
    }

    public int getScrollLeft() {
        WebElement sheet = driver.findElement(By.className("sheet"));
        Number scrollLeft = (Number) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].scrollLeft", sheet);
        return scrollLeft.intValue();
    }

    public int getScrollTop() {
        WebElement sheet = driver.findElement(By.className("sheet"));
        Number scrollLeft = (Number) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].scrollTop", sheet);
        return scrollLeft.intValue();
    }

    public void clickOnFormulaField() {
        getFormulaField().click();
    }

    private WebElement getFormulaField() {
        return driver.findElement(By.className("functionfield"));
    }

    public void setFormulaFieldValue(String value) {
        WebElement formulaField = getFormulaField();
        formulaField.clear();
        formulaField.sendKeys(value);
    }

    public String getCellValue(int col, int row) {
        return getCellAt(col, row).getText();
    }

    public String getCellValue(String address) {
        Point point = AddressUtil.addressToPoint(address);
        return getCellValue(point.getX(), point.getY());
    }

    public boolean isCellActiveWithinSelection(String address) {
        Point point = AddressUtil.addressToPoint(address);
        return "rgba(255,255,255,1)".equals(getCellAt(point.getX(),
                point.getY()).getCssValue("background-color").replace(" ", ""));
    }

    public void clickOnColumnHeader(String columnAddress, Keys... modifiers) {
        Point colPoint = AddressUtil.addressToPoint(columnAddress + "1");
        clickOnColumnHeader(colPoint.x, modifiers);
    }

    public void clickOnColumnHeader(int column, Keys... modifiers) {
        Actions actions = new Actions(driver);
        WebElement header = driver.findElement(By.cssSelector(".ch.col"
                + column));
        actions.moveToElement(header, 1, 1);
        for (Keys modifier : modifiers) {
            actions.keyDown(modifier);
        }
        actions.click(driver.findElement(By.cssSelector(".ch.col" + column)));
        for (Keys modifier : modifiers) {
            actions.keyUp(modifier);
        }
        actions.build().perform();
    }

    public void clickOnRowHeader(int row, Keys... modifiers) {
        Actions actions = new Actions(driver);
        actions.moveToElement(
                driver.findElement(By.cssSelector(".rh.row" + row)), 1, 1);
        for (Keys modifier : modifiers) {
            actions.keyDown(modifier);
        }
        actions.click(driver.findElement(By.cssSelector(".rh.row" + row)));
        for (Keys modifier : modifiers) {
            actions.keyUp(modifier);
        }
        actions.build().perform();
    }

    public String getFormulaFieldValue() {
        return getFormulaField().getAttribute("value");
    }
}
