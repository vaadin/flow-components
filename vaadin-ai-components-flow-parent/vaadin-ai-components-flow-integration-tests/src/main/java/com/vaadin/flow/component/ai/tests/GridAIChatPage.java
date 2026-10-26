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

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.grid.AIDataRow;
import com.vaadin.flow.component.ai.grid.GridAIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.SpringAILLMProvider;
import com.vaadin.flow.component.ai.tests.providers.SampleProviders;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.aura.Aura;

/**
 * Page for trying out {@link GridAIController} with a real AI chat against
 * OpenAI, rendered with the Aura theme. The grid on the left shows whatever the
 * chat on the right asks for, built from the sample data behind the provider
 * chosen in the selector above the grid: any of {@link SampleProviders}. The
 * grid pages, sorts and counts through the provider, so the selector also shows
 * how each kind of endpoint handles those. Changing the provider starts a new
 * chat, and the query the LLM wrote last is shown under the grid. The
 * {@code provider} query parameter picks the provider to start with. The API
 * key is read from the {@code OPENAI_API_KEY} environment variable. Not covered
 * by automated tests.
 * <p>
 * The buttons above the message input send example prompts that need joins,
 * aggregates or comparisons across rows.
 * </p>
 * <p>
 * Open at {@code http://localhost:8080/vaadin-ai/grid-ai-chat}.
 * </p>
 */
@Route("vaadin-ai/grid-ai-chat")
@StyleSheet(Aura.STYLESHEET)
public class GridAIChatPage extends HorizontalLayout
        implements BeforeEnterObserver {

    private static final String MODEL_NAME = "gpt-5.4-mini";

    private static final String SYSTEM_PROMPT = """
            You are a data assistant. Answer the user's questions about the \
            data by showing the matching rows in the grid next to the chat, \
            and keep your text answers short.""";

    private static final List<ExamplePrompts.Prompt> PROMPTS = List.of(
            new ExamplePrompts.Prompt("Salaries vs. average",
                    "List the employees with their salary, their department's average salary and how far above or below it they are."),
            new ExamplePrompts.Prompt("Month-over-month revenue",
                    "Show the monthly revenue of each region in chronological order, with the change from the previous month in percent."),
            new ExamplePrompts.Prompt("Product revenue share",
                    "Rank the products by revenue, units sold times price, and show each product's share of the total revenue."),
            new ExamplePrompts.Prompt("Reporting lines",
                    "Show every member of the organization with their manager's name and the number of people reporting directly to them."),
            new ExamplePrompts.Prompt("Task dependencies",
                    "List the project tasks in start order with the task each one depends on, the duration in days and the progress in percent."),
            new ExamplePrompts.Prompt("Pipeline conversion",
                    "Show the sales pipeline stages in order with the conversion rate from the previous stage and from the first stage."),
            new ExamplePrompts.Prompt("Busiest hours",
                    "Which hours of the day get the most website visitors on average across the weekdays? Show every hour as a clock time, busiest first."),
            new ExamplePrompts.Prompt("Rising trading days",
                    "Show the trading days on which ACME closed higher than it opened, with the gain in percent and the day's price range."));

    private final Select<String> providerSelect = new Select<>();
    private final VerticalLayout gridPane = new VerticalLayout();
    private final VerticalLayout chatPane = new VerticalLayout();
    private final Span status = new Span();
    private final Pre queryView = new Pre();

    private transient OpenAiChatModel chatModel;
    private Grid<AIDataRow> grid;
    private GridAIController controller;

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

        var options = OpenAiChatOptions.builder().apiKey(apiKey)
                .model(MODEL_NAME).build();
        chatModel = OpenAiChatModel.builder().options(options).build();

        providerSelect.setId("provider-select");
        providerSelect.setLabel("Data provider");
        providerSelect.setItems(SampleProviders.names());
        providerSelect.setItemLabelGenerator(
                name -> name + ": " + SampleProviders.describe(name));
        providerSelect.setValue(SampleProviders.DEFAULT);
        providerSelect.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                startChat();
                status.setText("Started a new chat with the " + event.getValue()
                        + " provider.");
            }
        });
        startChat();

        queryView.setId("query-view");
        queryView.setWidthFull();
        queryView.getStyle().set("max-height", "8em").set("overflow", "auto")
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "anywhere");

        gridPane.setSizeFull();
        gridPane.add(providerSelect, status, queryView);
        chatPane.setSizeFull();

        add(gridPane, chatPane);
        expand(gridPane, chatPane);
    }

    /**
     * Creates a grid with its controller and a chat with a new orchestrator,
     * replacing the current ones in the page.
     */
    private void startChat() {
        var newGrid = new Grid<AIDataRow>();
        newGrid.setSizeFull();
        if (grid == null) {
            gridPane.addComponentAsFirst(newGrid);
        } else {
            gridPane.replace(grid, newGrid);
        }
        grid = newGrid;
        gridPane.expand(grid);
        controller = new GridAIController(grid,
                SampleProviders.get(providerSelect.getValue()));
        controller.addStateChangeListener(
                state -> queryView.setText(state.query()));
        queryView.setText("");

        var messageList = new MessageList();
        messageList.setMarkdown(true);
        messageList.setWidthFull();

        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        var orchestrator = AIOrchestrator
                .builder(new SpringAILLMProvider(chatModel), SYSTEM_PROMPT)
                .withMessageList(messageList).withInput(messageInput)
                .withController(controller).build();

        chatPane.removeAll();
        chatPane.add(messageList, new ExamplePrompts(orchestrator, PROMPTS),
                messageInput);
        chatPane.expand(messageList);
    }

    /**
     * Starts with the provider named by the {@code provider} query parameter,
     * when there is one. The constructor cannot read it: the view is created in
     * the request of the client-side navigation, not in the page request.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (chatModel == null) {
            return;
        }
        event.getLocation().getQueryParameters().getSingleParameter("provider")
                .filter(SampleProviders.names()::contains)
                .filter(name -> !name.equals(providerSelect.getValue()))
                .ifPresent(name -> {
                    providerSelect.setValue(name);
                    startChat();
                });
    }
}
