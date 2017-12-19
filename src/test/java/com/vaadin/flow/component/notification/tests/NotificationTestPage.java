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
package com.vaadin.flow.component.notification.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

/**
 * Page created for testing purposes. Not suitable for demos.
 * 
 * @author Vaadin Ltd.
 *
 */
@Route("notification-test")
public class NotificationTestPage extends Div {

    private static final String BUTTON_CAPTION = "Open notification";

    public NotificationTestPage() {
        createNotificationWithButtonControl();
        createTwoNotificitonAtSamePosition();
    }

    private void createNotificationWithButtonControl() {
        NativeButton button1 = new NativeButton(BUTTON_CAPTION);
        button1.setId("notification-open");
        NativeButton button2 = new NativeButton("Close Notification");
        button2.setId("notification-close");
        Notification notification = new Notification(
                "<h3>Hello World!</h3>" + "This notification has HTML content",
                0);
        notification.setId("notification-with-button-control");
        button1.addClickListener(event -> notification.open());
        button2.addClickListener(event -> notification.close());

        add(notification, button1, button2);
    }

    private void createTwoNotificitonAtSamePosition() {
        NativeButton button1 = new NativeButton(BUTTON_CAPTION + "1");
        button1.setId("notification-button-1");
        NativeButton button2 = new NativeButton(BUTTON_CAPTION + "2");
        button2.setId("notification-button-2");
        Notification notification1 = new Notification(
                "<h3>11111111111</h3>", 4000);
        notification1.setId("notication-1");
        Notification notification2 = new Notification(
                "<h3>22222222222</h3>", 4000);
        notification1.setId("notication-2");
        button1.addClickListener(event -> notification1.open());
        button2.addClickListener(event -> notification2.open());
        add(notification1, button1,
                notification2, button2);
    }
}
