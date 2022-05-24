package com.vaadin.flow.component.login.testbench;

/*
 * #%L
 * Vaadin Login Testbench API
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;

interface Login {

    /**
     * Return the username field
     */
    TextFieldElement getUsernameField();

    /**
     * Return the password field
     */
    PasswordFieldElement getPasswordField();

    /**
     * Return the log in button
     */
    ButtonElement getSubmitButton();

    /**
     * Provide a shortcut for clicking the submit button
     */
    void submit();

    /**
     * Return the forgot password button
     */
    ButtonElement getForgotPasswordButton();

    /**
     * Provide a shortcut for clicking the forgot password button
     */
    void forgotPassword();

    /**
     * Return the form title of the login element
     */
    String getFormTitle();

    /**
     * Return the error component
     */
    TestBenchElement getErrorComponent();

    /**
     * Return the error message title. Returns empty string if the error message
     * is not displayed
     */
    String getErrorMessageTitle();

    /**
     * Return the error message text. Returns empty string if the error message
     * is not displayed
     */
    String getErrorMessage();

    /**
     * Return the additional information placed in a footer of the login element
     */
    String getAdditionalInformation();
}
