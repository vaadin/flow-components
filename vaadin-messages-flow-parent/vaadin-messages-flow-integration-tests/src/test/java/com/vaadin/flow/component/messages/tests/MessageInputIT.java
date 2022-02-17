/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
