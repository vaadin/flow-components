/*
 * Copyright 2000-2021 Vaadin Ltd.
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
 *
 */

package com.vaadin.tests.elements;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.commands.TestBenchCommandExecutor;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.List;

/**
 * Helper class for interacting with the shadow DOM. TestBench only provides
 * this feature on the test class, but not on TestBenchElement itself. This
 * class can be used when interacting with the shadow DOM from custom TestBench
 * elements.
 */
public class ShadowDomHelper {

    private final TestBenchCommandExecutor commandExecutor;

    public ShadowDomHelper(TestBenchCommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    /**
     * Searches for a single element within an elements shadow DOM
     * 
     * @param webComponent
     *            The element within which's shadow DOM to search
     * @param by
     *            The locator to search for
     * @return
     */
    public TestBenchElement findElementInShadowRoot(WebElement webComponent,
            By by) {
        return this.getShadowRoot(webComponent).findElement(by);
    }

    /**
     * Searches for multiple element within an elements shadow DOM
     * 
     * @param webComponent
     *            The element within which's shadow DOM to search
     * @param by
     *            The locator to search for
     * @return
     */
    public List<WebElement> findElementsInShadowRoot(WebElement webComponent,
            By by) {
        return this.getShadowRoot(webComponent).findElements(by);
    }

    /**
     * Gets the shadow DOM of the specified element. Asserts that the element
     * has a shadow DOM.
     * 
     * @param webComponent
     *            The element which contains the shadow DOM
     * @return
     */
    public TestBenchElement getShadowRoot(WebElement webComponent) {
        ShadowDomElementWrapper wrapper = new ShadowDomElementWrapper(
                webComponent, this.commandExecutor);

        wrapper.waitUntil((driver) -> wrapper.getCommandExecutor()
                .executeScript("return arguments[0].shadowRoot",
                        new Object[] { wrapper }) != null);
        TestBenchElement shadowRoot = (TestBenchElement) wrapper
                .getCommandExecutor()
                .executeScript("return arguments[0].shadowRoot",
                        new Object[] { wrapper });
        Assert.assertNotNull("Could not locate shadowRoot in the element",
                wrapper);
        return shadowRoot;
    }

    /**
     * TestBenchElement wrapper that exposes waitUntil
     */
    private static class ShadowDomElementWrapper extends TestBenchElement {
        public ShadowDomElementWrapper(WebElement webElement,
                TestBenchCommandExecutor commandExecutor) {
            super(webElement, commandExecutor);
        }

        protected <T> T waitUntil(ExpectedCondition<T> condition) {
            return super.waitUntil(condition);
        }
    }
}
