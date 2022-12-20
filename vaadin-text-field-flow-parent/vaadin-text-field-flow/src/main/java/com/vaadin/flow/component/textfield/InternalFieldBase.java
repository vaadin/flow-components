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

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.CompositionNotifier;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.InputNotifier;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.shared.HasClearButton;
import com.vaadin.flow.component.shared.HasClientValidation;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableFunction;

/**
 * Internal class that provides base functionality for input field components,
 * such as {@link TextField}. Not intended to be used publicly.
 *
 * @param <TComponent>
 *            Type of the component that extends from this class
 * @param <TValue>
 *            Type of the value of the extending component
 */
public abstract class InternalFieldBase<TComponent extends InternalFieldBase<TComponent, TValue>, TValue>
        extends AbstractSinglePropertyField<TComponent, TValue>
        implements CompositionNotifier, Focusable<TComponent>,
        HasAutocapitalize, HasAutocomplete, HasAutocorrect, HasClearButton,
        HasClientValidation, HasHelper, HasLabel, HasPrefixAndSuffix, HasSize,
        HasStyle, HasTooltip, HasValidation, HasValidator<TValue>,
        HasValueChangeMode, InputNotifier, KeyNotifier {

    private ValueChangeMode currentMode;

    private int valueChangeTimeout = DEFAULT_CHANGE_TIMEOUT;

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

    /**
     * Sets the placeholder text that should be displayed in the input element,
     * when the user has not entered a value
     *
     * @param placeholder
     *            the placeholder text
     */
    public void setPlaceholder(String placeholder) {
        getElement().setProperty("placeholder",
                placeholder == null ? "" : placeholder);
    }

    /**
     * The placeholder text that should be displayed in the input element, when
     * the user has not entered a value
     *
     * @return the {@code placeholder} property from the web component
     */
    public String getPlaceholder() {
        return getElement().getProperty("placeholder");
    }

    /**
     * Sets the error message that should be displayed when the component
     * becomes invalid.
     *
     * @param errorMessage
     *            the String value to set
     */
    public void setErrorMessage(String errorMessage) {
        getElement().setProperty("errorMessage",
                errorMessage == null ? "" : errorMessage);
    }

    /**
     * Gets the current error message from the web component.
     *
     * @return the current error message
     */
    public String getErrorMessage() {
        return getElement().getProperty("errorMessage");
    }

    /**
     * Sets the whether the component should automatically receive focus when
     * the page loads. Defaults to {@code false}.
     *
     * @param autofocus
     *            {@code true} component should automatically receive focus
     */
    public void setAutofocus(boolean autofocus) {
        getElement().setProperty("autofocus", autofocus);
    }

    /**
     * Specify that this control should have input focus when the page loads.
     *
     * @return the {@code autofocus} property from the webcomponent
     */
    public boolean isAutofocus() {
        return getElement().getProperty("autofocus", false);
    }

    /**
     * The text usually displayed in a tooltip popup when the mouse is over the
     * field.
     *
     * @return the {@code title} property from the webcomponent
     */
    public String getTitle() {
        return getElement().getProperty("title");
    }

    /**
     * The text usually displayed in a tooltip popup when the mouse is over the
     * field.
     *
     * @param title
     *            the String value to set
     */
    public void setTitle(String title) {
        getElement().setProperty("title", title == null ? "" : title);
    }

    /**
     * Specifies if the field value gets automatically selected when the field
     * gains focus.
     *
     * @return <code>true</code> if autoselect is active, <code>false</code>
     *         otherwise
     */
    public boolean isAutoselect() {
        return getElement().getProperty("autoselect", false);
    }

    /**
     * Set to <code>true</code> to always have the field value automatically
     * selected when the field gains focus, <code>false</code> otherwise.
     *
     * @param autoselect
     *            <code>true</code> to set auto select on, <code>false</code>
     *            otherwise
     */
    public void setAutoselect(boolean autoselect) {
        getElement().setProperty("autoselect", autoselect);
    }

    /**
     * Sets whether the component has an invalid value or not.
     *
     * @param invalid
     *            {@code true} for invalid, {@code false} for valid
     */
    public void setInvalid(boolean invalid) {
        getElement().setProperty("invalid", invalid);
    }

    /**
     * Whether the component has an invalid value or not.
     *
     * @return the {@code invalid} property from the web component
     */
    public boolean isInvalid() {
        return getElement().getProperty("invalid", false);
    }

    /**
     * Specifies that the user must fill in a value.
     *
     * @param required
     *            the boolean value to set
     */
    public void setRequired(boolean required) {
        getElement().setProperty("required", required);
    }

    /**
     * Determines whether the field is marked as input required.
     *
     * @return {@code true} if the input is required, {@code false} otherwise
     */
    public boolean isRequired() {
        return getElement().getProperty("required", false);
    }

    /**
     * Adds the given components as children of this component at the slot
     * 'prefix'.
     *
     * @param components
     *            The components to add.
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/HTML/Element/slot">MDN
     *      page about slots</a>
     * @see <a href=
     *      "https://html.spec.whatwg.org/multipage/scripting.html#the-slot-element">Spec
     *      website about slots</a>
     */
    protected void addToPrefix(Component... components) {
        for (Component component : components) {
            component.getElement().setAttribute("slot", "prefix");
            getElement().appendChild(component.getElement());
        }
    }

    /**
     * Adds the given components as children of this component at the slot
     * 'input'.
     *
     * @param components
     *            The components to add.
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/HTML/Element/slot">MDN
     *      page about slots</a>
     * @see <a href=
     *      "https://html.spec.whatwg.org/multipage/scripting.html#the-slot-element">Spec
     *      website about slots</a>
     */
    protected void addToInput(Component... components) {
        for (Component component : components) {
            component.getElement().setAttribute("slot", "input");
            getElement().appendChild(component.getElement());
        }
    }

    /**
     * Adds the given components as children of this component at the slot
     * 'suffix'.
     *
     * @param components
     *            The components to add.
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/HTML/Element/slot">MDN
     *      page about slots</a>
     * @see <a href=
     *      "https://html.spec.whatwg.org/multipage/scripting.html#the-slot-element">Spec
     *      website about slots</a>
     */
    protected void addToSuffix(Component... components) {
        for (Component component : components) {
            component.getElement().setAttribute("slot", "suffix");
            getElement().appendChild(component.getElement());
        }
    }

    /**
     * Constructs a new component with the given arguments.
     * <p>
     * If {@code isInitialValueOptional} is {@code true} then the initial value
     * is used only if element has no {@code "value"} property value, otherwise
     * element {@code "value"} property is ignored and the initial value is set.
     *
     * @param initialValue
     *            the initial value to set to the value
     * @param defaultValue
     *            the default value to use if the value isn't defined
     * @param elementPropertyType
     *            the type of the element property
     * @param presentationToModel
     *            a function that converts a string value to a model value
     * @param modelToPresentation
     *            a function that converts a model value to a string value
     *
     * @param isInitialValueOptional
     *            if {@code isInitialValueOptional} is {@code true} then the
     *            initial value is used only if element has no {@code "value"}
     *            property value, otherwise element {@code "value"} property is
     *            ignored and the initial value is set
     * @param <P>
     *            the property type
     */
    public <P> InternalFieldBase(TValue initialValue, TValue defaultValue,
            Class<P> elementPropertyType,
            SerializableFunction<P, TValue> presentationToModel,
            SerializableFunction<TValue, P> modelToPresentation,
            boolean isInitialValueOptional) {
        super("value", defaultValue, elementPropertyType, presentationToModel,
                modelToPresentation);
        if ((getElement().getProperty("value") == null
                || !isInitialValueOptional) && initialValue != null) {
            setPresentationValue(initialValue);
        }
    }

    /**
     * Constructs a new component with the given arguments.
     * <p>
     * If {@code isInitialValueOptional} is {@code true} then the initial value
     * is used only if element has no {@code "value"} property value, otherwise
     * element {@code "value"} property is ignored and the initial value is set.
     *
     * @param initialValue
     *            the initial value to set to the value
     * @param defaultValue
     *            the default value to use if the value isn't defined
     * @param elementPropertyType
     *            the type of the element property
     * @param presentationToModel
     *            a function that accepts this component and a property value
     *            and returns a model value
     * @param modelToPresentation
     *            a function that accepts this component and a model value and
     *            returns a property value
     * @param isInitialValueOptional
     *            if {@code isInitialValueOptional} is {@code true} then the
     *            initial value is used only if element has no {@code "value"}
     *            property value, otherwise element {@code "value"} property is
     *            ignored and the initial value is set
     * @param <P>
     *            the property type
     */
    public <P> InternalFieldBase(TValue initialValue, TValue defaultValue,
            Class<P> elementPropertyType,
            SerializableBiFunction<TComponent, P, TValue> presentationToModel,
            SerializableBiFunction<TComponent, TValue, P> modelToPresentation,
            boolean isInitialValueOptional) {
        super("value", defaultValue, elementPropertyType, presentationToModel,
                modelToPresentation);
        if ((getElement().getProperty("value") == null
                || !isInitialValueOptional) && initialValue != null) {
            setPresentationValue(initialValue);
        }
    }

    /**
     * Constructs a new component with the given arguments.
     * <p>
     * If {@code isInitialValueOptional} is {@code true} then the initial value
     * is used only if element has no {@code "value"} property value, otherwise
     * element {@code "value"} property is ignored and the initial value is set.
     *
     * @param initialValue
     *            the initial value to set to the value
     * @param defaultValue
     *            the default value to use if the value isn't defined
     * @param acceptNullValues
     *            whether <code>null</code> is accepted as a model value
     * @param isInitialValueOptional
     *            if {@code isInitialValueOptional} is {@code true} then the
     *            initial value is used only if element has no {@code "value"}
     *            property value, otherwise element {@code "value"} property is
     *            ignored and the initial value is set
     */
    public InternalFieldBase(TValue initialValue, TValue defaultValue,
            boolean acceptNullValues, boolean isInitialValueOptional) {
        super("value", defaultValue, acceptNullValues);
        if ((getElement().getProperty("value") == null
                || !isInitialValueOptional) && initialValue != null) {
            setPresentationValue(initialValue);
        }
    }
}
