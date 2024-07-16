/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/grid-on-client-and-slot")
public class GridOnClientAndServerIT extends AbstractComponentIT {

    @Test
    public void treeGridOnClientShouldWorkIfAnotherGridIsAddedFromServer() {
        open();

        TestBenchElement parent = $("grid-on-client-and-slot").first();

        TreeGridElement treeGrid = parent.$(TreeGridElement.class).id("tree");
        treeGrid.getExpandToggleElement(0, 0).click();
        GridTHTDElement cell = treeGrid.getCell(1, 0);

        Assert.assertEquals("child 1-1", cell.getText().trim());

        findElement(By.id("add-new-grid-button")).click();
        treeGrid.getExpandToggleElement(3, 0).click();
        cell = treeGrid.getCell(4, 0);

        Assert.assertEquals("child 2-1", cell.getText().trim());
    }
}
