package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

@TestPath("vaadin-multi-select-combo-box/custom-value")
public class MultiSelectComboBoxCustomValueIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).first();
    }

    @Test
    public void setCustomValue_addAndRefresh_sizeCorrectlyUpdated() {
        // enter filter, wait until overlay is empty because no items match
        comboBox.sendKeys("foo");
        comboBox.waitForLoadingFinished();

        // add custom value and refresh data provider, wait until updated items
        // are loaded
        comboBox.sendKeys(Keys.ENTER);
        comboBox.waitForLoadingFinished();

        // size should be initial size + custom value
        Integer size = comboBox.getPropertyInteger("size");
        Assert.assertEquals(101, size.intValue());
    }
}
