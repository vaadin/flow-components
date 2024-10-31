/*
 * Copyright 2000-2024 Vaadin Ltd.
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

@TestPath("vaadin-renderer-flow/component-renderer-in-new-thread")
public class ComponentRendererInNewThreadIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void addComponentRendererBeforeAttach_componentIsRendered() {
        findElement(By.id("add-component-renderer-before-attach")).click();

        WebElement component = waitUntil(driver -> findElement(
                By.tagName("lit-renderer-test-component")));

        WebElement item0 = component.findElement(By.id("item-0"));
        Assert.assertNotNull(item0);
        Assert.assertEquals("Item", item0.getText());
    }

    @Test
    public void addComponentRendererAfterAttach_componentIsRendered() {
        findElement(By.id("add-component-renderer-after-attach")).click();

        WebElement component = waitUntil(driver -> findElement(
                By.tagName("lit-renderer-test-component")));

        WebElement item0 = component.findElement(By.id("item-0"));
        Assert.assertNotNull(item0);
        Assert.assertEquals("Item", item0.getText());
    }
}
