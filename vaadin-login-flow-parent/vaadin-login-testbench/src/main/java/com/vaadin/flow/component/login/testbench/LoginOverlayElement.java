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
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.StaleElementReferenceException;

/**
 * TestBench element for the <code>&lt;vaadin-login-overlay&gt;</code> element
 */
@Element("vaadin-login-overlay")
public class LoginOverlayElement extends TestBenchElement implements Login {

    /**
     * Returns the container of the branding and form area
     */
    public TestBenchElement getLoginOverlayWrapper() {
        return $("vaadin-login-overlay-wrapper").onPage().waitForFirst();
    }

    /**
     * Returns the login form, the actual container of native html form
     */
    public LoginFormElement getLoginForm() {
        return getLoginOverlayWrapper().$(LoginFormElement.class).first();
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
        return getLoginForm().getUsernameField();
    }

    @Override
    public PasswordFieldElement getPasswordField() {
        return getLoginForm().getPasswordField();
    }

    @Override
    public ButtonElement getSubmitButton() {
        return getLoginForm().getSubmitButton();
    }

    @Override
    public ButtonElement getForgotPasswordButton() {
        return getLoginForm().getForgotPasswordButton();
    }

    @Override
    public TestBenchElement getErrorComponent() {
        return getLoginForm().getErrorComponent();
    }

    @Override
    public void submit() {
        getSubmitButton().click();
    }

    @Override
    public void forgotPassword() {
        getForgotPasswordButton().click();
    }

    /**
     * Returns the title displayed in the login overlay element
     */
    public String getTitle() {
        if (hasTitleComponent()) {
            return getTitleComponent().getText();
        }
        return getLoginOverlayWrapper().$(TestBenchElement.class)
                .attribute("part", "brand").first().$("h1").first()
                // Using textContent, since getText() works unpredictable in
                // Edge
                .getPropertyString("textContent");
    }

    /**
     * Returns the description displayed in the login element
     */
    public String getDescription() {
        return getLoginOverlayWrapper().$(TestBenchElement.class)
                .attribute("part", "brand").first().$("p").first().getText();
    }

    /**
     * Checks if anything was set into the title slot
     */
    public boolean hasTitleComponent() {
        return getLoginOverlayWrapper().$(TestBenchElement.class)
                .attribute("slot", "title").exists();
    }

    /**
     * Returns the title component which is set into the title slot of the login
     * element. If was not set returns <code>null</code>
     */
    public TestBenchElement getTitleComponent() {
        if (!hasTitleComponent()) {
            return null;
        }
        return getLoginOverlayWrapper().$(TestBenchElement.class)
                .attribute("slot", "title").first();
    }

    @Override
    public String getFormTitle() {
        return getLoginForm().getFormTitle();
    }

    @Override
    public String getErrorMessageTitle() {
        return getLoginForm().getErrorMessageTitle();
    }

    @Override
    public String getErrorMessage() {
        return getLoginForm().getErrorMessage();
    }

    @Override
    public String getAdditionalInformation() {
        return getLoginForm().getAdditionalInformation();
    }

    @Override
    public boolean isEnabled() {
        return getLoginForm().isEnabled();
    }
}
