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
import com.vaadin.flow.data.selection.MultiSelect;

/**
 * Per-field registration of the labels the LLM may pick from for a given field.
 * The label set is either fixed ({@link #options(Collection)}) or supplied on
 * demand by a callback ({@link #options(BiFunction)}); exactly one of the two
 * must be set, with the last call winning. Pass the configured registration to
 * {@link FormAIController#fieldValueOptions(ValueOptions)
 * controller.fieldValueOptions(...)} to apply it; the labels are then presented
 * to the LLM as the field's choices.
 * <p>
 * Labels are always {@link String} values, regardless of the field's value
 * type. For a non-{@link String} field, supply a label-to-value converter
 * through {@link FormAIController#fieldValueOptions(ValueOptions, Function)
 * fieldValueOptions(config, toValue)} — the converter resolves a chosen label
 * to the field's value type before the controller writes it.
 *
 * @param <I>
 *            the per-label item type the converter must produce — the field's
 *            value type for single-value fields, the per-element type for
 *            multi-select fields
 *
 * @author Vaadin Ltd
 */
public final class ValueOptions<I> {

    private final HasValue<?, ?> field;
    private final boolean multi;
    private BiFunction<String, Integer, List<String>> query;
    private List<String> fixedOptions;

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
     * Sets a fixed label list. A defensive copy is taken so later mutations of
     * the caller's collection have no effect. Use this when the option set is
     * known up front and small enough to enumerate; for large or dynamic sets
     * use {@link #options(BiFunction)} instead. Mutually exclusive with
     * {@link #options(BiFunction)} — calling either clears the other.
     *
     * @param options
     *            the labels the LLM may pick from, not {@code null} and not
     *            empty
     * @return this registration, for chaining
     * @throws IllegalArgumentException
     *             if {@code options} is empty — registering an empty fixed list
     *             leaves the field un-fillable and is always a developer
     *             mistake
     */
    public ValueOptions<I> options(Collection<String> options) {
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
     * the labels the LLM may pick from; labels are always {@link String}
     * values, regardless of the field's value type. Mutually exclusive with
     * {@link #options(Collection)} — calling either clears the other.
     *
     * @param query
     *            invoked with two arguments: a filter string the LLM picked,
     *            and a positive limit on how many labels to return. Returns the
     *            matching labels in display order, not {@code null} (an empty
     *            list signals "no matches" to the LLM)
     * @return this registration, for chaining
     */
    public ValueOptions<I> options(
            BiFunction<String, Integer, List<String>> query) {
        Objects.requireNonNull(query, "Options query must not be null");
        this.query = query;
        this.fixedOptions = null;
        return this;
    }

    HasValue<?, ?> field() {
        return field;
    }

    boolean isMulti() {
        return multi;
    }

    BiFunction<String, Integer, List<String>> query() {
        return query;
    }

    List<String> fixedOptions() {
        return fixedOptions;
    }
}
