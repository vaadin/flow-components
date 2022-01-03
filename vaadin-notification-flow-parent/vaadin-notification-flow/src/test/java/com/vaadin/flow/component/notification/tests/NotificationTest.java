/*
 * Copyright 2000-2022 Vaadin Ltd.
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
