package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import java.util.List;

@TestPath("vaadin-multi-select-combo-box/keep-filter")
public class MultiSelectComboBoxKeepFilterIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement keepFilter;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        keepFilter = $(TestBenchElement.class).id("keep-filter");
    }

    @Test
    public void keepFilterDisabled_clearsFilterAfterSelection() {
        comboBox.sendKeys("Item 1");
        comboBox.waitForLoadingFinished();
        List<String> filteredOptions = comboBox.getOptions();
        Assert.assertEquals(12, filteredOptions.size());

        comboBox.sendKeys(Keys.ENTER);

        Assert.assertEquals("", comboBox.getFilter());
        Assert.assertEquals(100, comboBox.getOptions().size());
    }

    @Test
    public void keepFilterEnabled_keepsFilterAfterSelection() {
        keepFilter.click();

        comboBox.sendKeys("Item 1");
        comboBox.waitForLoadingFinished();
        List<String> filteredOptions = comboBox.getOptions();
        Assert.assertEquals(12, filteredOptions.size());

        comboBox.sendKeys(Keys.ENTER);

        Assert.assertEquals("Item 1", comboBox.getFilter());
        Assert.assertEquals(filteredOptions, comboBox.getOptions());
    }
}
