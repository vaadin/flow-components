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

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-form-layout&gt;</code>
 * element.
 */
@Element("vaadin-form-layout")
public class FormLayoutElement extends TestBenchElement {

    /**
     * Retrieves all form items of type {@link FormItemElement}.
     *
     * @return a list of all {@link FormItemElement} instances.
     */
    public List<FormItemElement> getFormItems() {
        return $(FormItemElement.class).all();
    }

    /**
     * Retrieves a form item element by its label.
     *
     * @param label
     *            the label of the form item to retrieve
     * @return the form item element with the specified label
     * @throws IllegalArgumentException
     *             if no form item is found with the specified label
     */
    public FormItemElement getFormItemByLabel(String label) {
        return getFormItems().stream().filter(item -> {
            var itemLabel = item.getLabel();
            return itemLabel != null && itemLabel.getText().equals(label);
        }).findFirst().orElseThrow(() -> new IllegalArgumentException(
                "No form item found with label: " + label));
    }
}
