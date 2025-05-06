/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("vaadin-checkbox/validation")
public class ValidationPage extends Div {
    public ValidationPage() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("foo", "bar");
        checkboxGroup.setLabel("CheckboxGroup");

        NativeButton setInvalid = new NativeButton("Set invalid", e -> {
            checkboxGroup.setInvalid(true);
            checkboxGroup.setErrorMessage("Error message from server");
        });
        setInvalid.setId("set-invalid");

        NativeButton attach = new NativeButton("Attach", e -> {
            add(checkboxGroup);
        });
        attach.setId("attach");

        NativeButton detach = new NativeButton("Detach", e -> {
            remove(checkboxGroup);
        });
        detach.setId("detach");

        Span logOutput = new Span();
        logOutput.setId("log-output");
        NativeButton logInvalidState = new NativeButton("Log Invalid State",
                e -> {
                    logOutput
                            .setText(String.valueOf(checkboxGroup.isInvalid()));
                });
        logInvalidState.setId("log-invalid-state");

        add(checkboxGroup);
        add(new Div(setInvalid, attach, detach, logInvalidState, logOutput));
    }
}
