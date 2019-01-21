package com.vaadin.flow.component.login.testbench;

/*
 * #%L
 * Vaadin Login Testbench API
 * %%
 * Copyright (C) 2018 Vaadin Ltd
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
import com.vaadin.testbench.elementsbase.Element;

/**
 * TestBench element for the <code>&lt;vaadin-login&gt;</code> element
 */
@Element("vaadin-login")
public class LoginElement extends TestBenchElement implements Login {

    @Override
    public TextFieldElement getUsernameField() {
        return $(TextFieldElement.class).id("username");
    }

    @Override
    public PasswordFieldElement getPasswordField() {
        return $(PasswordFieldElement.class).id("password");
    }

    @Override
    public ButtonElement getSubmitButton() {
        return $(ButtonElement.class).id("submit");
    }

    @Override
    public ButtonElement getForgotPasswordButton() {
        return $(ButtonElement.class).id("forgotPasswordButton");
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
        return $(TestBenchElement.class)
                .attribute("part", "form").first().$("h2").first().getText();
    }

    @Override
    public String getErrorMessageTitle() {
        return $(TestBenchElement.class)
                .attribute("part", "error-message").first().$("h5").first().getText();
    }

    @Override
    public String getErrorMessage() {
        return $(TestBenchElement.class)
                .attribute("part", "error-message").first().$("p").first().getText();
    }

    @Override
    public String getAdditionalInformation() {
        return $(TestBenchElement.class)
                .attribute("part", "footer").first().$("p").first().getText();
    }
}
