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
 * Surfaces a single rejection reason for a field after the controller has just
 * written an LLM-supplied value. The output drives one entry in the
 * {@code fill_form} tool's {@code rejected} block — the LLM only needs the
 * first complaint per field to pick a corrected value on the next turn.
 *
 * <p>
 * The lookup follows a strict precedence: a bound field defers entirely to its
 * {@link Binding}, which means the binder's own chain (converter, default
 * {@link HasValidator} validator, every {@code withValidator} registration)
 * runs as one unit and the binder-level message is reported. An unbound field
 * that implements {@link HasValidator} runs that validator directly. Anything
 * else has no validator the controller can reach, so the write is accepted
 * as-is.
 * </p>
 *
 * <p>
 * {@link #beanLevelErrors(Binder)} complements that per-field path: cross-field
 * rules registered with {@code binder.withValidator((bean, ctx) -> ...)} are
 * not tied to a single field and never run during a per-binding validate, so
 * they are evaluated separately against the post-write bean.
 * </p>
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
     * Returns the first rejection message for {@code field}, if any.
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
     * Runs the binder's bean-level (cross-field) validators against the current
     * bean and returns one message per failure. These are the rules registered
     * with {@code binder.withValidator((bean, ctx) -> ...)}; they are not bound
     * to a single field, so the per-field {@link #firstError} path never runs
     * them. {@link Binder#validate()} only evaluates them when a bean is set
     * ({@code setBean}); with no bean, or a {@code null} binder, the result is
     * empty.
     *
     * @param binder
     *            the binder whose bean-level validators to run, or {@code null}
     *            when the form has no binder
     * @return the cross-field rejection messages, never {@code null}; empty
     *         when there is no binder, no bean, or all bean-level rules pass
     */
    static List<String> beanLevelErrors(Binder<?> binder) {
        if (binder == null) {
            return List.of();
        }
        try {
            return binder.validate().getBeanValidationErrors().stream()
                    .map(result -> {
                        var message = result.getErrorMessage();
                        return message == null || message.isBlank()
                                ? GENERIC_REJECTION_MESSAGE
                                : message;
                    }).toList();
        } catch (Exception ex) {
            LOGGER.warn("Bean-level validation threw for binder {}",
                    binder.getClass(), ex);
            return List.of();
        }
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
            return Optional.of(status.getMessage().filter(m -> !m.isBlank())
                    .orElse(GENERIC_REJECTION_MESSAGE));
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
            var message = outcome.getErrorMessage();
            return Optional.of(message == null || message.isBlank()
                    ? GENERIC_REJECTION_MESSAGE
                    : message);
        } catch (Exception ex) {
            LOGGER.warn("Default validator threw for {}", field.getClass(), ex);
            return Optional.empty();
        }
    }
}
