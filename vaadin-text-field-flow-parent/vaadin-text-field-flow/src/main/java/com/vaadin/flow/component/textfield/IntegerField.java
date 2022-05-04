/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableFunction;

/**
 * IntegerField is an extension of Text Field that only accepts integer numbers.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-integer-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/integer-field", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-text-field", version = "23.1.0-beta1")
@JsModule("@vaadin/integer-field/src/vaadin-integer-field.js")
public class IntegerField extends AbstractNumberField<IntegerField, Integer> {

    private static final SerializableFunction<String, Integer> PARSER = valueFormClient -> {
        if (valueFormClient == null || valueFormClient.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(valueFormClient);
        } catch (NumberFormatException e) {
            return null;
        }
    };

    private static final SerializableFunction<Integer, String> FORMATTER = valueFromModel -> valueFromModel == null
            ? ""
            : valueFromModel.toString();

    /**
     * Constructs an empty {@code IntegerField}.
     */
    public IntegerField() {
        super(PARSER, FORMATTER, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
    }

    /**
     * Constructs an empty {@code IntegerField} with the given label.
     *
     * @param label
     *            the text to set as the label
     */
    public IntegerField(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs an empty {@code IntegerField} with the given label and
     * placeholder text.
     *
     * @param label
     *            the text to set as the label
     * @param placeholder
     *            the placeholder text to set
     */
    public IntegerField(String label, String placeholder) {
        this(label);
        setPlaceholder(placeholder);
    }

    /**
     * Constructs an empty {@code IntegerField} with a value change listener.
     *
     * @param listener
     *            the value change listener
     *
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public IntegerField(
            ValueChangeListener<? super ComponentValueChangeEvent<IntegerField, Integer>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code IntegerField} with a value change listener and
     * a label.
     *
     * @param label
     *            the text to set as the label
     * @param listener
     *            the value change listener
     *
     * @see #setLabel(String)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public IntegerField(String label,
            ValueChangeListener<? super ComponentValueChangeEvent<IntegerField, Integer>> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a {@code IntegerField} with a value change listener, a label
     * and an initial value.
     *
     * @param label
     *            the text to set as the label
     * @param initialValue
     *            the initial value
     * @param listener
     *            the value change listener
     *
     * @see #setLabel(String)
     * @see #setValue(Object)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public IntegerField(String label, Integer initialValue,
            ValueChangeListener<? super ComponentValueChangeEvent<IntegerField, Integer>> listener) {
        this(label);
        setValue(initialValue);
        addValueChangeListener(listener);
    }

    /**
     * Sets the minimum value of the field. Entering a value which is smaller
     * than {@code min} invalidates the field.
     *
     * @param min
     *            the min value to set
     */
    public void setMin(int min) {
        super.setMin(min);
    }

    /**
     * Gets the minimum allowed value of the field.
     *
     * @return the min property of the field
     * @see #setMin(int)
     */
    public int getMin() {
        return (int) getMinDouble();
    }

    /**
     * Sets the maximum value of the field. Entering a value which is greater
     * than {@code max} invalidates the field.
     *
     * @param max
     *            the max value to set
     */
    public void setMax(int max) {
        super.setMax(max);
    }

    /**
     * Gets the maximum allowed value of the field.
     *
     * @return the max property of the field
     * @see #setMax(int)
     */
    public int getMax() {
        return (int) getMaxDouble();
    }

    /**
     * Sets the allowed number intervals of the field. This specifies how much
     * the value will be increased/decreased when clicking on the
     * {@link #setHasControls(boolean) control buttons}. It is also used to
     * invalidate the field, if the value doesn't align with the specified step
     * and {@link #setMin(int) min} (if specified by user).
     *
     * @param step
     *            the new step to set
     * @throws IllegalArgumentException
     *             if the argument is less or equal to zero.
     */
    public void setStep(int step) {
        if (step <= 0) {
            throw new IllegalArgumentException(
                    "The step cannot be less or equal to zero.");
        }
        super.setStep(step);
    }

    /**
     * Gets the allowed number intervals of the field.
     *
     * @return the step property of the field
     * @see #setStep(int)
     */
    public int getStep() {
        return (int) getStepDouble();
    }
}
