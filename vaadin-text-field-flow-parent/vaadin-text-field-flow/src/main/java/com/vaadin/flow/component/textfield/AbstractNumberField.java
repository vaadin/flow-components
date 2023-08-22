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

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Consumer;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.shared.ClientValidationUtil;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValidationStatusChangeEvent;
import com.vaadin.flow.data.binder.ValidationStatusChangeListener;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;

/**
 * Abstract base class for components based on {@code vaadin-number-field}
 * element and its subclasses.
 *
 * @author Vaadin Ltd.
 */
public abstract class AbstractNumberField<C extends AbstractNumberField<C, T>, T extends Number>
        extends TextFieldBase<C, T> {

    private boolean required;

    /*
     * Note: setters and getters for min/max/step needed to be duplicated in
     * NumberField and IntegerField, because they use primitive double and int
     * types, which can't be used as generic type parameters. Changing to Double
     * and Integer classes would be API-breaking change.
     */
    private double min;
    private double max;
    private double step;

    private boolean stepSetByUser;
    private boolean minSetByUser;

    private boolean manualValidationEnabled = false;

    /**
     * Sets up the common logic for number fields.
     *
     * @param parser
     *            function to parse the client-side value string into
     *            server-side value
     * @param formatter
     *            function to format the server-side value into client-side
     *            value string
     * @param absoluteMin
     *            the smallest possible value of the number type of the field,
     *            will be used as the default min value at server-side
     * @param absoluteMax
     *            the largest possible value of the number type of the field,
     *            will be used as the default max value at server-side
     */
    public AbstractNumberField(SerializableFunction<String, T> parser,
            SerializableFunction<T, String> formatter, double absoluteMin,
            double absoluteMax) {
        super(null, null, String.class, parser, formatter, true);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        // Not setting these defaults to the web component, so it will have
        // undefined as min and max
        this.min = absoluteMin;
        this.max = absoluteMax;
        this.step = 1.0;

        setValueChangeMode(ValueChangeMode.ON_CHANGE);

        addValueChangeListener(e -> validate());

        addUnparseableChangeListener(e -> validate());

        addHasInputValueChangedListener(e -> {
            if (getValueChangeMode().equals(ValueChangeMode.EAGER)
                    && Objects.equals(getValue(), getEmptyValue())) {
                validate();
            }
        });
    }

    /**
     * Sets the visibility of the buttons for increasing/decreasing the value
     * accordingly to the default or specified step.
     *
     * @see #setStep(double)
     *
     * @param stepButtonsVisible
     *            {@code true} if control buttons should be visible;
     *            {@code false} if those should be hidden
     */
    public void setStepButtonsVisible(boolean stepButtonsVisible) {
        getElement().setProperty("stepButtonsVisible", stepButtonsVisible);
    }

    /**
     * Gets whether the buttons for increasing/decreasing the value are visible.
     *
     * @see #setStep(double)
     *
     * @return {@code true} if buttons are visible, {@code false} otherwise
     */
    public boolean isStepButtonsVisible() {
        return getElement().getProperty("stepButtonsVisible", false);
    }

    /**
     * Returns the value that represents an empty value.
     */
    @Override
    public T getEmptyValue() {
        return null;
    }

    /**
     * Sets the value of this number field. If the new value is not equal to
     * {@code getValue()}, fires a value change event.
     *
     * @param value
     *            the new value
     */
    @Override
    public void setValue(T value) {
        T oldValue = getValue();

        super.setValue(value);

        // Clear the input element from possible bad input.
        if (Objects.equals(oldValue, getEmptyValue())
                && Objects.equals(value, getEmptyValue())
                && isInputValuePresent()) {
            // The check for value presence guarantees that a non-empty value
            // won't get cleared when setValue(null) and setValue(...) are
            // subsequently called within one round-trip.
            // Flow only sends the final component value to the client
            // when you update the value multiple times during a round-trip
            // and the final value is sent in place of the first one, so
            // `executeJs` can end up invoked after a non-empty value is set.
            getElement()
                    .executeJs("if (!this.value) this._inputElementValue = ''");
            getElement().setProperty("_hasInputValue", false);
            fireEvent(new UnparseableChangeEvent(this, false));
        }
    }

    /**
     * Returns the current value of the number field. By default, the empty
     * number field will return {@code null} .
     *
     * @return the current value.
     */
    @Override
    public T getValue() {
        return super.getValue();
    }

    /**
     * Sets the minimum value of the field.
     *
     * @param min
     *            the double value to set
     */
    protected void setMin(double min) {
        getElement().setProperty("min", min);
        this.min = min;
        minSetByUser = true;
    }

    /**
     * The minimum value of the field.
     */
    protected double getMinDouble() {
        return min;
    }

    /**
     * Sets the maximum value of the field.
     *
     * @param max
     *            the double value to set
     */
    protected void setMax(double max) {
        getElement().setProperty("max", max);
        this.max = max;
    }

    /**
     * The maximum value of the field.
     */
    protected double getMaxDouble() {
        return max;
    }

    /**
     * Sets the allowed number intervals of the field.
     *
     * @param step
     *            the double value to set
     */
    protected void setStep(double step) {
        getElement().setProperty("step", step);
        this.step = step;
        stepSetByUser = true;
    }

    /**
     * The allowed number intervals of the field.
     */
    protected double getStepDouble() {
        return step;
    }

    /**
     * Returns whether the input element has a value or not.
     *
     * @return <code>true</code> if the input element's value is populated,
     *         <code>false</code> otherwise
     */
    @Synchronize(property = "_hasInputValue", value = "has-input-value-changed")
    private boolean isInputValuePresent() {
        return getElement().getProperty("_hasInputValue", false);
    }

    @Override
    public Validator<T> getDefaultValidator() {
        return (value, context) -> checkValidity(value);
    }

    @Override
    public Registration addValidationStatusChangeListener(
            ValidationStatusChangeListener<T> listener) {
        return Registration.combine(addHasInputValueChangedListener(event -> {
            if (getValueChangeMode().equals(ValueChangeMode.EAGER)
                    && Objects.equals(getValue(), getEmptyValue())) {
                listener.validationStatusChanged(
                        new ValidationStatusChangeEvent<T>(this, !isInvalid()));
            }
        }), addUnparseableChangeListener(event -> {
            listener.validationStatusChanged(
                    new ValidationStatusChangeEvent<T>(this, !isInvalid()));
        }));
    }

    private ValidationResult checkValidity(T value) {
        boolean hasNonParsableValue = Objects.equals(value, getEmptyValue())
                && isInputValuePresent();
        if (hasNonParsableValue) {
            return ValidationResult.error("");
        }

        Double doubleValue = value != null ? value.doubleValue() : null;

        ValidationResult greaterThanMax = ValidationUtil
                .checkGreaterThanMax(doubleValue, max);
        if (greaterThanMax.isError()) {
            return greaterThanMax;
        }

        ValidationResult smallerThanMin = ValidationUtil
                .checkSmallerThanMin(doubleValue, min);
        if (smallerThanMin.isError()) {
            return smallerThanMin;
        }

        if (!isValidByStep(value)) {
            return ValidationResult.error("");
        }

        return ValidationResult.ok();
    }

    @Override
    public void setManualValidation(boolean enabled) {
        this.manualValidationEnabled = enabled;
    }

    /**
     * Performs server-side validation of the current value. This is needed
     * because it is possible to circumvent the client-side validation
     * constraints using browser development tools.
     */
    protected void validate() {
        if (!this.manualValidationEnabled) {
            T value = getValue();

            final var requiredValidation = ValidationUtil
                    .checkRequired(required, value, getEmptyValue());

            setInvalid(requiredValidation.isError()
                    || checkValidity(value).isError());
        }
    }

    private boolean isValidByStep(T value) {

        if (!stepSetByUser// Don't use step in validation if it's not explicitly
                          // set by user. This follows the web component logic.
                || value == null || step == 0) {
            return true;
        }

        // When min is not defined by user, its value is the absoluteMin
        // provided in constructor. In this case, min should not be considered
        // in the step validation.
        double stepBasis = minSetByUser ? getMinDouble() : 0.0;

        // (value - stepBasis) % step == 0
        return new BigDecimal(String.valueOf(value))
                .subtract(BigDecimal.valueOf(stepBasis))
                .remainder(BigDecimal.valueOf(step))
                .compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);
        this.required = requiredIndicatorVisible;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ClientValidationUtil.preventWebComponentFromModifyingInvalidState(this);
    }
}
