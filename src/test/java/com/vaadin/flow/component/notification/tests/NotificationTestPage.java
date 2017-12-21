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
        createNotificationAddComponent();
        createNotificationRemoveComponent();
        createNotificationRemoveAllComponent();
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

    private void createNotificationAddComponent() {
        NativeButton button = new NativeButton(BUTTON_CAPTION + "add");
        NativeButton buttonOff = new NativeButton("Close Notification add");
        NativeButton button1 = new NativeButton();
        NativeButton button2 = new NativeButton();
        NativeButton button3 = new NativeButton();
        button.setId("open-notification-button-add");
        buttonOff.setId("close-notification-button-add");

        Notification notification = new Notification();
        notification.setId("notification-add-components");
        notification.add(button1, button2, button3);

        button.addClickListener(event -> notification.open());
        buttonOff.addClickListener(event -> notification.close());
        add(notification, button, buttonOff);
    }

    private void createNotificationRemoveComponent() {
        NativeButton button = new NativeButton(BUTTON_CAPTION + "remove");
        NativeButton buttonOff = new NativeButton("Close Notification remove");
        NativeButton button1 = new NativeButton();
        NativeButton button2 = new NativeButton();
        NativeButton button3 = new NativeButton();
        button.setId("open-notification-button-remove");
        buttonOff.setId("close-notification-button-remove");

        Notification notification = new Notification(button1, button2, button3);
        notification.setId("notification-remove-components");
        notification.remove(button1);

        button.addClickListener(event -> notification.open());
        buttonOff.addClickListener(event -> notification.close());
        add(notification, button, buttonOff);
    }

    private void createNotificationRemoveAllComponent() {
        NativeButton button = new NativeButton(BUTTON_CAPTION + "remove all");
        NativeButton buttonOff = new NativeButton(
                "Close Notification remove all");
        NativeButton button1 = new NativeButton();
        NativeButton button2 = new NativeButton();
        NativeButton button3 = new NativeButton();
        button.setId("open-notification-button-remove-all");
        buttonOff.setId("close-notification-button-remove-all");

        Notification notification = new Notification(button1, button2, button3);
        notification.setId("notification-remove-all-components");
        notification.removeAll();

        button.addClickListener(event -> notification.open());
        buttonOff.addClickListener(event -> notification.close());
        add(notification, button, buttonOff);
    }
}
