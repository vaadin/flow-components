/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("context-menu-grid")
public class ContextMenuGridIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
    }

    @Test
    public void contextClickOnRow_itemClickGetsTargetItem() {
        grid.getCell(56, 1).contextClick();
        $("vaadin-item").first().click();
        assertMessage("Person 56");
    }

    @Test
    public void contextClickOnHeader_targetItemReturnsNull() {
        grid.getHeaderCell(0).contextClick();
        $("vaadin-item").first().click();
        assertMessage("null target item");
    }

    @Test
    public void dontOpenContextMenu_getTargetItem_throws() {
        $("button").id("show-name").click();
        assertThrows();
    }

    @Test
    public void openAndCloseContextMenu_getTargetItem_throws() {
        grid.getCell(10, 1).contextClick();
        $("vaadin-item").first().click();
        $("button").id("show-name").click();
        assertThrows();
    }

    @Test
    public void setOpenOnClick_clickOnRow_itemClickGetsTargetItem() {
        $("button").id("toggle-open-on-click").click();
        grid.getCell(14, 0).click();
        $("vaadin-item").first().click();
        assertMessage("Person 14");
    }

    @Test
    public void setOpenOnClick_contextClickOnRow_getTargetItem_throws() {
        $("button").id("toggle-open-on-click").click();
        grid.getCell(22, 0).contextClick();

        $("button").id("show-name").click();
        assertThrows();
    }

    @Test
    public void gridInATemplateWithContextMenu_itemClickGetsTargetItem() {
        GridElement gridInATemplate = $("grid-in-a-template").first()
                .$(GridElement.class).first();
        gridInATemplate.getCell(18, 0).contextClick();
        $("vaadin-item").first().click();
        assertMessage("Item 18");
    }

    private void assertThrows() {
        assertMessage("java.lang.IllegalStateException "
                + "Context menu target item is available "
                + "only when a context menu is open");
    }

    private void assertMessage(String expected) {
        Assert.assertEquals(expected, $("label").id("message").getText());
    }

}
