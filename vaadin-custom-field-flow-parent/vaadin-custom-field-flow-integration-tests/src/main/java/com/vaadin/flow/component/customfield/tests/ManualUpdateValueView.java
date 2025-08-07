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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;

@Route("vaadin-custom-field/manual")
public class ManualUpdateValueView extends Div {
    public ManualUpdateValueView() {
        final Div result = new Div();
        result.setId("result");
        ManualCustomField customField = new ManualCustomField();
        NativeButton button = new NativeButton("Update",
                e -> customField.updateValue());
        customField
                .addValueChangeListener(e -> result.setText("" + e.getValue()));
        add(customField, result, button);
    }

    private class ManualCustomField extends CustomField<Integer> {
        final IntegerField field1 = new IntegerField();
        final IntegerField field2 = new IntegerField();
        final Div updateValueCounter = new Div();
        int valueChangeCount = 0;

        ManualCustomField() {
            super(null, true);
            field1.setId("field1");
            field2.setId("field2");
            add(updateValueCounter, field1, field2);
            updateValueCounter.setId("updateValueCounter");
            updateValueCounter.setText("0");
        }

        @Override
        protected Integer generateModelValue() {
            int i1 = field1.getValue();
            int i2 = field2.getValue();
            return i1 + i2;
        }

        @Override
        protected void updateValue() {
            super.updateValue();
            valueChangeCount++;
            updateValueCounter.setText("" + valueChangeCount);
        }

        @Override
        protected void setPresentationValue(Integer integer) {

        }
    }
}
