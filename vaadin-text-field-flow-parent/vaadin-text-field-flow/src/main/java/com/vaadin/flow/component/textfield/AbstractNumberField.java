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

import java.math.BigDecimal;
import java.util.Objects;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.CompositionNotifier;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.InputNotifier;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.shared.ClientValidationUtil;
import com.vaadin.flow.component.shared.HasClientValidation;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValidationStatusChangeEvent;
import com.vaadin.flow.data.binder.ValidationStatusChangeListener;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

/**
 * Abstract base class for components based on {@code vaadin-number-field}
 * element and its subclasses.
 *
 * @author Vaadin Ltd.
 */
public abstract class AbstractNumberField<C extends AbstractNumberField<C, T>, T extends Number>
        extends GeneratedVaadinNumberField<C, T>
        implements HasSize, HasValidation, HasValueChangeMode,
        HasPrefixAndSuffix, InputNotifier, KeyNotifier, CompositionNotifier,
        HasAutocomplete, HasAutocapitalize, HasAutocorrect, HasHelper, HasLabel,
        HasValidator<T>, HasClientValidation {

    private ValueChangeMode currentMode;

    private boolean isConnectorAttached;

    private int valueChangeTimeout = DEFAULT_CHANGE_TIMEOUT;

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
        super(null, null, String.class, parser, formatter);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        // Not setting these defaults to the web component, so it will have
        // undefined as min and max
        this.min = absoluteMin;
        this.max = absoluteMax;
        this.step = 1.0;

        setValueChangeMode(ValueChangeMode.ON_CHANGE);

        addValueChangeListener(e -> validate());

        if (isEnforcedFieldValidationEnabled()) {
            addClientValidatedEventListener(e -> validate());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default value is {@link ValueChangeMode#ON_CHANGE}.
     */
    @Override
    public ValueChangeMode getValueChangeMode() {
        return currentMode;
    }

    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {
        currentMode = valueChangeMode;
        setSynchronizedEvent(
                ValueChangeMode.eventForMode(valueChangeMode, "value-changed"));
        applyChangeTimeout();
    }

    @Override
    public void setValueChangeTimeout(int valueChangeTimeout) {
        this.valueChangeTimeout = valueChangeTimeout;
        applyChangeTimeout();
    }

    @Override
    public int getValueChangeTimeout() {
        return valueChangeTimeout;
    }

    private void applyChangeTimeout() {
        ValueChangeMode.applyChangeTimeout(getValueChangeMode(),
                getValueChangeTimeout(), getSynchronizationRegistration());
    }

    @Override
    public String getErrorMessage() {
        return super.getErrorMessageString();
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        super.setErrorMessage(errorMessage);
    }

    /**
     * Sets the label for this component.
     *
     * @param label
     *            value for the {@code label} property in the webcomponent
     */
    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }

    /**
     * String used for the label element.
     *
     * @return the {@code label} property from the webcomponent
     */
    @Override
    public String getLabel() {
        return getLabelString();
    }

    @Override
    public void setPlaceholder(String placeholder) {
        super.setPlaceholder(placeholder);
    }

    /**
     * Sets the visibility of the control buttons for increasing/decreasing the
     * value accordingly to the default or specified step.
     *
     * @see #setStep(double)
     *
     * @param hasControls
     *            {@code true} if control buttons should be visible;
     *            {@code false} if those should be hidden
     */
    @Override
    public void setHasControls(boolean hasControls) {
        super.setHasControls(hasControls);
    }

    /**
     * Gets whether the control buttons for increasing/decreasing the value are
     * visible.
     *
     * @see #setStep(double)
     *
     * @return {@code true} if buttons are visible, {@code false} otherwise
     */
    public boolean hasControls() {
        return super.hasControlsBoolean();
    }

    /**
     * A hint to the user of what can be entered in the component.
     *
     * @return the {@code placeholder} property from the webcomponent
     */
    public String getPlaceholder() {
        return getPlaceholderString();
    }

    @Override
    public void setAutofocus(boolean autofocus) {
        super.setAutofocus(autofocus);
    }

    /**
     * Specify that this control should have input focus when the page loads.
     *
     * @return the {@code autofocus} property from the webcomponent
     */
    public boolean isAutofocus() {
        return isAutofocusBoolean();
    }

    /**
     * The text usually displayed in a tooltip popup when the mouse is over the
     * field.
     *
     * @return the {@code title} property from the webcomponent
     */
    public String getTitle() {
        return super.getTitleString();
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

    /**
     * Specifies if the field value gets automatically selected when the field
     * gains focus.
     *
     * @return <code>true</code> if autoselect is active, <code>false</code>
     *         otherwise
     */
    public boolean isAutoselect() {
        return super.isAutoselectBoolean();
    }

    /**
     * Set to <code>true</code> to always have the field value automatically
     * selected when the field gains focus, <code>false</code> otherwise.
     *
     * @param autoselect
     *            <code>true</code> to set auto select on, <code>false</code>
     *            otherwise
     */
    @Override
    public void setAutoselect(boolean autoselect) {
        super.setAutoselect(autoselect);
    }

    /**
     * Gets the visibility state of the button which clears the number field.
     *
     * @return <code>true</code> if the button is visible, <code>false</code>
     *         otherwise
     */
    public boolean isClearButtonVisible() {
        return isClearButtonVisibleBoolean();
    }

    /**
     * Set to <code>false</code> to hide the clear button which clears the
     * number field.
     *
     * @param clearButtonVisible
     *            <code>true</code> to set the button visible,
     *            <code>false</code> otherwise
     */
    @Override
    public void setClearButtonVisible(boolean clearButtonVisible) {
        super.setClearButtonVisible(clearButtonVisible);
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
            fireEvent(new ClientValidatedEvent(this, false));
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

    @Override
    protected void setMin(double min) {
        super.setMin(min);
        this.min = min;
        minSetByUser = true;
    }

    @Override
    protected double getMinDouble() {
        return min;
    }

    @Override
    protected void setMax(double max) {
        super.setMax(max);
        this.max = max;
    }

    @Override
    protected double getMaxDouble() {
        return max;
    }

    @Override
    protected void setStep(double step) {
        super.setStep(step);
        this.step = step;
        stepSetByUser = true;
    }

    @Override
    protected double getStepDouble() {
        return step;
    }

    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);
    }

    @Override
    public boolean isInvalid() {
        return isInvalidBoolean();
    }

    @Synchronize(property = "_hasInputValue", value = "has-input-value-changed")
    private boolean isInputValuePresent() {
        return getElement().getProperty("_hasInputValue", false);
    }

    @Override
    public Validator<T> getDefaultValidator() {
        if (isEnforcedFieldValidationEnabled()) {
            return (value, context) -> checkValidity(value);
        }

        return Validator.alwaysPass();
    }

    @Override
    public Registration addValidationStatusChangeListener(
            ValidationStatusChangeListener<T> listener) {
        if (isEnforcedFieldValidationEnabled()) {
            return addClientValidatedEventListener(
                    event -> listener.validationStatusChanged(
                            new ValidationStatusChangeEvent<T>(this,
                                    !isInvalid())));
        }

        return null;
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

    /**
     * Performs server-side validation of the current value. This is needed
     * because it is possible to circumvent the client-side validation
     * constraints using browser development tools.
     */
    @Override
    protected void validate() {
        T value = getValue();

        final ValidationResult requiredValidation = TextFieldValidationSupport
                .checkRequired(required, value, getEmptyValue());

        setInvalid(
                requiredValidation.isError() || checkValidity(value).isError());
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
        if (isEnforcedFieldValidationEnabled()) {
            ClientValidationUtil
                    .preventWebComponentFromModifyingInvalidState(this);
        } else {
            FieldValidationUtil.disableClientValidation(this);
        }
    }

    /**
     * Whether the full experience validation is enforced for the component.
     * <p>
     * Exposed with protected visibility to support mocking
     * <p>
     * The method requires the {@code VaadinSession} instance to obtain the
     * application configuration properties, otherwise, the feature is
     * considered disabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    protected boolean isEnforcedFieldValidationEnabled() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return false;
        }
        DeploymentConfiguration configuration = session.getConfiguration();
        if (configuration == null) {
            return false;
        }
        return configuration.isEnforcedFieldValidationEnabled();
    }
}
