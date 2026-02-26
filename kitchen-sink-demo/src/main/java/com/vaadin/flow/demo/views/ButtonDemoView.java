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
package com.vaadin.flow.demo.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for Button component.
 */
@Route(value = "button", layout = MainLayout.class)
@PageTitle("Button | Vaadin Kitchen Sink")
public class ButtonDemoView extends VerticalLayout {

    public ButtonDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Button Component"));
        add(new Paragraph("The Button component is used to trigger actions."));

        // Basic buttons
        addSection("Basic Buttons",
            new Button("Default Button", e -> Notification.show("Default clicked")),
            createButton("Primary", ButtonVariant.LUMO_PRIMARY),
            createButton("Tertiary", ButtonVariant.LUMO_TERTIARY),
            createButton("Tertiary Inline", ButtonVariant.LUMO_TERTIARY_INLINE)
        );

        // Theme variants
        addSection("Theme Variants",
            createButton("Success", ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS),
            createButton("Error", ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR),
            createButton("Contrast", ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST)
        );

        // Size variants
        addSection("Size Variants",
            createButton("Small", ButtonVariant.LUMO_SMALL),
            new Button("Medium (default)", e -> Notification.show("Medium clicked")),
            createButton("Large", ButtonVariant.LUMO_LARGE)
        );

        // Buttons with icons
        Button iconLeft = new Button("Icon Left", new Icon(VaadinIcon.PLUS));
        iconLeft.addClickListener(e -> Notification.show("Icon left clicked"));

        Button iconRight = new Button("Icon Right", new Icon(VaadinIcon.ARROW_RIGHT));
        iconRight.setIconAfterText(true);
        iconRight.addClickListener(e -> Notification.show("Icon right clicked"));

        Button iconOnly = new Button(new Icon(VaadinIcon.HEART));
        iconOnly.addThemeVariants(ButtonVariant.LUMO_ICON);
        iconOnly.setAriaLabel("Like");
        iconOnly.addClickListener(e -> Notification.show("Heart clicked"));

        addSection("Buttons with Icons", iconLeft, iconRight, iconOnly);

        // Disabled buttons
        Button disabled = new Button("Disabled");
        disabled.setEnabled(false);

        Button disabledPrimary = new Button("Disabled Primary");
        disabledPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        disabledPrimary.setEnabled(false);

        addSection("Disabled Buttons", disabled, disabledPrimary);

        // Disable on click
        Button disableOnClick = new Button("Click to disable", e -> {
            Notification.show("Button will be disabled for 2 seconds");
            e.getSource().setEnabled(false);
            e.getSource().getUI().ifPresent(ui ->
                ui.access(() -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                    e.getSource().setEnabled(true);
                })
            );
        });
        disableOnClick.setDisableOnClick(true);

        addSection("Disable on Click", disableOnClick);
    }

    private Button createButton(String text, ButtonVariant... variants) {
        Button button = new Button(text, e -> Notification.show(text + " clicked"));
        button.addThemeVariants(variants);
        return button;
    }

    private void addSection(String title, Button... buttons) {
        Div section = new Div();
        section.add(new H2(title));
        HorizontalLayout layout = new HorizontalLayout(buttons);
        layout.setSpacing(true);
        layout.setAlignItems(Alignment.CENTER);
        section.add(layout);
        add(section);
    }
}
