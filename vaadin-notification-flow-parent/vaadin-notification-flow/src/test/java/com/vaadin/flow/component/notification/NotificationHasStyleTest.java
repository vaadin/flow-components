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
package com.vaadin.flow.component.notification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.shared.internal.OverlayClassListProxy;

class NotificationHasStyleTest {
    private final Notification notification = new Notification();

    @Test
    void addClassName_notificationHasOverlayClass() {
        notification.addClassName("foo");
        Assertions.assertEquals("foo",
                notification.getElement().getProperty("overlayClass"));

        notification.addClassName("bar");
        Assertions.assertEquals("foo bar",
                notification.getElement().getProperty("overlayClass"));
    }

    @Test
    void removeClassName_notificationHasOverlayClass() {
        notification.addClassName("foo");

        notification.removeClassName("foo");
        Assertions.assertEquals(null,
                notification.getElement().getProperty("overlayClass"));
    }

    @Test
    void setClassNameString_notificationHasOverlayClass() {
        notification.setClassName("foo");
        Assertions.assertEquals("foo",
                notification.getElement().getProperty("overlayClass"));

        notification.setClassName("bar");
        Assertions.assertEquals("bar",
                notification.getElement().getProperty("overlayClass"));
    }

    @Test
    void setClassNameBoolean_notificationHasOverlayClass() {
        notification.setClassName("foo", true);

        notification.setClassName("foo", false);

        Assertions.assertEquals(null,
                notification.getElement().getProperty("overlayClass"));
    }

    @Test
    void setClassNameMultiple_notificationHasOverlayClass() {
        notification.setClassName("foo bar");
        notification.getClassNames().set("foo", false);

        Assertions.assertEquals("bar",
                notification.getElement().getProperty("overlayClass"));
    }

    @Test
    void addClassNames_notificationHasOverlayClass() {
        notification.addClassNames("foo", "bar");
        Assertions.assertEquals("foo bar",
                notification.getElement().getProperty("overlayClass"));

        notification.addClassNames("baz", "qux");
        Assertions.assertEquals("foo bar baz qux",
                notification.getElement().getProperty("overlayClass"));
    }

    @Test
    void removeClassNames_notificationHasOverlayClass() {
        notification.addClassNames("foo", "bar", "baz", "qux");

        notification.removeClassNames("foo", "bar");
        Assertions.assertEquals("baz qux",
                notification.getElement().getProperty("overlayClass"));
    }

    @Test
    void getStyle_unsupported() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> notification.getStyle());
    }

    @Test
    void getClassNames_usesProxy() {
        Assertions.assertTrue(
                notification.getClassNames() instanceof OverlayClassListProxy);
    }
}
