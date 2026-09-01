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

import java.util.stream.Collectors;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.common.PageRegion;
import com.vaadin.flow.component.ai.common.SourceExtract;
import com.vaadin.flow.component.ai.common.ValueSource;
import com.vaadin.flow.component.ai.form.FormAIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LangChain4JLLMProvider;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Minimal example of filling a form from an attached document, showing the
 * source the model reports for each value. Needs a real LLM: set the
 * {@code OPENAI_API_KEY} environment variable before opening the page.
 */
@Route("vaadin-ai/form-source-tracking")
public class FormSourceTrackingPage extends VerticalLayout {

    public FormSourceTrackingPage() {
        // Streamed tokens need push (or polling) to reach the browser.
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        var form = new VerticalLayout(new TextField("Name"),
                new EmailField("Email"), new TextField("Company"),
                new TextField("Invoice total"));

        var controller = new FormAIController(form);
        controller.setSourceTrackingEnabled(true);
        controller.setFieldMarkerPopoverContentProvider(change -> change
                .getFieldSource().map(FormSourceTrackingPage::describe)
                .map(Span::new).orElse(null));

        var model = OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-5.4-mini").build();

        var uploads = new UploadManager(this);
        var messages = new MessageList();
        var input = new MessageInput();

        // No system prompt: FormAIController's own tool descriptions already
        // tell the model how to fill a form.
        AIOrchestrator.builder(new LangChain4JLLMProvider(model), null)
                .withController(controller).withFileReceiver(uploads)
                .withMessageList(messages).withInput(input).build();

        var inputRow = new HorizontalLayout(new UploadButton("Attach", uploads),
                input);
        inputRow.setWidthFull();
        inputRow.expand(input);

        add(form, messages, new UploadFileList(uploads), inputRow);
    }

    /**
     * @return the reported source as text: how sure the model was, and the
     *         snippets it says it read
     */
    private static String describe(ValueSource source) {
        return "%s confidence, read from %s".formatted(source.confidence(),
                source.extracts().stream().map(FormSourceTrackingPage::describe)
                        .collect(Collectors.joining("; ")));
    }

    /**
     * @return one snippet as text, with its place in the document when the
     *         model reported one
     */
    private static String describe(SourceExtract extract) {
        var place = extract.location() instanceof PageRegion region
                ? " (page " + region.page() + ")"
                : "";
        return '"' + extract.text() + '"' + place;
    }
}
