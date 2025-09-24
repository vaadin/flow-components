/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.customfield.tests;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("vaadin-custom-field")
public class BasicView extends Div {
    public BasicView() {
        final Div result = new Div();
        result.setId("result");
        MyField customField = new MyField();
        NativeButton button = new NativeButton("Update");
        button.setId("button1");
        button.addClickListener(e -> customField.updateValue());
        customField
                .addValueChangeListener(e -> result.setText("" + e.getValue()));
        add(customField, result, button);
    }

    private class MyField extends CustomField<Integer> {
        final TextField field1 = new TextField();
        final TextField field2 = new TextField();

        MyField() {
            field1.setId("field1");
            field2.setId("field2");
            add(field1, field2);

        }

        @Override
        protected Integer generateModelValue() {
            try {
                int i1 = Integer.valueOf(field1.getValue());
                int i2 = Integer.valueOf(field2.getValue());
                return i1 + i2;
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        @Override
        protected void updateValue() {
            super.updateValue();
        }

        @Override
        protected void setPresentationValue(Integer integer) {

        }
    }
}
