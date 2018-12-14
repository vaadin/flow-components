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
import org.openqa.selenium.StaleElementReferenceException;

/**
 * TestBench element for the <code>&lt;vaadin-login-overlay&gt;</code> element
 */
@Element("vaadin-login-overlay")
public class LoginOverlayElement extends TestBenchElement implements Login {

    public TestBenchElement getLoginOverlayElement() {
        return $("vaadin-login-overlay-element").onPage().waitForFirst();
    }

    public LoginElement getLogin() {
        return getLoginOverlayElement().$(LoginElement.class).first();
    }

    public boolean isOpened() {
        try {
            return getPropertyBoolean("opened");
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    @Override
    public TextFieldElement getUsernameField() {
        return getLogin().getUsernameField();
    }

    @Override
    public PasswordFieldElement getPasswordField() {
        return getLogin().getPasswordField();
    }

    @Override
    public ButtonElement getSubmitButton() {
        return getLogin().getSubmitButton();
    }

    @Override
    public ButtonElement getForgotPasswordButton() {
        return getLogin().getForgotPasswordButton();
    }

    @Override
    public void submit() {
        getLogin().getSubmitButton().click();
    }

    @Override
    public void forgotPassword() {
        getLogin().getForgotPasswordButton().click();
    }

    @Override
    public String getTitle() {
        return getLogin().getTitle();
    }

    @Override
    public String getMessage() {
        return getLogin().getMessage();
    }

    @Override
    public String getFormTitle() {
        return getLogin().getFormTitle();
    }

    @Override
    public String getErrorMessageTitle() {
        return getLogin().getErrorMessageTitle();
    }

    @Override
    public String getErrorMessage() {
        return getLogin().getErrorMessage();
    }

    @Override
    public String getAdditionalInformation() {
        return getLogin().getAdditionalInformation();
    }
}
