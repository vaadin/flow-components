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
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class SignalBindingUtilTest extends AbstractSignalsTest {

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

    @Test
    void effectBinding_runsEffectWithInitialValue() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");
        List<String> values = new ArrayList<>();

        SignalBindingUtil.effectBinding(component, "test", signal, values::add);

        Assertions.assertEquals(List.of("initial"), values);
    }

    @Test
    void effectBinding_runsEffectOnSignalChange() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");
        List<String> values = new ArrayList<>();

        SignalBindingUtil.effectBinding(component, "test", signal, values::add);

        signal.set("updated");

        Assertions.assertEquals(List.of("initial", "updated"), values);
    }

    @Test
    void effectBinding_onChange_receivesCorrectTypedValues() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");

        List<String> oldValues = new ArrayList<>();
        List<String> newValues = new ArrayList<>();

        SignalBinding<String> binding = SignalBindingUtil
                .effectBinding(component, "test", signal, v -> {
                });
        binding.onChange(ctx -> {
            oldValues.add(ctx.getOldValue());
            newValues.add(ctx.getNewValue());
        });

        signal.set("updated");

        Assertions.assertEquals(List.of("initial"), oldValues);
        Assertions.assertEquals(List.of("updated"), newValues);
    }

    @Test
    void effectBinding_onChange_receivesElement() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");

        List<com.vaadin.flow.dom.Element> elements = new ArrayList<>();

        SignalBinding<String> binding = SignalBindingUtil
                .effectBinding(component, "test", signal, v -> {
                });
        binding.onChange(ctx -> elements.add(ctx.getElement()));

        signal.set("updated");

        Assertions.assertEquals(1, elements.size());
        Assertions.assertSame(component.getElement(), elements.get(0));
    }

    @Test
    void effectBinding_onChange_multipleCallbacksFiredInOrder() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");

        List<String> callOrder = new ArrayList<>();

        SignalBinding<String> binding = SignalBindingUtil
                .effectBinding(component, "test", signal, v -> {
                });
        binding.onChange(ctx -> callOrder.add("first"));
        binding.onChange(ctx -> callOrder.add("second"));

        signal.set("updated");

        Assertions.assertEquals(List.of("first", "second"), callOrder);
    }

    @Test
    void effectBinding_onChange_returnsBindingForFluentChaining() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");

        SignalBinding<String> binding = SignalBindingUtil
                .effectBinding(component, "test", signal, v -> {
                });
        SignalBinding<String> returned = binding.onChange(ctx -> {
        });

        Assertions.assertSame(binding, returned);
    }

    @Test
    void effectBinding_onChange_tracksOldAndNewAcrossMultipleChanges() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("a");

        List<String> oldValues = new ArrayList<>();
        List<String> newValues = new ArrayList<>();

        SignalBinding<String> binding = SignalBindingUtil
                .effectBinding(component, "test", signal, v -> {
                });
        binding.onChange(ctx -> {
            oldValues.add(ctx.getOldValue());
            newValues.add(ctx.getNewValue());
        });

        signal.set("b");
        signal.set("c");

        Assertions.assertEquals(List.of("a", "b"), oldValues);
        Assertions.assertEquals(List.of("b", "c"), newValues);
    }

    @Test
    void effectBinding_hasCallbacks_falseBeforeOnChange() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");

        SignalBinding<String> binding = SignalBindingUtil
                .effectBinding(component, "test", signal, v -> {
                });

        Assertions.assertFalse(binding.hasCallbacks());
    }

    @Test
    void effectBinding_hasCallbacks_trueAfterOnChange() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");

        SignalBinding<String> binding = SignalBindingUtil
                .effectBinding(component, "test", signal, v -> {
                });
        binding.onChange(ctx -> {
        });

        Assertions.assertTrue(binding.hasCallbacks());
    }

    @Test
    void effectBinding_calledTwiceWithSameType_throwsBindingActiveException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");

        SignalBindingUtil.effectBinding(component, "test", signal, v -> {
        });

        Assertions.assertThrows(BindingActiveException.class,
                () -> SignalBindingUtil.effectBinding(component, "test", signal,
                        v -> {
                        }));
    }

    @Test
    void effectBinding_differentBindingTypes_doesNotThrow() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");

        SignalBindingUtil.effectBinding(component, "type1", signal, v -> {
        });

        Assertions.assertDoesNotThrow(() -> SignalBindingUtil
                .effectBinding(component, "type2", signal, v -> {
                }));
    }

    @Test
    void effectBinding_nullOwner_throwsNullPointerException() {
        ValueSignal<String> signal = new ValueSignal<>("initial");

        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> SignalBindingUtil
                        .effectBinding(null, "test", signal, v -> {
                        }));
        Assertions.assertEquals("Owner cannot be null", exception.getMessage());
    }

    @Test
    void effectBinding_nullBindingType_throwsNullPointerException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");

        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> SignalBindingUtil
                        .effectBinding(component, null, signal, v -> {
                        }));
        Assertions.assertEquals("Binding type cannot be null",
                exception.getMessage());
    }

    @Test
    void effectBinding_nullSignal_throwsNullPointerException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> SignalBindingUtil
                        .effectBinding(component, "test", null, v -> {
                        }));
        Assertions.assertEquals("Signal cannot be null",
                exception.getMessage());
    }

    @Test
    void effectBinding_nullEffect_throwsNullPointerException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");

        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> SignalBindingUtil
                        .effectBinding(component, "test", signal, null));
        Assertions.assertEquals("Effect cannot be null",
                exception.getMessage());
    }

    @Test
    void throwIfBindingActive_noBinding_doesNotThrow() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        Assertions.assertDoesNotThrow(() -> SignalBindingUtil
                .throwIfBindingActive(component, "test"));
    }

    @Test
    void throwIfBindingActive_bindingActive_throwsBindingActiveException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");
        SignalBindingUtil.effectBinding(component, "test", signal, v -> {
        });

        Assertions.assertThrows(BindingActiveException.class,
                () -> SignalBindingUtil.throwIfBindingActive(component,
                        "test"));
    }

    @Test
    void throwIfBindingActive_differentBindingType_doesNotThrow() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");
        SignalBindingUtil.effectBinding(component, "type1", signal, v -> {
        });

        Assertions.assertDoesNotThrow(() -> SignalBindingUtil
                .throwIfBindingActive(component, "type2"));
    }

    @Test
    void throwIfBindingActive_nullComponent_throwsNullPointerException() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> SignalBindingUtil.throwIfBindingActive(null, "test"));
        Assertions.assertEquals("Component cannot be null",
                exception.getMessage());
    }

    @Test
    void throwIfBindingActive_nullBindingType_throwsNullPointerException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);

        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> SignalBindingUtil.throwIfBindingActive(component, null));
        Assertions.assertEquals("Binding type cannot be null",
                exception.getMessage());
    }
}
