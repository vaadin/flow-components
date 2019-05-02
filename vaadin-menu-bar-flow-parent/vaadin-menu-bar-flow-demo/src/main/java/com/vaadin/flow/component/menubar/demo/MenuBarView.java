/*
 * Copyright 2000-2019 Vaadin Ltd.
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
package com.vaadin.flow.component.menubar.demo;

import java.util.stream.Stream;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link MenuBar} demo.
 */
@Route("vaadin-menu-bar")
public class MenuBarView extends DemoView {

    @Override
    public void initView() {
        createBasicDemo();
        createOpenOnHover();
        createOverflowingButtons();
        createDisabledItems();
        createCheckableItems();
        createUsingComponents();
    }

    private void createBasicDemo() {
        // begin-source-example
        // source-example-heading: Menu Bar
        MenuBar menuBar = new MenuBar();
        Text selected = new Text("");
        Div message = new Div(new Text("Selected: "), selected);

        MenuItem project = menuBar.addItem("Project");
        MenuItem account = menuBar.addItem("Account");
        menuBar.addItem("Sign Out", e -> selected.setText("Sign Out"));

        SubMenu projectSubMenu = project.getSubMenu();
        MenuItem users = projectSubMenu.addItem("Users");
        MenuItem billing = projectSubMenu.addItem("Billing");

        SubMenu usersSubMenu = users.getSubMenu();
        usersSubMenu.addItem("List", e -> selected.setText("List"));
        usersSubMenu.addItem("Add", e -> selected.setText("Add"));

        SubMenu billingSubMenu = billing.getSubMenu();
        billingSubMenu.addItem("Invoices", e -> selected.setText("Invoices"));
        billingSubMenu.addItem("Balance Events",
                e -> selected.setText("Balance Events"));

        account.getSubMenu().addItem("Edit Profile",
                e -> selected.setText("Edit Profile"));
        account.getSubMenu().addItem("Privacy Settings",
                e -> selected.setText("Privacy Settings"));

        // end-source-example

        addCard("Menu Bar", menuBar, message);
    }

    private void createOpenOnHover() {
        // begin-source-example
        // source-example-heading: Open on Hover
        MenuBar menuBar = new MenuBar();

        menuBar.setOpenOnHover(true);

        Text selected = new Text("");
        Div message = new Div(new Text("Selected: "), selected);

        MenuItem project = menuBar.addItem("Project");
        MenuItem account = menuBar.addItem("Account");
        menuBar.addItem("Sign Out", e -> selected.setText("Sign Out"));

        SubMenu projectSubMenu = project.getSubMenu();
        MenuItem users = projectSubMenu.addItem("Users");
        MenuItem billing = projectSubMenu.addItem("Billing");

        SubMenu usersSubMenu = users.getSubMenu();
        usersSubMenu.addItem("List", e -> selected.setText("List"));
        usersSubMenu.addItem("Add", e -> selected.setText("Add"));

        SubMenu billingSubMenu = billing.getSubMenu();
        billingSubMenu.addItem("Invoices", e -> selected.setText("Invoices"));
        billingSubMenu.addItem("Balance Events",
                e -> selected.setText("Balance Events"));

        account.getSubMenu().addItem("Edit Profile",
                e -> selected.setText("Edit Profile"));
        account.getSubMenu().addItem("Privacy Settings",
                e -> selected.setText("Privacy Settings"));

        // end-source-example

        addCard("Open on Hover", menuBar, message);
    }

    private void createOverflowingButtons() {
        // begin-source-example
        // source-example-heading: Overflowing Buttons
        MenuBar menuBar = new MenuBar();
        Stream.of("Home", "Dashboard", "Content", "Structure", "Appearance",
                "Modules", "Users", "Configuration", "Reports", "Help")
                .forEach(menuBar::addItem);
        // end-source-example

        addCard("Overflowing Buttons", menuBar);
    }

    private void createDisabledItems() {
        // begin-source-example
        // source-example-heading: Disabled Items
        MenuBar menuBar = new MenuBar();

        Text selected = new Text("");
        Div message = new Div(new Text("Selected: "), selected);

        MenuItem project = menuBar.addItem("Project");
        MenuItem permissions = menuBar.addItem("Permissions",
                e -> selected.setText("Permissions"));
        menuBar.addItem("Help", e -> selected.setText("Help"));

        project.getSubMenu().addItem("Edit", e -> selected.setText("Edit"));
        MenuItem delete = project.getSubMenu().addItem("Delete",
                e -> selected.setText("Delete"));

        permissions.setEnabled(false);
        delete.setEnabled(false);

        Checkbox adminCheckbox = new Checkbox("View as admin");
        adminCheckbox.addValueChangeListener(e -> {
            permissions.setEnabled(e.getValue());
            delete.setEnabled(e.getValue());
        });

        // end-source-example

        addCard("Items", "Disabled Items", menuBar, message, adminCheckbox);
    }

    private void createCheckableItems() {
        // begin-source-example
        // source-example-heading: Checkable Items
        MenuBar menuBar = new MenuBar();

        Text selected = new Text("");
        Div message = new Div(new Text("Selected: "), selected);

        MenuItem development = menuBar.addItem("Development");
        MenuItem optimizations = menuBar.addItem("Optimizations");
        menuBar.addItem("Help", e -> selected.setText("Help"));

        MenuItem debug = development.getSubMenu().addItem("Debug",
                e -> selected.setText("Debug"
                        + (e.getSource().isChecked() ? " (On)" : " (Off)")));
        debug.setCheckable(true);
        MenuItem logging = development.getSubMenu().addItem("Logging",
                e -> selected.setText("Logging "
                        + (e.getSource().isChecked() ? " (On)" : " (Off)")));
        logging.setCheckable(true);
        logging.setChecked(true);

        Stream.of("Compression", "Caching").forEach(option -> optimizations
                .getSubMenu()
                .addItem(option, e -> selected.setText(option
                        + (e.getSource().isChecked() ? " (On)" : " (Off)")))
                .setCheckable(true));

        // end-source-example

        addCard("Items", "Checkable Items", menuBar, message);
    }

    private void createUsingComponents() {
        // begin-source-example
        // source-example-heading: Item Components
        MenuBar menuBar = new MenuBar();

        Text selected = new Text("");
        Div message = new Div(new Text("Selected: "), selected);

        menuBar.addItem(new Html("<b>Home</b>"), e -> selected.setText("Home"));

        MenuItem profile = menuBar.addItem("Profile");
        profile.addComponentAsFirst(new Icon(VaadinIcon.USER));

        profile.getSubMenu().addItem("Edit Profile",
                e -> selected.setText("Edit Profile"));

        // Components can be added to submenus as well:
        profile.getSubMenu().add(new Hr());

        profile.getSubMenu().addItem("Privacy Settings",
                e -> selected.setText("Privacy Settings"));
        profile.getSubMenu().addItem("Terms of Service",
                e -> selected.setText("Terms of Service"));

        MenuItem item = menuBar.addItem(new Icon(VaadinIcon.BELL));
        item.getSubMenu().addItem("Notifications",
                e -> selected.setText("Notifications"));
        item.getSubMenu().addItem("Mark as Read",
                e -> selected.setText("Mark as Read"));
        // end-source-example

        addCard("Items", "Item Components", menuBar, message);
    }

}
