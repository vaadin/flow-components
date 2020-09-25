package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-combo-box/helper-text")
public class HelperTextPageIT extends AbstractComboBoxIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void assertHelperText() {
        ComboBoxElement comboHelperText = $(ComboBoxElement.class)
              .id("combobox-helper-text");
        Assert.assertEquals("Helper text", comboHelperText.getHelperText());

        clickButton("empty-helper-text");
        Assert.assertEquals("", comboHelperText.getHelperText());
    }

    @Test
    public void assertHelperComponent() {
        ComboBoxElement comboHelperComponent = $(ComboBoxElement.class)
              .id("combobox-helper-component");
        Assert.assertEquals("helper-component",
              comboHelperComponent.getHelperComponent().getAttribute("id"));

        clickButton("empty-helper-component");
        Assert.assertEquals(null, comboHelperComponent.getHelperComponent());
    }

}
