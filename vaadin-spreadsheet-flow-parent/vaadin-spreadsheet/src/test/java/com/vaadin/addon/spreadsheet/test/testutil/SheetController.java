package com.vaadin.addon.spreadsheet.test.testutil;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.addon.spreadsheet.test.SheetClicker;
import com.vaadin.addon.spreadsheet.test.tb3.AbstractTB3Test.BrowserUtil;
import com.vaadin.testbench.commands.CanWaitForVaadin;

public class SheetController implements SheetClicker {

    private final WebDriver driver;
    private final CanWaitForVaadin sleeper;
    private DesiredCapabilities desiredCapabilities;

    public SheetController(WebDriver driver, CanWaitForVaadin sleeper,
            DesiredCapabilities desiredCapabilities) {
        this.driver = driver;
        this.sleeper = sleeper;
        this.desiredCapabilities = desiredCapabilities;
    }

    public SheetController insertAndRet(CharSequence k) {
        action(k).action(Keys.RETURN);
        return this;
    }

    protected boolean fixLeftParenthis() {
        Platform platform = desiredCapabilities.getPlatform();
        return !((platform.is(Platform.MAC) || platform.is(Platform.XP))
                && desiredCapabilities.getBrowserName().equalsIgnoreCase(
                        "chrome") || desiredCapabilities.getBrowserName()
                .equalsIgnoreCase((BrowserUtil.ie(9).getBrowserName())));
    }

    public SheetController action(CharSequence k) {
        waitForVaadin();
        // Fix left parentheses with SHIFT+9
        if (k instanceof String && fixLeftParenthis()) {
            k = ((String) k).replace("(", Keys.chord(Keys.SHIFT, "9"));
        }

        new Actions(driver).sendKeys(k).build().perform();
        waitForVaadin();

        return this;
    }

    public String cellToXPath(String cell) {
        int[] coordinates = numericCoordinates(cell);

        // TODO - This will not work with multiple spreadsheets, add reference
        // to sheet's XPath
        return "//*["
                + "contains(concat(' ', normalize-space(@class), ' '), ' col"
                + coordinates[0]
                + " ')"
                + "and contains(concat(' ', normalize-space(@class), ' '), ' row"
                + coordinates[1] + " ')" + "]";
    }

    private int[] numericCoordinates(String cell) {
        String alpha = "A";
        String number = "1";
        ;
        for (int i = 0; i < cell.length(); i++) {
            if (cell.charAt(i) < 65) {
                alpha = cell.substring(0, i);
                number = cell.substring(i);
                break;
            }
        }

        int col = 0;
        for (int i = 0; i < alpha.length(); i++) {
            char h = alpha.charAt(i);
            col = (h - 'A' + 1) + (col * 26);
        }
        int row = Integer.parseInt(number);

        int[] coordinates = new int[] { col, row };
        return coordinates;
    }

    public By mergedCell(String topLeftCell) {
        int[] coordinates = numericCoordinates(topLeftCell);
        return By.xpath("//*[contains(@class,'col" + coordinates[0] + " row"
                + coordinates[1] + " merged-cell')]");
    }

    public By columnToXPath(String column) {
        return By.xpath("//*[@class='ch col" + (column.charAt(0) - 'A' + 1)
                + "']");
    }

    public By rowToXPath(String row) {
        return By.xpath("//*[@class='rh row" + row + "']");
    }

    public String getCellContent(String cell) {
        return getCellElement(cell).getText();
    }

    public String getMergedCellContent(String topLeftCell) {
        return driver.findElement(mergedCell(topLeftCell)).getText();
    }

    public String getCellStyle(String cell, String propertyName) {
        return getCellElement(cell).getCssValue(propertyName);
    }

    public WebElement getCellElement(String cell) {
        return driver.findElement(By.xpath(cellToXPath(cell)));
    }

    public SheetController putCellContent(String cell, CharSequence value) {
        selectCell(cell);
        insertAndRet(value);
        return this;
    }

    public SheetController insertColumn(String[] values) {
        for (String value : values) {
            insertAndRet(value);
        }
        return this;
    }

    public final void waitForVaadin() {
        sleeper.waitForVaadin();
    }

    protected WebDriver getDriver() {
        return driver;
    }

    public void selectCell(String cell) {
        waitForVaadin();
        clickCell(cell);
        // TODO - This will not work with multiple spreadsheets
        driver.findElement(By.xpath("//*[@class='sheet']")).sendKeys("");
        waitForVaadin();
    }

    @Override
    public void clickCell(String cell) {
        driver.findElement(By.xpath(cellToXPath(cell))).click();
        sleeper.waitForVaadin();
    }

    @Override
    public void clickColumn(String column) {
        clickElement(columnToXPath(column));
    }

    @Override
    public void clickRow(String row) {
        clickElement(rowToXPath(row));
    }

    public SheetController clickElement(org.openqa.selenium.By by) {
        driver.findElement(by).click();
        return this;
    }

    public void navigateToCell(String cell) {
        driver.findElement(By.xpath("//*[@class='addressfield']")).clear();
        driver.findElement(By.xpath("//*[@class='addressfield']")).sendKeys(
                cell);
        sleeper.waitForVaadin();
        new Actions(driver).sendKeys(Keys.RETURN).perform();
        sleeper.waitForVaadin();
    }

    public String getSelectedCell() {
        String elemClass = driver.findElement(
                By.cssSelector(".sheet-selection")).getAttribute("class");

        int rowStart = elemClass.indexOf("row");
        if (rowStart == -1) {
            return "A1";
        }

        int k = rowStart + "row".length();
        String rowNumber = "";
        while (k < elemClass.length()) {
            char digit = elemClass.charAt(k);
            if (digit == ' ') {
                break;
            }
            rowNumber += elemClass.charAt(k);
            k++;
        }

        int colStart = elemClass.indexOf("col");
        k = colStart + "col".length();
        String colNumberStr = "";
        while (k < elemClass.length()) {
            char digit = elemClass.charAt(k);
            if (digit == ' ') {
                break;
            }
            colNumberStr += elemClass.charAt(k);
            k++;
        }
        int colNumber = Integer.parseInt(colNumberStr);
        int dividend = colNumber;
        String columnName = "";
        int modulo;

        while (dividend > 0) {
            modulo = (dividend - 1) % 26;
            columnName = ((char) (65 + modulo)) + columnName;
            dividend = (dividend - modulo) / 26;
        }

        return columnName + rowNumber;
    }

    public void selectRegion(String from, String to) {
        new Actions(driver).clickAndHold(getCellElement(from))
                .release(getCellElement(to)).perform();
    }

}
