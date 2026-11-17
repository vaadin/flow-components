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
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;

/**
 * A chat memory that keeps the tool traffic of a turn out of the durable
 * conversation.
 * <p>
 * LangChain4j's AI services write every message of the tool-calling loop into
 * the chat memory and read the memory back before each round trip. This memory
 * keeps the system message, the user messages and the final answers in the
 * wrapped memory, which windows them, and the tool requests and results in a
 * list that belongs to the turn. {@link #messages()} returns both, so the loop
 * sees its own traffic, and the traffic is gone with the turn, whose list is
 * discarded when it ends.
 * <p>
 * A final answer also goes to the list of its turn, and while the turn runs,
 * {@link #messages()} returns it there, after the tool traffic it is based on,
 * instead of from the wrapped memory. An output guardrail that reprompts the
 * model sends these messages again, so the model still has the tool results of
 * the turn.
 * <p>
 * The list is the one of the turn that runs on the calling thread, so a turn
 * that is still running after it was cancelled never sees, or drops, the tool
 * traffic of the turn that started after it. Tool traffic added outside a turn
 * goes to the wrapped memory, as it would in any AI service.
 * <p>
 * A final answer without text is left out of the wrapped memory, as a replayed
 * assistant message with neither content nor tool calls is rejected by some LLM
 * APIs. A user message is stored as the function given to the constructor
 * completes it, which lets the owner add what LangChain4j left out, such as the
 * attachments of the turn.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
final class TurnScopedChatMemory implements ChatMemory {

    private final ChatMemory durable;
    private final Supplier<List<ChatMessage>> turnMessages;
    private final UnaryOperator<UserMessage> userMessages;

    /**
     * Creates a memory over the given durable memory.
     *
     * @param durable
     *            the memory that keeps the conversation across turns
     * @param turnMessages
     *            supplies the list that holds the tool traffic and the answers
     *            of the turn running on the calling thread, or {@code null}
     *            when no turn runs on it
     * @param userMessages
     *            returns the user message to store in place of the given one
     */
    TurnScopedChatMemory(ChatMemory durable,
            Supplier<List<ChatMessage>> turnMessages,
            UnaryOperator<UserMessage> userMessages) {
        this.durable = durable;
        this.turnMessages = turnMessages;
        this.userMessages = userMessages;
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
        if (message instanceof UserMessage userMessage) {
            durable.add(userMessages.apply(userMessage));
            return;
        }
        var turn = turnMessages.get();
        if (isToolTraffic(message)) {
            if (turn != null) {
                turn.add(message);
            } else {
                durable.add(message);
            }
            return;
        }
        if (message instanceof AiMessage aiMessage) {
            var text = aiMessage.text();
            if (text == null || text.isEmpty()) {
                return;
            }
            if (turn != null) {
                turn.add(message);
            }
        }
        durable.add(message);
    }

    @Override
    public List<ChatMessage> messages() {
        var turn = turnMessages.get();
        if (turn == null) {
            return new ArrayList<>(durable.messages());
        }
        var messages = new ArrayList<ChatMessage>();
        for (var message : durable.messages()) {
            // An answer of the running turn is returned from its list
            if (turn.stream().noneMatch(inTurn -> inTurn == message)) {
                messages.add(message);
            }
        }
        messages.addAll(turn);
        return messages;
    }

    @Override
    public void clear() {
        var turn = turnMessages.get();
        if (turn != null) {
            turn.clear();
        }
        durable.clear();
    }

    private static boolean isToolTraffic(ChatMessage message) {
        return message instanceof ToolExecutionResultMessage
                || (message instanceof AiMessage aiMessage
                        && aiMessage.hasToolExecutionRequests());
    }
}
