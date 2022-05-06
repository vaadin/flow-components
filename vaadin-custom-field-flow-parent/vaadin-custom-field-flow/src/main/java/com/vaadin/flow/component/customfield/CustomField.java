package com.vaadin.flow.component.customfield;

/*
 * #%L
 * Vaadin CustomField for Vaadin 10
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
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

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.dom.Element;

/**
 * A {@link HasValue} whose UI content can be constructed by the user, enabling
 * the creation of e.g. form fields by composing Vaadin components.
 * Customization of both the visual presentation and the logic of the field is
 * possible.
 * <p>
 * Subclasses must implement {@link #generateModelValue()} and
 * AbstractField{@link #setPresentationValue(Object)}.
 *
 * @param <T>
 *            field value type
 */
@Tag("vaadin-custom-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/custom-field", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-custom-field", version = "23.1.0-beta1")
@JsModule("@vaadin/custom-field/src/vaadin-custom-field.js")
public abstract class CustomField<T> extends AbstractField<CustomField<T>, T>
        implements HasSize, HasValidation, Focusable<CustomField>, HasHelper,
        HasLabel, HasTheme, HasStyle {

    /**
     * Default constructor.
     */
    public CustomField() {
        this(null);
    }

    /**
     * Constructs a new custom field.
     *
     * @param defaultValue
     *            The initial value for the field. Will also be used by
     *            {@link #getEmptyValue()}.
     * @see AbstractField#AbstractField(Object)
     */
    public CustomField(T defaultValue) {
        super(defaultValue);
        // Force a value update when the change event generated
        getElement().addEventListener("change", e -> this.updateValue());
    }

    /**
     * This method should return the value of the field, based on value of the
     * internal fields.
     *
     * @return new value of the field.
     */
    protected abstract T generateModelValue();

    /**
     * This method should be implemented to set the value of the fields
     * contained in this custom field according to the value of the parameter.
     * It can also be use to show the value to the user in some way, like
     * placing it in an element contained on the field.
     * <p>
     * {@inheritDoc}
     *
     * @param newPresentationValue
     *            The new presentation value.
     */
    @Override
    protected abstract void setPresentationValue(T newPresentationValue);

    /**
     * Regenerates the value by calling {@link #generateModelValue()} and
     * updates the model. If the value is different than the current one, a
     * {@link ValueChangeEvent} will be generated with
     * {@link ValueChangeEvent#isFromClient()} set to <code>true</code>
     *
     * <p>
     * This method is called when the webcomponent generates a <b>changed</b>
     * event, typically in response to a change made by the user in one of the
     * contained fields.
     *
     * <p>
     * Custom implementations of this method <b>must</b> call
     * {@link #setModelValue(Object, boolean)} with the updated model value.
     * Subclasses can call this method when the model value needs to be
     * regenerated and updated.
     */
    protected void updateValue() {
        // The second parameter is true since this method is called in response
        // to a change event from the client.
        setModelValue(generateModelValue(), true);
    }

    /**
     * Adds the given components as children of this component.
     *
     * @param components
     *            the components to add
     */
    protected void add(Component... components) {
        Objects.requireNonNull(components, "Components should not be null");
        for (Component component : components) {
            Objects.requireNonNull(component,
                    "Component to add cannot be null");
            getElement().appendChild(component.getElement());
        }
    }

    /**
     * Removes the given child components from this component.
     *
     * @param components
     *            the components to remove
     * @throws IllegalArgumentException
     *             if any of the components is not a child of this component
     */
    protected void remove(Component... components) {
        Objects.requireNonNull(components, "Components should not be null");
        for (Component component : components) {
            Objects.requireNonNull(component,
                    "Component to remove cannot be null");
            Element parent = component.getElement().getParent();
            if (parent == null) {
                LoggerFactory.getLogger(CustomField.class).debug(
                        "Remove of a component with no parent does nothing.");
                return;
            }
            if (getElement().equals(parent)) {
                getElement().removeChild(component.getElement());
            } else {
                throw new IllegalArgumentException("The given component ("
                        + component + ") is not a child of this component");
            }
        }
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
     * @param invalid
     *            the boolean value to set
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
    @Override
    public String getLabel() {
        return getElement().getProperty("label", null);
    }

    /**
     * Sets the label for the field.
     *
     * @param label
     *            value for the {@code label} property in the webcomponent
     */
    @Override
    public void setLabel(String label) {
        getElement().setProperty("label", label);
    }

    /**
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(CustomFieldVariant... variants) {
        getThemeNames().addAll(
                Stream.of(variants).map(CustomFieldVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(CustomFieldVariant... variants) {
        getThemeNames().removeAll(
                Stream.of(variants).map(CustomFieldVariant::getVariantName)
                        .collect(Collectors.toList()));
    }
}
