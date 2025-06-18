/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.fieldhighlighter.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

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
