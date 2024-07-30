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
package com.vaadin.flow.component.checkbox;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.shared.ClientValidationUtil;
import com.vaadin.flow.component.shared.HasClientValidation;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.flow.dom.PropertyChangeListener;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Checkbox is an input field representing a binary choice.
 * <p>
 * Checkbox also has an indeterminate mode, see {@link #isIndeterminate()} for
 * more info.
 * <p>
 * Use {@link com.vaadin.flow.component.checkbox CheckboxGroup} to group related
 * items. Individual checkboxes should be used for options that are not related
 * to each other in any way.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-checkbox")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.5.0-alpha6")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/checkbox", version = "24.5.0-alpha6")
@JsModule("@vaadin/checkbox/src/vaadin-checkbox.js")
public class Checkbox extends AbstractSinglePropertyField<Checkbox, Boolean>
        implements ClickNotifier<Checkbox>, Focusable<Checkbox>, HasAriaLabel,
        HasClientValidation, HasValidationProperties, HasValidator<Boolean>,
        InputField<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>, Boolean> {

    private final Label labelElement;

    private static final PropertyChangeListener NO_OP = event -> {
    };
    private String ariaLabel;
    private String ariaLabelledBy;

    private CheckboxI18n i18n;

    private boolean manualValidationEnabled = false;

    private String customErrorMessage;
    private String constraintErrorMessage;

    /**
     * Default constructor.
     */
    public Checkbox() {
        super("checked", false, false);
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        ClientValidationUtil.preventWebComponentFromModifyingInvalidState(this);
    }

    /**
     * Sets an error message to display for all constraint violations.
     * <p>
     * This error message takes priority over i18n error messages when both are
     * set.
     *
     * @param errorMessage
     *            the error message to set, or {@code null} to clear
     */
    @Override
    public void setErrorMessage(String errorMessage) {
        customErrorMessage = errorMessage;
        updateErrorMessage();
    }

    /**
     * Gets the error message displayed for all constraint violations.
     *
     * @return the error message
     */
    @Override
    public String getErrorMessage() {
        return customErrorMessage;
    }

    private void setConstraintErrorMessage(String errorMessage) {
        constraintErrorMessage = errorMessage;
        updateErrorMessage();
    }

    private void updateErrorMessage() {
        String errorMessage = constraintErrorMessage;
        if (customErrorMessage != null && !customErrorMessage.isEmpty()) {
            errorMessage = customErrorMessage;
        }
        getElement().setProperty("errorMessage", errorMessage);
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
        toggleInputElementAttribute(ElementConstants.ARIA_LABEL_ATTRIBUTE_NAME,
                ariaLabel);
        this.ariaLabel = ariaLabel;
    }

    @Override
    public Optional<String> getAriaLabel() {
        return Optional.ofNullable(ariaLabel);
    }

    @Override
    public void setAriaLabelledBy(String ariaLabelledBy) {
        toggleInputElementAttribute(
                ElementConstants.ARIA_LABELLEDBY_ATTRIBUTE_NAME,
                ariaLabelledBy);
        this.ariaLabelledBy = ariaLabelledBy;
    }

    @Override
    public Optional<String> getAriaLabelledBy() {
        return Optional.ofNullable(ariaLabelledBy);
    }

    private void toggleInputElementAttribute(String attribute, String value) {
        if (value != null) {
            getElement().executeJs("this.inputElement.setAttribute($0, $1)",
                    attribute, value);
        } else {
            getElement().executeJs("this.inputElement.removeAttribute($0)",
                    attribute);
        }
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
        this.manualValidationEnabled = enabled;
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
        if (this.manualValidationEnabled) {
            return;
        }

        ValidationResult result = ValidationUtil.validateRequiredConstraint(
                getI18nErrorMessage(CheckboxI18n::getRequiredErrorMessage),
                isRequiredIndicatorVisible(), getValue(), getEmptyValue());
        if (result.isError()) {
            setInvalid(true);
            setConstraintErrorMessage(result.getErrorMessage());
        } else {
            setInvalid(false);
            setConstraintErrorMessage("");
        }
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
