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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link TextField}.
 */
@Route("vaadin-text-field/text-field-detach-attach")
public class TextFieldDetachAttachPage extends Div {

    /**
     * Constructs a basic layout with a text field.
     */
    public TextFieldDetachAttachPage() {
        TextField textField = new TextField();
        textField.setRequiredIndicatorVisible(true);
        textField.setId("text-field");
        add(textField);

        NativeButton toggleAttached = new NativeButton("toggle attached", e -> {
            if (textField.getParent().isPresent()) {
                remove(textField);
            } else {
                add(textField);
            }
        });
        toggleAttached.setId("toggle-attached");
        add(toggleAttached);
    }
}
