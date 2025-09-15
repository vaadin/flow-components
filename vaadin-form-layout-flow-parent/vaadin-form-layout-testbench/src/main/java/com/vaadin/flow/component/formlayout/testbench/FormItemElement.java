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
package com.vaadin.flow.component.formlayout.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a {@code <vaadin-form-item>} element.
 */
@Element("vaadin-form-item")
public class FormItemElement extends TestBenchElement {

    /**
     * Gets the label element from the label slot.
     *
     * @return the label element as a TestBenchElement or {@code null} if the
     *         form item contains no label.
     */
    public TestBenchElement getLabel() {
        return (TestBenchElement) executeScript("""
                return arguments[0].querySelector(':scope > [slot="label"]');
                """, this);
    }

    /**
     * Gets the text of the label element.
     *
     * @return the text of the label element or {@code null} if the form item
     *         contains no label.
     */
    public String getLabelText() {
        var label = getLabel();
        return label != null ? label.getText() : null;
    }

    /**
     * Gets the field element.
     *
     * @return the field element as a TestBenchElement or {@code null} if the
     *         form item contains no field.
     */
    public TestBenchElement getField() {
        return (TestBenchElement) executeScript(
                """
                        return [...arguments[0].children].find((child) => child.validate || child.checkValidity);
                        """,
                this);
    }
}
