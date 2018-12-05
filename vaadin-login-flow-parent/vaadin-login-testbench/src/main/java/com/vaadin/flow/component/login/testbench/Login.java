package com.vaadin.flow.component.login.testbench;

/*
 * #%L
 * Vaadin Login Testbench API
 * %%
 * Copyright (C) 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */


import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;

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
     * Return the title of the login element
     */
    String getTitle();

    /**
     * Return the message under the title of the login element
     */
    String getMessage();

    /**
     * Return the form title of the login element
     */
    String getFormTitle();

    /**
     * Return the error message title. Returns empty string
     * if the error message is not displayed
     */
    String getErrorMessageTitle();

    /**
     * Return the error message text. Returns empty string
     * if the error message is not displayed
     */
    String getErrorMessage();

    /**
     * Return the additional information placed in a footer
     * of the login element
     */
    String getAdditionalInformation();
}
