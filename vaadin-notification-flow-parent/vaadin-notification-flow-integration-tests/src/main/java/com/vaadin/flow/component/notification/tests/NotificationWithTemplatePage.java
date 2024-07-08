/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.notification.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

@Route("vaadin-notification/notification-template-test")
public class NotificationWithTemplatePage extends Div {

    private Notification notification;

    public NotificationWithTemplatePage() {
        notification = new Notification();
        TestTemplate testTemplate = new TestTemplate();
        testTemplate.setId("template");
        notification.add(testTemplate);

        NativeButton open = new NativeButton("Open notification");
        open.setId("open");
        open.addClickListener(event -> notification.open());
        add(open);
    }

}
