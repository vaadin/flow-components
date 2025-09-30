/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.virtuallist.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-virtual-list/virtual-list-focus")
public class VirtualListFocusIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id("virtual-list"));
    }

    @Test
    public void firstItemShouldBeFocusableOnInitialLoad() {
        // Wait for the virtual list to render items
        waitForElementPresent(By.id("item-Item-0"));

        // Click the focus button to ensure focus is attempted
        WebElement focusButton = findElement(By.id("focus-first-button"));
        focusButton.click();

        // Wait for status update
        waitUntil(driver -> {
            WebElement status = findElement(By.id("status"));
            String text = status.getText();
            return text.contains("First item focused")
                    || text.contains("No item found");
        });

        // Check that first item was successfully focused
        WebElement status = findElement(By.id("status"));
        Assert.assertTrue(
                "First item should be focused on initial load. Status: "
                        + status.getText(),
                status.getText().contains("First item focused: item-Item-0"));
    }

    @Test
    public void firstItemShouldBeFocusableAfterReset() {
        // Wait for initial render
        waitForElementPresent(By.id("item-Item-0"));

        // Reset the list (simulates navigation)
        WebElement resetButton = findElement(By.id("reset-button"));
        resetButton.click();

        // Wait for the focus attempt after reset
        waitUntil(driver -> {
            WebElement status = findElement(By.id("status"));
            String text = status.getText();
            return text.contains("First item focused")
                    || text.contains("No item found");
        }, 5);

        // Verify first item is focused after reset
        WebElement status = findElement(By.id("status"));
        Assert.assertTrue(
                "First item should be focused after reset. Status: "
                        + status.getText(),
                status.getText().contains("First item focused: item-Item-0"));
    }

    @Test
    public void focusShouldNotBeLostDueToUnnecessaryRangeUpdate() {
        // Wait for initial render
        waitForElementPresent(By.id("item-Item-0"));

        // Focus the first item
        WebElement focusButton = findElement(By.id("focus-first-button"));
        focusButton.click();

        // Wait for focus to be set (includes the 100ms delay from focusFirstItem)
        waitUntil(driver -> {
            WebElement status = findElement(By.id("status"));
            return status.getText().contains("First item focused");
        });

        // Wait a bit more to ensure focus has settled after the async operation
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Execute JavaScript to verify the focused element hasn't changed
        // This checks that no unexpected re-render occurred
        Object result = executeScript("""
            const activeId = document.activeElement.id;
            return activeId === 'item-Item-0' ? 'still-focused' : 'focus-lost: ' + activeId;
            """);

        Assert.assertEquals("Focus should remain on first item",
                "still-focused", result.toString());

        // Verify the element still exists in DOM (wasn't replaced)
        WebElement firstItem = findElement(By.id("item-Item-0"));
        Assert.assertNotNull("First item should still exist in DOM", firstItem);

        // Double-check by attempting to interact with it
        Assert.assertTrue("First item should be displayed",
                firstItem.isDisplayed());
    }
}
