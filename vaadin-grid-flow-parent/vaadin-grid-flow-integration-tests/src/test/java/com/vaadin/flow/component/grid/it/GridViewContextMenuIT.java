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
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/context-menu")
public class GridViewContextMenuIT extends AbstractComponentIT {

    private static final String OVERLAY_TAG = "vaadin-context-menu-overlay";

    @Before
    public void init() {
        open();
    }

    @Test
    public void contextMenu() {
        GridElement grid = $(GridElement.class).id("context-menu-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        assertFirstCells(grid, "Person 1", "Person 2", "Person 3", "Person 4");

        grid.getCell(2, 0).contextClick();
        $("vaadin-context-menu-item").first().click(); // Update button
        assertFirstCells(grid, "Person 1", "Person 2", "Person 3 Updated",
                "Person 4");

        grid.getCell(1, 0).contextClick();
        $("vaadin-context-menu-item").get(1).click(); // Remove button
        assertFirstCells(grid, "Person 1", "Person 3 Updated", "Person 4",
                "Person 5");
    }

    @Test
    public void openSubMenu_insertRowBefore_rowIsInserted() {
        clickSubmenu(0, 0);

        assertFirstCells($(GridElement.class).id("context-submenu-grid"),
                "Person 501", "Person 1", "Person 2", "Person 3", "Person 4");
    }

    @Test
    public void openSubMenu_insertRowAfter_rowIsInserted() {
        clickSubmenu(0, 1);

        assertFirstCells($(GridElement.class).id("context-submenu-grid"),
                "Person 1", "Person 501", "Person 2", "Person 3", "Person 4");
    }

    private void clickSubmenu(int menuIndex, int subMenuIndex) {
        GridElement grid = $(GridElement.class).id("context-submenu-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        assertFirstCells(grid, "Person 1", "Person 2", "Person 3", "Person 4");

        grid.getCell(0, 0).contextClick();

        verifyOpened(1);

        openSubMenu($(OVERLAY_TAG).first().$("vaadin-context-menu-item")
                .get(menuIndex));

        verifyOpened(2);

        $(OVERLAY_TAG).all().get(1).$("vaadin-context-menu-item")
                .get(subMenuIndex).click();
    }

    private void assertFirstCells(GridElement grid, String... cellContents) {
        IntStream.range(0, cellContents.length).forEach(i -> {
            Assert.assertEquals(cellContents[i], grid.getCell(i, 0).getText());
        });
    }

    private void verifyOpened(int overlayNumber) {
        waitUntil(driver -> $(OVERLAY_TAG).all().size() == overlayNumber);
    }

    private void openSubMenu(WebElement parentItem) {
        executeScript(
                "arguments[0].dispatchEvent(new Event('mouseover', {bubbles:true}))",
                parentItem);
    }
}
