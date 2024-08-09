/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.crud.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;

public class CustomSearchIT extends AbstractParallelTest {

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-crud");
        getDriver().get(url);
    }

    @Test
    public void filterSearchBar() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-crud") + "/customsearch";
        getDriver().get(url);

        GridElement grid = $(CrudElement.class).waitForFirst().getGrid();
        Assert.assertEquals(3, grid.getRowCount());

        TextFieldElement searchBar = $(TextFieldElement.class).id("searchBar");

        searchBar.setValue("ll");
        waitUntil(c -> grid.getRowCount() == 1);

        searchBar.setValue("");
        waitUntil(c -> grid.getRowCount() == 3);

        searchBar.setValue("o");
        waitUntil(c -> grid.getRowCount() == 2);
    }
}
