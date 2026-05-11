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

import com.vaadin.flow.component.HasValue;

/**
 * Per-field mutable state collected by {@link FormAIController} during
 * discovery and hint registration. Holds everything the controller and the
 * tool factory need at request time: the field reference, its identifier,
 * labels, type, options, resolvers, and the previous value used to revert
 * rejected writes.
 */
final class FormFieldEntry {

    final HasValue<?, ?> field;
    final FormFieldType type;

    String identifier;
    String label;
    String helperText;
    String description;
    List<Object> allowedValues;
    BiFunction<String, Integer, List<Object>> queryable;
    Function<String, Object> itemResolver;
    boolean ignored;

    Object previousValue;
    boolean previousValueCaptured;

    FormFieldEntry(HasValue<?, ?> field, String identifier, String label,
            FormFieldType type) {
        this.field = field;
        this.identifier = identifier;
        this.label = label;
        this.type = type;
    }

    String describe() {
        if (field instanceof com.vaadin.flow.component.HasLabel hl
                && hl.getLabel() != null) {
            return field.getClass().getSimpleName() + " label=\"" + hl.getLabel()
                    + "\"";
        }
        return field.getClass().getSimpleName();
    }
}
