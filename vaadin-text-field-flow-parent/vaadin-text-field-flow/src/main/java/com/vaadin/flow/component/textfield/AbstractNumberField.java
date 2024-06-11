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

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.Serializable;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.shared.ClientValidationUtil;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValidationStatusChangeEvent;
import com.vaadin.flow.data.binder.ValidationStatusChangeListener;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.DomListenerRegistration;
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

    private AbstractNumberFieldI18n i18n;
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

    private DomListenerRegistration inputListenerRegistration;

    private final CopyOnWriteArrayList<ValidationStatusChangeListener<T>> validationStatusChangeListeners = new CopyOnWriteArrayList<>();

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

        getElement().addEventListener("unparsable-change", e -> {
            validate();
            fireValidationStatusChangeEvent();
        });
    }

    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {
        if (inputListenerRegistration != null) {
            inputListenerRegistration.remove();
            inputListenerRegistration = null;
        }

        if (ValueChangeMode.EAGER.equals(valueChangeMode)
                || ValueChangeMode.LAZY.equals(valueChangeMode)
                || ValueChangeMode.TIMEOUT.equals(valueChangeMode)) {
            inputListenerRegistration = getElement().addEventListener("input",
                    event -> {
                        if (valueEquals(getValue(), getEmptyValue())) {
                            validate();
                            fireValidationStatusChangeEvent();
                        }
                    });
        }

        super.setValueChangeMode(valueChangeMode);
    }

    @Override
    void applyChangeTimeout() {
        super.applyChangeTimeout();
        ValueChangeMode.applyChangeTimeout(getValueChangeMode(),
                getValueChangeTimeout(), inputListenerRegistration);
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
        boolean isOldValueEmpty = valueEquals(oldValue, getEmptyValue());
        boolean isNewValueEmpty = valueEquals(value, getEmptyValue());
        boolean isValueRemainedEmpty = isOldValueEmpty && isNewValueEmpty;
        boolean isInputValuePresent = isInputValuePresent();

        // When the value is cleared programmatically, reset hasInputValue
        // so that the following validation doesn't treat this as bad input.
        if (isNewValueEmpty) {
            getElement().setProperty("_hasInputValue", false);
        }

        super.setValue(value);

        // Clear the input element from possible bad input.
        if (isValueRemainedEmpty && isInputValuePresent) {
            // The check for value presence guarantees that a non-empty value
            // won't get cleared when setValue(null) and setValue(...) are
            // subsequently called within one round-trip.
            // Flow only sends the final component value to the client
            // when you update the value multiple times during a round-trip
            // and the final value is sent in place of the first one, so
            // `executeJs` can end up invoked after a non-empty value is set.
            getElement()
                    .executeJs("if (!this.value) this._inputElementValue = ''");
            validate();
            fireValidationStatusChangeEvent();
        }
    }

    @Override
    protected void setModelValue(T newModelValue, boolean fromClient) {
        T oldModelValue = getValue();

        super.setModelValue(newModelValue, fromClient);

        // Triggers validation when an unparsable or empty value changes to a
        // value that is parsable on the client but still unparsable on the
        // server, which can happen for example due to the difference in Integer
        // limit in Java and JavaScript. In this case, there is no
        // ValueChangeEvent and no unparsable-change event.
        if (fromClient && valueEquals(oldModelValue, getEmptyValue())
                && valueEquals(newModelValue, getEmptyValue())) {
            validate();
            fireValidationStatusChangeEvent();
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
        return (value, context) -> checkValidity(value, false);
    }

    @Override
    public Registration addValidationStatusChangeListener(
            ValidationStatusChangeListener<T> listener) {
        return Registration.addAndRemove(validationStatusChangeListeners,
                listener);
    }

    /**
     * Notifies Binder that it needs to revalidate the component since the
     * component's validity state may have changed. Note, there is no need to
     * notify Binder separately in the case of a ValueChangeEvent, as Binder
     * already listens to this event and revalidates automatically.
     */
    private void fireValidationStatusChangeEvent() {
        ValidationStatusChangeEvent<T> event = new ValidationStatusChangeEvent<>(
                this, !isInvalid());
        validationStatusChangeListeners
                .forEach(listener -> listener.validationStatusChanged(event));
    }

    private ValidationResult checkValidity(T value,
            boolean withRequiredValidator) {
        boolean hasBadInput = valueEquals(value, getEmptyValue())
                && isInputValuePresent();
        if (hasBadInput) {
            return ValidationResult.error(getBadInputErrorMessage());
        }

        if (withRequiredValidator) {
            ValidationResult requiredResult = ValidationUtil
                    .validateRequiredConstraint(getRequiredErrorMessage(),
                            isRequiredIndicatorVisible(), value,
                            getEmptyValue());
            if (requiredResult.isError()) {
                return requiredResult;
            }
        }

        Double doubleValue = value != null ? value.doubleValue() : null;

        ValidationResult maxResult = ValidationUtil
                .validateMaxConstraint(getMaxErrorMessage(), doubleValue, max);
        if (maxResult.isError()) {
            return maxResult;
        }

        ValidationResult minResult = ValidationUtil
                .validateMinConstraint(getMinErrorMessage(), doubleValue, min);
        if (minResult.isError()) {
            return minResult;
        }

        if (!isValidByStep(value)) {
            return ValidationResult.error(getStepErrorMessage());
        }

        return ValidationResult.ok();
    }

    @Override
    public void setManualValidation(boolean enabled) {
        this.manualValidationEnabled = enabled;
    }

    /**
     * Validates the current value against the constraints and sets the
     * {@code invalid} property and the {@code errorMessage} property using the
     * error messages defined in the i18n object.
     * <p>
     * The method does nothing if the manual validation mode is enabled.
     */
    protected void validate() {
        if (this.manualValidationEnabled) {
            return;
        }

        ValidationResult result = checkValidity(getValue(), true);
        if (result.isError()) {
            setInvalid(true);
            setErrorMessage(result.getErrorMessage());
        } else {
            setInvalid(false);
            setErrorMessage("");
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
        double stepBasis = minSetByUser && !Double.isInfinite(getMinDouble())
                ? getMinDouble()
                : 0.0;

        // (value - stepBasis) % step == 0
        return new BigDecimal(String.valueOf(value))
                .subtract(BigDecimal.valueOf(stepBasis))
                .remainder(BigDecimal.valueOf(step))
                .compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ClientValidationUtil.preventWebComponentFromModifyingInvalidState(this);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link AbstractNumberField#setI18n(AbstractNumberFieldI18n)}
     *
     * @return the i18n object. It will be {@code null}, If the i18n properties
     *         weren't set.
     */
    protected AbstractNumberFieldI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization properties for this component.
     *
     * @param i18n
     *            the internationalized properties, not {@code null}
     */
    protected void setI18n(AbstractNumberFieldI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getBadInputErrorMessage() {
        return Optional.ofNullable(i18n)
                .map(AbstractNumberFieldI18n::getBadInputErrorMessage)
                .orElse("");
    }

    private String getRequiredErrorMessage() {
        return Optional.ofNullable(i18n)
                .map(AbstractNumberFieldI18n::getRequiredErrorMessage)
                .orElse("");
    }

    private String getMinErrorMessage() {
        return Optional.ofNullable(i18n)
                .map(AbstractNumberFieldI18n::getMinErrorMessage).orElse("");
    }

    private String getMaxErrorMessage() {
        return Optional.ofNullable(i18n)
                .map(AbstractNumberFieldI18n::getMaxErrorMessage).orElse("");
    }

    private String getStepErrorMessage() {
        return Optional.ofNullable(i18n)
                .map(AbstractNumberFieldI18n::getStepErrorMessage).orElse("");
    }

    /**
     * The abstract internationalization properties for
     * {@link AbstractNumberField}.
     */
    public static abstract class AbstractNumberFieldI18n
            implements Serializable {
        /**
         * Gets the error message displayed when the field contains user input
         * that the server is unable to convert to type {@link Number}.
         *
         * @return the error message or {@code null} if not set
         */
        public abstract String getBadInputErrorMessage();

        /**
         * Sets the error message to display when the field contains user input
         * that the server is unable to convert to type {@link Number}.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         */
        public abstract AbstractNumberFieldI18n setBadInputErrorMessage(
                String errorMessage);

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         */
        public abstract String getRequiredErrorMessage();

        /**
         * Sets the error message to display when the field is required but
         * empty.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         */
        public abstract AbstractNumberFieldI18n setRequiredErrorMessage(
                String errorMessage);

        /**
         * Gets the error message displayed when the field value is smaller than
         * the minimum allowed value.
         *
         * @return the error message or {@code null} if not set
         */
        public abstract String getMinErrorMessage();

        /**
         * Sets the error message to display when the field value is smaller
         * than the minimum allowed value.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         */
        public abstract AbstractNumberFieldI18n setMinErrorMessage(
                String errorMessage);

        /**
         * Gets the error message displayed when the field value is greater than
         * the maximum allowed value.
         *
         * @return the error message or {@code null} if not set
         */
        public abstract String getMaxErrorMessage();

        /**
         * Sets the error message to display when the field value is greater
         * than the maximum allowed value.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         */
        public abstract AbstractNumberFieldI18n setMaxErrorMessage(
                String errorMessage);

        /**
         * Gets the error message displayed when the field value is not a
         * multiple of the step value.
         *
         * @return the error message or {@code null} if not set
         */
        public abstract String getStepErrorMessage();

        /**
         * Sets the error message to display when the field value is not a
         * multiple of the step value.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         */
        public abstract AbstractNumberFieldI18n setStepErrorMessage(
                String errorMessage);
    }
}
