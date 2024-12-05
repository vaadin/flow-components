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
package com.vaadin.flow.component.virtuallist.tests;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.virtuallist.testbench.VirtualListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-virtual-list/selection")
public class VirtualListSelectionIT extends AbstractComponentIT {
    private VirtualListElement virtualList;
    private TestBenchElement singleSelectionModeButton;
    private TestBenchElement singleSelectionModeDeselectionDisallowedButton;
    private TestBenchElement multiSelectionModeButton;
    private TestBenchElement selectFirstButton;
    private TestBenchElement deselectAllButton;
    private TestBenchElement selectedIndexes;

    @Before
    public void init() {
        open();
        virtualList = $(VirtualListElement.class).waitForFirst();
        singleSelectionModeButton = $("button").id("single-selection-mode");
        singleSelectionModeDeselectionDisallowedButton = $("button")
                .id("single-selection-mode-deselection-disallowed");
        multiSelectionModeButton = $("button").id("multi-selection-mode");
        selectFirstButton = $("button").id("select-first");
        deselectAllButton = $("button").id("deselect-all");
        selectedIndexes = $("div").id("selected-indexes");
    }

    @Test
    public void select_shouldNotSelect() {
        virtualList.select(0);

        Assert.assertFalse(virtualList.isRowSelected(0));
    }

    @Test
    public void singleSelectionMode_select() {
        singleSelectionModeButton.click();
        virtualList.select(0);
        Assert.assertTrue(virtualList.isRowSelected(0));
    }

    @Test
    public void singleSelectionMode_selectAnother() {
        singleSelectionModeButton.click();
        virtualList.select(0);
        virtualList.select(1);
        Assert.assertFalse(virtualList.isRowSelected(0));
        Assert.assertTrue(virtualList.isRowSelected(1));
    }

    @Test
    public void singleSelectionMode_deselect() {
        singleSelectionModeButton.click();
        virtualList.select(0);
        virtualList.deselect(0);
        Assert.assertFalse(virtualList.isRowSelected(0));
    }

    @Test
    public void singleSelectionModeDeselectionDisallowed_deselectionNotAllowed() {
        singleSelectionModeDeselectionDisallowedButton.click();
        Assert.assertFalse(virtualList.isRowSelected(0));
        virtualList.select(0);
        virtualList.deselect(0);
        Assert.assertTrue(virtualList.isRowSelected(0));
    }

    @Test
    public void multiSelectionMode_selectMultiple() {
        multiSelectionModeButton.click();
        virtualList.select(0);
        virtualList.select(2);
        Assert.assertTrue(virtualList.isRowSelected(0));
        Assert.assertFalse(virtualList.isRowSelected(1));
        Assert.assertTrue(virtualList.isRowSelected(2));
    }

    @Test
    public void multiSelectionMode_deselect() {
        multiSelectionModeButton.click();
        virtualList.select(0);
        virtualList.select(2);
        virtualList.select(1);
        virtualList.deselect(0);
        Assert.assertFalse(virtualList.isRowSelected(0));
        Assert.assertTrue(virtualList.isRowSelected(1));
        Assert.assertTrue(virtualList.isRowSelected(2));
    }

    @Test
    public void multiSelectionMode_serverSideSelection() {
        multiSelectionModeButton.click();
        selectFirstButton.click();
        virtualList.select(3);
        virtualList.select(80);
        virtualList.deselect(0);

        var selectedIndexesSet = Set.of(selectedIndexes.getText().split(", "));
        Assert.assertEquals(Set.of("3", "80"), selectedIndexesSet);
    }

    @Test
    public void programmaticSelection() {
        singleSelectionModeButton.click();
        selectFirstButton.click();
        Assert.assertTrue(virtualList.isRowSelected(0));

        var selectedIndexesSet = Set.of(selectedIndexes.getText().split(", "));
        Assert.assertEquals(Set.of("0"), selectedIndexesSet);

        deselectAllButton.click();
        Assert.assertFalse(virtualList.isRowSelected(0));
        Assert.assertTrue(selectedIndexes.getText().isEmpty());
    }

    @Test
    public void accessibleName() {
        var firstChildElement = virtualList
                .findElement(By.xpath("child::div[@aria-posinset='1']"));
        Assert.assertEquals("Accessible Item 0",
                firstChildElement.getAttribute("aria-label"));
    }

}
