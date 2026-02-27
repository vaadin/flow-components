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
package com.vaadin.flow.component.combobox.test;

import static com.vaadin.flow.component.combobox.test.ComboBoxBindItemsPage.ADD_ITEM_BUTTON;
import static com.vaadin.flow.component.combobox.test.ComboBoxBindItemsPage.COMBO_BOX_ID;
import static com.vaadin.flow.component.combobox.test.ComboBoxBindItemsPage.ITEM_COUNT_SPAN;
import static com.vaadin.flow.component.combobox.test.ComboBoxBindItemsPage.REMOVE_ITEM_BUTTON;
import static com.vaadin.flow.component.combobox.test.ComboBoxBindItemsPage.SELECTED_VALUE_SPAN;
import static com.vaadin.flow.component.combobox.test.ComboBoxBindItemsPage.UPDATE_FIRST_ITEM_BUTTON;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/combo-box-bind-items")
public class ComboBoxBindItemsIT extends AbstractComboBoxIT {

    private ComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $(ComboBoxElement.class).id(COMBO_BOX_ID);
    }

    @Test
    public void bindItems_initialItemsDisplayed() {
        Assert.assertEquals("Initial item count should be 3", "3",
                $("span").id(ITEM_COUNT_SPAN).getText());

        comboBox.openPopup();

        // Verify initial items are displayed
        Assert.assertEquals("ComboBox should have 3 items", 3,
                comboBox.getOptions().size());

        Assert.assertEquals("First item should be Alice Smith", "Alice Smith",
                comboBox.getOptions().get(0));
        Assert.assertEquals("Second item should be Bob Johnson", "Bob Johnson",
                comboBox.getOptions().get(1));
        Assert.assertEquals("Third item should be Charlie Brown",
                "Charlie Brown", comboBox.getOptions().get(2));
    }

    @Test
    public void bindItems_addItem_comboBoxUpdated() {
        comboBox.openPopup();
        Assert.assertEquals("Initial item count", 3,
                comboBox.getOptions().size());

        comboBox.closePopup();
        waitForElementPresent(By.id(ADD_ITEM_BUTTON));
        findElement(By.id(ADD_ITEM_BUTTON)).click();

        Assert.assertEquals("Item count should be 4", "4",
                $("span").id(ITEM_COUNT_SPAN).getText());

        comboBox.openPopup();
        Assert.assertEquals("ComboBox should have 4 items", 4,
                comboBox.getOptions().size());
        Assert.assertEquals("New item should be added", "Person Lastname",
                comboBox.getOptions().get(3));
    }

    @Test
    public void bindItems_removeItem_comboBoxUpdated() {
        comboBox.openPopup();
        Assert.assertEquals("Initial item count", 3,
                comboBox.getOptions().size());

        comboBox.closePopup();
        waitForElementPresent(By.id(REMOVE_ITEM_BUTTON));
        findElement(By.id(REMOVE_ITEM_BUTTON)).click();

        Assert.assertEquals("Item count should be 2", "2",
                $("span").id(ITEM_COUNT_SPAN).getText());

        comboBox.openPopup();
        Assert.assertEquals("ComboBox should have 2 items", 2,
                comboBox.getOptions().size());
        Assert.assertEquals("First item still exists", "Alice Smith",
                comboBox.getOptions().get(0));
        Assert.assertEquals("Second item still exists", "Bob Johnson",
                comboBox.getOptions().get(1));
    }

    @Test
    public void bindItems_updateItem_comboBoxUpdated() {
        comboBox.openPopup();
        Assert.assertEquals("First item before update", "Alice Smith",
                comboBox.getOptions().get(0));

        comboBox.closePopup();
        waitForElementPresent(By.id(UPDATE_FIRST_ITEM_BUTTON));
        findElement(By.id(UPDATE_FIRST_ITEM_BUTTON)).click();

        comboBox.openPopup();
        Assert.assertEquals("First item after update", "Alice Updated Smith",
                comboBox.getOptions().get(0));
        Assert.assertEquals("Other items unchanged", "Bob Johnson",
                comboBox.getOptions().get(1));
    }

    @Test
    public void bindItems_selectItem_valueDisplayed() {
        Assert.assertEquals("Initial selected value", "None",
                $("span").id(SELECTED_VALUE_SPAN).getText());

        comboBox.selectByText("Bob Johnson");

        Assert.assertEquals("Selected value should be displayed", "Bob Johnson",
                $("span").id(SELECTED_VALUE_SPAN).getText());
    }

    @Test
    public void bindItems_filterItems_worksCorrectly() {
        comboBox.openPopup();
        Assert.assertEquals("All items shown initially", 3,
                comboBox.getOptions().size());

        // Type to filter
        comboBox.sendKeys("bob");

        // Wait a bit for filtering to apply
        waitUntil(driver -> comboBox.getOptions().size() == 1, 2);

        Assert.assertEquals("Only Bob should be shown", 1,
                comboBox.getOptions().size());
        Assert.assertEquals("Bob Johnson should match filter", "Bob Johnson",
                comboBox.getOptions().get(0));
    }

    @Test
    public void bindItems_multipleAdds_comboBoxUpdatesCorrectly() {
        waitForElementPresent(By.id(ADD_ITEM_BUTTON));
        findElement(By.id(ADD_ITEM_BUTTON)).click();
        findElement(By.id(ADD_ITEM_BUTTON)).click();

        Assert.assertEquals("Item count should be 5", "5",
                $("span").id(ITEM_COUNT_SPAN).getText());

        comboBox.openPopup();
        Assert.assertEquals("ComboBox should have 5 items", 5,
                comboBox.getOptions().size());
    }

    @Test
    public void bindItems_addThenRemove_comboBoxCorrect() {
        waitForElementPresent(By.id(ADD_ITEM_BUTTON));
        findElement(By.id(ADD_ITEM_BUTTON)).click();

        comboBox.openPopup();
        Assert.assertEquals("After add: 4 items", 4,
                comboBox.getOptions().size());

        comboBox.closePopup();
        waitForElementPresent(By.id(REMOVE_ITEM_BUTTON));
        findElement(By.id(REMOVE_ITEM_BUTTON)).click();

        comboBox.openPopup();
        Assert.assertEquals("After remove: 3 items", 3,
                comboBox.getOptions().size());

        // Verify original items are still there
        Assert.assertEquals("Alice Smith", comboBox.getOptions().get(0));
        Assert.assertEquals("Bob Johnson", comboBox.getOptions().get(1));
        Assert.assertEquals("Charlie Brown", comboBox.getOptions().get(2));
    }
}
