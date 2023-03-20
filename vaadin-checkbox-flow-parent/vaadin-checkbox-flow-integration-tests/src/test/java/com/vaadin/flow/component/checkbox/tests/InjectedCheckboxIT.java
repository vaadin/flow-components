
package com.vaadin.flow.component.checkbox.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.flow.testutil.TestPath;

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
