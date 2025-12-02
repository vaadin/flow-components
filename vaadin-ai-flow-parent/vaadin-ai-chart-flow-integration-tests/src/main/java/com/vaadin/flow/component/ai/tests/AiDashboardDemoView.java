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
import com.vaadin.flow.component.ai.pro.chart.ChartAiController;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.router.Route;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Demo view for AI Dashboard with chart visualizations.
 * <p>
 * Demonstrates the ChartAiController with a dashboard layout where each widget
 * can contain an AI-powered chart visualization. Charts support multiple types:
 * - Line charts (for trends over time)
 * - Bar/Column charts (for comparisons)
 * - Pie charts (for proportions)
 * - Area charts (for cumulative values)
 * </p>
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/ai-dashboard-demo")
@CssImport("@vaadin/vaadin-lumo-styles/lumo.css")
public class AiDashboardDemoView extends VerticalLayout {

    private Dashboard dashboard;
    private Map<String, ChartAiController> controllers = new HashMap<>();
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
        // Add sample chart widgets
        DashboardWidget chartWidget1 = createWidget("Revenue by Month");
        dashboard.add(chartWidget1);

        DashboardWidget chartWidget2 = createWidget("Sales by Region");
        dashboard.add(chartWidget2);

        DashboardWidget chartWidget3 = createWidget("Product Performance");
        dashboard.add(chartWidget3);
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

    private DashboardWidget createWidget(String title) {
        var widgetId = "widget-" + System.currentTimeMillis();

        // Create chart component
        var chart = new Chart();
        chart.setSizeFull();

        // Create widget
        var widget = new DashboardWidget(title);
        widget.setId(widgetId);
        widget.setRowspan(2);
        widget.setContent(chart);

        // Create controller for this widget
        var controller = createController(chart);
        controllers.put(widgetId, controller);

        // Add configure button
        addConfigureButton(widget, widgetId, controller);

        return widget;
    }

    private void addConfigureButton(DashboardWidget widget, String widgetId,
            ChartAiController controller) {
        var configureButton = new Button(VaadinIcon.COG.create());
        configureButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_SMALL);
        configureButton.addClickListener(e -> {
            openWidgetChat(widgetId, widget.getTitle(), controller, configureButton);
        });
        widget.setHeaderContent(configureButton);
    }

    private void openWidgetChat(String widgetId, String widgetTitle,
            ChartAiController controller, Button configureButton) {
        // Create chat container
        var chatContainer = new VerticalLayout();
        chatContainer.setPadding(false);
        chatContainer.setWidth("600px");
        chatContainer.setHeight("500px");

        var messageList = new MessageList();
        messageList.setSizeFull();
        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        // Create or update controller with chat UI
        if (controller == null) {
            var chart = new Chart();
            chart.setSizeFull();

            // Get the widget and update its content
            dashboard.getChildren()
                    .filter(c -> c instanceof DashboardWidget)
                    .map(c -> (DashboardWidget) c)
                    .filter(w -> widgetId.equals(w.getId().orElse(null)))
                    .findFirst().ifPresent(w -> w.setContent(chart));

            controller = createController(chart);
            controllers.put(widgetId, controller);
        }

        // Create LLM provider
        var model = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey).modelName("gpt-4o-mini").build();
        var provider = new LangChain4JLLMProvider(model);

        // Create chat orchestrator with controller
        AiOrchestrator.builder(provider, ChartAiController.getSystemPrompt())
                .withMessageList(messageList)
                .withInput(messageInput)
                .withController(controller)
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

    private ChartAiController createController(Chart chart) {
        // Create controller with chart and database provider
        return new ChartAiController(chart, new InMemoryDatabaseProvider());
    }
}
