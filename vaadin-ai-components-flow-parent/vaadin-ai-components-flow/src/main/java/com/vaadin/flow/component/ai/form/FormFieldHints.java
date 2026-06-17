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
 * field's opaque id.
 * <p>
 * Set by {@link FormAIController#fieldValueOptions(ValueOptions)
 * controller.fieldValueOptions(...)}: {@link #valueOptionsQuery} carries the
 * filter callback (or a fixed-options snapshot wrapped in one),
 * {@link #fixedOptions} flags whether the schema should render the options as
 * {@code enum} or {@code queryable}, and {@link #valueOptionsToValue} resolves
 * one label to one element. For multi-select fields the controller wraps the
 * resolved elements into a {@link java.util.LinkedHashSet} before
 * {@code setValue}; the hint state is the same shape in both cases.
 *
 * @author Vaadin Ltd
 */
final class FormFieldHints {

    String description;
    BiFunction<String, Integer, List<String>> valueOptionsQuery;
    Function<String, ?> valueOptionsToValue;
    /**
     * {@code true} when the field was registered with the fixed-options
     * variant; {@code false} when registered with a query callback or with no
     * value-options hint at all. Used by {@link FormFieldSchema} to choose
     * {@code enum} vs {@code queryable} in the {@code get_form_state} JSON.
     */
    boolean fixedOptions;
    boolean ignored;
}
