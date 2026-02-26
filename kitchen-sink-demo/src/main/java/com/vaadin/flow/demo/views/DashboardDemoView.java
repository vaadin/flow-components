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
package com.vaadin.flow.demo.views;

import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for Dashboard component.
 */
@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard | Vaadin Kitchen Sink")
public class DashboardDemoView extends VerticalLayout {

    public DashboardDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Dashboard Component"));
        add(new Paragraph("Dashboard provides a flexible grid layout with resizable and draggable widgets."));

        // Basic dashboard
        Dashboard basicDashboard = new Dashboard();

        DashboardWidget widget1 = new DashboardWidget();
        widget1.setTitle("Widget 1");
        widget1.setContent(createWidgetContent("Content for widget 1"));

        DashboardWidget widget2 = new DashboardWidget();
        widget2.setTitle("Widget 2");
        widget2.setContent(createWidgetContent("Content for widget 2"));

        DashboardWidget widget3 = new DashboardWidget();
        widget3.setTitle("Widget 3");
        widget3.setContent(createWidgetContent("Content for widget 3"));

        DashboardWidget widget4 = new DashboardWidget();
        widget4.setTitle("Widget 4");
        widget4.setContent(createWidgetContent("Content for widget 4"));

        basicDashboard.add(widget1, widget2, widget3, widget4);
        basicDashboard.setWidthFull();
        basicDashboard.setMinHeight("400px");
        addSection("Basic Dashboard", basicDashboard);

        // Dashboard with different widget sizes
        Dashboard sizedDashboard = new Dashboard();

        DashboardWidget largeWidget = new DashboardWidget();
        largeWidget.setTitle("Large Widget");
        largeWidget.setColspan(2);
        largeWidget.setRowspan(2);
        largeWidget.setContent(createWidgetContent("This is a larger widget spanning 2 columns and 2 rows"));

        DashboardWidget smallWidget1 = new DashboardWidget();
        smallWidget1.setTitle("Small 1");
        smallWidget1.setContent(createWidgetContent("Small widget"));

        DashboardWidget smallWidget2 = new DashboardWidget();
        smallWidget2.setTitle("Small 2");
        smallWidget2.setContent(createWidgetContent("Small widget"));

        DashboardWidget wideWidget = new DashboardWidget();
        wideWidget.setTitle("Wide Widget");
        wideWidget.setColspan(2);
        wideWidget.setContent(createWidgetContent("Wide widget spanning 2 columns"));

        sizedDashboard.add(largeWidget, smallWidget1, smallWidget2, wideWidget);
        sizedDashboard.setWidthFull();
        sizedDashboard.setMinHeight("500px");
        addSection("Different Widget Sizes", sizedDashboard);

        // Editable dashboard
        Dashboard editableDashboard = new Dashboard();
        editableDashboard.setEditable(true);

        DashboardWidget editWidget1 = new DashboardWidget();
        editWidget1.setTitle("Editable Widget 1");
        editWidget1.setContent(createWidgetContent("Try moving or resizing this widget"));

        DashboardWidget editWidget2 = new DashboardWidget();
        editWidget2.setTitle("Editable Widget 2");
        editWidget2.setContent(createWidgetContent("Dashboard is in edit mode"));

        DashboardWidget editWidget3 = new DashboardWidget();
        editWidget3.setTitle("Editable Widget 3");
        editWidget3.setContent(createWidgetContent("Widgets can be rearranged"));

        editableDashboard.add(editWidget1, editWidget2, editWidget3);
        editableDashboard.setWidthFull();
        editableDashboard.setMinHeight("350px");
        addSection("Editable Dashboard (Edit Mode)", editableDashboard);

        // Stats dashboard example
        Dashboard statsDashboard = new Dashboard();

        DashboardWidget usersWidget = createStatWidget("Total Users", "12,345", "+15%");
        DashboardWidget revenueWidget = createStatWidget("Revenue", "$45,678", "+8%");
        DashboardWidget ordersWidget = createStatWidget("Orders", "1,234", "+23%");
        DashboardWidget conversionWidget = createStatWidget("Conversion", "3.2%", "+5%");

        DashboardWidget chartWidget = new DashboardWidget();
        chartWidget.setTitle("Sales Overview");
        chartWidget.setColspan(2);
        chartWidget.setRowspan(2);
        chartWidget.setContent(createWidgetContent("Chart visualization would go here"));

        DashboardWidget tableWidget = new DashboardWidget();
        tableWidget.setTitle("Recent Orders");
        tableWidget.setColspan(2);
        tableWidget.setContent(createWidgetContent("Order table would go here"));

        statsDashboard.add(usersWidget, revenueWidget, ordersWidget, conversionWidget,
                chartWidget, tableWidget);
        statsDashboard.setWidthFull();
        statsDashboard.setMinHeight("500px");
        addSection("Stats Dashboard Example", statsDashboard);
    }

    private Div createWidgetContent(String text) {
        Div content = new Div();
        content.setText(text);
        content.addClassNames(LumoUtility.Padding.MEDIUM,
                LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER, LumoUtility.TextAlignment.CENTER);
        content.setMinHeight("80px");
        return content;
    }

    private DashboardWidget createStatWidget(String title, String value, String change) {
        DashboardWidget widget = new DashboardWidget();
        widget.setTitle(title);

        Div content = new Div();
        content.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.TextAlignment.CENTER);

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD,
                LumoUtility.Display.BLOCK);

        Span changeSpan = new Span(change);
        changeSpan.addClassNames(LumoUtility.FontSize.SMALL,
                change.startsWith("+") ? LumoUtility.TextColor.SUCCESS : LumoUtility.TextColor.ERROR);

        content.add(valueSpan, changeSpan);
        widget.setContent(content);
        return widget;
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }
}
