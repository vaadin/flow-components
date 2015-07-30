package com.vaadin.addon.spreadsheet.test.pageobjects;

import static org.junit.Assert.assertEquals;

import org.openqa.selenium.WebDriver;

import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.ComboBoxElement;
import com.vaadin.testbench.elements.NativeSelectElement;

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

    public void loadTestFixture(TestFixtures fixture) {
        $(NativeSelectElement.class).id("fixtureSelect").selectByText(
                fixture.toString());
        $(ButtonElement.class).id("loadFixtureBtn").click();

        // sanity check
        assertEquals("Fixture not loaded correctly", fixture.toString(),
                $(NativeSelectElement.class).id("fixtureSelect").getValue());
    }

}
