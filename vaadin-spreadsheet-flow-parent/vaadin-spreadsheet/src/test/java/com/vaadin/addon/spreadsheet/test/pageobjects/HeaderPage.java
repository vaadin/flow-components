package com.vaadin.addon.spreadsheet.test.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

import com.vaadin.testbench.TestBenchTestCase;

public class HeaderPage extends Page {

    public HeaderPage(WebDriver driver) {
        super(driver);
    }

    public SpreadsheetPage createNewSpreadsheet() {
        buttonWithCaption("Create new").click();
        return new SpreadsheetPage(driver);
    }

    public SpreadsheetPage loadFile(String testSheetFilename,
            TestBenchTestCase tbtc) {
        tbtc.testBenchElement(
                driver.findElement(By.xpath("//*[@id='testSheetSelect']/input")))
                .click(10, 10);
        TestBenchTestCase.testBench(driver).waitForVaadin();
        driver.findElement(By.xpath("//*[@id='testSheetSelect']/input"))
                .clear();
        driver.findElement(By.xpath("//*[@id='testSheetSelect']/input"))
                .sendKeys(testSheetFilename);
        TestBenchTestCase.testBench(driver).waitForVaadin();

        driver.findElement(By.xpath("//*[@id='testSheetSelect']/input"))
                .sendKeys(Keys.RETURN);
        TestBenchTestCase.testBench(driver).waitForVaadin();

        driver.findElement(By.xpath("//*[@id='update']")).click();

        return new SpreadsheetPage(driver);
    }
}
