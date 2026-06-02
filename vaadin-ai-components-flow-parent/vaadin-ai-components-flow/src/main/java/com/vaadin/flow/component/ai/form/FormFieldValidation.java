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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValueContext;

/**
 * Surfaces rejection reasons for the fields the controller has just written
 * with LLM-supplied values. The output drives the {@code fill_form} tool's
 * {@code rejected} block — the LLM only needs the first complaint per field to
 * pick a corrected value on the next turn.
 *
 * <p>
 * Validation runs as a single pass after every value has been written, so each
 * field is judged against the fully-written form rather than a partial
 * snapshot. {@link #validate(Binder)} runs the binder once: a single
 * {@link Binder#validate()} covers every binding's chain (converter, default
 * {@link HasValidator} validator, every {@code withValidator} registration) and
 * the cross-field {@code binder.withValidator((bean, ctx) -> ...)} rules,
 * returning the per-field messages keyed by field plus the bean-level messages
 * that are not tied to any single field. {@link #unboundFieldError(HasValue)}
 * complements that for fields the binder does not manage: an unbound field that
 * implements {@link HasValidator} runs that validator directly.
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
     * The outcome of one post-write validation pass over a binder: the first
     * rejection message per field keyed by the field, plus the cross-field
     * (bean-level) rejection messages that are not tied to any single field.
     *
     * @param fieldErrors
     *            the per-field messages keyed by the field, never {@code null}
     * @param beanErrors
     *            the cross-field messages, never {@code null}
     */
    record BinderErrors(Map<HasValue<?, ?>, String> fieldErrors,
            List<String> beanErrors) {
    }

    /**
     * Runs the binder's validation once over the current (post-write) state and
     * collects both per-field and cross-field errors. A single
     * {@link Binder#validate()} evaluates every binding plus the bean-level
     * rules registered with {@code binder.withValidator((bean, ctx) -> ...)},
     * so the verdict is order-independent over the fully-written form.
     * Bean-level rules only run when a bean is set ({@code setBean}); with no
     * bean, or a {@code null} binder, the bean errors are empty.
     *
     * @param binder
     *            the binder to validate, or {@code null} when the form has no
     *            binder
     * @return the field and bean errors, never {@code null}; empty when there
     *         is no binder or validation throws
     */
    static BinderErrors validate(Binder<?> binder) {
        if (binder == null) {
            return new BinderErrors(Map.of(), List.of());
        }
        try {
            var status = binder.validate();
            var fieldErrors = new LinkedHashMap<HasValue<?, ?>, String>();
            for (var bindingStatus : status.getFieldValidationErrors()) {
                // First error per field wins; the LLM only needs one complaint
                // to pick a corrected value next turn.
                fieldErrors.putIfAbsent(bindingStatus.getField(),
                        message(bindingStatus.getMessage().orElse(null)));
            }
            var beanErrors = status.getBeanValidationErrors().stream()
                    .map(result -> message(result.getErrorMessage())).toList();
            return new BinderErrors(fieldErrors, beanErrors);
        } catch (Exception ex) {
            LOGGER.warn("Binder validation threw for binder {}",
                    binder.getClass(), ex);
            return new BinderErrors(Map.of(), List.of());
        }
    }

    /**
     * Returns the first rejection message for an unbound field, if any. Bound
     * fields are validated as a group by {@link #validate(Binder)}; this path
     * covers fields the binder does not manage. An unbound field that
     * implements {@link HasValidator} runs that validator directly; anything
     * else has no validator the controller can reach, so the write is accepted
     * as-is.
     *
     * @param field
     *            the unbound field whose post-write value should be checked,
     *            not {@code null}
     * @return the rejection message, or empty when validation passes, the field
     *         has no reachable validator, or the validator throws
     */
    static Optional<String> unboundFieldError(HasValue<?, ?> field) {
        if (field instanceof HasValidator<?>) {
            return errorFromHasValidator(field);
        }
        return Optional.empty();
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
