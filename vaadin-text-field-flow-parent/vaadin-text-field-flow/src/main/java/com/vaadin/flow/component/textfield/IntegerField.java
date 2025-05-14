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

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.SerializableFunction;

/**
 * IntegerField is an extension of Text Field that only accepts integer numbers.
 *
 * <h2>Validation</h2>
 * <p>
 * Integer Field comes with a built-in validation mechanism based on
 * constraints. Validation is triggered whenever the user applies an input
 * change, for example by pressing Enter or blurring the field. Programmatic
 * value changes trigger validation as well. In eager and lazy value change
 * modes, validation is also triggered on every character press with a delay
 * according to the selected mode.
 * <p>
 * Validation verifies that the value is parsable into {@link Integer} and
 * satisfies the specified constraints. If validation fails, the component is
 * marked as invalid and an error message is displayed below the input.
 * <p>
 * The following constraints are supported:
 * <ul>
 * <li>{@link #setRequiredIndicatorVisible(boolean)}
 * <li>{@link #setMin(int)}
 * <li>{@link #setMax(int)}
 * <li>{@link #setStep(int)}
 * </ul>
 * <p>
 * Error messages for unparsable input and constraints can be configured with
 * the {@link IntegerFieldI18n} object, using the respective properties. If you
 * want to provide a single catch-all error message, you can also use the
 * {@link #setErrorMessage(String)} method. Note that such an error message will
 * take priority over i18n error messages if both are set.
 * <p>
 * For more advanced validation that requires custom rules, you can use
 * {@link Binder}. By default, before running custom validators, Binder will
 * also check if the value is parsable and satisfies the component constraints,
 * displaying error messages from the {@link IntegerFieldI18n} object. The
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
@Tag("vaadin-integer-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/integer-field", version = "24.8.0-alpha18")
@JsModule("@vaadin/integer-field/src/vaadin-integer-field.js")
public class IntegerField extends AbstractNumberField<IntegerField, Integer>
        implements HasThemeVariant<TextFieldVariant> {

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
        super(PARSER, FORMATTER, Integer.MIN_VALUE, Integer.MAX_VALUE);
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
     * {@inheritDoc}
     * <p>
     * Distinct error messages for unparsable input and different constraints
     * can be configured with the {@link IntegerFieldI18n} object, using the
     * respective properties. However, note that the error message set with
     * {@link #setErrorMessage(String)} will take priority and override any i18n
     * error messages if both are set.
     */
    @Override
    public void setErrorMessage(String errorMessage) {
        super.setErrorMessage(errorMessage);
    }

    /**
     * @see IntegerFieldI18n#setRequiredErrorMessage(String)
     */
    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * Sets the minimum value for the field. This will configure the field to
     * invalidate if the entered value is below the minimum. It will also limit
     * the decrementing of the value when step buttons are enabled.
     * <p>
     * The minimum value is inclusive.
     *
     * @param min
     *            the minimum integer value to set
     * @see IntegerFieldI18n#setMinErrorMessage(String)
     */
    public void setMin(int min) {
        super.setMin(min);
    }

    /**
     * Gets the minimum value for this field. The constraint activates only if
     * the value was explicitly set with {@link #setMin(int)}.
     *
     * @return the minimum integer value
     * @see #setMin(int)
     */
    public int getMin() {
        return (int) getMinDouble();
    }

    /**
     * Sets the maximum value for the field. This will configure the field to
     * invalidate if the entered value is above the maximum. It will also limit
     * the incrementing of the value when step buttons are enabled.
     * <p>
     * The maximum value is inclusive.
     *
     * @param max
     *            the maximum integer value to set
     * @see IntegerFieldI18n#setMaxErrorMessage(String)
     */
    public void setMax(int max) {
        super.setMax(max);
    }

    /**
     * Gets the maximum value for the field. The constraint activates only if
     * the value was explicitly set with {@link #setMax(int)}.
     *
     * @return the maximum integer value
     * @see #setMax(int)
     */
    public int getMax() {
        return (int) getMaxDouble();
    }

    /**
     * Sets the allowed number intervals for this field. This specifies how much
     * the value will be increased/decreased when clicking on the
     * {@link #setStepButtonsVisible(boolean) step buttons}. It is also used to
     * invalidate the field, if the value doesn't align with the specified step
     * and {@link #setMin(int) min} (if explicitly specified by developer).
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
     * Gets the allowed number intervals for this field. The constraint
     * activates only if the step was explicitly set with {@link #setStep(int)}.
     *
     * @return the step property of the field
     * @see #setStep(int)
     */
    public int getStep() {
        return (int) getStepDouble();
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(IntegerFieldI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    @Override
    public IntegerFieldI18n getI18n() {
        return (IntegerFieldI18n) super.getI18n();
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(IntegerFieldI18n i18n) {
        super.setI18n(i18n);
    }

    /**
     * The internationalization properties for {@link IntegerField}.
     */
    public static class IntegerFieldI18n implements AbstractNumberFieldI18n {
        private String requiredErrorMessage;
        private String badInputErrorMessage;
        private String minErrorMessage;
        private String maxErrorMessage;
        private String stepErrorMessage;

        /**
         * Gets the error message displayed when the field contains user input
         * that the server is unable to convert to type {@link Integer}.
         *
         * @return the error message or {@code null} if not set
         */
        @Override
        public String getBadInputErrorMessage() {
            return badInputErrorMessage;
        }

        /**
         * Sets the error message to display when the field contains user input
         * that the server is unable to convert to type {@link Integer}.
         * <p>
         * Note, custom error messages set with
         * {@link IntegerField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         */
        public IntegerFieldI18n setBadInputErrorMessage(String errorMessage) {
            badInputErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see IntegerField#isRequired()
         * @see IntegerField#setRequired(boolean)
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
         * {@link IntegerField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         * @see IntegerField#isRequired()
         * @see IntegerField#setRequired(boolean)
         */
        public IntegerFieldI18n setRequiredErrorMessage(String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value is smaller than
         * the minimum allowed value.
         *
         * @return the error message or {@code null} if not set
         * @see IntegerField#setMin(int)
         * @see IntegerField#getMin()
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
         * {@link IntegerField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         * @see IntegerField#setMin(int)
         * @see IntegerField#getMin()
         */
        public IntegerFieldI18n setMinErrorMessage(String errorMessage) {
            minErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value is greater than
         * the maximum allowed value.
         *
         * @return the error message or {@code null} if not set
         * @see IntegerField#setMax(int)
         * @see IntegerField#getMax()
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
         * {@link IntegerField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         * @see IntegerField#setMax(int)
         * @see IntegerField#getMax()
         */
        public IntegerFieldI18n setMaxErrorMessage(String errorMessage) {
            maxErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value is not a
         * multiple of the step value.
         *
         * @return the error message or {@code null} if not set
         * @see IntegerField#setStep(int)
         * @see IntegerField#getStep()
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
         * {@link IntegerField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         * @see IntegerField#setStep(int)
         * @see IntegerField#getStep()
         */
        public IntegerFieldI18n setStepErrorMessage(String errorMessage) {
            stepErrorMessage = errorMessage;
            return this;
        }
    }
}
