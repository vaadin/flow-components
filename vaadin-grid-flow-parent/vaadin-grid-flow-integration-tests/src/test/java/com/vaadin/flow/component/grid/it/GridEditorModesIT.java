/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/editor-modes")
public class GridEditorModesIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void bufferedEditor_validName_itemSaved() {
        GridElement grid = $(GridElement.class).id("buffered-editor");
        GridTHTDElement nameCell = grid.getCell(0, 0);
        GridTHTDElement subscriberCell = grid.getCell(0, 1);
        String personName = nameCell.getText();

        clickFirstButton(grid, 0, 2);
        assertEditButtonDisabled(grid, 1, 2);

        TestBenchElement subscriberCheckbox = subscriberCell
                .$("vaadin-checkbox").first();
        boolean isSubscriber = subscriberCheckbox.hasAttribute("checked");

        TestBenchElement nameInput = getInput(nameCell);
        Assert.assertTrue(nameInput.isFocused());
        nameInput.sendKeys(Keys.END, "foo", Keys.ENTER);
        subscriberCheckbox.click();

        clickFirstButton(grid, 0, 2);

        Assert.assertEquals("", $("div").id("validation").getText());
        Assert.assertEquals(personName + "foo", nameCell.getText());
        Assert.assertEquals(String.valueOf(!isSubscriber),
                subscriberCell.getText());
        Assert.assertEquals(personName + "foo, " + !isSubscriber,
                $("div").id("buffered-editor-msg").getText());
    }

    @Test
    public void bufferedDynamicEditor_toggleSubscriber_emailEditorSwapped() {
        GridElement grid = $(GridElement.class).id("buffered-dynamic-editor");
        GridTHTDElement emailCell = grid.getCell(0, 2);

        clickFirstButton(grid, 0, 3);
        assertEditButtonDisabled(grid, 1, 3);

        TestBenchElement nameInput = getInput(grid.getCell(0, 0));
        Assert.assertTrue(nameInput.isFocused());
        nameInput.sendKeys(Keys.END, "foo", Keys.ENTER);

        TestBenchElement emailInput = getInput(emailCell);
        emailInput.clear();
        emailInput.sendKeys("bar", Keys.ENTER);
        clickFirstButton(grid, 0, 3);
        Assert.assertEquals("Invalid email",
                $("div").id("email-validation").getText());

        // A non-subscriber gets a read-only email field
        toggleSubscriber(grid);
        emailInput = getInput(emailCell);
        Assert.assertTrue(emailInput.hasAttribute("readonly"));
        Assert.assertEquals("Not a subscriber",
                emailInput.getDomProperty("value"));

        toggleSubscriber(grid);
        emailInput = getInput(emailCell);
        emailInput.sendKeys("@example.com", Keys.ENTER);
        clickFirstButton(grid, 0, 3);

        TestBenchElement message = $("div").id("buffered-dynamic-editor-msg");
        waitUntil(driver -> !message.getText().isEmpty());
        Assert.assertEquals("Person 1foo, true, bar@example.com",
                message.getText());
    }

    @Test
    public void bufferedDynamicEditor_toggleSubscriber_navigateWithKeyboard() {
        GridElement grid = $(GridElement.class).id("buffered-dynamic-editor");

        clickFirstButton(grid, 0, 3);
        toggleSubscriber(grid);

        TestBenchElement nameInput = getInput(grid.getCell(0, 0));
        nameInput.focus();
        Assert.assertTrue(nameInput.isFocused());

        // Skip the checkbox and focus the read-only email field
        nameInput.sendKeys(Keys.TAB);
        new Actions(getDriver()).sendKeys(Keys.TAB).perform();

        TestBenchElement emailField = grid.getCell(0, 2).$("vaadin-text-field")
                .first();
        Assert.assertTrue(emailField.hasAttribute("focused"));
        Assert.assertEquals("Not a subscriber",
                emailField.getDomProperty("value"));

        toggleSubscriber(grid);

        nameInput = getInput(grid.getCell(0, 0));
        nameInput.click();
        nameInput.focus();
        new Actions(getDriver()).sendKeys(Keys.END, "foo").perform();

        // Skip the checkbox, change the email to .org, then save with Enter
        new Actions(getDriver()).sendKeys(Keys.TAB, Keys.TAB).perform();
        new Actions(getDriver()).sendKeys(Keys.END, Keys.BACK_SPACE,
                Keys.BACK_SPACE, Keys.BACK_SPACE, "org").perform();
        new Actions(getDriver()).sendKeys(Keys.TAB, Keys.ENTER).perform();

        TestBenchElement message = $("div").id("buffered-dynamic-editor-msg");
        waitUntil(driver -> !message.getText().isEmpty());
        Assert.assertEquals("Person 1foo, true, mailss@example.org",
                message.getText());
    }

    @Test
    public void notBufferedDynamicEditor_toggleSubscriber_emailEditorRemoved() {
        GridElement grid = $(GridElement.class)
                .id("not-buffered-dynamic-editor");
        GridTHTDElement nameCell = grid.getCell(0, 0);
        GridTHTDElement subscriberCell = grid.getCell(0, 1);
        GridTHTDElement emailCell = grid.getCell(0, 2);
        String personName = nameCell.getText();

        grid.getRow(0).doubleClick();

        TestBenchElement nameInput = getInput(nameCell);
        Assert.assertTrue(nameInput.isFocused());
        nameInput.sendKeys(Keys.END, "foo", Keys.ENTER);
        Assert.assertTrue(emailCell.$("vaadin-text-field").exists());

        TestBenchElement checkbox = subscriberCell.$("vaadin-checkbox").first();
        checkbox.click();
        Assert.assertFalse(emailCell.$("vaadin-text-field").exists());

        checkbox.click();
        TestBenchElement emailInput = getInput(emailCell);
        emailInput.clear();
        emailInput.sendKeys("bar@example.com", Keys.ENTER);

        grid.getRow(1).click(10, 10);

        Assert.assertEquals(personName + "foo", nameCell.getText());
        Assert.assertEquals("true", subscriberCell.getText());
        Assert.assertEquals("bar@example.com", emailCell.getText());
        Assert.assertEquals(personName + "foo, true, bar@example.com",
                $("div").id("not-buffered-dynamic-editor-msg").getText());
    }

    private TestBenchElement getInput(GridTHTDElement cell) {
        return cell.$("vaadin-text-field").first().$("input").first();
    }

    private void toggleSubscriber(GridElement grid) {
        grid.getCell(0, 1).$("vaadin-checkbox").first().click();
    }

    // Clicks Edit, or Save when the editor is open
    private void clickFirstButton(GridElement grid, int row, int column) {
        grid.getCell(row, column).$("vaadin-button").first().click();
    }

    private void assertEditButtonDisabled(GridElement grid, int row,
            int column) {
        Assert.assertTrue(grid.getCell(row, column).$("vaadin-button").first()
                .hasAttribute("disabled"));
    }
}
