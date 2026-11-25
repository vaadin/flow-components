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
 * attachments of the turn. A rewrite with {@link #set(Iterable)} stores the
 * messages as given. The owner also replaces the answer of a turn whose output
 * guardrails returned another one, see {@link #replaceAnswer(List, String)}.
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

    /**
     * Replaces the messages, keeping each where it was: the tool traffic and
     * the answers of the running turn in the list of the turn, and the other
     * messages in the wrapped memory. LangChain4j rewrites the memory this way
     * within a turn when it rolls back tools. The messages are stored as given,
     * so a user message is not completed again.
     */
    @Override
    public void set(Iterable<ChatMessage> messages) {
        if (messages == null || !messages.iterator().hasNext()) {
            throw new IllegalArgumentException(
                    "messages must not be null or empty");
        }
        var turn = turnMessages.get();
        var kept = new ArrayList<ChatMessage>();
        var inTurn = new ArrayList<ChatMessage>();
        for (var message : messages) {
            if (turn == null) {
                kept.add(message);
            } else if (isToolTraffic(message)) {
                inTurn.add(message);
            } else {
                // An answer of the running turn is in both, see add()
                if (turn.stream().anyMatch(answer -> answer == message)) {
                    inTurn.add(message);
                }
                kept.add(message);
            }
        }
        if (turn != null) {
            turn.clear();
            turn.addAll(inTurn);
        }
        if (kept.isEmpty()) {
            durable.clear();
        } else {
            durable.set(kept);
        }
    }

    @Override
    public void clear() {
        var turn = turnMessages.get();
        if (turn != null) {
            turn.clear();
        }
        durable.clear();
    }

    /**
     * Stores the given text as the answer of a turn, in place of the answer the
     * turn stored last. LangChain4j adds the answer of the model before the
     * output guardrails run and leaves it as it is, also when a guardrail
     * rewrites it or accepts a later answer after a reprompt. With the answer
     * replaced, the conversation continues from what the turn delivered.
     * <p>
     * Does nothing when the turn stored no answer, which is the case when the
     * service uses another chat memory, and when the stored answer already has
     * the given text. Without a text, the stored answer is removed.
     *
     * @param turn
     *            the list that holds the tool traffic and the answers of the
     *            turn
     * @param text
     *            the text of the answer the turn delivered, or {@code null} if
     *            it delivered none
     */
    void replaceAnswer(List<ChatMessage> turn, String text) {
        var answer = turn.stream().filter(AiMessage.class::isInstance)
                .map(AiMessage.class::cast)
                .filter(message -> !message.hasToolExecutionRequests())
                .reduce((first, second) -> second).orElse(null);
        if (answer == null || answer.text().equals(text)) {
            return;
        }
        var replacement = text == null || text.isEmpty() ? null
                : AiMessage.from(text);
        replace(turn, answer, replacement);
        var kept = new ArrayList<>(durable.messages());
        if (!replace(kept, answer, replacement)) {
            return;
        }
        if (kept.isEmpty()) {
            durable.clear();
        } else {
            durable.set(kept);
        }
    }

    private static boolean replace(List<ChatMessage> messages,
            ChatMessage message, ChatMessage replacement) {
        for (var i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i) == message) {
                if (replacement == null) {
                    messages.remove(i);
                } else {
                    messages.set(i, replacement);
                }
                return true;
            }
        }
        return false;
    }

    private static boolean isToolTraffic(ChatMessage message) {
        return message instanceof ToolExecutionResultMessage
                || (message instanceof AiMessage aiMessage
                        && aiMessage.hasToolExecutionRequests());
    }
}
