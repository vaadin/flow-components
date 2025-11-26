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
package com.vaadin.flow.component.ai.tests;

import com.vaadin.flow.component.ai.chat.AiChatOrchestrator;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.lumo.LumoIcon;

import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Demo view for AI Chat functionality.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/ai-chat-demo-experimental-component-features")
@CssImport("@vaadin/vaadin-lumo-styles/lumo.css")
public class AiChatExperimentalComponentFeaturesDemoView extends VerticalLayout {

    public AiChatExperimentalComponentFeaturesDemoView() {
        setSizeFull();

        // Create UI components
        var messageList = new MessageList();
        messageList.setSizeFull();
        messageList.setSnapToBottom(true);
        var messageInput = new MessageInput();

        // Upload Component for attachments
        var upload = new Upload();
        upload.setWidthFull();
        upload.setMaxFiles(5);
        upload.setMaxFileSize(5 * 1024 * 1024); // 5 MB
        upload.getThemeNames().add("compact");
        upload.getElement().setProperty("fileListAbove", true);
        upload.setAcceptedFileTypes("image/*", "application/pdf",
                "text/plain");
        upload.setUploadButton(new Button(LumoIcon.UPLOAD.create()));
        upload.setDropAreaContent(messageInput);
        
        add(messageList, upload);
        setFlexGrow(1, messageList);
        setFlexShrink(0, upload);

        // Create LLM provider
        var model = OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o-mini").build();
        var provider = new LangChain4JLLMProvider(model);

        // Create and configure orchestrator with input validation
        AiChatOrchestrator.create(provider)
                .withMessageList(messageList)
                .withInput(messageInput)
                .withFileReceiver(upload)
                .withInputValidator(new PromptInjectionValidator())
                .build();
    }
}
