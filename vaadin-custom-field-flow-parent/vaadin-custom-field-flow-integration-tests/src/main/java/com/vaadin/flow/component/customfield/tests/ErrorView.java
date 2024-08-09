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
package com.vaadin.flow.component.customfield.tests;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-custom-field/error")
public class ErrorView extends Div {
    public ErrorView() {
        CustomField<Integer> customField = new CustomField<Integer>() {
            @Override
            protected Integer generateModelValue() {
                return 0;
            }

            @Override
            protected void setPresentationValue(Integer integer) {
            }
        };
        customField.setLabel("My custom field");
        customField.setErrorMessage("My error message");
        add(customField);
    }
}
