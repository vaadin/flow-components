/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;

@TestPath("vaadin-radio-button/helper")
public class HelperIT extends AbstractComponentIT {

    /**
     * Assert that helper component exists after setItems. This issue is similar
     * to https://github.com/vaadin/vaadin-checkbox/issues/191
     */
    @Test
    public void assertHelperComponentExists() {
        open();
        TestBenchElement radioGroup = $("vaadin-radio-group").first();

        TestBenchElement helperComponent = radioGroup.$("span")
                .attributeContains("slot", "helper").first();
        Assert.assertEquals("Helper text", helperComponent.getText());

    }
}
