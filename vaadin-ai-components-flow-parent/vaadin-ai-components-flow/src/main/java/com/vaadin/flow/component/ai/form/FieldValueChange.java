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

import java.io.Serializable;

import com.vaadin.flow.component.HasValue;

/**
 * Captures the before / after values of a single field across one
 * {@link FormAIController} turn. Reported through
 * {@link FormAIController#addFieldValueChangedListener} for every field whose
 * value changed during the turn — either written by the LLM or by a cascade.
 *
 * @param field
 *            the field whose value changed
 * @param oldValue
 *            the field's value at the start of the turn, possibly {@code null}
 * @param newValue
 *            the field's value at the end of the turn, possibly {@code null}
 *
 * @author Vaadin Ltd
 * @since 25.2
 */
public record FieldValueChange(HasValue<?, ?> field, Object oldValue,
        Object newValue) implements Serializable {
}
