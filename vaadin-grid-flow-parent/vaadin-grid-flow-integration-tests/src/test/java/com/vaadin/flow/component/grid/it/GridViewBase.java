
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.TabbedComponentDemoTest;

/**
 * Integration tests for the {@link GridView}.
 */
public class GridViewBase extends TabbedComponentDemoTest {

    static final String OVERLAY_TAG = "vaadin-context-menu-overlay";

    @Override
    protected String getTestPath() {
        return "/vaadin-grid-it-demo";
    }

    void assertElementHasFocus(WebElement element) {
        Assert.assertTrue("Element should have focus",
                (Boolean) executeScript(
                        "return document.activeElement === arguments[0]",
                        element));
    }

    GridElement assertCloseEditorUsingKeyBoard(String gridId) {
        openTabAndCheckForErrors("grid-editor");

        GridElement grid = $(GridElement.class).id(gridId);
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);

        row.doubleClick();

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        nameInput.click();

        nameInput.sendKeys(Keys.chord(Keys.SHIFT, Keys.TAB));

        assertNotBufferedEditorClosed(grid);

        GridColumnElement subscriberColumn = grid.getColumn("Subscriber");
        GridTHTDElement subscriberCell = row.getCell(subscriberColumn);

        row.doubleClick();

        TestBenchElement checkbox = subscriberCell.$("vaadin-checkbox").first();
        checkbox.click();

        checkbox.sendKeys(Keys.TAB);

        assertNotBufferedEditorClosed(grid);

        // restore the previous state
        row.doubleClick();

        checkbox = subscriberCell.$("vaadin-checkbox").first();
        checkbox.click();

        // close the editor
        grid.getRow(1).click(5, 5);

        return grid;
    }

    void assertNotBufferedEditorClosed(GridElement grid) {
        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTRElement row = grid.getRow(0);
        GridTHTDElement nameCell = row.getCell(nameColumn);
        Assert.assertEquals(
                "Unexpected shown text field in the name cell when the editor should be closed",
                0, nameCell.$("vaadin-text-field").all().size());
    }

    void assertBufferedEditing(GridElement grid) {
        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        nameInput.click();

        // Move caret to the end of the text
        new Actions(getDriver()).sendKeys(Keys.END).build().perform();

        nameInput.sendKeys("foo");

        // skip checkbox and focus the email field
        new Actions(getDriver()).sendKeys(Keys.TAB).sendKeys(Keys.TAB).build()
                .perform();

        // change the e-mail to .org
        new Actions(getDriver()).sendKeys(Keys.END, Keys.BACK_SPACE,
                Keys.BACK_SPACE, Keys.BACK_SPACE).sendKeys("org").build()
                .perform();

        // press enter on the save button
        new Actions(getDriver()).sendKeys(Keys.TAB).sendKeys(Keys.ENTER).build()
                .perform();

        WebElement updatedItemMsg = findElement(
                By.id("buffered-dynamic-editor-msg"));

        waitUntil(driver -> !updatedItemMsg.getText().isEmpty());

        Assert.assertEquals("Person 1foo, true, mailss@example.org",
                updatedItemMsg.getText());
    }
}
