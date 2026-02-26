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

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for MenuBar component.
 */
@Route(value = "menu-bar", layout = MainLayout.class)
@PageTitle("Menu Bar | Vaadin Kitchen Sink")
public class MenuBarDemoView extends VerticalLayout {

    public MenuBarDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Menu Bar Component"));
        add(new Paragraph("MenuBar provides a horizontal menu with dropdown submenus."));

        // Basic menu bar
        MenuBar basic = new MenuBar();
        basic.addItem("File", e -> Notification.show("File clicked"));
        basic.addItem("Edit", e -> Notification.show("Edit clicked"));
        basic.addItem("View", e -> Notification.show("View clicked"));
        basic.addItem("Help", e -> Notification.show("Help clicked"));
        addSection("Basic Menu Bar", basic);

        // With submenus
        MenuBar withSubmenus = new MenuBar();

        MenuItem file = withSubmenus.addItem("File");
        SubMenu fileMenu = file.getSubMenu();
        fileMenu.addItem("New", e -> Notification.show("New"));
        fileMenu.addItem("Open", e -> Notification.show("Open"));
        fileMenu.addItem("Save", e -> Notification.show("Save"));
        fileMenu.addSeparator();
        fileMenu.addItem("Exit", e -> Notification.show("Exit"));

        MenuItem edit = withSubmenus.addItem("Edit");
        SubMenu editMenu = edit.getSubMenu();
        editMenu.addItem("Cut", e -> Notification.show("Cut"));
        editMenu.addItem("Copy", e -> Notification.show("Copy"));
        editMenu.addItem("Paste", e -> Notification.show("Paste"));

        MenuItem view = withSubmenus.addItem("View");
        SubMenu viewMenu = view.getSubMenu();
        viewMenu.addItem("Zoom In", e -> Notification.show("Zoom In"));
        viewMenu.addItem("Zoom Out", e -> Notification.show("Zoom Out"));
        viewMenu.addItem("Reset Zoom", e -> Notification.show("Reset Zoom"));

        addSection("With Dropdown Submenus", withSubmenus);

        // With icons
        MenuBar withIcons = new MenuBar();

        MenuItem fileIcon = withIcons.addItem(VaadinIcon.FILE.create());
        SubMenu fileIconMenu = fileIcon.getSubMenu();
        fileIconMenu.addItem(new Span(VaadinIcon.FILE_ADD.create(), new Span("New")), e -> Notification.show("New"));
        fileIconMenu.addItem(new Span(VaadinIcon.FOLDER_OPEN.create(), new Span("Open")), e -> Notification.show("Open"));
        fileIconMenu.addItem(new Span(VaadinIcon.DOWNLOAD.create(), new Span("Save")), e -> Notification.show("Save"));

        MenuItem editIcon = withIcons.addItem(VaadinIcon.EDIT.create());
        SubMenu editIconMenu = editIcon.getSubMenu();
        editIconMenu.addItem(new Span(VaadinIcon.SCISSORS.create(), new Span("Cut")), e -> Notification.show("Cut"));
        editIconMenu.addItem(new Span(VaadinIcon.COPY.create(), new Span("Copy")), e -> Notification.show("Copy"));
        editIconMenu.addItem(new Span(VaadinIcon.PASTE.create(), new Span("Paste")), e -> Notification.show("Paste"));

        withIcons.addItem(VaadinIcon.COG.create(), e -> Notification.show("Settings"));

        addSection("Icon-only Items", withIcons);

        // Primary variant
        MenuBar primary = new MenuBar();
        primary.addThemeVariants(MenuBarVariant.LUMO_PRIMARY);
        primary.addItem("Save", e -> Notification.show("Saved!"));
        primary.addItem("Cancel", e -> Notification.show("Cancelled"));
        addSection("Primary Variant", primary);

        // Tertiary variant
        MenuBar tertiary = new MenuBar();
        tertiary.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
        tertiary.addItem("Option 1", e -> Notification.show("Option 1"));
        tertiary.addItem("Option 2", e -> Notification.show("Option 2"));
        tertiary.addItem("Option 3", e -> Notification.show("Option 3"));
        addSection("Tertiary Variant", tertiary);

        // Small variant
        MenuBar small = new MenuBar();
        small.addThemeVariants(MenuBarVariant.LUMO_SMALL);
        small.addItem("Small 1", e -> Notification.show("Small 1"));
        small.addItem("Small 2", e -> Notification.show("Small 2"));
        small.addItem("Small 3", e -> Notification.show("Small 3"));
        addSection("Small Variant", small);

        // Overflow menu
        MenuBar overflow = new MenuBar();
        overflow.setWidth("300px");
        for (int i = 1; i <= 10; i++) {
            int index = i;
            overflow.addItem("Item " + i, e -> Notification.show("Item " + index));
        }
        addSection("With Overflow Menu", overflow);

        // Nested submenus
        MenuBar nested = new MenuBar();
        MenuItem parent = nested.addItem("Parent Menu");
        SubMenu parentSub = parent.getSubMenu();
        parentSub.addItem("Direct Item", e -> Notification.show("Direct"));

        MenuItem nestedItem = parentSub.addItem("Nested Menu");
        SubMenu nestedSub = nestedItem.getSubMenu();
        nestedSub.addItem("Level 2 Item 1", e -> Notification.show("Level 2-1"));
        nestedSub.addItem("Level 2 Item 2", e -> Notification.show("Level 2-2"));

        MenuItem deepNested = nestedSub.addItem("Even Deeper");
        SubMenu deepSub = deepNested.getSubMenu();
        deepSub.addItem("Level 3 Item", e -> Notification.show("Level 3"));

        addSection("Nested Submenus", nested);

        // With checkable items
        MenuBar checkable = new MenuBar();
        MenuItem settings = checkable.addItem("Settings");
        SubMenu settingsMenu = settings.getSubMenu();

        MenuItem darkMode = settingsMenu.addItem("Dark Mode", e ->
            Notification.show("Dark Mode: " + e.getSource().isChecked()));
        darkMode.setCheckable(true);

        MenuItem notifications = settingsMenu.addItem("Notifications", e ->
            Notification.show("Notifications: " + e.getSource().isChecked()));
        notifications.setCheckable(true);
        notifications.setChecked(true);

        addSection("Checkable Menu Items", checkable);
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
