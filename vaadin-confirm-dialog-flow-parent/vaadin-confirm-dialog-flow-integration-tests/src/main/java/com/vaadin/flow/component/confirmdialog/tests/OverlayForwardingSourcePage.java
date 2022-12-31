package com.vaadin.flow.component.confirmdialog.tests;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("vaadin-confirm-dialog/overlay-remains-in-dom-after-detach-view")
public class OverlayForwardingSourcePage extends ConfirmDialog
        implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.forwardTo(OverlayForwardingTargetPage.class);
    }

    public OverlayForwardingSourcePage() {
        setOpened(true);
    }
}
