
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/clear")
public class ComboBoxClearIT extends AbstractComboBoxIT {

    private ComboBoxElement box;

    @Test
    public void comboBoxClear() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
        box = $(ComboBoxElement.class).first();
        Assert.assertEquals(box.getSelectedText(), "Eight");
        box.clear();
        Assert.assertTrue(box.getSelectedText().isEmpty());
    }
}
