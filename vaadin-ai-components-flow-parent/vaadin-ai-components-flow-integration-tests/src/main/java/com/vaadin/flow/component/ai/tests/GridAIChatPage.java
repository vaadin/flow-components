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
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.SpringAILLMProvider;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.aura.Aura;

/**
 * Page for trying out {@link GridAIController} with a real AI chat against
 * OpenAI, rendered with the Aura theme. The grid on the left shows whatever the
 * chat on the right asks for, built from the sample data behind the provider
 * the {@code provider} query parameter names, see
 * {@link SampleDatabaseProviders}; without the parameter it is the H2 database
 * of {@link InMemoryDatabaseProvider}. The API key is read from the
 * {@code OPENAI_API_KEY} environment variable. Not covered by automated tests.
 * <p>
 * The buttons above the message input send example prompts that need joins,
 * aggregates or comparisons across rows.
 * </p>
 * <p>
 * Open at {@code http://localhost:8080/vaadin-ai/grid-ai-chat}, or at
 * {@code http://localhost:8080/vaadin-ai/grid-ai-chat?provider=federated} for
 * the REST and CSV endpoints federated with Apache Calcite.
 * </p>
 */
@Route("vaadin-ai/grid-ai-chat")
@StyleSheet(Aura.STYLESHEET)
public class GridAIChatPage extends HorizontalLayout
        implements BeforeEnterObserver {

    private static final String MODEL_NAME = "gpt-5.4-mini";

    private static final String SYSTEM_PROMPT = """
            You are a data assistant. Answer the user's questions about the \
            data in the database by showing the rows in the grid next to the \
            chat, and keep your text answers short.""";

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

    private final VerticalLayout gridPane = new VerticalLayout();
    private final VerticalLayout chatPane = new VerticalLayout();
    private final Span status = new Span();

    private transient OpenAiChatModel chatModel;

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

        gridPane.setSizeFull();
        gridPane.add(status);
        chatPane.setSizeFull();
        add(gridPane, chatPane);
        expand(gridPane, chatPane);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (chatModel == null) {
            return;
        }
        var name = event.getLocation().getQueryParameters()
                .getSingleParameter("provider")
                .orElse(SampleDatabaseProviders.DEFAULT);
        status.setText("Data: " + SampleDatabaseProviders.describe(name));
        startChat(SampleDatabaseProviders.get(name));
    }

    private void startChat(DatabaseProvider provider) {
        var grid = new Grid<AIDataRow>();
        grid.setSizeFull();
        gridPane.add(grid);
        gridPane.expand(grid);
        var controller = new GridAIController(grid, provider);

        var messageList = new MessageList();
        messageList.setMarkdown(true);
        messageList.setWidthFull();

        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        var orchestrator = AIOrchestrator
                .builder(new SpringAILLMProvider(chatModel), SYSTEM_PROMPT)
                .withMessageList(messageList).withInput(messageInput)
                .withController(controller).build();

        chatPane.add(messageList, new ExamplePrompts(orchestrator, PROMPTS),
                messageInput);
        chatPane.expand(messageList);
    }
}
