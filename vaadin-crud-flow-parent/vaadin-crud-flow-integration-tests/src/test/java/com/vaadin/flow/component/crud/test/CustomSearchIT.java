package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CustomSearchIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void filterSearchBar() {
        getDriver().get(getBaseURL() + "/customsearch");

        GridElement grid = $(GridElement.class).waitForFirst();
        Assert.assertEquals(3, grid.getRowCount());
        TextFieldElement searchBar = $(TextFieldElement.class).waitForFirst();

        searchBar.setValue("ll");
        waitUntil(c -> grid.getRowCount() == 1);
        searchBar.setValue("");
        waitUntil(c -> grid.getRowCount() == 3);
        searchBar.setValue("o");
        waitUntil(c -> grid.getRowCount() == 2);
    }
}
