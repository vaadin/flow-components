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
package com.vaadin.flow.component.checkbox;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.shared.HasClientValidation;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.dom.PropertyChangeListener;

/**
 * Checkbox is an input field representing a binary choice.
 * <p>
 * Checkbox also has an indeterminate mode, see {@link #isIndeterminate()} for
 * more info.
 * <p>
 * Use {@link com.vaadin.flow.component.checkbox CheckboxGroup} to group related
 * items. Individual checkboxes should be used for options that are not related
 * to each other in any way.
 * <h2>Validation</h2>
 * <p>
 * Checkbox comes with a built-in validation mechanism that verifies that the
 * field is selected when {@link #setRequiredIndicatorVisible(boolean) required}
 * is enabled.
 * <p>
 * Validation is triggered whenever the user toggles the checkbox. Programmatic
 * toggling triggers validation as well. If validation fails, the component is
 * marked as invalid and an error message is displayed below the input.
 * <p>
 * The required error message can be configured using either
 * {@link CheckboxI18n#setRequiredErrorMessage(String)} or
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
 */
@Tag("vaadin-checkbox")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/checkbox", version = "24.8.0-alpha18")
@JsModule("@vaadin/checkbox/src/vaadin-checkbox.js")
public class Checkbox extends AbstractSinglePropertyField<Checkbox, Boolean>
        implements ClickNotifier<Checkbox>, Focusable<Checkbox>, HasAriaLabel,
        HasClientValidation, HasValidationProperties, HasValidator<Boolean>,
        InputField<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>, Boolean> {

    private final Label labelElement;

    private static final PropertyChangeListener NO_OP = event -> {
    };

    private CheckboxI18n i18n;

    private Validator<Boolean> defaultValidator = (value, context) -> {
        boolean fromComponent = context == null;

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        boolean isRequired = fromComponent && isRequiredIndicatorVisible();
        return ValidationUtil.validateRequiredConstraint(
                getI18nErrorMessage(CheckboxI18n::getRequiredErrorMessage),
                isRequired, getValue(), getEmptyValue());
    };

    private ValidationController<Checkbox, Boolean> validationController = new ValidationController<>(
            this);

    /**
     * Default constructor.
     */
    public Checkbox() {
        super("checked", false, false);
        getElement().setProperty("manualValidation", true);
        getElement().addPropertyChangeListener("indeterminate",
                "indeterminate-changed", NO_OP);
        getElement().addPropertyChangeListener("checked", "checked-changed",
                NO_OP);
        // Initialize property value unless it has already been set from a
        // template
        if (getElement().getProperty("checked") == null) {
            setPresentationValue(false);
        }
        // https://github.com/vaadin/vaadin-checkbox/issues/25
        setIndeterminate(false);

        // Initialize custom label
        labelElement = new Label();
        labelElement.getElement().setAttribute("slot", "label");

        addValueChangeListener(e -> validate());
    }

    /**
     * Constructs a checkbox with the initial label text.
     *
     * @param labelText
     *            the label text to set
     * @see #setLabel(String)
     */
    public Checkbox(String labelText) {
        this();
        setLabel(labelText);
    }

    /**
     * Constructs a checkbox with the initial value.
     *
     * @param initialValue
     *            the initial value
     * @see AbstractField#setValue(Object)
     */
    public Checkbox(boolean initialValue) {
        this();
        setValue(initialValue);
    }

    /**
     * Constructs a checkbox with the initial value.
     *
     * @param labelText
     *            the label text to set
     * @param initialValue
     *            the initial value
     * @see #setLabel(String)
     * @see AbstractField#setValue(Object)
     */
    public Checkbox(String labelText, boolean initialValue) {
        this(labelText);
        setValue(initialValue);
    }

    /**
     * Constructs a checkbox with the initial label text and value change
     * listener.
     *
     * @param label
     *            the label text to set
     * @param listener
     *            the value change listener to add
     * @see #setLabel(String)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public Checkbox(String label,
            ValueChangeListener<ComponentValueChangeEvent<Checkbox, Boolean>> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a checkbox with the initial value and value change listener.
     *
     * @param initialValue
     *            the initial value
     * @param listener
     *            the value change listener to add
     * @see AbstractField#setValue(Object)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public Checkbox(boolean initialValue,
            ValueChangeListener<ComponentValueChangeEvent<Checkbox, Boolean>> listener) {
        this(initialValue);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a checkbox with the initial value, label text and value change
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
    public Checkbox(String labelText, boolean initialValue,
            ValueChangeListener<ComponentValueChangeEvent<Checkbox, Boolean>> listener) {
        this(labelText, initialValue);
        addValueChangeListener(listener);
    }

    /**
     * Sets whether the user is required to select the checkbox. When required,
     * an indicator appears next to the label and the field invalidates if the
     * checkbox is first selected and then deselected.
     * <p>
     * NOTE: The required indicator is only visible when the field has a label,
     * see {@link #setLabel(String)}.
     *
     * @param required
     *            {@code true} to make the field required, {@code false}
     *            otherwise
     * @see CheckboxI18n#setRequiredErrorMessage(String)
     */
    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * Gets whether the user is required to select the checkbox.
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
     * Set the current label text of this checkbox.
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
     *
     * @since 23.1
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
     * Set the checkbox to be input focused when the page loads.
     *
     * @param autofocus
     *            the boolean value to set
     */
    public void setAutofocus(boolean autofocus) {
        getElement().setProperty("autofocus", autofocus);
    }

    /**
     * Get the state for the auto-focus property of the checkbox.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return the {@code autofocus} property from the checkbox
     */
    public boolean isAutofocus() {
        return getElement().getProperty("autofocus", false);
    }

    /**
     * Set the indeterminate state of the checkbox.
     * <p>
     * <em>NOTE: As according to the HTML5 standard, this has only effect on the
     * visual appearance, not on the checked value!</em>
     *
     * @param indeterminate
     *            the boolean value to set
     * @see #isIndeterminate()
     */
    public void setIndeterminate(boolean indeterminate) {
        getElement().setProperty("indeterminate", indeterminate);
    }

    /**
     * Get the indeterminate state of the checkbox. The default value is
     * <code>false</code>.
     * <p>
     * An indeterminate checkbox is neither checked nor unchecked. A typical use
     * case is a “Select All” checkbox indicating that some, but not all, items
     * are selected. When the user clicks an indeterminate checkbox, it is no
     * longer indeterminate, and the <code>checked</code> value also changes.
     * <p>
     * <em>NOTE: As according to the HTML5 standard, this has only effect on the
     * visual appearance, not on the checked value!</em>
     *
     * @return the {@code indeterminate} property from the checkbox
     */
    @Synchronize(property = "indeterminate", value = "indeterminate-changed")
    public boolean isIndeterminate() {
        return getElement().getProperty("indeterminate", false);
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
     * If true, the user cannot interact with this element.
     *
     * @param disabled
     *            the boolean value to set
     */
    void setDisabled(boolean disabled) {
        getElement().setProperty("disabled", disabled);
    }

    /**
     * If true, the user cannot interact with this element.
     *
     * @return the {@code disabled} property from the webcomponent
     */
    boolean isDisabledBoolean() {
        return getElement().getProperty("disabled", false);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(CheckboxI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public CheckboxI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(CheckboxI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getI18nErrorMessage(Function<CheckboxI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }

    /**
     * The internationalization properties for {@link Checkbox}.
     */
    public static class CheckboxI18n implements Serializable {

        private String requiredErrorMessage;

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see Checkbox#isRequiredIndicatorVisible()
         * @see Checkbox#setRequiredIndicatorVisible(boolean)
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link Checkbox#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see Checkbox#isRequiredIndicatorVisible()
         * @see Checkbox#setRequiredIndicatorVisible(boolean)
         */
        public CheckboxI18n setRequiredErrorMessage(String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }
    }

}
