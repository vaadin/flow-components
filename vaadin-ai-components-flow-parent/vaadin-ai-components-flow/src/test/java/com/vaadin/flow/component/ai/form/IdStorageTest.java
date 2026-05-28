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
package com.vaadin.flow.component.ai.form;

import static com.vaadin.flow.component.ai.form.FormTestSupport.idOf;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

/**
 * Storage tests for {@link FormAIController#requireOwner(HasValue)} (§16.2):
 * Component-backed HasValues map to their owning Component for id storage; ids
 * survive controller re-creation against the same Component instance.
 */
class IdStorageTest {

    @Test
    void componentImplementingHasValueGetsIdStoredOnItself() {
        // A Component that implements HasValue owns itself — registering it
        // stamps the id on the field Component directly. Observable via
        // ComponentUtil.getData on the field, which is the same place the
        // production code reads it from.
        var field = new TestField();
        var controller = new FormAIController(new Div(field));
        controller.describe(field, "X");

        Assertions.assertNotNull(idOf(field),
                "A Component that implements HasValue must receive an id "
                        + "stored on itself");
    }

    @Test
    void hasValueWrapperStoresIdOnWrappingComponent() {
        // Synthetic HasValueAndElement wrapper whose getElement() returns
        // the wrapping Component's element — mirrors Grid.asMultiSelect().
        // The wrapper is not a Component (so it cannot carry data); the id
        // must land on the wrapping Component.
        var component = new WrapperHost();
        var controller = new FormAIController(new Div(component));
        controller.describe(component.asMultiValue(), "X");

        Assertions.assertNotNull(
                ComponentUtil.getData(component, FormAIController.FIELD_ID_KEY),
                "A wrapper that delegates getElement() to a Component must "
                        + "store its id on that wrapping Component");
    }

    @Test
    void hasValueWithoutComponentBackingRejectsAtRegistration() {
        var detached = new DetachedHasValue();
        var controller = new FormAIController(new Div());

        var ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> controller.describe(detached, "X"));
        Assertions.assertTrue(ex.getMessage().contains("Component"),
                "Rejection message must point at the Component-backing "
                        + "requirement; got: " + ex.getMessage());
    }

    @Test
    void idIsStableAcrossControllerInstancesForSameComponent() {
        // Component attribute (FIELD_ID_KEY) lives on the Component instance,
        // so creating a fresh controller around the same form gives the
        // same id — pinning the §13.5 reconnect-lifecycle guarantee.
        var field = new TestField();
        var form = new Div(field);
        var firstController = new FormAIController(form);
        firstController.describe(field, "X");
        var firstId = idOf(field);

        var secondController = new FormAIController(form);
        secondController.describe(field, "Y");
        var secondId = idOf(field);

        Assertions.assertEquals(firstId, secondId,
                "Id stored on the Component must survive controller "
                        + "recreation against the same Component instance");
    }

    @Test
    void idIsStableForMultipleWrapperInstancesOnSameComponent() {
        // grid.asMultiSelect() returns a fresh wrapper each call. Both
        // wrappers point at the same Element + Component, so registering
        // either should produce the same field id.
        var component = new WrapperHost();
        var controller = new FormAIController(new Div(component));

        controller.describe(component.asMultiValue(), "first");
        var firstId = (String) ComponentUtil.getData(component,
                FormAIController.FIELD_ID_KEY);

        controller.describe(component.asMultiValue(), "second");
        var secondId = (String) ComponentUtil.getData(component,
                FormAIController.FIELD_ID_KEY);

        Assertions.assertEquals(firstId, secondId,
                "Multiple wrapper instances of the same Component must share "
                        + "the same field id");
    }

    @Test
    void distinctComponentsProduceDistinctIds() {
        var a = new TestField();
        var b = new TestField();
        var controller = new FormAIController(new Div(a, b));
        controller.describe(a, "A");
        controller.describe(b, "B");

        Assertions.assertNotEquals(idOf(a), idOf(b),
                "Two distinct Components must get distinct field ids");
    }

    /** Component that hosts an Element-backed HasValue (Grid-like). */
    @Tag("wrapper-host")
    private static class WrapperHost extends Component {
        MultiSelect<WrapperHost, String> asMultiValue() {
            // Each call returns a fresh wrapper instance whose getElement()
            // points at the host's element — same Element identity across
            // calls, different wrapper Java identity. Mirrors the Grid
            // selection-model wrapper pattern verified in §10.4 / §14.1.1.
            return new MultiSelect<>() {
                private Set<String> value = Set.of();

                @Override
                public void updateSelection(Set<String> added,
                        Set<String> removed) {
                    var next = new java.util.HashSet<>(value);
                    next.addAll(added);
                    next.removeAll(removed);
                    value = Set.copyOf(next);
                }

                @Override
                public Set<String> getSelectedItems() {
                    return value;
                }

                @Override
                public Registration addSelectionListener(
                        MultiSelectionListener<WrapperHost, String> listener) {
                    return () -> {
                    };
                }

                @Override
                public Element getElement() {
                    return WrapperHost.this.getElement();
                }

                @Override
                public Registration addValueChangeListener(
                        ValueChangeListener<? super ComponentValueChangeEvent<WrapperHost, Set<String>>> listener) {
                    return () -> {
                    };
                }
            };
        }
    }

    /** HasValue without HasElement and not a Component — rejection case. */
    private static class DetachedHasValue
            implements HasValue<HasValue.ValueChangeEvent<String>, String> {
        private String value;

        @Override
        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Registration addValueChangeListener(
                ValueChangeListener<? super ValueChangeEvent<String>> listener) {
            return () -> {
            };
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            // no-op
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public void setRequiredIndicatorVisible(boolean visible) {
            // no-op
        }

        @Override
        public boolean isRequiredIndicatorVisible() {
            return false;
        }
    }
}
