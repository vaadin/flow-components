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
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;

/**
 * Classifies a {@link HasValue} field component into the
 * {@link FormAIController}'s internal type taxonomy.
 */
enum FormFieldType {
    STRING, EMAIL, BIG_DECIMAL, NUMBER, INTEGER, BOOLEAN, DATE, DATE_TIME, TIME,
    SINGLE_SELECT, MULTI_SELECT, UNSUPPORTED;

    /**
     * Pattern accepted for {@link BigDecimalField} string payloads. Rejects
     * locale separators and scientific notation before the LLM's payload
     * reaches {@code new BigDecimal(...)}.
     */
    static final String BIG_DECIMAL_PATTERN = "^-?\\d+(\\.\\d+)?$";

    static FormFieldType classify(HasValue<?, ?> field) {
        if (field instanceof PasswordField) {
            return UNSUPPORTED;
        }
        if (field instanceof EmailField) {
            return EMAIL;
        }
        if (field instanceof BigDecimalField) {
            return BIG_DECIMAL;
        }
        if (field instanceof IntegerField) {
            return INTEGER;
        }
        if (field instanceof NumberField) {
            return NUMBER;
        }
        if (field instanceof Checkbox) {
            return BOOLEAN;
        }
        if (field instanceof DateTimePicker) {
            return DATE_TIME;
        }
        if (field instanceof DatePicker) {
            return DATE;
        }
        if (field instanceof TimePicker) {
            return TIME;
        }
        if (field instanceof TextArea || field instanceof TextField) {
            return STRING;
        }
        if (field instanceof MultiSelectComboBox<?>
                || field instanceof CheckboxGroup<?>) {
            return MULTI_SELECT;
        }
        if (field instanceof ComboBox<?> || field instanceof Select<?>
                || field instanceof RadioButtonGroup<?>) {
            return SINGLE_SELECT;
        }
        // CustomField is auto-skipped — detected by class hierarchy walk
        // without requiring a compile-time dependency on its module.
        if (isAssignableTo(field.getClass(),
                "com.vaadin.flow.component.customfield.CustomField")) {
            return UNSUPPORTED;
        }
        return STRING;
    }

    private static boolean isAssignableTo(Class<?> type, String superTypeName) {
        for (Class<?> c = type; c != null && c != Object.class; c = c
                .getSuperclass()) {
            if (c.getName().equals(superTypeName)) {
                return true;
            }
        }
        return false;
    }
}
