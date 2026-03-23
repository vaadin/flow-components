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
package com.vaadin.flow.component.customfield;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.JacksonUtils;

class ManualValueUpdateTest {

    private IncrementingCustomField automaticField;
    private IncrementingCustomField manualField;

    @BeforeEach
    void setup() {
        automaticField = new IncrementingCustomField(false);
        manualField = new IncrementingCustomField(true);
    }

    @Test
    void automaticValueUpdate_changeEventUpdatesValue() {
        Assertions.assertEquals(Integer.valueOf(0), automaticField.getValue());

        fireChangeEvent(automaticField);
        Assertions.assertEquals(Integer.valueOf(1), automaticField.getValue());

        fireChangeEvent(automaticField);
        Assertions.assertEquals(Integer.valueOf(2), automaticField.getValue());
    }

    @Test
    void manualValueUpdate_changeEventDoesNotUpdateValue() {
        Assertions.assertEquals(Integer.valueOf(0), manualField.getValue());

        fireChangeEvent(manualField);
        Assertions.assertEquals(Integer.valueOf(0), manualField.getValue());

        fireChangeEvent(manualField);
        Assertions.assertEquals(Integer.valueOf(0), manualField.getValue());
    }

    @Test
    void manualValueUpdate_manuallyUpdateValue() {
        Assertions.assertEquals(Integer.valueOf(0), manualField.getValue());

        manualField.updateValue();
        Assertions.assertEquals(Integer.valueOf(1), manualField.getValue());

        manualField.updateValue();
        Assertions.assertEquals(Integer.valueOf(2), manualField.getValue());
    }

    private void fireChangeEvent(CustomField<?> field) {
        DomEvent changeEvent = new DomEvent(field.getElement(), "change",
                JacksonUtils.createObjectNode());
        field.getElement().getNode().getFeature(
                com.vaadin.flow.internal.nodefeature.ElementListenerMap.class)
                .fireEvent(changeEvent);
    }

    private static class IncrementingCustomField extends CustomField<Integer> {
        private int counter = 0;

        public IncrementingCustomField(boolean manualValueUpdate) {
            super(0, manualValueUpdate);
        }

        @Override
        protected Integer generateModelValue() {
            return ++counter;
        }

        @Override
        protected void setPresentationValue(Integer newPresentationValue) {
            // Not needed for this test
        }
    }
}
