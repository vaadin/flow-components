/*
 * Copyright 2000-2021 Vaadin Ltd.
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
 *
 */

package com.vaadin.flow.component.select.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;

@Route("vaadin-select/override-client-validation")
public class OverrideClientValidationPage extends Div {

    public static final String ID_SET_INVALID_BUTTON = "set-invalid-button";
    public static final String ID_LOG_BUTTON = "log-button";
    public static final String ID_RESULT_SPAN = "result-span";

    public OverrideClientValidationPage() {
        Select<String> select = new Select<>("a", "b", "c");
        Span resultSpan = new Span();
        resultSpan.setId(ID_RESULT_SPAN);

        NativeButton setInvalidButton = new NativeButton("Set invalid",
                e -> select.setInvalid(true));
        setInvalidButton.setId(ID_SET_INVALID_BUTTON);

        NativeButton logButton = new NativeButton("Log validation state",
                e -> resultSpan
                        .setText(select.isInvalid() ? "invalid" : "valid"));
        logButton.setId(ID_LOG_BUTTON);

        add(select, resultSpan, setInvalidButton, logButton);
    }
}
