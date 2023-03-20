
package com.vaadin.flow.component.combobox.test.template;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-combo-box/combo-box-in-template")
public class ComboBoxInTemplateIT extends AbstractComponentIT {

    private TestBenchElement message;
    private ComboBoxElement box1;
    private ComboBoxElement box2;

    @Before
    public void init() {
        open();
        message = $("label").id("message");
        box1 = $("wrapper-template").first().$("combo-box-in-a-template")
                .first().$(ComboBoxElement.class).first();
        box2 = $("wrapper-template").first().$("combo-box-in-a-template2")
                .first().$(ComboBoxElement.class).first();
    }

    @Test
    // Test for https://github.com/vaadin/flow/issues/4862
    public void twoLevelsOfTemplates_setValue_addValueChangeListener_noInitialValueChangeEvent() {
        Assert.assertEquals("Value change event should not be fired.", "-",
                message.getText());
    }

    @Test
    public void twoLevelsOfTemplates_valueChangeEventsFired() {
        box1.openPopup();
        box1.setProperty("value", "2");
        Assert.assertEquals("2", message.getText());

        box2.openPopup();
        box2.setProperty("value", "3");
        Assert.assertEquals("3", message.getText());

        box1.setProperty("value", "");
        Assert.assertEquals("null", message.getText());
    }

}
