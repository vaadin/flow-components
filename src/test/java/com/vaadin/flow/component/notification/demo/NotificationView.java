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

import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.HorizontalAlign;
import com.vaadin.flow.component.notification.Notification.VerticalAlign;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Notification} demo.
 */
@Route("vaadin-notification")
@HtmlImport("bower_components/vaadin-valo-theme/vaadin-button.html")
public class NotificationView extends DemoView {

    private static final String BUTTON_CAPTION = "Open notification";

    @Override
    public void initView() {
        createDefaultNotificaiton();
        createNotificationWithPosition();
    }

    private void createDefaultNotificaiton() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);
        // begin-source-example
        // source-example-heading: Default Notification
        Notification notification = new Notification(
                "<h3>Hello World!</h3>"
                        + "This notification has HTML content",
                4000);
        // end-source-example
        button.setId("default-notification-button");
        button.addClickListener(event -> notification.open());
        notification.setId("default-notification");
        addCard("Default Notification", notification, button);
    }

    private void createNotificationWithPosition() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);
        // begin-source-example
        // source-example-heading: Notification with position
        Notification notification = new Notification(
                "<h3>Hello World!</h3>"
                        + "This notification has position setting",
                2000, VerticalAlign.TOP, HorizontalAlign.START);
        // end-source-example
        button.setId("position-notification-button");
        button.addClickListener(event -> notification.open());
        notification.setId("position-notification");
        addCard("Notification with position", notification, button);
    }

}
