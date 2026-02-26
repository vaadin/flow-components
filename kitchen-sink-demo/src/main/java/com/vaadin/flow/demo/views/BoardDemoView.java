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

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for Board component.
 */
@Route(value = "board", layout = MainLayout.class)
@PageTitle("Board | Vaadin Kitchen Sink")
public class BoardDemoView extends VerticalLayout {

    public BoardDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Board Component"));
        add(new Paragraph("Board provides a responsive grid layout for dashboard-style interfaces."));

        // Basic board with equal columns
        Board basicBoard = new Board();
        basicBoard.addRow(
            createCell("Cell 1"),
            createCell("Cell 2"),
            createCell("Cell 3"),
            createCell("Cell 4")
        );
        addSection("Basic Board (4 Equal Columns)", basicBoard);

        // Board with different column spans
        Board spanBoard = new Board();
        Row spanRow = new Row();
        Div wideCell = createCell("Wide Cell (2 columns)");
        spanRow.add(wideCell, 2);
        spanRow.add(createCell("Normal"));
        spanRow.add(createCell("Normal"));
        spanBoard.add(spanRow);
        addSection("With Column Spans", spanBoard);

        // Dashboard stats example
        Board statsBoard = new Board();
        statsBoard.addRow(
            createStatCard("Total Users", "1,234", "+12%"),
            createStatCard("Revenue", "$45,678", "+8%"),
            createStatCard("Orders", "567", "+23%"),
            createStatCard("Conversion", "3.2%", "+5%")
        );
        addSection("Dashboard Stats", statsBoard);

        // Mixed layout
        Board mixedBoard = new Board();

        Row topRow = new Row();
        Div mainChart = createCell("Main Chart Area");
        mainChart.setMinHeight("200px");
        topRow.add(mainChart, 3);
        topRow.add(createCell("Side Panel"));
        mixedBoard.add(topRow);

        Row bottomRow = new Row();
        bottomRow.add(createCell("Table 1"));
        bottomRow.add(createCell("Table 2"));
        mixedBoard.add(bottomRow);

        addSection("Mixed Layout", mixedBoard);

        // Full dashboard example
        Board dashboard = new Board();

        // Stats row
        Row statsRow = new Row();
        statsRow.add(createStatCard("Users", "12,345", "+15%"));
        statsRow.add(createStatCard("Sessions", "45,678", "+8%"));
        statsRow.add(createStatCard("Bounce Rate", "42%", "-3%"));
        statsRow.add(createStatCard("Duration", "3m 24s", "+12%"));
        dashboard.add(statsRow);

        // Charts row
        Row chartsRow = new Row();
        Div lineChart = createCell("Line Chart - Traffic Over Time");
        lineChart.setMinHeight("250px");
        chartsRow.add(lineChart, 2);
        Div pieChart = createCell("Pie Chart - Traffic Sources");
        pieChart.setMinHeight("250px");
        chartsRow.add(pieChart, 2);
        dashboard.add(chartsRow);

        // Table row
        Row tableRow = new Row();
        Div table = createCell("Recent Activity Table");
        table.setMinHeight("200px");
        tableRow.add(table, 3);
        Div notifications = createCell("Notifications");
        notifications.setMinHeight("200px");
        tableRow.add(notifications);
        dashboard.add(tableRow);

        addSection("Complete Dashboard Layout", dashboard);
    }

    private Div createCell(String text) {
        Div cell = new Div();
        cell.setText(text);
        cell.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM,
                LumoUtility.BorderRadius.MEDIUM, LumoUtility.Display.FLEX,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
        cell.setMinHeight("100px");
        return cell;
    }

    private Div createStatCard(String title, String value, String change) {
        Div card = new Div();
        card.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM,
                LumoUtility.BorderRadius.MEDIUM);

        Span titleSpan = new Span(title);
        titleSpan.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD,
                LumoUtility.Display.BLOCK, LumoUtility.Margin.Vertical.XSMALL);

        Span changeSpan = new Span(change);
        boolean positive = change.startsWith("+") || change.startsWith("-") && change.contains("-") == false;
        if (change.startsWith("+")) {
            changeSpan.addClassNames(LumoUtility.TextColor.SUCCESS);
        } else if (change.startsWith("-")) {
            changeSpan.addClassNames(LumoUtility.TextColor.ERROR);
        }
        changeSpan.addClassNames(LumoUtility.FontSize.SMALL);

        card.add(titleSpan, valueSpan, changeSpan);
        return card;
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
