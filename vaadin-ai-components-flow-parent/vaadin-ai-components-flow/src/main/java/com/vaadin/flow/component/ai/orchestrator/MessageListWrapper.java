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
package com.vaadin.flow.component.ai.orchestrator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.ui.AIMessage;
import com.vaadin.flow.component.ai.ui.AIMessageList;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;

/**
 * Wrapper for Flow MessageList component to implement AIMessageList interface.
 */
class MessageListWrapper implements AIMessageList {

    private final MessageList messageList;
    private final Map<MessageListItem, AIMessage> itemToMessage = new HashMap<>();

    MessageListWrapper(MessageList messageList) {
        this.messageList = messageList;
    }

    @Override
    public void addMessage(AIMessage message) {
        var item = ((MessageListItemWrapper) message).getItem();
        itemToMessage.put(item, message);
        messageList.addItem(item);
    }

    @Override
    public AIMessage createMessage(String text, String userName,
            List<AIAttachment> attachments) {
        return new MessageListItemWrapper(text, userName, attachments);
    }

    @Override
    public void addAttachmentClickListener(AttachmentClickCallback callback) {
        messageList.addAttachmentClickListener(clickEvent -> {
            var aiMessage = itemToMessage.get(clickEvent.getItem());
            if (aiMessage != null) {
                var attIndex = clickEvent.getItem().getAttachments()
                        .indexOf(clickEvent.getAttachment());
                if (attIndex >= 0) {
                    callback.onAttachmentClick(aiMessage, attIndex);
                }
            }
        });
    }
}
