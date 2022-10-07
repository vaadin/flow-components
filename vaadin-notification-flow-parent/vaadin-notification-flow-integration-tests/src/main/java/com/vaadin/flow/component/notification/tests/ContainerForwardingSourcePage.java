package com.vaadin.flow.component.notification.tests;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("vaadin-notification/container-remains-in-dom-after-detach-view")
public class ContainerForwardingSourcePage extends Notification
        implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.forwardTo(ContainerForwardingTargetPage.class);
    }

    public ContainerForwardingSourcePage() {
        setOpened(true);
    }
}
