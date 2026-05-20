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
import java.util.Optional;
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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;

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
 * <b>Field identifiers:</b> the controller assigns an opaque UUID to each field
 * at discovery time and uses that UUID as the field's id in every tool call the
 * LLM makes. Developers never see UUIDs; the LLM never sees field labels in the
 * id slot. Semantic meaning travels through each field's <i>description</i> —
 * the field's label, helper text, and the {@link #describe(HasValue, String)}
 * hint.
 * </p>
 *
 * <p>
 * <b>Binder integration:</b> the two-argument constructor accepts a
 * {@link Binder}. For every named binding ({@code bind("propertyName")},
 * {@code bindInstanceFields(this)}, or {@code @PropertyId}) the property name
 * is used as a default field description so the LLM can refer to the field by
 * its bean-side name. The default only applies when no explicit
 * {@link #describe(HasValue, String)} has been registered; calling
 * {@code describe(...)} always wins. Lambda-bound bindings carry no property
 * name and contribute no default.
 * </p>
 *
 * <p>
 * <b>Field locking:</b> while a fill is in progress, every non-ignored field
 * that wasn't already read-only is set to read-only so the user cannot type
 * into a field the AI is about to overwrite. Locks are released when the turn
 * ends, successfully or otherwise. Application code that changes a field's
 * read-only state mid-turn (e.g. from a value-change listener reacting to the
 * LLM's writes) will be overridden when the controller releases its own locks
 * at turn end — applications should avoid toggling read-only state during a
 * fill turn, or reapply it after the turn completes.
 * </p>
 *
 * <p>
 * <b>Serialization:</b> the controller is not serialized with the orchestrator.
 * After deserialization, create a new controller against the same form (and
 * binder, if any) and call
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
    private final Binder<?> binder;
    private final Map<String, FormFieldHints> hintsById = new HashMap<>();
    private final List<HasValue<?, ?>> lockedFields = new ArrayList<>();

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
        this.binder = null;
    }

    /**
     * Creates a new form AI controller for the given container and binder. For
     * every named binding on the binder, the bean property name is used as a
     * default {@link #describe(HasValue, String) description} when the
     * developer has not registered one explicitly; the controller itself still
     * uses an opaque UUID as the field's tool-call id. Lambda-bound bindings
     * carry no property name and contribute no default. A field that is part of
     * the layout but not bound to the supplied binder behaves the same as in
     * the no-binder constructor.
     *
     * @param form
     *            the container whose fields the LLM may populate, not
     *            {@code null}
     * @param binder
     *            the binder whose property names default the field
     *            descriptions, not {@code null}; use the single-argument
     *            constructor for the no-binder case
     * @param <T>
     *            the container type
     * @throws NullPointerException
     *             if {@code form} or {@code binder} is {@code null}
     */
    public <T extends Component & HasComponents> FormAIController(T form,
            Binder<?> binder) {
        Objects.requireNonNull(form, "Form must not be null");
        Objects.requireNonNull(binder, "Binder must not be null");
        this.form = form;
        this.binder = binder;
    }

    /**
     * Returns the {@link Binding} registered for the given field on the binder
     * supplied to this controller. Returns an empty optional for unbound fields
     * and for controllers constructed without a binder. The lookup is live
     * against the binder's current state, not a snapshot taken at construction.
     *
     * @param field
     *            the field, not {@code null}
     * @return the binding, or empty if the field is not bound
     * @throws NullPointerException
     *             if {@code field} is {@code null}
     */
    Optional<Binding<?, ?>> findBinding(HasValue<?, ?> field) {
        Objects.requireNonNull(field, "Field must not be null");
        if (binder == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BinderReflection.findBinding(binder, field));
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
        hints.fixedOptions = false;
        return this;
    }

    /**
     * Declares the set of values the LLM may pick for a {@link String}-typed
     * field. The query callback receives a filter string and a limit and
     * returns matching options; each label is used as the field value as-is.
     * Later calls for the same field overwrite earlier ones.
     *
     * @param field
     *            the field whose options the LLM may query, not {@code null}
     * @param query
     *            the filter callback returning labels for the LLM, not
     *            {@code null}
     * @return this controller, for chaining
     */
    public FormAIController valueOptions(HasValue<?, String> field,
            BiFunction<String, Integer, List<String>> query) {
        return valueOptions(field, query, Function.identity());
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
        valueOptions(field, (filter, limit) -> {
            var matches = snapshot.stream();
            if (filter != null && !filter.isEmpty()) {
                var needle = filter.toLowerCase(Locale.ROOT);
                matches = matches.filter(
                        o -> o.toLowerCase(Locale.ROOT).contains(needle));
            }
            return matches.limit(limit).toList();
        }, toValue);
        hintsFor(field).fixedOptions = true;
        return this;
    }

    /**
     * Declares a fixed set of labels the LLM may pick for a
     * {@link String}-typed field. Each chosen label is used as the field value
     * as-is. Later calls for the same field overwrite earlier ones.
     *
     * @param field
     *            the field whose options the LLM may pick from, not
     *            {@code null}
     * @param options
     *            the labels the LLM may pick from, not {@code null}; a
     *            defensive copy is taken
     * @return this controller, for chaining
     */
    public FormAIController valueOptions(HasValue<?, String> field,
            Collection<String> options) {
        return valueOptions(field, options, Function.identity());
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
        seedDescriptionsFromBinder();
        lockFields();
    }

    @Override
    public void onResponseComplete() {
        unlockFields();
    }

    @Override
    public void onResponseFailed(Throwable error) {
        unlockFields();
    }

    /**
     * Walks the binder's property names and defaults {@code hints.description}
     * for any bound field that does not already have an explicit description.
     * Called at the start of every turn so bindings added or removed between
     * turns are reflected; an explicit {@link #describe(HasValue, String)}
     * always wins because the seeding only fills in nulls.
     */
    private void seedDescriptionsFromBinder() {
        if (binder == null) {
            return;
        }
        var propertyNames = BinderReflection.collectPropertyNames(binder);
        for (var entry : propertyNames.entrySet()) {
            var field = entry.getKey();
            var name = entry.getValue();
            if (name == null || !(field instanceof Component)) {
                continue;
            }
            var hints = hintsFor(field);
            if (hints.description == null) {
                hints.description = name;
            }
        }
    }

    /**
     * Puts every discovered, non-ignored, currently editable field into
     * read-only state so the user cannot type into a field the AI is about to
     * overwrite. Fields that were already read-only when the turn started are
     * left untouched and will not be unlocked at turn end.
     */
    private void lockFields() {
        for (var field : collectActiveFields()) {
            if (field.isReadOnly()) {
                continue;
            }
            field.setReadOnly(true);
            lockedFields.add(field);
        }
    }

    /**
     * Returns the discovered fields the controller acts on — every
     * {@link HasValue} in the form tree minus those hidden via
     * {@link #ignore(HasValue)}. Use this anywhere the LLM-visible field set
     * matters (locking, tool inputs and outputs).
     */
    private List<HasValue<?, ?>> collectActiveFields() {
        return FormFieldDiscovery.collectFields(form).stream()
                .filter(field -> !isIgnored(field)).toList();
    }

    /**
     * Restores fields locked by {@link #lockFields()} to read-write. Fields the
     * application set to read-only before the turn started are not touched
     * (they were skipped at lock time).
     */
    private void unlockFields() {
        for (var field : lockedFields) {
            field.setReadOnly(false);
        }
        lockedFields.clear();
    }

    private boolean isIgnored(HasValue<?, ?> field) {
        var id = (String) ComponentUtil.getData((Component) field,
                FIELD_ID_KEY);
        var hints = hintsById.get(id);
        return hints != null && hints.ignored;
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
        public List<FormAITools.FormFieldDescriptor> visibleFields() {
            var descriptors = new ArrayList<FormAITools.FormFieldDescriptor>();
            for (var field : collectActiveFields()) {
                var type = FormFieldType.classify(field);
                if (type == FormFieldType.UNSUPPORTED) {
                    continue;
                }
                var id = getOrCreateId(field);
                descriptors.add(new FormAITools.FormFieldDescriptor(id, field,
                        type, hintsById.get(id)));
            }
            return descriptors;
        }

        @Override
        public List<String> queryFieldOptions(String fieldId, String filter,
                int limit) {
            var hints = hintsById.get(fieldId);
            if (hints == null || hints.valueOptionsQuery == null) {
                throw new FormAITools.ToolException(
                        "Unknown field id: " + fieldId);
            }
            return new ArrayList<>(
                    hints.valueOptionsQuery.apply(filter, limit));
        }
    }
}
