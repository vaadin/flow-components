/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield.testbench;

import org.openqa.selenium.By;

import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasLabel;
import com.vaadin.testbench.HasPlaceholder;
import com.vaadin.testbench.HasStringValueProperty;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-password-field&gt;</code>
 * element.
 */
@Element("vaadin-password-field")
public class PasswordFieldElement extends TestBenchElement
        implements HasStringValueProperty, HasLabel, HasPlaceholder, HasHelper {

    /**
     * Checks whether the password is shown in clear text or is hidden from
     * view.
     *
     * @return <code>true</code> if the password is shown in clear text,
     *         <code>false</code> if it is hidden from view
     */
    public boolean isPasswordVisible() {
        return getPropertyBoolean("passwordVisible");
    }

    /**
     * Sets whether the password should be shown in clear text or be hidden from
     * view.
     *
     * @param passwordVisible
     *            <code>true</code> to show the password in clear text,
     *            <code>false</code> to hide the password from view
     */
    public void setPasswordVisible(boolean passwordVisible) {
        callFunction("_setPasswordVisible", passwordVisible);
    }

    /**
     * Emulates the user setting the value. This triggers server value change
     * listeners and validation. The emulation is done by setting the value
     * property of the input element to the given value and then triggering
     * synthetic {@code input}, {@code change}, and {@code focusout} DOM events.
     * <p>
     * For more complex scenarios that require a full browser simulation of
     * typing, use {@link #sendKeys(CharSequence...)} instead.
     *
     * @param string
     *            the value to set
     */
    @Override
    public void setValue(String string) {
        TestBenchElement input = $("input").first();
        TextFieldElementHelper.setValue(input, string);
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        findElement(By.tagName("input")).sendKeys(keysToSend);
    }

}
