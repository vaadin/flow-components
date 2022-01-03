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

import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.testutil.ValidationTestView;

/**
 * View for testing validation with {@link PasswordField}.
 */
@Route("vaadin-text-field/passwork-field-validation")
public class PasswordFieldValidationPage extends ValidationTestView {

    @Override
    protected HasValidation getValidationComponent() {
        return new PasswordField();
    }
}
