/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.data.renderer.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-renderer-flow/lit-renderer-declared-template")
public class LitRendererDeclaredTemplateIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void shouldBindEachTemplateArgument() {
        String appId = findElement(By.id("app-id")).getText();

        for (int i = 0; i < LitRendererDeclaredTemplatePage.ITEMS.size(); i++) {
            WebElement renderedItem = getRenderedItem(i);
            String index = String.valueOf(i);

            Assert.assertEquals("main",
                    renderedItem.getDomAttribute("data-root"));
            Assert.assertEquals(appId,
                    renderedItem.getDomAttribute("data-app-id"));
            Assert.assertEquals(index,
                    renderedItem.getDomAttribute("data-index"));
            Assert.assertEquals(index,
                    renderedItem.getDomAttribute("data-model-index"));
            Assert.assertEquals(LitRendererDeclaredTemplatePage.ITEMS.get(i),
                    renderedItem.getDomAttribute("data-value"));
            Assert.assertEquals("first",
                    renderedItem.getDomAttribute("data-arg0"));
            Assert.assertEquals("42",
                    renderedItem.getDomAttribute("data-arg1"));
        }
    }

    @Test
    public void shouldBindItemKeyAndFunctions() {
        getRenderedItem(1).click();

        String[] clicked = findElement(By.id("clicked")).getText().split(":");
        Assert.assertEquals("b", clicked[0]);
        // The key passed to the template is the key of the clicked item
        Assert.assertEquals(clicked[2], clicked[1]);
    }

    @Test
    public void shouldBindLiveDirective() {
        WebElement checkbox = findElement(By.cssSelector("#item-0 input"));
        checkbox.click();
        Assert.assertTrue(checkbox.isSelected());

        clickElementWithJs("refresh");

        checkbox = findElement(By.cssSelector("#item-0 input"));
        Assert.assertFalse(checkbox.isSelected());
    }

    private WebElement getRenderedItem(int index) {
        return findElement(By.cssSelector("#item-" + index + " .args"));
    }
}
