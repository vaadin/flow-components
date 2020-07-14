package com.vaadin.flow.component.select.test;

import java.util.stream.Collectors;

import com.vaadin.flow.component.select.examples.SelectListDataViewPage;
import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath("vaadin-select-list-data-view")
public class SelectListDataViewIT extends AbstractComponentIT {

    @Test
    public void selectListDataView_dataViewApiRequested_dataAvailable() {
        open();

        SelectElement select = $(SelectElement.class)
                .id(SelectListDataViewPage.SELECT);

        // Select items size
        Assert.assertEquals("Unexpected select size", 3,
                select.getItems().size());

        // Data set content
        Assert.assertEquals("Unexpected select labels", "John,Paul,Mike",
                $("span").id(SelectListDataViewPage.ALL_ITEMS).getText());

        // Item present
        Assert.assertEquals("Person 'John' is expected to be present", "true",
                $("span").id(SelectListDataViewPage.ITEM_PRESENT).getText());

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

        // Add item
        findElement(By.id(SelectListDataViewPage.ADD_ITEM)).click();
        Assert.assertEquals("Wrong name for added person", "Peter",
                select.getItems().get(3).getText());

        // Update item
        findElement(By.id(SelectListDataViewPage.UPDATE_ITEM)).click();
        Assert.assertEquals("Wrong name for updated person", "Jack",
                select.getItems().get(3).getText());

        // Delete item
        findElement(By.id(SelectListDataViewPage.DELETE_ITEM)).click();
        Assert.assertEquals("Item count not expected", 3,
                select.getItems().size());

        // Sort order
        findElement(By.id(SelectListDataViewPage.SORT_BUTTON)).click();
        Assert.assertEquals("Unexpected sort order", "John,Mike,Paul",
                select.getItems().stream().map(TestBenchElement::getText)
                        .collect(Collectors.joining(",")));

        findElement(By.id(SelectListDataViewPage.FILTER_BUTTON)).click();

        // Filtering
        Assert.assertEquals("Unexpected filtered checkbox count", 1,
                select.getItems().size());
        Assert.assertEquals("Unexpected filtered checkbox item", "Paul",
                select.getItems().get(0).getText());
    }
}
