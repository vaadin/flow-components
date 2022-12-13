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
package com.vaadin.tests.validation;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;

public abstract class AbstractValidationPage<T extends Component & HasValidation>
        extends Div {
    public static final String SERVER_VALIDITY_STATE = "server-validity-state";
    public static final String SERVER_VALIDITY_STATE_BUTTON = "server-validity-state-button";

    public static final String ATTACH_FIELD_BUTTON = "attach-field-button";
    public static final String DETACH_FIELD_BUTTON = "detach-field-button";

    protected T testField;

    protected AbstractValidationPage() {
        testField = createTestField();
        add(testField);

        addServerValidityStateControls();
        addAttachDetachControls();
    }

    private void addServerValidityStateControls() {
        Div validityState = new Div();
        validityState.setId(SERVER_VALIDITY_STATE);

        NativeButton validityStateButton = createButton(
                SERVER_VALIDITY_STATE_BUTTON, "Retrieve server validity state",
                event -> {
                    boolean isValid = !testField.isInvalid();
                    validityState.setText(String.valueOf(isValid));
                });

        add(new Div(validityState, validityStateButton));
    }

    private void addAttachDetachControls() {
        NativeButton attachButton = createButton(ATTACH_FIELD_BUTTON,
                "Attach field", event -> add(testField));
        NativeButton detachButton = createButton(DETACH_FIELD_BUTTON,
                "Detach field", event -> remove(testField));

        add(new Div(attachButton, detachButton));
    }

    /**
     * A helper to create a native button element.
     */
    protected NativeButton createButton(String id, String title,
            ComponentEventListener<ClickEvent<NativeButton>> listener) {
        NativeButton button = new NativeButton(title, listener);
        button.setId(id);
        return button;
    }

    /**
     * A helper to create a native input element.
     */
    protected Input createInput(String id, String placeholder,
            ValueChangeListener<? super ComponentValueChangeEvent<Input, String>> listener) {
        Input input = new Input();
        input.setId(id);
        input.setPlaceholder(placeholder);
        input.addValueChangeListener(listener);
        return input;
    }

    /**
     * A field to test.
     */
    protected abstract T createTestField();
}
