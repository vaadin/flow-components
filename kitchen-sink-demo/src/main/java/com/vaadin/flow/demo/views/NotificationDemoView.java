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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for Notification component.
 */
@Route(value = "notification", layout = MainLayout.class)
@PageTitle("Notification | Vaadin Kitchen Sink")
public class NotificationDemoView extends VerticalLayout {

    public NotificationDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Notification Component"));
        add(new Paragraph("Notifications display brief messages to the user."));

        // Basic notification
        Button basicBtn = new Button("Show Basic Notification", e ->
            Notification.show("This is a basic notification"));
        addSection("Basic Notification", basicBtn);

        // Notification with duration
        Button durationBtn = new Button("5 Second Notification", e ->
            Notification.show("This notification lasts 5 seconds", 5000, Position.MIDDLE));
        addSection("Custom Duration", durationBtn);

        // Success notification
        Button successBtn = new Button("Show Success", e -> {
            Notification notification = Notification.show("Operation completed successfully!");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        addSection("Success Notification", successBtn);

        // Error notification
        Button errorBtn = new Button("Show Error", e -> {
            Notification notification = Notification.show("An error occurred!");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        addSection("Error Notification", errorBtn);

        // Primary notification
        Button primaryBtn = new Button("Show Primary", e -> {
            Notification notification = Notification.show("Important information");
            notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        });
        addSection("Primary Notification", primaryBtn);

        // Contrast notification
        Button contrastBtn = new Button("Show Contrast", e -> {
            Notification notification = Notification.show("Contrast notification");
            notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        });
        addSection("Contrast Notification", contrastBtn);

        // Positions
        HorizontalLayout positions = new HorizontalLayout();

        Button topStart = new Button("Top Start", e ->
            Notification.show("Top Start", 2000, Position.TOP_START));
        Button topCenter = new Button("Top Center", e ->
            Notification.show("Top Center", 2000, Position.TOP_CENTER));
        Button topEnd = new Button("Top End", e ->
            Notification.show("Top End", 2000, Position.TOP_END));

        positions.add(topStart, topCenter, topEnd);
        addSection("Top Positions", positions);

        HorizontalLayout middlePositions = new HorizontalLayout();
        Button middle = new Button("Middle", e ->
            Notification.show("Middle", 2000, Position.MIDDLE));
        middlePositions.add(middle);
        addSection("Middle Position", middlePositions);

        HorizontalLayout bottomPositions = new HorizontalLayout();
        Button bottomStart = new Button("Bottom Start", e ->
            Notification.show("Bottom Start", 2000, Position.BOTTOM_START));
        Button bottomCenter = new Button("Bottom Center", e ->
            Notification.show("Bottom Center", 2000, Position.BOTTOM_CENTER));
        Button bottomEnd = new Button("Bottom End", e ->
            Notification.show("Bottom End", 2000, Position.BOTTOM_END));
        bottomPositions.add(bottomStart, bottomCenter, bottomEnd);
        addSection("Bottom Positions", bottomPositions);

        // Rich content notification
        Button richBtn = new Button("Rich Content", e -> {
            Notification notification = new Notification();

            Icon icon = VaadinIcon.CHECK_CIRCLE.create();
            icon.setColor("var(--lumo-success-text-color)");

            Span message = new Span("File uploaded successfully");

            Button closeBtn = new Button(VaadinIcon.CLOSE_SMALL.create());
            closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            closeBtn.addClickListener(event -> notification.close());

            HorizontalLayout layout = new HorizontalLayout(icon, message, closeBtn);
            layout.setAlignItems(Alignment.CENTER);

            notification.add(layout);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(5000);
            notification.open();
        });
        addSection("Rich Content Notification", richBtn);

        // Closable notification
        Button closableBtn = new Button("Closable Notification", e -> {
            Notification notification = new Notification();
            notification.setDuration(0); // Won't close automatically

            Span message = new Span("Click the X to close this notification");

            Button closeBtn = new Button(VaadinIcon.CLOSE.create());
            closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            closeBtn.addClickListener(event -> notification.close());

            HorizontalLayout layout = new HorizontalLayout(message, closeBtn);
            layout.setAlignItems(Alignment.CENTER);

            notification.add(layout);
            notification.open();
        });
        addSection("Closable (Manual Close)", closableBtn);

        // Notification with action
        Button actionBtn = new Button("With Action", e -> {
            Notification notification = new Notification();

            Span message = new Span("Item deleted");
            Button undoBtn = new Button("Undo", event -> {
                Notification.show("Undo successful");
                notification.close();
            });
            undoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

            HorizontalLayout layout = new HorizontalLayout(message, undoBtn);
            layout.setAlignItems(Alignment.CENTER);

            notification.add(layout);
            notification.setDuration(5000);
            notification.open();
        });
        addSection("With Action Button", actionBtn);
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
