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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/drag-and-drop")
public class DragAndDropGridIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
    }

    @Test
    public void startDragging_dragStartFired() {
        fireDragStart(2);
        assertMessages("2", "", "");
    }

    @Test
    public void rowsNotDraggable_startDragging_eventNotFired() {
        click("toggle-rows-draggable");
        fireDragStart(2);
        assertMessages("", "", "");
    }

    @Test
    public void dragEnd_eventFired() {
        fireDragEnd();
        assertMessages("", "grid", "");
    }

    @Test
    public void noDropMode_dropOnRow_dropEventNotFired() {
        fireDrop(3, "on-top");
        assertMessages("", "", "");
    }

    @Test
    public void dropModeOnTop_dropOnRowAbove_eventFired_dropLocationOnTop() {
        click("ON_TOP");
        fireDrop(3, "above");
        assertMessages("", "", "grid ON_TOP 3");
    }

    @Test
    public void dropModeBetween_dropOnRowMiddle_eventFired_dropLocationBelow() {
        click("BETWEEN");
        fireDrop(3, "on-top");
        assertMessages("", "", "grid BELOW 3");
    }

    @Test
    public void dropModeOnTopOrBetween_dropOnRowMiddle_eventFired_dropLocationOnTop() {
        click("ON_TOP_OR_BETWEEN");
        fireDrop(3, "on-top");
        assertMessages("", "", "grid ON_TOP 3");
    }

    @Test
    public void dropModeOnTopOrBetween_dropOnRowAbove_eventFired_dropLocationAbove() {
        click("ON_TOP_OR_BETWEEN");
        fireDrop(3, "above");
        assertMessages("", "", "grid ABOVE 3");
    }

    @Test
    public void dropModeOnGrid_dropOnRow_eventFired_dropLocationEmpty() {
        click("ON_GRID");
        fireDrop(3, "above");
        assertMessages("", "", "grid EMPTY null");
    }

    @Test
    public void dropEventHasDefaultTransferText() {
        click("ON_GRID");
        fireDragStart(2);
        fireDrop(0, "on-top");
        Assert.assertEquals("2",
                findElement(By.id("drop-data-text-message")).getText());
    }

    @Test
    public void multiSelectGrid_dragStartEventHasAllDraggedItems() {
        click("ON_GRID");
        click("multiselect");
        fireDragStart(0);
        assertMessages("0,1", "", "");
    }

    @Test
    public void multiSelectGrid_defaultDragTextDataGenerator_dropEventHasGeneratedTransferText() {
        click("ON_GRID");
        click("multiselect");
        fireDragStart(0);
        fireDrop(2, "on-top");
        Assert.assertEquals("0-1",
                findElement(By.id("drop-data-text-message")).getText());
    }

    @Test
    public void setDragTextDataGenerator_dropEventHasGeneratedTransferText() {
        click("set-generators");
        click("ON_GRID");
        fireDragStart(2);
        fireDrop(0, "on-top");
        Assert.assertEquals("2 foo",
                findElement(By.id("drop-data-text-message")).getText());
    }

    @Test
    public void setCustomTypeDragDataGenerator_dropEventHasGeneratedTransferData() {
        click("set-generators");
        click("ON_GRID");
        fireDragStart(2);
        fireDrop(0, "on-top");
        Assert.assertEquals("<b>2</b>",
                findElement(By.id("drop-data-html-message"))
                        .getAttribute("innerHTML"));
    }

    @Test
    public void setSelectionDragData_dropEventHasDefaultTransferData() {
        click("set-selection-drag-data");
        click("ON_GRID");
        fireDragStart(0);
        fireDrop(0, "on-top");
        Assert.assertEquals("0",
                findElement(By.id("drop-data-text-message")).getText());
    }

    @Test
    public void setSelectionDragData_dropEventHasCustomTransferData() {
        click("set-selection-drag-data");
        click("ON_GRID");
        click("multiselect");
        fireDragStart(0);
        fireDrop(0, "on-top");
        Assert.assertEquals("selection-drag-data",
                findElement(By.id("drop-data-text-message")).getText());
    }

    @Test
    public void setDragFilter_draggable() {
        click("set-filters");
        fireDragStart(1);
        assertMessages("1", "", "");
    }

    @Test
    public void setDragFilter_undraggable() {
        click("set-filters");
        fireDragStart(0);
        assertMessages("", "", "");
    }

    @Test
    public void setDropFilter_droppable() {
        click("ON_TOP");
        click("set-filters");
        fireDrop(2, "on-top");
        assertMessages("", "", "grid ON_TOP 2");
    }

    @Test
    public void setDropFilter_undroppable() {
        click("ON_TOP");
        click("set-filters");
        fireDrop(0, "on-top");
        assertMessages("", "", "");
    }

    @Test
    public void setDropFilter_undroppable_noDropMode() {
        click("set-filters");
        fireDrop(2, "on-top");
        assertMessages("", "", "");
    }

    private void assertMessages(String expectedStartMessage,
            String expectedEndMessage, String expectedDropMessage) {
        Assert.assertEquals(expectedStartMessage,
                findElement(By.id("start-message")).getText());
        Assert.assertEquals(expectedEndMessage,
                findElement(By.id("end-message")).getText());
        Assert.assertEquals(expectedDropMessage,
                findElement(By.id("drop-message")).getText());
    }

    private WebElement getCellContent(int rowIndex) {
        return (WebElement) executeScript("return arguments[0]._content",
                grid.getCell(rowIndex, 0));
    }

    private void fireDragStart(int rowIndex) {
        executeScript("fireDragStart(arguments[0])", getCellContent(rowIndex));
    }

    private void fireDragEnd() {
        executeScript("fireDragEnd(arguments[0])", grid);
    }

    private void fireDrop(int rowIndex, String dropLocation) {
        executeScript("fireDrop(arguments[0], arguments[1])",
                getCellContent(rowIndex), dropLocation);
    }

    private void click(String id) {
        findElement(By.id(id)).click();
    }
}
