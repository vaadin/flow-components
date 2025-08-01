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
package com.vaadin.flow.component.treegrid.it;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-basic-features")
public class TreeGridBasicFeaturesIT extends AbstractTreeGridIT {

    @Parameters
    public static Collection<String> getDataProviders() {
        return Arrays.asList("LazyHierarchicalDataProvider",
                "TreeDataProvider");
    }

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test
    public void toggle_collapse_server_side() {
        Assert.assertEquals(3, getTreeGrid().getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        findElement(By.id("expand-0-0")).click();
        Assert.assertEquals(6, getTreeGrid().getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        // expanding already expanded item should have no effect
        findElement(By.id("expand-0-0")).click();
        Assert.assertEquals(6, getTreeGrid().getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        findElement(By.id("collapse-0-0")).click();
        Assert.assertEquals(3, getTreeGrid().getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        // collapsing the same item twice should have no effect
        findElement(By.id("collapse-0-0")).click();
        Assert.assertEquals(3, getTreeGrid().getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        findElement(By.id("expand-1-1")).click();
        // 1 | 1 not yet visible, shouldn't immediately expand anything
        Assert.assertEquals(3, getTreeGrid().getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        findElement(By.id("expand-0-0")).click();
        // 1 | 1 becomes visible and is also expanded
        Assert.assertEquals(9, getTreeGrid().getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "2 | 0", "2 | 1",
                "2 | 2", "1 | 2" });

        // collapsing a leaf should have no effect
        findElement(By.id("collapse-2-1")).click();
        Assert.assertEquals(9, getTreeGrid().getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "2 | 0", "2 | 1",
                "2 | 2", "1 | 2" });

        // collapsing 0 | 0 should collapse the expanded 1 | 1
        findElement(By.id("collapse-0-0")).click();
        Assert.assertEquals(3, getTreeGrid().getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        // expand 0 | 0 recursively
        findElement(By.id("expand-0-0-recursively")).click();
        Assert.assertEquals(15, getTreeGrid().getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "1 | 0", "2 | 0" });

        // collapse 0 | 0 recursively
        findElement(By.id("collapse-0-0-recursively")).click();
        Assert.assertEquals(3, getTreeGrid().getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        // expanding 0 | 0 should result in 3 additional nodes after recursive
        // collapse
        findElement(By.id("expand-0-0")).click();
        Assert.assertEquals(6, getTreeGrid().getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        checkLogsForErrors();
    }

    @Test
    public void pending_expands_cleared_when_data_provider_set() {
        findElement(By.id("expand-1-1")).click();
        findElement(By.id("LazyHierarchicalDataProvider")).click();

        getTreeGrid().expandWithClick(0);
        Assert.assertEquals(6, getTreeGrid().getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });
    }

    @Test
    public void non_leaf_collapse_on_click() {
        Assert.assertEquals(3, getTreeGrid().getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        // Should expand "0 | 0"
        getTreeGrid().expandWithClick(0);
        Assert.assertEquals(6, getTreeGrid().getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        // Should collapse "0 | 0"
        getTreeGrid().collapseWithClick(0);
        Assert.assertEquals(3, getTreeGrid().getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });
    }

    @Test
    public void keyboard_navigation() {
        getTreeGrid().getCell(0, 0).focus();

        // Should expand "0 | 0" without moving focus
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        waitUntil(b -> getTreeGrid().getRowCount() != 3, 1);
        Assert.assertEquals(6, getTreeGrid().getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        // Should navigate 2 times down to "1 | 1" row
        new Actions(getDriver()).sendKeys(Keys.DOWN, Keys.DOWN).perform();
        Assert.assertEquals(6, getTreeGrid().getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        // Should expand "1 | 1" without moving focus
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        waitUntil(b -> getTreeGrid().getRowCount() != 6, 1);
        Assert.assertEquals(9, getTreeGrid().getRowCount());
        assertCellTexts(2, 0,
                new String[] { "1 | 1", "2 | 0", "2 | 1", "2 | 2", "1 | 2" });

        // Should collapse "1 | 1"
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        waitUntil(b -> getTreeGrid().getRowCount() != 9, 1);
        Assert.assertEquals(6, getTreeGrid().getRowCount());
        assertCellTexts(2, 0, new String[] { "1 | 1", "1 | 2", "0 | 1" });

        // Should navigate to "0 | 0"
        new Actions(getDriver()).sendKeys(Keys.UP, Keys.UP).perform();
        Assert.assertEquals(6, getTreeGrid().getRowCount());
        assertCellTexts(0, 0,
                new String[] { "0 | 0", "1 | 0", "1 | 1", "1 | 2", "0 | 1" });

        // Should collapse "0 | 0"
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        waitUntil(b -> getTreeGrid().getRowCount() != 6, 1);
        Assert.assertEquals(3, getTreeGrid().getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        // Nothing should happen
        new Actions(getDriver()).sendKeys(Keys.LEFT).perform();
        waitUntil(b -> getTreeGrid().getRowCount() == 3, 1);
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        checkLogsForErrors();
    }

    @Test
    public void keyboard_navigation_row_focus_expand_collapse() {
        getTreeGrid().getCell(0, 0).focus();

        // Enter row focus mode
        new Actions(getDriver()).sendKeys(Keys.LEFT).perform();

        // Should expand "0 | 0" on right arrow
        new Actions(getDriver()).sendKeys(Keys.RIGHT).perform();
        Assert.assertEquals(6, getTreeGrid().getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        // Should collapse "0 | 0" on left arrow
        new Actions(getDriver()).sendKeys(Keys.LEFT).perform();
        Assert.assertEquals(3, getTreeGrid().getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });
    }

    @Test
    public void keyboard_selection() {
        getTreeGrid().getCell(0, 0).focus();

        // Should expand "0 | 0" without moving focus
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        waitUntil(b -> getTreeGrid().getRowCount() != 3, 1);
        Assert.assertEquals(6, getTreeGrid().getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        // Should navigate 1 time right and 2 times down to "1 | 1" row
        new Actions(getDriver()).sendKeys(Keys.RIGHT, Keys.DOWN, Keys.DOWN)
                .perform();
        Assert.assertEquals(6, getTreeGrid().getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        // Should select "1 | 1"
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        waitUntil(b -> getTreeGrid().getRow(2).isSelected(), 1);

        // Should move focus but not selection
        new Actions(getDriver()).sendKeys(Keys.UP).perform();
        assertFalse(getTreeGrid().getRow(1).isSelected());
        assertTrue(getTreeGrid().getRow(2).isSelected());

        // Should select "1 | 0" without moving focus
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        waitUntil(b -> getTreeGrid().getRow(1).isSelected(), 1);
        assertFalse(getTreeGrid().getRow(2).isSelected());

        checkLogsForErrors();
    }

    @Test
    public void changing_hierarchy_column() {
        assertTrue(getTreeGrid().hasExpandToggle(0, 0));
        assertFalse(getTreeGrid().hasExpandToggle(0, 1));

        findElement(By.id("set-hierarchy-column-depth")).click();

        assertFalse(getTreeGrid().hasExpandToggle(0, 0));
        assertTrue(getTreeGrid().hasExpandToggle(0, 1));

        findElement(By.id("set-hierarchy-column-id")).click();

        assertTrue(getTreeGrid().hasExpandToggle(0, 0));
        assertFalse(getTreeGrid().hasExpandToggle(0, 1));
    }

    @Test
    public void expand_and_collapse_listeners() {
        findElement(By.id("add-expand-listener")).click();
        findElement(By.id("add-collapse-listener")).click();

        assertFalse(
                logContainsText("Item(s) expanded (from client: true): 0 | 0"));
        assertFalse(logContainsText(
                "Item(s) collapsed (from client: true): 0 | 0"));

        getTreeGrid().expandWithClick(0);

        assertTrue(
                logContainsText("Item(s) expanded (from client: true): 0 | 0"));
        assertFalse(logContainsText(
                "Item(s) collapsed (from client: true): 0 | 0"));

        getTreeGrid().collapseWithClick(0);

        assertTrue(
                logContainsText("Item(s) expanded (from client: true): 0 | 0"));
        assertTrue(logContainsText(
                "Item(s) collapsed (from client: true): 0 | 0"));

        findElement(By.id("expand-0-0")).click();

        assertTrue(logContainsText(
                "Item(s) expanded (from client: false): 0 | 0"));
        assertFalse(logContainsText(
                "Item(s) collapsed (from client: false): 0 | 0"));

        findElement(By.id("collapse-0-0")).click();

        assertTrue(logContainsText(
                "Item(s) expanded (from client: false): 0 | 0"));
        assertTrue(logContainsText(
                "Item(s) collapsed (from client: false): 0 | 0"));
    }

    @Test
    public void expanded_nodes_stay_expanded_when_parent_expand_state_is_toggled() {
        getTreeGrid().expandWithClick(0);
        getTreeGrid().expandWithClick(1);
        getTreeGrid().collapseWithClick(0);
        getTreeGrid().expandWithClick(0);
        assertCellTexts(0, 0, new String[] { "0 | 0", "1 | 0", "2 | 0", "2 | 1",
                "2 | 2", "1 | 1", "1 | 2", "0 | 1", "0 | 2" });
        Assert.assertEquals(9, getTreeGrid().getRowCount());

        getTreeGrid().expandWithClick(7);
        getTreeGrid().expandWithClick(8);
        getTreeGrid().collapseWithClick(7);
        getTreeGrid().collapseWithClick(0);
        getTreeGrid().expandWithClick(1);
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "1 | 0", "2 | 0",
                "2 | 1", "2 | 2", "1 | 1", "1 | 2", "0 | 2" });
        Assert.assertEquals(9, getTreeGrid().getRowCount());
    }

    @Test
    public void expand_and_collapse_rowLevelStateUpdated() {
        assertRowLevel(0, new int[] { 0, 0, 0 });

        getTreeGrid().expandWithClick(0);
        assertRowLevel(0, new int[] { 0, 1, 1, 1, 0, 0 });

        getTreeGrid().collapseWithClick(0);
        assertRowLevel(0, new int[] { 0, 0, 0 });
    }

    @Test
    public void expand_and_collapse_rowExpandedStateUpdated() {
        assertRowExpanded(0, new boolean[] { false, false, false });

        getTreeGrid().expandWithClick(0);
        assertRowExpanded(0,
                new boolean[] { true, false, false, false, false, false });

        getTreeGrid().collapseWithClick(0);
        assertRowExpanded(0, new boolean[] { false, false, false });
    }
}
