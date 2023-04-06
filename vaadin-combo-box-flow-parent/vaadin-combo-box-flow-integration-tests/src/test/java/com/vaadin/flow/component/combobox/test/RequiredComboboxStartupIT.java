
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;

@TestPath("vaadin-combo-box/required-combobox-startup")
public class RequiredComboboxStartupIT extends AbstractComponentIT {

    @Test
    public void serverSideValidation_persistsAtStartup() {
        open();

        ComboBoxElement comboBox = $(ComboBoxElement.class).first();

        Assert.assertEquals(Boolean.TRUE.toString(),
                comboBox.getAttribute("invalid"));

        TestBenchElement error = comboBox.$("[part='error-message']").first();

        Assert.assertTrue(error.getSize().getHeight() > 0);
        Assert.assertEquals("Must be false", error.getText());

        try {
            // This should timeout and the validation error preserved on screen.
            waitUntil(input -> error.getSize().getHeight() == 0, 3);

            Assert.fail(
                    "Validation error message was dismissed while it should have remained visible");

        } catch (TimeoutException e) {
            // Validation error message is still visible after the timeout and
            // wasn't hidden. This is expected since the input field is not
            // valid.
        }

    }

}
