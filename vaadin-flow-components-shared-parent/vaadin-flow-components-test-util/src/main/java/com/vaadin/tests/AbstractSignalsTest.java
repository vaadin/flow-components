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
package com.vaadin.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * Base class for testing components with full-stack signals. Since signal
 * bindings are only active when components are attached, this class sets up a
 * mock UI instance for attaching components under test.
 */
public class AbstractSignalsTest {
    @RegisterExtension
    protected MockUIExtension ui = new MockUIExtension();

    /**
     * Generates a suite of JUnit 5 dynamic tests that verify the standard
     * behavior of a signal binding on a component property. The returned stream
     * is intended to be used with {@link org.junit.jupiter.api.TestFactory}.
     * <p>
     * The following test cases are generated:
     * <ul>
     * <li><b>synchronizesWhileAttached</b> – verifies that the component
     * property reflects the signal's initial value after binding and updates
     * when the signal value changes, while the component is attached to a
     * UI.</li>
     * <li><b>appliesInitialValueWhileDetached</b> – verifies that the signal's
     * initial value is applied to the component property immediately upon
     * binding, even when the component is not attached to a UI.</li>
     * <li><b>doesNotSynchronizeWhileDetached</b> – verifies that subsequent
     * signal value changes are not propagated to the component property while
     * the component is detached.</li>
     * <li><b>resynchronizesAfterAttach</b> – verifies that the component
     * property catches up with the latest signal value when the component is
     * attached to a UI after the signal was updated while detached.</li>
     * <li><b>manualSetWhileBoundThrows</b> – verifies that imperative updates
     * to a bound property via its setter throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.</li>
     * <li><b>rebindWhileBoundThrows</b> – verifies that calling the bind method
     * again while a binding is already active throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.</li>
     * <li><b>bindNullSignalThrows</b> – verifies that passing a {@code null}
     * signal to the bind method throws a {@link NullPointerException}.</li>
     * </ul>
     * <p>
     * <b>NOTE:</b> The tests are not necessarily exhaustive for all aspects of
     * a specific signal binding, but only cover common expected behaviors.
     * Additional test cases may be needed for edge cases or specific
     * implementation details of a particular binding.
     *
     * @param <C>
     *            the component type
     * @param <T>
     *            the property value type
     * @param componentFactory
     *            supplier that creates a new component instance for each test
     * @param bind
     *            the bind method under test (e.g. {@code DatePicker::bindMin})
     * @param getter
     *            the getter for the bound property (e.g.
     *            {@code DatePicker::getMin})
     * @param setter
     *            the setter for the bound property (e.g.
     *            {@code DatePicker::setMin}), used to verify that imperative
     *            updates are rejected while a binding is active
     * @param signalFactory
     *            supplier that creates a new signal with an initial value for
     *            each test
     * @param updatedValue
     *            a value different from the signal's initial value, used to
     *            test synchronization behavior
     * @return a stream of dynamic tests to be returned from a
     *         {@link org.junit.jupiter.api.TestFactory} method
     */
    protected <C extends Component, T> Stream<DynamicTest> generateBindingTests(
            Supplier<C> componentFactory, BiConsumer<C, ValueSignal<T>> bind,
            Function<C, T> getter, BiConsumer<C, T> setter,
            Supplier<ValueSignal<T>> signalFactory, T updatedValue) {

        var synchronizesWhileAttached = createTest("synchronizesWhileAttached",
                () -> {
                    var component = componentFactory.get();
                    var signal = signalFactory.get();
                    var initialValue = signal.peek();

                    UI.getCurrent().add(component);

                    bind.accept(component, signal);
                    assertEquals(initialValue, getter.apply(component));

                    signal.set(updatedValue);
                    assertEquals(updatedValue, getter.apply(component));
                });

        var appliesInitialValueWhileDetached = createTest(
                "appliesInitialValueWhileDetached", () -> {
                    var component = componentFactory.get();
                    var signal = signalFactory.get();
                    var initialValue = signal.peek();

                    bind.accept(component, signal);

                    assertEquals(initialValue, getter.apply(component));
                });

        var doesNotSynchronizeWhileDetached = createTest(
                "doesNotSynchronizeWhileDetached", () -> {
                    var component = componentFactory.get();
                    var signal = signalFactory.get();
                    T initialValue = signal.peek();

                    bind.accept(component, signal);
                    signal.set(updatedValue);

                    assertEquals(initialValue, getter.apply(component));
                });

        var resynchronizesAfterAttach = createTest("resynchronizesAfterAttach",
                () -> {
                    var component = componentFactory.get();
                    var signal = signalFactory.get();

                    bind.accept(component, signal);
                    signal.set(updatedValue);

                    UI.getCurrent().add(component);

                    assertEquals(updatedValue, getter.apply(component));
                });

        var manualSetWhileBoundThrows = createTest("manualSetWhileBoundThrows",
                () -> {
                    var component = componentFactory.get();
                    var signal = signalFactory.get();

                    bind.accept(component, signal);

                    assertThrows(BindingActiveException.class,
                            () -> setter.accept(component, updatedValue));
                });

        var rebindWhileBoundThrows = createTest("rebindWhileBoundThrows",
                () -> {
                    var component = componentFactory.get();
                    var signal = signalFactory.get();

                    bind.accept(component, signal);

                    assertThrows(BindingActiveException.class,
                            () -> bind.accept(component, signalFactory.get()));
                });

        var bindNullSignalThrows = createTest("bindNullSignalThrows", () -> {
            var component = componentFactory.get();

            assertThrows(NullPointerException.class,
                    () -> bind.accept(component, null));
        });

        return Stream.of(synchronizesWhileAttached,
                appliesInitialValueWhileDetached,
                doesNotSynchronizeWhileDetached, resynchronizesAfterAttach,
                manualSetWhileBoundThrows, rebindWhileBoundThrows,
                bindNullSignalThrows);
    }

    /**
     * Creates a dynamic test that sets up and tears down a mock UI around the
     * test executable, since JUnit 5 dynamic tests do not support lifecycle
     * callbacks.
     */
    private DynamicTest createTest(String name, Executable test) {
        return dynamicTest(name, () -> {
            ui.beforeEach(null);
            try {
                test.execute();
            } finally {
                ui.afterEach(null);
            }
        });
    }
}
