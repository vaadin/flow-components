/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/basic-features")
public class GridViewBasicFeaturesIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void basicFeatures() {
        GridElement grid = $(GridElement.class).id("grid-basic-feature");
        scrollToElement(grid);
        waitUntil(driver -> grid.getAllColumns().size() == 11);

        TestBenchElement filteringField = grid
                .findElement(By.tagName("vaadin-text-field"));
        filteringField.sendKeys("sek");
        blur();

        Assert.assertTrue(
                "The first company name should contain the applied filter string",
                grid.getCell(0, 0).getInnerHTML().toLowerCase()
                        .contains("sek"));
    }
}
