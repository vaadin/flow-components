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

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

@Route("vaadin-messages/chat")
public class MessageListChatPage extends Div {

    private MessageList list = new MessageList();
    private MessageInput input = new MessageInput();

    private String answerMarkdown = """
            ## Hello! I’m your AI assistant 🤖

            I can help you with:

            1. **Answering questions** – from quick facts to in-depth explanations.
            2. **Explaining concepts** – breaking down complex ideas into clear, step-by-step logic.
            3. **Brainstorming & creativity** – generating outlines, stories, code snippets, or design ideas.
            4. **Guidance & troubleshooting** – walking you through processes or helping debug issues.

            ---

            ### How to get the most out of me 🛠️

            | Step | What to do | Why it matters |
            |------|------------|----------------|
            | 1️⃣ | **State your goal clearly.** | A precise prompt yields a precise answer. |
            | 2️⃣ | **Add constraints or context.** <br>*(e.g., audience, length, tone)* | Tailors the response to your needs. |
            | 3️⃣ | **Ask follow-ups.** | We can iterate until you’re satisfied. |

            ---

            #### Example

            > **You:** “Explain quantum entanglement in simple terms.”

            > **Me:**
            > *Imagine two coins spun so perfectly in sync that the moment you look at one and see “heads,” the other coin—no matter how far away—will instantly show “tails.” In quantum physics, particles can become linked in just that way…*

            ---

            Need anything else? Just let me know, and I’ll jump right in! ✨
                        """;

    private void simulateMessageStream(Consumer<String> consumer) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int tokenLength = 10;
            int tokenIndex = 0;

            while (tokenIndex < answerMarkdown.length()) {
                int endIndex = Math.min(tokenIndex + tokenLength,
                        answerMarkdown.length());
                String token = answerMarkdown.substring(tokenIndex, endIndex);

                consumer.accept(token);

                tokenIndex += tokenLength;

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Let's use null for the end of the message for now
            consumer.accept(null);

        }).start();

    }

    public MessageListChatPage() {
        setId("chat");

        var userItem = new MessageListItem(
                "Hello! Can you help me with a question?", Instant.now(),
                "User");
        userItem.setUserColorIndex(1);

        var assistantItem = new MessageListItem(
                "Of course! I'm here to help. What's your question?",
                Instant.now(), "Assistant");
        assistantItem.setUserColorIndex(2);

        list.setItems(List.of(userItem, assistantItem));

        list.setMarkdown(true);

        input.addSubmitListener(submitEvent -> {
            var newUserItem = new MessageListItem(submitEvent.getValue(),
                    Instant.now(), "User");
            newUserItem.setUserColorIndex(1);

            list.addItem(newUserItem);

            input.setEnabled(false);

            var ui = getUI().get();

            // If push isn't used, we need to set the poll interval
            ui.setPollInterval(100);

            var newAssistantItem = new MessageListItem("", Instant.now(),
                    "Assistant");
            newAssistantItem.setUserColorIndex(2);
            simulateMessageStream(token -> {
                if (token != null) {
                    ui.access(() -> {
                        if (!list.getItems().contains(newAssistantItem)) {
                            list.addItem(newAssistantItem);
                        }
                        newAssistantItem
                                .setText(newAssistantItem.getText() + token);
                    });
                } else {
                    ui.access(() -> {
                        ui.setPollInterval(-1);
                        input.setEnabled(true);
                    });
                }
            });
        });

        add(list, input);


        // Styles
        var style = new Element("style");
        style.setText("""
            #chat {
                display: flex;
                flex-direction: column;
                height: 100%;
            }

            vaadin-message-list {
                flex: 1;
                scroll-snap-type: y proximity;
            }

            vaadin-message-list::after {
                display: block;
                content: '';
                scroll-snap-align: end;
                min-height: 1px;
            }
        """);

        getElement().appendChild(style);
    }
}
