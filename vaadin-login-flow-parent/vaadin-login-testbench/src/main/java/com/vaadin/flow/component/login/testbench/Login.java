/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.login.testbench;

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
