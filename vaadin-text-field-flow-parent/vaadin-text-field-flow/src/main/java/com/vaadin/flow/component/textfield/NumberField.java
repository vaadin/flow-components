/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.function.SerializableFunction;

/**
 * Number Field sports many of the same features as Text Field but only accepts
 * numeric input. The input can be decimal, integral or big decimal. You can
 * specify a unit as a prefix or suffix for the field.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-number-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.1.12")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/number-field", version = "24.1.12")
@JsModule("@vaadin/number-field/src/vaadin-number-field.js")
public class NumberField extends AbstractNumberField<NumberField, Double>
        implements HasAllowedCharPattern, HasThemeVariant<TextFieldVariant> {

    /**
     * Constructs an empty {@code NumberField}.
     */
    public NumberField() {
        this(new Formatter());
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
     *
     * @param formatter
     *            Formatter for the field.
     */
    private NumberField(Formatter formatter) {
        super(formatter::parse, formatter, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);
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
