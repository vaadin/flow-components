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
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/pre-multisorted")
public class PreMultiSortedIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void rowsRendered() {
        GridElement grid = $(GridElement.class).first();
        // Wait for page with "First 13" to be loaded
        waitUntil(driver -> grid.getText().contains("First 13"));
        Assert.assertEquals("First 13", grid.getCell(5, 0).getText());
    }
}
