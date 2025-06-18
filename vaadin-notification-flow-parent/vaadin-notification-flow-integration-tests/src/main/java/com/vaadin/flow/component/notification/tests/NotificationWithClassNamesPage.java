/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.notification.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

@Route("vaadin-notification/notification-class-names-test")
public class NotificationWithClassNamesPage extends Div {

    public NotificationWithClassNamesPage() {
        final Notification notification = new Notification("Notification");
        notification.addClassName("custom");

        Button addClass = new Button("Add a class",
                event -> notification.addClassName("added"));
        addClass.setId("add-class-btn");

        Button clearAllClass = new Button("Clear all classes",
                event -> notification.getClassNames().clear());
        clearAllClass.setId("clear-classes-btn");

        Button close = new Button("Close notification",
                event -> notification.close());
        close.setId("close-notification-btn");

        Button open = new Button("Open notification",
                event -> notification.open());
        open.setId("open-notification-btn");
        add(addClass, clearAllClass, open, close);

        createOtherNotification();
    }

    private void createOtherNotification() {
        Button button = new Button("Open other notification");
        Notification notification = new Notification(
                "This notification has text content", 3000);
        notification.addClassName("other");
        button.addClickListener(event -> notification.open());
        button.setId("open-other-notification-btn");
        notification.setId("other-notification");
        add(button);
    }
}
