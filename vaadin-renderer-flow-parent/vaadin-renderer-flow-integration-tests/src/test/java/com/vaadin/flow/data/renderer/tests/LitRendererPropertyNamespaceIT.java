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

import java.util.List;

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
    public void attach_setLitRenderers_componentsAreRendered() {
        findElement(By.id("attach")).click();
        findElement(By.id("set-lit-renderers")).click();
        assertComponentsAreRendered();
    }

    @Test
    public void attach_setComponentRenderers_componentsAreRendered() {
        findElement(By.id("attach")).click();
        findElement(By.id("set-component-renderers")).click();
        assertComponentsAreRendered();
    }

    @Test
    public void setLitRenderers_attach_componentsAreRendered() {
        findElement(By.id("set-lit-renderers")).click();
        findElement(By.id("attach")).click();
        assertComponentsAreRendered();
    }

    @Test
    public void setComponentRenderers_attach_componentsAreRendered() {
        findElement(By.id("set-component-renderers")).click();
        findElement(By.id("attach")).click();
        assertComponentsAreRendered();
    }

    private void assertComponentsAreRendered() {
        List<WebElement> components = findElements(
                By.tagName("lit-renderer-test-component"));
        for (int i = 0; i < components.size(); i++) {
            WebElement component = components.get(i);

            WebElement main = component.findElement(By.className("main"));
            Assert.assertEquals(
                    "Component " + i + " : Default Renderer : Item 0",
                    main.getText());

            WebElement details = component.findElement(By.className("details"));
            Assert.assertEquals(
                    "Component " + i + " : Details Renderer : Item 0",
                    details.getText());
        }
    }
}
