/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("text-field-required-binder")
public class TextFieldRequiredValidationPage
        extends AbstractRequiredValidationPage<TextField> {

    @Override
    protected TextField createTextField() {
        return new TextField();
    }

    @Override
    protected void setRequired(TextField field) {
        field.setRequired(true);
    }

}
