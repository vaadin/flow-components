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

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
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
    }

    private void createBasicDemo() {
        // begin-source-example
        // source-example-heading: Basic MenuBar
        MenuBar menuBar = new MenuBar();

        MenuItem project = menuBar.addItem("Project");
        MenuItem account = menuBar.addItem("Account");
        MenuItem signOut = menuBar.addItem("Sign Out ");
        signOut.add(new Icon(VaadinIcon.SIGN_OUT));

        SubMenu projectSubMenu = project.getSubMenu();
        projectSubMenu.addItem("Dashboard");
        projectSubMenu.add(new Hr());
        MenuItem users = projectSubMenu.addItem("Users");
        MenuItem billing = projectSubMenu.addItem("Billing");

        SubMenu usersSubMenu = users.getSubMenu();
        usersSubMenu.addItem("List");
        usersSubMenu.addItem("Add");

        SubMenu billingSubMenu = billing.getSubMenu();
        billingSubMenu.addItem("Invoices");
        billingSubMenu.addItem("Balance Events");

        SubMenu accountSubMenu = account.getSubMenu();
        accountSubMenu.addItem("Edit profile");
        accountSubMenu.add(new Hr());
        accountSubMenu.addItem("Privacy Settings");
        accountSubMenu.addItem("Terms of Service");

        // end-source-example

        addCard("Basic MenuBar", menuBar);
    }

}
