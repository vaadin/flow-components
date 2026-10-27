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

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.grid.AIDataRow;
import com.vaadin.flow.component.ai.grid.GridAIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.SpringAILLMProvider;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.aura.Aura;

/**
 * Page for trying out {@link GridAIController} with a real AI chat against
 * OpenAI, rendered with the Aura theme. The grid on the left shows whatever the
 * chat on the right asks for, built from the sample data of
 * {@link InMemoryDatabaseProvider}, and the query the LLM wrote last is shown
 * under the grid. The API key is read from the {@code OPENAI_API_KEY}
 * environment variable. Not covered by automated tests.
 * <p>
 * Open at {@code http://localhost:8080/vaadin-ai/grid-ai-chat}.
 * </p>
 */
@Route("vaadin-ai/grid-ai-chat")
@StyleSheet(Aura.STYLESHEET)
public class GridAIChatPage extends HorizontalLayout {

    private static final String MODEL_NAME = "gpt-5.4-mini";

    private static final String SYSTEM_PROMPT = """
            You are a data assistant. Answer the user's questions about the \
            data by showing the matching rows in the grid next to the chat, \
            and keep your text answers short.""";

    public GridAIChatPage() {
        setSizeFull();
        // The response arrives on the LLM client's threads, so it needs push
        // to reach the browser
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        var apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            add(new Span("Set the OPENAI_API_KEY environment variable and "
                    + "restart the server to use this page."));
            return;
        }

        var grid = new Grid<AIDataRow>();
        grid.setSizeFull();

        var queryView = new Pre();
        queryView.setId("query-view");
        queryView.setWidthFull();
        queryView.getStyle().set("max-height", "8em").set("overflow", "auto")
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "anywhere");

        var controller = new GridAIController(grid,
                new InMemoryDatabaseProvider());
        controller.addStateChangeListener(
                state -> queryView.setText(state.query()));

        var messageList = new MessageList();
        messageList.setMarkdown(true);
        messageList.setWidthFull();

        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        var options = OpenAiChatOptions.builder().apiKey(apiKey)
                .model(MODEL_NAME).build();
        var chatModel = OpenAiChatModel.builder().options(options).build();
        AIOrchestrator
                .builder(new SpringAILLMProvider(chatModel), SYSTEM_PROMPT)
                .withMessageList(messageList).withInput(messageInput)
                .withController(controller).build();

        var gridPane = new VerticalLayout(grid, queryView);
        gridPane.setSizeFull();
        gridPane.expand(grid);

        var chatPane = new VerticalLayout(messageList, messageInput);
        chatPane.setSizeFull();
        chatPane.expand(messageList);

        add(gridPane, chatPane);
        expand(gridPane, chatPane);
    }
}
