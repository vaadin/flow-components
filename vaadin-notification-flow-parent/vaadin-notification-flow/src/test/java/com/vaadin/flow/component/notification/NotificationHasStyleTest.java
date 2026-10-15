/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.notification;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.shared.internal.OverlayClassListProxy;
import com.vaadin.flow.server.VaadinSession;

public class NotificationHasStyleTest {

    private UI ui = new UI();
    private Notification notification;

    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        notification = new Notification();
        ui.add(notification);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void addClassName_notificationHasOverlayClass() {
        notification.addClassName("foo");
        Assert.assertEquals(
                notification.getElement().getProperty("overlayClass"), "foo");

        notification.addClassName("bar");
        Assert.assertEquals(
                notification.getElement().getProperty("overlayClass"),
                "foo bar");
    }

    @Test
    public void removeClassName_notificationHasOverlayClass() {
        notification.addClassName("foo");

        notification.removeClassName("foo");
        Assert.assertEquals(
                notification.getElement().getProperty("overlayClass"), null);
    }

    @Test
    public void setClassNameString_notificationHasOverlayClass() {
        notification.setClassName("foo");
        Assert.assertEquals(
                notification.getElement().getProperty("overlayClass"), "foo");

        notification.setClassName("bar");
        Assert.assertEquals(
                notification.getElement().getProperty("overlayClass"), "bar");
    }

    @Test
    public void setClassNameBoolean_notificationHasOverlayClass() {
        notification.setClassName("foo", true);

        notification.setClassName("foo", false);

        Assert.assertEquals(
                notification.getElement().getProperty("overlayClass"), null);
    }

    @Test
    public void setClassNameMultiple_notificationHasOverlayClass() {
        notification.setClassName("foo bar");
        notification.getClassNames().set("foo", false);

        Assert.assertEquals(
                notification.getElement().getProperty("overlayClass"), "bar");
    }

    @Test
    public void addClassNames_notificationHasOverlayClass() {
        notification.addClassNames("foo", "bar");
        Assert.assertEquals(
                notification.getElement().getProperty("overlayClass"),
                "foo bar");

        notification.addClassNames("baz", "qux");
        Assert.assertEquals(
                notification.getElement().getProperty("overlayClass"),
                "foo bar baz qux");
    }

    @Test
    public void removeClassNames_notificationHasOverlayClass() {
        notification.addClassNames("foo", "bar", "baz", "qux");

        notification.removeClassNames("foo", "bar");
        Assert.assertEquals(
                notification.getElement().getProperty("overlayClass"),
                "baz qux");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getStyle_unsupported() {
        notification.getStyle();
    }

    @Test
    public void getClassNames_usesProxy() {
        Assert.assertTrue(
                notification.getClassNames() instanceof OverlayClassListProxy);
    }
}
