/*
 * Copyright 2000-2025 Vaadin Ltd.
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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.SerializableFunction;

/**
 * Number Field sports many of the same features as Text Field but only accepts
 * numeric input. The input can be decimal, integral or big decimal. You can
 * specify a unit as a prefix or suffix for the field.
 * <h2>Validation</h2>
 * <p>
 * Number Field comes with a built-in validation mechanism based on constraints.
 * Validation is triggered whenever the user applies an input change, for
 * example by pressing Enter or blurring the field. Programmatic value changes
 * trigger validation as well. In eager and lazy value change modes, validation
 * is also triggered on every character press with a delay according to the
 * selected mode.
 * <p>
 * Validation verifies that the value is parsable into {@link Double} and
 * satisfies the specified constraints. If validation fails, the component is
 * marked as invalid and an error message is displayed below the input.
 * <p>
 * The following constraints are supported:
 * <ul>
 * <li>{@link #setRequiredIndicatorVisible(boolean)}
 * <li>{@link #setMin(double)}
 * <li>{@link #setMax(double)}
 * <li>{@link #setStep(double)}
 * </ul>
 * <p>
 * Error messages for unparsable input and constraints can be configured with
 * the {@link NumberFieldI18n} object, using the respective properties. If you
 * want to provide a single catch-all error message, you can also use the
 * {@link #setErrorMessage(String)} method. Note that such an error message will
 * take priority over i18n error messages if both are set.
 * <p>
 * For more advanced validation that requires custom rules, you can use
 * {@link Binder}. By default, before running custom validators, Binder will
 * also check if the value is parsable and satisfies the component constraints,
 * displaying error messages from the {@link NumberFieldI18n} object. The
 * exception is the required constraint, for which Binder provides its own API,
 * see {@link Binder.BindingBuilder#asRequired(String) asRequired()}.
 * <p>
 * However, if Binder doesn't fit your needs and you want to implement fully
 * custom validation logic, you can disable the constraint validation by setting
 * {@link #setManualValidation(boolean)} to true. This will allow you to control
 * the invalid state and the error message manually using
 * {@link #setInvalid(boolean)} and {@link #setErrorMessage(String)} API.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-number-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/number-field", version = "24.8.0-alpha18")
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

    /**
     * {@inheritDoc}
     * <p>
     * Distinct error messages for unparsable input and different constraints
     * can be configured with the {@link NumberFieldI18n} object, using the
     * respective properties. However, note that the error message set with
     * {@link #setErrorMessage(String)} will take priority and override any i18n
     * error messages if both are set.
     */
    @Override
    public void setErrorMessage(String errorMessage) {
        super.setErrorMessage(errorMessage);
    }

    /**
     * @see NumberFieldI18n#setRequiredErrorMessage(String)
     */
    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * Sets the minimum value for this field. This will configure the field to
     * invalidate if the entered value is below the minimum. It will also limit
     * the decrementing of the value when step buttons are enabled.
     * <p>
     * The minimum value is inclusive.
     *
     * @param min
     *            the minimum double value to set
     * @see NumberFieldI18n#setMinErrorMessage(String)
     */
    @Override
    public void setMin(double min) {
        super.setMin(min);
    }

    /**
     * Gets the minimum value for this field. The constraint activates only if
     * the value was explicitly set with {@link #setMin(int)}.
     *
     * @return the minimum double value
     * @see #setMin(double)
     */
    public double getMin() {
        return getMinDouble();
    }

    /**
     * Sets the maximum value for this field. This will configure the field to
     * invalidate if the entered value is above the maximum. It will also limit
     * the incrementing of the value when step buttons are enabled.
     * <p>
     * The maximum value is inclusive.
     *
     * @param max
     *            the maximum double value to set
     * @see NumberFieldI18n#setMaxErrorMessage(String)
     */
    @Override
    public void setMax(double max) {
        super.setMax(max);
    }

    /**
     * Gets the maximum value for this field. The constraint activates only if
     * the value was explicitly set with {@link #setMax(double)}.
     *
     * @return the maximum double value
     * @see #setMax(double)
     */
    public double getMax() {
        return getMaxDouble();
    }

    /**
     * Sets the allowed number intervals for this field. This specifies how much
     * the value will be increased/decreased. It is also used to invalidate the
     * field, if the value doesn't align with the specified step and
     * {@link #setMin(double) min} (if explicitly specified by the developer).
     *
     * @param step
     *            the new step to set
     * @throws IllegalArgumentException
     *             if the argument is less or equal to zero.
     * @see NumberFieldI18n#setStepErrorMessage(String)
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
     * Gets the allowed number intervals for this field.
     *
     * @return the {@code step} property from the webcomponent
     * @see #setStep(double)
     */
    public double getStep() {
        return getStepDouble();
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(NumberFieldI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    @Override
    public NumberFieldI18n getI18n() {
        return (NumberFieldI18n) super.getI18n();
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(NumberFieldI18n i18n) {
        super.setI18n(i18n);
    }

    /**
     * The internationalization properties for {@link NumberField}.
     */
    public static class NumberFieldI18n implements AbstractNumberFieldI18n {
        private String requiredErrorMessage;
        private String badInputErrorMessage;
        private String minErrorMessage;
        private String maxErrorMessage;
        private String stepErrorMessage;

        /**
         * Gets the error message displayed when the field contains user input
         * that the server is unable to convert to type {@link Number}.
         *
         * @return the error message or {@code null} if not set
         */
        @Override
        public String getBadInputErrorMessage() {
            return badInputErrorMessage;
        }

        /**
         * Sets the error message to display when the field contains user input
         * that the server is unable to convert to type {@link Number}.
         * <p>
         * Note, custom error messages set with
         * {@link NumberField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         */
        public NumberFieldI18n setBadInputErrorMessage(String errorMessage) {
            badInputErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see NumberField#isRequired()
         * @see NumberField#setRequired(boolean)
         */
        @Override
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link NumberField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         * @see NumberField#isRequired()
         * @see NumberField#setRequired(boolean)
         */
        public NumberFieldI18n setRequiredErrorMessage(String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value is smaller than
         * the minimum allowed value.
         *
         * @return the error message or {@code null} if not set
         * @see NumberField#setMin(double)
         * @see NumberField#getMin()
         */
        @Override
        public String getMinErrorMessage() {
            return minErrorMessage;
        }

        /**
         * Sets the error message to display when the field value is smaller
         * than the minimum allowed value.
         * <p>
         * Note, custom error messages set with
         * {@link NumberField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         * @see NumberField#setMin(double)
         * @see NumberField#getMin()
         */
        public NumberFieldI18n setMinErrorMessage(String errorMessage) {
            minErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value is greater than
         * the maximum allowed value.
         *
         * @return the error message or {@code null} if not set
         * @see NumberField#setMax(double)
         * @see NumberField#getMax()
         */
        @Override
        public String getMaxErrorMessage() {
            return maxErrorMessage;
        }

        /**
         * Sets the error message to display when the field value is greater
         * than the maximum allowed value.
         * <p>
         * Note, custom error messages set with
         * {@link NumberField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         * @see NumberField#setMax(double)
         * @see NumberField#getMax()
         */
        public NumberFieldI18n setMaxErrorMessage(String errorMessage) {
            maxErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value is not a
         * multiple of the step value.
         *
         * @return the error message or {@code null} if not set
         * @see NumberField#setStep(double)
         * @see NumberField#getStep()
         */
        @Override
        public String getStepErrorMessage() {
            return stepErrorMessage;
        }

        /**
         * Sets the error message to display when the field value is not a
         * multiple of the step value.
         * <p>
         * Note, custom error messages set with
         * {@link NumberField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         * @see NumberField#setStep(double)
         * @see NumberField#getStep()
         */
        public NumberFieldI18n setStepErrorMessage(String errorMessage) {
            stepErrorMessage = errorMessage;
            return this;
        }
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
