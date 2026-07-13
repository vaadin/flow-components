/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.checkbox;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.Validator;

/**
 * Switch is an input field representing a binary on/off choice.
 * <p>
 * This component is experimental and needs to be enabled with the
 * {@code com.vaadin.experimental.switchComponent} feature flag.
 * <p>
 * Switch is functionally equivalent to {@link Checkbox}, but it is presented as
 * an on/off toggle and is intended for a single setting that takes effect
 * immediately. Unlike Checkbox, Switch has no indeterminate state.
 * <h2>Validation</h2>
 * <p>
 * Switch comes with a built-in validation mechanism that verifies that the
 * field is selected when {@link #setRequiredIndicatorVisible(boolean) required}
 * is enabled.
 * <p>
 * Validation is triggered whenever the user toggles the switch. Programmatic
 * toggling triggers validation as well. If validation fails, the component is
 * marked as invalid and an error message is displayed below the input.
 * <p>
 * The required error message can be configured using either
 * {@link SwitchI18n#setRequiredErrorMessage(String)} or
 * {@link #setErrorMessage(String)}.
 * <p>
 * For more advanced validation that requires custom rules, you can use
 * {@link Binder}. Please note that Binder provides its own API for the required
 * validation, see {@link Binder.BindingBuilder#asRequired(String)
 * asRequired()}.
 * <p>
 * However, if Binder doesn't fit your needs and you want to implement fully
 * custom validation logic, you can disable the built-in validation by setting
 * {@link #setManualValidation(boolean)} to true. This will allow you to control
 * the invalid state and the error message manually using
 * {@link #setInvalid(boolean)} and {@link #setErrorMessage(String)} API.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
@Tag("vaadin-switch")
@NpmPackage(value = "@vaadin/switch", version = "25.3.0-alpha4")
@JsModule("@vaadin/switch/src/vaadin-switch.js")
public class Switch extends AbstractSinglePropertyField<Switch, Boolean>
        implements ClickNotifier<Switch>, Focusable<Switch>, HasAriaLabel,
        HasValidationProperties, HasValidator<Boolean>,
        InputField<AbstractField.ComponentValueChangeEvent<Switch, Boolean>, Boolean>,
        HasThemeVariant<SwitchVariant> {

    private final NativeLabel labelElement;

    private SwitchI18n i18n;

    private Validator<Boolean> defaultValidator = (value, context) -> {
        boolean fromComponent = context == null;

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        boolean isRequired = fromComponent && isRequiredIndicatorVisible();
        return ValidationUtil.validateRequiredConstraint(
                getI18nErrorMessage(SwitchI18n::getRequiredErrorMessage),
                isRequired, getValue(), getEmptyValue());
    };

    private ValidationController<Switch, Boolean> validationController = new ValidationController<>(
            this);

    /**
     * Default constructor.
     */
    public Switch() {
        super("checked", false, false);
        getElement().setProperty("manualValidation", true);
        // Initialize property value unless it has already been set from a
        // template
        if (getElement().getProperty("checked") == null) {
            setPresentationValue(false);
        }

        // Initialize custom label
        labelElement = new NativeLabel();
        labelElement.getElement().setAttribute("slot", "label");

        addValueChangeListener(e -> validate());
    }

    /**
     * Constructs a switch with the initial label text.
     *
     * @param labelText
     *            the label text to set
     * @see #setLabel(String)
     */
    public Switch(String labelText) {
        this();
        setLabel(labelText);
    }

    /**
     * Constructs a switch with the initial value.
     *
     * @param initialValue
     *            the initial value
     * @see AbstractField#setValue(Object)
     */
    public Switch(boolean initialValue) {
        this();
        setValue(initialValue);
    }

    /**
     * Constructs a switch with the initial label text and value.
     *
     * @param labelText
     *            the label text to set
     * @param initialValue
     *            the initial value
     * @see #setLabel(String)
     * @see AbstractField#setValue(Object)
     */
    public Switch(String labelText, boolean initialValue) {
        this(labelText);
        setValue(initialValue);
    }

    /**
     * Constructs a switch with the initial label text and value change
     * listener.
     *
     * @param label
     *            the label text to set
     * @param listener
     *            the value change listener to add
     * @see #setLabel(String)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public Switch(String label,
            ValueChangeListener<ComponentValueChangeEvent<Switch, Boolean>> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a switch with the initial value and value change listener.
     *
     * @param initialValue
     *            the initial value
     * @param listener
     *            the value change listener to add
     * @see AbstractField#setValue(Object)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public Switch(boolean initialValue,
            ValueChangeListener<ComponentValueChangeEvent<Switch, Boolean>> listener) {
        this(initialValue);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a switch with the initial value, label text and value change
     * listener.
     *
     * @param labelText
     *            the label text to set
     * @param initialValue
     *            the initial value
     * @param listener
     *            the value change listener to add
     * @see #setLabel(String)
     * @see AbstractField#setValue(Object)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public Switch(String labelText, boolean initialValue,
            ValueChangeListener<ComponentValueChangeEvent<Switch, Boolean>> listener) {
        this(labelText, initialValue);
        addValueChangeListener(listener);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag(attachEvent.getUI());
    }

    private void checkFeatureFlag(UI ui) {
        FeatureFlags featureFlags = FeatureFlags
                .get(ui.getSession().getService().getContext());
        if (!featureFlags
                .isEnabled(SwitchFeatureFlagProvider.SWITCH_COMPONENT)) {
            throw new SwitchExperimentalFeatureException();
        }
    }

    /**
     * Sets whether the user is required to switch on the control. When
     * required, an indicator appears next to the label and the field
     * invalidates if the switch is first turned on and then turned off.
     * <p>
     * NOTE: The required indicator is only visible when the field has a label,
     * see {@link #setLabel(String)}.
     *
     * @param required
     *            {@code true} to make the field required, {@code false}
     *            otherwise
     * @see SwitchI18n#setRequiredErrorMessage(String)
     */
    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * Gets whether the user is required to switch on the control.
     *
     * @return {@code true} if the field is required, {@code false} otherwise
     * @see #setRequiredIndicatorVisible(boolean)
     */
    @Override
    public boolean isRequiredIndicatorVisible() {
        return super.isRequiredIndicatorVisible();
    }

    /**
     * Get the current label text.
     *
     * @return the current label text
     */
    @Override
    public String getLabel() {
        return getElement().getProperty("label");
    }

    /**
     * Set the current label text of this switch.
     *
     * @param label
     *            the label text to set
     */
    @Override
    public void setLabel(String label) {
        if (getElement().equals(labelElement.getElement().getParent())) {
            getElement().removeChild(labelElement.getElement());
        }
        getElement().setProperty("label", label == null ? "" : label);
    }

    /**
     * Replaces the label content with the given label component.
     *
     * @param component
     *            the component to be added to the label.
     */
    public void setLabelComponent(Component component) {
        setLabel("");
        getElement().appendChild(labelElement.getElement());
        labelElement.removeAll();
        labelElement.add(component);
    }

    @Override
    public void setAriaLabel(String ariaLabel) {
        getElement().setProperty("accessibleName", ariaLabel);
    }

    @Override
    public Optional<String> getAriaLabel() {
        return Optional.ofNullable(getElement().getProperty("accessibleName"));
    }

    @Override
    public void setAriaLabelledBy(String ariaLabelledBy) {
        getElement().setProperty("accessibleNameRef", ariaLabelledBy);
    }

    @Override
    public Optional<String> getAriaLabelledBy() {
        return Optional
                .ofNullable(getElement().getProperty("accessibleNameRef"));
    }

    /**
     * Set the switch to be input focused when the page loads.
     *
     * @param autofocus
     *            the boolean value to set
     */
    public void setAutofocus(boolean autofocus) {
        getElement().setProperty("autofocus", autofocus);
    }

    /**
     * Get the state for the auto-focus property of the switch.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return the {@code autofocus} property from the switch
     */
    public boolean isAutofocus() {
        return getElement().getProperty("autofocus", false);
    }

    @Override
    public void setManualValidation(boolean enabled) {
        validationController.setManualValidation(enabled);
    }

    @Override
    public Validator<Boolean> getDefaultValidator() {
        return defaultValidator;
    }

    /**
     * Validates the current value against the constraints and sets the
     * {@code invalid} property and the {@code errorMessage} property based on
     * the result. If a custom error message is provided with
     * {@link #setErrorMessage(String)}, it is used. Otherwise, the error
     * message defined in the i18n object is used.
     * <p>
     * The method does nothing if the manual validation mode is enabled.
     */
    protected void validate() {
        validationController.validate(getValue());
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using {@link #setI18n(SwitchI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public SwitchI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(SwitchI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getI18nErrorMessage(Function<SwitchI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }

    /**
     * The internationalization properties for {@link Switch}.
     */
    public static class SwitchI18n implements Serializable {

        private String requiredErrorMessage;

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see Switch#isRequiredIndicatorVisible()
         * @see Switch#setRequiredIndicatorVisible(boolean)
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link Switch#setErrorMessage(String)} take priority over i18n error
         * messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see Switch#isRequiredIndicatorVisible()
         * @see Switch#setRequiredIndicatorVisible(boolean)
         */
        public SwitchI18n setRequiredErrorMessage(String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }
    }
}
