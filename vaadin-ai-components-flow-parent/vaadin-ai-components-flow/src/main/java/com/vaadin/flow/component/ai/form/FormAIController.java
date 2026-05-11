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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.ai.form.FormValueConverter.RejectedValueException;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;

import tools.jackson.databind.JsonNode;

/**
 * AI controller for populating a Vaadin {@link FormLayout} from a user prompt
 * or an attached document via LLM tool calls. Attach it to an
 * {@link AIOrchestrator} via
 * {@link AIOrchestrator.Builder#withController(AIController) withController(...)}
 * to expose its tools to the LLM.
 *
 * <pre>
 * var form = new FormLayout(merchant, amount, currency, date, category, notes);
 * AIOrchestrator.builder(provider,
 *         "Help the user log an expense from a prompt or an attached receipt.")
 *         .withInput(prompt)
 *         .withFileReceiver(uploadManager)
 *         .withController(new FormAIController(form))
 *         .build();
 * </pre>
 * <p>
 * The controller discovers fields by walking the form's component tree and
 * collecting every component that implements {@link HasValue}. When a
 * {@link Binder} is supplied, bound property names are preferred over
 * label-derived identifiers; per-field validators registered through the
 * binder are run against AI-supplied values and rejections are reported back
 * to the LLM in the tool result.
 * </p>
 * <p>
 * Per-field metadata not derivable from the layout or the binder can be
 * registered through a small fluent surface:
 * </p>
 *
 * <pre>
 * controller
 *     .describe(merchant, "The vendor or business name as shown on the receipt.")
 *     .allowedValues(currency, List.of("EUR", "USD", "GBP"))
 *     .queryable(project, projectService::search)
 *     .resolveItemFromString(project, projectService::findByName)
 *     .as(amountMin, "amount_min")
 *     .ignore(internalReviewerNote);
 * </pre>
 *
 * <h2>What the controller exposes to the LLM</h2>
 * <ul>
 * <li><b>fill_form</b> — single JSON-schema tool whose properties match the
 * discovered fields. Every property is optional. Missing keys leave the field
 * untouched. Current field values are appended to each property description
 * as a {@code (current: ...)} suffix so the model knows the starting
 * state.</li>
 * <li><b>query_field_options</b> — generated only when at least one field is
 * registered via {@link #queryable(HasValue, BiFunction)}. The LLM can narrow
 * large option sets the same way a user types into a ComboBox.</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 * <li>{@link #onRequestStart()} locks every non-ignored field via
 * {@code setReadOnly(true)}.</li>
 * <li>The LLM may call {@code query_field_options} zero or more times.</li>
 * <li>The LLM calls {@code fill_form}; values are written through
 * {@code setValue(...)} and validation is run before the tool result is
 * returned.</li>
 * <li>{@link #onResponseComplete()} or {@link #onResponseFailed(Throwable)}
 * restores the read-only state on every locked field.</li>
 * </ol>
 *
 * <p>
 * Tool definitions live in {@link FormAITools}; this class owns the field map
 * and side effects, the tool factory owns the JSON-schema generation and
 * argument parsing. The two communicate through a {@link FormAITools.Callbacks
 * Callbacks} instance.
 * </p>
 *
 * <p>
 * <b>Serialization:</b> Like {@code GridAIController} and
 * {@code ChartAIController}, this controller is not serialized with the
 * orchestrator. After deserialization, create a new controller against the
 * same form and reattach hints, then call
 * {@code orchestrator.reconnect(provider).withController(controller).apply()}.
 * </p>
 *
 * @author Vaadin Ltd
 * @see FormAITools
 */
public class FormAIController implements AIController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(FormAIController.class);

    private final FormLayout form;
    private final Binder<?> binder;
    private final List<FormFieldEntry> entries = new ArrayList<>();
    private final Map<HasValue<?, ?>, FormFieldEntry> byField = new LinkedHashMap<>();
    private final Map<String, List<Object>> queryCache = new LinkedHashMap<>();
    private final Map<HasValue<?, ?>, Boolean> readOnlyBefore = new LinkedHashMap<>();

    private boolean frozen;

    /**
     * Creates a new form AI controller for the given form. Fields are
     * discovered by walking the form's component tree; metadata is read from
     * each field's label, helper text, and component type.
     *
     * @param form
     *            the form whose fields the LLM may populate, not {@code null}
     */
    public FormAIController(FormLayout form) {
        this(form, null);
    }

    /**
     * Creates a new form AI controller for the given form and binder. Bound
     * property names are preferred over label-derived identifiers; per-field
     * validators registered through the binder are run against AI-supplied
     * values.
     *
     * @param form
     *            the form whose fields the LLM may populate, not {@code null}
     * @param binder
     *            the binder, or {@code null} to use container-walk discovery
     *            only
     */
    public FormAIController(FormLayout form, Binder<?> binder) {
        this.form = Objects.requireNonNull(form, "Form must not be null");
        this.binder = binder;
        discoverEntries();
    }

    private void discoverEntries() {
        Map<HasValue<?, ?>, String> propertyNames = binder == null
                ? Collections.emptyMap()
                : BinderReflection.collectPropertyNames(binder);
        for (HasValue<?, ?> field : FormFieldDiscovery.collectFields(form)) {
            FormFieldType type = FormFieldType.classify(field);
            if (type == FormFieldType.UNSUPPORTED) {
                continue;
            }
            String label = field instanceof HasLabel hl ? hl.getLabel() : null;
            String propertyName = propertyNames.get(field);
            String identifier = propertyName != null ? propertyName
                    : FormFieldDiscovery.normalize(label);
            FormFieldEntry entry = new FormFieldEntry(field, identifier, label,
                    type);
            if (field instanceof HasHelper hh) {
                entry.helperText = hh.getHelperText();
            }
            entries.add(entry);
            byField.put(field, entry);
        }
    }

    // ----- Fluent hint API -----------------------------------------------

    /**
     * Adds a free-form description for the given field, surfaced to the LLM
     * alongside the auto-generated metadata.
     */
    public FormAIController describe(HasValue<?, ?> field,
            String description) {
        requireEntry(field).description = Objects.requireNonNull(description,
                "Description must not be null");
        return this;
    }

    /**
     * Restricts the field's accepted values to the given list. The values are
     * emitted as the {@code enum} keyword in the field's JSON Schema. Items
     * are rendered to strings via the field's {@link ItemLabelGenerator} when
     * one is set, or via {@link Object#toString()} otherwise.
     */
    public <T> FormAIController allowedValues(HasValue<?, T> field,
            List<? extends T> values) {
        requireEntry(field).allowedValues = new ArrayList<>(
                Objects.requireNonNull(values, "Values must not be null"));
        return this;
    }

    /**
     * Registers a queryable option callback for a field whose values are
     * dynamic, large, or backend-loaded. The LLM can call the
     * {@code query_field_options} tool to narrow the choices the same way a
     * user types into a ComboBox.
     */
    public <T> FormAIController queryable(HasValue<?, T> field,
            BiFunction<String, Integer, ? extends List<? extends T>> query) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        BiFunction<String, Integer, List<Object>> erased = (BiFunction) query;
        requireEntry(field).queryable = Objects.requireNonNull(erased,
                "Query function must not be null");
        return this;
    }

    /**
     * Registers a string-to-item parser for fields where the default label
     * match is not enough. Pairs with {@link #queryable(HasValue, BiFunction)}
     * or with backend-typed selectors.
     */
    public <T> FormAIController resolveItemFromString(HasValue<?, T> field,
            Function<String, ? extends T> resolver) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Function<String, Object> erased = (Function) resolver;
        requireEntry(field).itemResolver = Objects.requireNonNull(erased,
                "Resolver must not be null");
        return this;
    }

    /**
     * Pins an explicit identifier for the given field, overriding the
     * label-derived default. Use this to break label collisions or to keep
     * the key stable when the visible label changes for UX reasons.
     */
    public FormAIController as(HasValue<?, ?> field, String key) {
        Objects.requireNonNull(key, "Key must not be null");
        if (key.isBlank()) {
            throw new IllegalArgumentException("Key must not be blank");
        }
        requireEntry(field).identifier = key;
        return this;
    }

    /**
     * Hides the field from the LLM. The field is excluded from both
     * {@code fill_form} and {@code query_field_options}, and is never locked
     * during a turn. Use this for password fields, internal IDs, and any
     * field that must not be read or written by the AI.
     */
    public FormAIController ignore(HasValue<?, ?> field) {
        requireEntry(field).ignored = true;
        return this;
    }

    private FormFieldEntry requireEntry(HasValue<?, ?> field) {
        Objects.requireNonNull(field, "Field must not be null");
        FormFieldEntry entry = byField.get(field);
        if (entry == null) {
            throw new IllegalArgumentException(
                    "Field is not part of the form (or its component type is "
                            + "unsupported): " + describe(field));
        }
        return entry;
    }

    private static String describe(HasValue<?, ?> field) {
        if (field instanceof HasLabel hl && hl.getLabel() != null) {
            return field.getClass().getSimpleName() + " label=\""
                    + hl.getLabel() + "\"";
        }
        return field.getClass().getSimpleName();
    }

    // ----- AIController contract -----------------------------------------

    @Override
    public List<LLMProvider.ToolSpec> getTools() {
        freezeAndValidate();
        return FormAITools.createAll(new ToolCallbacks());
    }

    private void freezeAndValidate() {
        if (frozen) {
            return;
        }
        Map<String, FormFieldEntry> seen = new LinkedHashMap<>();
        for (FormFieldEntry e : entries) {
            if (e.ignored) {
                continue;
            }
            if (e.identifier == null || e.identifier.isEmpty()) {
                throw new IllegalStateException(
                        "Cannot derive identifier for field " + e.describe()
                                + ". Set a label, bind it to a bean property, "
                                + "or pin a key with .as(field, \"key\").");
            }
            FormFieldEntry previous = seen.putIfAbsent(e.identifier, e);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate field identifier \"" + e.identifier
                                + "\" derived from " + previous.describe()
                                + " and " + e.describe()
                                + ". Pin a unique key with .as(field, \"key\").");
            }
        }
        frozen = true;
    }

    @Override
    public void onRequestStart() {
        readOnlyBefore.clear();
        for (FormFieldEntry e : visibleEntries()) {
            readOnlyBefore.put(e.field, e.field.isReadOnly());
            e.field.setReadOnly(true);
            blur(e.field);
        }
    }

    @Override
    public void onResponseComplete() {
        unlockFields();
        queryCache.clear();
    }

    @Override
    public void onResponseFailed(Throwable error) {
        unlockFields();
        queryCache.clear();
    }

    private void unlockFields() {
        readOnlyBefore.forEach((field, wasReadOnly) -> {
            try {
                field.setReadOnly(wasReadOnly);
            } catch (Exception ex) {
                LOGGER.warn("Failed to restore read-only state", ex);
            }
        });
        readOnlyBefore.clear();
        for (FormFieldEntry e : entries) {
            e.previousValueCaptured = false;
            e.previousValue = null;
        }
    }

    private static void blur(HasValue<?, ?> field) {
        if (field instanceof Component component) {
            component.getElement().executeJs("this.blur && this.blur();");
        }
    }

    private List<FormFieldEntry> visibleEntries() {
        return entries.stream().filter(e -> !e.ignored).toList();
    }

    // ----- Test hook ------------------------------------------------------

    /**
     * Returns the identifier the controller derived for the given field, or
     * {@code null} if the field is not part of the controller.
     */
    public String getIdentifier(HasValue<?, ?> field) {
        Objects.requireNonNull(field, "Field must not be null");
        FormFieldEntry entry = byField.get(field);
        return entry == null ? null : entry.identifier;
    }

    // ----- Callbacks for FormAITools -------------------------------------

    private final class ToolCallbacks implements FormAITools.Callbacks {

        @Override
        public List<FormFieldEntry> visibleEntries() {
            return FormAIController.this.visibleEntries();
        }

        @Override
        public FormFieldEntry findById(String identifier) {
            FormFieldEntry e = entries.stream()
                    .filter(x -> !x.ignored && x.identifier.equals(identifier))
                    .findFirst().orElse(null);
            return e;
        }

        @Override
        public String executeFill(JsonNode arguments) {
            if (arguments == null || !arguments.isObject()) {
                return "Error: arguments must be a JSON object.";
            }
            Map<String, String> rejected = new LinkedHashMap<>();
            for (FormFieldEntry e : visibleEntries()) {
                if (!arguments.has(e.identifier)) {
                    continue;
                }
                JsonNode value = arguments.get(e.identifier);
                try {
                    Object converted = FormValueConverter.convert(e, value,
                            queryCache);
                    writeValue(e, converted);
                    String validationError = validate(e);
                    if (validationError != null) {
                        rejected.put(e.identifier, validationError);
                        revertValue(e);
                    }
                } catch (RejectedValueException rex) {
                    rejected.put(e.identifier, rex.getMessage());
                } catch (Exception ex) {
                    LOGGER.warn("Failed to apply value for field {}",
                            e.identifier, ex);
                    rejected.put(e.identifier,
                            "Could not apply value: " + ex.getMessage());
                }
            }
            return formatResult(rejected);
        }

        @Override
        public void cacheQueryResults(String identifier, List<Object> items) {
            queryCache.computeIfAbsent(identifier, k -> new ArrayList<>())
                    .addAll(items);
        }

        private String formatResult(Map<String, String> rejected) {
            StringBuilder b = new StringBuilder();
            b.append("Current state:\n");
            for (FormFieldEntry e : visibleEntries()) {
                b.append("  ").append(e.identifier).append(": ")
                        .append(FormValueConverter.displayValue(e)).append('\n');
            }
            if (!rejected.isEmpty()) {
                b.append("\nRejected:\n");
                rejected.forEach((k, v) -> b.append("  ").append(k).append(": ")
                        .append(v).append('\n'));
            }
            return b.toString();
        }
    }

    // ----- Value writing + validation ------------------------------------

    private void writeValue(FormFieldEntry e, Object converted) {
        e.previousValue = e.field.getValue();
        e.previousValueCaptured = true;
        @SuppressWarnings({ "rawtypes", "unchecked" })
        HasValue raw = e.field;
        form.getElement().getNode().runWhenAttached((d) -> {
            d.access(() -> {
                raw.setValue(converted);
            });
            
        });
        
    }

    private void revertValue(FormFieldEntry e) {
        if (!e.previousValueCaptured) {
            return;
        }
        @SuppressWarnings({ "rawtypes", "unchecked" })
        HasValue raw = e.field;
        try {
            raw.setValue(e.previousValue);
        } catch (Exception ex) {
            LOGGER.warn("Failed to revert value for field {}", e.identifier,
                    ex);
        }
        e.previousValueCaptured = false;
        e.previousValue = null;
    }

    private String validate(FormFieldEntry e) {
        if (binder != null) {
            Binding<?, ?> binding = BinderReflection.findBinding(binder,
                    e.field);
            if (binding != null) {
                var status = binding.validate(false);
                if (status.isError()) {
                    return status.getMessage().orElse("Validation failed");
                }
                return null;
            }
        }
        if (e.field instanceof HasValidator<?> hv) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            HasValidator hvRaw = hv;
            try {
                ValidationResult result = (ValidationResult) hvRaw
                        .getDefaultValidator()
                        .apply(e.field.getValue(), new ValueContext());
                if (result != null && result.isError()) {
                    return result.getErrorMessage();
                }
            } catch (Exception ex) {
                LOGGER.debug("Default validator failed for {}", e.identifier,
                        ex);
            }
        }
        return null;
    }
}
