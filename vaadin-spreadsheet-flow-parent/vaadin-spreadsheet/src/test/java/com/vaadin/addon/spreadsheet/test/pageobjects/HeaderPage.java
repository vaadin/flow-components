package com.vaadin.addon.spreadsheet.test.pageobjects;

import org.openqa.selenium.WebDriver;

import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.ComboBoxElement;

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
        $(ComboBoxElement.class).id("testSheetSelect").selectByText(
                testSheetFilename);
        $(ButtonElement.class).id("update").click();
        return new SpreadsheetPage(driver);
    }
}
