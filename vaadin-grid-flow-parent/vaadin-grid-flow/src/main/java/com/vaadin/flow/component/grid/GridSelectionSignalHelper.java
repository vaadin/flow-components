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

import java.util.Objects;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.dom.ElementEffect;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.nodefeature.SignalBindingFeature;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.Signal;

/**
 * Shared helper for binding signals to grid selection model values.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
class GridSelectionSignalHelper {

    /**
     * Binds a signal to a {@link HasValueAndElement} target, keeping the value
     * synchronized while the element is attached.
     *
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
     */
    static <E extends HasValue.ValueChangeEvent<V>, V> void bindValue(
            HasValueAndElement<E, V> target, Signal<V> valueSignal,
            SerializableConsumer<V> writeCallback) {
        Objects.requireNonNull(valueSignal, "Signal cannot be null");
        SignalBindingFeature feature = target.getElement().getNode()
                .getFeature(SignalBindingFeature.class);

        if (feature.hasBinding(SignalBindingFeature.VALUE)) {
            throw new BindingActiveException();
        }

        boolean[] fromSignal = { false };

        Registration effectReg = ElementEffect.bind(target.getElement(),
                valueSignal, (element, value) -> {
                    try {
                        fromSignal[0] = true;
                        target.setValue(value);
                    } finally {
                        fromSignal[0] = false;
                    }
                });

        Registration listenerReg = target.addValueChangeListener(event -> {
            if (!fromSignal[0]) {
                if (writeCallback != null) {
                    writeCallback.accept(target.getValue());
                }
            }
        });

        Registration combined = () -> {
            effectReg.remove();
            listenerReg.remove();
        };
        feature.setBinding(SignalBindingFeature.VALUE, combined, valueSignal,
                writeCallback);
    }
}
