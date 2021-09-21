package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-combo-box/combo-box-lit-wrapper-page")
public class ComboBoxLitTemplateNestedTemplateIT extends AbstractComboBoxIT {
    private ComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $("combo-box-lit-page").first().$("combo-box-lit-wrapper")
                .id("cbw1").$(ComboBoxElement.class).id("cb");
    }

    @Test
    public void comboBoxInitialValue_correctlySet_nestedLitTemplate() {
        String label = "D";
        Assert.assertEquals(label, comboBox.getSelectedText());
    }
}
