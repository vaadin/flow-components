/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
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
@HtmlImport("frontend://bower_components/vaadin-text-field/src/vaadin-email-field.html")
@NpmPackage(value = "@vaadin/vaadin-text-field", version = "2.10.0")
@JsModule("@vaadin/vaadin-text-field/src/vaadin-email-field.js")
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
