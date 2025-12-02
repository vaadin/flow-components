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
import com.vaadin.flow.component.ai.pro.chart.DataVisualizationPlugin;
import com.vaadin.flow.component.ai.pro.chart.VisualizationType;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
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
@CssImport("@vaadin/vaadin-lumo-styles/lumo.css")
public class AiDashboardDemoView extends VerticalLayout {

    private Dashboard dashboard;
    private Map<String, DataVisualizationPlugin> plugins = new HashMap<>();
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
        addConfigureButton(widget, widgetId, null);

        return widget;
    }

    private DashboardWidget createWidget(String title,
            VisualizationType type) {
        var widgetId = "widget-" + System.currentTimeMillis();

        // Create visualization container
        var visualizationContainer = new Div();
        visualizationContainer.setSizeFull();

        // Create widget
        var widget = new DashboardWidget(title);
        widget.setId(widgetId);
        widget.setRowspan(2);
        widget.setContent(visualizationContainer);

        // Create plugin for this widget
        var plugin = createPlugin(visualizationContainer, type);
        plugins.put(widgetId, plugin);

        // Add configure button
        addConfigureButton(widget, widgetId, plugin);

        return widget;
    }

    private void addConfigureButton(DashboardWidget widget, String widgetId,
            DataVisualizationPlugin plugin) {
        var configureButton = new Button(VaadinIcon.COG.create());
        configureButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_SMALL);
        configureButton.addClickListener(e -> {
            openWidgetChat(widgetId, widget.getTitle(), plugin, configureButton);
        });
        widget.setHeaderContent(configureButton);
    }

    private void openWidgetChat(String widgetId, String widgetTitle,
            DataVisualizationPlugin plugin, Button configureButton) {
        // Create chat container
        var chatContainer = new VerticalLayout();
        chatContainer.setPadding(false);
        chatContainer.setWidth("600px");
        chatContainer.setHeight("500px");

        var messageList = new MessageList();
        messageList.setSizeFull();
        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        // Create or update plugin with chat UI
        if (plugin == null) {
            var visualizationContainer = new Div();
            visualizationContainer.setSizeFull();

            // Get the widget and update its content
            dashboard.getChildren()
                    .filter(c -> c instanceof DashboardWidget)
                    .map(c -> (DashboardWidget) c)
                    .filter(w -> widgetId.equals(w.getId().orElse(null)))
                    .findFirst().ifPresent(w -> w.setContent(visualizationContainer));

            plugin = createPlugin(visualizationContainer, VisualizationType.CHART);
            plugins.put(widgetId, plugin);
        }

        // Create LLM provider
        var model = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey).modelName("gpt-4o-mini").build();
        var provider = new LangChain4JLLMProvider(model);

        // Create chat orchestrator with plugin
        AiOrchestrator.create(provider, DataVisualizationPlugin.getSystemPrompt())
                .withMessageList(messageList)
                .withInput(messageInput)
                .withPlugin(plugin)
                .build();

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
        popover.addThemeVariants(PopoverVariant.ARROW);
        popover.setModal(true);
        popover.setCloseOnOutsideClick(true);
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
