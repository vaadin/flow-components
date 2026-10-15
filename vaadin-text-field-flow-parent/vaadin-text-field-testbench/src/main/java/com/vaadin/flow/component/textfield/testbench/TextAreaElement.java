/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
 * A TestBench element representing a <code>&lt;vaadin-text-area&gt;</code>
 * element.
 */
@Element("vaadin-text-area")
public class TextAreaElement extends TestBenchElement
        implements HasStringValueProperty, HasLabel, HasPlaceholder, HasHelper {
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
