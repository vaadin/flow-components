/*
 * Copyright 2000-2025 Vaadin Ltd.
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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("vaadin-notification/notification-opened-before-forwarding-source")
public class NotificationOpenedBeforeForwardingSourcePage extends Div
        implements BeforeEnterObserver {

    public NotificationOpenedBeforeForwardingSourcePage() {
        new Notification().open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.forwardTo(NotificationOpenedBeforeForwardingTargetPage.class);
    }
}
