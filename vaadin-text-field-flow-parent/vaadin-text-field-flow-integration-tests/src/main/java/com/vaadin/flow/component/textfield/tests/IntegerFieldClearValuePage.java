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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;

@Route("vaadin-integer-field/clear-value")
public class IntegerFieldClearValuePage extends Div {
    public static final String CLEAR_BUTTON = "clear-button";
    public static final String CLEAR_AND_SET_VALUE_BUTTON = "clear-and-set-value-button";

    public IntegerFieldClearValuePage() {
        IntegerField integerField = new IntegerField();

        NativeButton clearButton = new NativeButton("Clear value", event -> {
            integerField.clear();
        });
        clearButton.setId(CLEAR_BUTTON);

        NativeButton clearAndSetValueButton = new NativeButton(
                "Clear and set value", event -> {
                    integerField.clear();
                    integerField.setValue(1234);
                });
        clearAndSetValueButton.setId(CLEAR_AND_SET_VALUE_BUTTON);

        add(integerField, clearButton, clearAndSetValueButton);
    }
}
