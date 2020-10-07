package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("hidden-editor-buttons")
public class HiddenEditorButtonsIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id("editor-grid"));
        grid = $(GridElement.class).id("editor-grid");

        waitForElementNotPresent(By.id("editor"));
        waitForElementPresent(By.id("edit-1"));
        waitForElementNotPresent(By.id("save-1"));
        waitForElementNotPresent(By.id("cancel-1"));
    }

    @Test
    public void editItem_buttonsAreShown_confirmEdit_buttonsAreHidden() {
        TestBenchElement editButton = grid.findElement(By.id("edit-1"));
        Assert.assertNull(editButton.getAttribute("hidden"));
        editButton.click();

        waitForElementPresent(By.id("editor"));

        Assert.assertEquals("true", editButton.getAttribute("hidden"));
        waitForElementPresent(By.id("save-1"));
        waitForElementPresent(By.id("cancel-1"));

        TestBenchElement editor = grid.findElement(By.id("editor"));
        editor.sendKeys("234");
        TestBenchElement saveButton = grid.findElement(By.id("save-1"));
        Assert.assertNull(saveButton.getAttribute("hidden"));
        TestBenchElement cancelButton = grid.findElement(By.id("cancel-1"));
        Assert.assertNull(cancelButton.getAttribute("hidden"));
        saveButton.click();

        waitForElementNotPresent(By.id("editor"));

        editButton = grid.findElement(By.id("edit-1"));
        waitForElementNotPresent(By.id("save-1"));
        waitForElementNotPresent(By.id("cancel-1"));
        Assert.assertNull(editButton.getAttribute("hidden"));
    }

    @Test
    public void editItem_scrollAway_scrollBack_buttonsAreStillVisible() {
        TestBenchElement editButton = grid.findElement(By.id("edit-1"));
        Assert.assertNull(editButton.getAttribute("hidden"));
        editButton.click();

        waitForElementPresent(By.id("editor"));

        Assert.assertEquals("true", editButton.getAttribute("hidden"));
        waitForElementPresent(By.id("save-1"));
        waitForElementPresent(By.id("cancel-1"));

        grid.scrollToRow(1000);
        waitForElementPresent(By.id("edit-1000"));
        grid.scrollToRow(0);
        waitForElementPresent(By.id("save-1"));
        waitForElementPresent(By.id("cancel-1"));
        waitForElementPresent(By.id("editor"));

        TestBenchElement saveButton = grid.findElement(By.id("save-1"));
        Assert.assertNull(saveButton.getAttribute("hidden"));
        TestBenchElement cancelButton = grid.findElement(By.id("cancel-1"));
        Assert.assertNull(cancelButton.getAttribute("hidden"));
    }

    @Test
    public void editItem_scrollAway_editAnotherItem_scrollBack_buttonsAreHidden() {
        TestBenchElement editButton = grid.findElement(By.id("edit-1"));
        Assert.assertNull(editButton.getAttribute("hidden"));
        editButton.click();

        waitForElementPresent(By.id("editor"));

        Assert.assertEquals("true", editButton.getAttribute("hidden"));
        waitForElementPresent(By.id("save-1"));
        waitForElementPresent(By.id("cancel-1"));

        grid.scrollToRow(1000);
        waitForElementPresent(By.id("edit-1000"));
        editButton = grid.findElement(By.id("edit-1000"));
        Assert.assertNull(editButton.getAttribute("hidden"));
        editButton.click();

        Assert.assertEquals("true", editButton.getAttribute("hidden"));
        waitForElementPresent(By.id("save-1000"));
        waitForElementPresent(By.id("cancel-1000"));

        grid.scrollToRow(0);
        waitForElementPresent(By.id("edit-1"));
        waitForElementNotPresent(By.id("save-1"));
        waitForElementNotPresent(By.id("cancel-1"));
        waitForElementNotPresent(By.id("editor"));

        editButton = grid.findElement(By.id("edit-1"));
        Assert.assertNull(editButton.getAttribute("hidden"));
    }

}
