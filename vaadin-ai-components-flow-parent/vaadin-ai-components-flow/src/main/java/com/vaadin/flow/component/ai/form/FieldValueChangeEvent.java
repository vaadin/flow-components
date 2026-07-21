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

import java.io.Serializable;
import java.util.Objects;

import com.vaadin.flow.component.HasValue;

/**
 * Fired by {@link FormAIController} once per field whose value changed during a
 * successful AI turn — either written by the LLM or by a cascade. Events fire
 * in document order, after every field's post-turn value has been applied. A
 * field whose post-turn value equals its pre-turn value (by
 * {@link Objects#equals(Object, Object)}) does not produce an event. No events
 * fire when the turn ended in error.
 *
 * @since 25.2
 */
public final class FieldValueChangeEvent implements Serializable {

    private final transient FormAIController source;
    private final HasValue<?, ?> field;
    private final transient Object oldValue;
    private final transient Object newValue;

    FieldValueChangeEvent(FormAIController source, HasValue<?, ?> field,
            Object oldValue, Object newValue) {
        this.source = Objects.requireNonNull(source, "Source must not be null");
        this.field = Objects.requireNonNull(field, "Field must not be null");
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the controller that produced this event. Listeners can use this
     * to call back into the controller (e.g.
     * {@link FormAIController#showFieldHighlight}) without capturing it from
     * the registration site.
     *
     * @return the source controller, never {@code null}
     */
    public FormAIController getSource() {
        return source;
    }

    /**
     * Returns the field whose value changed during this turn.
     *
     * @return the changed field, never {@code null}
     */
    @SuppressWarnings("java:S1452")
    public HasValue<?, ?> getField() {
        return field;
    }

    /**
     * Returns the field's value at the start of the turn, before the LLM ran.
     * For a field that was hidden or absent at turn start and revealed during
     * the turn, this is the field's actual pre-turn value rather than
     * {@code null}.
     *
     * @return the pre-turn value, possibly {@code null}
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Returns the field's value at the end of the turn, after the LLM's writes
     * and any cascading value-change listeners have settled.
     *
     * @return the post-turn value, possibly {@code null}
     */
    public Object getNewValue() {
        return newValue;
    }
}
