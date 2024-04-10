/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
 * {@code <vaadin-number-field>} is a Polymer 2 element for number field control
 * in forms.
 * </p>
 * <p>
 * &lt;vaadin-number-field label=&quot;Number&quot;&gt;
 * &lt;/vaadin-number-field&gt;
 * </p>
 *
 * @deprecated since v23.3, generated classes will be removed in v24.
 */
@Deprecated
@Tag("vaadin-number-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.5.0")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/number-field", version = "23.5.0")
@NpmPackage(value = "@vaadin/vaadin-text-field", version = "23.5.0")
@JsModule("@vaadin/number-field/src/vaadin-number-field.js")
public abstract class GeneratedVaadinNumberField<R extends GeneratedVaadinNumberField<R, T>, T>
        extends GeneratedVaadinTextField<R, T> implements HasStyle {

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Set to true to display value increase/decrease controls.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code hasControls} property from the webcomponent
     *
     * @deprecated since 23.3. Use
     *             {@link AbstractNumberField#isStepButtonsVisible()} instead.
     */
    @Deprecated
    protected boolean hasControlsBoolean() {
        return getElement().getProperty("hasControls", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Set to true to display value increase/decrease controls.
     * </p>
     *
     * @param hasControls
     *            the boolean value to set
     *
     * @deprecated since 23.3. Use
     *             {@link AbstractNumberField#setStepButtonsVisible(boolean)}
     *             instead.
     */
    @Deprecated
    protected void setHasControls(boolean hasControls) {
        getElement().setProperty("hasControls", hasControls);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * The minimum value of the field.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code min} property from the webcomponent
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected double getMinDouble() {
        return getElement().getProperty("min", 0.0);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * The minimum value of the field.
     * </p>
     *
     * @param min
     *            the double value to set
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected void setMin(double min) {
        getElement().setProperty("min", min);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * The maximum value of the field.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code max} property from the webcomponent
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected double getMaxDouble() {
        return getElement().getProperty("max", 0.0);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * The maximum value of the field.
     * </p>
     *
     * @param max
     *            the double value to set
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected void setMax(double max) {
        getElement().setProperty("max", max);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specifies the allowed number intervals of the field.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code step} property from the webcomponent
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected double getStepDouble() {
        return getElement().getProperty("step", 0.0);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specifies the allowed number intervals of the field.
     * </p>
     *
     * @param step
     *            the double value to set
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected void setStep(double step) {
        getElement().setProperty("step", step);
    }

    /**
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    @Override
    protected void checkValidity() {
        getElement().callJsFunction("checkValidity");
    }

    /**
     * Constructs a new GeneratedVaadinNumberField component with the given
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
     * @param elementPropertyType
     *            the type of the element property
     * @param presentationToModel
     *            a function that converts a string value to a model value
     * @param modelToPresentation
     *            a function that converts a model value to a string value
     * @param isInitialValueOptional
     *            if {@code isInitialValueOptional} is {@code true} then the
     *            initial value is used only if element has no {@code "value"}
     *            property value, otherwise element {@code "value"} property is
     *            ignored and the initial value is set
     * @param <P>
     *            the property type
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    public <P> GeneratedVaadinNumberField(T initialValue, T defaultValue,
            Class<P> elementPropertyType,
            SerializableFunction<P, T> presentationToModel,
            SerializableFunction<T, P> modelToPresentation,
            boolean isInitialValueOptional) {
        super(initialValue, defaultValue, elementPropertyType,
                presentationToModel, modelToPresentation,
                isInitialValueOptional);
    }

    /**
     * Constructs a new GeneratedVaadinNumberField component with the given
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
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    public <P> GeneratedVaadinNumberField(T initialValue, T defaultValue,
            Class<P> elementPropertyType,
            SerializableFunction<P, T> presentationToModel,
            SerializableFunction<T, P> modelToPresentation) {
        this(initialValue, defaultValue, elementPropertyType,
                presentationToModel, modelToPresentation, false);
    }

    /**
     * Constructs a new GeneratedVaadinNumberField component with the given
     * arguments.
     *
     * @param initialValue
     *            the initial value to set to the value
     * @param defaultValue
     *            the default value to use if the value isn't defined
     * @param acceptNullValues
     *            whether <code>null</code> is accepted as a model value
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    public GeneratedVaadinNumberField(T initialValue, T defaultValue,
            boolean acceptNullValues) {
        super(initialValue, defaultValue, acceptNullValues);
    }

    /**
     * Constructs a new GeneratedVaadinNumberField component with the given
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
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    public <P> GeneratedVaadinNumberField(T initialValue, T defaultValue,
            Class<P> elementPropertyType,
            SerializableBiFunction<R, P, T> presentationToModel,
            SerializableBiFunction<R, T, P> modelToPresentation) {
        super(initialValue, defaultValue, elementPropertyType,
                presentationToModel, modelToPresentation);
    }

    /**
     * Default constructor.
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    public GeneratedVaadinNumberField() {
        this(null, null, null, (SerializableFunction) null,
                (SerializableFunction) null);
    }
}
