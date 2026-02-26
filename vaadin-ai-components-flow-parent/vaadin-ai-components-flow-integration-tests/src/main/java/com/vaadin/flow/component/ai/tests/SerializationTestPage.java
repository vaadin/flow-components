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
package com.vaadin.flow.component.ai.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import reactor.core.publisher.Flux;

@Route("vaadin-ai/serialization")
public class SerializationTestPage extends VerticalLayout {

    static final Path SERIALIZED_PAGE_STATE_FILE_PATH = Paths.get(
            "vaadin-ai-components-flow-parent",
            "vaadin-ai-components-flow-integration-tests", "TESTFOLDER",
            "testFile.ser");

    private MessageList messageList;
    private MessageInput messageInput;
    private AIOrchestrator orchestrator;
    private Span responseCountSpan;
    private Span sizeSpan;

    public SerializationTestPage() throws Exception {
        int dataLength;
        if (Files.exists(SERIALIZED_PAGE_STATE_FILE_PATH)) {
            dataLength = simulateDeserialization();
            orchestrator.reconnect(new EchoLLMProvider()).apply();
        } else {
            init();
            dataLength = 0;
        }
        setHeightFull();
        add(messageList, messageInput, getControlLayout());
        sizeSpan.setText(String.valueOf(dataLength));
    }

    private int simulateDeserialization()
            throws IOException, ClassNotFoundException {
        var data = readSerializedData();
        var storedPageState = (PageState) new ObjectInputStream(
                new ByteArrayInputStream(data)).readObject();
        restoreFields(storedPageState);
        detachComponentsFromOldUI();
        return data.length;
    }

    private VerticalLayout getControlLayout() {
        var serializeButton = new NativeButton("Serialize", e -> {
            try {
                var pageState = new PageState(orchestrator, messageList,
                        messageInput, responseCountSpan, sizeSpan);
                var data = storePageState(pageState);
                sizeSpan.setText(String.valueOf(data.length));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        serializeButton.setId("serialize-button");
        var deserializeButton = new NativeButton("Deserialize", e -> e
                .getSource().getUI().ifPresent(ui -> ui.getPage().reload()));
        deserializeButton.setId("deserialize-button");
        return new VerticalLayout(new Div(serializeButton, deserializeButton),
                sizeSpan, responseCountSpan);
    }

    private void init() {
        messageList = new MessageList();
        messageList.setSizeFull();
        messageInput = new MessageInput();
        messageInput.setWidthFull();
        responseCountSpan = new Span("0");
        responseCountSpan.setId("response-count-span");
        sizeSpan = new Span();
        sizeSpan.setId("size-span");
        orchestrator = AIOrchestrator.builder(new EchoLLMProvider(), null)
                .withMessageList(messageList).withInput(messageInput)
                .withResponseCompleteListener(event -> {
                    var count = Integer.parseInt(responseCountSpan.getText())
                            + 1;
                    responseCountSpan.setText(String.valueOf(count));
                }).build();
    }

    private static byte[] readSerializedData() throws IOException {
        return Files.readAllBytes(SERIALIZED_PAGE_STATE_FILE_PATH);
    }

    private void restoreFields(PageState pageState) {
        orchestrator = pageState.orchestrator();
        messageList = pageState.messageList();
        messageInput = pageState.messageInput();
        responseCountSpan = pageState.responseCountSpan();
        sizeSpan = pageState.sizeSpan();
    }

    private void detachComponentsFromOldUI() {
        messageList.getElement().getNode().removeFromTree();
        messageInput.getElement().getNode().removeFromTree();
        responseCountSpan.getElement().getNode().removeFromTree();
        sizeSpan.getElement().getNode().removeFromTree();
    }

    private byte[] storePageState(PageState pageState) throws Exception {
        var bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(pageState);
        var data = bos.toByteArray();
        Files.createDirectories(SERIALIZED_PAGE_STATE_FILE_PATH.getParent());
        Files.write(SERIALIZED_PAGE_STATE_FILE_PATH, data);
        return data;
    }

    private record PageState(AIOrchestrator orchestrator,
            MessageList messageList, MessageInput messageInput,
            Span responseCountSpan, Span sizeSpan) implements Serializable {
    }

    private static class EchoLLMProvider implements LLMProvider {
        @Override
        public Flux<String> stream(LLMRequest request) {
            var response = "Echo: " + request.userMessage();
            return Flux.fromArray(response.split(" ")).map(word -> word + " ");
        }

        @Override
        public void setHistory(List<ChatMessage> history,
                Map<String, List<AIAttachment>> attachmentsByMessageId) {
            // No-op for echo provider
        }
    }
}
