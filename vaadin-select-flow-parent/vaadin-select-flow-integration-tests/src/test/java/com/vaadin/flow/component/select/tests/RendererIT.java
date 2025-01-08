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
package com.vaadin.flow.component.select.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-select/")
public class RendererIT extends AbstractSelectIT {

    @Test
    public void testRenderer_componentRendererSet_rendersComponentsThatWork() {
        page.clickRendererButton();

        runRendererTestPattern();

        page.clickRendererButton();

        List<SelectElement.ItemElement> items = selectElement.getItems();
        Assert.assertEquals("invalid number of items",
                getInitialNumberOfItems(), items.size());

        for (int i = 0; i < items.size(); i++) {
            SelectElement.ItemElement itemElement = items.get(i);
            Assert.assertEquals("invalid key", i + 1 + "",
                    itemElement.getPropertyString("value"));
            Assert.assertEquals("invalid text", "Item-" + i + "-UPDATED",
                    itemElement.getText());
        }

    }

    private void runRendererTestPattern() {
        List<SelectElement.ItemElement> items = selectElement.getItems();

        Assert.assertEquals("Invalid number of items",
                getInitialNumberOfItems(), items.size());

        for (int i = 0; i < items.size(); i++) {
            SelectElement.ItemElement item = items.get(i);

            TestBenchElement span = item.findElement(By.tagName("span"));
            List<WebElement> buttons = item.findElements(By.tagName("button"));

            Assert.assertEquals(2, buttons.size());
            Assert.assertEquals("Invalid text", "Item-" + i, span.getText());
            Assert.assertEquals("Invalid button text", "Update-" + i,
                    buttons.get(0).getText());
            Assert.assertEquals("Invalid button text", "Remove button " + i,
                    buttons.get(1).getText());

            // remove button
            buttons.get(1).click();
            waitUntil(e -> {
                List<WebElement> bts = item.findElements(By.tagName("button"));
                return 1 == bts.size();
            }, 200);

            // update click causes refreshItem which renders item again
            buttons.get(0).click();
            final String expected = "Item-" + i + "-UPDATED";
            waitUntil(e -> {
                TestBenchElement elm = item.findElement(By.tagName("span"));
                return expected.equals(elm.getText());
            }, 200);
        }
    }

    @Test
    public void testRenderer_initialComponentRendererSet_rendersComponentsThatWork() {
        openWithExtraParameter("renderer");

        runRendererTestPattern();
    }

    @Override
    protected int getInitialNumberOfItems() {
        return 3;
    }
}
