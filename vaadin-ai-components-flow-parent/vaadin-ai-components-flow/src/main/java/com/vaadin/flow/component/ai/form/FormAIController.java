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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;

/**
 * Populates a layout's fields with values an LLM extracts from a user prompt or
 * attached files. Attach it to an {@link AIOrchestrator} via
 * {@link AIOrchestrator.Builder#withController(AIController)
 * withController(...)}.
 *
 * <p>
 * The controller accepts any {@link HasComponents} container. It discovers
 * fields by walking the container's component tree and collecting every
 * component that implements {@link HasValue}. The walk recurses into nested
 * {@link HasComponents} children so layouts containing layouts are handled.
 * </p>
 *
 * <p>
 * <b>Serialization:</b> the controller is not serialized with the orchestrator.
 * After deserialization, create a new controller against the same form and call
 * {@code orchestrator.reconnect(provider).withController(controller).apply()}.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class FormAIController implements AIController {

    /**
     * Key under which a field's opaque id is stored on the field component via
     * {@link ComponentUtil#setData(Component, String, Object)}. The id survives
     * removing and re-adding the field within a session.
     */
    static final String FIELD_ID_KEY = "vaadin.ai.form.fieldId";

    private final Component form;
    private final Map<String, FormFieldHints> hintsById = new HashMap<>();

    /**
     * Creates a new form AI controller for the given container. Fields are
     * discovered by walking the container's component tree each time the
     * controller is asked for tools, so fields added or removed between turns
     * are picked up automatically.
     *
     * @param form
     *            the container whose fields the LLM may populate, not
     *            {@code null}
     * @param <T>
     *            the container type
     */
    public <T extends Component & HasComponents> FormAIController(T form) {
        Objects.requireNonNull(form, "Form must not be null");
        this.form = form;
    }

    /**
     * Adds a free-form description that the LLM sees alongside the field when
     * deciding what to fill in. Use it to add business semantics that are not
     * implied by the field's label, helper text, or component type (for example
     * clarifying that a numeric field expects a percentage rather than an
     * absolute amount). Later calls for the same field overwrite earlier ones.
     *
     * @param field
     *            the field to describe, not {@code null}
     * @param description
     *            the description text, not {@code null}
     * @return this controller, for chaining
     */
    public FormAIController describe(HasValue<?, ?> field, String description) {
        Objects.requireNonNull(description, "Description must not be null");
        hintsFor(field).description = description;
        return this;
    }

    /**
     * Declares the set of values the LLM may pick for the field. The query
     * callback receives a filter string and a limit and returns matching
     * options as the labels the LLM should see; the {@code toValue} function
     * converts a chosen label back to the field's value type. Later calls for
     * the same field overwrite earlier ones.
     *
     * @param field
     *            the field whose options the LLM may query, not {@code null}
     * @param query
     *            the filter callback returning labels for the LLM, not
     *            {@code null}
     * @param toValue
     *            converts a chosen label to the field's value type, not
     *            {@code null}
     * @param <T>
     *            the field's value type
     * @return this controller, for chaining
     */
    public <T> FormAIController valueOptions(HasValue<?, T> field,
            BiFunction<String, Integer, List<String>> query,
            Function<String, T> toValue) {
        Objects.requireNonNull(query, "Query function must not be null");
        Objects.requireNonNull(toValue, "Value converter must not be null");
        var hints = hintsFor(field);
        hints.valueOptionsQuery = query;
        hints.valueOptionsToValue = toValue;
        return this;
    }

    /**
     * Declares a fixed set of labels the LLM may pick for the field. The
     * {@code toValue} function converts a chosen label back to the field's
     * value type. Later calls for the same field overwrite earlier ones.
     *
     * @param field
     *            the field whose options the LLM may pick from, not
     *            {@code null}
     * @param options
     *            the labels the LLM may pick from, not {@code null}; a
     *            defensive copy is taken
     * @param toValue
     *            converts a chosen label to the field's value type, not
     *            {@code null}
     * @param <T>
     *            the field's value type
     * @return this controller, for chaining
     */
    public <T> FormAIController valueOptions(HasValue<?, T> field,
            Collection<String> options, Function<String, T> toValue) {
        Objects.requireNonNull(options, "Options must not be null");
        var snapshot = List.copyOf(options);
        return valueOptions(field, (filter, limit) -> {
            var matches = snapshot.stream();
            if (filter != null && !filter.isEmpty()) {
                var needle = filter.toLowerCase(Locale.ROOT);
                matches = matches.filter(
                        o -> o.toLowerCase(Locale.ROOT).contains(needle));
            }
            return matches.limit(limit).toList();
        }, toValue);
    }

    /**
     * Hides the given field from the LLM. The field is excluded from the tool
     * surface and is not locked during a fill. Use this for fields the AI must
     * not read or write (password fields, internal IDs, PII).
     *
     * @param field
     *            the field to hide, not {@code null}
     * @return this controller, for chaining
     */
    public FormAIController ignore(HasValue<?, ?> field) {
        hintsFor(field).ignored = true;
        return this;
    }

    @Override
    public List<LLMProvider.ToolSpec> getTools() {
        return FormAITools.createAll(new ToolCallbacks());
    }

    @Override
    public void onRequestStart() {
        // Refresh the field set so fields added or removed between turns
        // are picked up.
        attachIds();
    }

    @Override
    public void onResponseComplete() {
    }

    private FormFieldHints hintsFor(HasValue<?, ?> field) {
        Objects.requireNonNull(field, "Field must not be null");
        return hintsById.computeIfAbsent(getOrCreateId(field),
                k -> new FormFieldHints());
    }

    /**
     * Walks the form tree and ensures every discovered field has an id
     * attached. Ids already attached are left untouched so they stay stable
     * across removals, re-additions, and discovery walks.
     */
    private void attachIds() {
        FormFieldDiscovery.collectFields(form)
                .forEach(FormAIController::getOrCreateId);
    }

    private static String getOrCreateId(HasValue<?, ?> field) {
        if (!(field instanceof Component component)) {
            throw new IllegalArgumentException(
                    "Field must be a Component: " + field.getClass().getName());
        }
        var id = (String) ComponentUtil.getData(component, FIELD_ID_KEY);
        if (id == null) {
            id = UUID.randomUUID().toString();
            ComponentUtil.setData(component, FIELD_ID_KEY, id);
        }
        return id;
    }

    private final class ToolCallbacks implements FormAITools.Callbacks {

        @Override
        public List<String> queryFieldOptions(String fieldId, String filter,
                int limit) {
            var hints = hintsById.get(fieldId);
            if (hints == null || hints.valueOptionsQuery == null) {
                throw new IllegalArgumentException(
                        "Field has no value options: " + fieldId);
            }
            return new ArrayList<>(
                    hints.valueOptionsQuery.apply(filter, limit));
        }
    }
}
