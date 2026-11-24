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
package com.vaadin.flow.component.ai.orchestrator;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import com.vaadin.flow.function.SerializableConsumer;

/**
 * The busy flag of an {@link AIOrchestrator}. Works like an
 * {@link AtomicBoolean}, and notifies a listener when the value changes, so
 * that the code that claims or releases the flag does not need to.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
class ProcessingFlag implements Serializable {

    private final AtomicBoolean value = new AtomicBoolean();
    private SerializableConsumer<Boolean> listener;

    /**
     * Gets the value of the flag.
     *
     * @return {@code true} if a prompt is being processed
     */
    boolean get() {
        return value.get();
    }

    /**
     * Sets the flag to the new value if it has the expected value.
     *
     * @param expectedValue
     *            the expected value
     * @param newValue
     *            the new value
     * @return {@code true} if the flag had the expected value
     */
    boolean compareAndSet(boolean expectedValue, boolean newValue) {
        var set = value.compareAndSet(expectedValue, newValue);
        if (set && expectedValue != newValue) {
            notifyListener(newValue);
        }
        return set;
    }

    /**
     * Sets the flag to the given value.
     *
     * @param newValue
     *            the new value
     */
    void set(boolean newValue) {
        if (value.getAndSet(newValue) != newValue) {
            notifyListener(newValue);
        }
    }

    /**
     * Sets the listener that is notified with the new value when the value
     * changes. The listener is notified on the thread that changes the value.
     *
     * @param listener
     *            the listener to notify
     */
    void setListener(SerializableConsumer<Boolean> listener) {
        this.listener = listener;
    }

    private void notifyListener(boolean newValue) {
        if (listener != null) {
            listener.accept(newValue);
        }
    }
}
