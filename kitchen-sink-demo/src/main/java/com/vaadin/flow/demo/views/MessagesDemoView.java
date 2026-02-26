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
package com.vaadin.flow.demo.views;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for Messages components.
 */
@Route(value = "messages", layout = MainLayout.class)
@PageTitle("Messages | Vaadin Kitchen Sink")
public class MessagesDemoView extends VerticalLayout {

    public MessagesDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Messages Components"));
        add(new Paragraph("Messages components provide chat/messaging interfaces."));

        // Basic message list
        MessageList basicList = new MessageList();
        basicList.setItems(createBasicMessages());
        basicList.setWidthFull();
        basicList.setHeight("300px");
        addSection("Basic Message List", basicList);

        // Message input
        MessageInput basicInput = new MessageInput();
        basicInput.addSubmitListener(e -> {
            // Would normally add the message to a list
        });
        basicInput.setWidthFull();
        addSection("Message Input", basicInput);

        // Interactive chat
        List<MessageListItem> chatMessages = new ArrayList<>();
        chatMessages.add(new MessageListItem("Hello! How can I help you today?",
            Instant.now().minus(5, ChronoUnit.MINUTES), "Support Agent"));
        chatMessages.add(new MessageListItem("I have a question about my order.",
            Instant.now().minus(4, ChronoUnit.MINUTES), "You"));
        chatMessages.add(new MessageListItem("Sure! What's your order number?",
            Instant.now().minus(3, ChronoUnit.MINUTES), "Support Agent"));

        MessageList chatList = new MessageList();
        chatList.setItems(chatMessages);
        chatList.setWidthFull();
        chatList.setHeight("300px");

        MessageInput chatInput = new MessageInput();
        chatInput.addSubmitListener(e -> {
            chatMessages.add(new MessageListItem(e.getValue(), Instant.now(), "You"));
            chatList.setItems(new ArrayList<>(chatMessages));
        });
        chatInput.setWidthFull();

        Div chatContainer = new Div();
        chatContainer.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.MEDIUM,
                LumoUtility.Padding.MEDIUM);
        chatContainer.add(chatList, chatInput);
        addSection("Interactive Chat Demo", chatContainer);

        // Team discussion example
        MessageList teamList = new MessageList();
        List<MessageListItem> teamMessages = new ArrayList<>();

        MessageListItem msg1 = new MessageListItem(
            "Hey team, just pushed the new feature to staging.",
            Instant.now().minus(2, ChronoUnit.HOURS), "Alice"
        );
        msg1.setUserColorIndex(0);

        MessageListItem msg2 = new MessageListItem(
            "Great! I'll start testing it now.",
            Instant.now().minus(1, ChronoUnit.HOURS), "Bob"
        );
        msg2.setUserColorIndex(1);

        MessageListItem msg3 = new MessageListItem(
            "Found a minor issue with the form validation. Creating a bug ticket.",
            Instant.now().minus(30, ChronoUnit.MINUTES), "Bob"
        );
        msg3.setUserColorIndex(1);

        MessageListItem msg4 = new MessageListItem(
            "Thanks Bob! I'll fix it today.",
            Instant.now().minus(15, ChronoUnit.MINUTES), "Alice"
        );
        msg4.setUserColorIndex(0);

        MessageListItem msg5 = new MessageListItem(
            "The fix is deployed. Can you verify?",
            Instant.now().minus(5, ChronoUnit.MINUTES), "Alice"
        );
        msg5.setUserColorIndex(0);

        teamMessages.add(msg1);
        teamMessages.add(msg2);
        teamMessages.add(msg3);
        teamMessages.add(msg4);
        teamMessages.add(msg5);

        teamList.setItems(teamMessages);
        teamList.setWidthFull();
        teamList.setHeight("350px");
        addSection("Team Discussion Example", teamList);

        // With images (avatars shown via color index)
        MessageList withAvatars = new MessageList();
        List<MessageListItem> avatarMessages = new ArrayList<>();

        MessageListItem avatar1 = new MessageListItem(
            "Welcome to the project!",
            Instant.now().minus(1, ChronoUnit.DAYS), "Project Manager"
        );
        avatar1.setUserColorIndex(2);
        avatar1.setUserAbbreviation("PM");

        MessageListItem avatar2 = new MessageListItem(
            "Thanks! Excited to be here.",
            Instant.now().minus(23, ChronoUnit.HOURS), "New Developer"
        );
        avatar2.setUserColorIndex(3);
        avatar2.setUserAbbreviation("ND");

        avatarMessages.add(avatar1);
        avatarMessages.add(avatar2);
        withAvatars.setItems(avatarMessages);
        withAvatars.setWidthFull();
        withAvatars.setHeight("200px");
        addSection("With User Abbreviations", withAvatars);
    }

    private List<MessageListItem> createBasicMessages() {
        List<MessageListItem> messages = new ArrayList<>();

        messages.add(new MessageListItem(
            "Hi everyone!",
            Instant.now().minus(10, ChronoUnit.MINUTES),
            "John"
        ));

        messages.add(new MessageListItem(
            "Hey John, how's it going?",
            Instant.now().minus(8, ChronoUnit.MINUTES),
            "Jane"
        ));

        messages.add(new MessageListItem(
            "Pretty good! Just finished the new feature.",
            Instant.now().minus(5, ChronoUnit.MINUTES),
            "John"
        ));

        messages.add(new MessageListItem(
            "That's awesome! Can't wait to try it out.",
            Instant.now().minus(2, ChronoUnit.MINUTES),
            "Jane"
        ));

        return messages;
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }
}
