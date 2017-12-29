/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.HorizontalAlign;
import com.vaadin.flow.component.notification.Notification.VerticalAlign;


public class NotificationTest {

    private Notification notification;

    @Test
    public void stringAndDurationCtor() {
        notification = new Notification("foo", 4000);
        Assert.assertEquals(4000, notification.getDuration(), 0);
        Assert.assertEquals("bottom", notification.getVerticalAlign());
        Assert.assertEquals("start", notification.getHorizontalAlign());
    }

    @Test
    public void stringDurAndPositionCtor() {
        notification = new Notification("fooo", 10000, VerticalAlign.TOP,
                HorizontalAlign.END);
        Assert.assertEquals(10000, notification.getDuration(), 0);
        Assert.assertEquals("top", notification.getVerticalAlign());
        Assert.assertEquals("end", notification.getHorizontalAlign());
    }

    @Test
    public void componentCtor() {
        notification = new Notification(new Label(), new NativeButton());
        notification.setAlignment(VerticalAlign.BOTTOM, HorizontalAlign.END);

        Assert.assertEquals("bottom", notification.getVerticalAlign());
        Assert.assertEquals("end", notification.getHorizontalAlign());
    }

    @Test
    public void setAlignment() {
        notification = new Notification();

        notification.setHorizontalAlign(HorizontalAlign.CENTER);
        Assert.assertEquals("center", notification.getHorizontalAlign());

        notification.setVerticalAlign(VerticalAlign.TOP);
        Assert.assertEquals("top", notification.getVerticalAlign());

        notification.setAlignment(VerticalAlign.BOTTOM, HorizontalAlign.END);
        Assert.assertEquals("bottom", notification.getVerticalAlign());
        Assert.assertEquals("end", notification.getHorizontalAlign());

        notification.setAlignment(VerticalAlign.BOTTOM_STRETCH,
                HorizontalAlign.START);
        Assert.assertEquals("bottom-stretch", notification.getVerticalAlign());
        Assert.assertEquals("start", notification.getHorizontalAlign());
    }
}
