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

    /**
     * This particular test case, test when we use `combo-box` inside a
     * lit-template(the wrapper), and use the wrapped component in Java code for
     * setting the initial value or readonly attribute disabled and ... the
     * intent of this test is to check if combo-box value (visible label) is set
     * correctly or not.
     */
    @Test
    public void comboBoxInitialValue_litTemplate_ShouldBeSetWithSetValue() {
        String labelValue = "1";
        Assert.assertEquals(labelValue, comboBox.getSelectedText());
    }
}
