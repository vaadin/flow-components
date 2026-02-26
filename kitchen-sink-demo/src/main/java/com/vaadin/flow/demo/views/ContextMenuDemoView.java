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

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for ContextMenu component.
 */
@Route(value = "context-menu", layout = MainLayout.class)
@PageTitle("Context Menu | Vaadin Kitchen Sink")
public class ContextMenuDemoView extends VerticalLayout {

    public ContextMenuDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Context Menu Component"));
        add(new Paragraph("ContextMenu provides a right-click menu for any component."));

        // Basic context menu
        Div basicTarget = createTarget("Right-click me for basic menu");
        ContextMenu basicMenu = new ContextMenu(basicTarget);
        basicMenu.addItem("Option 1", e -> Notification.show("Option 1 clicked"));
        basicMenu.addItem("Option 2", e -> Notification.show("Option 2 clicked"));
        basicMenu.addItem("Option 3", e -> Notification.show("Option 3 clicked"));
        addSection("Basic Context Menu", basicTarget);

        // With icons
        Div iconTarget = createTarget("Right-click me for menu with icons");
        ContextMenu iconMenu = new ContextMenu(iconTarget);
        iconMenu.addItem(new Span(VaadinIcon.EDIT.create(), new Span("Edit")), e -> Notification.show("Edit"));
        iconMenu.addItem(new Span(VaadinIcon.COPY.create(), new Span("Copy")), e -> Notification.show("Copy"));
        iconMenu.addItem(new Span(VaadinIcon.TRASH.create(), new Span("Delete")), e -> Notification.show("Delete"));
        addSection("With Icons", iconTarget);

        // With separator
        Div sepTarget = createTarget("Right-click me for menu with separator");
        ContextMenu sepMenu = new ContextMenu(sepTarget);
        sepMenu.addItem("Cut", e -> Notification.show("Cut"));
        sepMenu.addItem("Copy", e -> Notification.show("Copy"));
        sepMenu.addItem("Paste", e -> Notification.show("Paste"));
        sepMenu.addSeparator();
        sepMenu.addItem("Select All", e -> Notification.show("Select All"));
        addSection("With Separator", sepTarget);

        // With submenu
        Div subTarget = createTarget("Right-click me for menu with submenu");
        ContextMenu subMenu = new ContextMenu(subTarget);
        subMenu.addItem("Open", e -> Notification.show("Open"));
        subMenu.addItem("Save", e -> Notification.show("Save"));

        MenuItem exportItem = subMenu.addItem("Export");
        SubMenu exportSubMenu = exportItem.getSubMenu();
        exportSubMenu.addItem("PDF", e -> Notification.show("Export PDF"));
        exportSubMenu.addItem("Excel", e -> Notification.show("Export Excel"));
        exportSubMenu.addItem("CSV", e -> Notification.show("Export CSV"));

        subMenu.addSeparator();
        subMenu.addItem("Close", e -> Notification.show("Close"));
        addSection("With Submenu", subTarget);

        // Checkable items
        Div checkTarget = createTarget("Right-click me for checkable items");
        ContextMenu checkMenu = new ContextMenu(checkTarget);
        MenuItem bold = checkMenu.addItem("Bold", e -> Notification.show("Bold: " + e.getSource().isChecked()));
        bold.setCheckable(true);
        MenuItem italic = checkMenu.addItem("Italic", e -> Notification.show("Italic: " + e.getSource().isChecked()));
        italic.setCheckable(true);
        MenuItem underline = checkMenu.addItem("Underline", e -> Notification.show("Underline: " + e.getSource().isChecked()));
        underline.setCheckable(true);
        addSection("Checkable Items", checkTarget);

        // Disabled items
        Div disabledTarget = createTarget("Right-click me for menu with disabled items");
        ContextMenu disabledMenu = new ContextMenu(disabledTarget);
        disabledMenu.addItem("Available", e -> Notification.show("Available"));
        MenuItem unavailable = disabledMenu.addItem("Unavailable");
        unavailable.setEnabled(false);
        disabledMenu.addItem("Also Available", e -> Notification.show("Also Available"));
        addSection("With Disabled Items", disabledTarget);

        // Open on left click
        Div leftClickTarget = createTarget("Left-click me (instead of right-click)");
        ContextMenu leftClickMenu = new ContextMenu(leftClickTarget);
        leftClickMenu.setOpenOnClick(true);
        leftClickMenu.addItem("Option A", e -> Notification.show("Option A"));
        leftClickMenu.addItem("Option B", e -> Notification.show("Option B"));
        leftClickMenu.addItem("Option C", e -> Notification.show("Option C"));
        addSection("Open on Left Click", leftClickTarget);

        // File browser example
        Div fileTarget = createTarget("Right-click me (File Browser style)");
        ContextMenu fileMenu = new ContextMenu(fileTarget);
        fileMenu.addItem(new Span(VaadinIcon.FILE_ADD.create(), new Span("New File")), e -> Notification.show("New File"));
        fileMenu.addItem(new Span(VaadinIcon.FOLDER_ADD.create(), new Span("New Folder")), e -> Notification.show("New Folder"));
        fileMenu.addSeparator();
        fileMenu.addItem(new Span(VaadinIcon.SCISSORS.create(), new Span("Cut")), e -> Notification.show("Cut"));
        fileMenu.addItem(new Span(VaadinIcon.COPY.create(), new Span("Copy")), e -> Notification.show("Copy"));
        fileMenu.addItem(new Span(VaadinIcon.PASTE.create(), new Span("Paste")), e -> Notification.show("Paste"));
        fileMenu.addSeparator();
        fileMenu.addItem(new Span(VaadinIcon.TRASH.create(), new Span("Delete")), e -> Notification.show("Delete"));
        fileMenu.addItem(new Span(VaadinIcon.EDIT.create(), new Span("Rename")), e -> Notification.show("Rename"));
        fileMenu.addSeparator();
        fileMenu.addItem(new Span(VaadinIcon.INFO_CIRCLE.create(), new Span("Properties")), e -> Notification.show("Properties"));
        addSection("File Browser Example", fileTarget);
    }

    private Div createTarget(String text) {
        Div target = new Div();
        target.setText(text);
        target.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.LARGE,
                LumoUtility.BorderRadius.MEDIUM, LumoUtility.TextAlignment.CENTER);
        target.setWidthFull();
        return target;
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
