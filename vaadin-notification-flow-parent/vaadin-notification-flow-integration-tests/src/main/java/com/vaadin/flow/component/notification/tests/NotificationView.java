/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.notification.tests;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;

/**
 * View for {@link Notification} demo.
 */
@Route("vaadin-notification")
public class NotificationView extends Div {

    private static final String BUTTON_CAPTION = "Open notification";

    public NotificationView() {
        createDefaultNotificaiton();
        createNotificationWithPosition();
        createNotificationUsingStaticConvenienceMethod();
        createNotificationWithComponents();
        addStyledNotificationContent();
        createThemeVariants();
    }

    private void createDefaultNotificaiton() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);
        Notification notification = new Notification(
                "This notification has text content", 3000);
        button.addClickListener(event -> notification.open());
        button.setId("default-notification-button");
        notification.setId("default-notification");
        addCard("Default Notification", button);
    }

    private void createNotificationWithPosition() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);
        Notification notification = new Notification(
                "This notification is located on Top-Left", 3000,
                Position.TOP_START);
        button.addClickListener(event -> notification.open());
        button.setId("position-notification-button");
        notification.setId("position-notification");
        addCard("Notification with position", button);
    }

    private void createNotificationUsingStaticConvenienceMethod() {
        Notification notification = Notification.show(
                "This is a notification created with static convenience method");
        notification.setId("static-notification");
        addCard("Notification using static convenience method");
    }

    private void createNotificationWithComponents() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);
        button.setId("component-notification-button");
        Span content = new Span("Hello, I am a notification with components!");
        NativeButton buttonInside = new NativeButton("Bye");
        Notification notification = new Notification(content, buttonInside);
        notification.setDuration(3000);
        buttonInside.addClickListener(event -> notification.close());
        notification.setPosition(Position.MIDDLE);
        button.addClickListener(event -> notification.open());
        notification.setId("component-notification");
        content.setId("label-inside-notification");
        buttonInside.setId("button-inside-notification");
        addCard("Notification with components", button);
    }

    private void addStyledNotificationContent() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        Div content = new Div();
        content.addClassName("my-style");
        content.setText("This component is styled using global styles");

        Notification notification = new Notification(content);
        notification.setDuration(3000);

        String styles = ".my-style { color: red; }";

        /*
         * The code below register the style file dynamically. Normally you
         * use @StyleSheet annotation for the component class. This way is
         * chosen just to show the style file source code.
         */
        StreamRegistration resource = UI.getCurrent().getSession()
                .getResourceRegistry()
                .registerResource(new StreamResource("styles.css", () -> {
                    byte[] bytes = styles.getBytes(StandardCharsets.UTF_8);
                    return new ByteArrayInputStream(bytes);
                }));
        UI.getCurrent().getPage().addStyleSheet(
                "base://" + resource.getResourceUri().toString());

        button.addClickListener(event -> notification.open());

        button.setId("styled-content-notification-button");
        addCard("Notification with styled content", button);
    }

    private void createThemeVariants() {
        createDefault();
        createPrimary();
        createContrast();
        createSuccess();
        createError();
    }

    private void createDefault() {
        Notification notification = new Notification();

        Span label = new Span("Please update your password");

        Button notNowButton = new Button("Not now", e -> notification.close());

        Button openSettingsButton = new Button("Open settings",
                e -> notification.close());
        openSettingsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        notification.add(label, notNowButton, openSettingsButton);

        Button openButton = new Button("Default notification",
                e -> notification.open());

        label.getStyle().set("margin-right", "0.5rem");
        notNowButton.getStyle().set("margin-right", "0.5rem");
        addCard("Theme Variants", "Default", openButton);
    }

    private void createPrimary() {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);

        Span label = new Span("Get notified with our latest updates");

        Button skipButton = new Button("Skip", e -> notification.close());

        Button subscribeButton = new Button("Subscribe",
                e -> notification.close());
        subscribeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        notification.add(label, skipButton, subscribeButton);

        Button openButton = new Button("Primary notification",
                e -> notification.open());
        openButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        label.getStyle().set("margin-right", "0.5rem");
        skipButton.getStyle().set("margin-right", "0.5rem");
        addCard("Theme Variants", "Primary", openButton);
    }

    private void createContrast() {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);

        Span label = new Span("Message deleted");

        Button dismissButton = new Button("Dismiss", e -> notification.close());

        Button undoButton = new Button("Undo", e -> notification.close());
        undoButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        notification.add(label, dismissButton, undoButton);

        Button openButton = new Button("Contrast notification",
                e -> notification.open());
        openButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST,
                ButtonVariant.LUMO_PRIMARY);

        label.getStyle().set("margin-right", "0.5rem");
        dismissButton.getStyle().set("margin-right", "0.5rem");
        addCard("Theme Variants", "Contrast", openButton);
    }

    private void createSuccess() {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        Span label = new Span("New version deployed sucessfully");

        Button viewLogButton = new Button("View log",
                e -> notification.close());

        Button openSiteButton = new Button("Open site",
                e -> notification.close());
        openSiteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        notification.add(label, viewLogButton, openSiteButton);

        Button openButton = new Button("Success notification",
                e -> notification.open());
        openButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS,
                ButtonVariant.LUMO_PRIMARY);

        label.getStyle().set("margin-right", "0.5rem");
        viewLogButton.getStyle().set("margin-right", "0.5rem");
        addCard("Theme Variants", "Success", openButton);
    }

    private void createError() {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Span label = new Span("System error occured");

        Button thisIsFineButton = new Button("This is fine",
                e -> notification.close());

        Button investigateButton = new Button("Investigate",
                e -> notification.close());
        investigateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        notification.add(label, thisIsFineButton, investigateButton);

        Button openButton = new Button("Error notification",
                e -> notification.open());
        openButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        label.getStyle().set("margin-right", "0.5rem");
        thisIsFineButton.getStyle().set("margin-right", "0.5rem");
        addCard("Theme Variants", "Error", openButton);
    }

    private void addCard(String title, Component... components) {
        addCard(title, null, components);
    }

    private void addCard(String title, String description,
            Component... components) {
        if (description != null) {
            title = title + ": " + description;
        }
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }
}
