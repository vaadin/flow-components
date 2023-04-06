
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
