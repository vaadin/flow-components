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

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableFunction;

/**
 * @deprecated since v23.3, will be removed in v24.
 */
@Deprecated
@Tag("vaadin-password-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.3.0-alpha5")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/password-field", version = "23.3.0-alpha5")
@NpmPackage(value = "@vaadin/vaadin-text-field", version = "23.3.0-alpha5")
@JsModule("@vaadin/password-field/src/vaadin-password-field.js")
public abstract class GeneratedVaadinPasswordField<R extends GeneratedVaadinPasswordField<R, T>, T>
        extends GeneratedVaadinTextField<R, T> implements HasStyle {

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Set to true to hide the eye icon which toggles the password visibility.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code revealButtonHidden} property from the webcomponent
     *
     * @deprecated since v23.3, will be removed in v24
     */
    @Deprecated
    protected boolean isRevealButtonHiddenBoolean() {
        return getElement().getProperty("revealButtonHidden", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Set to true to hide the eye icon which toggles the password visibility.
     * </p>
     *
     * @param revealButtonHidden
     *            the boolean value to set
     *
     * @deprecated since v23.3, will be removed in v24
     */
    @Deprecated
    protected void setRevealButtonHidden(boolean revealButtonHidden) {
        getElement().setProperty("revealButtonHidden", revealButtonHidden);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * True if the password is visible ([type=text]).
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code passwordVisible} property from the webcomponent
     *
     * @deprecated since v23.3, will be removed in v24
     */
    @Deprecated
    protected boolean isPasswordVisibleBoolean() {
        return getElement().getProperty("passwordVisible", false);
    }

    /**
     * Constructs a new GeneratedVaadinPasswordField component with the given
     * arguments.
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
     * @param <P>
     *            the property type
     *
     * @deprecated since v23.3, will be removed in v24
     */
    @Deprecated
    public <P> GeneratedVaadinPasswordField(T initialValue, T defaultValue,
            Class<P> elementPropertyType,
            SerializableFunction<P, T> presentationToModel,
            SerializableFunction<T, P> modelToPresentation) {
        super(initialValue, defaultValue, elementPropertyType,
                presentationToModel, modelToPresentation);
    }

    /**
     * Constructs a new GeneratedVaadinPasswordField component with the given
     * arguments.
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
     *
     * @deprecated since v23.3, will be removed in v24
     */
    @Deprecated
    public GeneratedVaadinPasswordField(T initialValue, T defaultValue,
            boolean acceptNullValues, boolean isInitialValueOptional) {
        super(initialValue, defaultValue, acceptNullValues,
                isInitialValueOptional);
    }

    /**
     * Constructs a new GeneratedVaadinPasswordField component with the given
     * arguments.
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
     * @param <P>
     *            the property type
     *
     * @deprecated since v23.3, will be removed in v24
     */
    @Deprecated
    public <P> GeneratedVaadinPasswordField(T initialValue, T defaultValue,
            Class<P> elementPropertyType,
            SerializableBiFunction<R, P, T> presentationToModel,
            SerializableBiFunction<R, T, P> modelToPresentation) {
        super(initialValue, defaultValue, elementPropertyType,
                presentationToModel, modelToPresentation);
    }

    /**
     * Default constructor.
     *
     * @deprecated since v23.3, will be removed in v24
     */
    @Deprecated
    public GeneratedVaadinPasswordField() {
        this(null, null, null, (SerializableFunction) null,
                (SerializableFunction) null);
    }
}
