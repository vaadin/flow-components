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
import com.vaadin.testbench.elementsbase.Element;

/**
 * TestBench element for the <code>&lt;vaadin-login-form&gt;</code> element
 */
@Element("vaadin-login-form")
public class LoginFormElement extends TestBenchElement implements Login {

    @Override
    public TextFieldElement getUsernameField() {
        return $(TextFieldElement.class).id("vaadinLoginUsername");
    }

    @Override
    public PasswordFieldElement getPasswordField() {
        return $(PasswordFieldElement.class).id("vaadinLoginPassword");
    }

    @Override
    public ButtonElement getSubmitButton() {
        return $(ButtonElement.class).attribute("part", "vaadin-login-submit")
                .first();
    }

    private TestBenchElement getFormWrapper() {
        return $("vaadin-login-form-wrapper").first();
    }

    @Override
    public ButtonElement getForgotPasswordButton() {
        return getFormWrapper().$(ButtonElement.class)
                .id("forgotPasswordButton");
    }

    @Override
    public void submit() {
        getSubmitButton().click();
    }

    @Override
    public void forgotPassword() {
        getForgotPasswordButton().click();
    }

    @Override
    public String getFormTitle() {
        return getFormWrapper().$(TestBenchElement.class)
                .attribute("part", "form").first().$("h2").first().getText();
    }

    @Override
    public TestBenchElement getErrorComponent() {
        return getFormWrapper().$(TestBenchElement.class)
                .attribute("part", "error-message").first();
    }

    @Override
    public String getErrorMessageTitle() {
        return getErrorComponent().$("h5").first().getText();
    }

    @Override
    public String getErrorMessage() {
        return getErrorComponent().$("p").first().getText();
    }

    @Override
    public String getAdditionalInformation() {
        return getFormWrapper().$(TestBenchElement.class)
                .attribute("part", "footer").first().$("p").first().getText();
    }

    @Override
    public boolean isEnabled() {
        return !Boolean.TRUE.equals(getPropertyBoolean("disabled"))
                && super.isEnabled();
    }
}
