/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/grid-editor")
public class GridViewEditorIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void notBufferedEditor() {
        GridElement grid = $(GridElement.class).id("not-buffered-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);
        String personName = nameCell.getText();

        GridColumnElement subscriberColumn = grid.getColumn("Subscriber");

        GridTHTDElement subscriberCell = row.getCell(subscriberColumn);

        row.doubleClick();

        TestBenchElement subscriberCheckbox = subscriberCell
                .$("vaadin-checkbox").first();
        boolean isSubscriber = subscriberCheckbox
                .getAttribute("checked") != null;

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameInput);

        nameInput.sendKeys(Keys.END);
        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ENTER);

        subscriberCheckbox.click();

        // click on another row
        grid.getRow(1).click(10, 10);

        // New data should be shown in the grid cell
        Assert.assertEquals(personName + "foo", nameCell.getText());
        Assert.assertEquals(String.valueOf(!isSubscriber),
                subscriberCell.getText());

        // The edited person should have new data
        WebElement msg = findElement(By.id("not-buffered-editor-msg"));
        Assert.assertEquals(personName + "foo, " + !isSubscriber,
                msg.getText());
    }

    @Test
    public void notBufferedEditor_closeEditorUsingKeyboard() {
        assertCloseEditorUsingKeyBoard("not-buffered-editor");
    }

    @Test
    public void dynamicNotBufferedEditor_closeEditorUsingKeyboard()
            throws InterruptedException {
        GridElement grid = assertCloseEditorUsingKeyBoard(
                "not-buffered-dynamic-editor");

        GridTRElement row = grid.getRow(0);

        GridColumnElement emailColumn = grid.getColumn("E-mail");
        GridTHTDElement emailCell = row.getCell(emailColumn);

        row.doubleClick();

        TestBenchElement emailField = emailCell.$("vaadin-text-field").first();

        TestBenchElement emailInput = emailField.$("input").first();
        emailInput.click();
        emailInput.sendKeys(Keys.TAB);
        assertNotBufferedEditorClosed(grid);
    }

    @Test
    public void dynamicEditor_bufferedMode_useKeyboardToSwitchEditorComponent() {

        GridElement grid = $(GridElement.class).id("buffered-dynamic-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        // start to edit
        GridColumnElement editColumn = grid.getAllColumns().get(3);
        grid.getRow(0).getCell(editColumn).$("vaadin-button").first().click();

        assertBufferedEditing(grid);
    }

    @Test
    public void bufferedEditor_invalidName() {
        GridElement grid = $(GridElement.class).id("buffered-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);
        String personName = nameCell.getText();

        WebElement edit = findElement(By.className("edit"));
        edit.click();

        // Write invalid name. There should be a status message with validation
        // error.
        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameInput);

        nameInput.clear();
        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ENTER);

        GridTHTDElement editColumn = row.getCell(grid.getAllColumns().get(2));
        editColumn.$("vaadin-button").attribute("class", "save").first()
                .click();

        String validation = findElement(By.id("validation")).getText();
        // There is an error in the status message
        Assert.assertEquals("Name should start with Person", validation);

        WebElement msg = findElement(By.id("buffered-editor-msg"));
        // No save events
        Assert.assertEquals("", msg.getText());

        editColumn.$("vaadin-button").attribute("class", "cancel").first()
                .click();

        Assert.assertEquals(personName, nameCell.getText());
        // Still no any save events
        Assert.assertEquals("", msg.getText());
    }

    @Test
    public void bufferedEditor_cancelWithEscape() {

        GridElement grid = $(GridElement.class).id("buffered-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);
        String personName = nameCell.getText();

        WebElement edit = findElement(By.className("edit"));
        edit.click();

        // Test cancel by ESC
        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameInput);

        nameInput.clear();
        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ESCAPE);

        Assert.assertFalse("Edit button should be visible",
                nameCell.$("vaadin-text-field").exists());

        nameColumn = grid.getColumn("Name");
        nameCell = row.getCell(nameColumn);
        Assert.assertEquals("Field name should not have changed.", personName,
                nameCell.getText());

    }

    @Test
    public void bufferedEditor_validName() {

        GridElement grid = $(GridElement.class).id("buffered-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);
        String personName = nameCell.getText();

        GridColumnElement subscriberColumn = grid.getColumn("Subscriber");

        WebElement edit = findElement(By.className("edit"));
        edit.click();

        // check that shown Edit buttons are disabled
        WebElement nextEditButton = grid.getRow(1)
                .getCell(grid.getAllColumns().get(2)).$("vaadin-button")
                .first();
        Assert.assertEquals(Boolean.TRUE.toString(),
                nextEditButton.getAttribute("disabled"));

        GridTHTDElement subscriberCell = row.getCell(subscriberColumn);

        TestBenchElement subscriberCheckbox = subscriberCell
                .$("vaadin-checkbox").first();
        boolean isSubscriber = subscriberCheckbox
                .getAttribute("checked") != null;

        // Write valid name.
        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameInput);

        nameInput.sendKeys(Keys.END);
        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ENTER);

        subscriberCheckbox.click();

        GridColumnElement editColumn = grid.getAllColumns().get(2);

        TestBenchElement save = row.getCell(editColumn).$("vaadin-button")
                .first();
        save.click();

        String validation = findElement(By.id("validation")).getText();
        // Validation is empty
        Assert.assertEquals("", validation);

        // New data should be shown in the grid cell
        Assert.assertEquals(personName + "foo", nameCell.getText());
        Assert.assertEquals(String.valueOf(!isSubscriber),
                subscriberCell.getText());

        // There should be an event for the edited person
        WebElement msg = findElement(By.id("buffered-editor-msg"));
        Assert.assertEquals(personName + "foo, " + !isSubscriber,
                msg.getText());
    }

    @Test
    public void dynamicEditor_bufferedMode() {

        GridElement grid = $(GridElement.class).id("buffered-dynamic-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);

        GridColumnElement editColumn = grid.getAllColumns().get(3);

        WebElement editButon = row.getCell(editColumn).$("vaadin-button")
                .first();
        editButon.click();

        // check that shown Edit buttons are disabled
        WebElement nextEditButton = grid.getRow(1).getCell(editColumn)
                .$("vaadin-button").first();
        Assert.assertEquals(Boolean.TRUE.toString(),
                nextEditButton.getAttribute("disabled"));

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameInput);

        nameInput.sendKeys(Keys.END);
        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ENTER);

        GridTHTDElement email = row.getCell(grid.getAllColumns().get(2));

        TestBenchElement emailField = email.$("vaadin-text-field").first();
        TestBenchElement emailInput = emailField.$("input").first();

        // clear the email and type wrong value
        emailInput.clear();
        emailInput.sendKeys("bar");
        emailInput.sendKeys(Keys.ENTER);

        TestBenchElement save = row.getCell(editColumn).$("vaadin-button")
                .first();
        save.click();

        // Check there is a validation error
        WebElement validation = findElement(By.id("email-validation"));
        Assert.assertEquals("Invalid email", validation.getText());

        GridTHTDElement subscriberCell = row
                .getCell(grid.getAllColumns().get(1));

        // Switch subscriber value off
        TestBenchElement checkbox = subscriberCell.$("vaadin-checkbox").first();
        checkbox.click();

        // email field should become read-only
        emailField = email.$("vaadin-text-field").first();
        emailInput = emailField.$("input").first();

        Assert.assertEquals(Boolean.TRUE.toString(),
                emailInput.getAttribute("readonly"));

        Assert.assertEquals("Not a subscriber",
                emailInput.getAttribute("value"));

        // Switch subscriber value on
        checkbox = subscriberCell.$("vaadin-checkbox").first();
        checkbox.click();

        emailField = email.$("vaadin-text-field").first();
        emailInput = emailField.$("input").first();
        emailInput.sendKeys("@example.com");
        emailInput.sendKeys(Keys.ENTER);
        save.click();

        WebElement updatedItemMsg = findElement(
                By.id("buffered-dynamic-editor-msg"));

        waitUntil(driver -> !updatedItemMsg.getText().isEmpty());

        Assert.assertEquals("Person 1foo, true, bar@example.com",
                updatedItemMsg.getText());
    }

    @Test
    public void dynamicEditor_bufferedMode_updateSubscriberValue_useKeyboardToSwitchEditorComponent() {
        GridElement grid = $(GridElement.class).id("buffered-dynamic-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        // start to edit
        GridColumnElement editColumn = grid.getAllColumns().get(3);
        row.getCell(editColumn).$("vaadin-button").first().click();

        GridTHTDElement subscriberCell = row
                .getCell(grid.getAllColumns().get(1));
        subscriberCell.$("vaadin-checkbox").first().click();

        GridTHTDElement nameCell = row.getCell(grid.getColumn("Name"));

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();
        nameField.focus();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameInput);
        nameInput.sendKeys(Keys.TAB);

        // skip checkbox and focus the email field
        new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();

        TestBenchElement emailField = row.getCell(grid.getAllColumns().get(2))
                .$("vaadin-text-field").first();
        Assert.assertNotNull(emailField.getAttribute("focused"));
        Assert.assertEquals("Not a subscriber",
                emailField.getAttribute("value"));

        subscriberCell.$("vaadin-checkbox").first().click();

        assertBufferedEditing(grid);
    }

    @Test
    public void dynamicNotBufferedEditor() throws InterruptedException {
        GridElement grid = $(GridElement.class)
                .id("not-buffered-dynamic-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);
        String personName = nameCell.getText();

        GridColumnElement subscriberColumn = grid.getColumn("Subscriber");

        GridTHTDElement subscriberCell = row.getCell(subscriberColumn);

        row.doubleClick();

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameInput);
        nameInput.sendKeys(Keys.END);
        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ENTER);

        GridTHTDElement emailCell = row.getCell(grid.getAllColumns().get(2));
        Assert.assertEquals(1, emailCell.$("vaadin-text-field").all().size());

        TestBenchElement subscriberCheckbox = subscriberCell
                .$("vaadin-checkbox").first();
        subscriberCheckbox.click();

        // The editor component should disappear for non-subscriber
        Assert.assertEquals(0, emailCell.$("vaadin-text-field").all().size());

        subscriberCheckbox.click();
        // Now it should return back
        Assert.assertEquals(1, emailCell.$("vaadin-text-field").all().size());

        TestBenchElement emailInput = emailCell.$("vaadin-text-field").first()
                .$("input").first();
        emailInput.clear();
        emailInput.sendKeys("bar@example.com");
        emailCell.sendKeys(Keys.ENTER);

        // click on another row
        grid.getRow(1).click(10, 10);

        // New data should be shown in the grid cell
        Assert.assertEquals(personName + "foo", nameCell.getText());
        Assert.assertEquals(Boolean.TRUE.toString(), subscriberCell.getText());
        Assert.assertEquals("bar@example.com",
                row.getCell(grid.getAllColumns().get(2)).getText());

        // The edited person should have new data
        WebElement msg = findElement(By.id("not-buffered-dynamic-editor-msg"));
        Assert.assertEquals(personName + "foo, true, bar@example.com",
                msg.getText());
    }

    @Test
    public void dynamicNotBufferedEditor_navigateUsingKeyboard()
            throws InterruptedException {
        GridElement grid = $(GridElement.class)
                .id("not-buffered-dynamic-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);
        String personName = nameCell.getText();

        GridColumnElement subscriberColumn = grid.getColumn("Subscriber");

        GridTHTDElement subscriberCell = row.getCell(subscriberColumn);

        row.doubleClick();

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameInput);

        nameInput.sendKeys(Keys.END);
        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ENTER);
        nameInput.click();
        nameInput.sendKeys(Keys.TAB);

        new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();

        new Actions(getDriver()).sendKeys(Keys.ARROW_RIGHT)
                .sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE)
                .sendKeys("org").build().perform();

        // click on another row
        grid.getRow(1).click(10, 10);

        // New data should be shown in the grid cell
        Assert.assertEquals(personName + "foo", nameCell.getText());
        Assert.assertEquals(Boolean.TRUE.toString(), subscriberCell.getText());
        Assert.assertEquals("mailss@example.org",
                row.getCell(grid.getAllColumns().get(2)).getText());

        // The edited person should have new data
        WebElement msg = findElement(By.id("not-buffered-dynamic-editor-msg"));
        Assert.assertEquals(personName + "foo, true, mailss@example.org",
                msg.getText());
    }

    private void assertElementHasFocus(WebElement element) {
        Assert.assertTrue("Element should have focus",
                (Boolean) executeScript(
                        "return document.activeElement === arguments[0]",
                        element));
    }

    private GridElement assertCloseEditorUsingKeyBoard(String gridId) {
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

    private void assertNotBufferedEditorClosed(GridElement grid) {
        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTRElement row = grid.getRow(0);
        GridTHTDElement nameCell = row.getCell(nameColumn);
        Assert.assertEquals(
                "Unexpected shown text field in the name cell when the editor should be closed",
                0, nameCell.$("vaadin-text-field").all().size());
    }

    private void assertBufferedEditing(GridElement grid) {
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
