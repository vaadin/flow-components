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
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.NotSupported;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableFunction;

/**
 * Provides base functionality for text field related components, such as
 * {@link TextField}
 *
 * @param <TComponent>
 *            Type of the component that extends from this class
 * @param <TValue>
 *            Type of the selection / value of the extending component
 */
public abstract class TextFieldBase<TComponent extends TextFieldBase<TComponent, TValue>, TValue>
        extends AbstractSinglePropertyField<TComponent, TValue>
        implements HasStyle, Focusable<TComponent> {

    /**
     * Whether the component should automatically receive focus when the page
     * loads.
     * <p>
     * This control should have input focus when the page loads.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code autofocus} property from the web component
     */
    protected boolean isAutofocusBoolean() {
        return getElement().getProperty("autofocus", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * This control should have input focus when the page loads.
     * </p>
     *
     * @param autofocus
     *            the boolean value to set
     */
    protected void setAutofocus(boolean autofocus) {
        getElement().setProperty("autofocus", autofocus);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specify that the value should be automatically selected when the field
     * gains focus.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code autoselect} property from the webcomponent
     */
    protected boolean isAutoselectBoolean() {
        return getElement().getProperty("autoselect", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specify that the value should be automatically selected when the field
     * gains focus.
     * </p>
     *
     * @param autoselect
     *            the boolean value to set
     */
    protected void setAutoselect(boolean autoselect) {
        getElement().setProperty("autoselect", autoselect);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Error to show when the input value is invalid.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code errorMessage} property from the webcomponent
     */
    protected String getErrorMessageString() {
        return getElement().getProperty("errorMessage");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Error to show when the input value is invalid.
     * </p>
     *
     * @param errorMessage
     *            the String value to set
     */
    protected void setErrorMessage(String errorMessage) {
        getElement().setProperty("errorMessage",
                errorMessage == null ? "" : errorMessage);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * String used for the label element.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code label} property from the webcomponent
     */
    protected String getLabelString() {
        return getElement().getProperty("label");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * String used for the label element.
     * </p>
     *
     * @param label
     *            the String value to set
     */
    protected void setLabel(String label) {
        getElement().setProperty("label", label == null ? "" : label);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Maximum number of characters (in Unicode code points) that the user can
     * enter.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code maxlength} property from the webcomponent
     */
    protected double getMaxlengthDouble() {
        return getElement().getProperty("maxlength", 0.0);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Maximum number of characters (in Unicode code points) that the user can
     * enter.
     * </p>
     *
     * @param maxlength
     *            the double value to set
     */
    protected void setMaxlength(double maxlength) {
        getElement().setProperty("maxlength", maxlength);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Minimum number of characters (in Unicode code points) that the user can
     * enter.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code minlength} property from the webcomponent
     */
    protected double getMinlengthDouble() {
        return getElement().getProperty("minlength", 0.0);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Minimum number of characters (in Unicode code points) that the user can
     * enter.
     * </p>
     *
     * @param minlength
     *            the double value to set
     */
    protected void setMinlength(double minlength) {
        getElement().setProperty("minlength", minlength);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * The name of the control, which is submitted with the form data.
     * </p>
     *
     * @param name
     *            the String value to set
     */
    protected void setName(String name) {
        getElement().setProperty("name", name == null ? "" : name);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * A hint to the user of what can be entered in the control.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code placeholder} property from the webcomponent
     */
    protected String getPlaceholderString() {
        return getElement().getProperty("placeholder");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * A hint to the user of what can be entered in the control.
     * </p>
     *
     * @param placeholder
     *            the String value to set
     */
    protected void setPlaceholder(String placeholder) {
        getElement().setProperty("placeholder",
                placeholder == null ? "" : placeholder);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specifies that the user must fill in a value.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code required} property from the webcomponent
     */
    protected boolean isRequiredBoolean() {
        return getElement().getProperty("required", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specifies that the user must fill in a value.
     * </p>
     *
     * @param required
     *            the boolean value to set
     */
    protected void setRequired(boolean required) {
        getElement().setProperty("required", required);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * This property is set to true when the control value is invalid.
     * </p>
     *
     * @return the {@code invalid} property from the webcomponent
     */
    protected boolean isInvalidBoolean() {
        return getElement().getProperty("invalid", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * This property is set to true when the control value is invalid.
     * </p>
     *
     * @param invalid
     *            the boolean value to set
     */
    protected void setInvalid(boolean invalid) {
        getElement().setProperty("invalid", invalid);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * A regular expression that the value is checked against. The pattern must
     * match the entire value, not just some subset.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code pattern} property from the webcomponent
     */
    protected String getPatternString() {
        return getElement().getProperty("pattern");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * A regular expression that the value is checked against. The pattern must
     * match the entire value, not just some subset.
     * </p>
     *
     * @param pattern
     *            the String value to set
     */
    protected void setPattern(String pattern) {
        getElement().setProperty("pattern", pattern == null ? "" : pattern);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Message to show to the user when validation fails.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code title} property from the webcomponent
     */
    protected String getTitleString() {
        return getElement().getProperty("title");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * The text usually displayed in a tooltip popup when the mouse is over the
     * field.
     * </p>
     *
     * @param title
     *            the String value to set
     */
    protected void setTitle(String title) {
        getElement().setProperty("title", title == null ? "" : title);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Returns true if the current input value satisfies all constraints (if
     * any)
     * </p>
     * <p>
     * This function is not supported by Flow because it returns a
     * <code>boolean</code>. Functions with return types different than void are
     * not supported at this moment.
     */
    @NotSupported
    protected void checkValidity() {
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Returns true if {@code value} is valid. {@code <iron-form>} uses this to
     * check the validity or all its elements.
     * </p>
     * <p>
     * This function is not supported by Flow because it returns a
     * <code>boolean</code>. Functions with return types different than void are
     * not supported at this moment.
     */
    @NotSupported
    protected void validate() {
    }

    @DomEvent("change")
    public static class ChangeEvent<R extends TextFieldBase<R, ?>>
            extends ComponentEvent<R> {
        public ChangeEvent(R source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    public static class InvalidChangeEvent<R extends TextFieldBase<R, ?>>
            extends ComponentEvent<R> {
        private final boolean invalid;

        public InvalidChangeEvent(R source, boolean fromClient) {
            super(source, fromClient);
            this.invalid = source.isInvalidBoolean();
        }

        public boolean isInvalid() {
            return invalid;
        }
    }

    /**
     * Removes the given child components from this component.
     *
     * @param components
     *            The components to remove.
     * @throws IllegalArgumentException
     *             if any of the components is not a child of this component.
     */
    protected void remove(Component... components) {
        for (Component component : components) {
            if (getElement().equals(component.getElement().getParent())) {
                component.getElement().removeAttribute("slot");
                getElement().removeChild(component.getElement());
            } else {
                throw new IllegalArgumentException("The given component ("
                        + component + ") is not a child of this component");
            }
        }
    }

    /**
     * Removes all contents from this component, this includes child components,
     * text content as well as child elements that have been added directly to
     * this component using the {@link Element} API.
     */
    protected void removeAll() {
        getElement().getChildren()
                .forEach(child -> child.removeAttribute("slot"));
        getElement().removeAllChildren();
    }

    /**
     * Constructs a new TextFieldBase component with the given arguments.
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
    public <P> TextFieldBase(TValue initialValue, TValue defaultValue,
            Class<P> elementPropertyType,
            SerializableFunction<P, TValue> presentationToModel,
            SerializableFunction<TValue, P> modelToPresentation,
            boolean isInitialValueOptional) {
        super("value", defaultValue, elementPropertyType, presentationToModel,
                modelToPresentation);
        if (shouldSetInitialValue(initialValue, isInitialValueOptional)) {
            setPresentationValue(initialValue);
        }
    }

    /**
     * Constructs a new TextFieldBase component with the given arguments.
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
    public TextFieldBase(TValue initialValue, TValue defaultValue,
            boolean acceptNullValues, boolean isInitialValueOptional) {
        super("value", defaultValue, acceptNullValues);
        if (shouldSetInitialValue(initialValue, isInitialValueOptional)) {
            setPresentationValue(initialValue);
        }
    }

    /**
     * Constructs a new TextFieldBase component with the given arguments.
     * <p>
     * If {@code isInitialValueOptional} is {@code true} then the initial value
     * is used only if element has no {@code "value"} property value, otherwise
     * element {@code "value"} property is ignored and the initial value is set.
     *
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
    public <P> TextFieldBase(TValue initialValue, TValue defaultValue,
            Class<P> elementPropertyType,
            SerializableBiFunction<TComponent, P, TValue> presentationToModel,
            SerializableBiFunction<TComponent, TValue, P> modelToPresentation,
            boolean isInitialValueOptional) {
        super("value", defaultValue, elementPropertyType, presentationToModel,
                modelToPresentation);
        if (shouldSetInitialValue(initialValue, isInitialValueOptional)) {
            setPresentationValue(initialValue);
        }
    }

    private boolean shouldSetInitialValue(TValue initialValue,
            boolean isInitialValueOptional) {
        if (initialValue == null) {
            return false;
        }
        return getElement().getProperty("value") == null
                || !isInitialValueOptional;
    }

}
