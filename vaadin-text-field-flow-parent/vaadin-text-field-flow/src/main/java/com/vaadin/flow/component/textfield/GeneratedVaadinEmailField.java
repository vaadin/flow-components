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
 * <p>
 * Description copied from corresponding location in WebComponent:
 * </p>
 * <p>
 * {@code <vaadin-email-field>} is a Web Component for email field control in
 * forms.
 * </p>
 * <p>
 * &lt;vaadin-email-field label=&quot;Email&quot;&gt;
 * &lt;/vaadin-email-field&gt;
 * </p>
 * <h3>Styling</h3>
 * <p>
 * See vaadin-text-field.html for the styling documentation
 * </p>
 * <p>
 * See
 * <a href="https://github.com/vaadin/vaadin-themable-mixin/wiki">ThemableMixin
 * – how to apply styles for shadow parts</a>
 * </p>
 */
@Tag("vaadin-email-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/email-field", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-text-field", version = "23.1.0-beta1")
@JsModule("@vaadin/email-field/src/vaadin-email-field.js")
public abstract class GeneratedVaadinEmailField<R extends GeneratedVaadinEmailField<R, T>, T>
        extends GeneratedVaadinTextField<R, T> implements HasStyle {

    /**
     * Constructs a new GeneratedVaadinEmailField component with the given
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
     */
    public <P> GeneratedVaadinEmailField(T initialValue, T defaultValue,
            Class<P> elementPropertyType,
            SerializableFunction<P, T> presentationToModel,
            SerializableFunction<T, P> modelToPresentation) {
        super(initialValue, defaultValue, elementPropertyType,
                presentationToModel, modelToPresentation);
    }

    /**
     * Constructs a new GeneratedVaadinEmailField component with the given
     * arguments.
     *
     * @param initialValue
     *            the initial value to set to the value
     * @param defaultValue
     *            the default value to use if the value isn't defined
     * @param acceptNullValues
     *            whether <code>null</code> is accepted as a model value
     */
    public GeneratedVaadinEmailField(T initialValue, T defaultValue,
            boolean acceptNullValues) {
        super(initialValue, defaultValue, acceptNullValues);
    }

    /**
     * Constructs a new GeneratedVaadinEmailField component with the given
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
     */
    public GeneratedVaadinEmailField(T initialValue, T defaultValue,
            boolean acceptNullValues, boolean isInitialValueOptional) {
        super(initialValue, defaultValue, acceptNullValues,
                isInitialValueOptional);
    }

    /**
     * Constructs a new GeneratedVaadinEmailField component with the given
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
     */
    public <P> GeneratedVaadinEmailField(T initialValue, T defaultValue,
            Class<P> elementPropertyType,
            SerializableBiFunction<R, P, T> presentationToModel,
            SerializableBiFunction<R, T, P> modelToPresentation) {
        super(initialValue, defaultValue, elementPropertyType,
                presentationToModel, modelToPresentation);
    }

    /**
     * Default constructor.
     */
    public GeneratedVaadinEmailField() {
        this(null, null, null, (SerializableFunction) null,
                (SerializableFunction) null);
    }
}
