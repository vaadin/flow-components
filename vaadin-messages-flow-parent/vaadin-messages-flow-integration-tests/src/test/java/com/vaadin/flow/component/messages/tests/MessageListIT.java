/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.messages.testbench.MessageElement;
import com.vaadin.flow.component.messages.testbench.MessageListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
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
        Assert.assertTrue("Image URL should start with 'VAADIN/dynamic'",
                imageUrl.startsWith("VAADIN/dynamic"));
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
    public void changeLocale_addItem_doesNotThrow() {
        clickElementWithJs("setLocale");
        clickElementWithJs("addItem");

        checkLogsForErrors(message -> message.contains("test.jpg"));
    }

    @Test
    public void changeLocale_addItem_itemTimesFormatted() {
        clickElementWithJs("setLocale");
        clickElementWithJs("addItem");

        List<MessageElement> messages = messageList.getMessageElements();
        Assert.assertTrue("Unexpected time prop",
                messages.get(0).getTime().matches("1 gen 2021, [0-9]+:[0-9]+"));
        Assert.assertTrue("Unexpected time format", messages.get(2).getTime()
                .matches("[0-9]+ [a-z]+ [0-9]{4}, [0-9]+:[0-9]+"));
    }

    @Test
    public void changeLocaleVariant_messagesRendered() {
        clickElementWithJs("setLocaleVariant");

        List<MessageElement> messages = messageList.getMessageElements();
        Assert.assertEquals("Unexpected items count", 2, messages.size());

        MessageElement msg = messages.get(0);
        Assert.assertEquals("Unexpected text content", "foo", msg.getText());
        // Should fallback to German locale formatting
        Assert.assertTrue("Unexpected time prop", msg.getTime()
                .matches("[0-9]+\\. [A-Za-z\\.]+ 2021, [0-9]+:[0-9]+"));
        Assert.assertEquals("Unexpected userName prop", "sender",
                msg.getUserName());

        checkLogsForErrors(message -> message.contains("test.jpg"));
    }

    @Test
    public void changeLocaleVariant_addItem_doesNotThrow() {
        clickElementWithJs("setLocaleVariant");
        clickElementWithJs("addItem");

        List<MessageElement> messages = messageList.getMessageElements();
        Assert.assertEquals("Unexpected items count", 3, messages.size());

        checkLogsForErrors(message -> message.contains("test.jpg"));
    }

    @Test
    public void setImageAsDownloadResource_imageLoaded() {
        getLogEntries(Level.WARNING); // message logs before setting resource
        clickElementWithJs("setImageAsDownloadHandler");
        String imageUrl = messageList.getMessageElements().get(0).getUserImg();
        Assert.assertTrue("Image URL should start with 'VAADIN/dynamic'",
                imageUrl.startsWith("VAADIN/dynamic"));
        checkLogsForErrors(); // would fail if the image wasn't hosted
    }

    @Test
    public void addItemWithAttachments_attachmentsRendered() {
        clickElementWithJs("addItemWithAttachments");

        var messages = messageList.getMessageElements();
        var messageWithAttachments = messages.get(messages.size() - 1);

        Assert.assertTrue("Message should have attachments",
                messageWithAttachments.hasAttachments());

        var attachments = messageWithAttachments.getAttachmentElements();
        Assert.assertEquals("Should have 3 attachments", 3, attachments.size());

        // Check file attachment
        var pdfAttachment = messageWithAttachments
                .getAttachmentByName("proposal.pdf");
        Assert.assertNotNull("Should find proposal.pdf attachment",
                pdfAttachment);
        Assert.assertFalse("PDF should not be an image attachment",
                messageWithAttachments.isImageAttachment(pdfAttachment));

        // Check image attachment
        var imageAttachment = messageWithAttachments
                .getAttachmentByName("chart.svg");
        Assert.assertNotNull("Should find chart.svg attachment",
                imageAttachment);
        Assert.assertTrue("SVG should be an image attachment",
                messageWithAttachments.isImageAttachment(imageAttachment));
    }

    @Test
    public void addAttachmentToExistingItem_attachmentRendered() {
        clickElementWithJs("addAttachmentToFirstItem");

        var firstMessage = getFirstMessage(messageList);

        Assert.assertTrue("First message should have attachments",
                firstMessage.hasAttachments());

        var attachment = firstMessage.getAttachmentByName("agenda.pdf");
        Assert.assertNotNull("Should find agenda.pdf attachment", attachment);
    }

    @Test
    public void clickAttachment_eventFired() {
        clickElementWithJs("addItemWithAttachments");

        var messages = messageList.getMessageElements();
        var messageWithAttachments = messages.get(messages.size() - 1);

        var pdfAttachment = messageWithAttachments
                .getAttachmentByName("proposal.pdf");
        pdfAttachment.click();

        var clickedAttachment = $("span").id("clickedAttachment");
        // Event includes item's userName, attachment name, and mime type
        Assert.assertEquals("User | proposal.pdf | application/pdf",
                clickedAttachment.getText());
    }

    @Test
    public void clickImageAttachment_eventFired() {
        clickElementWithJs("addItemWithAttachments");

        var messages = messageList.getMessageElements();
        var messageWithAttachments = messages.get(messages.size() - 1);

        var imageAttachment = messageWithAttachments
                .getAttachmentByName("chart.svg");
        imageAttachment.click();

        var clickedAttachment = $("span").id("clickedAttachment");
        // Event includes item's userName, attachment name, and mime type
        Assert.assertEquals("User | chart.svg | image/svg+xml",
                clickedAttachment.getText());
    }

    @Test
    public void showTyping_typingIndicatorRendered() {
        clickElementWithJs("showTyping");

        Assert.assertEquals("Unexpected typing users", List.of("Alice"),
                messageList.getTypingUserNames());
        Assert.assertTrue("Unexpected typing indicator text",
                getTypingIndicatorText().contains("Typing"));
    }

    @Test
    public void showTyping_typingIndicatorNotIncludedInMessages() {
        clickElementWithJs("showTyping");

        Assert.assertEquals("Typing indicator must not be listed as a message",
                2, messageList.getMessageElements().size());
    }

    @Test
    public void showTwoTyping_bothTypingUsersRendered() {
        clickElementWithJs("showTwoTyping");

        Assert.assertEquals("Unexpected typing users", List.of("Alice", "Bob"),
                messageList.getTypingUserNames());
    }

    @Test
    public void hideTyping_typingIndicatorRemoved() {
        clickElementWithJs("showTyping");
        Assert.assertNotNull(getTypingIndicator());

        clickElementWithJs("hideTyping");

        Assert.assertNull("Expected no typing indicator", getTypingIndicator());
        Assert.assertTrue(messageList.getTypingUserNames().isEmpty());
    }

    @Test
    public void setI18n_showTyping_typingIndicatorTextTranslated() {
        clickElementWithJs("setI18n");
        clickElementWithJs("showTyping");

        Assert.assertTrue("Unexpected typing indicator text",
                getTypingIndicatorText().contains("is thinking"));
    }

    @Test
    public void showTyping_setI18n_visibleTypingIndicatorTextTranslated() {
        clickElementWithJs("showTyping");
        Assert.assertTrue("Expected the default text",
                getTypingIndicatorText().contains("Typing"));

        clickElementWithJs("setI18n");

        Assert.assertTrue("Unexpected typing indicator text",
                getTypingIndicatorText().contains("is thinking"));
    }

    @Test
    public void showTyping_setEllipsisType_visibleTypingIndicatorUpdated() {
        clickElementWithJs("showTyping");
        Assert.assertEquals("Expected the default type", "",
                getTypingIndicatorType());

        clickElementWithJs("setEllipsisTypingIndicator");
        Assert.assertEquals("Unexpected typing indicator type", "ellipsis",
                getTypingIndicatorType());

        clickElementWithJs("setDefaultTypingIndicator");
        Assert.assertEquals("Expected the default type", "",
                getTypingIndicatorType());
    }

    @Test
    public void showTypingWithImageHandler_avatarImageLoaded() {
        clickElementWithJs("showTypingWithImageHandler");
        var avatar = getTypingIndicator().$("vaadin-avatar").first();

        var imageUrl = avatar.getPropertyString("img");
        Assert.assertNotNull("Expected an avatar image URL", imageUrl);
        Assert.assertTrue(
                "Image URL should start with 'VAADIN/dynamic', was " + imageUrl,
                imageUrl.startsWith("VAADIN/dynamic"));

        // The image only decodes if the download handler is actually served
        waitUntil(driver -> (Boolean) executeScript("""
                const img = arguments[0].shadowRoot.querySelector('img');
                return !!(img && img.complete && img.naturalWidth > 0);
                """, avatar));
    }

    @Test
    public void showTyping_reattachList_typingIndicatorRestored() {
        clickElementWithJs("showTyping");
        clickElementWithJs("detachList");
        clickElementWithJs("attachList");

        messageList = $(MessageListElement.class).first();
        Assert.assertEquals("Expected the typing indicator to be restored",
                List.of("Alice"), messageList.getTypingUserNames());
    }

    /**
     * Gets the element rendered as the typing indicator, or {@code null} if no
     * user is typing. Intentionally relies on how the web component renders the
     * indicator, as the public TestBench API only exposes the user names.
     */
    private TestBenchElement getTypingIndicator() {
        var query = messageList.$(TestBenchElement.class).withAttribute("slot",
                "typing-indicator");
        return query.exists() ? query.first() : null;
    }

    private String getTypingIndicatorText() {
        return getTypingIndicator().getPropertyString("textContent");
    }

    private String getTypingIndicatorType() {
        return getTypingIndicator().getDomAttribute("typing-indicator");
    }

    private MessageElement getFirstMessage(MessageListElement list) {
        return list.getMessageElements().get(0);
    }
}
