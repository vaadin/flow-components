

package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-combo-box/lazy-combo-box-filter")
public class LazyComboBoxFilterIT extends AbstractComponentIT {

    @Test
    public void lazyComboBoxFilterFirstQuery() {
        open();
        ComboBoxElement comboBox = $(ComboBoxElement.class).first();
        comboBox.sendKeys("1");
        comboBox.openPopup();

        WebElement query = findElement(By.id("query"));
        Assert.assertTrue(query.getText().contains("Filter: 1"));
        Assert.assertTrue(query.getText().contains("Count: 10"));
    }
}
