/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.form;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValueContext;

/**
 * Surfaces rejection reasons for the fields the controller has just written
 * with LLM-supplied values. The output drives the {@code fill_form} tool's
 * {@code rejected} block — the LLM only needs the first complaint per field to
 * pick a corrected value on the next turn.
 *
 * <p>
 * Validation runs after every value has been written, so each field is judged
 * against the fully-written form rather than a partial snapshot. Two
 * complementary entry points read the verdict without changing the UI:
 * </p>
 * <ul>
 * <li>{@link #firstError(HasValue, Binding)} reads the per-field message. A
 * bound field defers to its {@link Binding}, which means the binder's own chain
 * (converter, default {@link HasValidator} validator, every
 * {@code withValidator} registration) runs as one unit. An unbound field that
 * implements {@link HasValidator} runs that validator directly; anything else
 * has no reachable validator, so the write is accepted as-is. The read uses
 * {@code fireEvent=false}, so it does not re-fire status events or touch a
 * field's invalid indicator.</li>
 * <li>{@link #beanErrors(Binder)} reads the bean-level cross-field rules
 * registered with {@code binder.withValidator((bean, ctx) -> ...)}. These are
 * not tied to any single field, so the controller surfaces them under a
 * sentinel id. The read runs without modifying the UI, so a field this turn did
 * not write is never marked invalid as a side effect.</li>
 * </ul>
 */
final class FormFieldValidation {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(FormFieldValidation.class);

    private static final String GENERIC_REJECTION_MESSAGE = "Field rejected the value.";

    /**
     * Synthetic rejection id for a bean-level cross-field error. Such an error
     * is not attributable to a single field, so it cannot reuse a field id; the
     * LLM is told to adjust the offending fields rather than write to this id.
     */
    static final String FORM_LEVEL_REJECTION_ID = "__form__";

    private FormFieldValidation() {
    }

    /**
     * Returns the first rejection message for {@code field}, if any. A bound
     * field defers entirely to its {@code binding}; an unbound field that
     * implements {@link HasValidator} runs that validator directly; anything
     * else has no reachable validator and is accepted as-is.
     *
     * @param field
     *            the field whose post-write value should be checked, not
     *            {@code null}
     * @param binding
     *            the binding attached to the field, or {@code null} when the
     *            field is unbound
     * @return the rejection message, or empty when validation passes, the field
     *         has no reachable validator, or the validator throws
     */
    static Optional<String> firstError(HasValue<?, ?> field,
            Binding<?, ?> binding) {
        if (binding != null) {
            return errorFromBinding(binding);
        }
        if (field instanceof HasValidator<?>) {
            return errorFromHasValidator(field);
        }
        return Optional.empty();
    }

    /**
     * Returns the bean-level (cross-field) rejection messages for the
     * post-write state of {@code binder}. These come from
     * {@code binder.withValidator((bean, ctx) -> ...)} rules, which only run
     * when a bean is set ({@code setBean}) and every binding is individually
     * valid; with no bean, a {@code null} binder, or a per-field error pending,
     * the list is empty. The underlying validation does not modify the UI, so
     * fields this turn did not write are not marked invalid.
     *
     * @param binder
     *            the binder to validate, or {@code null} when the form has no
     *            binder
     * @return the cross-field messages, never {@code null}; empty when there
     *         are none
     */
    static List<String> beanErrors(Binder<?> binder) {
        return BinderReflection.beanValidationErrors(binder).stream()
                .map(result -> message(result.getErrorMessage())).toList();
    }

    private static Optional<String> errorFromBinding(Binding<?, ?> binding) {
        try {
            // fireEvent=false: setValue already triggered the binder's
            // value-change listener, which fired its own validation events
            // and updated the field's invalid indicator. This call is only
            // here to read the message — re-firing would just duplicate
            // the status-change notifications the application already saw.
            var status = binding.validate(false);
            if (!status.isError()) {
                return Optional.empty();
            }
            return Optional.of(message(status.getMessage().orElse(null)));
        } catch (Exception ex) {
            LOGGER.warn("Binding validation threw for {}",
                    binding.getField().getClass(), ex);
            return Optional.empty();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Optional<String> errorFromHasValidator(
            HasValue<?, ?> field) {
        try {
            var validator = ((HasValidator) field).getDefaultValidator();
            if (validator == null) {
                return Optional.empty();
            }
            // The controller rejects non-Component fields at registration,
            // so every field that reaches this code is a Component.
            var context = new ValueContext((Component) field);
            var outcome = validator.apply(field.getValue(), context);
            if (outcome == null || !outcome.isError()) {
                return Optional.empty();
            }
            return Optional.of(message(outcome.getErrorMessage()));
        } catch (Exception ex) {
            LOGGER.warn("Default validator threw for {}", field.getClass(), ex);
            return Optional.empty();
        }
    }

    private static String message(String raw) {
        return raw == null || raw.isBlank() ? GENERIC_REJECTION_MESSAGE : raw;
    }
}
