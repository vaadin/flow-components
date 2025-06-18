/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-checkbox/injected-checkbox")
public class InjectedCheckboxIT extends AbstractComponentIT {

    @Test
    public void initialCheckboxValue() {
        open();

        TestBenchElement checkbox = $("inject-checkbox").first()
                .$("vaadin-checkbox").first();

        String isChecked = checkbox.getAttribute("checked");
        Assert.assertEquals(Boolean.TRUE.toString(), isChecked);

        Assert.assertEquals("Accept",
                checkbox.getPropertyString("textContent").trim());
    }
}
