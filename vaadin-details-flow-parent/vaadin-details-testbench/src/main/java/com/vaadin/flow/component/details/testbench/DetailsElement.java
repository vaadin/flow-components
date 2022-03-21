package com.vaadin.flow.component.details.testbench;

/*
 * #%L
 * Vaadin Details Testbench API
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-details")
public class DetailsElement extends TestBenchElement {

    /**
     * Returns summary element
     */
    public TestBenchElement getSummary() {
        return $(TestBenchElement.class).attribute("slot", "summary").first();
    }

    /**
     * Returns summary element as string
     */
    public String getSummaryText() {
        return getSummary().getText();
    }

    /**
     * Returns content element
     */
    public TestBenchElement getContent() {
        TestBenchElement contentPlaceholder = $(TestBenchElement.class)
                .attribute("part", "content").first();

        return (TestBenchElement) executeScript(
                "return arguments[0].firstElementChild.assignedNodes()[0];",
                contentPlaceholder);
    }

    /**
     * Whether the details are opened or not
     */
    public boolean isOpened() {
        return getPropertyBoolean("opened");
    }

    /**
     * Whether the component is enabled or not
     */
    public boolean isEnabled() {
        return !getPropertyBoolean("disabled");
    }

    /**
     * Returns a wrapper of the summary component
     */
    public TestBenchElement getSummaryWrapper() {
        return $(TestBenchElement.class).attribute("part", "summary").first();
    }

    /**
     * Expands or collapses the details
     */
    public void toggle() {
        getSummaryWrapper().click();
    }
}
