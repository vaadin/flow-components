
package com.vaadin.flow.component.notification.tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class NotificationTest {

    private Notification notification;

    @Before
    public void setUp() {
        UI.setCurrent(new UI());
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void stringAndDurationCtor() {
        notification = new Notification("foo", 4000);
        Assert.assertEquals(4000, notification.getDuration(), 0);
        Assert.assertEquals("bottom-start",
                notification.getPosition().getClientName());
    }

    @Test
    public void stringDurAndPositionCtor() {
        notification = new Notification("fooo", 10000, Position.TOP_END);
        Assert.assertEquals(10000, notification.getDuration(), 0);
        Assert.assertEquals("top-end",
                notification.getPosition().getClientName());
    }

    @Test
    public void componentCtor() {
        notification = new Notification(new Label(), new NativeButton());

        notification.setPosition(Position.BOTTOM_END);
        Assert.assertEquals("bottom-end",
                notification.getPosition().getClientName());
    }

    @Test
    public void staticCtor() {
        notification = Notification.show("fooooo", 4000,
                Position.BOTTOM_CENTER);
        Assert.assertEquals("bottom-center",
                notification.getPosition().getClientName());
    }

    @Test
    public void setPositon() {
        notification = new Notification();

        notification.setPosition(Position.BOTTOM_STRETCH);
        Assert.assertEquals("bottom-stretch",
                notification.getPosition().getClientName());
    }
}
