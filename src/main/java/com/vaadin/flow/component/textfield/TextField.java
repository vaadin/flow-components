/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Server-side component for the {@code vaadin-text-field} element.
 *
 * @author Vaadin Ltd
 */
public class TextField extends GeneratedVaadinTextField<TextField>
        implements HasSize, HasValidation, HasValueChangeMode<TextField, String> {
    private ValueChangeMode currentMode;

    /**
     * Constructs an empty {@code TextField}.
     */
    public TextField() {
        setValueChangeMode(ValueChangeMode.ON_BLUR);
    }

    /**
     * Constructs an empty {@code TextField} with the given label.
     *
     * @param label
     *            the text to set as the label
     */
    public TextField(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs an empty {@code TextField} with the given label and
     * placeholder text.
     *
     * @param label
     *            the text to set as the label
     * @param placeholder
     *            the placeholder text to set
     */
    public TextField(String label, String placeholder) {
        this(label);
        setPlaceholder(placeholder);
    }

    /**
     * Constructs a {@code TextField} with the given label, an initial value and
     * placeholder text.
     *
     * @param label
     *            the text to set as the label
     * @param initialValue
     *            the initial value
     * @param placeholder
     *            the placeholder text to set
     *
     * @see #setValue(String)
     * @see #setPlaceholder(String)
     */
    public TextField(String label, String initialValue, String placeholder) {
        this(label);
        setValue(initialValue);
        setPlaceholder(placeholder);
    }

    /**
     * Constructs an empty {@code TextField} with a value change listener.
     *
     * @param listener
     *            the value change listener
     *
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public TextField(ValueChangeListener<TextField, String> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code TextField} with a label and a value change
     * listener.
     *
     * @param label
     *            the text to set as the label
     * @param listener
     *            the value change listener
     * @see #setLabel(String)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public TextField(String label,
            ValueChangeListener<TextField, String> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code TextField} with a label,a value change
     * listener and an initial value.
     *
     * @param label
     *            the text to set as the label
     * @param initialValue
     *            the initial value
     * @param listener
     *            the value change listener
     *
     * @see #setLabel(String)
     * @see #setValue(String)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public TextField(String label, String initialValue,
            ValueChangeListener<TextField, String> listener) {
        this(label);
        setValue(initialValue);
        addValueChangeListener(listener);
    }

    @Override
    public ValueChangeMode getValueChangeMode() {
        return currentMode;
    }

    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {
        currentMode = valueChangeMode;
        HasValueChangeMode.super.setValueChangeMode(valueChangeMode);
    }
}
