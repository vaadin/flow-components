/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
 * @since 25.3
 */
public final class FieldValueChangeEvent implements Serializable {

    private final transient FormAIController source;
    private final HasValue<?, ?> field;
    @SuppressWarnings("java:S1948")
    private final Object oldValue;
    @SuppressWarnings("java:S1948")
    private final Object newValue;

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
     * <p>
     * The source is transient, so it is {@code null} on an event restored from
     * a serialized session.
     *
     * @return the source controller, or {@code null} if this event was
     *         deserialized
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
     * For a field that was hidden at turn start and revealed during the turn,
     * this is the field's actual pre-turn value rather than {@code null}. For a
     * field added to the form during the turn, this is the field's
     * {@link HasValue#getEmptyValue() empty value} — the field had no pre-turn
     * value to report.
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
