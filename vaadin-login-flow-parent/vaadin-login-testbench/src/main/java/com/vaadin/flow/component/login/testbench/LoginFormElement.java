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
        return $(ButtonElement.class).first();
    }

    private TestBenchElement getFormWrapper() {
        return $("vaadin-login-form-wrapper").first();
    }

    @Override
    public ButtonElement getForgotPasswordButton() {
        return $(ButtonElement.class).withAttribute("slot", "forgot-password")
                .first();
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
                .withAttribute("part", "form-title").first().getText();
    }

    @Override
    public TestBenchElement getErrorComponent() {
        return getFormWrapper().$(TestBenchElement.class)
                .withAttribute("part", "error-message").first();
    }

    @Override
    public String getErrorMessageTitle() {
        return getErrorComponent().$(TestBenchElement.class)
                .withAttribute("part", "error-message-title").first().getText();
    }

    @Override
    public String getErrorMessage() {
        return getErrorComponent().$(TestBenchElement.class)
                .withAttribute("part", "error-message-description").first()
                .getText();
    }

    @Override
    public String getAdditionalInformation() {
        return getFormWrapper().$(TestBenchElement.class)
                .withAttribute("part", "footer").first().$("div").first()
                .getText();
    }

    @Override
    public boolean isEnabled() {
        return !Boolean.TRUE.equals(getPropertyBoolean("disabled"))
                && super.isEnabled();
    }
}
