/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.messages.tests;

import static org.hamcrest.CoreMatchers.startsWith;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.messages.testbench.MessageElement;
import com.vaadin.flow.component.messages.testbench.MessageListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-messages/message-list-test")
public class MessageListIT extends AbstractComponentIT {

    private MessageListElement messageList;

    @Before
    public void init() {
        open();
        messageList = $(MessageListElement.class).first();
    }

    @Test
    public void setInitialItems_messagesRendered() {
        List<MessageElement> messages = messageList.getMessageElements();
        Assert.assertEquals("Unexpected items count", 2, messages.size());

        MessageElement msg = messages.get(0);
        Assert.assertEquals("Unexpected text content", "foo", msg.getText());
        Assert.assertTrue("Unexpected time prop",
                msg.getTime().matches("Jan 1, 2021, [0-9]+:[0-9]+ [A|P]M"));
        Assert.assertEquals("Unexpected userName prop", "sender",
                msg.getUserName());
        Assert.assertEquals("Unexpected userImage prop", "/test.jpg",
                msg.getUserImg());
        Assert.assertEquals("Unexpected userAbbreviation prop", "AB",
                msg.getUserAbbr());
        Assert.assertEquals("Unexpected userColorIndex prop", 1,
                msg.getUserColorIndex());

        Assert.assertEquals("Unexpected text content", "bar",
                messages.get(1).getText());
    }

    @Test
    public void updateItemPropertiesAfterRendering_messagesUpdated() {
        MessageElement msg = messageList.getMessageElements().get(0);

        /*
         * Testing each setter separately to make sure that they all trigger the
         * client-side property update.
         */

        clickElementWithJs("setText");
        Assert.assertEquals("Unexpected text content", "foo2", msg.getText());

        clickElementWithJs("setTime");
        Assert.assertTrue("Unexpected time prop",
                msg.getTime().matches("Feb 2, 2000, [0-9]+:[0-9]+ [A|P]M"));

        clickElementWithJs("setUserName");
        Assert.assertEquals("Unexpected userName prop", "sender2",
                msg.getUserName());

        clickElementWithJs("setUserImage");
        Assert.assertEquals("Unexpected userImage prop", "/test2.jpg",
                msg.getUserImg());

        clickElementWithJs("setAbbreviation");
        Assert.assertEquals("Unexpected userAbbreviation prop", "CD",
                msg.getUserAbbr());

        clickElementWithJs("setUserColorIndex");
        Assert.assertEquals("Unexpected userColorIndex prop", 2,
                msg.getUserColorIndex());
    }

    @Test
    public void changeItemsAfterRendering_messagesUpdated() {
        clickElementWithJs("setItems");

        List<MessageElement> messages = messageList.getMessageElements();
        Assert.assertEquals("Unexpected items count", 1, messages.size());

        MessageElement msg = messages.get(0);

        Assert.assertEquals("Unexpected text content", "", msg.getText());
        Assert.assertEquals("Unexpected time prop", null, msg.getTime());
        Assert.assertEquals("Unexpected userName prop", "sender3",
                msg.getUserName());
    }

    @Test
    public void changeLocale_timeFormatted() {
        clickElementWithJs("setLocale");

        List<MessageElement> messages = messageList.getMessageElements();
        MessageElement msg = messages.get(0);

        Assert.assertTrue("Unexpected time prop",
                msg.getTime().matches("1 gen 2021, [0-9]+:[0-9]+"));
    }

    @Test
    public void reattachElement_messagesRendered() {
        clickElementWithJs("detachList");
        clickElementWithJs("attachList");

        List<MessageElement> messages = $(MessageListElement.class).first()
                .getMessageElements();
        Assert.assertEquals("Unexpected items count", 2, messages.size());
    }

    @Test
    public void setImageAsStreamResource_imageLoaded() {
        clickElementWithJs("setImageAsStreamResource");
        String imageUrl = messageList.getMessageElements().get(0).getUserImg();
        MatcherAssert.assertThat(imageUrl, startsWith("VAADIN/dynamic"));

        // the following would fail if the image wasn't hosted
        checkLogsForErrors(msg -> !msg.contains("VAADIN/dynamic"));
    }
}
