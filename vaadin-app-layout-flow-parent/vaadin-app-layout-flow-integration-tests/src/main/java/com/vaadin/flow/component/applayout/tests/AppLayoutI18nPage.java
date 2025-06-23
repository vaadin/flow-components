/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.applayout.tests;

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
