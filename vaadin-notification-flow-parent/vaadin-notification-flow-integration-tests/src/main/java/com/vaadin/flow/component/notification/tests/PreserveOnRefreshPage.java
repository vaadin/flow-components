package com.vaadin.flow.component.notification.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@PreserveOnRefresh
@Route("vaadin-notification/preserve-on-refresh")
public class PreserveOnRefreshPage extends Div {

    public PreserveOnRefreshPage() {
        // Notification should be auto-added for this test
        Notification notification = new Notification("Notification", 0,
                Notification.Position.MIDDLE);

        NativeButton showNotification = new NativeButton("Show", e -> {
            notification.open();
        });
        showNotification.setId("show-notification");
        add(showNotification);

        NativeButton closeNotification = new NativeButton("Close", e -> {
            notification.close();
        });
        closeNotification.setId("close-notification");
        add(closeNotification);

        NativeButton addComponent = new NativeButton(
                "Add component to notification", e -> {
                    Span span = new Span("Component content");
                    span.setId("component-content");
                    notification.add(span);
                });
        addComponent.setId("add-component");
        add(addComponent);
    }
}