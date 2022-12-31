package com.vaadin.flow.component.fieldhighlighter.tests;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;

@TestPath("vaadin-field-highlighter")
public class FieldHighligterIT extends AbstractComponentIT {

    @Test
    public void callInitMethod_TextFieldHasAttribute() {
        open();
        ButtonElement button = $(ButtonElement.class).id("call-init");
        TextFieldElement textField = $(TextFieldElement.class)
                .id("tf-with-highlighter");

        Assert.assertFalse(
                "TextField should not have the has-highlighter attribute before calling initializing.",
                textField.hasAttribute("has-highlighter"));

        button.click();
        Assert.assertTrue(
                "TextField should have the has-highlighter attribute after calling initializing.",
                textField.hasAttribute("has-highlighter"));
    }
}
