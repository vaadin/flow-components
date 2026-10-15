/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the TreeGridExpandedKeysPage view.
 */
@TestPath("vaadin-grid/treegrid-expanded-keys")
public class TreeGridExpandedKeysIT extends AbstractComponentIT {

    private TreeGridElement grid;

    private TestBenchElement expandAllButton;

    private TestBenchElement showKeysAfterExpandButton;

    private TestBenchElement scrollToButton;

    private TestBenchElement showKeysAfterFirstScrollButton;

    private TestBenchElement scrollToStartButton;

    private TestBenchElement showKeysAfterSecondScrollButton;

    @Before
    public void init() {
        open();
        waitUntil(e -> $(TreeGridElement.class).exists(), 2);
        grid = $(TreeGridElement.class).first();
        expandAllButton = $("button").id("expand-all");
        showKeysAfterExpandButton = $("button").id("show-keys-after-expand");
        scrollToButton = $("button").id("scroll-to-0-250");
        showKeysAfterFirstScrollButton = $("button")
                .id("show-keys-after-first-scroll");
        scrollToStartButton = $("button").id("scroll-to-0-250");
        showKeysAfterSecondScrollButton = $("button")
                .id("show-keys-after-second-scroll");
    }

    @Test
    public void expandAll_ensureOriginalKeysMatchKeysAfterExpanding() {
        expandAllButton.click();

        String originalKeys = findElement(By.id("originalkeys")).getText();

        // Waits for TreeGrid to finish all its calls
        waitUntil(e -> grid.getRowCount() > 50, 5);

        showKeysAfterExpandButton.click();
        String expandedKeys = findElement(By.id("afterExpandKeys")).getText();
        Assert.assertEquals("Item keys do not match after expanding all",
                originalKeys, expandedKeys);

        scrollToButton.click();
        showKeysAfterFirstScrollButton.click();
        String afterFirstScrollKeys = findElement(By.id("afterFirstScroll"))
                .getText();
        Assert.assertEquals("Item keys do not match after scrolling down",
                originalKeys, afterFirstScrollKeys);

        scrollToStartButton.click();
        showKeysAfterSecondScrollButton.click();
        String afterSecondScrollKeys = findElement(By.id("afterSecondScroll"))
                .getText();
        Assert.assertEquals("Item keys do not match after scrolling to start",
                originalKeys, afterSecondScrollKeys);
    }

}
