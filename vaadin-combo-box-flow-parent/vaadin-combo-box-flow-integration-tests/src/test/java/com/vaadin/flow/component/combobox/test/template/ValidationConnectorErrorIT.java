
package com.vaadin.flow.component.combobox.test.template;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

/**
 * Test for https://github.com/vaadin/vaadin-combo-box-flow/issues/219
 */
@TestPath("vaadin-combo-box/validation-connector")
public class ValidationConnectorErrorIT extends AbstractComponentIT {

    @Test
    public void noClientSideConnectorError() {
        open();

        Assert.assertFalse(isElementPresent(By.className("v-system-error")));
        checkLogsForErrors();

        ComboBoxElement combo = $(ComboBoxElement.class).first();
        combo.openPopup();
        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[0]",
                combo);
        Assert.assertEquals("1", combo.getProperty("value"));

        checkLogsForErrors();
    }
}
