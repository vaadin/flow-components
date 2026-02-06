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

import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import com.vaadin.flow.component.ai.common.AiAttachment;
import com.vaadin.flow.component.ai.component.AiMessage;
import com.vaadin.flow.component.messages.MessageListItem;

/**
 * Wrapper for MessageListItem to implement AiMessage interface.
 */
class MessageListItemWrapper implements AiMessage {

    private final MessageListItem item;

    MessageListItemWrapper(String text, String userName,
            List<AiAttachment> attachments) {
        item = new MessageListItem(text, Instant.now(), userName);
        if (attachments != null && !attachments.isEmpty()) {
            var messageAttachments = attachments.stream()
                    .map(a -> new MessageListItem.Attachment(a.name(),
                            toDataUrl(a), a.mimeType()))
                    .toList();
            item.setAttachments(messageAttachments);
        }
    }

    MessageListItem getItem() {
        return item;
    }

    @Override
    public String getText() {
        return item.getText();
    }

    @Override
    public void setText(String text) {
        item.setText(text);
    }

    @Override
    public Instant getTime() {
        return item.getTime();
    }

    @Override
    public String getUserName() {
        return item.getUserName();
    }

    @Override
    public void appendText(String token) {
        item.appendText(token);
    }

    private static String toDataUrl(AiAttachment attachment) {
        return "data:" + attachment.mimeType() + ";base64,"
                + Base64.getEncoder().encodeToString(attachment.data());
    }
}
