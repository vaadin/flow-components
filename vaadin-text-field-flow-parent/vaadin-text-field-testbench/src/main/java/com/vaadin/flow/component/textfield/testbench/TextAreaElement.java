/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.testbench;

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
    @Override
    public void setValue(String string) {
        TextFieldElementHelper.setValue(getInputElement(), string);
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        getInputElement().sendKeys(keysToSend);
    }

    private TestBenchElement getInputElement() {
        return (TestBenchElement) getProperty("inputElement");
    }

}
