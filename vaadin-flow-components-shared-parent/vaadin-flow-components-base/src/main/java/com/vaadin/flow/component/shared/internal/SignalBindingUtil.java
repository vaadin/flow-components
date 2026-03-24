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

import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.BindingContext;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.SignalBinding;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.StateNode;
import com.vaadin.flow.internal.nodefeature.SignalBindingFeature;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.Signal;

/**
 * Internal utility for working with signal bindings in components.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
public final class SignalBindingUtil {

    private SignalBindingUtil() {
    }

    /**
     * Maps a signal binding from one type to another. Creates a computed signal
     * by applying the mapper to the source signal, delegates to the provided
     * binder function, and returns a {@link SignalBinding} typed to the
     * original source type.
     * <p>
     * This is useful when a component API works with typed values (e.g. enums)
     * but the underlying binding mechanism works with a different type (e.g.
     * strings).
     *
     * @param source
     *            the source signal with the original type, not {@code null}
     * @param mapper
     *            function to convert source values to the type expected by the
     *            binder, not {@code null}
     * @param binder
     *            function that creates a binding from a mapped signal, not
     *            {@code null}
     * @param <T>
     *            the source signal value type
     * @param <U>
     *            the mapped signal value type used by the underlying binding
     * @return a {@link SignalBinding} typed to the source type
     */
    public static <T, U> SignalBinding<T> mapBinding(Signal<T> source,
            SerializableFunction<T, U> mapper,
            SerializableFunction<Signal<U>, SignalBinding<U>> binder) {
        Objects.requireNonNull(source, "Source signal cannot be null");
        Objects.requireNonNull(mapper, "Mapper function cannot be null");
        Objects.requireNonNull(binder, "Binder function cannot be null");

        @SuppressWarnings("unchecked")
        T[] previous = (T[]) new Object[] { source.peek() };

        Signal<U> mapped = Signal.computed(() -> mapper.apply(source.get()));
        SignalBinding<U> inner = binder.apply(mapped);

        SignalBinding<T> outer = new SignalBinding<>();
        inner.onChange(ctx -> {
            if (outer.hasCallbacks()) {
                T current = source.peek();
                outer.fireOnChange(new BindingContext<>(ctx.isInitialRun(),
                        ctx.isBackgroundChange(), previous[0], current,
                        ctx.getElement()));
                previous[0] = current;
            }
        });
        return outer;
    }

    /**
     * Creates a signal effect with {@link Signal#effect} and returns a
     * {@link SignalBinding} that is notified with the updated signal value when
     * the effect runs.
     * <p>
     * This is useful for implementing bind APIs in components which only run
     * side-effects (update a class field, run Javascript), but can not use an
     * existing Flow API (such as
     * {@link Element#bindProperty(String, Signal, SerializableConsumer)}) to
     * create a binding.
     * <p>
     * The binding is registered on the component using the specified binding
     * type. If a binding of the same type is already active, a
     * {@link BindingActiveException} is thrown. Components can use
     * {@link #throwIfBindingActive} to manually check for active bindings in
     * their API methods to prevent changes that would interfere with the
     * binding.
     *
     * @param owner
     *            the component that owns the effect, not {@code null}
     * @param bindingType
     *            a unique identifier for the binding type, used to prevent
     *            duplicate bindings on the same component, not {@code null}
     * @param signal
     *            the signal to observe, not {@code null}
     * @param effect
     *            the consumer to invoke with the signal's current value
     *            whenever it changes, not {@code null}
     * @param <T>
     *            the signal value type
     * @return a {@link SignalBinding} that can be used to register change
     *         callbacks
     * @throws BindingActiveException
     *             if a binding of the same type is already active on the
     *             component
     */
    public static <T> SignalBinding<T> effectBinding(Component owner,
            String bindingType, Signal<T> signal,
            SerializableConsumer<T> effect) {
        Objects.requireNonNull(owner, "Owner cannot be null");
        Objects.requireNonNull(bindingType, "Binding type cannot be null");
        Objects.requireNonNull(signal, "Signal cannot be null");
        Objects.requireNonNull(effect, "Effect cannot be null");

        StateNode node = owner.getElement().getNode();
        SignalBindingFeature feature = node
                .getFeature(SignalBindingFeature.class);
        if (feature.hasBinding(bindingType)) {
            throw new BindingActiveException();
        }

        T[] previous = (T[]) new Object[] { signal.peek() };
        SignalBinding<T> binding = new SignalBinding<>();

        Signal.effect(owner, ctx -> {
            T value = signal.get();
            effect.accept(value);
            if (binding.hasCallbacks()) {
                binding.fireOnChange(new BindingContext<>(ctx.isInitialRun(),
                        ctx.isBackgroundChange(), previous[0], value,
                        owner.getElement()));
            }
            previous[0] = value;
        });

        feature.setBinding(bindingType, signal, null);

        return binding;
    }

    /**
     * Throws a {@link BindingActiveException} if a binding of the specified
     * type is active on the component. This can be used in component APIs to
     * prevent changes that would interfere with an active binding.
     * 
     * @param component
     *            the component to check for active bindings, not {@code null}
     * @param bindingType
     *            a unique identifier for the binding type to check, not
     *            {@code null}
     * @throws BindingActiveException
     *             if a binding of the specified type is active on the component
     */
    public static void throwIfBindingActive(Component component,
            String bindingType) {
        Objects.requireNonNull(component, "Component cannot be null");
        Objects.requireNonNull(bindingType, "Binding type cannot be null");

        StateNode node = component.getElement().getNode();
        SignalBindingFeature feature = node
                .getFeature(SignalBindingFeature.class);
        if (feature.hasBinding(bindingType)) {
            throw new BindingActiveException();
        }
    }
}
