/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.messages.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.html.testbench.InputTextElement;
import com.vaadin.flow.component.messages.testbench.MessageInputElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-messages/message-input-test")
public class MessageInputIT extends AbstractComponentIT {

    private MessageInputElement messageInput;

    @Before
    public void init() {
        open();
        messageInput = $(MessageInputElement.class).first();
    }

    @Test
    public void submitValue_eventHasCorrectValue() {
        messageInput.submit("foo");
        Assert.assertEquals("foo",
                $(InputTextElement.class).id("verify-field").getValue());
    }

    @Test
    public void setEnabledFalse_elementHasDisabledAttribute() {
        clickElementWithJs("toggle-enabled");
        Assert.assertTrue(messageInput.hasAttribute("disabled"));
    }

    @Test
    public void setEnabledFalse_removeDisabledAtClientSide_submit_ignoredAtServerSide() {
        clickElementWithJs("toggle-enabled");

        messageInput.setProperty("disabled", false);

        messageInput.submit("foo");
        Assert.assertEquals(
                "The event shouldn't have fired on a disabled component", "",
                $(InputTextElement.class).id("verify-field").getValue());
    }

    @Test
    public void setI18n_textTranslated() {
        clickElementWithJs("set-i18n");

        String inputPlaceholder = messageInput
                .$("vaadin-message-input-text-area").first()
                .getPropertyString("placeholder");
        Assert.assertEquals("Viesti", inputPlaceholder);

        String buttonText = messageInput.$("vaadin-message-input-button")
                .first().getText();
        Assert.assertEquals("Lähetä", buttonText);
    }
}
