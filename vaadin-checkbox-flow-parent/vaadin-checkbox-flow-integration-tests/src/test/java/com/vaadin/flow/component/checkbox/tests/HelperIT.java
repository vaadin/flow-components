/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;

@TestPath("vaadin-checkbox/helper")
public class HelperIT extends AbstractComponentIT {

    /**
     * Assert that helper component exists after setItems.
     * https://github.com/vaadin/vaadin-checkbox/issues/191
     */
    @Test
    public void assertHelperComponentExists() {
        open();
        TestBenchElement checkboxGroup = $("vaadin-checkbox-group").first();

        TestBenchElement helperComponent = checkboxGroup.$("span")
                .attributeContains("slot", "helper").first();
        Assert.assertEquals("Helper text", helperComponent.getText());

    }
}
