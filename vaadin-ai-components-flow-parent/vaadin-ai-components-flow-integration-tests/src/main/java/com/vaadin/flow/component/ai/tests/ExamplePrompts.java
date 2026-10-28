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

import java.util.List;

import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Example prompts of an AI chat page, shown as a wrapping row of buttons. A
 * button shows a short label and the whole prompt as its tooltip, and a click
 * sends the prompt as if the user had typed it.
 */
final class ExamplePrompts extends HorizontalLayout {

    /**
     * A prompt with the short label its button shows.
     *
     * @param label
     *            the button text
     * @param text
     *            the prompt sent to the chat
     */
    record Prompt(String label, String text) {
    }

    /**
     * Creates a button for each prompt.
     *
     * @param orchestrator
     *            the orchestrator of the chat the prompts are sent to
     * @param prompts
     *            the prompts, in button order
     */
    ExamplePrompts(AIOrchestrator orchestrator, List<Prompt> prompts) {
        setId("example-prompts");
        setWidthFull();
        setWrap(true);
        for (var prompt : prompts) {
            var button = new NativeButton(prompt.label(),
                    event -> orchestrator.prompt(prompt.text()));
            button.setTitle(prompt.text());
            add(button);
        }
    }
}
