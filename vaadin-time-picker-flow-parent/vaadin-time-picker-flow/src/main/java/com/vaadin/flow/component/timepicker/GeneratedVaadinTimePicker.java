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
package com.vaadin.flow.component.timepicker;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.NotSupported;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonObject;

/**
 * <p>
 * Description copied from corresponding location in WebComponent:
 * </p>
 * <p>
 * {@code <vaadin-time-picker>} is a Web Component providing a time-selection
 * field.
 * </p>
 * <p>
 * &lt;vaadin-time-picker&gt;&lt;/vaadin-time-picker&gt;
 * {@code timePicker.value = '14:30';}
 * </p>
 * <p>
 * When the selected {@code value} is changed, a {@code value-changed} event is
 * triggered.
 * </p>
 * <h3>Styling</h3>
 * <p>
 * The following custom properties are available for styling:
 * </p>
 * <table>
 * <thead>
 * <tr>
 * <th>Part name</th>
 * <th>Description</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@code toggle-button}</td>
 * <td>The toggle button</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * See
 * <a href="https://github.com/vaadin/vaadin-themable-mixin/wiki">ThemableMixin
 * – how to apply styles for shadow parts</a>
 * </p>
 * <p>
 * The following state attributes are available for styling:
 * </p>
 * <table>
 * <thead>
 * <tr>
 * <th>Attribute</th>
 * <th>Description</th>
 * <th>Part name</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@code disabled}</td>
 * <td>Set to a disabled time picker</td>
 * <td>:host</td>
 * </tr>
 * <tr>
 * <td>{@code readonly}</td>
 * <td>Set to a read only time picker</td>
 * <td>:host</td>
 * </tr>
 * <tr>
 * <td>{@code invalid}</td>
 * <td>Set when the element is invalid</td>
 * <td>:host</td>
 * </tr>
 * <tr>
 * <td>{@code focused}</td>
 * <td>Set when the element is focused</td>
 * <td>:host</td>
 * </tr>
 * <tr>
 * <td>{@code focus-ring}</td>
 * <td>Set when the element is keyboard focused</td>
 * <td>:host</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * In addition to {@code <vaadin-time-picker>} itself, the following internal
 * components are themable:
 * </p>
 * <ul>
 * <li>{@code <vaadin-time-picker-text-field>}, see <a href=
 * "https://vaadin.com/components/vaadin-text-field/html-api/elements/Vaadin.TextFieldElement"
 * >{@code <vaadin-text-field>} documentation</a> for the text field parts.</li>
 * <li>{@code <vaadin-combo-box-light>}, see <a href=
 * "https://vaadin.com/components/vaadin-combo-box/html-api/elements/Vaadin.ComboBoxElement"
 * >{@code <vaadin-combo-box>} documentation</a> for the combo box parts.</li>
 * </ul>
 * <p>
 * Note: the {@code theme} attribute value set on {@code <vaadin-time-picker>}
 * is propagated to the internal themable components listed above.
 * </p>
 */
@Tag("vaadin-time-picker")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/time-picker", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-time-picker", version = "23.1.0-beta1")
@JsModule("@vaadin/time-picker/src/vaadin-time-picker.js")
public abstract class GeneratedVaadinTimePicker<R extends GeneratedVaadinTimePicker<R, T>, T>
        extends AbstractSinglePropertyField<R, T>
        implements HasStyle, Focusable<R> {

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specify that this control should have input focus when the page loads.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code autofocus} property from the webcomponent
     */
    protected boolean isAutofocusBoolean() {
        return getElement().getProperty("autofocus", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specify that this control should have input focus when the page loads.
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
     * Set to true to disable this input.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code disabled} property from the webcomponent
     */
    protected boolean isDisabledBoolean() {
        return getElement().getProperty("disabled", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Set to true to disable this input.
     * </p>
     *
     * @param disabled
     *            the boolean value to set
     */
    protected void setDisabled(boolean disabled) {
        getElement().setProperty("disabled", disabled);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * The name of this element.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code name} property from the webcomponent
     */
    protected String getNameString() {
        return getElement().getProperty("name");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * The name of this element.
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
     * The label for this element.
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
     * The label for this element.
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
     * Set to true to mark the input as required.
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
     * Set to true to mark the input as required.
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
     * Set to true to prevent the user from entering invalid input.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code preventInvalidInput} property from the webcomponent
     */
    protected boolean isPreventInvalidInputBoolean() {
        return getElement().getProperty("preventInvalidInput", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Set to true to prevent the user from entering invalid input.
     * </p>
     *
     * @param preventInvalidInput
     *            the boolean value to set
     */
    protected void setPreventInvalidInput(boolean preventInvalidInput) {
        getElement().setProperty("preventInvalidInput", preventInvalidInput);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * A pattern to validate the {@code input} with.
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
     * A pattern to validate the {@code input} with.
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
     * The error message to display when the input is invalid.
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
     * The error message to display when the input is invalid.
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
     * A placeholder string in addition to the label.
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
     * A placeholder string in addition to the label.
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
     * Set to true to prevent user picking a date or typing in the input.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code readonly} property from the webcomponent
     */
    protected boolean isReadonlyBoolean() {
        return getElement().getProperty("readonly", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Set to true to prevent user picking a date or typing in the input.
     * </p>
     *
     * @param readonly
     *            the boolean value to set
     */
    protected void setReadonly(boolean readonly) {
        getElement().setProperty("readonly", readonly);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Set to true if the value is invalid.
     * <p>
     * This property is synchronized automatically from client side when a
     * 'invalid-changed' event happens.
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
     * Set to true if the value is invalid.
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
     * Minimum time allowed.
     * </p>
     * <p>
     * Supported time formats are in ISO 8601:
     * </p>
     * <ul>
     * <li>{@code hh:mm}</li>
     * <li>{@code hh:mm:ss}</li>
     * <li>{@code hh:mm:ss.fff}
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.</li>
     * </ul>
     *
     * @return the {@code min} property from the webcomponent
     */
    protected String getMinString() {
        return getElement().getProperty("min");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Minimum time allowed.
     * </p>
     * <p>
     * Supported time formats are in ISO 8601:
     * </p>
     * <ul>
     * <li>{@code hh:mm}</li>
     * <li>{@code hh:mm:ss}</li>
     * <li>{@code hh:mm:ss.fff}</li>
     * </ul>
     *
     * @param min
     *            the String value to set
     */
    protected void setMin(String min) {
        getElement().setProperty("min", min == null ? "" : min);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Maximum time allowed.
     * </p>
     * <p>
     * Supported time formats are in ISO 8601:
     * </p>
     * <ul>
     * <li>{@code hh:mm}</li>
     * <li>{@code hh:mm:ss}</li>
     * <li>{@code hh:mm:ss.fff}
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.</li>
     * </ul>
     *
     * @return the {@code max} property from the webcomponent
     */
    protected String getMaxString() {
        return getElement().getProperty("max");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Maximum time allowed.
     * </p>
     * <p>
     * Supported time formats are in ISO 8601:
     * </p>
     * <ul>
     * <li>{@code hh:mm}</li>
     * <li>{@code hh:mm:ss}</li>
     * <li>{@code hh:mm:ss.fff}</li>
     * </ul>
     *
     * @param max
     *            the String value to set
     */
    protected void setMax(String max) {
        getElement().setProperty("max", max == null ? "" : max);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specifies the number of valid intervals in a day used for configuring the
     * items displayed in the selection box.
     * </p>
     * <p>
     * It also configures the precision of the value string. By default the
     * component formats values as {@code hh:mm} but setting a step value lower
     * than one minute or one second, format resolution changes to
     * {@code hh:mm:ss} and {@code hh:mm:ss.fff} respectively.
     * </p>
     * <p>
     * Unit must be set in seconds, and for correctly configuring intervals in
     * the dropdown, it need to evenly divide a day.
     * </p>
     * <p>
     * Note: it is possible to define step that is dividing an hour in inexact
     * fragments (i.e. 5760 seconds which equals 1 hour 36 minutes), but it is
     * not recommended to use it for better UX experience.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code step} property from the webcomponent
     */
    protected double getStepDouble() {
        return getElement().getProperty("step", 0.0);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specifies the number of valid intervals in a day used for configuring the
     * items displayed in the selection box.
     * </p>
     * <p>
     * It also configures the precision of the value string. By default the
     * component formats values as {@code hh:mm} but setting a step value lower
     * than one minute or one second, format resolution changes to
     * {@code hh:mm:ss} and {@code hh:mm:ss.fff} respectively.
     * </p>
     * <p>
     * Unit must be set in seconds, and for correctly configuring intervals in
     * the dropdown, it need to evenly divide a day.
     * </p>
     * <p>
     * Note: it is possible to define step that is dividing an hour in inexact
     * fragments (i.e. 5760 seconds which equals 1 hour 36 minutes), but it is
     * not recommended to use it for better UX experience.
     * </p>
     *
     * @param step
     *            the double value to set
     */
    protected void setStep(double step) {
        getElement().setProperty("step", step);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * The object used to localize this component. To change the default
     * localization, replace the entire <em>i18n</em> object or just the
     * property you want to modify.
     * </p>
     * <p>
     * The object has the following JSON structure:
     * </p>
     *
     * <pre>
     * <code>            {
     * 	              // A function to format given {@code Object} as
     * 	              // time string. Object is in the format {@code { hours: ..., minutes: ..., seconds: ..., milliseconds: ... }}
     * 	              formatTime: (time) =&gt; {
     * 	                // returns a string representation of the given
     * 	                // object in {@code hh} / 'hh:mm' / 'hh:mm:ss' / 'hh:mm:ss.fff' - formats
     * 	              },
     *
     * 	              // A function to parse the given text to an {@code Object} in the format
     * 	              // {@code { hours: ..., minutes: ..., seconds: ..., milliseconds: ... }}.
     * 	              // Must properly parse (at least) text
     * 	              // formatted by {@code formatTime}.
     * 	              parseTime: text =&gt; {
     * 	                // Parses a string in object/string that can be formatted by{@code formatTime}.
     * 	              }
     *
     * 	              // Translation of the time selector icon button title.
     * 	              selector: 'Time selector',
     *
     * 	              // Translation of the time selector clear button title.
     * 	              clear: 'Clear'
     * 	            }
     * 	          &lt;p&gt;This property is not synchronized automatically from the client side, so the returned value may not be the same as in client side.
     * 	</code>
     * </pre>
     *
     * @return the {@code i18n} property from the webcomponent
     */
    protected JsonObject getI18nJsonObject() {
        return (JsonObject) getElement().getPropertyRaw("i18n");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * The object used to localize this component. To change the default
     * localization, replace the entire <em>i18n</em> object or just the
     * property you want to modify.
     * </p>
     * <p>
     * The object has the following JSON structure:
     * </p>
     *
     * <pre>
     * <code>            {
     * 	              // A function to format given {@code Object} as
     * 	              // time string. Object is in the format {@code { hours: ..., minutes: ..., seconds: ..., milliseconds: ... }}
     * 	              formatTime: (time) =&gt; {
     * 	                // returns a string representation of the given
     * 	                // object in {@code hh} / 'hh:mm' / 'hh:mm:ss' / 'hh:mm:ss.fff' - formats
     * 	              },
     *
     * 	              // A function to parse the given text to an {@code Object} in the format
     * 	              // {@code { hours: ..., minutes: ..., seconds: ..., milliseconds: ... }}.
     * 	              // Must properly parse (at least) text
     * 	              // formatted by {@code formatTime}.
     * 	              parseTime: text =&gt; {
     * 	                // Parses a string in object/string that can be formatted by{@code formatTime}.
     * 	              }
     *
     * 	              // Translation of the time selector icon button title.
     * 	              selector: 'Time selector',
     *
     * 	              // Translation of the time selector clear button title.
     * 	              clear: 'Clear'
     * 	            }
     * 	</code>
     * </pre>
     *
     * @param i18n
     *            the JsonObject value to set
     */
    protected void setI18n(JsonObject i18n) {
        getElement().setPropertyJson("i18n", i18n);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Returns true if {@code value} is valid, and sets the {@code invalid} flag
     * appropriately.
     * </p>
     * <p>
     * This function is not supported by Flow because it returns a
     * <code>boolean</code>. Functions with return types different than void are
     * not supported at this moment.
     */
    @NotSupported
    protected void validate() {
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
     * You can override the {@code checkValidity} method for custom validations.
     * </p>
     */
    protected void checkValidity() {
        getElement().callJsFunction("checkValidity");
    }

    public static class InvalidChangeEvent<R extends GeneratedVaadinTimePicker<R, ?>>
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
     * Adds a listener for {@code invalid-changed} events fired by the
     * webcomponent.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    protected Registration addInvalidChangeListener(
            ComponentEventListener<InvalidChangeEvent<R>> listener) {
        return getElement()
                .addPropertyChangeListener("invalid",
                        event -> listener.onComponentEvent(
                                new InvalidChangeEvent<R>((R) this,
                                        event.isUserOriginated())));
    }

    /**
     * Constructs a new GeneratedVaadinTimePicker component with the given
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
     *            ignored and the initial value is set <<<<<<< HEAD
     * @param <P>
     *            the property type
     */
    public <P> GeneratedVaadinTimePicker(T initialValue, T defaultValue,
            Class<P> elementPropertyType,
            SerializableFunction<P, T> presentationToModel,
            SerializableFunction<T, P> modelToPresentation,
            boolean isInitialValueOptional) {
        super("value", defaultValue, elementPropertyType, presentationToModel,
                modelToPresentation);
        // Only apply initial value if the element does not already have a value
        // (this can be the case when binding to an existing element from a Lit
        // template), or if isInitialValueOptional enforces setting the initial
        // value, which is the case when calling a TimePicker constructor with a
        // custom initial value.
        if ((getElement().getProperty("value") == null
                || !isInitialValueOptional)) {
            setPresentationValue(initialValue);
        }
    }

    /**
     * Constructs a new GeneratedVaadinTimePicker component with the given
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
    public <P> GeneratedVaadinTimePicker(T initialValue, T defaultValue,
            Class<P> elementPropertyType,
            SerializableFunction<P, T> presentationToModel,
            SerializableFunction<T, P> modelToPresentation) {
        this(initialValue, defaultValue, elementPropertyType,
                presentationToModel, modelToPresentation, false);
    }

    /**
     * Constructs a new GeneratedVaadinTimePicker component with the given
     * arguments.
     *
     * @param initialValue
     *            the initial value to set to the value
     * @param defaultValue
     *            the default value to use if the value isn't defined
     * @param acceptNullValues
     *            whether <code>null</code> is accepted as a model value
     */
    public GeneratedVaadinTimePicker(T initialValue, T defaultValue,
            boolean acceptNullValues) {
        super("value", defaultValue, acceptNullValues);
        setPresentationValue(initialValue);
    }

    /**
     * Constructs a new GeneratedVaadinTimePicker component with the given
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
    public <P> GeneratedVaadinTimePicker(T initialValue, T defaultValue,
            Class<P> elementPropertyType,
            SerializableBiFunction<R, P, T> presentationToModel,
            SerializableBiFunction<R, T, P> modelToPresentation) {
        super("value", defaultValue, elementPropertyType, presentationToModel,
                modelToPresentation);
        setPresentationValue(initialValue);
    }

    /**
     * Default constructor.
     */
    public GeneratedVaadinTimePicker() {
        this(null, null, null, (SerializableFunction) null,
                (SerializableFunction) null, false);
    }
}
