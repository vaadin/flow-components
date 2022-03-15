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
package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * View for {@link ContextMenu} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-context-menu")
public class ContextMenuView extends Div {

    public ContextMenuView() {
        addBasicContextMenu();
        addContextMenuWithSubMenus();
        addCheckableMenuItems();
        addContextMenuWithComponents();
        addContextMenuWithComponentsInSubMenu();
    }

    private void addBasicContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        Component target = createTargetComponent();
        contextMenu.setTarget(target);

        Label message = new Label("-");

        contextMenu.addItem("First menu item",
                e -> message.setText("Clicked on the first item"));

        contextMenu.addItem("Second menu item",
                e -> message.setText("Clicked on the second item"));

        // The created MenuItem component can be saved for later use
        MenuItem item = contextMenu.addItem("Disabled menu item",
                e -> message.setText("This cannot happen"));
        item.setEnabled(false);

        addCard("Basic ContextMenu", target, message);
        target.setId("basic-context-menu-target");
        contextMenu.setId("basic-context-menu");
    }

    private void addContextMenuWithSubMenus() {
        ContextMenu contextMenu = new ContextMenu();

        Component target = createTargetComponent();
        contextMenu.setTarget(target);

        Label message = new Label("-");

        contextMenu.addItem("First menu item",
                event -> message.setText("Clicked on the first item"));

        MenuItem parent = contextMenu.addItem("Parent item");
        SubMenu subMenu = parent.getSubMenu();

        subMenu.addItem("Second menu item",
                event -> message.setText("Clicked on the second item"));

        subMenu = subMenu.addItem("Parent item").getSubMenu();
        subMenu.addItem("Third menu item",
                event -> message.setText("Clicked on the third item"));

        addCard("Hierarchical Menu", target, message);
        target.setId("hierarchical-menu-target");
        message.setId("hierarchical-menu-message");
    }

    private void addCheckableMenuItems() {
        ContextMenu contextMenu = new ContextMenu();

        Component target = createTargetComponent();
        contextMenu.setTarget(target);

        Label message = new Label("-");

        MenuItem item1 = contextMenu.addItem("Option 1", event -> {
            if (event.getSource().isChecked()) {
                message.setText("Selected option 1");
            } else {
                message.setText("Unselected option 1");
            }
        });
        item1.setCheckable(true);

        MenuItem item2 = contextMenu.addItem("Option 2", event -> {
            if (event.getSource().isChecked()) {
                message.setText("Selected option 2");
            } else {
                message.setText("Unselected option 2");
            }
        });
        item2.setCheckable(true);
        item2.setChecked(true);

        addCard("Checkable Menu Items", target, message);
        target.setId("checkable-menu-items-target");
        message.setId("checkable-menu-items-message");
    }

    private void addContextMenuWithComponents() {
        Component target = createTargetComponent();
        ContextMenu contextMenu = new ContextMenu(target);

        Label message = new Label("-");

        // Components can be used also inside menu items
        contextMenu.addItem(new H5("First menu item"),
                e -> message.setText("Clicked on the first item"));

        Checkbox checkbox = new Checkbox("Checkbox");
        contextMenu.addItem(checkbox, e -> message.setText(
                "Clicked on checkbox with value: " + checkbox.getValue()));

        // Components can also be added to the overlay
        // without creating menu items with add()
        Component separator = new Hr();
        contextMenu.add(separator, new Label("This is not a menu item"));

        addCard("ContextMenu With Components", target, message);
        target.setId("context-menu-with-components-target");
        contextMenu.setId("context-menu-with-components");
        message.setId("context-menu-with-components-message");
    }

    private void addContextMenuWithComponentsInSubMenu() {
        Component target = createTargetComponent();
        ContextMenu contextMenu = new ContextMenu(target);

        Label message = new Label("-");

        contextMenu.addItem(new H5("First menu item"),
                event -> message.setText("Clicked on the first item"));

        MenuItem subMenuItem = contextMenu.addItem("SubMenu Item");
        SubMenu subMenu = subMenuItem.getSubMenu();

        Checkbox checkbox = new Checkbox("Checkbox");
        subMenu.addItem(checkbox, event -> message.setText(
                "Clicked on checkbox with value: " + checkbox.getValue()));

        subMenu.addItem("TextItem",
                event -> message.setText("Clicked on text item"));

        // Components can also be added to the submenu overlay
        // without creating menu items with add()
        subMenu.addComponentAtIndex(1, new Hr());
        subMenu.add(new Label("This is not a menu item"));

        addCard("ContextMenu With Components in Sub Menu", target, message);
        target.setId("context-menu-with-submenu-components-target");
        contextMenu.setId("context-menu-with-submenu-components");
        message.setId("context-menu-with-submenu-components-message");
    }

    private Component createTargetComponent() {
        H2 header = new H2("Right click this component");
        Paragraph paragraph = new Paragraph("(or long touch on mobile)");
        Div div = new Div(header, paragraph);
        div.getStyle().set("border", "1px solid black").set("textAlign",
                "center");
        return div;
    }

    private void addCard(String title, Component... components) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }

}
