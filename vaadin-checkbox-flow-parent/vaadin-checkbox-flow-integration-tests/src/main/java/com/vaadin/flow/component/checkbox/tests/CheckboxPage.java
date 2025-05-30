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
package com.vaadin.flow.component.checkbox.tests;

import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link Checkbox}.
 */
@Route("vaadin-checkbox/checkbox-test")
public class CheckboxPage extends Div {

    public static class Bean {
        private Boolean property;

        public Boolean isProperty() {
            return property;
        }

        public void setProperty(Boolean property) {
            this.property = property;
        }
    }

    /**
     * Creates a new instance.
     */
    public CheckboxPage() {
        add(createInitialValueTest(false, false),
                createInitialValueTest(true, false),
                createInitialValueTest(false, true),
                createInitialValueTest(true, true));
    }

    // https://github.com/vaadin/vaadin-checkbox-flow/issues/22
    private Component createInitialValueTest(boolean checked,
            boolean indeterminate) {
        int id = checked ? 1 : 0;
        id += indeterminate ? 2 : 0;

        Checkbox checkbox2 = new Checkbox("initial value cb");
        checkbox2.setValue(checked);
        checkbox2.setIndeterminate(indeterminate);
        checkbox2.setId("cb-" + id);

        Span valueText = new Span("Value: " + checkbox2.getValue());
        valueText.setId("value-text-" + id);

        Span indeterminateText = new Span(
                "Indeterminate: " + checkbox2.isIndeterminate());
        indeterminateText.setId("indeterminate-text-" + id);

        AtomicInteger checkedCounter = new AtomicInteger();

        checkbox2.addValueChangeListener(event -> {
            valueText.setText("Value: " + checkedCounter.incrementAndGet() + " "
                    + event.getValue());
        });

        AtomicInteger indeterminateCounter = new AtomicInteger();

        checkbox2.getElement().addPropertyChangeListener("indeterminate",
                event -> indeterminateText.setText("Indeterminate: "
                        + indeterminateCounter.incrementAndGet() + " "
                        + event.getValue()));

        return new Div(checkbox2, valueText, indeterminateText);
    }
}
