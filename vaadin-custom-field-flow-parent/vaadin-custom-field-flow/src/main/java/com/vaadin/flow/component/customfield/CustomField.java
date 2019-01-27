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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.HtmlImport;

@Tag("vaadin-custom-field")
@HtmlImport("frontend://bower_components/vaadin-custom-field/src/vaadin-custom-field.html")
public abstract class CustomField<T> extends AbstractField<CustomField<T>, T>
    implements HasComponents, HasSize, HasValidation, Focusable<CustomField> {

    /**
     * Constructs a new custom field.
     *
     * @see AbstractField#AbstractField(Object)
     */
    public CustomField(T defaultValue) {
        super(defaultValue);
    }

    /**
     * Specifies that the user must fill in a value.
     *
     * @return the {@code required} property from the webcomponent
     */
    public boolean isRequired() {
        return getElement().getProperty("required", false);
    }

    public void setRequired(boolean required) {
        getElement().setProperty("required", required);
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
    public void setInvalid(boolean invalid) {
        getElement().setProperty("invalid", invalid);
    }

    /**
     * <p>
     * This property is set in the frontend when the inputs change value
     * <p>
     * This property is synchronized automatically from client side when a
     * 'value-changed' event happens.
     * </p>
     *
     * @return the {@code invalid} property from the webcomponent
     */
    @Synchronize(property = "value", value = "value-changed")
    public String getFrontendValue() {
        return getElement().getProperty("value");
    }

    /**
     * <p>
     * This property is set to true when the control value is invalid.
     * </p>
     *
     * @param value the value to set
     */
    public void setFrontendValue(String value) {
        getElement().setProperty("value", value != null ? value : "");
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

    @DomEvent("change")
    public static class CustomFieldChangeEvent extends ComponentEvent<CustomField<?>> {

        private final String value;

        public CustomFieldChangeEvent(CustomField<?> source, boolean fromClient,@EventData("event.detail.value") String value) {
            super(source, fromClient);
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

