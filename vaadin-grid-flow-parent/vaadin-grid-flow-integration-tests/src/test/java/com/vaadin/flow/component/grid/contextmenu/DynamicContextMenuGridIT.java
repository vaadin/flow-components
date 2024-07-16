/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.contextmenu;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/dynamic-context-menu-grid")
public class DynamicContextMenuGridIT extends AbstractComponentIT {

    private static final String OVERLAY_TAG = "vaadin-context-menu-overlay";

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
        verifyClosed();
    }

    @Test
    public void shouldNotOpenContextMenuWhenClickedOnRowWithPersonUnder30Years() {
        grid.getCell(25, 0).contextClick();
        verifyClosed();
    }

    @Test
    public void shouldOpenContextMenuWhenClickedOnRowWithPersonAbove30Years() {
        grid.getCell(40, 0).contextClick();
        verifyOpened();

        Assert.assertEquals("Person 40",
                $(OVERLAY_TAG).first().getAttribute("innerText"));

        $("body").first().click();
        verifyClosed();
    }

    private void verifyOpened() {
        waitForElementPresent(By.tagName(OVERLAY_TAG));
    }

    private void verifyClosed() {
        waitForElementNotPresent(By.tagName(OVERLAY_TAG));
    }
}
