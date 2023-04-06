
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

@TestPath("vaadin-combo-box/readonly-blur")
public class ComboBoxReadOnlyBlurIT extends AbstractComboBoxIT {

    @Test
    public void comboBoxReadOnlyBlur() {
        open();
        ComboBoxElement comboBoxElement = $(ComboBoxElement.class)
                .waitForFirst();

        // simulate blur on combo box
        comboBoxElement.dispatchEvent("focusout");
        getCommandExecutor().waitForVaadin();

        // Blur should not trigger custom value set event.
        Assert.assertThrows(NoSuchElementException.class,
                () -> findElement(By.id("custom-value-set")));
    }
}
