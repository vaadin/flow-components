/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.testutil.TestPath;

import static org.junit.Assert.assertTrue;

@TestPath("vaadin-grid/treegrid-huge-tree-navigation")
public class TreeGridHugeTreeNavigationIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test

    public void keyboard_navigation() {
        getTreeGrid().getCell(0, 0).focus();

        // Should navigate to "Granddad 1" and expand it
        new Actions(getDriver()).sendKeys(Keys.DOWN, Keys.SPACE).perform();
        Assert.assertEquals(6, getTreeGrid().getRowCount());
        assertCellTexts(0, 0, "Granddad 0", "Granddad 1", "Dad 1/0", "Dad 1/1",
                "Dad 1/2", "Granddad 2");

        // Should navigate to and expand "Dad 1/1"
        new Actions(getDriver()).sendKeys(Keys.DOWN, Keys.DOWN, Keys.SPACE)
                .perform();
        assertCellTexts(0, 0, "Granddad 0", "Granddad 1", "Dad 1/0", "Dad 1/1",
                "Son 1/1/0", "Son 1/1/1", "Son 1/1/2", "Son 1/1/3");

        // Should navigate 100 items down
        Keys downKeyArr[] = new Keys[100];
        Arrays.fill(downKeyArr, Keys.DOWN);

        new Actions(getDriver()).sendKeys(downKeyArr).perform();

        assertCellTexts(103, 0, "Son 1/1/99");

        // Should navigate to "Dad 1/1" back
        getTreeGrid().sendKeys(Keys.chord(Keys.CONTROL, Keys.HOME), Keys.DOWN,
                Keys.DOWN, Keys.DOWN);
        assertCellTexts(3, 0, "Dad 1/1");

        // Should collapse "Dad 1/1"
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        assertCellTexts(0, 0, "Granddad 0", "Granddad 1", "Dad 1/0", "Dad 1/1",
                "Dad 1/2", "Granddad 2");

        // Should navigate to "Granddad 1"
        new Actions(getDriver()).sendKeys(Keys.UP, Keys.UP).perform();

        // Should collapse "Granddad 1"
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        assertCellTexts(0, 0, "Granddad 0", "Granddad 1", "Granddad 2");

        checkLogsForErrors();
    }

    @Test
    public void no_exception_when_calling_expand_and_collapse_same_time() {
        getTreeGrid().getCell(0, 0).focus();
        new Actions(getDriver()).sendKeys(Keys.SPACE, Keys.SPACE).perform();
        checkLogsForErrors();
    }

    @Test
    public void can_toggle_collapse_on_row_that_is_no_longer_in_cache() {
        getTreeGrid().getCell(0, 0).focus();

        // Expand 2 levels
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        waitUntil(b -> getTreeGrid().getNumberOfExpandedRows() == 1, 1);
        new Actions(getDriver()).sendKeys(Keys.DOWN, Keys.SPACE).perform();
        waitUntil(b -> getTreeGrid().getNumberOfExpandedRows() == 2, 1);
        waitUntil(b -> getTreeGrid().getRowCount() > 6, 1);
        assertCellTexts(200, 0, "Son 0/0/198");
        // Jump into view
        new Actions(getDriver()).sendKeys(Keys.RIGHT).perform();
        new Actions(getDriver()).sendKeys(Keys.LEFT).perform();
        assertTrue(getTreeGrid().getFirstVisibleRowIndex() == 1);

        // Collapse
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        waitUntil(b -> getTreeGrid().getNumberOfExpandedRows() == 1, 2);
        Assert.assertEquals(6, getTreeGrid().getRowCount());

        // Expand
        new Actions(getDriver()).sendKeys(Keys.SPACE, Keys.UP).perform();
        assertCellTexts(200, 0, "Son 0/0/198");
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        Assert.assertEquals(306, getTreeGrid().getRowCount());
    }

}
