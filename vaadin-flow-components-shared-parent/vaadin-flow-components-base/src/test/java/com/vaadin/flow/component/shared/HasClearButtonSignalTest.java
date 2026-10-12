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
package com.vaadin.flow.component.shared;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class HasClearButtonSignalTest extends AbstractSignalsTest {

    @Tag("test")
    private static class TestComponent extends Component
            implements HasClearButton {
    }

    @TestFactory
    Stream<DynamicTest> bindClearButtonVisible() {
        return generateBindingTests(TestComponent::new,
                TestComponent::bindClearButtonVisible,
                TestComponent::isClearButtonVisible,
                TestComponent::setClearButtonVisible, true, false);
    }

    @Test
    void bindClearButtonVisible_nullValue_mapsToFalse() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindClearButtonVisible(signal);

        signal.set(null);
        assertFalse(component.isClearButtonVisible());
        assertFalse(component.getElement().getProperty("clearButtonVisible",
                false));
    }
}
