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
package com.vaadin.flow.component.ai.orchestrator;

import java.util.List;

import com.vaadin.flow.component.ai.common.AiAttachment;
import com.vaadin.flow.component.ai.component.AiMessage;
import com.vaadin.flow.component.ai.component.AiMessageList;
import com.vaadin.flow.component.messages.MessageList;

/**
 * Wrapper for Flow MessageList component to implement AiMessageList interface.
 */
record MessageListWrapper(MessageList messageList) implements AiMessageList {

    @Override
    public void addMessage(AiMessage message) {
        messageList.addItem(((MessageListItemWrapper) message).getItem());
    }

    @Override
    public AiMessage createMessage(String text, String userName,
            List<AiAttachment> attachments) {
        return new MessageListItemWrapper(text, userName, attachments);
    }
}
