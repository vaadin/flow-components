/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

@Route("vaadin-notification/nested")
public class NestedNotificationPage extends Div {

    public NestedNotificationPage() {
        var parent = new Notification();

        var child = new Notification();
        child.setText("Child text");

        parent.add(child);
        parent.add(new NativeButton("Open child", e -> child.open()));

        var openParentButton = new NativeButton("Open parent",
                e -> parent.open());
        openParentButton.setId("open");
        add(openParentButton);
    }
}
