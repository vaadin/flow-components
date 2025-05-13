/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.router.Route;

@CssImport("./notification-not-animated-styles.css")
@Route("vaadin-notification")
public class NotificationView extends Div {

    public NotificationView() {
        createDefaultNotification();
        createNotificationWithPosition();
        createNotificationUsingStaticConvenienceMethod();
        createNotificationWithComponents();
    }

    private void createDefaultNotification() {
        NativeButton button = new NativeButton("Default notification");
        Notification notification = new Notification(
                "This notification has text content", 3000);
        button.addClickListener(event -> notification.open());
        button.setId("default-notification-button");
        notification.setId("default-notification");
        add(button);
    }

    private void createNotificationWithPosition() {
        NativeButton button = new NativeButton("Notification with position");
        Notification notification = new Notification(
                new Span("This notification is located on Top-Left"));
        notification.setDuration(3000);
        notification.setPosition(Position.TOP_START);
        button.addClickListener(event -> notification.open());
        button.setId("position-notification-button");
        notification.setId("position-notification");
        add(button);
    }

    private void createNotificationUsingStaticConvenienceMethod() {
        NativeButton button = new NativeButton(
                "Notification using static method");
        button.addClickListener(event -> {
            Notification notification = Notification.show(
                    "This is a notification created with static convenience method",
                    3000, Position.BOTTOM_START);
            notification.setId("static-notification");
        });
        button.setId("static-notification-button");
        add(button);
    }

    private void createNotificationWithComponents() {
        NativeButton button = new NativeButton("Notification with component");
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
        add(button);
    }
}
