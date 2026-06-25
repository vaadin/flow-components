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
     * Items the controller has seen for this registration: the fixed list for
     * {@link ValueOptions#options(java.util.Collection)}, or items accumulated
     * from {@link ValueOptions#options(java.util.function.BiFunction)} batches.
     * {@link FormValueConverter} walks this list at fill time, applies
     * {@link #itemLabelGenerator} per item, and returns the first whose label
     * matches the LLM-supplied one (insertion order — first-wins on
     * duplicates). Non-{@code null} whenever {@link #valueOptionsQuery} is set;
     * empty until the query callback runs for query-mode registrations.
     */
    List<Object> valueOptionsItems;
    /**
     * Item-to-label function used to render the field's current value and to
     * resolve LLM-supplied labels back to items via {@link #valueOptionsItems}.
     * Resolved at registration to the explicit
     * {@link ValueOptions#itemLabelGenerator(com.vaadin.flow.component.ItemLabelGenerator)}
     * or to a delegate that defers to {@link FormValueConverter#renderItem}
     * (field's own {@code getItemLabelGenerator()}, then
     * {@link String#valueOf(Object)}). Non-{@code null} whenever
     * {@link #valueOptionsQuery} is set.
     */
    Function<Object, String> itemLabelGenerator;
    /**
     * {@code true} for fixed-options registrations, {@code false} for
     * query-callback or no-value-options registrations. Drives the {@code enum}
     * vs {@code queryable} choice in {@link FormFieldSchema}.
     */
    boolean fixedOptions;
    boolean ignored;
}
