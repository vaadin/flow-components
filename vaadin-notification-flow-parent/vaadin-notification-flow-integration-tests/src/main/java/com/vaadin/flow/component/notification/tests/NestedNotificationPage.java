/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.notification.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

@Route("vaadin-notification/nested")
public class NestedNotificationPage extends Div {

    public NestedNotificationPage() {
        var parent = new Notification();

        var child = new Notification();
        child.setText("Child text");

        parent.add(child);
        parent.add(new NativeButton("Open child", e -> child.open()));

        var openParentButton = new NativeButton("Open parent",
                e -> parent.open());
        openParentButton.setId("open");
        add(openParentButton);
    }
}
