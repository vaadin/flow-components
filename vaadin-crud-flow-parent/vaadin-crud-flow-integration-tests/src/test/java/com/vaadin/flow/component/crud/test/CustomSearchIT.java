package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CustomSearchIT extends AbstractParallelTest {

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(), super.getBaseURL() + "/vaadin-crud") ;
        getDriver().get(url);
    }

    @Test
    public void filterSearchBar() {
        String url = getBaseURL().replace(super.getBaseURL(), super.getBaseURL() + "/vaadin-crud") + "/customsearch";
        getDriver().get(url);

        GridElement grid = $(CrudElement.class).waitForFirst().getGrid();
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
