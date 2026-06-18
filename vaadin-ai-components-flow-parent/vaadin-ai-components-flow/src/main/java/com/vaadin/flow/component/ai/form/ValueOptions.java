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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.data.selection.MultiSelect;

/**
 * Per-field registration of the items the LLM may pick from for a given field.
 * The item set is either fixed ({@link #options(Collection)}) or supplied on
 * demand by a callback ({@link #options(BiFunction)}); exactly one of the two
 * must be set, with the last call winning. Pass the configured registration to
 * {@link FormAIController#fieldValueOptions(ValueOptions)
 * controller.fieldValueOptions(...)} to apply it; the items are then rendered
 * to labels and presented to the LLM as the field's choices.
 * <p>
 * Items carry the field's value type, so a {@code ComboBox<Project>}
 * registration passes {@code Project} items, not pre-rendered label strings.
 * The controller derives the LLM-facing label for each item through this chain:
 * an explicit {@link #itemLabelGenerator(ItemLabelGenerator)} if set, otherwise
 * the field's own {@code setItemLabelGenerator(...)} (read reflectively),
 * otherwise {@link String#valueOf(Object)} as a last resort. The label produced
 * by this chain is what the LLM sees, picks from, and sends back to the
 * {@code toValue} converter — so the field's UI label generator drives the AI
 * labels automatically when set.
 * <p>
 * For a non-{@link String} field, supply a label-to-value converter through
 * {@link FormAIController#fieldValueOptions(ValueOptions, Function)
 * fieldValueOptions(config, toValue)} — the converter resolves a chosen label
 * back to the field's value type before the controller writes it.
 *
 * @param <V>
 *            the item type — the field's value type for single-value fields,
 *            the per-element type for multi-select fields
 *
 * @author Vaadin Ltd
 */
public final class ValueOptions<V> {

    private final HasValue<?, ?> field;
    private final boolean multi;
    private BiFunction<String, Integer, List<V>> query;
    private List<V> fixedOptions;
    private ItemLabelGenerator<V> itemLabelGenerator;

    private ValueOptions(HasValue<?, ?> field, boolean multi) {
        this.field = field;
        this.multi = multi;
    }

    /**
     * Starts an options registration for a single-value field. The field's
     * value type {@code V} flows through; for any {@code V} other than
     * {@link String}, the controller's two-argument
     * {@link FormAIController#fieldValueOptions(ValueOptions, Function)}
     * overload must be used to supply a label-to-value converter (a
     * compile-time requirement, not a runtime check).
     *
     * @param field
     *            the single-value field whose options the LLM may pick from,
     *            not {@code null}
     * @param <V>
     *            the field's value type
     * @return a fresh registration ready to receive
     *         {@link #options(Collection)} or {@link #options(BiFunction)}
     */
    public static <V> ValueOptions<V> forField(HasValue<?, V> field) {
        Objects.requireNonNull(field, "Field must not be null");
        return new ValueOptions<>(field, false);
    }

    /**
     * Starts an options registration for a multi-select field. Picked by the
     * compiler over {@link #forField(HasValue)} whenever the reference is
     * statically typed as {@link MultiSelect}. The per-element type {@code T}
     * flows through; for any {@code T} other than {@link String}, the
     * controller's two-argument
     * {@link FormAIController#fieldValueOptions(ValueOptions, Function)}
     * overload must be used. The controller aggregates resolved per-label items
     * into a {@link LinkedHashSet} before {@link HasValue#setValue}.
     *
     * @param field
     *            the multi-select field whose options the LLM may pick from,
     *            not {@code null}
     * @param <T>
     *            the per-element type
     * @param <C>
     *            the field's source-component type
     * @return a fresh registration ready to receive
     *         {@link #options(Collection)} or {@link #options(BiFunction)}
     */
    public static <T, C extends Component> ValueOptions<T> forField(
            MultiSelect<C, T> field) {
        Objects.requireNonNull(field, "Field must not be null");
        return new ValueOptions<>(field, true);
    }

    /**
     * Sets a fixed item list. A defensive copy is taken so later mutations of
     * the caller's collection have no effect. Use this when the option set is
     * known up front and small enough to enumerate; for large or dynamic sets
     * use {@link #options(BiFunction)} instead. Mutually exclusive with
     * {@link #options(BiFunction)} — calling either clears the other.
     *
     * @param options
     *            the items the LLM may pick from, not {@code null} and not
     *            empty; each item is rendered to an LLM-facing label through
     *            the chain documented on the class JavaDoc
     * @return this registration, for chaining
     * @throws IllegalArgumentException
     *             if {@code options} is empty — registering an empty fixed list
     *             leaves the field un-fillable and is always a developer
     *             mistake
     */
    public ValueOptions<V> options(Collection<V> options) {
        Objects.requireNonNull(options, "Options must not be null");
        if (options.isEmpty()) {
            throw new IllegalArgumentException("Options must not be empty");
        }
        this.fixedOptions = List.copyOf(options);
        this.query = null;
        return this;
    }

    /**
     * Sets a callback the controller invokes whenever the LLM needs to see the
     * field's options. Use this when the option set is too large or too dynamic
     * for a fixed list via {@link #options(Collection)} — for example options
     * that come from a database query or a remote service. The callback returns
     * items the LLM may pick from; each item is rendered to an LLM-facing label
     * through the chain documented on the class JavaDoc. Mutually exclusive
     * with {@link #options(Collection)} — calling either clears the other.
     *
     * @param query
     *            invoked with two arguments: a filter string the LLM picked,
     *            and a positive limit on how many items to return. Returns the
     *            matching items in display order, not {@code null} (an empty
     *            list signals "no matches" to the LLM)
     * @return this registration, for chaining
     */
    public ValueOptions<V> options(BiFunction<String, Integer, List<V>> query) {
        Objects.requireNonNull(query, "Options query must not be null");
        this.query = query;
        this.fixedOptions = null;
        return this;
    }

    /**
     * Sets the item-to-label function the controller uses to derive the
     * LLM-facing label for each item. Optional — when unset, the controller
     * falls back to the field's own {@code getItemLabelGenerator()} (read
     * reflectively), then to {@link String#valueOf(Object)} as a last resort.
     * Set this when the field's UI label generator is absent or when the LLM
     * needs a different label from the UI (for example a code rather than a
     * display name). Calling this multiple times overwrites the previous value.
     * <p>
     * Two items rendering to the same label are not an error: the duplicate
     * appears verbatim in the LLM's option list, and when the LLM picks the
     * shared label the supplied {@code toValue} converter decides which item is
     * written. Emit unique labels per item if disambiguation matters.
     *
     * @param generator
     *            the per-item label generator, not {@code null}
     * @return this registration, for chaining
     */
    public ValueOptions<V> itemLabelGenerator(ItemLabelGenerator<V> generator) {
        Objects.requireNonNull(generator,
                "Item label generator must not be null");
        this.itemLabelGenerator = generator;
        return this;
    }

    HasValue<?, ?> field() {
        return field;
    }

    boolean isMulti() {
        return multi;
    }

    BiFunction<String, Integer, List<V>> query() {
        return query;
    }

    List<V> fixedOptions() {
        return fixedOptions;
    }

    ItemLabelGenerator<V> itemLabelGenerator() {
        return itemLabelGenerator;
    }
}
