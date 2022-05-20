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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

import com.vaadin.flow.function.SerializableFunction;

/**
 * Number Field sports many of the same features as Text Field but only accepts
 * numeric input. The input can be decimal, integral or big decimal. You can
 * specify a unit as a prefix or suffix for the field.
 *
 * @author Vaadin Ltd.
 */
public class NumberField extends AbstractNumberField<NumberField, Double> {

    /**
     * Constructs an empty {@code NumberField}.
     */
    public NumberField() {
        this(new Formatter(), true);
    }

    /**
     * Constructs an empty {@code NumberField} with the given label.
     *
     * @param label
     *            the text to set as the label
     */
    public NumberField(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs an empty {@code NumberField} with the given label and
     * placeholder text.
     *
     * @param label
     *            the text to set as the label
     * @param placeholder
     *            the placeholder text to set
     */
    public NumberField(String label, String placeholder) {
        this(label);
        setPlaceholder(placeholder);
    }

    /**
     * Constructs an empty {@code NumberField} with a value change listener.
     *
     * @param listener
     *            the value change listener
     *
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public NumberField(
            ValueChangeListener<? super ComponentValueChangeEvent<NumberField, Double>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code NumberField} with a value change listener and
     * a label.
     *
     * @param label
     *            the text to set as the label
     * @param listener
     *            the value change listener
     *
     * @see #setLabel(String)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public NumberField(String label,
            ValueChangeListener<? super ComponentValueChangeEvent<NumberField, Double>> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a {@code NumberField} with a value change listener, a label
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
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public NumberField(String label, Double initialValue,
            ValueChangeListener<? super ComponentValueChangeEvent<NumberField, Double>> listener) {
        this(label);
        setValue(initialValue);
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code NumberField}.
     * <p>
     * If {@code isInitialValueOptional} is {@code true} then the initial value
     * is used only if element has no {@code "value"} property value, otherwise
     * element {@code "value"} property is ignored and the initial value is set.
     *
     * @param formatter
     *            Formatter for the field.
     * @param isInitialValueOptional
     *            if {@code isInitialValueOptional} is {@code true} then the
     *            initial value is used only if element has no {@code "value"}
     *            property value, otherwise element {@code "value"} property is
     *            ignored and the initial value is set
     */
    private NumberField(Formatter formatter, boolean isInitialValueOptional) {
        super(formatter::parse, formatter, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, isInitialValueOptional);
    }

    @Override
    public void setMin(double min) {
        super.setMin(min);
    }

    /**
     * The minimum value of the field.
     *
     * @return the {@code min} property from the webcomponent
     */
    public double getMin() {
        return getMinDouble();
    }

    @Override
    public void setMax(double max) {
        super.setMax(max);
    }

    /**
     * The maximum value of the field.
     *
     * @return the {@code max} property from the webcomponent
     */
    public double getMax() {
        return getMaxDouble();
    }

    /**
     * Sets the allowed number intervals of the field. This specifies how much
     * the value will be increased/decreased. It is also used to invalidate the
     * field, if the value doesn't align with the specified step and
     * {@link #setMin(double) min} (if specified by user).
     *
     * @param step
     *            the new step to set
     * @throws IllegalArgumentException
     *             if the argument is less or equal to zero.
     */
    @Override
    public void setStep(double step) {
        if (step <= 0) {
            throw new IllegalArgumentException(
                    "The step cannot be less or equal to zero.");
        }
        super.setStep(step);
    }

    /**
     * Specifies the allowed number intervals of the field.
     *
     * @return the {@code step} property from the webcomponent
     */
    public double getStep() {
        return getStepDouble();
    }

    /**
     * Maximum number of characters (in Unicode code points) that the user can
     * enter.
     *
     * @param maxLength
     *            the maximum length
     *
     * @deprecated Not supported by NumberField (as it's built on
     *             {@code <input type="number">} in HTML). You can set numeric
     *             value constraints with {@link #setMin(double)},
     *             {@link #setMax(double)} and {@link #setStep(double)}.
     */
    @Deprecated
    public void setMaxLength(int maxLength) {
        super.setMaxlength(maxLength);
    }

    /**
     * Maximum number of characters (in Unicode code points) that the user can
     * enter.
     *
     * @return the {@code maxlength} property from the webcomponent
     *
     * @deprecated Not supported by NumberField (as it's built on
     *             {@code <input type="number">} in HTML). You can set numeric
     *             value constraints with {@link #setMin(double)},
     *             {@link #setMax(double)} and {@link #setStep(double)}.
     */
    @Deprecated
    public int getMaxLength() {
        return (int) getMaxlengthDouble();
    }

    /**
     * Minimum number of characters (in Unicode code points) that the user can
     * enter.
     *
     * @param minLength
     *            the minimum length
     *
     * @deprecated Not supported by NumberField (as it's built on
     *             {@code <input type="number">} in HTML). You can set numeric
     *             value constraints with {@link #setMin(double)},
     *             {@link #setMax(double)} and {@link #setStep(double)}.
     */
    @Deprecated
    public void setMinLength(int minLength) {
        super.setMinlength(minLength);
    }

    /**
     * Minimum number of characters (in Unicode code points) that the user can
     * enter.
     *
     * @return the {@code minlength} property from the webcomponent
     *
     * @deprecated Not supported by NumberField (as it's built on
     *             {@code <input type="number">} in HTML). You can set numeric
     *             value constraints with {@link #setMin(double)},
     *             {@link #setMax(double)} and {@link #setStep(double)}.
     */
    @Deprecated
    public int getMinLength() {
        return (int) getMinlengthDouble();
    }

    /**
     * When set to <code>true</code>, user is prevented from typing a value that
     * conflicts with the given {@code pattern}.
     *
     * @return the {@code preventInvalidInput} property from the webcomponent
     *
     * @deprecated Not supported by NumberField (as it's built on
     *             {@code <input type="number">} in HTML). You can set numeric
     *             value constraints with {@link #setMin(double)},
     *             {@link #setMax(double)} and {@link #setStep(double)}. For
     *             setting a custom value pattern and preventing invalid input,
     *             use the TextField component instead.
     */
    @Deprecated
    public boolean isPreventInvalidInput() {
        return isPreventInvalidInputBoolean();
    }

    /**
     * @deprecated Not supported by NumberField (as it's built on
     *             {@code <input type="number">} in HTML). You can set numeric
     *             value constraints with {@link #setMin(double)},
     *             {@link #setMax(double)} and {@link #setStep(double)}. For
     *             setting a custom value pattern and preventing invalid input,
     *             use the TextField component instead.
     */
    @Override
    @Deprecated
    public void setPreventInvalidInput(boolean preventInvalidInput) {
        super.setPreventInvalidInput(preventInvalidInput);
    }

    /**
     * @deprecated Not supported by NumberField (as it's built on
     *             {@code <input type="number">} in HTML). You can set numeric
     *             value constraints with {@link #setMin(double)},
     *             {@link #setMax(double)} and {@link #setStep(double)}. For
     *             setting a custom value pattern, use the TextField component
     *             instead.
     */
    @Override
    @Deprecated
    public void setPattern(String pattern) {
        super.setPattern(pattern);
    }

    /**
     * A regular expression that the value is checked against. The pattern must
     * match the entire value, not just some subset.
     *
     * @return the {@code pattern} property from the webcomponent
     *
     * @deprecated Not supported by NumberField (as it's built on
     *             {@code <input type="number">} in HTML). You can set numeric
     *             value constraints with {@link #setMin(double)},
     *             {@link #setMax(double)} and {@link #setStep(double)}. For
     *             setting a custom value pattern, use the TextField component
     *             instead.
     */
    @Deprecated
    public String getPattern() {
        return getPatternString();
    }

    private static class Formatter
            implements SerializableFunction<Double, String> {

        // Using Locale.ENGLISH to keep format independent of JVM locale
        // settings. The value property always uses period as the decimal
        // separator regardless of the browser locale.
        private final DecimalFormat decimalFormat = new DecimalFormat("#.#",
                DecimalFormatSymbols.getInstance(Locale.ENGLISH));

        private Formatter() {
            decimalFormat.setMaximumFractionDigits(Integer.MAX_VALUE);
        }

        @Override
        public String apply(Double valueFromModel) {
            return valueFromModel == null ? ""
                    : decimalFormat.format(valueFromModel.doubleValue());
        }

        private Double parse(String valueFromClient) {
            try {
                return valueFromClient == null || valueFromClient.isEmpty()
                        ? null
                        : decimalFormat.parse(valueFromClient).doubleValue();
            } catch (ParseException e) {
                throw new NumberFormatException(valueFromClient);
            }
        }
    }
}
