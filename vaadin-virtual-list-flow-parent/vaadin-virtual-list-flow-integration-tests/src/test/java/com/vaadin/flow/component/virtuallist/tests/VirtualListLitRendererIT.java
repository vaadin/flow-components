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
package com.vaadin.flow.component.virtuallist.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.component.virtuallist.testbench.VirtualListElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-virtual-list/lit-renderer")
public class VirtualListLitRendererIT extends AbstractComponentIT {

    private VirtualListElement list;

    @Before
    public void init() {
        open();
        list = $(VirtualListElement.class).first();
        waitForElementPresent(By.id("item-0"));
    }

    @Test
    public void shouldRenderFirstItem() {
        WebElement item = list.findElement(By.id("item-0"));
        Assert.assertNotNull(item);
    }

    @Test
    public void shouldRenderLastItem() {
        int rowCount = list.getRowCount();
        list.scrollToRow(rowCount - 1);
        waitForElementPresent(By.id("item-" + (rowCount - 1)));
    }

    @Test
    public void shouldSwitchToComponentRenderer() {
        clickElementWithJs("componentRendererButton");
        waitForElementPresent(By.id("item-0"));
        WebElement item = list.findElement(By.id("item-0"));
        Assert.assertEquals("Component: Item 0", item.getText());
    }

    @Test
    public void shouldSwitchBackToLitRenderer() {
        clickElementWithJs("componentRendererButton");
        clickElementWithJs("litRendererButton");
        WebElement item = list.findElement(By.id("item-0"));
        Assert.assertEquals("Lit: Item 0", item.getText());
    }

    @Test
    public void shouldClickAnItem() {
        waitForElementPresent(By.id("item-0"));
        clickElementWithJs("item-0");
        waitForElementPresent(
                By.cssSelector("vaadin-virtual-list[data-clicked-item=\"0\"]"));
    }

}
