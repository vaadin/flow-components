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
package com.vaadin.flow.component.ai.common;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a chat message in a conversation history.
 * <p>
 * This is a text-only, framework-agnostic representation used with
 * {@code AIOrchestrator.getHistory()} and {@code Builder.withHistory()} to
 * persist and restore conversation state across sessions. The following data is
 * not preserved:
 * <ul>
 * <li>File attachments and multi-modal content</li>
 * <li>Tool call requests and tool execution results</li>
 * <li>Provider-specific metadata</li>
 * <li>System messages (re-injected by the orchestrator on each request)</li>
 * </ul>
 *
 * @param role
 *            the role of the message sender, not {@code null}
 * @param content
 *            the text content of the message, not {@code null}
 * @param messageId
 *            an optional identifier assigned to user messages by the
 *            orchestrator, used to correlate with attachment data stored via
 *            {@code AttachmentSubmitListener}; may be {@code null}
 * @param time
 *            the timestamp when the message was created; may be {@code null}
 * @author Vaadin Ltd
 */
public record ChatMessage(Role role, String content, String messageId,
        Instant time) implements Serializable {

    /**
     * Creates a new chat message.
     *
     * @param role
     *            the role of the message sender
     * @param content
     *            the text content of the message
     * @param messageId
     *            an optional identifier for this message, may be {@code null}
     * @param time
     *            the timestamp when the message was created, may be
     *            {@code null}
     * @throws NullPointerException
     *             if role or content is {@code null}
     */
    public ChatMessage {
        Objects.requireNonNull(role, "Role cannot be null");
        Objects.requireNonNull(content, "Content cannot be null");
    }

    /**
     * The role of a message sender in a conversation.
     */
    public enum Role {
        /** A message sent by the user. */
        USER,
        /** A message sent by the AI assistant. */
        ASSISTANT
    }
}
