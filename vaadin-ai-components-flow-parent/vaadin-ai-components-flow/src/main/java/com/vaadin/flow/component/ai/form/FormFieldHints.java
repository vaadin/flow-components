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
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Mutable per-field hint state held by {@link FormAIController}, keyed by the
 * field's opaque id.
 *
 * @author Vaadin Ltd
 */
final class FormFieldHints {

    String description;
    BiFunction<String, Integer, List<String>> valueOptionsQuery;
    Function<String, ?> valueOptionsToValue;
    /**
     * Snapshot of the labels passed to the fixed-collection
     * {@code valueOptions} overloads. {@code null} when the field was
     * registered with a query callback (queryable) or has no value-options hint
     * at all. Used by {@link FormFieldSchema} to choose {@code enum} vs
     * {@code queryable} in the {@code get_form_state} JSON.
     */
    Collection<String> fixedOptions;
    boolean ignored;
}
