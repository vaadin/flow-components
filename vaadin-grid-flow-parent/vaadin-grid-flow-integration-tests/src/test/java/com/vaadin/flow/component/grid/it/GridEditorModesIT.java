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

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
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

        getButton(grid, 0, 2, 0).click();
        Assert.assertFalse(getButton(grid, 1, 2, 0).isEnabled());

        CheckboxElement checkbox = getCheckbox(subscriberCell);
        boolean isSubscriber = checkbox.isChecked();

        TextFieldElement nameField = getTextField(nameCell);
        Assert.assertTrue(nameField.hasAttribute("focused"));
        nameField.sendKeys(Keys.END, "foo", Keys.ENTER);
        checkbox.click();

        getButton(grid, 0, 2, 0).click();

        Assert.assertEquals("", $("div").id("validation").getText());
        Assert.assertEquals(personName + "foo", nameCell.getText());
        Assert.assertEquals(String.valueOf(!isSubscriber),
                subscriberCell.getText());
        Assert.assertEquals(personName + "foo, " + !isSubscriber,
                $("div").id("buffered-editor-msg").getText());
    }

    @Test
    public void bufferedEditor_invalidName_saveRejected_cancelRestoresValue() {
        GridElement grid = $(GridElement.class).id("buffered-editor");
        GridTHTDElement nameCell = grid.getCell(0, 0);
        String personName = nameCell.getText();
        TestBenchElement message = $("div").id("buffered-editor-msg");

        getButton(grid, 0, 2, 0).click();
        getTextField(nameCell).setValue("foo");

        // Save
        getButton(grid, 0, 2, 0).click();
        Assert.assertEquals("Name should start with Person",
                $("div").id("validation").getText());
        Assert.assertTrue("The editor should stay open",
                nameCell.$(TextFieldElement.class).exists());
        Assert.assertEquals("", message.getText());

        // Cancel
        getButton(grid, 0, 2, 1).click();
        Assert.assertFalse("The editor should be closed",
                nameCell.$(TextFieldElement.class).exists());
        Assert.assertEquals(personName, nameCell.getText());
        Assert.assertEquals("", message.getText());
    }

    @Test
    public void bufferedDynamicEditor_toggleSubscriber_emailEditorSwapped() {
        GridElement grid = $(GridElement.class).id("buffered-dynamic-editor");
        GridTHTDElement emailCell = grid.getCell(0, 2);

        getButton(grid, 0, 3, 0).click();
        Assert.assertFalse(getButton(grid, 1, 3, 0).isEnabled());

        TextFieldElement nameField = getTextField(grid.getCell(0, 0));
        Assert.assertTrue(nameField.hasAttribute("focused"));
        nameField.sendKeys(Keys.END, "foo", Keys.ENTER);

        getTextField(emailCell).setValue("bar");
        getButton(grid, 0, 3, 0).click();
        Assert.assertEquals("Invalid email",
                $("div").id("email-validation").getText());

        // A non-subscriber gets a read-only email field
        toggleSubscriber(grid);
        TextFieldElement emailField = getTextField(emailCell);
        Assert.assertTrue(emailField.getPropertyBoolean("readonly"));
        Assert.assertEquals("Not a subscriber", emailField.getValue());

        toggleSubscriber(grid);
        getTextField(emailCell).sendKeys("@example.com", Keys.ENTER);
        getButton(grid, 0, 3, 0).click();

        TestBenchElement message = $("div").id("buffered-dynamic-editor-msg");
        waitUntil(driver -> !message.getText().isEmpty());
        Assert.assertEquals("Person 1foo, true, bar@example.com",
                message.getText());
    }

    @Test
    public void bufferedDynamicEditor_toggleSubscriber_navigateWithKeyboard() {
        GridElement grid = $(GridElement.class).id("buffered-dynamic-editor");

        getButton(grid, 0, 3, 0).click();
        toggleSubscriber(grid);

        TextFieldElement nameField = getTextField(grid.getCell(0, 0));
        nameField.focus();
        Assert.assertTrue(nameField.hasAttribute("focused"));

        // Skip the checkbox and focus the read-only email field
        new Actions(getDriver()).sendKeys(Keys.TAB, Keys.TAB).perform();

        TextFieldElement emailField = getTextField(grid.getCell(0, 2));
        Assert.assertTrue(emailField.hasAttribute("focused"));
        Assert.assertEquals("Not a subscriber", emailField.getValue());

        toggleSubscriber(grid);

        nameField = getTextField(grid.getCell(0, 0));
        nameField.focus();
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

        TextFieldElement nameField = getTextField(nameCell);
        Assert.assertTrue(nameField.hasAttribute("focused"));
        nameField.sendKeys(Keys.END, "foo", Keys.ENTER);
        Assert.assertTrue(emailCell.$(TextFieldElement.class).exists());

        CheckboxElement checkbox = getCheckbox(subscriberCell);
        checkbox.click();
        Assert.assertFalse(emailCell.$(TextFieldElement.class).exists());

        checkbox.click();
        getTextField(emailCell).setValue("bar@example.com");

        grid.getRow(1).click(10, 10);

        Assert.assertEquals(personName + "foo", nameCell.getText());
        Assert.assertEquals("true", subscriberCell.getText());
        Assert.assertEquals("bar@example.com", emailCell.getText());
        Assert.assertEquals(personName + "foo, true, bar@example.com",
                $("div").id("not-buffered-dynamic-editor-msg").getText());
    }

    private TextFieldElement getTextField(GridTHTDElement cell) {
        return cell.$(TextFieldElement.class).first();
    }

    private CheckboxElement getCheckbox(GridTHTDElement cell) {
        return cell.$(CheckboxElement.class).first();
    }

    private void toggleSubscriber(GridElement grid) {
        getCheckbox(grid.getCell(0, 1)).click();
    }

    // In the editor column, button 0 is Edit, or Save when the editor is
    // open. Button 1 is Cancel.
    private ButtonElement getButton(GridElement grid, int row, int column,
            int index) {
        return grid.getCell(row, column).$(ButtonElement.class).get(index);
    }
}
