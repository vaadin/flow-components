/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import static org.hamcrest.CoreMatchers.startsWith;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

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
        /*
         * Testing each setter separately to make sure that they all trigger the
         * client-side property update.
         */

        clickElementWithJs("setText");
        Assert.assertEquals("Unexpected text content", "newfoo",
                getFirstMessage(messageList).getText());

        clickElementWithJs("setTime");
        Assert.assertTrue("Unexpected time prop", getFirstMessage(messageList)
                .getTime().matches("Feb 2, 2000, [0-9]+:[0-9]+ [A|P]M"));

        clickElementWithJs("setUserName");
        Assert.assertEquals("Unexpected userName prop", "sender2",
                getFirstMessage(messageList).getUserName());

        clickElementWithJs("setUserImage");
        Assert.assertEquals("Unexpected userImage prop", "/test2.jpg",
                getFirstMessage(messageList).getUserImg());

        clickElementWithJs("setAbbreviation");
        Assert.assertEquals("Unexpected userAbbreviation prop", "CD",
                getFirstMessage(messageList).getUserAbbr());

        clickElementWithJs("setUserColorIndex");
        Assert.assertEquals("Unexpected userColorIndex prop", 2,
                getFirstMessage(messageList).getUserColorIndex());

        clickElementWithJs("addThemeNames");
        Assert.assertEquals("Unexpected theme prop after adding theme names",
                "foo bar", getFirstMessage(messageList).getTheme());

        clickElementWithJs("removeThemeNames");
        Assert.assertEquals("Unexpected theme prop after removing theme names",
                null, getFirstMessage(messageList).getTheme());

        clickElementWithJs("addClassNames");
        Assert.assertEquals("Unexpected class name after adding class names",
                Set.of("urgent", "pinned"),
                getFirstMessage(messageList).getClassNames());

        clickElementWithJs("removeClassNames");
        Assert.assertEquals("Unexpected class name after removing class names",
                Set.of("pinned"), getFirstMessage(messageList).getClassNames());
    }

    @Test
    public void appendText_messagesUpdated() {
        clickElementWithJs("appendText");
        Assert.assertEquals("Unexpected text content", "foo2",
                getFirstMessage(messageList).getText());
    }

    @Test
    public void setText_appendText_messagesUpdated() {
        clickElementWithJs("setText");
        clickElementWithJs("appendText");
        Assert.assertEquals("Unexpected text content", "newfoo2",
                getFirstMessage(messageList).getText());
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
        getLogEntries(Level.WARNING); // message logs before setting resource
        clickElementWithJs("setImageAsStreamResource");
        String imageUrl = messageList.getMessageElements().get(0).getUserImg();
        MatcherAssert.assertThat(imageUrl, startsWith("VAADIN/dynamic"));
        // would fail if the avatar.png image wasn't hosted
        checkLogsForErrors(message -> message.contains("test.jpg"));
    }

    @Test
    public void addItem_itemAdded() {
        clickElementWithJs("addItem");

        var messages = messageList.getMessageElements();
        var msg = messages.get(2);

        Assert.assertEquals("User", msg.getUserName());
        Assert.assertEquals("Foo", msg.getText());
    }

    @Test
    public void addItem_setItems() {
        clickElementWithJs("addItem");
        clickElementWithJs("setItems");

        var messages = messageList.getMessageElements();
        Assert.assertEquals("Unexpected items count", 1, messages.size());
        var msg = messages.get(0);

        Assert.assertEquals("sender3", msg.getUserName());
    }

    @Test
    public void addTwoItems_twoItemsAdded() {
        clickElementWithJs("addTwoItems");

        var messages = messageList.getMessageElements();
        Assert.assertEquals("Unexpected items count", 4, messages.size());
        var msg = messages.get(3);

        Assert.assertEquals("Bar", msg.getText());
    }

    @Test
    public void setImageAsDownloadResource_imageLoaded() {
        getLogEntries(Level.WARNING); // message logs before setting resource
        clickElementWithJs("setImageAsDownloadHandler");
        String imageUrl = messageList.getMessageElements().get(0).getUserImg();
        MatcherAssert.assertThat(imageUrl, startsWith("VAADIN/dynamic"));
        checkLogsForErrors(); // would fail if the image wasn't hosted
    }

    private MessageElement getFirstMessage(MessageListElement list) {
        return list.getMessageElements().get(0);
    }
}
