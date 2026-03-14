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

import com.vaadin.flow.dom.BindingContext;
import com.vaadin.flow.dom.SignalBinding;
import com.vaadin.flow.function.SerializableFunction;
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
}
