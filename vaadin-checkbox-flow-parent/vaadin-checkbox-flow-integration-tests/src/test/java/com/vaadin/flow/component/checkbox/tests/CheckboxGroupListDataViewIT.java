/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.Arrays;
import java.util.List;

@TestPath("vaadin-checkbox-group-list-data-view")
public class CheckboxGroupListDataViewIT extends AbstractComponentIT {

    private CheckboxGroupElement checkboxGroup;
    private CheckboxGroupElement otherCheckboxGroup;

    @Before
    public void init() {
        open();

        checkboxGroup = $(CheckboxGroupElement.class)
                .id(CheckboxGroupListDataViewPage.CHECKBOX_GROUP);

        otherCheckboxGroup = $(CheckboxGroupElement.class)
                .id(CheckboxGroupListDataViewPage.OTHER_CHECKBOX_GROUP);
    }

    @Test
    public void getItems_showsCheckboxContent() {
        List<CheckboxElement> checkboxes = checkboxGroup.getCheckboxes();

        // Checkbox items size
        Assert.assertEquals("Unexpected checkbox count", 3, checkboxes.size());

        // Data set size
        Assert.assertEquals("Unexpected item count", "3", $("span")
                .id(CheckboxGroupListDataViewPage.ITEMS_SIZE).getText());

        // Data set content
        Assert.assertEquals("Unexpected checkbox labels", "John,Paul,Mike",
                $("span").id(CheckboxGroupListDataViewPage.ALL_ITEMS)
                        .getText());
    }

    @Test
    public void navigateBetweenItems_showCorrectItems() {
        // Item present
        Assert.assertEquals("Person 'John' is expected to be present", "true",
                $("span").id(CheckboxGroupListDataViewPage.ITEM_PRESENT)
                        .getText());

        // Item on index
        Assert.assertEquals("Person 'John' is expected on index 0", "John",
                $("span").id(CheckboxGroupListDataViewPage.ITEM_ON_INDEX)
                        .getText());

        // Has next item
        Assert.assertEquals("Next item is expected", "true", $("span")
                .id(CheckboxGroupListDataViewPage.HAS_NEXT_ITEM).getText());

        // Has previous item
        Assert.assertEquals("Previous item is expected", "true", $("span")
                .id(CheckboxGroupListDataViewPage.HAS_PREVIOUS_ITEM).getText());

        findElement(By.id(CheckboxGroupListDataViewPage.NEXT_ITEM)).click();

        // Next item
        Assert.assertEquals("Unexpected next item", "Mike", $("span")
                .id(CheckboxGroupListDataViewPage.CURRENT_ITEM).getText());

        findElement(By.id(CheckboxGroupListDataViewPage.PREVIOUS_ITEM)).click();

        // Previous item
        Assert.assertEquals("Unexpected previous item", "Paul", $("span")
                .id(CheckboxGroupListDataViewPage.CURRENT_ITEM).getText());
    }

    @Test
    public void addItem_itemAddedAndShownInBothComponents() {
        findElement(By.id(CheckboxGroupListDataViewPage.ADD_ITEM)).click();

        Assert.assertEquals(
                "Unexpected item count after adding a new item in first "
                        + "CheckboxGroup",
                4, checkboxGroup.getCheckboxes().size());

        Assert.assertEquals(
                "Wrong name for added person in first CheckboxGroup", "Peter",
                checkboxGroup.getCheckboxes().get(3).getLabel());

        Assert.assertEquals(
                "Unexpected item count after adding a new item in"
                        + " second CheckboxGroup",
                4, checkboxGroup.getCheckboxes().size());

        Assert.assertEquals(
                "Wrong name for added person in second " + "CheckboxGroup",
                "Peter", otherCheckboxGroup.getCheckboxes().get(3).getLabel());
    }

    @Test
    public void updateItem_itemAddedAndShownInBothComponents() {
        findElement(By.id(CheckboxGroupListDataViewPage.UPDATE_ITEM)).click();

        Assert.assertEquals("Wrong name for updated person in first Checkbox",
                "Jack", checkboxGroup.getCheckboxes().get(0).getLabel());

        Assert.assertEquals("Wrong name for updated person in second Checkbox",
                "Jack", otherCheckboxGroup.getCheckboxes().get(0).getLabel());
    }

    @Test
    public void deleteItem_itemAddedAndShownInBothComponents() {
        findElement(By.id(CheckboxGroupListDataViewPage.DELETE_ITEM)).click();

        Assert.assertEquals("Unexpected item count in first Checkbox", 2,
                checkboxGroup.getCheckboxes().size());

        Assert.assertEquals("Unexpected item count in second Checkbox", 2,
                otherCheckboxGroup.getCheckboxes().size());
    }

    @Test
    public void sorting_itemsSortedOnlyInOneComponent() {
        findElement(By.id(CheckboxGroupListDataViewPage.SORT_BUTTON)).click();

        Assert.assertEquals("Unexpected sort order",
                Arrays.asList("John", "Mike", "Paul"),
                checkboxGroup.getOptions());

        Assert.assertEquals("Unexpected sort order",
                Arrays.asList("John", "Paul", "Mike"),
                otherCheckboxGroup.getOptions());
    }

    @Test
    public void filtering_itemsFilteredOnlyInOneComponent() {
        findElement(By.id(CheckboxGroupListDataViewPage.FILTER_BUTTON)).click();

        List<CheckboxElement> checkboxes = checkboxGroup.getCheckboxes();
        Assert.assertEquals("Unexpected filtered checkbox count", 1,
                checkboxes.size());
        Assert.assertEquals("Unexpected filtered checkbox item", "Paul",
                checkboxes.get(0).getLabel());

        // Verify no impact on other component bound to the same data provider
        checkboxes = otherCheckboxGroup.getCheckboxes();
        Assert.assertEquals("No filter expected on other CheckboxGroup bound "
                + "to the same data provider", 3, checkboxes.size());
        Assert.assertArrayEquals(
                "No filtering expected on other "
                        + "CheckboxGroup bound to the same data provider",
                new String[] { "John", "Paul", "Mike" },
                otherCheckboxGroup.getOptions().toArray());
    }
}
