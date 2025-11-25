/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.tests;

import com.vaadin.flow.component.ai.chat.AiChatOrchestrator;
import com.vaadin.flow.component.ai.pro.chart.DataVisualizationPlugin;
import com.vaadin.flow.component.ai.pro.chart.VisualizationType;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Demo view for AI Dashboard with multiple visualization types.
 * <p>
 * Demonstrates the new AiDataVisualizationOrchestrator which supports:
 * - Charts (line, bar, pie, column, area)
 * - Grids (tables with sortable columns)
 * - KPIs (key performance indicator cards)
 * - Dynamic type switching ("show this as a table")
 * </p>
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/ai-dashboard-demo")
public class AiDashboardDemoView extends VerticalLayout {

    private Dashboard dashboard;
    private Map<String, DataVisualizationPlugin> plugins = new HashMap<>();
    private String apiKey;

    public AiDashboardDemoView() {
        // Enable push for streaming responses
        getUI().ifPresent(ui -> ui.getPushConfiguration()
                .setPushMode(PushMode.AUTOMATIC));

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        H2 title = new H2("AI Dashboard Demo");
        add(title);

        // Check for API key
        apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            Div error = new Div();
            error.setText(
                    "Error: OPENAI_API_KEY environment variable is not set. "
                            + "Please set it to use this demo.");
            error.getStyle().set("color", "red").set("padding", "20px");
            add(error);
            return;
        }

        // Instructions
        Paragraph instructions = new Paragraph(
                "This demo showcases the new AI Data Visualization Orchestrator which supports charts, grids, and KPIs. "
                        + "Each widget has its own AI chat - click the configure button to interact. "
                        + "Try: 'Show monthly revenue as a chart', 'Convert this to a table', 'Show total revenue as a KPI'");
        instructions.getStyle().set("color", "gray");
        add(instructions);

        // Create dashboard
        dashboard = new Dashboard();
        dashboard.setEditable(true);
        dashboard.setMaximumColumnCount(3);
        dashboard.setMinimumColumnWidth("300px");
        dashboard.setGap("var(--lumo-space-m)");
        dashboard.setSizeFull();

        // Add FAB button for adding new widgets
        Button addWidgetButton = new Button(VaadinIcon.PLUS.create());
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
        DashboardWidget chartWidget = createWidget("Revenue by Month",
                VisualizationType.CHART);
        dashboard.add(chartWidget);

        // Add a grid widget
        DashboardWidget gridWidget = createWidget("Sales Data",
                VisualizationType.GRID);
        dashboard.add(gridWidget);

        // Add a KPI widget
        DashboardWidget kpiWidget = createWidget("Total Revenue",
                VisualizationType.KPI);
        dashboard.add(kpiWidget);
    }

    private DashboardWidget createEmptyWidget() {
        String widgetId = "widget-" + System.currentTimeMillis();

        // Create empty content
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setJustifyContentMode(JustifyContentMode.CENTER);
        content.setAlignItems(Alignment.CENTER);

        Paragraph message = new Paragraph("Empty Widget");
        message.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Paragraph instruction = new Paragraph(
                "Click configure to set up this widget with AI");
        instruction.getStyle()
                .set("color", "var(--lumo-tertiary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        content.add(VaadinIcon.DASHBOARD.create(), message, instruction);

        // Create widget
        DashboardWidget widget = new DashboardWidget("Empty Widget");
        widget.setId(widgetId);
        widget.setRowspan(2);
        widget.setContent(content);

        // Add configure button
        addConfigureButton(widget, widgetId, null);

        return widget;
    }

    private DashboardWidget createWidget(String title,
            VisualizationType type) {
        String widgetId = "widget-" + System.currentTimeMillis();

        // Create visualization container
        Div visualizationContainer = new Div();
        visualizationContainer.setSizeFull();
        visualizationContainer.getStyle().set("overflow", "auto");

        // Create widget
        DashboardWidget widget = new DashboardWidget(title);
        widget.setId(widgetId);
        widget.setRowspan(2);
        widget.setContent(visualizationContainer);

        // Create orchestrator for this widget
        DataVisualizationPlugin plugin = createPlugin(visualizationContainer,
                type);
        plugins.put(widgetId, plugin);

        // Add configure button
        addConfigureButton(widget, widgetId, plugin);

        return widget;
    }

    private void addConfigureButton(DashboardWidget widget, String widgetId,
            DataVisualizationPlugin plugin) {
        Button configureButton = new Button(VaadinIcon.COG.create());
        configureButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_SMALL);
        configureButton.getStyle().setMargin("0");
        configureButton.addClickListener(e -> {
            openWidgetChat(widgetId, widget.getTitle(), plugin, configureButton);
        });
        widget.setHeaderContent(configureButton);
    }

    private void openWidgetChat(String widgetId, String widgetTitle,
            DataVisualizationPlugin plugin, Button configureButton) {
        // Create chat container
        VerticalLayout chatContainer = new VerticalLayout();
        chatContainer.setPadding(false);
        chatContainer.setSpacing(true);
        chatContainer.setWidth("600px");
        chatContainer.setHeight("500px");

        H2 chatTitle = new H2("Configure: " + widgetTitle);
        chatTitle.getStyle().set("margin", "0");

        MessageList messageList = new MessageList();
        messageList.setWidthFull();
        messageList.getStyle().set("flex-grow", "1");

        MessageInput messageInput = new MessageInput();
        messageInput.setWidthFull();

        // Create or update plugin with chat UI
        if (plugin == null) {
            // Create new plugin for empty widget
            Div visualizationContainer = new Div();
            visualizationContainer.setSizeFull();

            // Get the widget and update its content
            dashboard.getChildren()
                    .filter(c -> c instanceof DashboardWidget)
                    .map(c -> (DashboardWidget) c)
                    .filter(w -> widgetId.equals(w.getId().orElse(null)))
                    .findFirst().ifPresent(w -> {
                        w.setContent(visualizationContainer);
                    });

            plugin = createPlugin(visualizationContainer,
                    VisualizationType.CHART);
            plugins.put(widgetId, plugin);
        }

        // Create LLM provider for orchestrator
        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey).modelName("gpt-4o-mini").build();
        LLMProvider llmProvider = new LangChain4JLLMProvider(model);

        // Create chat orchestrator with plugin
        AiChatOrchestrator orchestrator = AiChatOrchestrator
                .create(llmProvider).withMessageList(messageList)
                .withInput(messageInput).withPlugin(plugin).build();

        Button closeButton = new Button("Close");
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout footer = new HorizontalLayout(closeButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.END);

        chatContainer.add(chatTitle, messageList, messageInput, footer);

        // Create popover attached to the configure button
        Popover popover = new Popover();
        popover.setTarget(configureButton);
        popover.setPosition(PopoverPosition.END_TOP);
        popover.addThemeVariants(PopoverVariant.ARROW);
        popover.setModal(true);
        popover.setCloseOnOutsideClick(false);
        popover.add(chatContainer);

        closeButton.addClickListener(e -> popover.close());

        popover.open();
    }

    private DataVisualizationPlugin createPlugin(
            Div visualizationContainer, VisualizationType initialType) {
        // Create plugin
        return DataVisualizationPlugin.create(new InMemoryDatabaseProvider())
                .withVisualizationContainer(visualizationContainer)
                .withInitialType(initialType).build();
    }
}
