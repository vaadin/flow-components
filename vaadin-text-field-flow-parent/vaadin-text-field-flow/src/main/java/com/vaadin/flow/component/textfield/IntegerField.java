/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import com.vaadin.flow.function.SerializableFunction;

/**
 * IntegerField is an extension of Text Field that only accepts integer numbers.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-integer-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.5.0-alpha8")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/integer-field", version = "24.5.0-alpha8")
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
     * {@link #setStepButtonsVisible(boolean) step buttons}. It is also used to
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
