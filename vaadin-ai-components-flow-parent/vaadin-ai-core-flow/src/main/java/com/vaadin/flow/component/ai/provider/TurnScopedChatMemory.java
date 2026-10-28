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
package com.vaadin.flow.component.ai.provider;

import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.ChatMemory;

/**
 * A chat memory that keeps the tool traffic of a turn out of the durable
 * conversation.
 * <p>
 * LangChain4j's AI services write every message of the tool-calling loop into
 * the chat memory and read the memory back before each round trip. This memory
 * keeps the conversation in two lists: the durable one holds the system
 * message, the user messages and the final answers, windowed by the wrapped
 * memory, and the turn list holds the tool requests and results added since the
 * current turn started. {@link #messages()} returns both, so the loop sees its
 * own traffic, and the turn list is dropped when the final answer arrives or
 * when the provider ends a failed turn.
 * <p>
 * A final answer without text is left out of the durable list, as a replayed
 * assistant message with neither content nor tool calls is rejected by some LLM
 * APIs.
 * <p>
 * Intended only for internal use and can be removed in the future.
 */
final class TurnScopedChatMemory implements ChatMemory {

    private final ChatMemory durable;
    private final List<ChatMessage> turn = new ArrayList<>();

    TurnScopedChatMemory(ChatMemory durable) {
        this.durable = durable;
    }

    @Override
    public Object id() {
        return durable.id();
    }

    @Override
    public void add(ChatMessage message) {
        if (message == null) {
            // A response without an AI message adds nothing; the wrapped
            // window memory would store the null and fail the next turn.
            return;
        }
        if (message instanceof ToolExecutionResultMessage) {
            turn.add(message);
            return;
        }
        if (message instanceof AiMessage aiMessage) {
            if (aiMessage.hasToolExecutionRequests()) {
                turn.add(message);
                return;
            }
            endTurn();
            var text = aiMessage.text();
            if (text == null || text.isEmpty()) {
                return;
            }
        }
        durable.add(message);
    }

    @Override
    public List<ChatMessage> messages() {
        var messages = new ArrayList<>(durable.messages());
        messages.addAll(turn);
        return messages;
    }

    @Override
    public void clear() {
        turn.clear();
        durable.clear();
    }

    /**
     * Drops the tool traffic of the current turn.
     */
    void endTurn() {
        turn.clear();
    }
}
