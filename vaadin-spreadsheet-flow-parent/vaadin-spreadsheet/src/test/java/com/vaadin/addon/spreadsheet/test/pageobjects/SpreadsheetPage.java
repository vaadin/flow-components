package com.vaadin.addon.spreadsheet.test.pageobjects;

import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.addon.spreadsheet.elements.AddressUtil;
import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.By;

public class SpreadsheetPage extends Page {

    public static final String BACKGROUND_COLOR = "background-color";
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
        return $(SpreadsheetElement.class).first().getCellAt(row, col)
                .isCellSelected();
    }

    public boolean isCellSelected(String address) {
        Point point = AddressUtil.addressToPoint(address);
        return isCellSelected(point.getX(), point.getY());
    }

    public SheetCellElement getCellAt(int col, int row) {
        return $(SpreadsheetElement.class).first().getCellAt(row, col);
    }

    public void clickOnCell(int col, int row) {
        getCellAt(col, row).click();
    }

    public void clickOnCell(String address) {
        $(SpreadsheetElement.class).first().getCellAt(address).click();
    }

    public void clickOnCell(String address, Keys... modifiers) {
        WebElement cell = getCellAt(address);
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
        WebElement fromCell = getCellAt(from);
        WebElement toCell = getCellAt(to);

        new Actions(driver).dragAndDrop(fromCell, toCell).build().perform();
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
    
    public void setCellValue(String address, String newValue) {
        getCellAt(address).setValue(newValue);
    }
    
    public String getCellColor(String cellAddress) {
        SheetCellElement cellAt = getCellAt(cellAddress);
        return cellAt.getCssValue(BACKGROUND_COLOR);
    }

    public boolean isCellActiveWithinSelection(String address) {
        SheetCellElement cell = getCellAt(address);
        return cell.isCellSelected()
                && !cell.getAttribute("class").contains("cell-range");
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
    
    public void deleteCellValue(String cellAddress){
        clickOnCell(cellAddress);
        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();
    }

    public String getFormulaFieldValue() {
        return getFormulaField().getAttribute("value");
    }

    public void selectSheetAt(int sheetIndex) {
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.selectSheetAt(sheetIndex);
    }
    
    private SheetCellElement getCellAt(String address) {
        Point point = AddressUtil.addressToPoint(address);
        return getCellAt(point.getX(), point.getY());
    }
}
