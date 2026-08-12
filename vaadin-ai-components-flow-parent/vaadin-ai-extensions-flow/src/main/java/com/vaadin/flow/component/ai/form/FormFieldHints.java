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
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Mutable per-field hint state held by {@link FormAIController}, keyed by the
 * field's opaque id. Populated by {@code describeField}, {@code ignoreField},
 * and {@code fieldValueOptions}; consumed by {@link FormFieldSchema} when
 * building the {@code get_form_state} payload and by {@link FormValueConverter}
 * when applying {@code fill_form} values.
 *
 * @author Vaadin Ltd
 */
final class FormFieldHints {

    String description;
    /**
     * Label-producing callback the {@code query_field_options} tool drives.
     * Wraps the {@link ValueOptions} item source plus
     * {@link #itemLabelGenerator} into a single (filter, limit) → labels
     * function. Non-{@code null} whenever {@code fieldValueOptions} has been
     * called for this field.
     */
    BiFunction<String, Integer, List<String>> valueOptionsQuery;
    /**
     * Items the controller has seen for this registration, keyed by their
     * LLM-facing label. {@link FormValueConverter} resolves a chosen label at
     * fill time via {@link Map#get(Object)} — O(1) — and the first put per
     * label wins. Populated upfront for fixed-options registrations and as each
     * query-callback batch arrives for query-mode registrations. Iteration
     * order is insertion order. Reset at each
     * {@link FormAIController#onRequest()} turn boundary; non-{@code null}
     * whenever {@link #valueOptionsQuery} is set.
     */
    Map<String, Object> valueOptionsItems;
    /**
     * Item-to-label function used to render the field's current value and to
     * compute the keys in {@link #valueOptionsItems}. Captured at each
     * {@link FormAIController#onRequest()} from the explicit
     * {@link ValueOptions#itemLabelGenerator(com.vaadin.flow.component.ItemLabelGenerator)}
     * if set, otherwise from the field's own {@code getItemLabelGenerator()}
     * (read reflectively), otherwise {@link String#valueOf(Object)}. Stable
     * within a turn so {@link #valueOptionsItems}' keys remain valid for
     * lookup. Non-{@code null} whenever {@link #valueOptionsQuery} is set.
     */
    Function<Object, String> itemLabelGenerator;
    /**
     * {@code true} for fixed-options registrations, {@code false} for
     * query-callback or no-value-options registrations. Drives the {@code enum}
     * vs {@code queryable} choice in {@link FormFieldSchema}.
     */
    boolean fixedOptions;
    /**
     * Rebuilds {@link #valueOptionsItems}, {@link #valueOptionsQuery}, and
     * {@link #itemLabelGenerator} from the registration's captured config
     * (fixed list or query callback, explicit labeler or field reference).
     * Invoked once at registration so the schema works before the first turn,
     * and again at each {@link FormAIController#onRequest()} so a labeler
     * change on the field between turns is picked up and the query-mode map
     * starts each turn empty. {@code null} when no value-options registration
     * exists for this field.
     */
    Runnable valueOptionsTurnSetup;
    boolean ignored;
}
