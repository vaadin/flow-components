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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import reactor.core.publisher.Flux;

/**
 * Test page for disabling submitting on the input of the AIOrchestrator while a
 * file is being uploaded.
 */
@Route("vaadin-ai/orchestrator-submit-disabled")
public class AIOrchestratorSubmitDisabledPage extends VerticalLayout {

    public AIOrchestratorSubmitDisabledPage() {
        // The orchestrator disables submitting while the upload request is
        // being handled, so the change needs push to reach the browser
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        var uploadManager = new UploadManager(this);
        var uploadButton = new UploadButton(uploadManager);

        var messageInput = new MessageInput();
        messageInput.setId("message-input");

        AIOrchestrator
                .builder(request -> Flux.just("Echo: " + request.userMessage()),
                        null)
                .withInput(messageInput).withFileReceiver(uploadManager)
                .build();

        add(uploadButton, messageInput);
    }
}
