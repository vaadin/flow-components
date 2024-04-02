/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

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
