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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.shared.internal.OverlayClassListProxy;

public class NotificationHasStyleTest {
    private final Notification notification = new Notification();

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
