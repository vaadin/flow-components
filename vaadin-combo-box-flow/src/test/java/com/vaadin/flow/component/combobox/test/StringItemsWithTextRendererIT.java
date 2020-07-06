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
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("string-items-text-renderer")
public class StringItemsWithTextRendererIT extends AbstractComponentIT {

    @Test
    public void stringItemsAreRendered() {
        open();

        findElement(By.id("list")).sendKeys(Keys.ARROW_DOWN);

        WebElement overlay = findElement(
                By.tagName("vaadin-combo-box-overlay"));
        WebElement content = getInShadowRoot(overlay, By.id("content"));
        WebElement selector = getInShadowRoot(content, By.id("selector"));
        List<String> items = selector
                .findElements(By.tagName("vaadin-combo-box-item")).stream()
                .map(item -> getInShadowRoot(item,
                        By.cssSelector("flow-component-renderer")))
                .map(WebElement::getText).collect(Collectors.toList());
        Assert.assertEquals(
                "Unexpected items size. The rendered items size must be 2", 2,
                items.size());
        Assert.assertEquals("Unexpected rendered the first item text", "foo",
                items.get(0));
        Assert.assertEquals("Unexpected rendered the second item text", "bar",
                items.get(1));

        findElement(By.id("list")).sendKeys(Keys.ARROW_DOWN, Keys.ENTER);

        Assert.assertEquals("Unexpected selected item text", "foo",
                findElement(By.id("info")).getText());
    }
}
