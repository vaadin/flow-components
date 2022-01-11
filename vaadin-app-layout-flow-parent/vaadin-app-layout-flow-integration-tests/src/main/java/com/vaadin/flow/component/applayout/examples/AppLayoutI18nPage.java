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
package com.vaadin.flow.component.applayout.examples;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.AppLayout.AppLayoutI18n;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-app-layout/i18n")
public class AppLayoutI18nPage extends Div {
    public AppLayoutI18nPage() {
        AppLayout layout = new AppLayout();
        add(layout);

        NativeButton toggleAttachedButton = new NativeButton("toggle attached",
                e -> {
                    if (layout.getParent().isPresent()) {
                        remove(layout);
                    } else {
                        add(layout);
                    }
                });
        toggleAttachedButton.setId("toggle-attached");

        NativeButton setI18nButton = new NativeButton("set i18n", e -> {
            AppLayoutI18n i18n = new AppLayout.AppLayoutI18n()
                    .setDrawer("Custom drawer");
            layout.setI18n(i18n);
        });
        setI18nButton.setId("set-i18n");

        NativeButton setEmptyI18nButton = new NativeButton("set empty i18n",
                e -> {
                    AppLayoutI18n i18n = new AppLayout.AppLayoutI18n();
                    layout.setI18n(i18n);
                });
        setEmptyI18nButton.setId("set-empty-i18n");

        add(setI18nButton, setEmptyI18nButton, toggleAttachedButton);
    }
}
