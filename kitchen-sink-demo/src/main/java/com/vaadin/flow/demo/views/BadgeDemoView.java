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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for Badge component using Lumo theme utility classes.
 */
@Route(value = "badge", layout = MainLayout.class)
@PageTitle("Badge | Vaadin Kitchen Sink")
public class BadgeDemoView extends VerticalLayout {

    public BadgeDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Badge Component"));
        add(new Paragraph("Badges are used to highlight status information or counts."));

        // Basic badges
        HorizontalLayout basic = new HorizontalLayout();
        basic.setSpacing(true);
        basic.add(
            createBadge("Default", "badge"),
            createBadge("Primary", "badge primary"),
            createBadge("Success", "badge success"),
            createBadge("Error", "badge error"),
            createBadge("Contrast", "badge contrast")
        );
        addSection("Basic Badges", basic);

        // Small badges
        HorizontalLayout small = new HorizontalLayout();
        small.setSpacing(true);
        small.add(
            createBadge("Small", "badge small"),
            createBadge("Small Primary", "badge small primary"),
            createBadge("Small Success", "badge small success")
        );
        addSection("Small Badges", small);

        // Pill badges
        HorizontalLayout pill = new HorizontalLayout();
        pill.setSpacing(true);
        pill.add(
            createBadge("Pill", "badge pill"),
            createBadge("Pill Primary", "badge pill primary"),
            createBadge("Pill Success", "badge pill success")
        );
        addSection("Pill Badges", pill);

        // With icons
        HorizontalLayout withIcons = new HorizontalLayout();
        withIcons.setSpacing(true);
        withIcons.add(
            createBadgeWithIcon(VaadinIcon.CHECK, "Approved", "badge success"),
            createBadgeWithIcon(VaadinIcon.CLOSE, "Rejected", "badge error"),
            createBadgeWithIcon(VaadinIcon.CLOCK, "Pending", "badge"),
            createBadgeWithIcon(VaadinIcon.INFO, "Info", "badge primary")
        );
        addSection("Badges with Icons", withIcons);

        // Count badges
        HorizontalLayout counts = new HorizontalLayout();
        counts.setSpacing(true);
        counts.add(
            createBadge("5", "badge pill small"),
            createBadge("12", "badge pill small primary"),
            createBadge("99+", "badge pill small error")
        );
        addSection("Count Badges", counts);

        // Status badges example
        VerticalLayout statusExamples = new VerticalLayout();
        statusExamples.setPadding(false);
        statusExamples.setSpacing(true);

        HorizontalLayout status1 = new HorizontalLayout();
        status1.setAlignItems(Alignment.CENTER);
        status1.add(new Span("Order #1234"), createBadge("Shipped", "badge success"));

        HorizontalLayout status2 = new HorizontalLayout();
        status2.setAlignItems(Alignment.CENTER);
        status2.add(new Span("Order #1235"), createBadge("Processing", "badge"));

        HorizontalLayout status3 = new HorizontalLayout();
        status3.setAlignItems(Alignment.CENTER);
        status3.add(new Span("Order #1236"), createBadge("Cancelled", "badge error"));

        statusExamples.add(status1, status2, status3);
        addSection("Status Badge Examples", statusExamples);
    }

    private Span createBadge(String text, String themeNames) {
        Span badge = new Span(text);
        badge.getElement().getThemeList().add(themeNames);
        return badge;
    }

    private Span createBadgeWithIcon(VaadinIcon icon, String text, String themeNames) {
        Icon iconComponent = icon.create();
        iconComponent.getStyle().set("padding", "var(--lumo-space-xs)");
        Span badge = new Span(iconComponent, new Span(text));
        badge.getElement().getThemeList().add(themeNames);
        return badge;
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
