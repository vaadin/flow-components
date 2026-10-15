/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import elemental.json.Json;
import elemental.json.JsonObject;

@TestPath("vaadin-grid/grid-client-item-toggle-event")
public class GridClientItemToggleEventIT extends AbstractComponentIT {
    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
    }

    @After
    public void tearDown() {
        shiftUp();
    }

    @Test
    public void toggleWithClick_eventIsFired() {
        grid.select(0);
        assertEvent("Item 0", true, false);

        grid.deselect(0);
        assertEvent("Item 0", false, false);
    }

    @Test
    public void toggleWithShiftClick_eventIsFired() {
        shiftDown();
        grid.select(0);
        assertEvent("Item 0", true, true);

        grid.deselect(0);
        assertEvent("Item 0", false, true);
    }

    private void assertEvent(String item, boolean isSelected,
            boolean isShiftKey) {
        List<WebElement> records = findElements(
                By.cssSelector("#event-log > div"));
        Assert.assertEquals(
                "GridClientItemToggleEvent should be fired only once", 1,
                records.size());

        JsonObject record = Json.parse(records.get(0).getText());
        Assert.assertTrue("isFromClient should be true",
                record.getBoolean("isFromClient"));
        Assert.assertEquals("Item should match the toggled item", item,
                record.getString("item"));
        Assert.assertEquals("isSelected should match the selected state",
                isSelected, record.getBoolean("isSelected"));
        Assert.assertEquals("isShiftKey should match the shift key state",
                isShiftKey, record.getBoolean("isShiftKey"));

        findElement(By.id("clear-event-log")).click();
    }

    private void shiftDown() {
        new Actions(getDriver()).keyDown(Keys.SHIFT).perform();
    }

    private void shiftUp() {
        new Actions(getDriver()).keyUp(Keys.SHIFT).perform();
    }
}
