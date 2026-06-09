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
 * Per-field options registration passed to
 * {@link FormAIController#valueOptions(ValueOptions)
 * controller.valueOptions(...)}. Carries the field together with either a fixed
 * label list or a query callback. The label-to-value converter is supplied
 * separately to the controller at registration time, not on this object — see
 * {@link FormAIController#valueOptions(ValueOptions, Function)
 * valueOptions(config, toValue)}.
 * <p>
 * Two {@code forField} factories cover all field shapes:
 * <ul>
 * <li>{@link #forField(HasValue)} — single-value fields. The type parameter
 * {@code V} of the field flows through.</li>
 * <li>{@link #forField(MultiSelect)} — multi-select fields. Picked by the
 * compiler whenever the reference is statically typed as {@link MultiSelect}.
 * The per-element type flows through; the controller aggregates resolved
 * per-label items into a {@link LinkedHashSet} before
 * {@link HasValue#setValue}.</li>
 * </ul>
 * Exactly one of {@link #options(Collection)} (fixed labels) and
 * {@link #options(BiFunction)} (queryable labels) must be set; calling one
 * clears the other so the last setter wins.
 *
 * @param <I>
 *            the per-label item type the controller's converter must produce.
 *            For a single-value field {@code I} is the field's value type; for
 *            a multi-select field {@code I} is the per-element type. The
 *            controller's {@code valueOptions(...)} overload set enforces at
 *            compile time that a converter is supplied for every {@code I}
 *            other than {@link String}
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
     * Starts a single-value options registration. The field's value type
     * {@code V} flows through; for a {@code V} other than {@link String}, the
     * controller's two-argument
     * {@link FormAIController#valueOptions(ValueOptions, Function)} overload
     * must be used to supply a converter — that is a compile-time requirement,
     * not a runtime check.
     *
     * @param field
     *            the single-value field whose options the LLM may pick from,
     *            not {@code null}
     * @param <V>
     *            the field's value type
     * @return a fresh registration ready to receive {@link #options(Collection)
     *         options(...)}
     */
    public static <V> ValueOptions<V> forField(HasValue<?, V> field) {
        Objects.requireNonNull(field, "Field must not be null");
        return new ValueOptions<>(field, false);
    }

    /**
     * Starts a multi-select options registration. Picked by the compiler over
     * {@link #forField(HasValue)} whenever the reference is statically typed as
     * {@link MultiSelect}. The per-element type flows through; for any type
     * other than {@link String}, the controller's two-argument
     * {@link FormAIController#valueOptions(ValueOptions, Function)} overload
     * must be used. The controller aggregates resolved per-label items into a
     * {@link LinkedHashSet} before {@link HasValue#setValue}.
     *
     * @param field
     *            the multi-select field whose options the LLM may pick from,
     *            not {@code null}
     * @param <T>
     *            the per-element type
     * @param <C>
     *            the field's source-component type
     * @return a fresh registration ready to receive {@link #options(Collection)
     *         options(...)}
     */
    public static <T, C extends Component> ValueOptions<T> forField(
            MultiSelect<C, T> field) {
        Objects.requireNonNull(field, "Field must not be null");
        return new ValueOptions<>(field, true);
    }

    /**
     * Sets a fixed label list. A defensive copy is taken so later mutations of
     * the caller's collection have no effect. Mutually exclusive with
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
     * Sets a queryable label callback the LLM drives via
     * {@code query_field_options}. Mutually exclusive with
     * {@link #options(Collection)} — calling either clears the other.
     *
     * @param query
     *            the filter callback returning labels for the LLM, not
     *            {@code null}
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
