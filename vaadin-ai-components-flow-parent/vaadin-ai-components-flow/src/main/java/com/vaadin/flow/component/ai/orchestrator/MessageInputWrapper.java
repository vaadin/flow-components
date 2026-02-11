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
package com.vaadin.flow.component.ai.orchestrator;

import com.vaadin.flow.component.ai.component.AiInput;
import com.vaadin.flow.component.ai.component.InputSubmitListener;
import com.vaadin.flow.component.messages.MessageInput;

/**
 * Wrapper for Flow MessageInput component to implement AiInput interface.
 */
record MessageInputWrapper(MessageInput messageInput) implements AiInput {

    @Override
    public void addSubmitListener(InputSubmitListener listener) {
        messageInput
                .addSubmitListener(event -> listener.onSubmit(event::getValue));
    }
}
