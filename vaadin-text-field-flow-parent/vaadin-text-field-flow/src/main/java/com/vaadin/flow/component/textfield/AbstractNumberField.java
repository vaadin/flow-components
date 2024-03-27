/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.textfield;

import java.math.BigDecimal;
import java.util.Objects;

import com.vaadin.experimental.Feature;
import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.CompositionNotifier;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.InputNotifier;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasClearButton;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.server.VaadinService;

/**
 * Abstract base class for components based on {@code vaadin-number-field}
 * element and its subclasses.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("deprecation")
public abstract class AbstractNumberField<C extends AbstractNumberField<C, T>, T extends Number>
        extends GeneratedVaadinNumberField<C, T>
        implements HasSize, HasValidation, HasValueChangeMode,
        HasPrefixAndSuffix, InputNotifier, KeyNotifier, CompositionNotifier,
        HasAutocomplete, HasAutocapitalize, HasAutocorrect, HasHelper, HasLabel,
        HasClearButton, HasAllowedCharPattern,
        HasThemeVariant<TextFieldVariant>, HasTooltip, HasValidator<T> {

    private ValueChangeMode currentMode;

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
     * <p>
     * If {@code isInitialValueOptional} is {@code true} then the initial value
     * is used only if element has no {@code "value"} property value, otherwise
     * element {@code "value"} property is ignored and the initial value is set.
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
     * @param isInitialValueOptional
     *            if {@code isInitialValueOptional} is {@code true} then the
     *            initial value is used only if element has no {@code "value"}
     *            property value, otherwise element {@code "value"} property is
     *            ignored and the initial value is set
     */
    public AbstractNumberField(SerializableFunction<String, T> parser,
            SerializableFunction<T, String> formatter, double absoluteMin,
            double absoluteMax, boolean isInitialValueOptional) {
        super(null, null, String.class, parser, formatter,
                isInitialValueOptional);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        // Not setting these defaults to the web component, so it will have
        // undefined as min and max
        this.min = absoluteMin;
        this.max = absoluteMax;
        this.step = 1.0;

        setValueChangeMode(ValueChangeMode.ON_CHANGE);

        addValueChangeListener(e -> validate());
    }

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
        this(parser, formatter, absoluteMin, absoluteMax, false);
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
     * @deprecated since 23.3. Use {@link #setStepButtonsVisible(boolean)}
     *             instead.
     */
    @Override
    @Deprecated
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
     * @deprecated since 23.3. Use {@link #isStepButtonsVisible()} instead.
     */
    @Deprecated
    public boolean hasControls() {
        return super.hasControlsBoolean();
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
                && Objects.equals(value, getEmptyValue())) {
            // The check for value presence guarantees that a non-empty value
            // won't get cleared when setValue(null) and setValue(...) are
            // subsequently called within one round-trip.
            // Flow only sends the final component value to the client
            // when you update the value multiple times during a round-trip
            // and the final value is sent in place of the first one, so
            // `executeJs` can end up invoked after a non-empty value is set.
            getElement()
                    .executeJs("if (!this.value) this.inputElement.value = ''");
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

    @Override
    public Validator<T> getDefaultValidator() {
        if (isFeatureFlagEnabled(FeatureFlags.ENFORCE_FIELD_VALIDATION)) {
            return (value, context) -> checkValidity(value);
        }

        return Validator.alwaysPass();
    }

    private ValidationResult checkValidity(T value) {
        final boolean isGreaterThanMax = value != null
                && value.doubleValue() > max;
        if (isGreaterThanMax) {
            return ValidationResult.error("");
        }

        final boolean isSmallerThanMin = value != null
                && value.doubleValue() < min;
        if (isSmallerThanMin) {
            return ValidationResult.error("");
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

        final var requiredValidation = ValidationUtil.checkRequired(required,
                value, getEmptyValue());

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
        FieldValidationUtil.disableClientValidation(this);
    }

    // Override is only required to keep binary compatibility with other 23.x
    // minor versions, can be removed in a future major
    @Override
    public void addThemeVariants(TextFieldVariant... variants) {
        HasThemeVariant.super.addThemeVariants(variants);
    }

    // Override is only required to keep binary compatibility with other 23.x
    // minor versions, can be removed in a future major
    @Override
    public void removeThemeVariants(TextFieldVariant... variants) {
        HasThemeVariant.super.removeThemeVariants(variants);
    }

    /**
     * Returns true if the given feature flag is enabled, false otherwise.
     * <p>
     * Exposed with protected visibility to support mocking
     * <p>
     * The method requires the {@code VaadinService} instance to obtain the
     * available feature flags, otherwise, the feature is considered disabled.
     *
     * @param feature
     *            the feature flag.
     * @return whether the feature flag is enabled.
     */
    protected boolean isFeatureFlagEnabled(Feature feature) {
        VaadinService service = VaadinService.getCurrent();
        if (service == null) {
            return false;
        }

        return FeatureFlags.get(service.getContext()).isEnabled(feature);
    }
}
