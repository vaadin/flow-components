/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("vaadin-notification/notificationtest")
public class NotificationTest extends Div implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        init();
    }

    public void init() {
        NativeButton btn = new NativeButton("clickme", listener -> {
            Notification notification = createNotification("123", "123", "123");
            notification.open();
        });
        btn.getStyle().set("position", "fixed");
        btn.getStyle().set("right", "366px");
        btn.getStyle().set("bottom", "100px");

        add(btn);

        Notification notification = createNotification("Hello", "Max",
                "Tomorrow");
        notification.open();

    }

    private Notification createNotification(String title, String decription,
            String timeline) {

        Div taskNotification = new Div();
        taskNotification.getStyle().set("min-width", "255px");
        Notification notification = new Notification(taskNotification);

        H4 hdr = new H4(title);
        hdr.getStyle().set("margin-top", "0");
        taskNotification.add(hdr);

        Paragraph p = new Paragraph();
        p.setText(decription);
        taskNotification.add(p);

        Paragraph timelineP = new Paragraph();
        timelineP.setText(timeline);
        taskNotification.add(timelineP);

        NativeButton btn = new NativeButton("OK");
        btn.getStyle().set("margin-right", "10px");
        btn.addClickListener(listener -> {
            notification.close();
        });
        taskNotification.add(btn);

        notification.setPosition(Position.BOTTOM_END);
        notification.setDuration(0);
        return notification;

    }

}
