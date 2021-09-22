package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-combo-box/combo-box-lit-wrapper-lit-template-page")
public class ComboBoxLitTemplateWrapperLitTemplatePageIT extends AbstractComboBoxIT {
    private ComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $("combo-box-lit-wrapper-lit-template-page").first().$("combo-box-lit-template-wrapper")
                .id("cbw1").$(ComboBoxElement.class).id("cb");
    }

    /**
     *  This particular test case, test when we use `combo-box` inside a lit-template(the wrapper),
     *  and use the wrapped element in another template and interact with those components via serverside
     *  Java code (setting value, readonly, disabled ....)
     */
    @Test
    public void comboBoxInitialValue_correctlySet_nestedLitTemplate() {
        String label = "D";
        Assert.assertEquals(label, comboBox.getSelectedText());
    }
}
