package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-multi-select-combo-box/i18n")
public class MultiSelectComboBoxI18nIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement toggleAttached;
    private TestBenchElement setI18n;
    private TestBenchElement setEmptyI18n;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        toggleAttached = $("button").id("toggle-attached");
        setI18n = $("button").id("set-i18n");
        setEmptyI18n = $("button").id("set-empty-i18n");
    }

    @Test
    public void setI18n_i18nIsUpdated() {
        setI18n.click();

        Assert.assertEquals("Custom cleared",
                comboBox.getPropertyString("i18n", "cleared"));
        Assert.assertEquals("Custom focused",
                comboBox.getPropertyString("i18n", "focused"));
        Assert.assertEquals("Custom selected",
                comboBox.getPropertyString("i18n", "selected"));
        Assert.assertEquals("Custom deselected",
                comboBox.getPropertyString("i18n", "deselected"));
        Assert.assertEquals("{count} Custom total",
                comboBox.getPropertyString("i18n", "total"));
    }

    @Test
    public void setEmptyI18n_defaultI18nIsNotOverridden() {
        setEmptyI18n.click();

        Assert.assertEquals("Selection cleared",
                comboBox.getPropertyString("i18n", "cleared"));
    }

    @Test
    public void setI18n_detach_attach_i18nIsPersisted() {
        setI18n.click();
        toggleAttached.click();
        toggleAttached.click();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();

        Assert.assertEquals("Custom cleared",
                comboBox.getPropertyString("i18n", "cleared"));
    }
}
