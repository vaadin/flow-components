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
package com.vaadin.flow.component.badge.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-badge&gt;</code> element.
 */
@Element("vaadin-badge")
public class BadgeElement extends TestBenchElement {

    /**
     * Gets the text content of the badge.
     *
     * @return the text content
     */
    public String getText() {
        return getPropertyString("textContent");
    }

    /**
     * Gets the number displayed in the badge.
     *
     * @return the number, or {@code null} if not set
     */
    public Integer getNumber() {
        return getPropertyDouble("number") != null
                ? getPropertyDouble("number").intValue()
                : null;
    }

    /**
     * Gets the icon element of the badge.
     *
     * @return the icon element, or {@code null} if not set
     */
    public TestBenchElement getIcon() {
        return $("*").withAttribute("slot", "icon").first();
    }
}
