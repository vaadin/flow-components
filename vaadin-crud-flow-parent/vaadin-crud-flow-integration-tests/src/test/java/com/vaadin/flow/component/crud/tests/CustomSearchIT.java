/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-crud/customsearch")
public class CustomSearchIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void filterSearchBar() {
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
