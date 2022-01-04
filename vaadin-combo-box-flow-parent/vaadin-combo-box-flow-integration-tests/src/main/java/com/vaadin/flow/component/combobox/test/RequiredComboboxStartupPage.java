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
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/required-combobox-startup")
public class RequiredComboboxStartupPage extends Div {

    public RequiredComboboxStartupPage() {
        Binder<TestBean> testBinder = new Binder<>();
        testBinder.setBean(new TestBean());
        ComboBox<Boolean> comboBox = new ComboBox<>();
        comboBox.setItems(true, false);
        Binder.Binding<TestBean, Boolean> comboBoxBinding = testBinder
                .forField(comboBox).withValidator((value, context) -> {
                    if (value) {
                        return ValidationResult.error("Must be false");
                    } else {
                        return ValidationResult.ok();
                    }
                }).asRequired().bind(TestBean::getDob, TestBean::setDob);
        add(comboBox);

        // validator should fail immediately, text field should be invalid
        comboBoxBinding.validate();
    }

    private class TestBean {

        private Boolean dob = true;

        public Boolean getDob() {
            return dob;
        }

        public void setDob(Boolean dob) {
            this.dob = dob;
        }

    }
}
