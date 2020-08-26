/*
 * Copyright 2000-2020 Vaadin Ltd.
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

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath("vaadin-checkbox-group-list-data-view")
public class CheckboxGroupListDataViewIT extends AbstractComponentIT {

    @Test
    public void checkboxGroupListDataView_dataViewApiRequested_dataAvailable() {
        open();

        TestBenchElement checkboxGroup = $("vaadin-checkbox-group")
                .id(CheckboxGroupListDataViewPage.CHECKBOX_GROUP);

        List<TestBenchElement> checkboxes =
                checkboxGroup.$("vaadin-checkbox").all();

        // Checkbox items size
        Assert.assertEquals("Unexpected checkbox count", 3, checkboxes.size());

        // Data set size
        Assert.assertEquals("Unexpected item count", "3",
                $("span").id(CheckboxGroupListDataViewPage.ITEMS_SIZE)
                        .getText());

        // Data set content
        Assert.assertEquals("Unexpected checkbox labels", "John,Paul,Mike",
                $("span").id(CheckboxGroupListDataViewPage.ALL_ITEMS)
                        .getText());

        // Item present
        Assert.assertEquals("Person 'John' is expected to be present", "true",
                $("span").id(CheckboxGroupListDataViewPage.ITEM_PRESENT)
                        .getText());

        // Item on index
        Assert.assertEquals("Person 'John' is expected on index 0", "John",
                $("span").id(CheckboxGroupListDataViewPage.ITEM_ON_INDEX)
                        .getText());

        // Has next item
        Assert.assertEquals("Next item is expected", "true",
                $("span").id(CheckboxGroupListDataViewPage.HAS_NEXT_ITEM)
                        .getText());

        // Has previous item
        Assert.assertEquals("Previous item is expected", "true",
                $("span").id(CheckboxGroupListDataViewPage.HAS_PREVIOUS_ITEM)
                        .getText());

        findElement(By.id(CheckboxGroupListDataViewPage.NEXT_ITEM)).click();

        // Next item
        Assert.assertEquals("Unexpected next item", "Mike",
                $("span").id(CheckboxGroupListDataViewPage.CURRENT_ITEM)
                        .getText());

        findElement(By.id(CheckboxGroupListDataViewPage.PREVIOUS_ITEM)).click();

        // Previous item
        Assert.assertEquals("Unexpected previous item", "Paul",
                $("span").id(CheckboxGroupListDataViewPage.CURRENT_ITEM)
                        .getText());

        // Add item
        findElement(By.id(CheckboxGroupListDataViewPage.ADD_ITEM)).click();
        Assert.assertEquals("Wrong name for added person",
                "Peter",
                checkboxGroup.$("vaadin-checkbox").all().get(3).getText());

        // Update item
        findElement(By.id(CheckboxGroupListDataViewPage.UPDATE_ITEM)).click();
        Assert.assertEquals("Wrong name for updated person",
                "Jack",
                checkboxGroup.$("vaadin-checkbox").all().get(3).getText());

        // Delete item
        findElement(By.id(CheckboxGroupListDataViewPage.DELETE_ITEM)).click();
        Assert.assertEquals("Item count not expected", 3,
                checkboxGroup.$("vaadin-checkbox").all().size());

        // Sort order
        findElement(By.id(CheckboxGroupListDataViewPage.SORT_BUTTON)).click();
        Assert.assertEquals("Unexpected sort order", "John,Mike,Paul",
                checkboxGroup.$("vaadin-checkbox").all().stream()
                        .map(TestBenchElement::getText)
                        .collect(Collectors.joining(",")));

        findElement(By.id(CheckboxGroupListDataViewPage.FILTER_BUTTON)).click();

        // Filtering
        checkboxes = checkboxGroup.$("vaadin-checkbox").all();
        Assert.assertEquals("Unexpected filtered checkbox count", 1,
                checkboxes.size());
        Assert.assertEquals("Unexpected filtered checkbox item", "Paul",
                checkboxes.get(0).getText());
    }
}
