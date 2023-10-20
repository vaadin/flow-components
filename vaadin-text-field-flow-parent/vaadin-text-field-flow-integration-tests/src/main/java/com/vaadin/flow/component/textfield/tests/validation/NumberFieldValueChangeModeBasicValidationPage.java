/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-number-field/validation/value-change-mode/basic")
public class NumberFieldValueChangeModeBasicValidationPage
        extends AbstractValidationPage<NumberField> {

    public static final String SET_EAGER_MODE_BUTTON = "set-eager-mode-button";
    public static final String SET_LAZY_MODE_BUTTON = "set-lazy-mode-button";
    public static final String SET_TIMEOUT_MODE_BUTTON = "set-timeout-mode-button";

    public static final String SERVER_VALIDITY_STATE_LOG = "server-validity-state-log";
    public static final String RESET_SERVER_VALIDITY_STATE_LOG_BUTTON = "reset-server-validity-state-log-button";

    private Div serverValidityStateLog;

    public NumberFieldValueChangeModeBasicValidationPage() {
        super();

        add(createButton(SET_EAGER_MODE_BUTTON, "Set eager mode", event -> {
            testField.setValueChangeMode(ValueChangeMode.EAGER);
        }));

        add(createButton(SET_LAZY_MODE_BUTTON, "Set lazy mode", event -> {
            testField.setValueChangeMode(ValueChangeMode.LAZY);
        }));

        add(createButton(SET_TIMEOUT_MODE_BUTTON, "Set timeout mode", event -> {
            testField.setValueChangeMode(ValueChangeMode.TIMEOUT);
        }));

        addServerValidityLog();
    }

    private void addServerValidityLog() {
        serverValidityStateLog = new Div();
        serverValidityStateLog.setId(SERVER_VALIDITY_STATE_LOG);

        NativeButton resetServerValidityStateLogButton = createButton(RESET_SERVER_VALIDITY_STATE_LOG_BUTTON, "Reset server validity log", event -> {
            resetServerValidityLog();
        });

        add(new Div(serverValidityStateLog, resetServerValidityStateLogButton));
    }

    private void logServerValidityStateChange(boolean isInvalid) {
        Div record = new Div();
        record.setText(isInvalid ? "invalid" : "valid");
        serverValidityStateLog.add(record);
    }

    private void resetServerValidityLog() {
        serverValidityStateLog.removeAll();
    }

    @Override
    protected NumberField createTestField() {
        return new NumberField() {
            @Override
            protected void validate() {
                super.validate();
                logServerValidityStateChange(isInvalid());
            }
        };
    }
}
