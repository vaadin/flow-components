/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextFieldBase;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.tests.validation.AbstractValidationPage;

public abstract class AbstractValueChangeModeValidationPage<T extends TextFieldBase>
        extends AbstractValidationPage<T> {

    public static final String SET_EAGER_MODE_BUTTON = "set-eager-mode-button";
    public static final String SET_LAZY_MODE_BUTTON = "set-lazy-mode-button";
    public static final String SET_TIMEOUT_MODE_BUTTON = "set-timeout-mode-button";

    public static final String VALIDATION_LOG = "validation-log";
    public static final String RESET_VALIDATION_LOG_BUTTON = "reset-validation-log-button";

    public static final int VALUE_CHANGE_TIMEOUT = 600;

    private Div validationLog;

    public AbstractValueChangeModeValidationPage() {
        super();

        add(createButton(SET_EAGER_MODE_BUTTON, "Set eager mode", event -> {
            testField.setValueChangeMode(ValueChangeMode.EAGER);
        }));

        add(createButton(SET_LAZY_MODE_BUTTON, "Set lazy mode", event -> {
            testField.setValueChangeMode(ValueChangeMode.LAZY);
            testField.setValueChangeTimeout(VALUE_CHANGE_TIMEOUT);
        }));

        add(createButton(SET_TIMEOUT_MODE_BUTTON, "Set timeout mode", event -> {
            testField.setValueChangeMode(ValueChangeMode.TIMEOUT);
            testField.setValueChangeTimeout(VALUE_CHANGE_TIMEOUT);
        }));

        addValidationLog();
    }

    private void addValidationLog() {
        validationLog = new Div();
        validationLog.setId(VALIDATION_LOG);

        NativeButton resetValidationLogButton = createButton(
                RESET_VALIDATION_LOG_BUTTON, "Reset validation log", event -> {
                    resetValidationLog();
                });

        add(new Div(validationLog, resetValidationLogButton));
    }

    private void resetValidationLog() {
        validationLog.removeAll();
    }

    protected void logValidationResult(boolean isValid) {
        Div record = new Div();
        record.setText(isValid ? "valid" : "invalid");
        validationLog.add(record);
    }
}
