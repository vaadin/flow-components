/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.select.tests;

import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-select-list-data-view")
public class SelectListDataViewIT extends AbstractComponentIT {

    private SelectElement select;
    private SelectElement otherSelect;

    @Before
    public void init() {
        open();

        select = $(SelectElement.class).id(SelectListDataViewPage.SELECT);

        otherSelect = $(SelectElement.class)
                .id(SelectListDataViewPage.OTHER_SELECT);
    }

    @Test
    public void getItems_showsCheckboxContent() {
        // Select items size
        Assert.assertEquals("Unexpected select size", 3,
                select.getItems().size());

        // Data set content
        Assert.assertEquals("Unexpected select labels", "John,Paul,Mike",
                $("span").id(SelectListDataViewPage.ALL_ITEMS).getText());

        // Item present
        Assert.assertEquals("Person 'John' is expected to be present", "true",
                $("span").id(SelectListDataViewPage.ITEM_PRESENT).getText());
    }

    @Test
    public void navigateBetweenItems_showCorrectItems() {
        // Item on index
        Assert.assertEquals("Person 'John' is expected on index 0", "John",
                $("span").id(SelectListDataViewPage.ITEM_ON_INDEX).getText());

        // Has next item
        Assert.assertEquals("Next item is expected", "true",
                $("span").id(SelectListDataViewPage.HAS_NEXT_ITEM).getText());

        // Has previous item
        Assert.assertEquals("Previous item is expected", "true", $("span")
                .id(SelectListDataViewPage.HAS_PREVIOUS_ITEM).getText());

        findElement(By.id(SelectListDataViewPage.NEXT_ITEM)).click();

        // Next item
        Assert.assertEquals("Unexpected next item", "Mike",
                $("span").id(SelectListDataViewPage.CURRENT_ITEM).getText());

        findElement(By.id(SelectListDataViewPage.PREVIOUS_ITEM)).click();

        // Previous item
        Assert.assertEquals("Unexpected previous item", "Paul",
                $("span").id(SelectListDataViewPage.CURRENT_ITEM).getText());
    }

    @Test
    public void addItem_itemAddedAndShownInBothComponents() {
        findElement(By.id(SelectListDataViewPage.ADD_ITEM)).click();

        Assert.assertEquals("Unexpected item count after adding a new item in"
                + " first select", 4, select.getItems().size());
        Assert.assertEquals("Wrong name for added person in first select",
                "Peter", select.getItems().get(3).getText());

        Assert.assertEquals("Unexpected item count after adding a new item in"
                + " second select", 4, otherSelect.getItems().size());
        Assert.assertEquals("Wrong name for added person in second select",
                "Peter", otherSelect.getItems().get(3).getText());
    }

    @Test
    public void updateItem_itemAddedAndShownInBothComponents() {
        findElement(By.id(SelectListDataViewPage.UPDATE_ITEM)).click();

        Assert.assertEquals("Wrong name for updated person", "Jack",
                select.getItems().get(0).getText());

        Assert.assertEquals("Wrong name for updated person", "Jack",
                otherSelect.getItems().get(0).getText());
    }

    @Test
    public void deleteItem_itemAddedAndShownInBothComponents() {
        findElement(By.id(SelectListDataViewPage.DELETE_ITEM)).click();

        Assert.assertEquals("Item count not expected", 2,
                select.getItems().size());

        Assert.assertEquals("Item count not expected", 2,
                otherSelect.getItems().size());
    }

    @Test
    public void sorting_itemsSortedOnlyInOneComponent() {
        findElement(By.id(SelectListDataViewPage.SORT_BUTTON)).click();

        Assert.assertEquals("Unexpected sort order", "John,Mike,Paul",
                select.$("vaadin-select-item").all().stream()
                        .map(TestBenchElement::getText)
                        .collect(Collectors.joining(",")));

        Assert.assertEquals("Unexpected sort order", "John,Paul,Mike",
                otherSelect.$("vaadin-select-item").all().stream()
                        .map(TestBenchElement::getText)
                        .collect(Collectors.joining(",")));
    }

    @Test
    public void filtering_itemsFilteredOnlyInOneComponent() {
        findElement(By.id(SelectListDataViewPage.FILTER_BUTTON)).click();

        Assert.assertEquals("Unexpected filtered items count", 1,
                select.$("vaadin-select-item").all().size());
        Assert.assertEquals("Unexpected filtered item", "Paul",
                select.$("vaadin-select-item").all().get(0).getText());

        Assert.assertEquals("No filter expected", 3,
                otherSelect.$("vaadin-select-item").all().size());
        Assert.assertArrayEquals("No filter expected",
                new String[] { "John", "Paul", "Mike" },
                otherSelect.$("vaadin-select-item").all().stream()
                        .map(TestBenchElement::getText).toArray());
    }
}
