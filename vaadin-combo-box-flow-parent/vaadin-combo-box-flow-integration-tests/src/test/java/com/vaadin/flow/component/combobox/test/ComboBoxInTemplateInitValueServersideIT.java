package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-combo-box/combo-box-in-template-initial-value")
public class ComboBoxInTemplateInitValueServersideIT
        extends AbstractComboBoxIT {

    private ComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $("combo-box-initial-value").first().$(ComboBoxElement.class)
                .id("combo");
    }

    @Test
    public void comboBoxInitialValue_litTemplate_ShouldBeSetWithSetValue() {
        String labelValue = "1";
        Assert.assertEquals(labelValue, comboBox.getSelectedText());
    }
}
