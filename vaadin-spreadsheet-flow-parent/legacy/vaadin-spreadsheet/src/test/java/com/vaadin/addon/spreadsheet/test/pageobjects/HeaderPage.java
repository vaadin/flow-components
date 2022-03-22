package com.vaadin.addon.spreadsheet.test.pageobjects;

import static org.junit.Assert.assertEquals;

import org.openqa.selenium.WebDriver;

import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.ComboBoxElement;
import com.vaadin.testbench.elements.NativeSelectElement;
import com.vaadin.testbench.elements.TextFieldElement;

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
        ComboBoxElement testSheetSelect = $(ComboBoxElement.class).id("testSheetSelect");
        testSheetSelect.selectByText(testSheetFilename);
        testSheetSelect.waitForVaadin();
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

    public void addFreezePane() {
        $(ButtonElement.class).caption("Freeze Pane").first().click();
        $(ButtonElement.class).caption("Submit values").first().click();
    }

    public void addFreezePane(int horizontalSplitPosition, int verticalSplitPosition) {
        $(ButtonElement.class).caption("Freeze Pane").first().click();
        $(TextFieldElement.class).caption("Vertical Split Position").first().setValue(String.valueOf(verticalSplitPosition));
        $(TextFieldElement.class).caption("Horizontal Split Position").first().setValue(String.valueOf(horizontalSplitPosition));
        $(ButtonElement.class).caption("Submit values").first().click();
    }

}
