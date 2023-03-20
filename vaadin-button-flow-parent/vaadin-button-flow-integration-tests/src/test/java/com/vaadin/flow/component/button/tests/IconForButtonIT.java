
package com.vaadin.flow.component.button.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-button/icon-button")
public class IconForButtonIT extends AbstractComponentIT {

    @Test
    public void slotAttributeIsNotRemoved() {
        open();

        TestBenchElement button = $("vaadin-button").first();
        TestBenchElement icon = button.$("vaadin-icon").first();
        String slot = icon.getAttribute("slot");

        // self check: this is expected in the initialization and not part of
        // the test
        Assert.assertEquals("prefix", slot);

        button.click();
        // self check: the text is updated.
        Assert.assertEquals("Updated text", button.getText());

        icon = button.$("vaadin-icon").first();
        slot = icon.getAttribute("slot");
        // slot should have the same value after text update
        Assert.assertEquals("prefix", slot);
    }
}
