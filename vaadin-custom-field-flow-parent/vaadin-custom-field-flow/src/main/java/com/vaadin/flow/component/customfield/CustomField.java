package com.vaadin.flow.component.customfield;

/*
 * #%L
 * Vaadin CustomField for Vaadin 10
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

/**
 * A {@link HasValue} whose UI content can be constructed by the user, enabling
 * the creation of e.g. form fields by composing Vaadin components.
 * Customization of both the visual presentation and the logic of the field is
 * possible.
 * <p>
 * Subclasses must implement {@link #generateModelValue()} and AbstractField{@link #setPresentationValue(Object)}.
 *
 * @param <T> field value type
 */
@Tag("vaadin-custom-field")
@HtmlImport("frontend://bower_components/vaadin-custom-field/src/vaadin-custom-field.html")
public abstract class CustomField<T> extends AbstractField<CustomField<T>, T>
    implements HasComponents, HasSize, HasValidation, Focusable<CustomField> {

    /**
     * Default constructor.
     */
    public CustomField() {
        this(null);
    }

    /**
     * Constructs a new custom field.
     *
     * @param defaultValue The initial value for the field. Will also be used by {@link #getEmptyValue()}.
     * @see AbstractField#AbstractField(Object)
     */
    public CustomField(T defaultValue) {
        super(defaultValue);
        // Force a value update when the change event generated
        getElement().addEventListener("change", e -> this.updateValue());
    }

    /**
     * This method should return the value of the field, based on value of the internal fields.
     *
     * @return new value of the field.
     */
    protected abstract T generateModelValue();

    /**
     * This method should be implemented to set the value of the fields contained
     * in this custom field according to the value of the parameter.
     * It can also be use to show the value to the user in some way, like placing it in an element contained on the field.
     *
     * {@inheritDoc}
     *
     * @param newPresentationValue The new presentation value.
     */
    @Override
    protected abstract void setPresentationValue(T newPresentationValue);

    /**
     * Updates both the model and the presentation values.
     */
    protected void updateValue() {
        setModelValue(generateModelValue(), false);
        setPresentationValue(getValue());
    }

    /**
     * <p>
     * This property is set to true when the control value is invalid.
     * <p>
     * This property is synchronized automatically from client side when a
     * 'invalid-changed' event happens.
     * </p>
     *
     * @return the {@code invalid} property from the webcomponent
     */
    @Synchronize(property = "invalid", value = "invalid-changed")
    @Override
    public boolean isInvalid() {
        return getElement().getProperty("invalid", false);
    }

    /**
     * <p>
     * This property is set to true when the control value is invalid.
     * </p>
     *
     * @param invalid the boolean value to set
     */
    @Override
    public void setInvalid(boolean invalid) {
        getElement().setProperty("invalid", invalid);
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        getElement().setProperty("errorMessage", errorMessage);
    }

    @Override
    public String getErrorMessage() {
        return getElement().getProperty("errorMessage");
    }

    /**
     * Gets the label for the field.
     *
     * @return the {@code label} property from the webcomponent
     */
    public String getLabel() {
        return getElement().getProperty("label", null);
    }

    /**
     * Sets the label for the field.
     *
     * @param label value for the {@code label} property in the webcomponent
     */
    public void setLabel(String label) {
        getElement().setProperty("label", label);
    }

}

