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
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

class TurnScopedChatMemoryTest {

    private final ToolExecutionRequest toolRequest = ToolExecutionRequest
            .builder().id("1").name("myTool").arguments("{}").build();
    private final AiMessage toolCall = AiMessage.from(List.of(toolRequest));
    private final ToolExecutionResultMessage toolResult = ToolExecutionResultMessage
            .from(toolRequest, "result");

    private MessageWindowChatMemory durable;
    private List<ChatMessage> turn;
    private boolean turnRunning;
    private UnaryOperator<UserMessage> userMessages;
    private TurnScopedChatMemory memory;

    @BeforeEach
    void setup() {
        durable = MessageWindowChatMemory.withMaxMessages(10);
        turn = new ArrayList<>();
        turnRunning = true;
        userMessages = UnaryOperator.identity();
        memory = new TurnScopedChatMemory(durable,
                () -> turnRunning ? turn : null,
                message -> userMessages.apply(message));
    }

    @Test
    void userMessage_isStoredAsCompleted() {
        userMessages = message -> UserMessage
                .from(message.singleText() + " and its attachment");

        memory.add(UserMessage.from("Question"));

        Assertions.assertEquals(
                List.of(UserMessage.from("Question and its attachment")),
                durable.messages());
    }

    @Test
    void toolTrafficInTurn_isReturnedButNotKept() {
        memory.add(UserMessage.from("Question"));
        memory.add(toolCall);
        memory.add(toolResult);

        Assertions.assertEquals(
                List.of(UserMessage.from("Question"), toolCall, toolResult),
                memory.messages());
        Assertions.assertEquals(List.of(UserMessage.from("Question")),
                durable.messages());
    }

    @Test
    void finalAnswer_followsToolTrafficOfTurnAndIsKeptWithoutIt() {
        memory.add(UserMessage.from("Question"));
        memory.add(toolCall);
        memory.add(toolResult);

        memory.add(AiMessage.from("Answer"));

        Assertions.assertEquals(List.of(UserMessage.from("Question"), toolCall,
                toolResult, AiMessage.from("Answer")), memory.messages());
        turnRunning = false;
        Assertions.assertEquals(
                List.of(UserMessage.from("Question"), AiMessage.from("Answer")),
                memory.messages());
    }

    @Test
    void finalAnswerWithoutText_isNotKept() {
        memory.add(UserMessage.from("Question"));

        memory.add(AiMessage.builder().build());

        Assertions.assertEquals(List.of(UserMessage.from("Question")),
                memory.messages());
    }

    @Test
    void toolTrafficOutsideTurn_goesToDurableMemory() {
        turnRunning = false;

        memory.add(toolCall);
        memory.add(toolResult);

        Assertions.assertEquals(List.of(toolCall, toolResult),
                durable.messages());
    }

    @Test
    void messagesOutsideTurn_returnsDurableMemoryOnly() {
        memory.add(UserMessage.from("Question"));
        memory.add(toolCall);
        turnRunning = false;

        Assertions.assertEquals(List.of(UserMessage.from("Question")),
                memory.messages());
    }

    @Test
    void clear_clearsDurableMemoryAndTurn() {
        memory.add(UserMessage.from("Question"));
        memory.add(toolCall);

        memory.clear();

        Assertions.assertTrue(memory.messages().isEmpty());
        Assertions.assertTrue(turn.isEmpty());
    }

    @Test
    void replaceAnswer_replacesAnswerOfTurnAfterItsToolTraffic() {
        memory.add(UserMessage.from("Question"));
        memory.add(toolCall);
        memory.add(toolResult);
        memory.add(AiMessage.from("Answer"));

        memory.replaceAnswer(turn, "Checked answer");

        Assertions
                .assertEquals(
                        List.of(UserMessage.from("Question"), toolCall,
                                toolResult, AiMessage.from("Checked answer")),
                        memory.messages());
        Assertions.assertEquals(List.of(UserMessage.from("Question"),
                AiMessage.from("Checked answer")), durable.messages());
    }

    @Test
    void replaceAnswer_leavesEqualAnswerOfEarlierTurn() {
        durable.add(UserMessage.from("Earlier question"));
        durable.add(AiMessage.from("Answer"));
        memory.add(UserMessage.from("Question"));
        memory.add(AiMessage.from("Answer"));

        memory.replaceAnswer(turn, "Checked answer");

        Assertions.assertEquals(List.of(UserMessage.from("Earlier question"),
                AiMessage.from("Answer"), UserMessage.from("Question"),
                AiMessage.from("Checked answer")), durable.messages());
    }

    @Test
    void replaceAnswer_sameText_keepsStoredMessage() {
        var answer = AiMessage.builder().text("Answer").thinking("Thought")
                .build();
        memory.add(UserMessage.from("Question"));
        memory.add(answer);

        memory.replaceAnswer(turn, "Answer");

        Assertions.assertEquals(List.of(UserMessage.from("Question"), answer),
                durable.messages());
    }

    @Test
    void replaceAnswer_withoutText_removesAnswer() {
        memory.add(UserMessage.from("Question"));
        memory.add(AiMessage.from("Answer"));

        memory.replaceAnswer(turn, null);

        Assertions.assertEquals(List.of(UserMessage.from("Question")),
                memory.messages());
        Assertions.assertEquals(List.of(UserMessage.from("Question")),
                durable.messages());
    }

    @Test
    void replaceAnswer_withoutText_answerIsOnlyMessage_emptiesDurableMemory() {
        memory.add(AiMessage.from("Answer"));

        memory.replaceAnswer(turn, null);

        Assertions.assertTrue(durable.messages().isEmpty());
    }

    @Test
    void replaceAnswer_messageAfterAnswer_replacesAnswerAndKeepsMessage() {
        memory.add(UserMessage.from("Question"));
        memory.add(AiMessage.from("Answer"));
        durable.add(UserMessage.from("Next question"));

        memory.replaceAnswer(turn, "Checked answer");

        Assertions.assertEquals(List.of(UserMessage.from("Question"),
                AiMessage.from("Checked answer"),
                UserMessage.from("Next question")), durable.messages());
    }

    @Test
    void replaceAnswer_turnWithoutAnswer_changesNothing() {
        durable.add(UserMessage.from("Earlier question"));
        durable.add(AiMessage.from("Earlier answer"));
        memory.add(UserMessage.from("Question"));
        memory.add(toolCall);

        memory.replaceAnswer(turn, "Checked answer");

        Assertions.assertEquals(List.of(UserMessage.from("Earlier question"),
                AiMessage.from("Earlier answer"), UserMessage.from("Question")),
                durable.messages());
        Assertions.assertEquals(List.of(toolCall), turn);
    }

    @Test
    void nullMessage_isIgnored() {
        memory.add((ChatMessage) null);

        Assertions.assertTrue(memory.messages().isEmpty());
    }
}
