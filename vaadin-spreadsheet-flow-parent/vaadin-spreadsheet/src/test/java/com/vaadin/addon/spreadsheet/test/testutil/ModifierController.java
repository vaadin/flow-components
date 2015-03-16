package com.vaadin.addon.spreadsheet.test.testutil;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.testbench.commands.CanWaitForVaadin;

public class ModifierController extends SheetController {

    private Keys modifier;

    public ModifierController(WebDriver driver, Keys modifier,
            CanWaitForVaadin sleeper, DesiredCapabilities desiredCapabilities) {
        super(driver, sleeper, desiredCapabilities);
        this.modifier = modifier;
    }

    @Override
    public void clickCell(String cell) {
        new Actions(getDriver()).keyDown(modifier).build().perform();
        super.clickCell(cell);
        new Actions(getDriver()).keyUp(modifier).build().perform();
    }

    @Override
    public void clickRow(int row) {
        new Actions(getDriver()).keyDown(modifier).build().perform();
        super.clickRow(row);
        new Actions(getDriver()).keyUp(modifier).build().perform();
    }

    @Override
    public void clickColumn(String column) {
        new Actions(getDriver()).keyDown(modifier).build().perform();
        super.clickColumn(column);
        new Actions(getDriver()).keyUp(modifier).build().perform();
    }
}
