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

@TestPath("vaadin-renderer-flow/lit-renderer-property-namespace")
public class LitRendererPropertyNamespaceIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void setLitRenderers_componentsAreRendered() {
        findElement(By.id("set-lit-renderers")).click();

        findElements(By.tagName("lit-renderer-test-component"))
                .forEach((component) -> {
                    WebElement item = findElement(By.className("main"));
                    Assert.assertEquals("Default renderer: 0", item.getText());

                    WebElement details = findElement(By.className("details"));
                    Assert.assertEquals("Details renderer: 0",
                            details.getText());
                });
    }

    @Test
    public void setComponentRenderers_componentsAreRendered() {
        findElement(By.id("set-component-renderers")).click();

        findElements(By.tagName("lit-renderer-test-component"))
                .forEach((component) -> {
                    WebElement item = findElement(By.className("main"));
                    Assert.assertEquals("Default renderer: 0", item.getText());

                    WebElement details = findElement(By.className("details"));
                    Assert.assertEquals("Details renderer: 0",
                            details.getText());
                });
    }
}
