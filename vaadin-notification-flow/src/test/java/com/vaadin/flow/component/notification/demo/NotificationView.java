/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.notification.demo;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;

/**
 * View for {@link Notification} demo.
 */
@Route("vaadin-notification")
public class NotificationView extends DemoView {

    private static final String BUTTON_CAPTION = "Open notification";

    @Override
    public void initView() {
        createDefaultNotificaiton();
        createNotificationWithPosition();
        createNotificationUsingStaticConvenienceMethod();
        createNotificationWithComponents();
        addStyledNotificationContent();
    }

    private void createDefaultNotificaiton() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);
        // begin-source-example
        // source-example-heading: Default Notification
        Notification notification = new Notification(
                "This notification has text content", 3000);
        button.addClickListener(event -> notification.open());
        // end-source-example
        button.setId("default-notification-button");
        notification.setId("default-notification");
        addCard("Default Notification", button);
    }

    private void createNotificationWithPosition() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);
        // begin-source-example
        // source-example-heading: Notification with position
        Notification notification = new Notification(
                "This notification is located on Top-Left", 3000,
                Position.TOP_START);
        button.addClickListener(event -> notification.open());
        // end-source-example
        button.setId("position-notification-button");
        notification.setId("position-notification");
        addCard("Notification with position", button);
    }

    private void createNotificationUsingStaticConvenienceMethod() {
        // begin-source-example
        // source-example-heading: Notification using static convenience method
        Notification notification = Notification.show(
                "This is a notification created with static convenience method");
        // end-source-example
        notification.setId("static-notification");
        addCard("Notification using static convenience method", notification);
    }

    private void createNotificationWithComponents() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);
        button.setId("component-notification-button");
        // begin-source-example
        // source-example-heading: Notification with components
        Label content = new Label(
                "Hello, I am a notification with components!");
        NativeButton buttonInside = new NativeButton("Bye");
        Notification notification = new Notification(content, buttonInside);
        notification.setDuration(3000);
        buttonInside.addClickListener(event -> notification.close());
        notification.setPosition(Position.MIDDLE);
        button.addClickListener(event -> notification.open());
        // end-source-example
        notification.setId("component-notification");
        content.setId("label-inside-notification");
        buttonInside.setId("button-inside-notification");
        addCard("Notification with components", button);
    }

    private void addStyledNotificationContent() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        // begin-source-example
        // source-example-heading: Notification with styled content
        Div content = new Div();
        content.addClassName("my-style");
        content.setText("This component is styled using global styles");

        Notification notification = new Notification(content);
        notification.setDuration(3000);

        // @formatter:off
        String styles = ".my-style { "
                + "  color: red;"
                + " }";
        // @formatter:on

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
        // end-source-example

        button.setId("styled-content-notification-button");
        addCard("Notification with styled content", button);
    }
}
