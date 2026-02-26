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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for Icons component.
 */
@Route(value = "icons", layout = MainLayout.class)
@PageTitle("Icons | Vaadin Kitchen Sink")
public class IconsDemoView extends VerticalLayout {

    public IconsDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Icons Component"));
        add(new Paragraph("Vaadin provides a comprehensive set of icons."));

        // Basic icons
        HorizontalLayout basic = new HorizontalLayout();
        basic.setSpacing(true);
        basic.setAlignItems(Alignment.CENTER);
        basic.add(
            VaadinIcon.HOME.create(),
            VaadinIcon.USER.create(),
            VaadinIcon.COG.create(),
            VaadinIcon.ENVELOPE.create(),
            VaadinIcon.BELL.create()
        );
        addSection("Basic Icons", basic);

        // Icon sizes
        HorizontalLayout sizes = new HorizontalLayout();
        sizes.setSpacing(true);
        sizes.setAlignItems(Alignment.CENTER);

        Icon small = VaadinIcon.STAR.create();
        small.setSize("16px");

        Icon medium = VaadinIcon.STAR.create();
        medium.setSize("24px");

        Icon large = VaadinIcon.STAR.create();
        large.setSize("36px");

        Icon xlarge = VaadinIcon.STAR.create();
        xlarge.setSize("48px");

        sizes.add(small, medium, large, xlarge);
        addSection("Icon Sizes", sizes);

        // Icon colors
        HorizontalLayout colors = new HorizontalLayout();
        colors.setSpacing(true);
        colors.setAlignItems(Alignment.CENTER);

        Icon primary = VaadinIcon.HEART.create();
        primary.setColor("var(--lumo-primary-color)");

        Icon success = VaadinIcon.CHECK.create();
        success.setColor("var(--lumo-success-color)");

        Icon error = VaadinIcon.CLOSE.create();
        error.setColor("var(--lumo-error-color)");

        Icon warning = VaadinIcon.WARNING.create();
        warning.setColor("orange");

        Icon custom = VaadinIcon.STAR.create();
        custom.setColor("#9c27b0");

        colors.add(primary, success, error, warning, custom);
        addSection("Icon Colors", colors);

        // Common UI icons
        FlexLayout uiIcons = new FlexLayout();
        uiIcons.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        uiIcons.getStyle().set("gap", "var(--lumo-space-m)");

        VaadinIcon[] commonIcons = {
            VaadinIcon.PLUS, VaadinIcon.MINUS, VaadinIcon.EDIT, VaadinIcon.TRASH,
            VaadinIcon.SEARCH, VaadinIcon.FILTER, VaadinIcon.DOWNLOAD, VaadinIcon.UPLOAD,
            VaadinIcon.REFRESH, VaadinIcon.COPY, VaadinIcon.PRINT, VaadinIcon.SHARE,
            VaadinIcon.LINK, VaadinIcon.UNLINK, VaadinIcon.LOCK, VaadinIcon.UNLOCK
        };

        for (VaadinIcon icon : commonIcons) {
            Div iconContainer = createIconWithLabel(icon);
            uiIcons.add(iconContainer);
        }
        addSection("Common UI Icons", uiIcons);

        // Navigation icons
        FlexLayout navIcons = new FlexLayout();
        navIcons.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        navIcons.getStyle().set("gap", "var(--lumo-space-m)");

        VaadinIcon[] navigationIcons = {
            VaadinIcon.HOME, VaadinIcon.MENU, VaadinIcon.ARROW_LEFT, VaadinIcon.ARROW_RIGHT,
            VaadinIcon.ARROW_UP, VaadinIcon.ARROW_DOWN, VaadinIcon.CHEVRON_LEFT, VaadinIcon.CHEVRON_RIGHT,
            VaadinIcon.ANGLE_LEFT, VaadinIcon.ANGLE_RIGHT, VaadinIcon.ANGLE_UP, VaadinIcon.ANGLE_DOWN
        };

        for (VaadinIcon icon : navigationIcons) {
            Div iconContainer = createIconWithLabel(icon);
            navIcons.add(iconContainer);
        }
        addSection("Navigation Icons", navIcons);

        // Communication icons
        FlexLayout commIcons = new FlexLayout();
        commIcons.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        commIcons.getStyle().set("gap", "var(--lumo-space-m)");

        VaadinIcon[] communicationIcons = {
            VaadinIcon.ENVELOPE, VaadinIcon.ENVELOPE_OPEN, VaadinIcon.PHONE, VaadinIcon.CHAT,
            VaadinIcon.COMMENT, VaadinIcon.COMMENTS, VaadinIcon.BELL, VaadinIcon.MEGAPHONE
        };

        for (VaadinIcon icon : communicationIcons) {
            Div iconContainer = createIconWithLabel(icon);
            commIcons.add(iconContainer);
        }
        addSection("Communication Icons", commIcons);

        // Status icons
        FlexLayout statusIcons = new FlexLayout();
        statusIcons.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        statusIcons.getStyle().set("gap", "var(--lumo-space-m)");

        VaadinIcon[] statusIconList = {
            VaadinIcon.CHECK, VaadinIcon.CHECK_CIRCLE, VaadinIcon.CLOSE, VaadinIcon.CLOSE_CIRCLE,
            VaadinIcon.WARNING, VaadinIcon.EXCLAMATION, VaadinIcon.INFO, VaadinIcon.INFO_CIRCLE,
            VaadinIcon.QUESTION, VaadinIcon.QUESTION_CIRCLE
        };

        for (VaadinIcon icon : statusIconList) {
            Div iconContainer = createIconWithLabel(icon);
            statusIcons.add(iconContainer);
        }
        addSection("Status Icons", statusIcons);
    }

    private Div createIconWithLabel(VaadinIcon icon) {
        Div container = new Div();
        container.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.AlignItems.CENTER, LumoUtility.Gap.XSMALL);

        Icon iconComponent = icon.create();
        iconComponent.setSize("24px");

        Span label = new Span(icon.name());
        label.addClassNames(LumoUtility.FontSize.XXSMALL, LumoUtility.TextColor.SECONDARY);

        container.add(iconComponent, label);
        container.setWidth("80px");
        return container;
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }
}
