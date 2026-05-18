/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.ai.form;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.selection.MultiSelect;

/**
 * Classifies a {@link HasValue} field component into the
 * {@link FormAIController}'s internal type taxonomy.
 * <p>
 * Detection is done by walking the field's class hierarchy and matching on
 * class names so this module does not need to declare Maven dependencies on
 * every individual Vaadin input component module.
 */
enum FormFieldType {
    STRING,
    EMAIL,
    BIG_DECIMAL,
    NUMBER,
    INTEGER,
    BOOLEAN,
    DATE,
    DATE_TIME,
    TIME,
    SINGLE_SELECT,
    MULTI_SELECT,
    UNSUPPORTED;

    /**
     * Pattern accepted for {@link BIG_DECIMAL} string payloads. Rejects locale
     * separators and scientific notation at the JSON-protocol layer.
     */
    static final String BIG_DECIMAL_PATTERN = "^-?\\d+(\\.\\d+)?$";

    private static final String PKG = "com.vaadin.flow.component.";

    static FormFieldType classify(HasValue<?, ?> field) {
        if (field == null) {
            return UNSUPPORTED;
        }
        var type = field.getClass();
        if (isAssignableTo(type, PKG + "textfield.PasswordField")
                || isAssignableTo(type, PKG + "customfield.CustomField")) {
            return UNSUPPORTED;
        }
        if (isAssignableTo(type, PKG + "textfield.EmailField")) {
            return EMAIL;
        }
        if (isAssignableTo(type, PKG + "textfield.BigDecimalField")) {
            return BIG_DECIMAL;
        }
        if (isAssignableTo(type, PKG + "textfield.IntegerField")) {
            return INTEGER;
        }
        if (isAssignableTo(type, PKG + "textfield.NumberField")) {
            return NUMBER;
        }
        if (isAssignableTo(type, PKG + "checkbox.Checkbox")) {
            return BOOLEAN;
        }
        if (isAssignableTo(type, PKG + "datetimepicker.DateTimePicker")) {
            return DATE_TIME;
        }
        if (isAssignableTo(type, PKG + "datepicker.DatePicker")) {
            return DATE;
        }
        if (isAssignableTo(type, PKG + "timepicker.TimePicker")) {
            return TIME;
        }
        if (isAssignableTo(type, PKG + "textfield.TextArea")
                || isAssignableTo(type, PKG + "textfield.TextField")) {
            return STRING;
        }
        if (field instanceof MultiSelect<?, ?>) {
            return MULTI_SELECT;
        }
        if (isAssignableTo(type, PKG + "combobox.ComboBox")
                || isAssignableTo(type, PKG + "select.Select")
                || isAssignableTo(type, PKG + "radiobutton.RadioButtonGroup")) {
            return SINGLE_SELECT;
        }
        return STRING;
    }

    private static boolean isAssignableTo(Class<?> type, String superTypeName) {
        for (var c = type; c != null
                && c != Object.class; c = c.getSuperclass()) {
            if (c.getName().equals(superTypeName)) {
                return true;
            }
        }
        return false;
    }
}
