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
package com.vaadin.flow.component.textfield.testbench;

import org.openqa.selenium.By;

import com.vaadin.testbench.HasClearButton;
import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasLabel;
import com.vaadin.testbench.HasPlaceholder;
import com.vaadin.testbench.HasStringValueProperty;
import com.vaadin.testbench.HasValidation;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-text-area&gt;</code>
 * element.
 */
@Element("vaadin-text-area")
public class TextAreaElement extends TestBenchElement
        implements HasStringValueProperty, HasLabel, HasPlaceholder, HasHelper,
        HasClearButton, HasValidation {

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
        TestBenchElement textarea = $("textarea").first();
        TextFieldElementHelper.setValue(textarea, string);
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        findElement(By.tagName("textarea")).sendKeys(keysToSend);
    }

}
