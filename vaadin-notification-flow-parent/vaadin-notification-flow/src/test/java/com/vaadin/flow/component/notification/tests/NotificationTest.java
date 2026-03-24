/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class NotificationTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Notification notification;

    @Test
    void stringAndDurationCtor() {
        notification = new Notification("foo", 4000);
        Assertions.assertEquals(4000, notification.getDuration(), 0);
        Assertions.assertEquals("bottom-start",
                notification.getPosition().getClientName());
    }

    @Test
    void stringDurAndPositionCtor() {
        notification = new Notification("fooo", 10000, Position.TOP_END);
        Assertions.assertEquals(10000, notification.getDuration(), 0);
        Assertions.assertEquals("top-end",
                notification.getPosition().getClientName());
    }

    @Test
    void stringDurationPositionAndAssertiveCtor() {
        notification = new Notification("fooo", 10000, Position.TOP_END, true);
        Assertions.assertEquals(10000, notification.getDuration(), 0);
        Assertions.assertEquals("top-end",
                notification.getPosition().getClientName());
        Assertions.assertTrue(notification.isAssertive());
    }

    @Test
    void componentCtor() {
        notification = new Notification(new Span(), new NativeButton());

        notification.setPosition(Position.BOTTOM_END);
        Assertions.assertEquals("bottom-end",
                notification.getPosition().getClientName());
    }

    @Test
    void staticCtor() {
        notification = Notification.show("fooooo", 4000, Position.BOTTOM_CENTER,
                true);
        Assertions.assertEquals("bottom-center",
                notification.getPosition().getClientName());
        Assertions.assertTrue(notification.isAssertive());
    }

    @Test
    void setPositon() {
        notification = new Notification();

        notification.setPosition(Position.BOTTOM_STRETCH);
        Assertions.assertEquals("bottom-stretch",
                notification.getPosition().getClientName());
    }
}
