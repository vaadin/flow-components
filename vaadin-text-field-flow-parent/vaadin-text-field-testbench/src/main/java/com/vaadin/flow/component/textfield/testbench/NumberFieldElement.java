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
 * A TestBench element representing a <code>&lt;vaadin-number-field&gt;</code>
 * element.
 */
@Element("vaadin-number-field")
public class NumberFieldElement extends TestBenchElement
        implements HasStringValueProperty, HasLabel, HasPlaceholder, HasHelper {

    /**
     * Emulates the user setting the value and pressing Enter. In practice, this
     * updates the value property of the input element to the given value and
     * then triggers {@code input}, {@code keydown}, and {@code change} DOM
     * events.
     * <p>
     * This method is intended for basic cases where you just need to get the
     * field's value updated. If you want to simulate real user input with the
     * keyboard, use {@link #sendKeys(CharSequence...)}.
     * <p>
     * WARNING: Setting values that aren't parsable into a number is not
     * supported. If you need to enter such values e.g. to test the validation
     * workflow, use {@link #sendKeys(CharSequence...)} instead.
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
