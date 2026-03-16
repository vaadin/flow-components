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
package com.vaadin.flow.component.shared.internal;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.SignalBinding;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsJUnit6Test;

class SignalBindingUtilTest extends AbstractSignalsJUnit6Test {

    private enum Color {
        RED("red"), GREEN("green"), BLUE("blue");

        private final String value;

        Color(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    @Tag("test")
    private static class TestComponent extends Component {

        SignalBinding<Color> bindColor(Signal<Color> colorSignal) {
            return SignalBindingUtil.mapBinding(colorSignal, Color::getValue,
                    mapped -> getElement().bindProperty("color", mapped, null));
        }
    }

    @Test
    void mapBinding_mapsSourceValueToTargetType() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<Color> source = new ValueSignal<>(Color.RED);
        component.bindColor(source);

        Assertions.assertEquals("red",
                component.getElement().getProperty("color"));
    }

    @Test
    void mapBinding_signalChanges_propertyUpdated() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<Color> source = new ValueSignal<>(Color.RED);
        component.bindColor(source);

        source.set(Color.GREEN);

        Assertions.assertEquals("green",
                component.getElement().getProperty("color"));
    }

    @Test
    void mapBinding_onChange_receivesCorrectTypedValues() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<Color> source = new ValueSignal<>(Color.RED);

        List<Color> oldValues = new ArrayList<>();
        List<Color> newValues = new ArrayList<>();

        component.bindColor(source).onChange(ctx -> {
            oldValues.add(ctx.getOldValue());
            newValues.add(ctx.getNewValue());
        });

        source.set(Color.GREEN);

        Assertions.assertEquals(List.of(Color.RED), oldValues);
        Assertions.assertEquals(List.of(Color.GREEN), newValues);
    }

    @Test
    void mapBinding_onChange_receivesElement() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<Color> source = new ValueSignal<>(Color.RED);

        List<com.vaadin.flow.dom.Element> elements = new ArrayList<>();

        component.bindColor(source)
                .onChange(ctx -> elements.add(ctx.getElement()));

        source.set(Color.GREEN);

        Assertions.assertEquals(1, elements.size());
        Assertions.assertSame(component.getElement(), elements.get(0));
    }

    @Test
    void mapBinding_onChange_multipleCallbacksFiredInOrder() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<Color> source = new ValueSignal<>(Color.RED);

        List<String> callOrder = new ArrayList<>();

        SignalBinding<Color> binding = component.bindColor(source);
        binding.onChange(ctx -> callOrder.add("first"));
        binding.onChange(ctx -> callOrder.add("second"));

        source.set(Color.GREEN);

        Assertions.assertEquals(List.of("first", "second"), callOrder);
    }

    @Test
    void mapBinding_onChange_returnsBindingForFluentChaining() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<Color> source = new ValueSignal<>(Color.RED);

        SignalBinding<Color> binding = component.bindColor(source);
        SignalBinding<Color> returned = binding.onChange(ctx -> {
        });

        Assertions.assertSame(binding, returned);
    }

    @Test
    void mapBinding_onChange_tracksOldAndNewAcrossMultipleChanges() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<Color> source = new ValueSignal<>(Color.RED);

        List<Color> oldValues = new ArrayList<>();
        List<Color> newValues = new ArrayList<>();

        component.bindColor(source).onChange(ctx -> {
            oldValues.add(ctx.getOldValue());
            newValues.add(ctx.getNewValue());
        });

        source.set(Color.GREEN);
        source.set(Color.BLUE);

        Assertions.assertEquals(List.of(Color.RED, Color.GREEN), oldValues);
        Assertions.assertEquals(List.of(Color.GREEN, Color.BLUE), newValues);
    }

    @Test
    void mapBinding_hasCallbacks_falseBeforeOnChange() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<Color> source = new ValueSignal<>(Color.RED);

        SignalBinding<Color> binding = component.bindColor(source);

        Assertions.assertFalse(binding.hasCallbacks());
    }

    @Test
    void mapBinding_hasCallbacks_trueAfterOnChange() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<Color> source = new ValueSignal<>(Color.RED);

        SignalBinding<Color> binding = component.bindColor(source);
        binding.onChange(ctx -> {
        });

        Assertions.assertTrue(binding.hasCallbacks());
    }

    @Test
    void mapBinding_nullSource_throwsNullPointerException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> component.bindColor(null));
        Assertions.assertEquals("Source signal cannot be null",
                exception.getMessage());
    }

    @Test
    void mapBinding_nullMapper_throwsNullPointerException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<Color> source = new ValueSignal<>(Color.RED);

        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> SignalBindingUtil.mapBinding(source, null,
                        mapped -> component.getElement().bindProperty("color",
                                mapped, null)));
        Assertions.assertEquals("Mapper function cannot be null",
                exception.getMessage());
    }

    @Test
    void mapBinding_nullBinder_throwsNullPointerException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<Color> source = new ValueSignal<>(Color.RED);

        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> SignalBindingUtil
                        .mapBinding(source, Color::getValue, null));
        Assertions.assertEquals("Binder function cannot be null",
                exception.getMessage());
    }
}
