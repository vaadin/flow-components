/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.tests;

import com.vaadin.flow.component.ai.orchestrator.AiOrchestrator;
import com.vaadin.flow.component.ai.pro.chart.ChartTools;
import com.vaadin.flow.component.ai.pro.chart.DefaultDataConverter;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.router.Route;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Demo view for AI Dashboard with multiple visualization types.
 * <p>
 * Demonstrates one orchestrator per widget pattern where each widget has its
 * own AiOrchestrator and tools, but the dashboard uses a single shared chat
 * interface. Supports:
 * - Charts (line, bar, pie, column, area)
 * - Grids (tables with sortable columns)
 * - KPIs (key performance indicator cards)
 * - Dynamic type switching ("show this as a table")
 * </p>
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/ai-dashboard-demo")
@CssImport("@vaadin/vaadin-lumo-styles/lumo.css")
public class AiDashboardDemoView extends VerticalLayout {

    private Dashboard dashboard;
    private Map<String, AiOrchestrator> orchestrators = new HashMap<>();
    private String apiKey;

    public AiDashboardDemoView() {
        setSizeFull();

        apiKey = System.getenv("OPENAI_API_KEY");

        // Create dashboard
        dashboard = new Dashboard();
        dashboard.setEditable(true);
        dashboard.setMaximumColumnCount(3);
        dashboard.setMinimumColumnWidth("300px");
        dashboard.setSizeFull();

        // Add FAB button for adding new widgets
        var addWidgetButton = new Button(VaadinIcon.PLUS.create());
        addWidgetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_LARGE);
        addWidgetButton.getStyle().set("position", "fixed")
                .set("bottom", "var(--lumo-space-l)")
                .set("right", "var(--lumo-space-l)")
                .set("border-radius", "50%").set("width", "56px")
                .set("height", "56px")
                .set("box-shadow", "0 4px 8px rgba(0,0,0,0.2)")
                .set("z-index", "1000");
        addWidgetButton.addClickListener(e -> addNewWidget());

        // Add initial sample widgets
        addSampleWidgets();

        add(dashboard, addWidgetButton);
    }

    private void addNewWidget() {
        getUI().ifPresent(ui -> ui.access(() -> {
            DashboardWidget widget = createEmptyWidget();
            dashboard.add(widget);
        }));
    }

    private void addSampleWidgets() {
        // Add a chart widget
        DashboardWidget chartWidget = createWidget("Revenue by Month");
        dashboard.add(chartWidget);

        // Add a grid widget
        DashboardWidget gridWidget = createWidget("Sales Data");
        dashboard.add(gridWidget);

        // Add a KPI widget
        DashboardWidget kpiWidget = createWidget("Total Revenue");
        dashboard.add(kpiWidget);
    }

    private DashboardWidget createEmptyWidget() {
        var widgetId = "widget-" + System.currentTimeMillis();

        // Create empty content
        var content = new VerticalLayout();
        content.setSizeFull();
        content.setJustifyContentMode(JustifyContentMode.CENTER);
        content.setAlignItems(Alignment.CENTER);
        content.add(VaadinIcon.DASHBOARD.create());

        // Create widget
        var widget = new DashboardWidget("Empty Widget");
        widget.setId(widgetId);
        widget.setRowspan(2);
        widget.setContent(content);

        // Add configure button
        addConfigureButton(widget, widgetId);

        return widget;
    }

    private DashboardWidget createWidget(String title) {
        var widgetId = "widget-" + System.currentTimeMillis();

        // Create visualization container
        var visualizationContainer = new Div();
        visualizationContainer.setSizeFull();

        // Create widget
        var widget = new DashboardWidget(title);
        widget.setId(widgetId);
        widget.setRowspan(2);
        widget.setContent(visualizationContainer);

        // Add configure button
        addConfigureButton(widget, widgetId);

        return widget;
    }

    private void addConfigureButton(DashboardWidget widget, String widgetId) {
        var configureButton = new Button(VaadinIcon.COG.create());
        configureButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_SMALL);
        configureButton.addClickListener(e -> {
            openWidgetChat(widgetId, widget.getTitle(), configureButton);
        });
        widget.setHeaderContent(configureButton);
    }

    private void openWidgetChat(String widgetId, String widgetTitle,
            Button configureButton) {
        // Create chat container
        var chatContainer = new VerticalLayout();
        chatContainer.setPadding(false);
        chatContainer.setWidth("600px");
        chatContainer.setHeight("500px");

        var messageList = new MessageList();
        messageList.setSizeFull();
        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        // Get or create orchestrator for this widget
        AiOrchestrator orchestrator = orchestrators.get(widgetId);
        if (orchestrator == null) {
            // Get the widget's visualization container
            var visualizationContainer = dashboard.getChildren()
                    .filter(c -> c instanceof DashboardWidget)
                    .map(c -> (DashboardWidget) c)
                    .filter(w -> widgetId.equals(w.getId().orElse(null)))
                    .findFirst()
                    .map(w -> (Div) w.getContent())
                    .orElseGet(() -> {
                        var container = new Div();
                        container.setSizeFull();
                        return container;
                    });

            var chart = new Chart();
            visualizationContainer.add(chart);

            // Create database provider and data converter
            var databaseProvider = new InMemoryDatabaseProvider();
            var dataConverter = new DefaultDataConverter();

            // Create chart tools for this widget
            var chartTools = ChartTools.createTools(chart,
                    databaseProvider, dataConverter);
            var systemPrompt = ChartTools.defaultPrompt();

            // Create LLM provider
            var model = OpenAiStreamingChatModel.builder()
                    .apiKey(apiKey).modelName("gpt-4o-mini").build();
            var provider = new LangChain4JLLMProvider(model);

            // Create orchestrator for this widget
            // NOTE: we do NOT call .withInput(...) here, because input is shared
            orchestrator = AiOrchestrator.builder(provider)
                    .withSystemPrompt(systemPrompt)
                    .withTools(chartTools)
                    .withMessageList(messageList)
                    .build();

            orchestrators.put(widgetId, orchestrator);
        }

        // Route input to the widget's orchestrator
        var finalOrchestrator = orchestrator;
        messageInput.addSubmitListener(
                (com.vaadin.flow.component.messages.MessageInput.SubmitEvent e) -> {
                    finalOrchestrator.prompt(e.getValue());
                });

        var closeButton = new Button("Close");
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var footer = new HorizontalLayout(closeButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.END);

        chatContainer.add(messageList, messageInput, footer);

        // Create popover attached to the configure button
        var popover = new Popover();
        popover.setTarget(configureButton);
        popover.setPosition(PopoverPosition.END_TOP);
        popover.setModal(true);
        popover.setCloseOnOutsideClick(true);
        popover.add(chatContainer);

        closeButton.addClickListener(e -> popover.close());

        popover.open();
    }
}
