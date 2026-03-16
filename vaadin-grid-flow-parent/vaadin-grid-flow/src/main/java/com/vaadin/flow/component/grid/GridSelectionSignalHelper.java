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
package com.vaadin.flow.component.grid;

import java.io.Serializable;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.dom.BindingContext;
import com.vaadin.flow.dom.SignalBinding;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.signals.Signal;

/**
 * Shared helper for binding signals to grid selection model values.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
class GridSelectionSignalHelper {

    /**
     * Result of binding a signal to a selection model value.
     *
     * @param signalBinding
     *            the signal binding for registering change callbacks
     * @param cleanup
     *            a registration to remove the effect and listener when the
     *            selection model is removed
     * @param <V>
     *            the value type
     */
    record BindResult<V>(SignalBinding<V> signalBinding,
            Registration cleanup) implements Serializable {
    }

    /**
     * Binds a signal to a {@link HasValueAndElement} target, keeping the value
     * synchronized while the owner component is attached.
     *
     * @param owner
     *            the component that owns the effect lifecycle
     * @param target
     *            the HasValueAndElement to bind to
     * @param valueSignal
     *            the signal to bind
     * @param writeCallback
     *            the callback to propagate value changes back, or {@code null}
     *            for a read-only binding
     * @param <E>
     *            the value change event type
     * @param <V>
     *            the value type
     * @return the binding result containing the signal binding and a cleanup
     *         registration
     */
    @SuppressWarnings("unchecked")
    static <E extends HasValue.ValueChangeEvent<V>, V> BindResult<V> bindValue(
            Component owner, HasValueAndElement<E, V> target,
            Signal<V> valueSignal, SerializableConsumer<V> writeCallback) {
        Objects.requireNonNull(valueSignal, "Signal cannot be null");

        SignalBinding<V> binding = new SignalBinding<>();
        boolean[] fromSignal = { false };
        V[] previousValue = (V[]) new Object[] { valueSignal.peek() };

        Registration effectReg = Signal.effect(owner, ctx -> {
            V newValue = valueSignal.get();
            V oldValue = previousValue[0];
            try {
                fromSignal[0] = true;
                target.setValue(newValue);
            } finally {
                fromSignal[0] = false;
            }
            if (ctx.isInitialRun() || binding.hasCallbacks()) {
                var bindingContext = new BindingContext<>(ctx.isInitialRun(),
                        ctx.isBackgroundChange(), oldValue, newValue,
                        target.getElement());
                binding.setInitialContext(bindingContext);
                if (binding.hasCallbacks()) {
                    binding.fireOnChange(bindingContext);
                }
            }
            previousValue[0] = newValue;
        });

        Registration listenerReg = target.addValueChangeListener(event -> {
            if (!fromSignal[0] && writeCallback != null) {
                writeCallback.accept(target.getValue());
            }
        });

        Registration cleanup = () -> {
            effectReg.remove();
            listenerReg.remove();
        };

        return new BindResult<>(binding, cleanup);
    }
}
