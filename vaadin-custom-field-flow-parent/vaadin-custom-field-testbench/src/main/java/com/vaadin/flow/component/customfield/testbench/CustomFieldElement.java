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
package com.vaadin.flow.component.customfield.testbench;

import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasLabel;
import com.vaadin.testbench.HasValidation;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-custom-field")
public class CustomFieldElement extends TestBenchElement
        implements HasHelper, HasLabel, HasValidation {
    /**
     * {@inheritDoc}
     */
    // TODO: Remove once https://github.com/vaadin/testbench/issues/1299 is
    // fixed
    @Override
    public TestBenchElement getHelperComponent() {
        final ElementQuery<TestBenchElement> query = $(TestBenchElement.class)
                .withAttribute("slot", "helper");
        if (query.exists()) {
            TestBenchElement last = query.last();
            // To avoid getting the "slot" element, for components with slotted
            // slots
            if (!"slot".equals(last.getTagName())
                    && this.equals(last.getPropertyElement("parentElement"))) {
                return last;
            }
        }
        return null;
    }
}
