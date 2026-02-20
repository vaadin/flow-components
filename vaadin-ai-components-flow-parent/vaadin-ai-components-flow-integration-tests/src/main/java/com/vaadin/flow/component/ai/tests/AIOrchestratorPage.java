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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadFileListVariant;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.router.Route;

import reactor.core.publisher.Flux;

/**
 * Test page for AIOrchestrator.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/orchestrator")
public class AIOrchestratorPage extends UploadDropZone {

    private final AIOrchestrator orchestrator;

    // Attachment storage keyed by message ID
    private final Map<String, List<AIAttachment>> attachmentStorage = new HashMap<>();

    // Displays info about the last clicked attachment
    private final Span clickedAttachmentInfo = new Span();

    public AIOrchestratorPage() {
        setHeightFull();

        var messageList = new MessageList();
        messageList.setId("message-list");
        messageList.setSizeFull();

        var uploadManager = new UploadManager(this);
        setUploadManager(uploadManager);

        var uploadButton = new UploadButton(uploadManager);
        uploadButton.setIcon(VaadinIcon.UPLOAD.create());
        var fileList = new UploadFileList(uploadManager);
        fileList.addThemeVariants(UploadFileListVariant.THUMBNAILS);

        var messageInput = new MessageInput();
        messageInput.setId("message-input");
        messageInput.getStyle().set("flexGrow", "1");

        clickedAttachmentInfo.setId("clicked-attachment-info");

        orchestrator = AIOrchestrator.builder(new EchoLLMProvider(), null)
                .withMessageList(messageList).withInput(messageInput)
                .withFileReceiver(uploadManager)
                .withAttachmentSubmitListener(event -> {
                    attachmentStorage.put(event.getMessageId(),
                            event.getAttachments());
                }).withAttachmentClickListener(event -> {
                    var attachments = attachmentStorage
                            .get(event.getMessageId());
                    if (attachments != null) {
                        var attachment = attachments
                                .get(event.getAttachmentIndex());
                        clickedAttachmentInfo.setText(attachment.name() + " | "
                                + attachment.mimeType());
                    }
                }).build();

        var promptButton = new NativeButton("Send Hello",
                e -> orchestrator.prompt("Hello from button"));
        promptButton.setId("prompt-button");

        var inputLayout = new Div(uploadButton, messageInput);
        inputLayout.getStyle().set("display", "flex");
        inputLayout.setWidthFull();

        var chatLayout = new Div(messageList, fileList, inputLayout,
                promptButton, clickedAttachmentInfo);
        chatLayout.getStyle().set("display", "flex");
        chatLayout.getStyle().set("flexDirection", "column");
        chatLayout.setSizeFull();

        setContent(chatLayout);
    }

    /**
     * A simple LLM provider that echoes the user message back.
     */
    private static class EchoLLMProvider implements LLMProvider {
        @Override
        public Flux<String> stream(LLMRequest request) {
            var response = "Echo: " + request.userMessage();
            return Flux.fromArray(response.split(" ")).map(word -> word + " ");
        }
    }
}
