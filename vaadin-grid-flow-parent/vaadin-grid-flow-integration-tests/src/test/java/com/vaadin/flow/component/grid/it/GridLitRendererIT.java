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

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/lit-renderer")
public class GridLitRendererIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
    }

    @Test
    public void shouldRenderFirstItem() {
        WebElement item = grid.findElement(By.id("item-0"));
        Assert.assertEquals("Lit: Item 0", item.getText());
    }

    @Test
    public void shouldRenderLastItem() {
        int rowCount = grid.getRowCount();
        grid.scrollToRow(rowCount - 1);
        WebElement item = grid.findElement(By.id("item-" + (rowCount - 1)));
        Assert.assertNotNull(item);
    }

    @Test
    public void shouldSwitchToComponentRenderer() {
        clickElementWithJs("componentRendererButton");
        clickElementWithJs("item-0");
        WebElement details = grid.findElement(By.id("details-0"));
        Assert.assertEquals("Component: Item details 0", details.getText());
    }

    @Test
    public void shouldSwitchBackToLitRenderer() {
        clickElementWithJs("componentRendererButton");
        clickElementWithJs("litRendererButton");
        clickElementWithJs("item-0");
        WebElement item = grid.findElement(By.id("details-0"));
        Assert.assertEquals("Lit: Item details 0", item.getText());
    }

    @Test
    public void shouldEnterEditMode() {
        clickElementWithJs("toggleEditButton");
        Assert.assertEquals("Editor component", grid.getCell(0, 0).getText());
    }

    @Test
    public void shouldExitEditMode() {
        clickElementWithJs("toggleEditButton");
        clickElementWithJs("toggleEditButton");
        Assert.assertEquals("Lit: Item 0", grid.getCell(0, 0).getText());
    }

    @Test
    public void shouldEnterEditModeAfterReattach() {
        clickElementWithJs("toggleAttachedButton");
        clickElementWithJs("toggleAttachedButton");
        clickElementWithJs("toggleEditButton");
        grid = $(GridElement.class).first();
        Assert.assertEquals("Editor component", grid.getCell(0, 0).getText());
    }
}
